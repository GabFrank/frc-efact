# Guía de Testing con Postman

Cómo usar Postman para probar la API.

## 📥 Importar Colección

1. Abre Postman
2. Click en "Import"
3. Crea una nueva colección llamada "FRC eFact API"

## 🔐 Configurar Variables

En la colección, configura estas variables:

- `baseUrl`: `http://localhost:8080` (desarrollo) o tu URL de Render (producción)
- `token`: (se llenará automáticamente después del login)

## 📋 Endpoints Principales

### 1. Login

```
POST {{baseUrl}}/api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

Guarda el `token` de la respuesta.

### 2. Obtener Perfil

```
GET {{baseUrl}}/api/auth/me
Authorization: Bearer {{token}}
```

### 3. Listar Usuarios (Admin)

```
GET {{baseUrl}}/api/users
Authorization: Bearer {{token}}
```

### 4. Crear Usuario (Admin)

```
POST {{baseUrl}}/api/users
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "username": "nuevo_usuario",
  "email": "usuario@example.com",
  "password": "password123",
  "role": "USER"
}
```

## 🔄 Automatizar el Token

En la pestaña "Tests" del request de Login, agrega:

```javascript
if (pm.response.code === 200) {
    const response = pm.response.json();
    pm.collectionVariables.set("token", response.token);
}
```

Esto guardará automáticamente el token para otros requests.

## 📚 Documentación Completa

Ver `frc-efact-backend/API_DOCUMENTATION.md` para todos los endpoints disponibles.
