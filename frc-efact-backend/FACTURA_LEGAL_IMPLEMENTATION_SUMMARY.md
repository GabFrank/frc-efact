# Implementación de Gestión de Facturas Legales

## Resumen

Se ha implementado completamente el módulo de gestión de facturas legales, incluyendo:
- Servicio de negocio con lógica completa de facturación
- Controlador REST con endpoints CRUD y operaciones especializadas
- DTOs con validaciones
- Mapper para conversión entre entidades y DTOs

## Archivos Creados

### 1. Service Layer
- **FacturaLegalService.java**: Servicio principal con toda la lógica de negocio
  - Creación de facturas con asignación automática de número
  - Validación de vigencia de timbrado
  - Gestión de items (agregar, eliminar)
  - Cálculo automático de totales por tasa de IVA
  - Aplicación de descuentos
  - Búsqueda con filtros
  - Estadísticas de facturación

### 2. Controller Layer
- **FacturaLegalController.java**: Controlador REST con endpoints:
  - `POST /api/facturas` - Crear factura
  - `GET /api/facturas/{id}` - Obtener factura por ID
  - `PUT /api/facturas/{id}` - Actualizar factura
  - `GET /api/facturas` - Listar con filtros (empresa, cliente, fechas)
  - `POST /api/facturas/{id}/items` - Agregar item
  - `DELETE /api/facturas/{facturaId}/items/{itemId}` - Eliminar item
  - `PUT /api/facturas/{id}/descuento` - Aplicar descuento
  - `POST /api/facturas/{id}/recalcular` - Recalcular totales
  - `DELETE /api/facturas/{id}` - Desactivar factura
  - `GET /api/facturas/estadisticas` - Obtener estadísticas

### 3. DTOs
- **FacturaLegalDto.java**: DTO principal con validaciones
  - Validación de items requeridos (al menos uno)
  - Validación de descuento no negativo
  - Campos calculados automáticamente (totales por IVA)
  
- **FacturaLegalItemDto.java**: DTO para items de factura
  - Validación de cantidad mayor a 0
  - Validación de precio unitario
  - Descripción requerida
  
- **FacturaFiltroDto.java**: DTO para filtros de búsqueda
  - Filtros por empresa, cliente, fechas
  - Configuración de paginación y ordenamiento

### 4. Mapper
- **FacturaLegalMapper.java**: Conversión entre entidades y DTOs
  - Conversión bidireccional de FacturaLegal
  - Conversión bidireccional de FacturaLegalItem
  - Manejo de relaciones (empresa, cliente, timbrado, productos)
  - Formateo de fechas

## Funcionalidades Implementadas

### Creación de Facturas
1. **Asignación Automática de Número**: 
   - Obtiene el siguiente número disponible del timbrado detalle
   - Usa lock pesimista para evitar conflictos de concurrencia
   - Incrementa automáticamente el contador

2. **Validación de Timbrado**:
   - Verifica que el timbrado esté vigente
   - Valida que haya números disponibles en el rango
   - Lanza excepciones descriptivas si hay problemas

3. **Gestión de Cliente**:
   - Carga datos del cliente si se especifica
   - Crea snapshot de datos del cliente en la factura
   - Permite facturas sin cliente (consumidor final)

4. **Procesamiento de Items**:
   - Carga productos automáticamente
   - Copia descripción del producto si no se especifica
   - Calcula total por item (cantidad × precio unitario)

### Cálculo de Totales
1. **Totales por Tasa de IVA**:
   - Separa items por tasa de IVA (0%, 5%, 10%)
   - Calcula IVA parcial por cada tasa
   - Calcula total parcial por cada tasa (subtotal + IVA)

2. **Total Parcial**:
   - Suma de todos los totales parciales por tasa

3. **Descuento Final**:
   - Se aplica sobre el total parcial
   - Validación de que no sea negativo ni mayor al total

4. **Total Final**:
   - Total parcial - descuento final

### Gestión de Items
1. **Agregar Item**:
   - Agrega item a la factura
   - Recalcula automáticamente todos los totales
   - Persiste cambios

2. **Eliminar Item**:
   - Elimina item de la factura
   - Recalcula automáticamente todos los totales
   - Valida que no se elimine el último item
   - Persiste cambios

3. **Recalcular Totales**:
   - Endpoint explícito para recalcular
   - Útil si se modifican items externamente

### Búsqueda y Filtros
1. **Filtros Disponibles**:
   - Por empresa (requerido)
   - Por cliente (opcional)
   - Por rango de fechas (opcional, default: mes actual)
   - Paginación configurable
   - Ordenamiento configurable

