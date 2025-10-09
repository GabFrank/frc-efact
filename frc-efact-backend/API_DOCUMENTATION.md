# Documentación de API - FRC eFact Backend

## Información General

- **Base URL (Desarrollo)**: `http://localhost:8080/api`
- **Base URL (Producción)**: `https://frc-efact-backend.onrender.com/api`
- **Formato de Respuesta**: JSON
- **Autenticación**: JWT Bearer Token
- **Versión de API**: v1

## Swagger UI

La documentación interactiva de la API está disponible en:

- **Desarrollo**: http://localhost:8080/swagger-ui.html
- **Producción**: https://frc-efact-backend.onrender.com/swagger-ui.html

## Autenticación

### Login

Autentica un usuario y devuelve un JWT token.

**Endpoint**: `POST /api/auth/login`

**Headers**:
```
Content-Type: application/json
```

**Request Body**:
```json
{
  "username": "admin",
  "password": "Admin123!"
}
```

**Response Success (200 OK)**:
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "expiresIn": 86400000,
  "user": {
    "id": 1,
    "username": "admin",
    "email": "admin@frcefact.com",
    "creadoEn": "2024-01-15T10:30:00",
    "ultimoLogin": "2024-01-20T14:25:00"
  }
}
```

**Response Error (401 Unauthorized)**:
```json
{
  "timestamp": "2024-01-20T14:25:00.123",
  "status": 401,
  "error": "Unauthorized",
  "message": "Credenciales inválidas",
  "path": "/api/auth/login"
}
```

**Códigos de Estado**:
- `200 OK`: Autenticación exitosa
- `401 Unauthorized`: Credenciales inválidas
- `400 Bad Request`: Datos de entrada inválidos
- `429 Too Many Requests`: Demasiados intentos de login

---

### Refresh Token

Renueva un JWT token expirado usando el refresh token.

**Endpoint**: `POST /api/auth/refresh`

**Headers**:
```
Content-Type: application/json
```

**Request Body**:
```json
{
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
}
```

**Response Success (200 OK)**:
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "expiresIn": 86400000
}
```

**Response Error (401 Unauthorized)**:
```json
{
  "timestamp": "2024-01-20T14:25:00.123",
  "status": 401,
  "error": "Unauthorized",
  "message": "Refresh token inválido o expirado",
  "path": "/api/auth/refresh"
}
```

**Códigos de Estado**:
- `200 OK`: Token renovado exitosamente
- `401 Unauthorized`: Refresh token inválido o expirado

---

### Logout

Invalida el token actual del usuario.

**Endpoint**: `POST /api/auth/logout`

**Headers**:
```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
Content-Type: application/json
```

**Response Success (200 OK)**:
```json
{
  "message": "Logout exitoso"
}
```

**Códigos de Estado**:
- `200 OK`: Logout exitoso
- `401 Unauthorized`: Token inválido o no proporcionado

---

## Gestión de Usuarios

### Obtener Perfil de Usuario

Obtiene la información del perfil del usuario autenticado.

**Endpoint**: `GET /api/users/profile`

