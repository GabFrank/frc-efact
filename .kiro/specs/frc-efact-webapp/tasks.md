# Implementation Plan

- [x] 1. Crear estructura de proyecto y configuración inicial
  - [x] 1.1 Crear estructura de directorios del proyecto
    - Crear directorio raíz frc-efact-webapp con subcarpetas frc-efact-backend y frc-efact-frontend
    - Crear .gitignore principal y archivos de configuración del workspace
    - _Requirements: 1.1, 1.4_
  
  - [x] 1.2 Inicializar repositorios Git independientes
    - Configurar Git en cada subcarpeta con repositorios independientes
    - Crear .gitignore específicos para Java/Maven y Node.js/Angular
    - Preparar estructura para repositorios GitHub separados
    - _Requirements: 1.2, 1.3_

- [x] 2. Configurar proyecto backend Spring Boot
  - [x] 2.1 Generar proyecto Spring Boot con dependencias necesarias
    - Crear proyecto Maven con Spring Boot 3.2+, Spring Security, Spring Data JPA, PostgreSQL driver, Flyway
    - Configurar estructura de paquetes siguiendo convenciones de Spring Boot
    - _Requirements: 2.1, 2.3, 2.6_

  - [x] 2.2 Configurar conexión a base de datos PostgreSQL
    - Implementar configuración de DataSource para PostgreSQL
    - Crear archivos de configuración para diferentes environments (dev, prod)
    - Configurar HikariCP connection pool
    - _Requirements: 2.2, 2.6_

  - [x] 2.3 Implementar configuración de Flyway para migraciones
    - Configurar Flyway en application.yml
    - Crear estructura de directorios para migraciones
    - Implementar migración inicial para tabla de usuarios
    - _Requirements: 8.1, 8.2, 8.3, 8.4_

- [x] 3. Implementar sistema de autenticación JWT
  - [x] 3.1 Crear modelo de datos User y repositorio
    - Implementar entidad User con JPA annotations y validaciones
    - Crear UserRepository con Spring Data JPA y queries personalizadas
    - Implementar UserService con lógica de negocio de usuarios
    - Crear migración Flyway V2 para tabla users con índices
    - _Requirements: 4.2, 4.5, 8.2_

  - [x] 3.2 Implementar JWT token provider y configuración de seguridad
    - Crear JwtTokenProvider para generación, validación y refresh de tokens
    - Configurar Spring Security con JWT authentication filter
    - Implementar UserDetailsService personalizado
    - Configurar password encoding con BCrypt
    - _Requirements: 4.5, 4.4_

  - [x] 3.3 Crear controladores de autenticación REST
    - Implementar AuthController con endpoints /login, /refresh, /logout
    - Crear DTOs (LoginRequest, AuthResponse, UserDto) con validaciones
    - Implementar GlobalExceptionHandler para errores de autenticación
    - Configurar rate limiting básico para endpoints de login
    - _Requirements: 4.1, 4.2, 4.6_

- [ ] 4. Configurar APIs REST y documentación
  - [x] 4.1 Implementar configuración OpenAPI/Swagger
    - Configurar Swagger UI para documentación automática de APIs
    - Crear configuración OpenAPI con información del proyecto
    - Documentar endpoints de autenticación con annotations
    - _Requirements: 2.4, 7.3_

  - [x] 4.2 Crear endpoint para perfil de usuario
    - Implementar UserController con endpoint para obtener perfil
    - Crear DTOs para respuestas de usuario
    - Implementar validación de autorización
    - _Requirements: 5.2, 4.4_

  - [ ]* 4.3 Configurar GraphQL como alternativa a REST
    - Implementar GraphQL schema para User y Auth
    - Crear resolvers para queries y mutations
    - Configurar GraphiQL para testing
    - _Requirements: 2.5_

- [x] 5. Configurar proyecto frontend Angular 17
  - [x] 5.1 Generar proyecto Angular con configuración inicial
    - Crear proyecto Angular 17 con Angular CLI (routing, SCSS, standalone components)
    - Instalar y configurar Angular Material 17 con tema personalizado
    - Configurar estructura de carpetas (components, services, guards, models)
    - Configurar environments para desarrollo y producción
    - _Requirements: 3.1, 3.2, 3.4_

  - [x] 5.2 Implementar servicios core y configuración HTTP
    - Crear AuthService para manejo de JWT tokens y estado de autenticación
    - Implementar HTTP interceptors para autenticación automática y manejo de errores
    - Configurar HttpClient con base URL y timeout
    - Crear modelos TypeScript (User, LoginRequest, AuthResponse)
    - _Requirements: 4.1, 4.4, 4.5_

  - [x] 5.3 Configurar routing y guards de seguridad
    - Implementar AuthGuard para protección de rutas privadas
    - Configurar lazy loading para módulos de funcionalidades
    - Crear guards de redirección para usuarios ya autenticados
    - Implementar resolver para datos de usuario
    - _Requirements: 4.4, 5.4_

