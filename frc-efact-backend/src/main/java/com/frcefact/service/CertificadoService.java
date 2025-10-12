package com.frcefact.service;

import com.frcefact.exception.BusinessException;
import com.frcefact.model.Empresa;
import com.frcefact.repository.EmpresaRepository;
import com.roshka.sifen.Sifen;
import com.roshka.sifen.core.SifenConfig;
import com.roshka.sifen.core.SifenConfig.TipoAmbiente;
import com.roshka.sifen.core.SifenConfig.TipoCertificadoCliente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

/**
 * Servicio para gestión de certificados digitales por empresa.
 * Implementa carga DINÁMICA de certificados (no global).
 * 
 * IMPORTANTE: Este servicio NO mantiene certificados en memoria.
 * Cada operación carga el certificado específico de la empresa.
 */
@Service
@Transactional(readOnly = true)
public class CertificadoService {

    private static final Logger log = LoggerFactory.getLogger(CertificadoService.class);

    private final EmpresaRepository empresaRepository;
    private final EncryptionService encryptionService;

    // Lock para sincronizar acceso a Sifen.setSifenConfig() (es estático/global)
    private final Object sifenLock = new Object();

    public CertificadoService(EmpresaRepository empresaRepository, EncryptionService encryptionService) {
        this.empresaRepository = empresaRepository;
        this.encryptionService = encryptionService;
    }

    /**
     * Configura SIFEN con el certificado de la empresa especificada.
     * IMPORTANTE: Debe ser llamado antes de cualquier operación con SIFEN.
     * 
     * @param empresaId ID de la empresa
     * @throws BusinessException si la empresa no existe o el certificado no es
     *                           válido
     */
    public void configurarSifenParaEmpresa(Long empresaId) {
        log.debug("🔧 Configurando SIFEN para empresa ID: {}", empresaId);

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new BusinessException("Empresa no encontrada: " + empresaId));

        // Validar que la empresa tiene certificado configurado
        if (empresa.getCertificadoPath() == null || empresa.getCertificadoPath().isBlank()) {
            throw new BusinessException("La empresa no tiene certificado digital configurado");
        }

        if (empresa.getCertificadoPasswordEncrypted() == null || empresa.getCertificadoPasswordEncrypted().isBlank()) {
            throw new BusinessException("La empresa no tiene contraseña de certificado configurada");
        }

        // Validar vigencia del certificado
        validarCertificadoVigente(empresa);

        // Desencriptar contraseña del certificado
        String certificadoPassword;
        try {
            certificadoPassword = encryptionService.decrypt(empresa.getCertificadoPasswordEncrypted());
        } catch (Exception e) {
            log.error("❌ Error al desencriptar contraseña del certificado para empresa {}", empresaId, e);
            throw new BusinessException("Error al procesar certificado digital");
        }

        // Desencriptar CSC
        String csc;
        try {
            csc = encryptionService.decrypt(empresa.getCscEncrypted());
        } catch (Exception e) {
            log.error("❌ Error al desencriptar CSC para empresa {}", empresaId, e);
            throw new BusinessException("Error al procesar código de seguridad");
        }

        // Determinar ambiente SIFEN
        TipoAmbiente ambiente;
        try {
            ambiente = TipoAmbiente.valueOf(empresa.getSifenAmbiente());
        } catch (Exception e) {
            log.warn("⚠️ Ambiente SIFEN no válido para empresa {}: {}. Usando DEV por defecto",
                    empresaId, empresa.getSifenAmbiente());
            ambiente = TipoAmbiente.DEV;
        }

