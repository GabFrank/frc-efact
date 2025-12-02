package com.frcefact.controller;

import com.frcefact.dto.DocumentoElectronicoDto;
import com.frcefact.dto.EventoCancelacionDeDto;
import com.frcefact.dto.EventoInutilizacionDeDto;
import com.frcefact.dto.EventoNominacionDeDto;
import com.frcefact.dto.LoteDeDto;
import com.frcefact.dto.mapper.DocumentoElectronicoMapper;
import com.frcefact.model.Cliente;
import com.frcefact.model.DocumentoElectronico;
import com.frcefact.model.EventoInutilizacionDE;
import com.frcefact.model.EventoNominacionDE;
import com.frcefact.model.LoteDE;
import com.frcefact.model.Timbrado;
import com.frcefact.repository.ClienteRepository;
import com.frcefact.repository.TimbradoRepository;
import com.frcefact.service.sifen.SifenEventoService;
import com.frcefact.service.sifen.SifenService;
import com.roshka.sifen.core.exceptions.SifenException;
import com.roshka.sifen.core.types.TTiDE;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sifen")
@Tag(name = "SIFEN", description = "Operaciones de integración con SIFEN")
public class SifenController {

    private static final Logger log = LoggerFactory.getLogger(SifenController.class);

    private final SifenService sifenService;
    private final SifenEventoService sifenEventoService;
    private final DocumentoElectronicoMapper documentoElectronicoMapper;
    private final ClienteRepository clienteRepository;
    private final TimbradoRepository timbradoRepository;

    public SifenController(SifenService sifenService,
                           SifenEventoService sifenEventoService,
                           DocumentoElectronicoMapper documentoElectronicoMapper,
                           ClienteRepository clienteRepository,
                           TimbradoRepository timbradoRepository) {
        this.sifenService = sifenService;
        this.sifenEventoService = sifenEventoService;
        this.documentoElectronicoMapper = documentoElectronicoMapper;
        this.clienteRepository = clienteRepository;
        this.timbradoRepository = timbradoRepository;
    }

    @PostMapping("/lotes/{loteId}/enviar")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR')")
    @Operation(summary = "Enviar lote a SIFEN")
    public ResponseEntity<LoteDeDto> enviarLote(
            @Parameter(description = "ID del lote") @PathVariable Long loteId) {
        log.info("📤 Enviando lote {} a SIFEN", loteId);
        LoteDE lote = sifenService.enviarLote(loteId);
        return ResponseEntity.ok(LoteDeDto.fromEntity(lote));
    }

    @PostMapping("/lotes/{loteId}/consultar")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR')")
    @Operation(summary = "Consultar estado de lote en SIFEN")
    public ResponseEntity<LoteDeDto> consultarLote(
            @Parameter(description = "ID del lote") @PathVariable Long loteId) {
        log.info("🔍 Consultando lote {} en SIFEN", loteId);
        LoteDE lote = sifenService.consultarLote(loteId);
        return ResponseEntity.ok(LoteDeDto.fromEntity(lote));
    }

    @PostMapping("/documentos/{cdc}/consultar")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR')")
    @Operation(summary = "Consultar estado de documento en SIFEN")
    public ResponseEntity<DocumentoElectronicoDto> consultarDocumento(
            @Parameter(description = "CDC del documento") @PathVariable String cdc) {
        log.info("🔍 Consultando documento {} en SIFEN", cdc);
        DocumentoElectronico documento = sifenService.consultarDocumento(cdc);
        return ResponseEntity.ok(documentoElectronicoMapper.toDto(documento));
    }

    @PostMapping("/documentos/{cdc}/cancelar")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR')")
    @Operation(summary = "Registrar cancelación de documento en SIFEN")
    public ResponseEntity<EventoCancelacionDeDto> cancelarDocumento(
            @Parameter(description = "CDC del documento") @PathVariable String cdc,
            @RequestBody CancelarRequest request) throws SifenException {
        log.info("🚫 Registrando solicitud de cancelación para {}", cdc);
        var evento = sifenEventoService.cancelarDocumento(cdc, request.getMotivo());
        return ResponseEntity.ok(EventoCancelacionDeDto.fromEntity(evento));
    }

    @GetMapping("/documentos/factura/{facturaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR')")
    @Operation(summary = "Obtener documento electrónico por ID de factura")
    public ResponseEntity<DocumentoElectronicoDto> obtenerDocumentoPorFactura(
            @Parameter(description = "ID de la factura") @PathVariable Long facturaId) {
        log.info("🔍 Obteniendo documento electrónico para factura {}", facturaId);
        DocumentoElectronico documento = sifenService.obtenerDocumentoPorFacturaId(facturaId);
        return ResponseEntity.ok(documentoElectronicoMapper.toDto(documento));
    }

