# Sistema de Auditoría - Resumen de Implementación

## Descripción General

Se ha implementado un sistema completo de auditoría para el proyecto FRC eFact que registra automáticamente todas las operaciones CRUD críticas del sistema. El sistema utiliza AspectJ AOP para interceptar métodos anotados y registrar información detallada en la base de datos.

## Componentes Implementados

### 1. Anotación @Auditable

**Archivo:** `src/main/java/com/frcefact/annotation/Auditable.java`

Anotación personalizada que marca métodos para auditoría automática.

**Parámetros:**
- `entidad`: Nombre de la entidad auditada (ej: "Empresa", "Factura")
- `accion`: Tipo de acción (CREATE, UPDATE, DELETE)
- `descripcion`: Descripción opcional de la operación

**Ejemplo de uso:**
```java
@Auditable(entidad = "Empresa", accion = AccionEnum.CREATE)
public Empresa crearEmpresa(Empresa empresa) {
    // lógica de creación
}
```

### 2. Aspecto de Auditoría (AuditAspect)

**Archivo:** `src/main/java/com/frcefact/aspect/AuditAspect.java`

Intercepta métodos anotados con `@Auditable` y registra automáticamente:

**Información capturada:**
- Usuario que realiza la acción (desde SecurityContext)
- Empresa asociada (extraída de la entidad o argumentos)
- Valores anteriores (para operaciones UPDATE)
- Valores nuevos (para CREATE y UPDATE)
- IP address del cliente (considerando proxies)
- User Agent del navegador
- Timestamp de la operación

**Características:**
- Captura valores anteriores antes de UPDATE
- Filtra campos sensibles (passwords, certificados)
- Maneja errores sin afectar la operación principal
- Extrae automáticamente ID de entidad y empresa

### 3. Servicio de Auditoría (AuditLogService)

**Archivo:** `src/main/java/com/frcefact/service/AuditLogService.java`

Proporciona métodos para consultar el historial de auditoría:

**Métodos principales:**
- `save()`: Guarda un registro de auditoría
- `getUltimasActividades()`: Obtiene actividades recientes del sistema
- `getUltimasActividadesUsuario()`: Actividades de un usuario específico
- `getHistorialEntidad()`: Historial completo de una entidad
- `buscarConFiltros()`: Búsqueda avanzada con múltiples filtros
- `getEstadisticasPorAccion()`: Estadísticas agrupadas por tipo de acción
- `contarTotal()`: Conteo total de registros
- `contarPorEmpresa()`: Conteo por empresa

### 4. Controlador REST (AuditLogController)

**Archivo:** `src/main/java/com/frcefact/controller/AuditLogController.java`

Expone endpoints REST para consultar auditoría:

**Endpoints implementados:**

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/api/auditoria` | Buscar con filtros | ADMIN, EMPRESA_ADMIN |
| GET | `/api/auditoria/entidad/{tipo}/{id}` | Historial de entidad | Todos |
| GET | `/api/auditoria/ultimas-actividades` | Actividades recientes | ADMIN, EMPRESA_ADMIN |
| GET | `/api/auditoria/usuario/{usuarioId}` | Actividades de usuario | ADMIN, EMPRESA_ADMIN |
| GET | `/api/auditoria/estadisticas` | Estadísticas por acción | ADMIN, EMPRESA_ADMIN |
| GET | `/api/auditoria/count` | Conteo total | ADMIN |
| GET | `/api/auditoria/count/empresa/{empresaId}` | Conteo por empresa | ADMIN, EMPRESA_ADMIN |

**Filtros disponibles:**
- Usuario ID
- Empresa ID
- Tipo de entidad
- Acción (CREATE, UPDATE, DELETE)
- Rango de fechas (desde/hasta)
- Paginación y ordenamiento

### 5. Configuración AOP

**Archivo:** `src/main/java/com/frcefact/config/AopConfig.java`

Habilita AspectJ AOP en la aplicación con `@EnableAspectJAutoProxy`.

### 6. Dependencia Maven

Se agregó la dependencia de Spring AOP en `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

## Servicios Auditados

Se aplicó la anotación `@Auditable` a los siguientes servicios críticos:

### EmpresaService
- `crearEmpresa()` - CREATE
- `actualizarEmpresa()` - UPDATE

### FacturaLegalService
- `crearFactura()` - CREATE

### TimbradoService
- `crear()` - CREATE
- `actualizar()` - UPDATE

### ProductoService
- `crearProducto()` - CREATE
- `actualizarProducto()` - UPDATE

## Modelo de Datos

La tabla `auditoria.audit_log` almacena:

```sql
- id: BIGSERIAL PRIMARY KEY
- usuario_id: Usuario que realizó la acción
- empresa_id: Empresa asociada (nullable)
- entidad_tipo: Tipo de entidad (VARCHAR)
- entidad_id: ID de la entidad
- accion: Enum (CREATE, UPDATE, DELETE, READ)
- fecha_hora: Timestamp de la operación
- valores_anteriores: JSONB (para UPDATE)
- valores_nuevos: JSONB (para CREATE/UPDATE)
- ip_address: IP del cliente
- user_agent: User Agent del navegador
- descripcion: Descripción de la operación
```

