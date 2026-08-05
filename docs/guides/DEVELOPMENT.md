# Guía de Desarrollo Local

Cómo trabajar con el proyecto en tu máquina local.

## 🚀 Inicio Rápido

### 1. Pre-requisitos

- Java 17+
- Node.js 18+
- PostgreSQL 15+
- Maven 3.8+

### 2. Configurar Base de Datos Local

El perfil `dev` (`application-dev.yml`) apunta a:

```
jdbc:postgresql://172.25.0.36:5551/frc_efact_dev
```

(usuario/contraseña por defecto `postgres`/`postgres`, override con `DB_USERNAME` / `DB_PASSWORD`).

PostgreSQL **no crea la base automáticamente**; debe existir previamente:

```bash
# Crear la base de datos de desarrollo (ajusta host/puerto/usuario a tu entorno)
psql -h 172.25.0.36 -p 5551 -U postgres -c "CREATE DATABASE frc_efact_dev;"

# O usar el script incluido
cd frc-efact-backend
./setup-local-db.sh
```

> Si trabajas contra un PostgreSQL local, ajusta la `url` en `application-dev.yml` (por ejemplo `jdbc:postgresql://localhost:5432/frc_efact_dev`). El esquema lo gestiona Flyway al arrancar (`ddl-auto: validate`).

### 3. Iniciar Backend

```bash
cd frc-efact-backend
./dev.sh
```

El backend estará disponible en `http://localhost:8080`

### 4. Iniciar Frontend

```bash
cd frc-efact-frontend
./dev.sh
```

El frontend estará disponible en `http://localhost:4200`

## 🔧 Configuración

### Backend

Edita `frc-efact-backend/src/main/resources/application-dev.yml` (perfil `dev`):

```yaml
spring:
  datasource:
    url: jdbc:postgresql://172.25.0.36:5551/frc_efact_dev
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}
```

> El backend arranca con el perfil `dev` (ver `dev.sh`). Otras variables útiles en dev: `MAIL_PASSWORD` (por defecto `CHANGE_ME`) para el envío de emails.

### Frontend

`frc-efact-frontend/src/environments/environment.ts` no expone un `apiUrl` fijo: usa una función `getApiUrl()` que resuelve la URL del backend según el host del navegador y **siempre agrega el sufijo `/api`**:

```typescript
// localhost → http://localhost:8080/api
// IP local (192.168.x / 10.x / 172.16-31.x) → http://<host>:8080/api
export const environment = {
  production: false,
  apiUrl: getApiUrl(), // p.ej. http://localhost:8080/api
  // ...
};
```

## 🧪 Testing

### Backend
```bash
cd frc-efact-backend
mvn test
```

### Frontend
```bash
cd frc-efact-frontend
npm test
```

## 📚 Más Información

- **API Documentation**: `frc-efact-backend/API_DOCUMENTATION.md`
- **Security**: `frc-efact-backend/SECURITY.md`
