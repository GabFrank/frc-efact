# Flujo del Sistema - Manejo de Entidades

## Introducción

Este documento describe el flujo completo para el manejo de entidades en el sistema FRC-eFact, desde la capa de datos en el backend hasta la presentación en el frontend. El sistema sigue una arquitectura en capas bien definida que separa las responsabilidades y facilita el mantenimiento.

## Arquitectura General

El sistema está dividido en dos aplicaciones principales:
- **Backend**: Spring Boot con arquitectura en capas (Java)
- **Frontend**: Angular con NgRx para manejo de estado

---

## BACKEND - Spring Boot

### 1. Entidad Java (Model Layer)

**Ubicación**: `frc-efact-backend/src/main/java/com/frcefact/model/`

**Propósito**: Representar las tablas de la base de datos como objetos Java.

**Estructura típica**:
- Anotaciones JPA (`@Entity`, `@Table`, `@Id`, etc.)
- Campos con anotaciones de validación
- Relaciones entre entidades (`@OneToMany`, `@ManyToOne`, etc.)
- Herencia de clase base para auditoría

**Ejemplos de referencia**:
- `Usuario.java` - Entidad principal de usuarios
- `Empresa.java` - Entidad de empresas
- `Cliente.java` - Entidad de clientes
- `Producto.java` - Entidad de productos
- `FacturaLegal.java` - Entidad de facturas

**Clase base**: `frc-efact-backend/src/main/java/com/frcefact/model/base/` (para auditoría)

### 2. Repository (Data Access Layer)

**Ubicación**: `frc-efact-backend/src/main/java/com/frcefact/repository/`

**Propósito**: Interfaz para acceso a datos, extiende JpaRepository.

**Características**:
- Métodos CRUD automáticos
- Consultas personalizadas con `@Query`
- Métodos de búsqueda por convención de nombres

**Ejemplos de referencia**:
- `UsuarioRepository.java`
- `EmpresaRepository.java`
- `ClienteRepository.java`
- `ProductoRepository.java`
- `FacturaLegalRepository.java`

### 3. DTO (Data Transfer Objects)

**Ubicación**: `frc-efact-backend/src/main/java/com/frcefact/dto/`

**Propósito**: Objetos para transferir datos entre capas, especialmente hacia el frontend.

**Tipos de DTOs**:
- DTOs de entidad principal (ej: `UsuarioDto.java`)
- DTOs de request (ej: `CreateUserRequest.java`, `UpdateUserRequest.java`)
- DTOs de response específicos (ej: `AuthResponse.java`)
- DTOs para reportes (ej: `ClienteRankingDto.java`)

**Mappers**: `frc-efact-backend/src/main/java/com/frcefact/dto/mapper/`

### 4. Service (Business Logic Layer)

**Ubicación**: `frc-efact-backend/src/main/java/com/frcefact/service/`

**Propósito**: Contiene la lógica de negocio y orquesta las operaciones.

**Responsabilidades**:
- Validaciones de negocio
- Transformación de datos (Entity ↔ DTO)
- Coordinación entre múltiples repositorios
- Manejo de transacciones

**Ejemplos de referencia**:
- `UsuarioService.java`
- `EmpresaService.java`
- `ClienteService.java`
- `ProductoService.java`
- `FacturaLegalService.java`

### 5. Controller (Presentation Layer)

**Ubicación**: `frc-efact-backend/src/main/java/com/frcefact/controller/`

**Propósito**: Exponer endpoints REST y manejar requests HTTP.

**Características**:
- Anotaciones REST (`@RestController`, `@RequestMapping`)
- Validación de entrada (`@Valid`)
- Manejo de respuestas HTTP
- Documentación OpenAPI/Swagger

**Ejemplos de referencia**:
- `UsuarioController.java`
- `EmpresaController.java`
- `ClienteController.java`
- `ProductoController.java`
- `FacturaLegalController.java`

### 6. Componentes Adicionales del Backend

#### Validadores Personalizados
**Ubicación**: `frc-efact-backend/src/main/java/com/frcefact/validation/`
- Validaciones específicas del dominio (RUC, CDC, etc.)

