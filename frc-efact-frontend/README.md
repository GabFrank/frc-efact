# FRC eFact Frontend

Frontend de la aplicación web FRC eFact construido con Angular 17 y Angular Material.

## Stack Tecnológico

- **Framework**: Angular 17 (Standalone Components)
- **UI Library**: Angular Material 17
- **Lenguaje**: TypeScript 5+
- **Estilos**: SCSS + Angular Flex Layout
- **HTTP Client**: Angular HttpClient con interceptors
- **Routing**: Angular Router con lazy loading
- **Build Tool**: Angular CLI 17
- **Node Version**: 18+

## Estructura del Proyecto

```
src/
├── app/
│   ├── components/          # Componentes de UI
│   │   ├── login/          # Componente de login
│   │   └── welcome/        # Página de bienvenida
│   ├── services/           # Servicios de negocio
│   │   └── auth.service.ts # Servicio de autenticación
│   ├── guards/             # Guards de routing
│   │   ├── auth.guard.ts   # Protección de rutas privadas
│   │   └── no-auth.guard.ts # Redirección de usuarios autenticados
│   ├── interceptors/       # HTTP Interceptors
│   │   ├── auth.interceptor.ts   # Inyección de JWT
│   │   ├── error.interceptor.ts  # Manejo de errores
│   │   └── https.interceptor.ts  # Forzar HTTPS
│   ├── models/             # Interfaces TypeScript
│   │   ├── user.model.ts
│   │   ├── login-request.model.ts
│   │   └── auth-response.model.ts
│   ├── app.component.ts    # Componente raíz
│   ├── app.config.ts       # Configuración de la app
│   └── app.routes.ts       # Definición de rutas
├── environments/           # Configuraciones de entorno
│   ├── environment.ts      # Desarrollo
│   └── environment.prod.ts # Producción
├── assets/                 # Recursos estáticos
├── styles.scss            # Estilos globales
└── index.html             # HTML principal
```

## Requisitos Previos

- Node.js 18+ y npm 9+
- Angular CLI 17: `npm install -g @angular/cli@17`

## Configuración de Desarrollo

### 1. Instalar Dependencias

```bash
npm install
```

### 2. Configurar Variables de Entorno

El archivo `src/environments/environment.ts` ya está configurado para desarrollo local:

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
```

Para producción, editar `src/environments/environment.prod.ts`:

```typescript
export const environment = {
  production: true,
  apiUrl: 'https://frc-efact-backend.onrender.com/api'
};
```

### 3. Ejecutar Servidor de Desarrollo

```bash
# Iniciar servidor de desarrollo
npm start

# O usando Angular CLI directamente
ng serve

# Con puerto personalizado
ng serve --port 4200

# Con proxy para evitar CORS en desarrollo
ng serve --proxy-config proxy.conf.json
```

La aplicación estará disponible en `http://localhost:4200/`

## Scripts Disponibles

```bash
# Desarrollo
npm start                    # Servidor de desarrollo
npm run build               # Build de producción
npm run build:dev           # Build de desarrollo
npm run watch               # Build con watch mode

# Testing
npm test                    # Ejecutar tests unitarios
npm run test:ci             # Tests en modo CI (sin watch)
npm run test:coverage       # Tests con reporte de cobertura

# Linting y Formato
npm run lint                # Ejecutar ESLint
npm run lint:fix            # Corregir problemas de linting automáticamente

# Análisis
npm run analyze             # Analizar tamaño del bundle
```

## Características Principales

### Autenticación JWT

- Login con username/password
- Almacenamiento seguro de tokens en memoria
- Refresh automático de tokens
- Interceptor para inyección automática de JWT en requests
- Guards para protección de rutas

### Componentes

#### LoginComponent
- Formulario reactivo con validaciones
- Manejo de errores de autenticación
- Redirección automática después del login
- Diseño responsivo con Material Design

#### WelcomeComponent
- Dashboard de bienvenida
- Información del usuario autenticado
- Navegación principal
- Funcionalidad de logout

### Guards de Routing

- **AuthGuard**: Protege rutas que requieren autenticación
- **NoAuthGuard**: Redirige usuarios autenticados (ej: página de login)

