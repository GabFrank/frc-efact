# Consideraciones Multi-Empresa: Gestión de Certificados Digitales

## Contexto

Este documento describe las consideraciones críticas para implementar la gestión de certificados digitales en un sistema multi-empresa de facturación electrónica con integración a SIFEN Paraguay.

## Diferencias con Sistemas Single-Tenant

### Sistema Single-Tenant (Ejemplo de Referencia)
El repositorio de ejemplo (https://github.com/GabFrank/franco-system-backend-filial) maneja:
- **Una sola empresa**
- **Un solo certificado PFX** configurado globalmente
- Certificado cargado una vez al inicio de la aplicación
- KeyStore mantenido en memoria como singleton

### Nuestro Sistema Multi-Tenant
Debe manejar:
- **Múltiples empresas** (potencialmente cientos)
- **Un certificado PFX por empresa**
- Carga dinámica de certificados según la empresa que opera
- No mantener certificados en memoria (seguridad y escalabilidad)

## Arquitectura de Certificados

### Almacenamiento

Cada empresa tiene en la base de datos:

```sql
-- Tabla: empresa.empresa
certificado_path VARCHAR(500)                    -- Path al archivo .pfx
certificado_password_encrypted TEXT              -- Contraseña encriptada con AES-256
certificado_fecha_expiracion DATE                -- Fecha de expiración
```

### Modelo de Datos

```java
@Entity
public class Empresa {
    // ... otros campos
    
    private String certificadoPath;              // "/var/certs/empresa_123.pfx"
    private String certificadoPasswordEncrypted; // Encriptado con EncryptionService
    private LocalDate certificadoFechaExpiracion;
    
    public boolean isCertificadoVigente() {
        return certificadoFechaExpiracion != null && 
               certificadoFechaExpiracion.isAfter(LocalDate.now());
    }
}
```

## Flujo de Firma Digital

### ❌ Enfoque INCORRECTO (Single-Tenant)

```java
@Service
public class CertificadoService {
    
    // ❌ NO HACER ESTO - Certificado global
    private KeyStore keyStore;
    private PrivateKey privateKey;
    
    @PostConstruct
    public void init() {
        // Cargar certificado una vez al inicio
        this.keyStore = loadKeyStore(globalCertPath, globalPassword);
        this.privateKey = extractPrivateKey(keyStore);
    }
    
    public String firmarXml(String xml) {
        // Usar siempre el mismo certificado
        return sign(xml, this.privateKey);
    }
}
```

**Problemas**:
- Solo funciona para una empresa
- No puede manejar múltiples certificados
- Riesgo de seguridad (certificados en memoria permanentemente)

### ✅ Enfoque CORRECTO (Multi-Tenant)

```java
@Service
public class CertificadoService {
    
    @Autowired
    private EmpresaRepository empresaRepository;
    
    @Autowired
    private EncryptionService encryptionService;
    
    /**
     * Firma XML con el certificado de una empresa específica.
     * Carga el certificado dinámicamente y lo libera después.
     */
    public String firmarXml(String xml, Long empresaId) {
        // 1. Obtener empresa
        Empresa empresa = empresaRepository.findById(empresaId)
            .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada"));
        
        // 2. Validar certificado vigente
        validarCertificadoVigente(empresa);
        
        // 3. Desencriptar contraseña
        String password = encryptionService.decrypt(
            empresa.getCertificadoPasswordEncrypted()
        );
        
        KeyStore keyStore = null;
        try {
            // 4. Cargar certificado de la empresa
            keyStore = KeyStore.getInstance("PKCS12");
            try (FileInputStream fis = new FileInputStream(empresa.getCertificadoPath())) {
                keyStore.load(fis, password.toCharArray());
            }
            
            // 5. Obtener clave privada
            String alias = keyStore.aliases().nextElement();
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(
                alias, 
                password.toCharArray()
            );
            
            X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);
            
            // 6. Firmar XML
            String xmlFirmado = signXml(xml, privateKey, certificate);
            
            return xmlFirmado;
            
        } catch (Exception e) {
            throw new CertificadoInvalidoException(
                "Error al firmar XML con certificado de empresa " + empresaId, e
            );
        } finally {
            // 7. CRÍTICO: Limpiar datos sensibles de memoria
            password = null;
            keyStore = null;
            System.gc(); // Sugerir recolección de basura
        }
    }
    
    private void validarCertificadoVigente(Empresa empresa) {
        if (empresa.getCertificadoPath() == null) {
            throw new CertificadoInvalidoException(
                "La empresa no tiene certificado configurado"
            );
        }
        
        if (!empresa.isCertificadoVigente()) {
            throw new CertificadoInvalidoException(
                "El certificado ha expirado"
            );
        }
        
        File certFile = new File(empresa.getCertificadoPath());
        if (!certFile.exists()) {
            throw new CertificadoInvalidoException(
                "No se encuentra el archivo de certificado"
            );
        }
    }
}
```

**Ventajas**:
- ✅ Soporta múltiples empresas
- ✅ Carga dinámica de certificados
- ✅ Libera recursos después de cada uso
- ✅ Valida vigencia antes de usar
- ✅ Seguro (no mantiene certificados en memoria)

## Integración con SIFEN

### Servicio de Documentos Electrónicos

```java
@Service
public class DocumentoElectronicoService {
    
    @Autowired
    private CertificadoService certificadoService;
    
    @Autowired
    private XmlGeneratorService xmlGeneratorService;
    
    public DocumentoElectronico generarDE(FacturaLegal factura) {
        Empresa empresa = factura.getEmpresa();
        
        // 1. Validar certificado antes de empezar
        certificadoService.validarCertificadoVigente(empresa);
        
        // 2. Generar XML original
        String xmlOriginal = xmlGeneratorService.generarXmlOriginal(factura);
        
        // 3. Firmar con certificado de la empresa
        // IMPORTANTE: Pasar empresaId, no el certificado directamente
        String xmlFirmado = certificadoService.firmarXml(
            xmlOriginal, 
            empresa.getId()  // ← Carga dinámica del certificado
        );
        
        // 4. Generar CDC y QR
        String cdc = xmlGeneratorService.generarCDC(factura, xmlFirmado);
        String urlQr = xmlGeneratorService.generarUrlQr(cdc);
        
        // 5. Crear y guardar DE
        DocumentoElectronico de = new DocumentoElectronico();
        de.setFacturaLegal(factura);
        de.setXmlOriginal(xmlOriginal);
        de.setXmlFirmado(xmlFirmado);
        de.setCdc(cdc);
        de.setUrlQr(urlQr);
        de.setEstado(EstadoDE.PENDIENTE);
        
        return documentoElectronicoRepository.save(de);
    }
}
```

### Servicio SIFEN

```java
@Service
public class SifenService {
    
    @Autowired
    private CertificadoService certificadoService;
    
    /**
     * Envía un lote a SIFEN usando el certificado de la empresa.
     */
    public LoteResponse enviarLote(LoteDE lote, Long empresaId) {
        // Validar certificado de la empresa
        Empresa empresa = empresaRepository.findById(empresaId)
            .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada"));
        
        certificadoService.validarCertificadoVigente(empresa);
        
        try {
            // Configurar jsifenlib con certificado de la empresa
            SifenConfig config = SifenConfig.builder()
                .environment(environment)
                .apiUrl(sifenApiUrl)
                .certificadoPath(empresa.getCertificadoPath())
                .certificadoPassword(
                    encryptionService.decrypt(empresa.getCertificadoPasswordEncrypted())
                )
                .build();
            
            SifenClient client = new SifenClient(config);
            
            // Enviar lote
            return client.enviarLote(prepararLoteRequest(lote));
            
        } catch (Exception e) {
            throw new SifenIntegrationException("Error al enviar lote a SIFEN", e);
        }
    }
}
```

## Seguridad

### Encriptación de Contraseñas

```java
@Service
public class EncryptionService {
    
    @Value("${encryption.secret.key}")
    private String secretKey;
    
    /**
     * Encripta la contraseña del certificado con AES-256.
     */
    public String encrypt(String plainText) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(
                secretKey.getBytes(StandardCharsets.UTF_8), 
                "AES"
            );
            
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            
            byte[] encrypted = cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
            
        } catch (Exception e) {
            throw new EncryptionException("Error al encriptar", e);
        }
    }
    
    /**
     * Desencripta la contraseña del certificado.
     */
    public String decrypt(String encryptedText) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(
                secretKey.getBytes(StandardCharsets.UTF_8), 
                "AES"
            );
            
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            
            byte[] decrypted = cipher.doFinal(
                Base64.getDecoder().decode(encryptedText)
            );
            return new String(decrypted);
            
        } catch (Exception e) {
            throw new EncryptionException("Error al desencriptar", e);
        }
    }
}
```

### Almacenamiento de Certificados

**Opciones de almacenamiento**:

1. **Sistema de archivos local** (desarrollo/pequeña escala)
   ```
   /var/frc-efact/certificados/
   ├── empresa_1.pfx
   ├── empresa_2.pfx
   └── empresa_3.pfx
   ```

2. **Sistema de archivos compartido** (producción multi-instancia)
   - NFS
   - AWS EFS
   - Azure Files

3. **Object Storage** (producción cloud)
   - AWS S3
   - Google Cloud Storage
   - MinIO (self-hosted)

**Recomendación**: Usar object storage en producción con:
- Encriptación en reposo
- Versionado habilitado
- Acceso restringido por IAM
- Backup automático

## Alertas de Expiración

### Scheduler para Certificados

```java
@Component
public class CertificadoScheduler {
    
    @Autowired
    private EmpresaRepository empresaRepository;
    
    @Autowired
    private NotificacionService notificacionService;
    
    /**
     * Verifica certificados que expiran pronto.
     * Ejecuta diariamente a las 8 AM.
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void alertarCertificadosPorVencer() {
        List<Empresa> empresas = empresaRepository.findByActivoTrue();
        
        for (Empresa empresa : empresas) {
            // Alertar 30 días antes
            if (empresa.isCertificadoPorVencer(30)) {
                notificacionService.alertarCertificadoPorVencer(
                    empresa, 
                    30
                );
            }
            
            // Alertar 15 días antes
            if (empresa.isCertificadoPorVencer(15)) {
                notificacionService.alertarCertificadoPorVencer(
                    empresa, 
                    15
                );
            }
            
            // Alertar 7 días antes
            if (empresa.isCertificadoPorVencer(7)) {
                notificacionService.alertarCertificadoPorVencer(
                    empresa, 
                    7
                );
            }
            
            // Alertar 1 día antes (crítico)
            if (empresa.isCertificadoPorVencer(1)) {
                notificacionService.alertarCertificadoPorVencer(
                    empresa, 
                    1
                );
            }
        }
    }
}
```

## Carga de Certificados

### Endpoint para Subir Certificado

```java
@RestController
@RequestMapping("/api/empresas/{empresaId}/certificado")
public class CertificadoController {
    
    @Autowired
    private CertificadoService certificadoService;
    
    @Autowired
    private EmpresaService empresaService;
    
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN')")
    public ResponseEntity<Map<String, Object>> cargarCertificado(
            @PathVariable Long empresaId,
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("password") String password) {
        
        // Validar archivo
        if (!archivo.getOriginalFilename().endsWith(".pfx")) {
            throw new IllegalArgumentException("El archivo debe ser formato .pfx");
        }
        
        // Validar certificado antes de guardar
        certificadoService.validarCertificado(archivo, password);
        
        // Guardar archivo
        String path = certificadoService.guardarCertificado(empresaId, archivo);
        
        // Actualizar empresa
        empresaService.actualizarCertificado(empresaId, path, password);
        
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Certificado cargado exitosamente");
        response.put("path", path);
        
        return ResponseEntity.ok(response);
    }
}
```

## Testing

### Test con Certificado Mock

```java
@SpringBootTest
public class CertificadoServiceTest {
    
    @Autowired
    private CertificadoService certificadoService;
    
    @MockBean
    private EmpresaRepository empresaRepository;
    
    @Test
    public void testFirmarXmlConCertificadoDeEmpresa() {
        // Arrange
        Empresa empresa = crearEmpresaMock();
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        
        String xml = "<xml>test</xml>";
        
        // Act
        String xmlFirmado = certificadoService.firmarXml(xml, 1L);
        
        // Assert
        assertNotNull(xmlFirmado);
        assertTrue(xmlFirmado.contains("Signature"));
    }
    
    @Test
    public void testValidarCertificadoExpirado() {
        // Arrange
        Empresa empresa = crearEmpresaMock();
        empresa.setCertificadoFechaExpiracion(LocalDate.now().minusDays(1));
        
        // Act & Assert
        assertThrows(CertificadoInvalidoException.class, () -> {
            certificadoService.validarCertificadoVigente(empresa);
        });
    }
}
```

## Checklist de Implementación

- [ ] Modelo Empresa tiene campos de certificado (path, password encriptado, fecha expiración)
- [ ] EncryptionService implementado con AES-256
- [ ] CertificadoService con carga dinámica por empresa
- [ ] Método firmarXml(xml, empresaId) implementado
- [ ] Validación de certificado vigente antes de firmar
- [ ] Liberación de recursos después de cada firma
- [ ] DocumentoElectronicoService usa certificado por empresa
- [ ] SifenService recibe empresaId y carga certificado dinámicamente
- [ ] Endpoint para cargar certificado .pfx
- [ ] Scheduler para alertas de expiración
- [ ] Tests unitarios con certificados mock
- [ ] Documentación de configuración de certificados

## Referencias

- **Repositorio de ejemplo**: https://github.com/GabFrank/franco-system-backend-filial/tree/facturacion-electronica
- **Path de servicios SIFEN**: `src/main/java/com/franco/dev/service/sifen`
- **Especificación SIFEN**: Documentación oficial de facturación electrónica Paraguay
- **Bouncy Castle**: Librería para firma digital XML
- **Apache Santuario**: Alternativa para firma XML

## Conclusión

La gestión de certificados en un sistema multi-empresa requiere:

1. **Carga dinámica** de certificados por empresa
2. **No mantener estado** entre operaciones
3. **Validación rigurosa** antes de cada uso
4. **Seguridad** en almacenamiento y manejo de contraseñas
5. **Monitoreo** de expiración de certificados

Seguir estos principios garantiza un sistema escalable, seguro y que cumple con los requisitos de SIFEN para múltiples empresas.
