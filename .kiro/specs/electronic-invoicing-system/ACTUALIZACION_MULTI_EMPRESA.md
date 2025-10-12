# Actualización del Diseño: Multi-Empresa con jsifenlib

## Resumen de Cambios

Se ha actualizado el diseño y las tareas del sistema basándose en la implementación funcional de referencia del repositorio:
**https://github.com/GabFrank/franco-system-backend-filial** (branch: facturacion-electronica)

## Documentos Creados/Actualizados

### 1. MULTI_EMPRESA_CERTIFICADOS.md
**Propósito**: Explica las consideraciones críticas para manejar certificados digitales en un sistema multi-empresa.

**Contenido**:
- Diferencias entre single-tenant y multi-tenant
- Arquitectura de almacenamiento de certificados
- Flujo correcto de firma digital por empresa
- Integración con SIFEN multi-empresa
- Seguridad y encriptación
- Alertas de expiración
- Checklist de implementación

### 2. SIFEN_IMPLEMENTATION_REFERENCE.md
**Propósito**: Documentación completa de la implementación de referencia.

**Contenido**:
- Estructura de archivos del repositorio de referencia
- Componentes clave (SifenConfiguration, SifenService, SifenEventoService)
- Uso de jsifenlib (clases, métodos, tipos)
- Flujos completos (generar DE, enviar lote, cancelar DE)
- Códigos de respuesta SIFEN
- Bugs conocidos y workarounds
- Mejores prácticas
- Dependencias Maven

### 3. SIFEN_DESIGN_UPDATED.md
**Propósito**: Diseño actualizado de servicios SIFEN adaptado para multi-empresa.

**Contenido**:
- Arquitectura de servicios (SifenService, SifenEventoService, SifenSchedulerService)
- Código completo adaptado para multi-empresa
- Configuración dinámica de jsifenlib
- Thread-safety con sincronización
- Modelo de datos actualizado
- Configuración de aplicación
- Tabla comparativa single-tenant vs multi-tenant

### 4. design.md (Actualizado)
**Cambios**:
- Agregada sección "CONSIDERACIONES CRÍTICAS MULTI-EMPRESA" al inicio
- Actualizado CertificadoService para carga dinámica por empresa
- Actualizado DocumentoElectronicoService para usar empresaId
- Agregadas notas sobre referencia de implementación

### 5. tasks.md (Actualizado)
**Cambios**:
- Tarea 9.1: Agregadas instrucciones detalladas sobre carga dinámica de certificados
- Tarea 9.3: Actualizada para incluir validación de certificado y paso de empresaId
- Tarea 10.1: Agregada referencia al repositorio de ejemplo
- Tarea 10.2: Actualizada con instrucciones críticas sobre multi-empresa y thread-safety

## Cambios Clave en el Diseño

### Antes (Diseño Original - Incorrecto para Multi-Empresa)

```java
// ❌ Configuración global única
@Bean
public SifenConfig sifenConfig() {
    return new SifenConfig(ambiente, csc, certPath, certPassword);
}

// ❌ Método sin empresaId
public String firmarXml(String xml, String certificadoPath, String password) {
    // Firma con certificado pasado como parámetro
}
```

### Después (Diseño Actualizado - Correcto para Multi-Empresa)

```java
// ✅ Sin bean singleton - configuración dinámica
private void configurarSifenParaEmpresa(Long empresaId) {
    Empresa empresa = empresaRepository.findById(empresaId)...;
    SifenConfig config = new SifenConfig(
        ambiente,
        empresa.getCscId(),
        encryptionService.decrypt(empresa.getCscEncrypted()),
        TipoCertificadoCliente.PFX,
        empresa.getCertificadoPath(),
        encryptionService.decrypt(empresa.getCertificadoPasswordEncrypted())
    );
    Sifen.setSifenConfig(config);
}

// ✅ Método con empresaId
public String firmarXml(String xml, Long empresaId) {
    Empresa empresa = empresaRepository.findById(empresaId)...;
    // Carga dinámica del certificado de la empresa
}
```

## Aspectos Críticos Identificados

### 1. Thread-Safety
**Problema**: `Sifen.setSifenConfig()` es estático y global.  
**Solución**: Sincronizar acceso con lock:

```java
private final Object sifenLock = new Object();

public void enviarLote(LoteDE lote, Long empresaId) {
    synchronized (sifenLock) {
        configurarSifenParaEmpresa(empresaId);
        // Operación con SIFEN
    }
}
```

### 2. Almacenamiento de Datos Sensibles
**Campos en Empresa**:
- `certificadoPath`: Path al archivo .pfx
- `certificadoPasswordEncrypted`: Contraseña encriptada con AES-256
- `certificadoFechaExpiracion`: Fecha de expiración
- `cscId`: ID del CSC
- `cscEncrypted`: CSC encriptado con AES-256
- `sifenAmbiente`: DEV, TEST, PRODUCTION

### 3. Firma de Métodos
**Todos los métodos de servicio deben incluir empresaId**:

```java
// SifenService
void enviarLote(LoteDE lote, Long empresaId)
RespuestaConsultaDE consultarDE(String cdc, Long empresaId)
void consultarLote(LoteDE lote, Long empresaId)

// SifenEventoService
RespuestaRecepcionEvento cancelarDE(String cdc, String motivo, Long empresaId)

// CertificadoService
String firmarXml(String xml, Long empresaId)
void validarCertificadoVigente(Empresa empresa)
```

