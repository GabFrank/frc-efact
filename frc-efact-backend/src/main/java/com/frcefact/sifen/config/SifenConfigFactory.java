package com.frcefact.sifen.config;

import com.frcefact.exception.BusinessException;
import com.frcefact.model.Empresa;
import com.frcefact.model.Timbrado;
import com.frcefact.repository.EmpresaRepository;
import com.frcefact.repository.TimbradoRepository;
import com.frcefact.service.CertificadoService;
import com.frcefact.service.EncryptionService;
import com.roshka.sifen.Sifen;
import com.roshka.sifen.core.SifenConfig;
import com.roshka.sifen.core.SifenConfig.TipoAmbiente;
import com.roshka.sifen.core.SifenConfig.TipoCertificadoCliente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;

/**
 * Fábrica de configuraciones dinámicas para jsifenlib.
 * 
 * IMPORTANTE - Multi-tenancy:
 * A diferencia del proyecto de referencia que configura SIFEN una sola vez al iniciar la app,
 * este proyecto necesita configurar SIFEN dinámicamente según la empresa/timbrado que está
 * realizando la acción, ya que cada empresa tiene su propio certificado y CSC.
 * 
 * Funcionamiento:
 * 1. jsifenlib usa un contexto global (`Sifen.setSifenConfig`) que es compartido por todos los hilos
 * 2. Para evitar conflictos entre empresas, usamos `synchronized` en `executeWithConfig`
 * 3. Cada operación configura temporalmente el contexto global, ejecuta la operación, y luego
 *    el siguiente hilo puede configurar su propio contexto
 * 
 * Uso:
 * - Construir config: `SifenConfig config = factory.buildForTimbrado(timbradoId)`
 * - Ejecutar operación: `factory.executeWithConfig(config, () -> Sifen.metodo(...))`
 * 
 * NOTA: Todas las llamadas a jsifenlib deben usar `executeWithConfig` para asegurar
 * que el contexto global esté configurado correctamente para la empresa correspondiente.
 */
@Service
@Transactional(readOnly = true)
public class SifenConfigFactory {

    private static final Logger log = LoggerFactory.getLogger(SifenConfigFactory.class);

    private final EmpresaRepository empresaRepository;
    private final TimbradoRepository timbradoRepository;
    private final EncryptionService encryptionService;
    private final CertificadoService certificadoService;
    private final SifenProperties sifenProperties;
    private final Object sifenLock = new Object();

    public SifenConfigFactory(
            EmpresaRepository empresaRepository,
            TimbradoRepository timbradoRepository,
            EncryptionService encryptionService,
            CertificadoService certificadoService,
            SifenProperties sifenProperties) {
        this.empresaRepository = empresaRepository;
        this.timbradoRepository = timbradoRepository;
        this.encryptionService = encryptionService;
        this.certificadoService = certificadoService;
        this.sifenProperties = sifenProperties;
    }

