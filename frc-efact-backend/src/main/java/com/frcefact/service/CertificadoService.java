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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.ZoneId;

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
    private final Path certificatesDir;

    // Lock para sincronizar acceso a Sifen.setSifenConfig() (es estático/global)
    private final Object sifenLock = new Object();

    public CertificadoService(
            EmpresaRepository empresaRepository,
            EncryptionService encryptionService,
            @Value("${certificates.upload-dir:/var/certificates}") String uploadDir) {
        this.empresaRepository = empresaRepository;
        this.encryptionService = encryptionService;
        this.certificatesDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        
        // Crear directorio si no existe
        try {
            Files.createDirectories(this.certificatesDir);
            log.info("📁 Directorio de certificados: {}", this.certificatesDir);
        } catch (IOException e) {
            log.error("❌ Error al crear directorio de certificados: {}", this.certificatesDir, e);
            throw new RuntimeException("No se pudo crear el directorio de certificados", e);
        }
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

        // Nota: CSC ahora se maneja a nivel de timbrado específico, no a nivel de empresa
        // El CSC se configura cuando se crea o actualiza un timbrado electrónico específico

        // Determinar ambiente SIFEN
        TipoAmbiente ambiente;
        try {
            ambiente = TipoAmbiente.valueOf(empresa.getSifenAmbiente());
        } catch (Exception e) {
            log.warn("⚠️ Ambiente SIFEN no válido para empresa {}: {}. Usando DEV por defecto",
                    empresaId, empresa.getSifenAmbiente());
            ambiente = TipoAmbiente.DEV;
        }

        // Obtener path absoluto del certificado
        Path certificadoPathAbsoluto = obtenerPathAbsoluto(empresa.getCertificadoPath());

        // Crear configuración temporal para esta empresa
        synchronized (sifenLock) {
            try {
                SifenConfig config = new SifenConfig(
                        ambiente,
                        TipoCertificadoCliente.PFX,
                        certificadoPathAbsoluto.toString(),
                        certificadoPassword);

                // Configurar CSC (Código de Seguridad del Contribuyente)
                // NOTA: El CSC ahora se maneja a nivel de timbrado específico.
                // La configuración básica del certificado es suficiente para operaciones básicas.
                // El CSC se configura por separado cuando se procesa un timbrado electrónico específico.

                log.debug("Certificado configurado para empresa: {}", empresa.getRuc());

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

        // Obtener path absoluto
        Path certificadoPath = obtenerPathAbsoluto(empresa.getCertificadoPath());
        
        // Validar que el archivo existe
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
            // La construcción de SifenConfig valida automáticamente el certificado
            new SifenConfig(
                    TipoAmbiente.DEV,
                    TipoCertificadoCliente.PFX,
                    path,
                    password);

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
     * Resultado del guardado de certificado.
     */
    public static class CertificadoGuardado {
        private final String path;
        private final LocalDate fechaExpiracion;

        public CertificadoGuardado(String path, LocalDate fechaExpiracion) {
            this.path = path;
            this.fechaExpiracion = fechaExpiracion;
        }

        public String getPath() {
            return path;
        }

        public LocalDate getFechaExpiracion() {
            return fechaExpiracion;
        }
    }

    /**
     * Guarda un archivo de certificado PFX en el sistema de archivos.
     * 
     * @param empresaId ID de la empresa (para nombrar el archivo)
     * @param file Archivo PFX a guardar
     * @param password Contraseña del certificado (para validación)
     * @return Resultado con la ruta y fecha de expiración
     * @throws BusinessException si el archivo es inválido o hay error al guardarlo
     */
    @Transactional
    public CertificadoGuardado guardarCertificado(Long empresaId, MultipartFile file, String password) {
        log.info("💾 Guardando certificado para empresa ID: {}", empresaId);

        // Validar archivo
        if (file == null || file.isEmpty()) {
            throw new BusinessException("El archivo de certificado está vacío");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new BusinessException("El nombre del archivo es inválido");
        }

        // Validar extensión
        String lowerFilename = originalFilename.toLowerCase();
        if (!lowerFilename.endsWith(".pfx") && !lowerFilename.endsWith(".p12")) {
            throw new BusinessException("El archivo debe ser un certificado .pfx o .p12");
        }

        // Validar tamaño (máximo 5MB)
        long maxSize = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxSize) {
            throw new BusinessException("El archivo de certificado es demasiado grande. Máximo 5MB");
        }

        try {
            // Generar nombre único para el archivo: empresa_{id}_{timestamp}.pfx
            String extension = lowerFilename.endsWith(".pfx") ? ".pfx" : ".p12";
            String uniqueFilename = String.format("empresa_%d_%d%s", empresaId, System.currentTimeMillis(), extension);
            Path targetPath = certificatesDir.resolve(uniqueFilename);

            // Guardar archivo
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.debug("✅ Archivo guardado en: {}", targetPath);

            // Validar certificado con la contraseña
            validarCertificado(targetPath.toString(), password);

            // Intentar extraer fecha de expiración del certificado
            LocalDate fechaExpiracion = extraerFechaExpiracion(targetPath.toString(), password);
            if (fechaExpiracion != null) {
                log.info("📅 Fecha de expiración detectada: {}", fechaExpiracion);
            } else {
                log.warn("⚠️ No se pudo extraer la fecha de expiración del certificado");
            }

            // Retornar path relativo para almacenar en BD (formato: /certificates/empresa_123_...pfx)
            // Pero el sistema usará el path absoluto para acceder al archivo
            String relativePath = uniqueFilename; // Solo el nombre del archivo
            log.info("✅ Certificado guardado exitosamente en: {}", targetPath);
            log.info("📝 Path que se guardará en BD: {}", relativePath);
            
            return new CertificadoGuardado(relativePath, fechaExpiracion);

        } catch (IOException e) {
            log.error("❌ Error al guardar certificado", e);
            throw new BusinessException("Error al guardar el archivo de certificado: " + e.getMessage());
        } catch (BusinessException e) {
            // Re-lanzar excepciones de negocio
            throw e;
        } catch (Exception e) {
            log.error("❌ Error inesperado al guardar certificado", e);
            throw new BusinessException("Error al procesar el certificado: " + e.getMessage());
        }
    }

    /**
     * Extrae la fecha de expiración de un certificado PFX.
     * 
     * @param certificadoPath Ruta al archivo del certificado
     * @param password Contraseña del certificado
     * @return Fecha de expiración o null si no se puede extraer
     */
    private LocalDate extraerFechaExpiracion(String certificadoPath, String password) {
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            try (java.io.FileInputStream fis = new java.io.FileInputStream(certificadoPath)) {
                keyStore.load(fis, password.toCharArray());
            }

            String alias = keyStore.aliases().nextElement();
            X509Certificate cert = (X509Certificate) keyStore.getCertificate(alias);

            if (cert != null && cert.getNotAfter() != null) {
                return cert.getNotAfter().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
            }

        } catch (Exception e) {
            log.debug("No se pudo extraer fecha de expiración: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Convierte un path relativo de certificado a path absoluto.
     * 
     * @param certificadoPath Path relativo almacenado en BD (solo nombre del archivo)
     * @return Path absoluto al archivo
     */
    public Path obtenerPathAbsoluto(String certificadoPath) {
        if (certificadoPath == null || certificadoPath.isBlank()) {
            throw new BusinessException("El path del certificado está vacío");
        }
        
        // Si ya es absoluto, usarlo directamente
        if (Paths.get(certificadoPath).isAbsolute()) {
            return Paths.get(certificadoPath);
        }
        
        // Si es relativo, combinarlo con el directorio de certificados
        return certificatesDir.resolve(certificadoPath).normalize();
    }

    /**
     * Elimina el certificado anterior de una empresa si existe.
     * 
     * @param certificadoPath Ruta del certificado a eliminar (relativo o absoluto)
     */
    @Transactional
    public void eliminarCertificado(String certificadoPath) {
        if (certificadoPath == null || certificadoPath.isBlank()) {
            return;
        }

        try {
            Path filePath = obtenerPathAbsoluto(certificadoPath);
            
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("🗑️ Certificado eliminado: {}", filePath);
            }
        } catch (IOException e) {
            log.warn("⚠️ No se pudo eliminar el certificado anterior: {}", certificadoPath, e);
            // No lanzar excepción, solo loggear
        } catch (BusinessException e) {
            log.warn("⚠️ Path inválido al eliminar certificado: {}", certificadoPath);
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
