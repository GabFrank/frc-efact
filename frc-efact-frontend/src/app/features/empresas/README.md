# Gestión de Usuarios de Empresas

Este módulo implementa la funcionalidad completa para gestionar la asignación de usuarios a empresas en el sistema FRC-eFact.

## Funcionalidades Implementadas

### Backend

#### Endpoints Disponibles
- `GET /api/empresas/{id}/usuarios` - Listar usuarios de una empresa
- `POST /api/empresas/{id}/usuarios` - Asignar usuario a empresa
- `DELETE /api/empresas/{id}/usuarios/{usuarioId}` - Remover usuario de empresa

#### Características de Seguridad
- **Control de acceso granular**: Solo usuarios con permisos pueden gestionar usuarios
- **Auto-asignación**: El creador de una empresa se asigna automáticamente como ADMINISTRADOR
- **Soft delete**: Los usuarios se marcan como inactivos en lugar de eliminarse físicamente

### Frontend

#### Componentes Principales
- `EmpresaUsuariosComponent` - Lista y gestión de usuarios de empresa
- `AddUserDialogComponent` - Dialog para agregar usuarios
- `EditUserDialogComponent` - Dialog para editar roles de usuarios

#### Características de UX
- **Estados de carga**: Loading spinner durante operaciones
- **Manejo de errores**: Mensajes de error claros
- **Confirmaciones**: Dialogs de confirmación para acciones destructivas
- **Navegación intuitiva**: Botón de regreso y breadcrumbs
- **Responsive**: Diseño adaptable a diferentes tamaños de pantalla

## Flujo de Trabajo

### 1. Creación de Empresa
1. Usuario `EMPRESA_ADMIN` crea una nueva empresa
2. Sistema auto-asigna al creador como `ADMINISTRADOR` de la empresa
3. Usuario puede acceder inmediatamente a gestionar usuarios

### 2. Gestión de Usuarios
1. Desde la lista de empresas, hacer clic en "Gestionar usuarios"
2. Ver lista de usuarios asignados a la empresa
3. Agregar nuevos usuarios seleccionando de la lista de usuarios del sistema
4. Asignar roles: `ADMINISTRADOR` o `LECTOR`
5. Editar roles existentes o remover usuarios según sea necesario

### 3. Control de Acceso
- **ADMIN del sistema**: Puede gestionar usuarios de cualquier empresa
- **EMPRESA_ADMIN**: Solo puede gestionar usuarios de empresas donde es ADMINISTRADOR
- **FACTURADOR**: Puede crear y gestionar facturas, productos y clientes

## Estructura de Archivos

```
frc-efact-frontend/src/app/features/empresas/
├── empresa-usuarios.component.ts    # Componente principal
├── empresa-info.component.ts       # Componente de información
├── empresas.routes.ts              # Rutas del módulo
└── index.ts                        # Exports del módulo

frc-efact-frontend/src/app/models/
└── usuario-empresa.model.ts        # Modelos de datos

frc-efact-frontend/src/app/core/api/
└── empresa-api.service.ts          # Servicio API actualizado
```

## Rutas Disponibles

- `/empresas` - Lista de empresas
- `/empresas/new` - Crear nueva empresa
- `/empresas/:id` - Información de empresa
- `/empresas/:id/edit` - Editar empresa
- `/empresas/:id/usuarios` - Gestionar usuarios de empresa

## Modelos de Datos

### UsuarioEmpresa
```typescript
interface UsuarioEmpresa {
  id: number;
  usuarioId: number;
  empresaId: number;
  rolEmpresa: 'ADMINISTRADOR' | 'LECTOR';
  activo: boolean;
  usuarioUsername?: string;
  empresaRazonSocial?: string;
  creadoEn: string;
  actualizadoEn: string;
}
```

### AsignarUsuarioEmpresaRequest
```typescript
interface AsignarUsuarioEmpresaRequest {
  usuarioId: number;
  rolEmpresa: 'ADMINISTRADOR' | 'LECTOR';
}
```

## Permisos y Roles

### Roles del Sistema
- **ADMIN**: Administrador del sistema (acceso completo)
- **EMPRESA_ADMIN**: Administrador de empresa
- **FACTURADOR**: Puede crear facturas y gestionar documentos electrónicos, pero no gestionar usuarios de la empresa
- **LECTOR**: Solo lectura

### Roles en Empresa
- **ADMINISTRADOR**: Puede gestionar usuarios y configuraciones de la empresa
- **FACTURADOR**: Puede crear facturas y gestionar documentos electrónicos
- **LECTOR**: Solo puede ver información de la empresa

## Próximos Pasos

1. **Pruebas**: Implementar tests unitarios y de integración
2. **Auditoría**: Agregar logs de auditoría para cambios de usuarios
3. **Notificaciones**: Implementar notificaciones por email cuando se asignan usuarios
4. **Bulk operations**: Permitir asignar múltiples usuarios a la vez
5. **Filtros avanzados**: Agregar filtros por rol, estado, fecha de asignación

## Consideraciones de Seguridad

- Todos los endpoints requieren autenticación
- Verificación de permisos en cada operación
- Validación de datos en frontend y backend
- Soft delete para mantener auditoría
- Logs de seguridad para operaciones sensibles