#### Manejo de Excepciones
**Ubicación**: `frc-efact-backend/src/main/java/com/frcefact/exception/`
- `GlobalExceptionHandler.java` - Manejo centralizado de errores

#### Seguridad
**Ubicación**: `frc-efact-backend/src/main/java/com/frcefact/security/`
- JWT, autenticación y autorización

#### Auditoría
**Ubicación**: `frc-efact-backend/src/main/java/com/frcefact/aspect/`
- `AuditAspect.java` - Logging automático de operaciones

---

## FRONTEND - Angular

### 1. Modelo (TypeScript Interfaces)

**Ubicación**: `frc-efact-frontend/src/app/models/`

**Propósito**: Definir la estructura de datos que maneja el frontend.

**Características**:
- Interfaces TypeScript
- Corresponden a los DTOs del backend
- Tipado fuerte para desarrollo

**Ejemplos de referencia**:
- `user.model.ts`
- `empresa.model.ts`
- `cliente.model.ts`
- `producto.model.ts`
- `factura.model.ts`

### 2. API Service (HTTP Client Layer)

**Ubicación**: `frc-efact-frontend/src/app/core/api/`

**Propósito**: Comunicación HTTP con el backend.

**Características**:
- Métodos para operaciones CRUD
- Manejo de headers y autenticación
- Transformación de datos
- Manejo de errores HTTP

**Ejemplos de referencia**:
- `usuario-api.service.ts`
- `empresa-api.service.ts`
- `cliente-api.service.ts`
- `producto-api.service.ts`
- `factura-api.service.ts`

### 3. NgRx State Management

#### Actions
**Ubicación**: `frc-efact-frontend/src/app/core/state/[entidad]/[entidad].actions.ts`

**Propósito**: Definir las acciones que pueden ocurrir en el estado.

**Tipos de acciones típicas**:
- Load (cargar datos)
- Load Success/Failure
- Create, Update, Delete
- Select (seleccionar item)

**Ejemplos de referencia**:
- `frc-efact-frontend/src/app/core/state/usuarios/usuarios.actions.ts`
- `frc-efact-frontend/src/app/core/state/empresas/empresas.actions.ts`

#### Effects
**Ubicación**: `frc-efact-frontend/src/app/core/state/[entidad]/[entidad].effects.ts`

**Propósito**: Manejar efectos secundarios (llamadas HTTP, navegación, etc.).

**Responsabilidades**:
- Llamadas a API services
- Transformación de datos
- Manejo de errores
- Navegación automática

**Ejemplos de referencia**:
- `frc-efact-frontend/src/app/core/state/usuarios/usuarios.effects.ts`
- `frc-efact-frontend/src/app/core/state/empresas/empresas.effects.ts`

#### Reducers
**Ubicación**: `frc-efact-frontend/src/app/core/state/[entidad]/[entidad].reducer.ts`

**Propósito**: Manejar cambios de estado de forma inmutable.

**Características**:
- Estado inicial
- Manejo de loading states
- Actualización inmutable del estado

**Ejemplos de referencia**:
- `frc-efact-frontend/src/app/core/state/usuarios/usuarios.reducer.ts`
- `frc-efact-frontend/src/app/core/state/empresas/empresas.reducer.ts`

#### Selectors
**Ubicación**: `frc-efact-frontend/src/app/core/state/[entidad]/[entidad].selectors.ts`

**Propósito**: Seleccionar y derivar datos del estado.

**Ejemplos de referencia**:
- `frc-efact-frontend/src/app/core/state/usuarios/usuarios.selectors.ts`
- `frc-efact-frontend/src/app/core/state/empresas/empresas.selectors.ts`

### 4. Servicios de Negocio (Opcional)

**Ubicación**: `frc-efact-frontend/src/app/services/`

**Propósito**: Lógica de negocio específica del frontend.

**Ejemplos**:
- `auth.service.ts` - Manejo de autenticación
- `dashboard.service.ts` - Lógica del dashboard
- `ruc-validation.service.ts` - Validaciones específicas

### 5. Componentes de Lista

**Ubicación**: `frc-efact-frontend/src/app/features/[entidad]/[entidad]-list.component.ts`