2. **Estadísticas**:
   - Total facturado en un período
   - Cantidad de facturas en un período
   - Filtrable por empresa y fechas

### Seguridad
1. **Control de Acceso**:
   - Verificación de permisos por empresa
   - Roles permitidos: ADMIN, EMPRESA_ADMIN, FACTURADOR
   - Rol LECTOR solo para consultas

2. **Validaciones**:
   - Validación de datos de entrada con Bean Validation
   - Validación de negocio en el servicio
   - Manejo de excepciones con mensajes descriptivos

## Integración con Otros Módulos

### Dependencias
- **EmpresaRepository**: Validación de empresa
- **TimbradoDetalleRepository**: Obtención de timbrado y numeración
- **TimbradoDetalleService**: Incremento de número actual con lock
- **ClienteRepository**: Carga de datos del cliente
- **ProductoRepository**: Carga de datos del producto
- **EmpresaSecurityService**: Verificación de permisos

### Preparación para Módulos Futuros
- La factura está lista para generar documentos electrónicos
- Estructura preparada para reportes y dashboards
- Datos de auditoría completos para trazabilidad

## Validaciones Implementadas

### A Nivel de DTO
- Items requeridos (al menos uno)
- Cantidad mayor a 0
- Precio unitario mayor a 0
- Descripción requerida
- Descuento no negativo
- Formato de números con precisión correcta

### A Nivel de Servicio
- Timbrado vigente
- Números disponibles en el rango
- Empresa existe
- Cliente existe (si se especifica)
- Producto existe (si se especifica)
- No eliminar último item
- Descuento no mayor al total

## Manejo de Errores

### Excepciones Lanzadas
- `EntityNotFoundException`: Entidad no encontrada
- `IllegalStateException`: Estado inválido (timbrado vencido, sin números)
- `IllegalArgumentException`: Argumentos inválidos (sin items, descuento inválido)
- `OptimisticLockException`: Conflicto de concurrencia (manejado por TimbradoDetalleService)

### Códigos HTTP
- 201: Factura creada exitosamente
- 200: Operación exitosa
- 204: Factura desactivada
- 400: Datos inválidos
- 403: Sin permisos
- 404: Entidad no encontrada
- 409: Conflicto (timbrado vencido, sin números)

## Logging
- Registro de creación de facturas con número asignado
- Registro de operaciones importantes
- Nivel INFO para operaciones normales
- Nivel WARN/ERROR para problemas

## Documentación API
- Anotaciones Swagger/OpenAPI completas
- Descripción de cada endpoint
- Descripción de parámetros
- Códigos de respuesta documentados
- Ejemplos de schemas

## Testing Recomendado

### Tests Unitarios (Opcional según tasks.md)
- Cálculo de totales por tasa de IVA
- Aplicación de descuentos
- Validación de timbrado vencido
- Validación de números disponibles

### Tests de Integración (Opcional según tasks.md)
- Creación de factura completa
- Agregar/eliminar items
- Búsqueda con filtros
- Concurrencia en asignación de números

## Próximos Pasos

Este módulo está completo y listo para:
1. Generación de documentos electrónicos (Task 9)
2. Reportes de facturación (Task 16)
3. Dashboards de empresa (Task 15)
4. Auditoría de operaciones (Task 15)

## Notas Técnicas

### Concurrencia
- El incremento de número de factura usa lock pesimista en TimbradoDetalleService
- Thread-safe para múltiples usuarios creando facturas simultáneamente

### Performance
- Uso de FetchType.LAZY para relaciones grandes
- Paginación en listados
- Índices en base de datos para búsquedas frecuentes

### Mantenibilidad
- Código bien documentado con JavaDoc
- Separación clara de responsabilidades
- Validaciones centralizadas
- Logging apropiado

## Cumplimiento de Requirements

✅ Requirement 7.1: Creación de facturas con empresa y timbrado  
✅ Requirement 7.2: Generación automática de número de factura  
✅ Requirement 7.3: Autocompletado de datos del cliente  
✅ Requirement 7.4: Agregar items con productos  
✅ Requirement 7.5: Cálculo de totales por tasa de IVA  
✅ Requirement 7.6: Aplicación de descuento final  
✅ Requirement 7.7: Validación de al menos un item  
✅ Requirement 8.1: Asociación de items a factura  
✅ Requirement 8.2: Cantidad con decimales  
✅ Requirement 8.3: Modificación de precio unitario  
✅ Requirement 8.4: Cálculo de total por item  
✅ Requirement 8.5: Recálculo automático de totales  
✅ Requirement 8.6: Eliminación de items con recálculo  
✅ Requirement 16.2: Filtros por empresa, fecha, cliente  
✅ Requirement 19.5: Endpoints REST completos  