**Headers**:
```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

**Response Success (200 OK)**:
```json
{
  "id": 1,
  "username": "admin",
  "email": "admin@frcefact.com",
  "creadoEn": "2024-01-15T10:30:00",
  "actualizadoEn": "2024-01-20T14:25:00",
  "ultimoLogin": "2024-01-20T14:25:00",
  "isActive": true
}
```

**Response Error (401 Unauthorized)**:
```json
{
  "timestamp": "2024-01-20T14:25:00.123",
  "status": 401,
  "error": "Unauthorized",
  "message": "Token JWT inválido o expirado",
  "path": "/api/users/profile"
}
```

**Códigos de Estado**:
- `200 OK`: Perfil obtenido exitosamente
- `401 Unauthorized`: No autenticado o token inválido
- `404 Not Found`: Usuario no encontrado

---

## Health Check

### Verificar Estado del Servicio

Endpoint para verificar que el servicio está funcionando correctamente.

**Endpoint**: `GET /actuator/health`

**Headers**: Ninguno requerido

**Response Success (200 OK)**:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 250685575168,
        "free": 100685575168,
        "threshold": 10485760
      }
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

**Códigos de Estado**:
- `200 OK`: Servicio funcionando correctamente
- `503 Service Unavailable`: Servicio con problemas

---

## Modelos de Datos

### Usuario (User)

```typescript
{
  id: number;              // ID único del usuario
  username: string;        // Nombre de usuario (único)
  email: string;           // Email (único)
  creadoEn: string;       // Fecha de creación (ISO 8601)
  actualizadoEn: string;  // Fecha de última actualización (ISO 8601)
  ultimoLogin?: string;   // Fecha del último login (ISO 8601, opcional)
  isActive: boolean;      // Estado del usuario
}
```

### LoginRequest

```typescript
{
  username: string;  // Nombre de usuario (requerido, 3-50 caracteres)
  password: string;  // Contraseña (requerido, mínimo 8 caracteres)
}
```

### AuthResponse

```typescript
{
  token: string;         // JWT access token
  refreshToken: string;  // JWT refresh token
  type: string;          // Tipo de token (siempre "Bearer")
  expiresIn: number;     // Tiempo de expiración en milisegundos
  user: User;            // Información del usuario autenticado
}
```

### RefreshTokenRequest

```typescript
{
  refreshToken: string;  // Refresh token para renovar el access token
}
```

---

## Códigos de Error Comunes

### 400 Bad Request
Datos de entrada inválidos o mal formateados.

```json
{
  "timestamp": "2024-01-20T14:25:00.123",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    {
      "field": "username",
      "message": "Username debe tener entre 3 y 50 caracteres"
    },
    {
      "field": "password",
      "message": "Password debe tener al menos 8 caracteres"
    }
  ],
  "path": "/api/auth/login"
}
```

### 401 Unauthorized
No autenticado o credenciales inválidas.

```json
{
  "timestamp": "2024-01-20T14:25:00.123",
  "status": 401,
  "error": "Unauthorized",
  "message": "Token JWT inválido o expirado",
  "path": "/api/users/profile"
}
```

### 403 Forbidden
Autenticado pero sin permisos suficientes.

```json
{
  "timestamp": "2024-01-20T14:25:00.123",
  "status": 403,
  "error": "Forbidden",
  "message": "No tiene permisos para acceder a este recurso",
  "path": "/api/admin/users"
}
```

### 404 Not Found
Recurso no encontrado.

```json
{
  "timestamp": "2024-01-20T14:25:00.123",
  "status": 404,
  "error": "Not Found",
  "message": "Usuario no encontrado",
  "path": "/api/users/999"
}
```

### 429 Too Many Requests
Demasiadas solicitudes en un período de tiempo.

```json
{
  "timestamp": "2024-01-20T14:25:00.123",
  "status": 429,
  "error": "Too Many Requests",
  "message": "Demasiados intentos de login. Intente nuevamente en 5 minutos",
  "path": "/api/auth/login"
}
```

### 500 Internal Server Error
Error interno del servidor.

```json
{
  "timestamp": "2024-01-20T14:25:00.123",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Ha ocurrido un error inesperado",
  "path": "/api/users/profile"
}
```

---

## Seguridad

### Autenticación JWT

Todos los endpoints protegidos requieren un JWT token válido en el header `Authorization`:

```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

### Rate Limiting

Los endpoints de autenticación tienen rate limiting implementado:

- **Login**: Máximo 5 intentos por minuto por IP
- **Refresh**: Máximo 10 solicitudes por minuto por usuario

### CORS

El backend acepta requests desde los siguientes orígenes:

- `http://localhost:4200` (desarrollo)
- `https://frc-efact-frontend.onrender.com` (producción)

### HTTPS

En producción, todas las comunicaciones deben ser por HTTPS. El backend redirige automáticamente HTTP a HTTPS.

---

## Ejemplos de Uso

### Ejemplo con cURL

#### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "Admin123!"
  }'
```

#### Obtener Perfil
```bash
curl -X GET http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..."
```

### Ejemplo con JavaScript (Fetch API)

#### Login
```javascript
const response = await fetch('http://localhost:8080/api/auth/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    username: 'admin',
    password: 'Admin123!'
  })
});

const data = await response.json();
console.log('Token:', data.token);
```

#### Obtener Perfil
```javascript
const response = await fetch('http://localhost:8080/api/users/profile', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`
  }
});

const user = await response.json();
console.log('User:', user);
```

### Ejemplo con Postman

1. **Configurar Environment**:
   - Variable: `baseUrl` = `http://localhost:8080/api`
   - Variable: `token` = (se llenará después del login)

2. **Request de Login**:
   - Method: POST
   - URL: `{{baseUrl}}/auth/login`
   - Body (JSON):
     ```json
     {
       "username": "admin",
       "password": "Admin123!"
     }
     ```
   - Test Script (para guardar token):
     ```javascript
     pm.environment.set("token", pm.response.json().token);
     ```

3. **Request de Perfil**:
   - Method: GET
   - URL: `{{baseUrl}}/users/profile`
   - Headers:
     - Key: `Authorization`
     - Value: `Bearer {{token}}`

---

## Versionado de API

Actualmente la API está en versión 1. Futuras versiones se indicarán en la URL:

- v1: `/api/...` (actual)
- v2: `/api/v2/...` (futuro)

---

## Soporte y Contacto

Para reportar problemas o solicitar nuevas funcionalidades, crear un issue en el repositorio de GitHub del proyecto.

## Recursos Adicionales

- [Swagger UI](http://localhost:8080/swagger-ui.html) - Documentación interactiva
- [OpenAPI Spec](http://localhost:8080/v3/api-docs) - Especificación OpenAPI 3.0
- [README.md](./README.md) - Documentación general del proyecto
- [SECURITY.md](./SECURITY.md) - Consideraciones de seguridad
