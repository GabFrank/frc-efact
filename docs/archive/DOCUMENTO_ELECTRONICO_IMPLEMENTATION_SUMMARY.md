# Implementación de Generación de Documentos Electrónicos

## Resumen

Se ha completado la implementación de la tarea 9: "Implementar generación de documentos electrónicos". Esta implementación incluye todos los componentes necesarios para generar documentos electrónicos (DEs) a partir de facturas legales, siguiendo la especificación SIFEN de Paraguay.

## Componentes Implementados

### 1. XmlGeneratorService (Subtarea 9.2)

**Ubicación**: `src/main/java/com/frcefact/service/XmlGeneratorService.java`

**Funcionalidades**:
- ✅ Generación de XML original simplificado del documento electrónico
- ✅ Generación de CDC (Código de Control del Documento) de 44 caracteres según especificación SIFEN
- ✅ Generación de URL QR para consulta del documento
- ✅ Generación de código de seguridad aleatorio de 9 dígitos

**Métodos principales**:
```java
public String generarXmlOriginal(FacturaLegal factura)
public String generarCDC(FacturaLegal factura, String codigoSeguridad)
public String generarUrlQr(String cdc)
public String generarCodigoSeguridad()
```

**Características**:
- Genera XML simplificado con estructura básica SIFEN
- Calcula CDC con formato: TT + RUC(8) + DV(1) + PuntoExp(3) + CodEst(3) + NumDoc(7) + TipoContrib(1) + Fecha(8) + TipoEmision(1) + CodSeg(9) + DV(1)
- Valida longitud del CDC (44 caracteres)
- Calcula dígito verificador usando algoritmo Módulo 11
- Escapa caracteres especiales XML

### 2. DocumentoElectronicoService (Subtarea 9.3)

**Ubicación**: `src/main/java/com/frcefact/service/DocumentoElectronicoService.java`

**Funcionalidades**:
- ✅ Generación de documentos electrónicos a partir de facturas legales
- ✅ Validación de certificado vigente antes de generar DE
- ✅ Asociación de DEs a lotes
- ✅ Consulta y actualización de estado en SIFEN (estructura preparada)
- ✅ Gestión de XML original y firmado
- ✅ Búsqueda y filtrado de DEs

**Métodos principales**:
```java
public DocumentoElectronico generarDE(Long facturaLegalId)
public void asociarALote(Long deId, LoteDE lote)
public DocumentoElectronico consultarYActualizarEstado(Long deId)
public Page<DocumentoElectronico> listar(EstadoDE estado, Long empresaId, Pageable pageable)
public String obtenerXmlFirmado(Long id)
public String obtenerXmlOriginal(Long id)
public void actualizarEstado(Long id, EstadoDE nuevoEstado, String codigoRespuesta, String mensajeRespuesta)
```

**Flujo de generación de DE**:
1. Validar que la factura existe y no tiene DE asociado
2. Validar certificado vigente de la empresa
3. Generar código de seguridad aleatorio
4. Generar CDC
5. Generar URL QR
6. Generar XML original
7. Crear y guardar DocumentoElectronico con estado PENDIENTE

**Características**:
- Integración con CertificadoService para validación de certificados
- Integración con XmlGeneratorService para generación de XML y CDC
- Soporte para paginación y filtros
- Manejo de estados del DE (PENDIENTE, EN_PROCESO, APROBADO, RECHAZADO, CANCELADO, ERROR)

### 3. DocumentoElectronicoDto y Mapper (Subtarea 9.5)

**Ubicación**: 
- `src/main/java/com/frcefact/dto/DocumentoElectronicoDto.java`
- `src/main/java/com/frcefact/dto/mapper/DocumentoElectronicoMapper.java`

**Funcionalidades**:
- ✅ DTO con todos los campos del documento electrónico
- ✅ Campos adicionales para visualización (número factura, cliente, empresa)
- ✅ Mapper bidireccional entre entidad y DTO
- ✅ Método de actualización de entidad desde DTO

**Campos del DTO**:
- Identificadores: id, facturaLegalId, loteDeId
- Datos SIFEN: cdc, urlQr, numeroDocumento, tipoDocumento
- Estado: estado, codigoRespuestaSifen, mensajeRespuestaSifen
- Fechas: fechaEmision, fechaRecepcionSifen
- Datos adicionales: numeroFacturaFormateado, nombreCliente, rucCliente, razonSocialEmpresa

### 4. DocumentoElectronicoController (Subtarea 9.4)

**Ubicación**: `src/main/java/com/frcefact/controller/DocumentoElectronicoController.java`

**Endpoints implementados**:

#### GET /api/documentos-electronicos
- Lista documentos electrónicos con filtros opcionales
- Parámetros: estado, empresaId, paginación
- Roles: ADMIN, EMPRESA_ADMIN, FACTURADOR, LECTOR

#### GET /api/documentos-electronicos/{id}
- Obtiene un documento electrónico por ID
- Roles: ADMIN, EMPRESA_ADMIN, FACTURADOR, LECTOR

#### GET /api/documentos-electronicos/cdc/{cdc}
- Obtiene un documento electrónico por CDC
- Roles: ADMIN, EMPRESA_ADMIN, FACTURADOR, LECTOR

#### GET /api/documentos-electronicos/{id}/xml
- Descarga el XML del documento (firmado u original)
- Parámetro: tipo (firmado/original)
- Retorna archivo XML con Content-Disposition
- Roles: ADMIN, EMPRESA_ADMIN, FACTURADOR, LECTOR