- [x] 6. Implementar componentes de UI principales
  - [x] 6.1 Crear componente de login
    - Implementar formulario de login con Angular Reactive Forms
    - Crear validaciones de campos y manejo de errores
    - Implementar integración con AuthService
    - _Requirements: 4.1, 4.2, 4.6_

  - [x] 6.2 Crear componente de página de bienvenida
    - Implementar dashboard básico con información del usuario
    - Crear navegación principal con Material Design
    - Implementar funcionalidad de logout
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5_

  - [x] 6.3 Implementar routing y navegación
    - Configurar rutas principales (login, welcome, protected routes)
    - Implementar guards de autenticación para rutas protegidas
    - Crear componente de layout principal
    - _Requirements: 4.4, 5.4_

- [x] 7. Configurar deployment para Render
  - [x] 7.1 Configurar backend para deployment en Render
    - Crear application-prod.yml con configuración de producción
    - Configurar variables de entorno necesarias (DATABASE_URL, JWT_SECRET)
    - Implementar Spring Actuator para health checks (/actuator/health)
    - Crear Dockerfile o configuración de build para Render
    - _Requirements: 6.1, 6.3, 6.5_

  - [x] 7.2 Configurar frontend para deployment en Render
    - Configurar environment.prod.ts con URLs de producción
    - Optimizar build de Angular para producción (AOT, tree-shaking)
    - Configurar routing para SPA en servidor estático
    - Crear scripts de build optimizados
    - _Requirements: 6.1, 6.4, 6.5_

  - [x] 7.3 Implementar configuración de seguridad CORS y HTTPS
    - Configurar CORS en Spring Boot para permitir frontend de Render
    - Implementar headers de seguridad SSL/HTTPS (HSTS, CSP)
    - Configurar JWT cookies como secure y httpOnly
    - Implementar redirección automática HTTP a HTTPS
    - _Requirements: 6.5, 6.6_

- [x] 8. Crear documentación y scripts de desarrollo
  - [x] 8.1 Crear documentación completa del proyecto en espanhol
    - Escribir README detallado para backend con instrucciones de setup
    - Escribir README detallado para frontend con instrucciones de desarrollo
    - Documentar APIs y endpoints disponibles
    - _Requirements: 7.1, 7.3, 7.5_

  - [x] 8.2 Configurar scripts de desarrollo y build
    - Crear scripts Maven para desarrollo y testing del backend
    - Configurar scripts npm para desarrollo y build del frontend
    - Implementar scripts de setup para base de datos local
    - _Requirements: 7.2, 7.4_

  - [ ]* 8.3 Configurar testing automatizado
    - Implementar tests unitarios para AuthService y UserRepository
    - Crear tests de componentes para LoginComponent y WelcomeComponent
    - Configurar GitHub Actions para CI/CD con testing automático
    - _Requirements: 7.4_

- [x] 9. Integración y deployment final
  - [x] 9.1 Verificar integración completa en desarrollo
    - Probar flujo completo de autenticación entre backend y frontend
    - Verificar manejo de errores y validaciones
    - Confirmar navegación y protección de rutas
    - Validar responsive design en diferentes dispositivos
    - _Requirements: 4.1, 4.2, 4.3, 5.1, 3.3_

  - [x] 9.2 Ejecutar deployment inicial en Render
    - Crear y configurar servicios web en Render (backend y frontend)
    - Configurar base de datos PostgreSQL managed en Render
    - Ejecutar migraciones de Flyway en producción
    - Verificar conectividad y funcionamiento HTTPS
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.6_

  - [x] 9.3 Validar deployment y crear documentación final
    - Probar aplicación completa en producción (login, welcome, logout)
    - Verificar performance y tiempos de respuesta
    - Actualizar documentación con URLs de producción
    - Crear guía de troubleshooting básico
    - _Requirements: 6.6, 7.1, 7.5_