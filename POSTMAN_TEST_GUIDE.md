# Guía de Pruebas con Postman - FRC eFact API

## Usuarios de Prueba

Se han creado usuarios mediante las migraciones de Flyway:

**Usuario Admin (V2):**
- **Username:** `admin`
- **Password:** `password`
- **Email:** `admin@frcefact.com`

**Usuario de Prueba (V3):**
- **Username:** `testuser`
- **Password:** `test123`
- **Email:** `testuser@frcefact.com`

## Endpoints Disponibles

### 1. Login (Obtener Token JWT)

**Endpoint:** `POST http://localhost:8080/api/auth/login`

**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**Respuesta Esperada (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "usuario": {
    "id": 1,
    "username": "admin",
    "email": "admin@frcefact.com",
    "isActive": true,
    "ultimoLogin": "2024-01-20T14:45:00",
    "creadoEn": "2024-01-15T10:30:00"
  }
}
```

**Nota:** Copia el valor del campo `token` para usarlo en las siguientes peticiones.

---

### 2. Obtener Perfil de Usuario (Endpoint Nuevo)

**Endpoint:** `GET http://localhost:8080/api/usuarios/perfil`

**Headers:**
```
Authorization: Bearer {tu_token_aqui}
Content-Type: application/json
```

**Respuesta Esperada (200 OK):**
```json
{
  "id": 1,
  "username": "admin",
  "email": "admin@frcefact.com",
  "isActive": true,
  "ultimoLogin": "2024-01-20T14:45:00",
  "creadoEn": "2024-01-15T10:30:00"
}
```

**Errores Posibles:**
- **401 Unauthorized:** Token inválido, expirado o faltante
- **404 Not Found:** Usuario no encontrado

---

### 3. Refrescar Token

**Endpoint:** `POST http://localhost:8080/api/auth/refresh`

**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "refreshToken": "{tu_refresh_token_aqui}"
}
```

**Respuesta Esperada (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "usuario": {
    "id": 1,
    "username": "admin",
    "email": "admin@frcefact.com",
    "isActive": true,
    "ultimoLogin": "2024-01-20T14:45:00",
    "creadoEn": "2024-01-15T10:30:00"
  }
}
```

---

### 4. Logout

**Endpoint:** `POST http://localhost:8080/api/auth/logout`

**Headers:**
```
Authorization: Bearer {tu_token_aqui}
Content-Type: application/json
```

**Respuesta Esperada (200 OK):**
```
Logout exitoso
```

---

## Documentación Swagger UI

También puedes probar los endpoints directamente desde Swagger UI:

**URL:** `http://localhost:8080/api/swagger-ui.html`

**Nota:** La aplicación usa el context-path `/api`, por lo que todas las URLs deben incluir este prefijo.

Swagger UI proporciona una interfaz interactiva donde puedes:
1. Ver todos los endpoints disponibles
2. Probar los endpoints directamente desde el navegador
3. Ver los esquemas de request/response
4. Autenticarte usando el botón "Authorize" con tu token JWT

---

## Pasos para Probar en Postman

### Paso 1: Login
1. Crea una nueva request en Postman
2. Método: `POST`
3. URL: `http://localhost:8080/api/auth/login`
4. En la pestaña "Body", selecciona "raw" y "JSON"
5. Pega el JSON con las credenciales del admin
6. Click en "Send"
7. **Copia el token** de la respuesta

### Paso 2: Obtener Perfil
1. Crea una nueva request en Postman
2. Método: `GET`
3. URL: `http://localhost:8080/api/usuarios/perfil`
4. En la pestaña "Headers", agrega:
   - Key: `Authorization`
   - Value: `Bearer {pega_aqui_el_token}`
5. Click en "Send"
6. Deberías ver la información del perfil del usuario admin

### Paso 3: Probar sin Token (Error 401)
1. Usa la misma request del Paso 2
2. Elimina el header `Authorization`
3. Click en "Send"
4. Deberías recibir un error 401 Unauthorized

---

## Colección de Postman

Puedes importar esta colección JSON en Postman para tener todas las requests configuradas:

```json
{
  "info": {
    "name": "FRC eFact API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Auth",
      "item": [
        {
          "name": "Login",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"username\": \"admin\",\n  \"password\": \"admin123\"\n}"
            },
            "url": {
              "raw": "http://localhost:8080/api/auth/login",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "auth", "login"]
            }
          }
        },
        {
          "name": "Refresh Token",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"refreshToken\": \"{{refreshToken}}\"\n}"
            },
            "url": {
              "raw": "http://localhost:8080/api/auth/refresh",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "auth", "refresh"]
            }
          }
        },
        {
          "name": "Logout",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{token}}"
              }
            ],
            "url": {
              "raw": "http://localhost:8080/api/auth/logout",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "auth", "logout"]
            }
          }
        }
      ]
    },
    {
      "name": "Usuarios",
      "item": [
        {
          "name": "Obtener Perfil",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{token}}"
              }
            ],
            "url": {
              "raw": "http://localhost:8080/api/usuarios/perfil",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8080",
              "path": ["api", "usuarios", "perfil"]
            }
          }
        }
      ]
    }
  ]
}
```

---

## Notas Importantes

1. **Asegúrate de que la aplicación esté corriendo** antes de hacer las pruebas
2. **El token JWT expira** después de cierto tiempo (configurado en application.properties)
3. **Usa el refresh token** para obtener un nuevo access token sin hacer login nuevamente
4. **El usuario admin se crea automáticamente** cuando ejecutas las migraciones de Flyway al iniciar la aplicación

---

## Verificar que el Usuario Admin Existe

Si quieres verificar que el usuario admin fue creado correctamente en la base de datos:

```sql
SELECT * FROM usuarios WHERE username = 'admin';
```

Deberías ver un registro con:
- username: `admin`
- email: `admin@frcefact.com`
- is_active: `true`
