# Autenticación y seguridad (backend)

Spring Security 6, **STATELESS**, con **doble vía de autenticación**: JWT local y Auth0. Config central en `config/SecurityConfig` (`@EnableWebSecurity` + `@EnableMethodSecurity`). Verificado 2026-08-05.

## Doble vía

### 1. JWT local
- `AuthController /auth/login` → `JwtTokenProvider` firma un token (HS512, `JWT_SECRET` ≥512 bits). `AuthResponse` real: `{ token, refreshToken, type, usuario }`.
- **`JwtAuthenticationFilter`** corre en cada request: si `tokenProvider.isLocalToken(jwt)` y `validateToken`, extrae el username y llama **`userDetailsService.loadUserByUsername(username)` EN CADA REQUEST** (`JwtAuthenticationFilter.java:50`). Esto es clave: las authorities se recalculan siempre, no viven en el token.

### 2. Auth0 (OAuth2 Resource Server)
- Tokens RS256 de Auth0 pasan por `.oauth2ResourceServer().jwt(...)` con **`CustomJwtAuthenticationConverter`**, que también resuelve el `Usuario` local y mapea sus roles/`rolEmpresa`. `OAuth2TokenFilter` complementa el flujo.
- `issuer-uri: dev-gp1w0u2bgw35q6v5.us.auth0.com`, `audiences: https://api.frcefact.com` (en `application.yml`).

Un token no-local se deja pasar al OAuth2 Resource Server; uno local lo consume `JwtAuthenticationFilter`.

## Roles: `rolEmpresa` → authorities (en cada request)

`CustomUserDetailsService.getAuthorities()` construye las authorities Spring combinando **dos capas**:
1. **Roles globales** (`Usuario.usuarioRoles` → `Rol`): `ROLE_ADMIN`, `ROLE_EMPRESA_ADMIN`, `ROLE_FACTURADOR`, `ROLE_LECTOR`.
2. **Roles por empresa** (`UsuarioEmpresa.rolEmpresa`, solo vínculos activos), mapeados a rol de sistema (`CustomUserDetailsService.java:128-133`):

| `rolEmpresa` | authority |
|---|---|
| `ADMINISTRADOR` | `ROLE_EMPRESA_ADMIN` |
| `FACTURADOR` | `ROLE_FACTURADOR` |
| `LECTOR` | `ROLE_LECTOR` |

Sin roles → `ROLE_USER` por defecto. Como el mapeo corre en cada request (tanto JWT local como Auth0), **los `@PreAuthorize` del backend pasan solo con tener `rolEmpresa`**.

### ⚠️ Bug RBAC-1 (tenelo presente)
El **frontend `PermissionsService` NO mira `rolEmpresa`**, solo `user.roles` global. Resultado: el backend autoriza pero el frontend oculta menús/botones ("sin permisos"). Fix de raíz: que `UsuarioMapper.toDto()` inyecte los `rolEmpresa` mapeados al array `roles` (misma lógica que `CustomUserDetailsService`, una sola fuente de verdad). Workaround: asignar también el rol global. Ver [known-bugs.md](../reference/known-bugs.md) y [frontend-capas.md](frontend-capas.md).
También: usuario nuevo queda **sin rol** (ni `crearUsuario()` ni auto-registro Auth0 asignan default) → RBAC-2.

## SecurityConfig — puntos clave

- **STATELESS** (`SessionCreationPolicy.STATELESS`), CSRF deshabilitado (API sin cookies de sesión).
- **BCrypt** (`BCryptPasswordEncoder`) vía `DaoAuthenticationProvider`.
- **HTTPS enforcement**: `requiresChannel` fuerza `requiresSecure()` cuando llega header `X-Forwarded-Proto` (Render termina el SSL).
- **Security headers**: `frameOptions.deny`, HSTS (1 año, includeSubDomains, preload), CSP (`connect-src 'self' https://frc-efact-backend.onrender.com`), `contentTypeOptions`, `xssProtection`, Referrer-Policy, Permissions-Policy.
- **Filtros** (orden): `RateLimitingFilter` → `JwtAuthenticationFilter` → `OAuth2TokenFilter` (antes de `BearerTokenAuthenticationFilter`).
- **Rate limiting** (`RateLimitingFilter`): solo `POST /auth/login`, por IP. Config real (`application-prod.yml`): **`max-attempts: 100` por ventana de `15` minutos** (defaults `100`/`15`), `enabled: true`. (No es "5/min".) Excede → HTTP 429.

### Autorización de rutas (SecurityConfig)
- Públicos: `/auth/**`, `/actuator/health*`, `/actuator/info`, `/v3/api-docs/**`, `/swagger-ui/**`, `/swagger-ui.html`.
- Solo ADMIN: `/admin/**`, `/usuarios/admin/**` (⚠️ este último **no existe** — RBAC-6), `/roles/**`.
- El resto (`/empresas/**`, `/facturas/**`, `/clientes/**`, `/sifen/**`, `/reportes/**`, `/auditoria/**`, `/usuarios/**`, …): `authenticated()`; la autorización fina va por **`@PreAuthorize` a nivel método**. Matriz de roles por endpoint: [reference/endpoints-index.md](../reference/endpoints-index.md).

### CORS (hardcodeado en `corsConfigurationSource()`)
`allowedOriginPatterns`: `localhost:4200` (http/https), rangos LAN `192.168.*.*`, `10.*.*.*`, `172.*.*.*` (http/https, puerto 4200), y **`https://*.onrender.com`**. Métodos `GET/POST/PUT/DELETE/OPTIONS/PATCH`, `allowCredentials: true`, expone `Authorization` y `Content-Disposition`.

## Datos sensibles
CSC y password de certificado se cifran con **AES-256** (`EncryptionService`, `ENCRYPTION_KEY` 32 chars). ⚠️ Si falta `ENCRYPTION_KEY` en prod se cifra con clave pública conocida (SEC-3). Ver [known-bugs.md](../reference/known-bugs.md).
