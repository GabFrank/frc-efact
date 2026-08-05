# Documentación de API - FRC eFact Backend

> **Fuente de verdad:** este documento se generó verificando los controllers reales en
> `src/main/java/com/frcefact/controller/`. Para el detalle interactivo (schemas, ejemplos)
> usar Swagger UI.

## Información General

- **Base URL (Desarrollo)**: `http://localhost:8080/api`
- **Base URL (Producción)**: `https://frc-efact-backend.onrender.com/api`
- **Context-path**: `/api` (definido en `application.yml`). **Todas las rutas de todos los
  controllers cuelgan de `/api`.** Un controller anotado `@RequestMapping("/clientes")`
  responde en `/api/clientes`. Ver [CONTROLLER_ROUTING_RULE.md](./CONTROLLER_ROUTING_RULE.md).
- **Formato de Respuesta**: JSON
- **Autenticación**: JWT Bearer Token (login local) **o** Auth0 OAuth2 (ambas vías conviven)
- **Autorización**: `@PreAuthorize` por endpoint (roles Spring `ADMIN`, `EMPRESA_ADMIN`,
  `FACTURADOR`, `LECTOR`).

### Convención de roles usada en este documento

| Notación | Roles que autoriza |
|----------|--------------------|
| **todos** | `ADMIN`, `EMPRESA_ADMIN`, `FACTURADOR`, `LECTOR` |
| **A, EA, F** | `ADMIN`, `EMPRESA_ADMIN`, `FACTURADOR` |
| **A, EA** | `ADMIN`, `EMPRESA_ADMIN` |
| **ADMIN** | solo `ADMIN` |
| **público** | sin autenticación (endpoints de login/refresh) |
| **autenticado** | requiere token válido, sin restricción de rol (`@PreAuthorize` a nivel método ausente) |

> Nota sobre roles por empresa: el backend mapea dinámicamente el `rolEmpresa`
> (`ADMINISTRADOR` → `ROLE_EMPRESA_ADMIN`, `FACTURADOR` → `ROLE_FACTURADOR`,
> `LECTOR` → `ROLE_LECTOR`) en cada request (`CustomUserDetailsService`). Por eso un usuario
> con `rolEmpresa` cumple los `@PreAuthorize` aunque no tenga el rol global.

## Swagger UI

- **Desarrollo**: http://localhost:8080/swagger-ui.html
- **Producción**: https://frc-efact-backend.onrender.com/swagger-ui.html
- **OpenAPI JSON**: `/api/v3/api-docs`

---

## ⚠️ Controllers con bug de doble prefijo `/api`

Tres controllers declaran `@RequestMapping("/api/...")`. Como el context-path ya agrega
`/api`, la ruta efectiva queda **duplicada** (`/api/api/...`). Es un bug real de routing —
ver [CONTROLLER_ROUTING_RULE.md](./CONTROLLER_ROUTING_RULE.md). En este documento se listan
con la ruta **efectiva actual** (`/api/api/...`) y se marcan con ⚠️.

| Controller | `@RequestMapping` declarado | Ruta efectiva (bug) | Ruta esperada |
|------------|-----------------------------|---------------------|---------------|
| `GeografiaController` | `/api/geografia` | `/api/api/geografia` | `/api/geografia` |
| `AuditLogController` | `/api/auditoria` | `/api/api/auditoria` | `/api/auditoria` |
| `ReporteController` | `/api/reportes` | `/api/api/reportes` | `/api/reportes` |

---

## 1. Autenticación — `AuthController` (`/auth`)

Endpoints **públicos** (sin `@PreAuthorize`).

| Método | Ruta | Roles | Descripción |
|--------|------|-------|-------------|
| POST | `/api/auth/login` | público | Login con username/password → tokens JWT + usuario |
| POST | `/api/auth/refresh` | público | Renueva tokens con un refresh token válido |
| POST | `/api/auth/logout` | autenticado | Cierra sesión del usuario autenticado |

### `POST /api/auth/login`

