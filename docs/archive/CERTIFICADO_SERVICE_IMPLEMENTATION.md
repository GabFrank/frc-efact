# Implementación del Servicio de Certificados Digitales

## Resumen

Se ha implementado el `CertificadoService` que maneja la carga DINÁMICA de certificados digitales por empresa, utilizando la librería `jsifenlib` (fork de GabFrank).

## Archivos Creados/Modificados

### 1. Nuevos Archivos

- **`CertificadoService.java`**: Servicio principal para gestión de certificados
- **`BusinessException.java`**: Excepción base para errores de negocio
- **`CertificadoException.java`**: Excepción específica para errores de certificados

### 2. Archivos Modificados

- **`pom.xml`**: 
  - Agregada dependencia `jsifenlib` (versión 0.2.4-frc.13)
  - Agregado repositorio GitHub Packages
  - Agregada dependencia Lombok

- **`Empresa.java`**: 
  - Agregados campos: `cscId`, `cscEncrypted`, `sifenAmbiente`
  - Agregados getters y setters correspondientes

## Características Principales del CertificadoService

### 1. Carga Dinámica de Certificados

```java
public void configurarSifenParaEmpresa(Long empresaId)
```

- Carga el certificado específico de cada empresa
- NO mantiene certificados en memoria
- Thread-safe mediante sincronización

### 2. Validación de Certificados

```java
public void validarCertificadoVigente(Empresa empresa)
```

- Verifica existencia del archivo .pfx
- Valida fecha de expiración
- Advierte si el certificado está por vencer (30 días)

### 3. Testing de Certificados

```java
public boolean validarCertificado(String path, String password)
```

- Permite validar certificados antes de guardarlos
- Útil para el endpoint de carga de certificados

### 4. Ejecución Segura

```java
public <T> T ejecutarConCertificado(Long empresaId, SifenOperation<T> operation)
```

- Wrapper para ejecutar operaciones SIFEN
- Garantiza configuración correcta del certificado
- Manejo automático de errores

## Configuración de jsifenlib

### Dependencia Maven

```xml
<repositories>
    <repository>
        <id>github-jsifenlib</id>
        <url>https://maven.pkg.github.com/GabFrank/jsifenlib</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>io.github.gabfrank</groupId>
        <artifactId>jsifenlib</artifactId>
        <version>0.2.4-frc.13</version>
    </dependency>
</dependencies>
```

### Autenticación GitHub Packages

Crear/editar `~/.m2/settings.xml`:

```xml
<settings>
    <servers>
        <server>
            <id>github-jsifenlib</id>
            <username>TU_USUARIO_GITHUB</username>
            <password>TU_PERSONAL_ACCESS_TOKEN</password>
        </server>
    </servers>
</settings>
```

**Nota**: El Personal Access Token debe tener el scope `read:packages`.

## Flujo de Uso

### 1. Configuración Inicial

```java
@Autowired
private CertificadoService certificadoService;

// Configurar SIFEN para una empresa específica
certificadoService.configurarSifenParaEmpresa(empresaId);
```

### 2. Ejecución de Operaciones SIFEN

```java
// Opción A: Configurar manualmente
certificadoService.configurarSifenParaEmpresa(empresaId);
RespuestaConsultaRUC respuesta = Sifen.consultaRUC(ruc);

// Opción B: Usar wrapper (recomendado)
RespuestaConsultaRUC respuesta = certificadoService.ejecutarConCertificado(
    empresaId,
    () -> Sifen.consultaRUC(ruc)
);
```

### 3. Validación de Certificados

```java
// Validar certificado existente de empresa
Empresa empresa = empresaRepository.findById(empresaId).orElseThrow();
certificadoService.validarCertificadoVigente(empresa);

// Validar nuevo certificado antes de guardar
boolean esValido = certificadoService.validarCertificado(
    "/path/to/certificado.pfx",
    "password"
);
```

## Modelo de Datos

### Campos Agregados a Empresa

```java
// Certificado digital (ya existían)
private String certificadoPath;
private String certificadoPasswordEncrypted;
private LocalDate certificadoFechaExpiracion;

// CSC (Código de Seguridad del Contribuyente) - NUEVOS
private String cscId;              // ID del CSC (ej: "0001")
private String cscEncrypted;       // CSC encriptado

// Configuración SIFEN - NUEVO
private String sifenAmbiente;      // "DEV", "TEST", "PRODUCTION"
```