    /**
     * Construye una configuración de SIFEN para una empresa utilizando su CSC por defecto.
     *
     * @param empresaId identificador de la empresa
     * @return configuración lista para usar con jsifenlib
     */
    public SifenConfig buildForEmpresa(Long empresaId) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new BusinessException("Empresa no encontrada: " + empresaId));
        String cscId = (empresa.getCscId() != null && !empresa.getCscId().isBlank())
                ? empresa.getCscId()
                : "001"; // Valor por defecto si no está configurado
        return buildConfig(empresa, cscId, empresa.getCscEncrypted());
    }

    /**
     * Construye una configuración de SIFEN utilizando los datos de un timbrado específico.
     * Prioriza el CSC configurado en el timbrado; si no existe, reusa el CSC de la empresa.
     *
     * @param timbradoId identificador del timbrado
     * @return configuración lista para usar con jsifenlib
     */
    public SifenConfig buildForTimbrado(Long timbradoId) {
        Timbrado timbrado = timbradoRepository.findById(timbradoId)
                .orElseThrow(() -> new BusinessException("Timbrado no encontrado: " + timbradoId));
        Empresa empresa = timbrado.getEmpresa();

        String cscId = (timbrado.getCscId() != null && !timbrado.getCscId().isBlank())
                ? timbrado.getCscId()
                : (empresa.getCscId() != null && !empresa.getCscId().isBlank())
                    ? empresa.getCscId()
                    : "001"; // Valor por defecto si no está configurado
        String cscEncrypted = (timbrado.getCscEncrypted() != null && !timbrado.getCscEncrypted().isBlank())
                ? timbrado.getCscEncrypted()
                : empresa.getCscEncrypted();

        return buildConfig(empresa, cscId, cscEncrypted);
    }

    /**
     * Ejecuta de forma segura una operación con jsifenlib configurando el contexto global.
     * 
     * IMPORTANTE - Thread Safety:
     * jsifenlib usa un contexto global compartido (`Sifen.setSifenConfig`) que es accedido
     * por todos los hilos. Para evitar conflictos cuando múltiples empresas operan simultáneamente:
     * 
     * 1. Usamos `synchronized` para asegurar que solo un hilo configure el contexto a la vez
     * 2. Configuramos el contexto global antes de ejecutar la operación
     * 3. La operación se ejecuta dentro del contexto configurado
     * 4. Cuando termina, el siguiente hilo puede configurar su propio contexto
     * 
     * Esto garantiza que cada operación use el certificado y CSC correctos de su empresa,
     * incluso cuando múltiples empresas operan simultáneamente.
     * 
     * Ejemplo de uso:
     * <pre>
     * SifenConfig config = factory.buildForTimbrado(timbradoId);
     * RespuestaRecepcionLoteDE respuesta = factory.executeWithConfig(config, () -> {
     *     return Sifen.recepcionLoteDE(documentos);
     * });
     * </pre>
     *
     * @param config    configuración SIFEN específica de la empresa/timbrado
     * @param operation operación a ejecutar con jsifenlib
     * @param <T>       tipo de respuesta de la operación
     * @return resultado de la operación
     * @throws BusinessException si hay error en la ejecución
     */
    public <T> T executeWithConfig(SifenConfig config, SifenOperation<T> operation) {
        synchronized (sifenLock) {
            try {
                log.debug("🔧 Configurando contexto global SIFEN para operación (empresa: {})", 
                        config != null ? "configurada" : "null");
                Sifen.setSifenConfig(config);
                T result = operation.execute();
                log.debug("✅ Operación SIFEN completada exitosamente");
                return result;
            } catch (BusinessException e) {
                throw e;
            } catch (Exception e) {
                log.error("❌ Error al ejecutar operación con SIFEN", e);
                throw new BusinessException("Error en operación con SIFEN: " + e.getMessage());
            }
        }
    }

    private SifenConfig buildConfig(Empresa empresa, String cscId, String cscEncrypted) {
        if (empresa.getCertificadoPath() == null || empresa.getCertificadoPath().isBlank()) {
            throw new BusinessException("La empresa no tiene certificado digital configurado");
        }

        if (empresa.getCertificadoPasswordEncrypted() == null ||
            empresa.getCertificadoPasswordEncrypted().isBlank()) {
            throw new BusinessException("La empresa no tiene contraseña de certificado configurada");
        }

        if (cscId == null || cscId.isBlank()) {
            throw new BusinessException("La empresa/timbrado no tiene CSC ID configurado");
        }

        if (cscEncrypted == null || cscEncrypted.isBlank()) {
            throw new BusinessException("La empresa/timbrado no tiene CSC configurado");
        }

        String certificadoPassword = decrypt(empresa.getCertificadoPasswordEncrypted(), "contraseña del certificado");
        String csc = decrypt(cscEncrypted, "CSC del contribuyente");

        TipoAmbiente ambiente = resolveAmbiente(empresa.getSifenAmbiente());
        Path certificadoPath = certificadoService.obtenerPathAbsoluto(empresa.getCertificadoPath());

        log.debug("🔧 Construyendo configuración SIFEN para empresa {} (ambiente: {}, cscId: {})",
                empresa.getId(), ambiente, cscId);
        // TODO: Remover este log de info con el CSC explícito después de verificar
        log.info("🔍 CSC Desencriptado para SIFEN: [{}]", csc);

        SifenConfig config = new SifenConfig(
                ambiente,
                cscId,
                csc,
                TipoCertificadoCliente.PFX,
                certificadoPath.toString(),
                certificadoPassword
        );

        try {
            config.setHabilitarNotaTecnica13(sifenProperties.isHabilitarNotaTecnica13());
        } catch (NoSuchMethodError | UnsupportedOperationException e) {
            log.warn("⚠️ No se pudo establecer habilitarNotaTecnica13 a través de SifenConfig: {}", e.getMessage());
        }

        return config;
    }

    private TipoAmbiente resolveAmbiente(String ambienteRaw) {
        if (ambienteRaw == null || ambienteRaw.isBlank()) {
            return TipoAmbiente.DEV;
        }
        String ambienteNormalizado = ambienteRaw.trim().toUpperCase();
        
        // Mapear valores comunes a los valores del enum
        // La librería jsifenlib solo acepta DEV y PROD
        if ("PRODUCTION".equals(ambienteNormalizado) || "PROD".equals(ambienteNormalizado)) {
            return TipoAmbiente.PROD;
        } else if ("DEV".equals(ambienteNormalizado) || "DEVELOPMENT".equals(ambienteNormalizado) || "TEST".equals(ambienteNormalizado)) {
            return TipoAmbiente.DEV;
        }
        
        // Intentar parsear directamente
        try {
            return TipoAmbiente.valueOf(ambienteNormalizado);
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Ambiente SIFEN inválido '{}', usando DEV por defecto", ambienteRaw);
            return TipoAmbiente.DEV;
        }
    }

    private String decrypt(String encrypted, String description) {
        try {
            return encryptionService.decrypt(encrypted);
        } catch (Exception e) {
            throw new BusinessException("No se pudo desencriptar " + description + ": " + e.getMessage());
        }
    }

    @FunctionalInterface
    public interface SifenOperation<T> {
        T execute() throws Exception;
    }
}