**Request**:
```json
{
  "username": "admin",
  "password": "Admin123!"
}
```

**Response 200 — `AuthResponse` (DTO real)**:
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "usuario": {
    "id": 1,
    "username": "admin",
    "email": "admin@frcefact.com"
  }
}
```

> **Importante:** `AuthResponse` (ver `dto/AuthResponse.java`) tiene exactamente cuatro
> campos: `token`, `refreshToken`, `type` (siempre `"Bearer"`) y `usuario` (un `UsuarioDto`).
> **No existe** el campo `expiresIn`, ni un campo `user` (el objeto se llama `usuario`).

**Códigos**: `200` OK · `400` request inválido · `401` credenciales inválidas ·
`429` rate limit de login excedido.

### `POST /api/auth/refresh`

**Request**:
```json
{ "refreshToken": "eyJhbGciOiJIUzUxMiJ9..." }
```

**Response 200**: mismo `AuthResponse` con tokens renovados.

### `POST /api/auth/logout`

Requiere `Authorization: Bearer <token>`. Responde `200 OK` con texto plano `"Logout exitoso"`.

---

## 2. Perfil de usuario — `UserProfileController` (`/perfil`)

Sin `@PreAuthorize` a nivel método (requiere estar autenticado; opera sobre el usuario del
contexto de seguridad).

| Método | Ruta | Roles | Descripción |
|--------|------|-------|-------------|
| POST | `/api/perfil/vincular-auth0` | autenticado | Vincula la cuenta con un `auth0Id` |
| POST | `/api/perfil/desvincular-auth0` | autenticado | Elimina la vinculación con Auth0 |
| PUT | `/api/perfil/actualizar` | autenticado | Actualiza el perfil propio (username, email) |
| POST | `/api/perfil/cambiar-password` | autenticado | Cambia la contraseña (valida la actual) |
| GET | `/api/perfil/actividad` | autenticado | Últimas actividades/auditorías del usuario (paginado) |
| POST | `/api/perfil/actualizar-desde-auth0` | autenticado | Actualiza datos (imagen, email) desde el JWT de Auth0/Google |

> El perfil del usuario autenticado **también** se obtiene con
> `GET /api/usuarios/perfil` (ver `UsuarioController`).

---

## 3. Usuarios — `UsuarioController` (`/usuarios`)

| Método | Ruta | Roles |
|--------|------|-------|
| GET | `/api/usuarios/perfil` | autenticado (usa contexto) |
| GET | `/api/usuarios/asignables` | todos |
| GET | `/api/usuarios` | todos |
| GET | `/api/usuarios/buscar` | todos |
| GET | `/api/usuarios/{id}` | todos |
| POST | `/api/usuarios` | A, EA |
| PUT | `/api/usuarios/{id}` | ADMIN |
| DELETE | `/api/usuarios/{id}` | ADMIN |
| POST | `/api/usuarios/reset-password` | ADMIN |
| POST | `/api/usuarios/{id}/activar` | ADMIN |
| POST | `/api/usuarios/{id}/desactivar` | ADMIN |
| POST | `/api/usuarios/{id}/desbloquear` | ADMIN |
| GET | `/api/usuarios/{id}/roles` | todos |
| GET | `/api/usuarios/roles` | todos |
| GET | `/api/usuarios/estadisticas` | ADMIN |
| GET | `/api/usuarios/search` | A, EA |
| GET | `/api/usuarios/check-username` | ADMIN |
| GET | `/api/usuarios/check-email` | ADMIN |

> `SecurityConfig` referencia un patrón `/usuarios/admin/**`, pero **no existe** ningún
> endpoint bajo esa ruta en el controller. Ver [USUARIO_ADMIN_ENDPOINTS_SUMMARY.md](./USUARIO_ADMIN_ENDPOINTS_SUMMARY.md).

---

## 4. Roles — `RolController` (`/roles`)

| Método | Ruta | Roles |
|--------|------|-------|
| GET | `/api/roles` | ADMIN |

---

## 5. Empresas — `EmpresaController` (`/empresas`)

| Método | Ruta | Roles |
|--------|------|-------|
| POST | `/api/empresas` (multipart y JSON) | A, EA |
| PUT | `/api/empresas/{id}` (multipart y JSON) | A, EA |
| GET | `/api/empresas/{id}` | autenticado |
| GET | `/api/empresas` | todos |
| GET | `/api/empresas/mis-empresas` | autenticado |
| DELETE | `/api/empresas/{id}` | A, EA |
| PUT | `/api/empresas/{id}/certificado/password` | A, EA |
| POST | `/api/empresas/{id}/certificado` (multipart) | A, EA |
| GET | `/api/empresas/{id}/usuarios` | todos |
| POST | `/api/empresas/{id}/usuarios` | A, EA |
| DELETE | `/api/empresas/{empresaId}/usuarios/{usuarioId}` | A, EA |
| GET | `/api/empresas/buscar` | autenticado |
| GET | `/api/empresas/diagnostico` | ADMIN |

---

## 6. Timbrados — `TimbradoController` (`/timbrados`)

| Método | Ruta | Roles |
|--------|------|-------|
| POST | `/api/timbrados` | A, EA |
| PUT | `/api/timbrados/{id}` | A, EA |
| GET | `/api/timbrados/{id}` | todos |
| GET | `/api/timbrados/empresa/{empresaId}` | todos |
| GET | `/api/timbrados/empresa/{empresaId}/activos` | todos |
| GET | `/api/timbrados/{id}/vigente` | todos |
| GET | `/api/timbrados/empresa/{empresaId}/vigentes` | todos |
| GET | `/api/timbrados/empresa/{empresaId}/electronicos-vigentes` | todos |
| GET | `/api/timbrados/empresa/{empresaId}/por-vencer` | todos |
| DELETE | `/api/timbrados/{id}` | A, EA |

---

## 7. Detalles de timbrado — `TimbradoDetalleController`

Caso atípico: `@RequestMapping` **sin path base**; las rutas completas se definen en cada
método (algunas cuelgan de `/timbrados/...` y otras de `/timbrado-detalles/...`). Es válido.

| Método | Ruta | Roles |
|--------|------|-------|
| POST | `/api/timbrados/{timbradoId}/detalles` | A, EA |
| GET | `/api/timbrados/{timbradoId}/detalles` | todos |
| GET | `/api/timbrados/{timbradoId}/detalles/activos` | todos |
| GET | `/api/timbrados/{timbradoId}/detalles/por-agotarse` | todos |
| GET | `/api/timbrado-detalles/{id}` | todos |
| PUT | `/api/timbrado-detalles/{id}` | A, EA |
| DELETE | `/api/timbrado-detalles/{id}` | A, EA |
| GET | `/api/timbrado-detalles/empresa/{empresaId}` | todos |

---

## 8. Clientes — `ClienteController` (`/clientes`)

Todo scoped por `/empresa/{empresaId}`.

| Método | Ruta | Roles |
|--------|------|-------|
| POST | `/api/clientes/empresa/{empresaId}` | A, EA, F |
| PUT | `/api/clientes/empresa/{empresaId}/{clienteId}` | A, EA, F |
| GET | `/api/clientes/empresa/{empresaId}/{clienteId}` | todos |
| GET | `/api/clientes/empresa/{empresaId}` | todos |
| GET | `/api/clientes/empresa/{empresaId}/paginado` | todos |
| GET | `/api/clientes/empresa/{empresaId}/buscar` | todos |
| GET | `/api/clientes/empresa/{empresaId}/buscar/paginado` | todos |
| GET | `/api/clientes/empresa/{empresaId}/filtrar` | todos |
| GET | `/api/clientes/empresa/{empresaId}/ruc/{ruc}` | todos |
| GET | `/api/clientes/empresa/{empresaId}/ruc/{ruc}/existe` | todos |
| DELETE | `/api/clientes/empresa/{empresaId}/{clienteId}` | A, EA |
| PATCH | `/api/clientes/empresa/{empresaId}/{clienteId}/reactivar` | A, EA |
| GET | `/api/clientes/empresa/{empresaId}/count` | todos |

---

## 9. Productos — `ProductoController` (`/productos`)

| Método | Ruta | Roles |
|--------|------|-------|
| POST | `/api/productos` | A, EA, F |
| PUT | `/api/productos/{id}` | A, EA, F |
| GET | `/api/productos/{id}` | todos |
| GET | `/api/productos` | todos |
| GET | `/api/productos/buscar` | todos |
| DELETE | `/api/productos/{id}` | A, EA, F |
| POST | `/api/productos/importar` (multipart) | A, EA, F |
| GET | `/api/productos/verificar-codigo` | A, EA, F |
| GET | `/api/productos/verificar-descripcion` | A, EA, F |

---

## 10. Facturas legales — `FacturaLegalController` (`/facturas`)

| Método | Ruta | Roles |
|--------|------|-------|
| POST | `/api/facturas` | A, EA, F |
| GET | `/api/facturas/{id}` | todos |
| PUT | `/api/facturas/{id}` | A, EA, F |
| GET | `/api/facturas` | todos |
| GET | `/api/facturas/resumen` | todos |
| POST | `/api/facturas/{id}/items` | A, EA, F |
| DELETE | `/api/facturas/{facturaId}/items/{itemId}` | A, EA, F |
| PUT | `/api/facturas/{id}/descuento` | A, EA, F |
| POST | `/api/facturas/{id}/recalcular` | A, EA, F |
| DELETE | `/api/facturas/{id}` | A, EA |
| GET | `/api/facturas/estadisticas` | todos |
| POST | `/api/facturas/{id}/generar-de` | A, EA, F |
| POST | `/api/facturas/{id}/desvincular-de` | A, EA, F |
| GET | `/api/facturas/{id}/kude-pdf` | todos |
| POST | `/api/facturas/{id}/reenviar-email` | A, EA, F |

---

## 11. Documentos electrónicos — `DocumentoElectronicoController` (`/documentos-electronicos`)

| Método | Ruta | Roles |
|--------|------|-------|
| GET | `/api/documentos-electronicos` | todos |
| GET | `/api/documentos-electronicos/{id}` | todos |
| GET | `/api/documentos-electronicos/cdc/{cdc}` | todos |
| GET | `/api/documentos-electronicos/{id}/xml` | todos |
| POST | `/api/documentos-electronicos/{id}/consultar` | A, EA, F |

---

## 12. Notas de crédito — `NotaCreditoController` (`/notas-credito`)

| Método | Ruta | Roles |
|--------|------|-------|
| POST | `/api/notas-credito` | A, EA, F |
| GET | `/api/notas-credito/{id}` | todos |
| GET | `/api/notas-credito` | todos |
| DELETE | `/api/notas-credito/{id}` | A, EA |
| POST | `/api/notas-credito/{id}/generar-de` | A, EA, F |
| GET | `/api/notas-credito/{id}/kude-pdf` | todos |

---

## 13. Notas de débito — `NotaDebitoController` (`/notas-debito`)

| Método | Ruta | Roles |
|--------|------|-------|
| POST | `/api/notas-debito` | A, EA, F |
| GET | `/api/notas-debito/{id}` | todos |
| GET | `/api/notas-debito` | todos |
| DELETE | `/api/notas-debito/{id}` | A, EA |

---

## 14. Notas de remisión — `NotaRemisionController` (`/notas-remision`)

| Método | Ruta | Roles |
|--------|------|-------|
| POST | `/api/notas-remision` | A, EA, F |
| GET | `/api/notas-remision/{id}` | todos |
| GET | `/api/notas-remision` | todos |
| DELETE | `/api/notas-remision/{id}` | A, EA |
| POST | `/api/notas-remision/{id}/generar-de` | A, EA, F |
| POST | `/api/notas-remision/{id}/generar-y-enviar` | A, EA, F |
| POST | `/api/notas-remision/{id}/vincular-lote` | A, EA, F |
| GET | `/api/notas-remision/{id}/kude-pdf` | todos |
| POST | `/api/notas-remision/{id}/enviar-email` | A, EA, F |

---

## 15. SIFEN — `SifenController` (`/sifen`)

| Método | Ruta | Roles |
|--------|------|-------|
| POST | `/api/sifen/lotes/{loteId}/enviar` | A, EA, F |
| POST | `/api/sifen/lotes/{loteId}/consultar` | A, EA, F |
| POST | `/api/sifen/documentos/{cdc}/consultar` | A, EA, F |
| POST | `/api/sifen/documentos/{cdc}/cancelar` | A, EA, F |
| GET | `/api/sifen/documentos/factura/{facturaId}` | A, EA, F |
| GET | `/api/sifen/documentos/nota-credito/{id}` | A, EA, F |
| GET | `/api/sifen/documentos/nota-remision/{id}` | A, EA, F |
| POST | `/api/sifen/documentos/{deId}/reenviar` | A, EA, F |
| POST | `/api/sifen/documentos/{cdc}/nominar` | A, EA, F |
| POST | `/api/sifen/timbrados/{timbradoId}/inutilizar` | A, EA, F |
| GET | `/api/sifen/eventos/cancelacion` | todos |
| GET | `/api/sifen/eventos/nominacion` | todos |
| GET | `/api/sifen/eventos/inutilizacion` | todos |

---

## 16. Dashboard — `DashboardController` (`/dashboard`)

| Método | Ruta | Roles |
|--------|------|-------|
| GET | `/api/dashboard/usuario/{usuarioId}` | todos |
| GET | `/api/dashboard/empresa/{empresaId}` | todos |
| GET | `/api/dashboard/general` | todos |

---

## 17. Vehículos — `VehiculoController` (`/vehiculos`)

Scoped por `/empresa/{empresaId}`.

| Método | Ruta | Roles |
|--------|------|-------|
| POST | `/api/vehiculos/empresa/{empresaId}` | A, EA, F |
| PUT | `/api/vehiculos/empresa/{empresaId}/{vehiculoId}` | A, EA, F |
| GET | `/api/vehiculos/empresa/{empresaId}/{vehiculoId}` | todos |
| GET | `/api/vehiculos/empresa/{empresaId}` | todos |
| GET | `/api/vehiculos/empresa/{empresaId}/paginado` | todos |
| GET | `/api/vehiculos/empresa/{empresaId}/buscar` | todos |
| GET | `/api/vehiculos/empresa/{empresaId}/filtrar` | todos |
| DELETE | `/api/vehiculos/empresa/{empresaId}/{vehiculoId}` | A, EA |
| PATCH | `/api/vehiculos/empresa/{empresaId}/{vehiculoId}/reactivar` | A, EA |
| GET | `/api/vehiculos/empresa/{empresaId}/count` | todos |

---

## 18. Choferes — `ChoferController` (`/choferes`)

Mismo patrón y roles que `VehiculoController`, scoped por `/empresa/{empresaId}`.

| Método | Ruta | Roles |
|--------|------|-------|
| POST | `/api/choferes/empresa/{empresaId}` | A, EA, F |
| PUT | `/api/choferes/empresa/{empresaId}/{choferId}` | A, EA, F |
| GET | `/api/choferes/empresa/{empresaId}/{choferId}` | todos |
| GET | `/api/choferes/empresa/{empresaId}` | todos |
| GET | `/api/choferes/empresa/{empresaId}/paginado` | todos |
| GET | `/api/choferes/empresa/{empresaId}/buscar` | todos |
| GET | `/api/choferes/empresa/{empresaId}/filtrar` | todos |
| DELETE | `/api/choferes/empresa/{empresaId}/{choferId}` | A, EA |
| PATCH | `/api/choferes/empresa/{empresaId}/{choferId}/reactivar` | A, EA |
| GET | `/api/choferes/empresa/{empresaId}/count` | todos |

---

## 19. ⚠️ Geografía — `GeografiaController` (`/api/geografia` → **bug** `/api/api/geografia`)

Todos los endpoints: rol **todos**. Ruta efectiva actual con el prefijo duplicado.

| Método | Ruta efectiva (bug) |
|--------|---------------------|
| GET | `/api/api/geografia/departamentos` |
| GET | `/api/api/geografia/departamentos/buscar` |
| GET | `/api/api/geografia/departamentos/{codigo}` |
| GET | `/api/api/geografia/departamentos/{depCodigo}/distritos` |
| GET | `/api/api/geografia/distritos/buscar` |
| GET | `/api/api/geografia/distritos/{codigo}` |
| GET | `/api/api/geografia/distritos/{distritoCodigo}/ciudades` |
| GET | `/api/api/geografia/ciudades/buscar` |
| GET | `/api/api/geografia/ciudades/{codigo}` |
| GET | `/api/api/geografia/ciudades/id/{id}` |
| GET | `/api/api/geografia/ciudades/{ciudadCodigo}/barrios` |
| GET | `/api/api/geografia/barrios/buscar` |
| GET | `/api/api/geografia/barrios/{codigo}` |

---

## 20. ⚠️ Auditoría — `AuditLogController` (`/api/auditoria` → **bug** `/api/api/auditoria`)

| Método | Ruta efectiva (bug) | Roles |
|--------|---------------------|-------|
| GET | `/api/api/auditoria` | A, EA |
| GET | `/api/api/auditoria/entidad/{tipo}/{id}` | todos |
| GET | `/api/api/auditoria/ultimas-actividades` | A, EA |
| GET | `/api/api/auditoria/usuario/{usuarioId}` | A, EA |
| GET | `/api/api/auditoria/estadisticas` | A, EA |
| GET | `/api/api/auditoria/count` | ADMIN |
| GET | `/api/api/auditoria/count/empresa/{empresaId}` | A, EA |

---

## 21. ⚠️ Reportes — `ReporteController` (`/api/reportes` → **bug** `/api/api/reportes`)

Sin `@PreAuthorize` a nivel método (autenticado). Cada recurso tiene su variante `/excel` y `/pdf`.

| Método | Ruta efectiva (bug) |
|--------|---------------------|
| GET | `/api/api/reportes/facturas` (+ `/facturas/excel`, `/facturas/pdf`) |
| GET | `/api/api/reportes/clientes` (+ `/clientes/excel`, `/clientes/pdf`) |
| GET | `/api/api/reportes/productos` (+ `/productos/excel`, `/productos/pdf`) |
| GET | `/api/api/reportes/usuarios` (+ `/usuarios/excel`, `/usuarios/pdf`) |

---

## Health Check (Actuator)

Actuator también cuelga del context-path `/api` (no hay override de `management.endpoints.web.base-path`).

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/actuator/health` | Estado del servicio (usado por Render como health check) |

**Response 200**:
```json
{ "status": "UP" }
```

---

## Enums de dominio (referencia de valores)

Definidos en `com.frcefact.model`.

### `EstadoDE` (estado del documento electrónico)
`PENDIENTE`, `EN_PROCESO`, `APROBADO`, `RECHAZADO`, `CANCELADO`, `ERROR`

### `EstadoLoteDE` (estado del lote enviado a SIFEN)
`PENDIENTE`, `EN_PROCESO`, `APROBADO`, `RECHAZADO`, `ERROR`, `PROCESADO`, `ERROR_ENVIO`, `ERROR_PERMANENTE`

### `EstadoEvento` (estado de eventos: cancelación / nominación / inutilización)
`PENDIENTE`, `APROBADO`, `RECHAZADO`, `ERROR_ENVIO`

### `AccionEnum` (auditoría)
`CREATE`, `UPDATE`, `DELETE`, `READ`

### `TipoClienteSifen`
`(iNatRec, iTiContRec, iTiOpe, descripción, requiereRuc)`

| Valor | iNatRec | iTiContRec | iTiOpe | requiereRuc |
|-------|---------|-----------|--------|-------------|
| `PERSONA_FISICA` | 1 | 1 | 1 | sí |
| `PERSONA_JURIDICA` | 1 | 2 | 1 | sí |
| `NO_CONTRIBUYENTE` (Consumidor Final) | 2 | null | 2 | no |
| `EXTRANJERO` | 2 | null | 4 | no |
| `GUBERNAMENTAL` | 1 | 2 | 3 | sí |

### `TipoTransaccionProducto` (código SIFEN)
| Valor | Código |
|-------|--------|
| `VENTA_MERCADERIA` | 1 |
| `PRESTACION_SERVICIOS` | 2 |
| `MIXTO` | 3 |
| `VENTA_ACTIVO_FIJO` | 4 |
| `VENTA_DIVISAS` | 5 |
| `COMPRA_DIVISAS` | 6 |
| `PROMOCION_MUESTRAS` | 7 |
| `DONACION` | 8 |
| `ANTICIPO` | 9 |
| `COMPRA_PRODUCTOS` | 10 |
| `COMPRA_SERVICIOS` | 11 |
| `VENTA_CREDITO_FISCAL` | 12 |
| `MUESTRAS_MEDICAS` | 13 |

---

## Modelos de datos (DTOs de autenticación)

### `LoginRequest`
```typescript
{
  username: string;  // requerido
  password: string;  // requerido
}
```

### `RefreshTokenRequest`
```typescript
{
  refreshToken: string;  // requerido
}
```

### `AuthResponse` (real)
```typescript
{
  token: string;         // JWT access token
  refreshToken: string;  // JWT refresh token
  type: string;          // siempre "Bearer"
  usuario: UsuarioDto;   // datos del usuario autenticado
}
```

---

## Códigos de error comunes

Los errores los serializa `GlobalExceptionHandler`.

| Código | Significado |
|--------|-------------|
| `400 Bad Request` | Datos de entrada inválidos / validación fallida |
| `401 Unauthorized` | No autenticado o token inválido/expirado |
| `403 Forbidden` | Autenticado pero sin el rol requerido (`@PreAuthorize`) |
| `404 Not Found` | Recurso no encontrado |
| `429 Too Many Requests` | Rate limit excedido (login) |
| `500 Internal Server Error` | Error interno |

Ejemplo de body de error (formato aproximado):
```json
{
  "timestamp": "2026-01-20T14:25:00.123",
  "status": 401,
  "error": "Unauthorized",
  "message": "Token JWT inválido o expirado",
  "path": "/api/usuarios/perfil"
}
```

---

## Seguridad

- **JWT Bearer**: endpoints protegidos requieren `Authorization: Bearer <token>`.
- **Auth0**: soportado en paralelo como OAuth2 Resource Server (ver `docs/AUTH0_SETUP.md`).
- **Rate limiting**: aplicado a endpoints de autenticación.
- **CORS**: orígenes permitidos configurados en `SecurityConfig` (dev `http://localhost:4200`,
  prod `https://frc-efact-frontend.onrender.com`).

---

## Recursos adicionales

- [Swagger UI](http://localhost:8080/swagger-ui.html) — documentación interactiva
- [OpenAPI Spec](http://localhost:8080/api/v3/api-docs)
- [README.md](./README.md) — documentación general
- [CONTROLLER_ROUTING_RULE.md](./CONTROLLER_ROUTING_RULE.md) — regla de `@RequestMapping`
- [USUARIO_ADMIN_ENDPOINTS_SUMMARY.md](./USUARIO_ADMIN_ENDPOINTS_SUMMARY.md) — endpoints de administración de usuarios
- [SECURITY.md](./SECURITY.md) — consideraciones de seguridad
