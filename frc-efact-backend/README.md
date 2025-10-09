# FRC eFact Backend

Backend component of the FRC eFact web application built with Spring Boot 3.2+ and PostgreSQL.

## Technology Stack

- **Framework**: Spring Boot 3.2+
- **Security**: Spring Security 6+ with JWT
- **Database**: PostgreSQL 15+
- **Migrations**: Flyway
- **API Documentation**: OpenAPI 3 (Swagger)
- **Build Tool**: Maven 3.9+
- **Java Version**: 17+
- **Connection Pool**: HikariCP

## Estructura del Proyecto

```
src/
├── main/
│   ├── java/
│   │   └── com/frcefact/
│   │       ├── FrcEfactBackendApplication.java
│   │       ├── config/          # Configuraciones
│   │       ├── controller/      # REST Controllers
│   │       ├── service/         # Servicios de negocio
│   │       ├── repository/      # Repositorios JPA
│   │       ├── entity/          # Entidades JPA
│   │       ├── dto/             # Data Transfer Objects
│   │       └── security/        # Configuración de seguridad
│   └── resources/
│       ├── db/migration/        # Scripts Flyway
│       ├── application.yml      # Configuración principal
│       └── application-prod.yml # Configuración producción
└── test/                        # Tests unitarios e integración
```

## Requisitos Previos

- Java 17+
- Maven 3.9+
- PostgreSQL 15+ (para desarrollo local)

## Configuración de Desarrollo

### 1. Base de Datos Local

```bash
# Crear base de datos PostgreSQL
createdb frc_efact_dev

# Configurar usuario (opcional)
psql -d frc_efact_dev -c "CREATE USER frc_efact WITH PASSWORD 'password';"
psql -d frc_efact_dev -c "GRANT ALL PRIVILEGES ON DATABASE frc_efact_dev TO frc_efact;"
```

### 2. Variables de Entorno

Crear archivo `application-local.yml` (no versionado):

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/frc_efact_dev
    username: frc_efact
    password: password
  
jwt:
  secret: your-local-jwt-secret-key-here
  expiration: 86400000
```

### 3. Ejecutar la Aplicación

```bash
# Compilar y ejecutar
./mvnw spring-boot:run

# O con perfil específico
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

## Endpoints Principales

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/v3/api-docs
- **Health Check**: http://localhost:8080/actuator/health

### Autenticación

- `POST /api/auth/login` - Login de usuario
- `POST /api/auth/refresh` - Refresh token
- `POST /api/auth/logout` - Logout

### Usuario

- `GET /api/users/profile` - Obtener perfil del usuario autenticado

Para documentación completa de la API, ver [API_DOCUMENTATION.md](./API_DOCUMENTATION.md)

## Testing

```bash
# Ejecutar todos los tests
./mvnw test

# Ejecutar tests con coverage
./mvnw test jacoco:report
```

## Build para Producción

```bash
# Crear JAR optimizado
./mvnw clean package -Dmaven.test.skip=true

# El JAR se genera en target/frc-efact-backend-*.jar
```

## Deployment

### Deployment en Render (Recomendado: Docker)

El proyecto incluye un `Dockerfile` optimizado para deployment en Render:

**Configuración en Render:**
- **Runtime**: Docker
- **Dockerfile Path**: `frc-efact-backend/Dockerfile`
- **Docker Context**: `frc-efact-backend`
- **Health Check**: `/actuator/health`

**Variables de Entorno en Render:**
```
DATABASE_URL=postgresql://...
JWT_SECRET=generated-secure-key
JWT_EXPIRATION=86400000
SPRING_PROFILES_ACTIVE=prod
CORS_ALLOWED_ORIGINS=https://frc-efact-frontend.onrender.com
LOG_LEVEL=INFO
```

### Test Docker Localmente

Antes de desplegar, puedes probar el Dockerfile localmente:

```bash
# Probar build y ejecución de Docker
./test-docker.sh

# O manualmente:
docker build -t frc-efact-backend .
docker run -p 8080:8080 \
  -e DATABASE_URL="jdbc:postgresql://host.docker.internal:5432/frc_efact_db?user=postgres&password=postgres" \
  -e JWT_SECRET="test-secret" \
  frc-efact-backend
```

### Deployment Alternativo (Java Buildpack)

Si prefieres no usar Docker:

1. Crear `system.properties`:
   ```properties
   java.runtime.version=17
   maven.version=3.9.6
   ```

2. Configurar en Render:
   - **Runtime**: Java
   - **Build Command**: `./mvnw clean package -DskipTests`
   - **Start Command**: `java -Dserver.port=$PORT -Dspring.profiles.active=prod -jar target/frc-efact-backend-*.jar`

**Nota:** Docker es más confiable y recomendado.

## Estándares de Base de Datos

Este proyecto sigue estándares específicos para la base de datos. Ver [DATABASE_STANDARDS.md](./DATABASE_STANDARDS.md) para detalles completos.

### Resumen de Estándares

- **Esquemas**: Todas las tablas organizadas en esquemas lógicos (ej: `persona.usuario`)
- **Nomenclatura**: Bilingüe - español para campos específicos, inglés para genéricos
- **Auditoría**: Campos obligatorios en todas las tablas: `id`, `creado_en`, `creado_por`, `actualizado_en`, `actualizado_por`
- **Triggers**: Actualización automática de `actualizado_en` mediante triggers
- **Documentación**: Comentarios SQL en tablas y columnas

### Ejemplo de Tabla

```sql
CREATE TABLE persona.usuario (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    
    -- Campos de auditoría (obligatorios)
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    creado_por VARCHAR(50),
    actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    actualizado_por VARCHAR(50)
);
```

## Desarrollo

Este proyecto sigue las convenciones estándar de Spring Boot y está preparado para ser un repositorio GitHub independiente.