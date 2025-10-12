# Módulo de Auditoría

## Descripción

El módulo de auditoría proporciona una interfaz completa para visualizar y analizar el historial de cambios en el sistema. Permite a los administradores rastrear todas las operaciones CRUD realizadas por los usuarios.

## Componentes

### AuditoriaListComponent

Componente principal que muestra una lista paginada de registros de auditoría con capacidades de filtrado avanzado.

**Características:**
- Filtros por tipo de entidad, acción, rango de fechas
- Tabla con información detallada de cada registro
- Paginación y ordenamiento
- Visualización de usuario, empresa, IP y fecha
- Acceso rápido al detalle de cambios
- Navegación al historial completo de una entidad

**Filtros disponibles:**
- Tipo de Entidad: Empresa, Factura, Cliente, Producto, Timbrado, Usuario
- Acción: Crear, Actualizar, Eliminar, Leer
- Fecha Desde / Fecha Hasta

### AuditoriaDetailComponent

Componente de diálogo que muestra el detalle completo de un cambio, incluyendo una comparación lado a lado de valores anteriores y nuevos.

**Características:**
- Visualización de información general del cambio
- Comparación visual de valores anteriores vs nuevos
- Resaltado de campos modificados
- Formato inteligente de valores (JSON, booleanos, etc.)
- Información del navegador y dirección IP

### AuditoriaEntidadComponent

Componente que muestra el historial completo de una entidad específica en formato de línea de tiempo.

**Características:**
- Timeline visual de todos los cambios
- Marcadores de color según tipo de acción
- Resumen de campos modificados en cada cambio
- Acceso rápido al detalle de cada cambio
- Animación en el cambio más reciente

## Modelos

### AuditLog

```typescript
interface AuditLog {
  id: number;
  usuario: {
    id: number;
    username: string;
    nombreCompleto?: string;
  };
  empresa?: {
    id: number;
    razonSocial: string;
  };
  entidadTipo: string;
  entidadId: number;
  accion: AccionEnum;
  fechaHora: string;
  valoresAnteriores?: Record<string, any>;
  valoresNuevos?: Record<string, any>;
  ipAddress?: string;
  userAgent?: string;
  descripcion?: string;
}
```

### AccionEnum

```typescript
enum AccionEnum {
  CREATE = 'CREATE',
  UPDATE = 'UPDATE',
  DELETE = 'DELETE',
  READ = 'READ'
}
```

## Servicios

### AuditApiService

Servicio que maneja todas las llamadas a la API de auditoría.

**Métodos principales:**
- `buscarConFiltros(filtros)`: Busca registros con filtros opcionales
- `getHistorialEntidad(tipo, id)`: Obtiene el historial completo de una entidad
- `getUltimasActividades(limit)`: Obtiene las actividades más recientes
- `getActividadesUsuario(usuarioId, limit)`: Obtiene actividades de un usuario
- `getEstadisticas(fechaDesde, fechaHasta, empresaId?)`: Obtiene estadísticas de auditoría
- `contarTotal()`: Cuenta el total de registros
- `contarPorEmpresa(empresaId)`: Cuenta registros por empresa

## Rutas

```typescript
/auditoria                    // Lista de registros de auditoría
/auditoria/entidad/:tipo/:id  // Historial de una entidad específica
```

## Seguridad

El módulo está protegido con el `roleGuard` y solo es accesible para usuarios con rol **ADMIN**.

## Uso

### Navegación desde otros módulos

```typescript
// Navegar al historial de una entidad
this.router.navigate(['/auditoria/entidad', 'Factura', facturaId]);

// Navegar a la lista de auditoría
this.router.navigate(['/auditoria']);
```

### Visualización de cambios

1. Acceder a `/auditoria` para ver todos los registros
2. Aplicar filtros según necesidad
3. Hacer clic en el ícono de "ojo" para ver el detalle de un cambio UPDATE
4. Hacer clic en el ícono de "historial" para ver todos los cambios de una entidad

## Estilos

El módulo utiliza un esquema de colores consistente:
- **Verde (#4caf50)**: Operaciones CREATE
- **Azul (#2196f3)**: Operaciones UPDATE
- **Rojo (#f44336)**: Operaciones DELETE
- **Gris (#9e9e9e)**: Operaciones READ

## Dependencias

- Angular Material (Cards, Tables, Dialogs, Chips, etc.)
- RxJS para manejo de observables
- Angular Router para navegación
- Componentes compartidos (LoadingSpinner, ErrorMessage)

## Requisitos cumplidos

- ✅ 17.4: Consulta de historial con filtros
- ✅ 17.5: Búsqueda por usuario, fecha, entidad y acción
- ✅ 17.6: Visualización de valores anteriores/nuevos con diff

## Mejoras futuras

- Exportación de registros de auditoría a Excel/PDF
- Gráficos de estadísticas de auditoría
- Notificaciones en tiempo real de cambios críticos
- Búsqueda de texto completo en valores JSON
- Comparación de múltiples versiones de una entidad