### HTTP Interceptors

- **AuthInterceptor**: Inyecta JWT token en headers
- **ErrorInterceptor**: Manejo centralizado de errores HTTP
- **HttpsInterceptor**: Fuerza HTTPS en producción

## Build para Producción

```bash
# Build optimizado para producción
npm run build

# Build con análisis de bundle
npm run build -- --stats-json
npm run analyze

# Los archivos se generan en dist/frc-efact-frontend/
```

### Optimizaciones de Build

- **AOT Compilation**: Compilación ahead-of-time
- **Tree Shaking**: Eliminación de código no usado
- **Minification**: Minificación de JS y CSS
- **Lazy Loading**: Carga diferida de módulos
- **Service Worker**: PWA capabilities (opcional)

## Deployment en Render

El proyecto está configurado para deployment automático en Render:

### Configuración de Render

- **Build Command**: `npm ci && npm run build`
- **Publish Directory**: `dist/frc-efact-frontend/browser`
- **Node Version**: 18.x
- **Auto-Deploy**: Habilitado desde rama main

### Variables de Entorno en Render

```bash
NODE_ENV=production
API_BASE_URL=https://frc-efact-backend.onrender.com
```

### Archivo _redirects

El archivo `src/_redirects` configura el routing para SPA:

```
/*    /index.html   200
```

## Desarrollo de Componentes

### Generar Nuevo Componente

```bash
# Componente standalone
ng generate component components/mi-componente --standalone

# Servicio
ng generate service services/mi-servicio

# Guard
ng generate guard guards/mi-guard

# Interceptor
ng generate interceptor interceptors/mi-interceptor
```

### Convenciones de Código

- **Componentes**: PascalCase (ej: `LoginComponent`)
- **Archivos**: kebab-case (ej: `login.component.ts`)
- **Servicios**: Sufijo `Service` (ej: `AuthService`)
- **Guards**: Sufijo `Guard` (ej: `AuthGuard`)
- **Interfaces**: PascalCase (ej: `User`, `LoginRequest`)

## Testing

### Tests Unitarios

```bash
# Ejecutar tests
npm test

# Tests con cobertura
npm run test:coverage

# Tests en modo CI (sin watch)
npm run test:ci
```

### Estructura de Tests

```typescript
describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService]
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('should authenticate user', () => {
    // Test implementation
  });
});
```

## Seguridad

### Mejores Prácticas Implementadas

- ✅ JWT tokens almacenados en memoria (no en localStorage)
- ✅ HTTPS forzado en producción
- ✅ Sanitización automática de HTML por Angular
- ✅ CORS configurado correctamente
- ✅ Headers de seguridad (CSP, X-Frame-Options)
- ✅ Validación de formularios en cliente y servidor
- ✅ Manejo seguro de errores sin exponer información sensible

Ver [SECURITY.md](./SECURITY.md) para más detalles.

## Troubleshooting

### Error de CORS en Desarrollo

Si encuentras errores de CORS, asegúrate de que el backend esté configurado para permitir `http://localhost:4200`:

```java
// Backend: SecurityConfig.java
configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
```

### Error de Conexión al Backend

Verifica que:
1. El backend esté ejecutándose en `http://localhost:8080`
2. La URL en `environment.ts` sea correcta
3. No haya firewall bloqueando la conexión

### Build Falla por Memoria

Si el build falla por falta de memoria:

```bash
# Aumentar memoria de Node.js
export NODE_OPTIONS="--max-old-space-size=4096"
npm run build
```

## Recursos Adicionales

- [Angular Documentation](https://angular.io/docs)
- [Angular Material](https://material.angular.io/)
- [RxJS Documentation](https://rxjs.dev/)
- [TypeScript Handbook](https://www.typescriptlang.org/docs/)

## Documentación Relacionada

- [DEPLOYMENT.md](./DEPLOYMENT.md) - Guía de deployment
- [SECURITY.md](./SECURITY.md) - Consideraciones de seguridad
- [COMPONENT_TESTING.md](./COMPONENT_TESTING.md) - Guía de testing

## Soporte

Para problemas o preguntas, consultar la documentación del proyecto o crear un issue en el repositorio de GitHub.
