# Guía de Desarrollo Local

Cómo trabajar con el proyecto en tu máquina local.

## 🚀 Inicio Rápido

### 1. Pre-requisitos

- Java 17+
- Node.js 18+
- PostgreSQL 14+
- Maven 3.8+

### 2. Configurar Base de Datos Local

```bash
# Crear base de datos
createdb frc_efact

# O usar el script incluido
cd frc-efact-backend
./setup-local-db.sh
```

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

Edita `frc-efact-backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/frc_efact
    username: tu_usuario
    password: tu_password
```

### Frontend

Edita `frc-efact-frontend/src/environments/environment.ts`:

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080'
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
- **Component Testing**: `frc-efact-frontend/COMPONENT_TESTING.md`