## Thread Safety

El servicio implementa sincronización para manejar el hecho de que `Sifen.setSifenConfig()` es estático/global:

```java
private final Object sifenLock = new Object();

synchronized (sifenLock) {
    SifenConfig config = new SifenConfig(...);
    Sifen.setSifenConfig(config);
    // Ejecutar operación
}
```

Esto garantiza que:
- Solo una empresa puede configurar SIFEN a la vez
- No hay race conditions entre threads
- Cada operación usa el certificado correcto

## Seguridad

### Encriptación de Datos Sensibles

- **Contraseña del certificado**: Almacenada encriptada en `certificadoPasswordEncrypted`
- **CSC**: Almacenado encriptado en `cscEncrypted`
- Desencriptación solo en memoria durante operaciones
- Uso del `EncryptionService` existente

### Validaciones

- Existencia del archivo de certificado
- Permisos de lectura del archivo
- Fecha de expiración
- Formato del archivo (.pfx o .p12)

## Logging

El servicio implementa logging detallado con emojis para facilitar debugging:

- ✅ Operaciones exitosas
- ❌ Errores
- ⚠️ Advertencias
- 🔧 Configuración
- 🔍 Validación

Ejemplo:
```
✅ SIFEN configurado para empresa 1 (RUC: 80089752-1) en ambiente PROD
⚠️ El certificado de la empresa 1 expira pronto: 2024-12-31
❌ Error al configurar SIFEN para empresa 1
```

## Próximos Pasos

**IMPORTANTE**: La librería `jsifenlib` maneja automáticamente:
- ✅ Generación de XML según especificación SIFEN
- ✅ Firma digital del XML con el certificado configurado
- ✅ Generación de CDC (Código de Control)
- ✅ Validación de estructura

Por lo tanto, **NO necesitamos** crear servicios separados para:
- ❌ XmlGeneratorService (Tarea 9.2) - jsifenlib lo hace automáticamente
- ❌ Servicio de firma digital - jsifenlib lo hace automáticamente

Este servicio será utilizado por:

1. **SifenService**: Wrapper que use `CertificadoService` + `jsifenlib` para operaciones SIFEN (copiar del ejemplo de referencia)
2. **DocumentoElectronicoService** (Tarea 9.3): Lógica de negocio y persistencia de DEs
3. **DocumentoElectronicoController** (Tarea 9.4): API REST

## Ejemplo de Integración

```java
@Service
public class DocumentoElectronicoService {
    
    @Autowired
    private CertificadoService certificadoService;
    
    public DocumentoElectronico generarDE(FacturaLegal factura) {
        Long empresaId = factura.getEmpresa().getId();
        
        return certificadoService.ejecutarConCertificado(empresaId, () -> {
            // Generar XML
            String xml = generarXml(factura);
            
            // Firmar con certificado de la empresa
            DocumentoElectronico de = new DocumentoElectronico(xml);
            
            // jsifenlib usa automáticamente el certificado configurado
            return de;
        });
    }
}
```

## Notas Importantes

1. **No usar configuración global**: Nunca configurar SIFEN en `@PostConstruct` o como bean singleton
2. **Siempre pasar empresaId**: Todos los métodos que usen SIFEN deben recibir el ID de empresa
3. **Sincronización obligatoria**: Siempre usar el lock al configurar SIFEN
4. **Liberar recursos**: No mantener certificados en memoria después de usarlos

## Referencias

- [jsifenlib README](https://github.com/GabFrank/rshk-jsifenlib/blob/master/README.md)
- [Manual Técnico SIFEN](https://www.dnit.gov.py/documents/20123/420592/Manual+T%C3%A9cnico+Versi%C3%B3n+150.pdf)
- [SIFEN_IMPLEMENTATION_REFERENCE.md](.kiro/specs/electronic-invoicing-system/SIFEN_IMPLEMENTATION_REFERENCE.md)
- [MULTI_EMPRESA_CERTIFICADOS.md](.kiro/specs/electronic-invoicing-system/MULTI_EMPRESA_CERTIFICADOS.md)