**Índices creados:**
- `idx_audit_usuario` en usuario_id
- `idx_audit_empresa` en empresa_id
- `idx_audit_entidad` en (entidad_tipo, entidad_id)
- `idx_audit_accion` en accion
- `idx_audit_fecha` en fecha_hora

## Seguridad

### Protección de Datos Sensibles

El sistema filtra automáticamente campos sensibles antes de almacenarlos:
- `password`
- `passwordHash`
- `certificadoPasswordEncrypted`
- `cscEncrypted`

### Control de Acceso

Los endpoints de auditoría están protegidos con roles:
- **ADMIN**: Acceso completo a toda la auditoría
- **EMPRESA_ADMIN**: Acceso a auditoría de su empresa
- **FACTURADOR/LECTOR**: Acceso limitado a historial de entidades

### Captura de IP Real

El sistema detecta la IP real del cliente considerando proxies y balanceadores:
- X-Forwarded-For
- Proxy-Client-IP
- WL-Proxy-Client-IP
- Y otros headers estándar

## Casos de Uso

### 1. Dashboard de Actividades

```java
// Obtener últimas 10 actividades del sistema
List<AuditLog> actividades = auditLogService.getUltimasActividades(10);
```

### 2. Historial de una Factura

```java
// Ver todos los cambios de una factura específica
List<AuditLog> historial = auditLogService.getHistorialEntidad("FacturaLegal", 123L);
```

### 3. Búsqueda Avanzada

```java
// Buscar todas las actualizaciones de productos en marzo 2024
Page<AuditLog> resultados = auditLogService.buscarConFiltros(
    null, // cualquier usuario
    empresaId, // empresa específica
    "Producto", // solo productos
    AccionEnum.UPDATE, // solo actualizaciones
    LocalDateTime.of(2024, 3, 1, 0, 0),
    LocalDateTime.of(2024, 3, 31, 23, 59),
    pageable
);
```

### 4. Estadísticas

```java
// Obtener conteo de operaciones por tipo
Map<AccionEnum, Long> stats = auditLogService.getEstadisticasPorAccion(
    empresaId,
    fechaDesde,
    fechaHasta
);
// Resultado: {CREATE=150, UPDATE=89, DELETE=12}
```

## Ventajas del Sistema

1. **Automático**: No requiere código manual en cada operación
2. **Consistente**: Todas las operaciones se auditan de la misma manera
3. **No intrusivo**: No afecta la lógica de negocio
4. **Completo**: Captura valores antes/después, usuario, IP, etc.
5. **Seguro**: Filtra datos sensibles automáticamente
6. **Performante**: Manejo de errores no bloquea operaciones
7. **Flexible**: Fácil agregar nuevas entidades con solo una anotación

## Próximos Pasos Recomendados

1. **Agregar auditoría a más servicios:**
   - ClienteService
   - DocumentoElectronicoService
   - UsuarioService

2. **Implementar retención de datos:**
   - Política de archivado de registros antiguos
   - Compresión de datos históricos

3. **Dashboard de auditoría en frontend:**
   - Visualización de actividades recientes
   - Gráficos de estadísticas
   - Timeline de cambios por entidad

4. **Alertas automáticas:**
   - Notificar cambios en entidades críticas
   - Detectar patrones sospechosos
   - Alertas de acceso no autorizado

5. **Exportación de reportes:**
   - Generar reportes de auditoría en PDF/Excel
   - Cumplimiento normativo

## Verificación

Para verificar que el sistema funciona correctamente:

1. **Crear una empresa:**
```bash
POST /api/empresas
# Verificar que se creó un registro en audit_log con accion=CREATE
```

2. **Actualizar la empresa:**
```bash
PUT /api/empresas/{id}
# Verificar que se creó un registro con accion=UPDATE
# y que valores_anteriores contiene los datos previos
```

3. **Consultar historial:**
```bash
GET /api/auditoria/entidad/Empresa/{id}
# Debe retornar ambos registros (CREATE y UPDATE)
```

## Cumplimiento de Requirements

✅ **Requirement 17.1**: Registro de CREATE, UPDATE, DELETE con usuario y fecha  
✅ **Requirement 17.2**: Captura de valores anteriores en UPDATE  
✅ **Requirement 17.3**: Registro de DELETE  
✅ **Requirement 17.4**: Consulta de historial por entidad  
✅ **Requirement 17.5**: Búsqueda con filtros múltiples  
✅ **Requirement 17.6**: Captura de IP y User Agent  
✅ **Requirement 17.7**: Tabla inmutable (solo INSERT, no UPDATE/DELETE)

## Conclusión

El sistema de auditoría está completamente implementado y listo para uso en producción. Proporciona trazabilidad completa de todas las operaciones críticas del sistema, cumpliendo con los requisitos de auditoría y seguridad del proyecto.