### 4. Reconstrucción de DEs
**Preferencia**: Usar XML original guardado en BD  
**Fallback**: Regenerar desde FacturaLegal

```java
if (de.getXmlOriginal() != null && !de.getXmlOriginal().isEmpty()) {
    // Método preferido: reconstruir desde XML
    deSifen = new DocumentoElectronico(de.getXmlOriginal());
} else {
    // Fallback: regenerar desde factura
    deSifen = regenerarDEDesdeFactura(de);
}
```

### 5. Procesamiento de Eventos
**Orden de procesamiento en consultarDE()**:
1. Actualizar estado base del DE (APROBADO/RECHAZADO)
2. Procesar eventos asociados (cancelación)
3. Si hay evento cancelación APROBADO → Estado final: CANCELADO

## Implementación de Referencia

### Repositorio
- **URL**: https://github.com/GabFrank/franco-system-backend-filial
- **Branch**: facturacion-electronica
- **Path**: src/main/java/com/franco/dev/service/sifen

### Archivos Clave Revisados
1. `config/SifenConfiguration.java` - Configuración Spring Boot
2. `config/SifenProperties.java` - Properties
3. `service/SifenService.java` - Servicio principal (1000+ líneas)
4. `service/SifenEventoService.java` - Gestión de eventos
5. `util/SifenReceptorHelper.java` - Helper para receptores
6. `util/SifenEventoParser.java` - Parser de eventos

### Características Implementadas
- ✅ Envío de lotes de DEs
- ✅ Consulta de estado de lotes
- ✅ Consulta de DEs individuales
- ✅ Cancelación de DEs con reintentos
- ✅ Procesamiento de eventos asociados
- ✅ Parsing robusto de XML
- ✅ Workarounds para bugs de jsifenlib
- ✅ Logging detallado con emojis
- ✅ Manejo de estados transitorios

## Próximos Pasos

### Para Implementación (Tareas 9 y 10)

1. **Tarea 9.1**: Implementar CertificadoService con carga dinámica
   - Método `firmarXml(xml, empresaId)`
   - Método `validarCertificadoVigente(empresa)`
   - Usar Bouncy Castle o Apache Santuario

2. **Tarea 9.2**: Implementar XmlGeneratorService
   - Generar XML según especificación SIFEN
   - Generar CDC (44 caracteres)
   - Generar URL QR

3. **Tarea 9.3**: Implementar DocumentoElectronicoService
   - Integrar CertificadoService y XmlGeneratorService
   - Flujo completo: validar → generar → firmar → CDC → QR

4. **Tarea 10.1**: Configurar jsifenlib en Maven
   - Agregar GitHub Packages como repositorio
   - Configurar autenticación
   - Agregar dependencia jsifenlib 0.2.4-frc.13

5. **Tarea 10.2**: Implementar SifenService
   - Método `enviarLote(lote, empresaId)`
   - Método `consultarLote(lote, empresaId)`
   - Método `consultarDE(cdc, empresaId)`
   - Configuración dinámica por empresa
   - Thread-safety con lock

6. **Tarea 10.3**: Implementar SifenEventoService
   - Método `cancelarDE(cdc, motivo, empresaId)`
   - Manejo de reintentos
   - Procesamiento de respuestas

7. **Tarea 10.4**: Implementar SifenSchedulerService
   - Consulta de lotes cada 5 minutos
   - Consulta de DEs cada 10 minutos

### Para Testing

1. Crear certificados de prueba para múltiples empresas
2. Configurar ambiente DEV de SIFEN
3. Probar flujo completo con 2+ empresas simultáneamente
4. Verificar thread-safety bajo carga
5. Validar que cada empresa usa su propio certificado

## Recursos

### Documentación
- [MULTI_EMPRESA_CERTIFICADOS.md](./MULTI_EMPRESA_CERTIFICADOS.md) - Guía de certificados
- [SIFEN_IMPLEMENTATION_REFERENCE.md](./SIFEN_IMPLEMENTATION_REFERENCE.md) - Referencia completa
- [SIFEN_DESIGN_UPDATED.md](./SIFEN_DESIGN_UPDATED.md) - Diseño actualizado

### Repositorio de Referencia
- https://github.com/GabFrank/franco-system-backend-filial
- Branch: facturacion-electronica
- Implementación funcional y probada

### Librería jsifenlib
- Repositorio: https://github.com/GabFrank/jsifenlib
- Versión: 0.2.4-frc.13
- Distribuida vía GitHub Packages

## Conclusión

El diseño ha sido actualizado para reflejar:

1. **Realidad de jsifenlib**: Uso de clases estáticas, configuración global, tipos de datos específicos
2. **Multi-empresa**: Configuración dinámica, thread-safety, almacenamiento por empresa
3. **Implementación probada**: Basado en código funcional en producción
4. **Mejores prácticas**: Logging, manejo de errores, workarounds para bugs conocidos

El sistema está ahora diseñado correctamente para manejar múltiples empresas, cada una con su propio certificado PFX y CSC, integrándose de forma segura y eficiente con SIFEN Paraguay.