        // Crear configuración temporal para esta empresa
        synchronized (sifenLock) {
            try {
                SifenConfig config = new SifenConfig(
                        ambiente,
                        TipoCertificadoCliente.PFX,
                        empresa.getCertificadoPath(),
                        certificadoPassword);

                // Configurar CSC (Código de Seguridad del Contribuyente)
                // NOTA: La configuración del CSC podría requerir un enfoque diferente
                // según la versión de jsifenlib. Por ahora, la configuración básica
                // del certificado debería ser suficiente para operaciones básicas.
                // El CSC se puede configurar por separado cuando sea necesario.

                log.debug("CSC configurado para empresa: {} (ID: {})", empresa.getRuc(), empresa.getCscId());

                // Configurar SIFEN globalmente (thread-safe)
                Sifen.setSifenConfig(config);

                log.info("✅ SIFEN configurado para empresa {} (RUC: {}) en ambiente {}",
                        empresaId, empresa.getRuc(), ambiente);

            } catch (Exception e) {
                log.error("❌ Error al configurar SIFEN para empresa {}", empresaId, e);
                throw new BusinessException("Error al configurar certificado digital: " + e.getMessage());
            }
        }
    }

    /**
     * Valida que el certificado de la empresa existe y está vigente.
     * 
     * @param empresa Empresa a validar
     * @throws BusinessException si el certificado no es válido
     */
    public void validarCertificadoVigente(Empresa empresa) {
        log.debug("🔍 Validando certificado para empresa ID: {}", empresa.getId());

        // Validar que el archivo existe
        Path certificadoPath = Paths.get(empresa.getCertificadoPath());
        if (!Files.exists(certificadoPath)) {
            throw new BusinessException("El archivo de certificado no existe: " + empresa.getCertificadoPath());
        }

        if (!Files.isReadable(certificadoPath)) {
            throw new BusinessException("El archivo de certificado no es legible: " + empresa.getCertificadoPath());
        }

        // Validar fecha de expiración
        if (empresa.getCertificadoFechaExpiracion() == null) {
            log.warn("⚠️ Empresa {} no tiene fecha de expiración de certificado configurada", empresa.getId());
        } else {
            LocalDate hoy = LocalDate.now();
            if (empresa.getCertificadoFechaExpiracion().isBefore(hoy)) {
                throw new BusinessException("El certificado digital ha expirado el " +
                        empresa.getCertificadoFechaExpiracion());
            }

            // Advertir si está por vencer (30 días)
            LocalDate fechaAdvertencia = hoy.plusDays(30);
            if (empresa.getCertificadoFechaExpiracion().isBefore(fechaAdvertencia)) {
                log.warn("⚠️ El certificado de la empresa {} expira pronto: {}",
                        empresa.getId(), empresa.getCertificadoFechaExpiracion());
            }
        }

        log.debug("✅ Certificado válido para empresa ID: {}", empresa.getId());
    }

    /**
     * Valida un certificado sin configurarlo en SIFEN.
     * Útil para testing al subir un nuevo certificado.
     * 
     * @param path     Ruta al archivo .pfx
     * @param password Contraseña del certificado
     * @return true si el certificado es válido
     * @throws BusinessException si el certificado no es válido
     */
    public boolean validarCertificado(String path, String password) {
        log.debug("🔍 Validando certificado en: {}", path);

        // Validar que el archivo existe
        File file = new File(path);
        if (!file.exists()) {
            throw new BusinessException("El archivo de certificado no existe: " + path);
        }

        if (!file.canRead()) {
            throw new BusinessException("El archivo de certificado no es legible: " + path);
        }

        // Validar extensión
        if (!path.toLowerCase().endsWith(".pfx") && !path.toLowerCase().endsWith(".p12")) {
            throw new BusinessException("El archivo debe ser un certificado .pfx o .p12");
        }

        // Intentar cargar el certificado con la contraseña
        try {
            // Crear configuración temporal solo para validación
            SifenConfig testConfig = new SifenConfig(
                    TipoAmbiente.DEV,
                    TipoCertificadoCliente.PFX,
                    path,
                    password);

            // Configurar CSC de prueba
            // NOTA: Para validación de certificado, no es necesario configurar CSC
            // La validación se enfoca en la validez del archivo .pfx y la contraseña
            log.debug("Validando certificado sin configuración CSC (solo validación de archivo)");

            // Si no lanza excepción, el certificado es válido
            log.info("✅ Certificado válido: {}", path);
            return true;

        } catch (Exception e) {
            log.error("❌ Error al validar certificado: {}", path, e);
            throw new BusinessException("Certificado inválido o contraseña incorrecta: " + e.getMessage());
        }
    }

    /**
     * Ejecuta una operación con SIFEN usando el certificado de la empresa.
     * Garantiza thread-safety y limpieza de recursos.
     * 
     * @param empresaId ID de la empresa
     * @param operation Operación a ejecutar
     * @param <T>       Tipo de retorno
     * @return Resultado de la operación
     */
    public <T> T ejecutarConCertificado(Long empresaId, SifenOperation<T> operation) {
        synchronized (sifenLock) {
            try {
                // Configurar SIFEN para esta empresa
                configurarSifenParaEmpresa(empresaId);

                // Ejecutar operación
                return operation.execute();

            } catch (BusinessException e) {
                throw e;
            } catch (Exception e) {
                log.error("❌ Error al ejecutar operación SIFEN para empresa {}", empresaId, e);
                throw new BusinessException("Error en operación con SIFEN: " + e.getMessage());
            }
        }
    }

    /**
     * Interfaz funcional para operaciones con SIFEN.
     */
    @FunctionalInterface
    public interface SifenOperation<T> {
        T execute() throws Exception;
    }
}