**Propósito**: Mostrar listados de entidades con funcionalidades de búsqueda, filtrado y paginación.

**Características típicas**:
- Tabla con datos
- Búsqueda y filtros
- Paginación
- Acciones (editar, eliminar, ver)
- Integración con NgRx store

**Ejemplos de referencia**:
- `frc-efact-frontend/src/app/features/usuarios/usuarios-list.component.ts`
- `frc-efact-frontend/src/app/features/empresas/empresas-list.component.ts`
- `frc-efact-frontend/src/app/features/clientes/clientes-list.component.ts`
- `frc-efact-frontend/src/app/features/productos/productos-list.component.ts`

### 6. Componentes de Formulario

**Ubicación**: `frc-efact-frontend/src/app/features/[entidad]/[entidad]-form.component.ts`

**Propósito**: Crear y editar entidades.

**Características típicas**:
- Reactive Forms
- Validaciones
- Manejo de estados (crear/editar)
- Integración con NgRx store
- Navegación después de guardar

**Ejemplos de referencia**:
- `frc-efact-frontend/src/app/features/usuarios/usuario-form.component.ts`
- `frc-efact-frontend/src/app/features/empresas/empresa-form.component.ts`
- `frc-efact-frontend/src/app/features/clientes/cliente-form.component.ts`
- `frc-efact-frontend/src/app/features/productos/producto-form.component.ts`

### 7. Routing

**Ubicación**: `frc-efact-frontend/src/app/features/[entidad]/[entidad].routes.ts`

**Propósito**: Definir las rutas para cada módulo de entidad.

**Rutas típicas**:
- Lista: `/entidades`
- Crear: `/entidades/nuevo`
- Editar: `/entidades/:id/editar`
- Ver: `/entidades/:id`

**Ejemplos de referencia**:
- `frc-efact-frontend/src/app/features/usuarios/usuarios.routes.ts`
- `frc-efact-frontend/src/app/features/empresas/empresas.routes.ts`

---

## Flujo Completo de Datos

### 1. Flujo de Lectura (GET)
```
Frontend Component → NgRx Action → Effect → API Service → HTTP Request → 
Backend Controller → Service → Repository → Database → 
Entity → DTO → JSON Response → Frontend Model → NgRx State → Component View
```

### 2. Flujo de Escritura (POST/PUT)
```
Frontend Form → NgRx Action → Effect → API Service → HTTP Request → 
Backend Controller → Validation → Service → Business Logic → Repository → Database → 
Entity → DTO → JSON Response → NgRx State Update → Component Navigation
```

### 3. Flujo de Eliminación (DELETE)
```
Frontend Component → Confirmation → NgRx Action → Effect → API Service → HTTP Request → 
Backend Controller → Service → Repository → Database → 
Success Response → NgRx State Update → Component Refresh
```

---

## Patrones y Convenciones

### Backend
- **Naming**: Entidades en singular, repositorios con sufijo "Repository"
- **DTOs**: Sufijo "Dto" para entidades, "Request" para inputs
- **Services**: Lógica de negocio, transacciones con `@Transactional`
- **Controllers**: Solo manejo HTTP, delegación a services

### Frontend
- **Naming**: Archivos kebab-case, clases PascalCase
- **State**: Un store por entidad principal
- **Components**: Separación clara entre lista y formulario
- **Services**: Solo para lógica específica del frontend

---

## Herramientas de Desarrollo

### Backend
- **Base de datos**: PostgreSQL con Flyway para migraciones
- **Documentación**: OpenAPI/Swagger automático
- **Testing**: JUnit para pruebas unitarias

### Frontend
- **UI**: Angular Material para componentes
- **State**: NgRx para manejo de estado
- **HTTP**: Interceptors para autenticación y manejo de errores

---

## Consideraciones de Seguridad

### Backend
- JWT para autenticación
- Validación en múltiples capas
- Auditoría automática de operaciones

### Frontend
- Guards para protección de rutas
- Interceptors para manejo de tokens
- Validación de formularios

---

Este documento proporciona una guía completa para entender y trabajar con el flujo de entidades en el sistema FRC-eFact. Cada nueva entidad debe seguir estos patrones para mantener la consistencia y facilitar el mantenimiento del código.