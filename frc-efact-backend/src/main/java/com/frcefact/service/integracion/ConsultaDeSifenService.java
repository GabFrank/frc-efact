package com.frcefact.service.integracion;

import com.frcefact.dto.integracion.DeConsultadoDto;
import com.frcefact.security.apikey.ApiKeyPrincipal;
import com.frcefact.sifen.config.SifenConfigFactory;
import com.frcefact.sifen.util.DeSifenXmlParser;
import com.frcefact.util.RucParaguayo;
import com.roshka.sifen.Sifen;
import com.roshka.sifen.core.SifenConfig;
import com.roshka.sifen.core.beans.response.RespuestaConsultaDE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * Consulta de un DE por CDC directamente en SIFEN (modo consultor de la API de integración).
 *
 * <p>No guarda nada del DE consultado: ni el XML ni los datos. El DE puede ser de cualquier
 * emisor, no solo de empresas de eFact. Tampoco loguea el contenido, porque son datos de
 * terceros; solo CDC, código de respuesta y la key que consultó.
 *
 * <p>Certificado para el mTLS con SIFEN: el de la empresa de la cuenta si la tiene y tiene
 * certificado; si no, el de la empresa de plataforma ({@code integracion.empresa-plataforma-id}).
 * El ambiente (test/producción) es el de esa empresa: un CDC de producción no existe en test.
 */
@Service
public class ConsultaDeSifenService {

    private static final Logger log = LoggerFactory.getLogger(ConsultaDeSifenService.class);
    private static final Pattern CDC = Pattern.compile("\\d{44}");

    private final SifenConfigFactory sifenConfigFactory;
    private final Long empresaPlataformaId;

    public ConsultaDeSifenService(SifenConfigFactory sifenConfigFactory,
                                  @Value("${integracion.empresa-plataforma-id:#{null}}") Long empresaPlataformaId) {
        this.sifenConfigFactory = sifenConfigFactory;
        this.empresaPlataformaId = empresaPlataformaId;
    }

    /** DE consultado: el DTO completo (con ítems y XML) y el código de SIFEN para el registro. */
    public record ResultadoConsulta(DeConsultadoDto de, String codigoSifen) {
    }

    public ResultadoConsulta consultar(String cdc, ApiKeyPrincipal principal) {
        validarCdc(cdc);
        Long empresaCertificado = resolverEmpresaCertificado(principal);
        SifenConfig config;
        try {
            config = sifenConfigFactory.buildParaConsulta(empresaCertificado);
        } catch (RuntimeException e) {
            log.error("❌ No se pudo armar la config SIFEN de la empresa {}: {}", empresaCertificado, e.getMessage());
            throw new IntegracionException(HttpStatus.SERVICE_UNAVAILABLE, "SERVICIO_NO_CONFIGURADO",
                    "El servicio de consulta no está configurado para esta cuenta");
        }

        RespuestaConsultaDE respuesta;
        try {
            respuesta = sifenConfigFactory.executeWithConfig(config, () -> Sifen.consultaDE(cdc, config));
        } catch (RuntimeException e) {
            log.error("❌ Consulta a SIFEN falló: cdc={}, key={}, error={}", cdc, principal.prefijo(), e.getMessage());
            throw new IntegracionException(HttpStatus.BAD_GATEWAY, "SIFEN_NO_DISPONIBLE",
                    "No se pudo consultar SIFEN. Intente nuevamente más tarde.");
        }

        String raw = respuesta != null ? respuesta.getRespuestaBruta() : null;
        if (raw == null || raw.isBlank()) {
            throw new IntegracionException(HttpStatus.BAD_GATEWAY, "SIFEN_SIN_RESPUESTA",
                    "SIFEN no devolvió respuesta");
        }

        DeSifenXmlParser.RespuestaConsulta parseada;
        try {
            parseada = DeSifenXmlParser.parsearRespuestaConsulta(raw);
        } catch (IllegalArgumentException e) {
            log.error("❌ Respuesta de SIFEN ilegible: cdc={}, error={}", cdc, e.getMessage());
            throw new IntegracionException(HttpStatus.BAD_GATEWAY, "SIFEN_RESPUESTA_INVALIDA",
                    "La respuesta de SIFEN no se pudo interpretar");
        }

        log.info("🔍 Consulta DE por API: cdc={}, key={}, codigoSifen={}, encontrado={}",
                cdc, principal.prefijo(), parseada.codigo(), parseada.contenido().isPresent());

        if (parseada.contenido().isEmpty()) {
            throw new IntegracionException(HttpStatus.NOT_FOUND, "DE_NO_ENCONTRADO",
                    "SIFEN no devolvió el documento" + (parseada.mensaje() != null ? ": " + parseada.mensaje() : ""),
                    parseada.codigo());
        }

        try {
            DeConsultadoDto dto = DeSifenXmlParser.aDto(parseada.contenido().get(), parseada);
            return new ResultadoConsulta(dto, parseada.codigo());
        } catch (IllegalArgumentException e) {
            log.error("❌ DE devuelto por SIFEN ilegible: cdc={}, error={}", cdc, e.getMessage());
            throw new IntegracionException(HttpStatus.BAD_GATEWAY, "SIFEN_RESPUESTA_INVALIDA",
                    "El documento devuelto por SIFEN no se pudo interpretar", parseada.codigo());
        }
    }

    /** 44 dígitos y el último es el DV módulo 11 de los 43 anteriores. Evita gastar una consulta a SIFEN. */
    static void validarCdc(String cdc) {
        if (cdc == null || !CDC.matcher(cdc).matches()) {
            throw new IntegracionException(HttpStatus.BAD_REQUEST, "CDC_INVALIDO",
                    "El CDC debe tener exactamente 44 dígitos");
        }
        Integer dv = RucParaguayo.calcularDv(cdc.substring(0, 43));
        if (dv == null || dv != Character.getNumericValue(cdc.charAt(43))) {
            throw new IntegracionException(HttpStatus.BAD_REQUEST, "CDC_INVALIDO",
                    "El dígito verificador del CDC no es válido");
        }
    }

    private Long resolverEmpresaCertificado(ApiKeyPrincipal principal) {
        if (principal.empresaId() != null && sifenConfigFactory.tieneCertificado(principal.empresaId())) {
            return principal.empresaId();
        }
        if (empresaPlataformaId != null) {
            return empresaPlataformaId;
        }
        log.error("❌ Consulta sin certificado disponible: la cuenta {} no tiene empresa con certificado "
                + "y no está configurado integracion.empresa-plataforma-id", principal.cuentaServicioId());
        throw new IntegracionException(HttpStatus.SERVICE_UNAVAILABLE, "SERVICIO_NO_CONFIGURADO",
                "El servicio de consulta no está configurado para esta cuenta");
    }
}