#### POST /api/documentos-electronicos/{id}/consultar
- Consulta el estado del documento en SIFEN
- Actualiza el estado local según respuesta
- Roles: ADMIN, EMPRESA_ADMIN, FACTURADOR

#### POST /api/facturas/{id}/generar-de (en FacturaLegalController)
- Genera un documento electrónico desde una factura
- Endpoint preparado para integración futura
- Roles: ADMIN, EMPRESA_ADMIN, FACTURADOR

**Características**:
- Documentación OpenAPI/Swagger completa
- Control de acceso basado en roles
- Manejo de errores con respuestas HTTP apropiadas
- Paginación y ordenamiento
- Descarga de archivos XML

### 5. Actualizaciones en Repositorio

**Ubicación**: `src/main/java/com/frcefact/repository/DocumentoElectronicoRepository.java`

**Métodos agregados**:
```java
boolean existsByFacturaLegalId(Long facturaLegalId)
Page<DocumentoElectronico> findByEstado(EstadoDE estado, Pageable pageable)
Page<DocumentoElectronico> findByFacturaLegal_Empresa_Id(Long empresaId, Pageable pageable)
Page<DocumentoElectronico> findByEstadoAndFacturaLegal_Empresa_Id(EstadoDE estado, Long empresaId, Pageable pageable)
```

## Integración con Componentes Existentes

### CertificadoService
- Validación de certificados vigentes antes de generar DE
- Carga dinámica de certificados por empresa
- Ejecución de operaciones SIFEN con certificado específico

### FacturaLegal
- Generación de DE a partir de facturas legales
- Acceso a datos de empresa, timbrado, cliente e items
- Cálculo de totales por tasa de IVA

### Empresa
- Validación de certificado digital
- Datos fiscales para XML
- Configuración de ambiente SIFEN

## Estados del Documento Electrónico

```
PENDIENTE    → DE generado, esperando envío a SIFEN
EN_PROCESO   → DE asociado a lote, enviado a SIFEN
APROBADO     → DE aprobado por SIFEN
RECHAZADO    → DE rechazado por SIFEN
CANCELADO    → DE cancelado mediante evento
ERROR        → Error en procesamiento
```

## Formato del CDC

El CDC (Código de Control del Documento) tiene 44 caracteres con la siguiente estructura:

```
TT AAAAAAAA D PPP EEE NNNNNNN C YYYYMMDD T SSSSSSSSS V
│  │        │ │   │   │       │ │        │ │         │
│  │        │ │   │   │       │ │        │ │         └─ Dígito verificador (1)
│  │        │ │   │   │       │ │        │ └─────────── Código seguridad (9)
│  │        │ │   │   │       │ │        └───────────── Tipo emisión (1)
│  │        │ │   │   │       │ └────────────────────── Fecha YYYYMMDD (8)
│  │        │ │   │   │       └──────────────────────── Tipo contribuyente (1)
│  │        │ │   │   └──────────────────────────────── Número documento (7)
│  │        │ │   └──────────────────────────────────── Código establecimiento (3)
│  │        │ └──────────────────────────────────────── Punto expedición (3)
│  │        └─────────────────────────────────────────── DV del RUC (1)
│  └──────────────────────────────────────────────────── RUC emisor (8)
└─────────────────────────────────────────────────────── Tipo documento (2)
```

Ejemplo: `01800123451001001000000112025011090123456781`

## Seguridad

- ✅ Control de acceso basado en roles (RBAC)
- ✅ Validación de permisos por empresa
- ✅ Validación de certificados digitales
- ✅ Encriptación de datos sensibles (CSC, contraseñas)
- ✅ Auditoría de operaciones

## Próximos Pasos

La implementación actual proporciona la base para:

1. **Tarea 10**: Integración completa con SIFEN
   - Envío de lotes de DEs
   - Consulta de estado en SIFEN
   - Procesamiento de respuestas

2. **Tarea 11**: Consulta de estado de DEs
   - Parsing completo de respuestas SIFEN
   - Actualización automática de estados
   - Manejo de eventos asociados

3. **Tarea 12**: Cancelación de documentos electrónicos
   - Generación de eventos de cancelación
   - Envío a SIFEN
   - Actualización de estado del DE

4. **Tarea 13**: Procesamiento asíncrono
   - Schedulers para consultas periódicas
   - Reintentos automáticos
   - Notificaciones

## Notas Técnicas

### Multi-Empresa
- Todos los servicios están diseñados para soportar múltiples empresas
- Cada empresa tiene su propio certificado digital
- Los certificados se cargan dinámicamente según la empresa

### XML Simplificado
- El XML generado actualmente es simplificado
- El XML completo con todos los campos SIFEN se generará cuando se envíe el lote usando jsifenlib
- Esto permite flexibilidad y reduce complejidad en esta fase

### Thread Safety
- CertificadoService usa sincronización para operaciones con SIFEN
- Esto es necesario porque `Sifen.setSifenConfig()` es estático/global

### Validaciones
- Certificado vigente antes de generar DE
- Factura no debe tener DE previo
- CDC debe tener exactamente 44 caracteres
- Dígito verificador calculado con Módulo 11

## Testing

Los tests unitarios están marcados como opcionales (subtarea 9.6*) según la especificación del proyecto que prioriza funcionalidad core sobre testing exhaustivo.

## Documentación API

Todos los endpoints están documentados con OpenAPI/Swagger y son accesibles en:
- Desarrollo: http://localhost:8080/swagger-ui.html
- Producción: https://[domain]/swagger-ui.html

## Conclusión

La implementación de la tarea 9 está completa y proporciona una base sólida para la generación de documentos electrónicos. Todos los componentes están integrados correctamente y listos para la siguiente fase de integración con SIFEN.