    @PostMapping("/documentos/{deId}/reenviar")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR')")
    @Operation(summary = "Reenviar un documento electrónico en un nuevo lote")
    public ResponseEntity<LoteDeDto> reenviarDEEnNuevoLote(
            @Parameter(description = "ID del documento electrónico") @PathVariable Long deId) {
        log.info("🔄 Reenviando DE ID: {} en un nuevo lote", deId);
        LoteDE lote = sifenService.reenviarDEEnNuevoLote(deId);
        return ResponseEntity.ok(LoteDeDto.fromEntity(lote));
    }

    @PostMapping("/documentos/{cdc}/nominar")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR')")
    @Operation(summary = "Nominar receptor de documento electrónico innominado")
    public ResponseEntity<EventoNominacionDeDto> nominarReceptor(
            @Parameter(description = "CDC del documento") @PathVariable String cdc,
            @RequestBody NominarRequest request) throws SifenException {
        log.info("👤 Registrando nominación de receptor para {}", cdc);
        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + request.getClienteId()));
        EventoNominacionDE evento = sifenEventoService.nominarReceptorDocumento(cdc, cliente);
        return ResponseEntity.ok(EventoNominacionDeDto.fromEntity(evento));
    }

    @PostMapping("/timbrados/{timbradoId}/inutilizar")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR')")
    @Operation(summary = "Inutilizar rango de números de documentos electrónicos")
    public ResponseEntity<EventoInutilizacionDeDto> inutilizarNumeros(
            @Parameter(description = "ID del timbrado") @PathVariable Long timbradoId,
            @RequestBody InutilizarRequest request) throws SifenException {
        log.info("📝 Registrando inutilización de números para timbrado {}", timbradoId);
        Timbrado timbrado = timbradoRepository.findById(timbradoId)
                .orElseThrow(() -> new IllegalArgumentException("Timbrado no encontrado con ID: " + timbradoId));
        TTiDE tipoDE = TTiDE.valueOf(request.getTipoDE());
        EventoInutilizacionDE evento = sifenEventoService.inutilizarNumerosDocumento(
                timbrado,
                request.getEstablecimiento(),
                request.getPuntoExpedicion(),
                request.getNumeroInicio(),
                request.getNumeroFin(),
                tipoDE,
                request.getMotivo(),
                request.getTimbradoDetalleId()
        );
        return ResponseEntity.ok(EventoInutilizacionDeDto.fromEntity(evento));
    }

    public static class CancelarRequest {
        private String motivo;

        public String getMotivo() {
            return motivo;
        }

        public void setMotivo(String motivo) {
            this.motivo = motivo;
        }
    }

    public static class NominarRequest {
        private Long clienteId;

        public Long getClienteId() {
            return clienteId;
        }

        public void setClienteId(Long clienteId) {
            this.clienteId = clienteId;
        }
    }

    public static class InutilizarRequest {
        private String establecimiento;
        private String puntoExpedicion;
        private Integer numeroInicio;
        private Integer numeroFin;
        private String tipoDE;
        private String motivo;
        private Long timbradoDetalleId;

        public String getEstablecimiento() {
            return establecimiento;
        }

        public void setEstablecimiento(String establecimiento) {
            this.establecimiento = establecimiento;
        }

        public String getPuntoExpedicion() {
            return puntoExpedicion;
        }

        public void setPuntoExpedicion(String puntoExpedicion) {
            this.puntoExpedicion = puntoExpedicion;
        }

        public Integer getNumeroInicio() {
            return numeroInicio;
        }

        public void setNumeroInicio(Integer numeroInicio) {
            this.numeroInicio = numeroInicio;
        }

        public Integer getNumeroFin() {
            return numeroFin;
        }

        public void setNumeroFin(Integer numeroFin) {
            this.numeroFin = numeroFin;
        }

        public String getTipoDE() {
            return tipoDE;
        }

        public void setTipoDE(String tipoDE) {
            this.tipoDE = tipoDE;
        }

        public String getMotivo() {
            return motivo;
        }

        public void setMotivo(String motivo) {
            this.motivo = motivo;
        }

        public Long getTimbradoDetalleId() {
            return timbradoDetalleId;
        }

        public void setTimbradoDetalleId(Long timbradoDetalleId) {
            this.timbradoDetalleId = timbradoDetalleId;
        }
    }
}

