# Dominio: Usuarios, Roles y Permisos (RBAC)

El sistema tiene **dos capas de roles** que `CustomUserDetailsService` unifica en cada request. Entender esta doble capa es clave: la mayoría de los bugs de permisos vienen de que el **frontend solo mira una** de las dos. Esquema **`persona`**. Store NgRx: `usuarios`, `auth`.

---

## Capa 1 — Roles globales (RBAC clásico)

Tres entidades en `persona`:

| Entidad | Tabla | Notas |
|---|---|---|
| `Usuario` | `persona.usuario` | login local (`passwordHash`) + Auth0 (`auth0Id`, V27), `imagenPerfil` (V30), `isActive`, `intentosFallidosLogin`, `bloqueadoHasta`, `ultimoLogin` |
| `Rol` | `persona.rol` | catálogo: **`ADMIN`, `EMPRESA_ADMIN`, `FACTURADOR`, `LECTOR`** |
| `UsuarioRol` | `persona.usuario_rol` | N:M usuario↔rol global |

`Usuario.usuarioRoles` → cada `UsuarioRol.rol.nombre` se mapea a authority `ROLE_<nombre>`.

## Capa 2 — Rol por empresa

`UsuarioEmpresa.rolEmpresa` (String en `empresa.usuario_empresa`): **`ADMINISTRADOR` / `FACTURADOR` / `LECTOR`** (ver [empresas-multiempresa.md](empresas-multiempresa.md)).

## Cómo se unen — `CustomUserDetailsService` (en cada request)

En `loadUserByUsername` se acumulan authorities de **ambas** capas:
1. Cada `usuarioRol` activo → `ROLE_<Rol.nombre>`.
2. Cada `usuarioEmpresa` **activo** con `rolEmpresa` → mapeo:
   - `ADMINISTRADOR` → `ROLE_EMPRESA_ADMIN`
   - `FACTURADOR` → `ROLE_FACTURADOR`
   - `LECTOR` → `ROLE_LECTOR`
3. Si no queda ninguna authority → `ROLE_USER` por defecto.

Se ejecuta **por request**: JWT local (`JwtAuthenticationFilter` llama `loadUserByUsername` en cada request) y Auth0 (`CustomJwtAuthenticationConverter`). Por eso los `@PreAuthorize` del backend **pasan solo con tener `rolEmpresa`**, sin rol global.

### Autenticación: Auth0 + JWT local
Dos vías conviven (Spring Security 6): JWT propio (jjwt, `AuthController /auth`) y OAuth2 Resource Server contra Auth0. Ambas terminan resolviendo el mismo `Usuario` y sus authorities.

---

## Endpoints

### `UsuarioController` `/usuarios`
`GET /perfil` · `GET /asignables` · `GET` · `GET /buscar` · `GET /{id}` · `GET /{id}/roles` · `GET /roles` `[todos]` · `POST` `[A,EA]` · `PUT /{id}` · `DELETE /{id}` · `POST /reset-password` · `POST /{id}/activar|desactivar|desbloquear` · `GET /estadisticas` · `GET /check-username|check-email` `[A]` · `GET /search` `[A,EA]`.

### `UserProfileController` `/perfil`
`POST /vincular-auth0` · `POST /desvincular-auth0` · `PUT /actualizar` · `POST /cambiar-password` · `GET /actividad` · `POST /actualizar-desde-auth0`.
> ⚠️ QA-4: `actualizarDesdeAuth0()` usa `System.out.println("DEBUG…")` + `printStackTrace()`.

### `RolController` `/roles`
`GET` `[A]`.

Tabla completa de roles por operación en [../reference/endpoints-index.md](../reference/endpoints-index.md).

---

## 🐛 Los 4 bugs de permisos (ver [../../../../docs/ISSUES_CANDIDATOS.md](../../../../docs/ISSUES_CANDIDATOS.md))

1. **RBAC-1 🔴 — `PermissionsService` (frontend) ignora `rolEmpresa`.** `core/services/permissions.service.ts` (`hasAnyRole`/`hasAnyRoleSync`) solo lee `user.roles` (globales). Un usuario con `rolEmpresa=ADMINISTRADOR` es autorizado por el backend pero el **frontend le oculta menús/botones**. Síntoma típico: asignás `rolEmpresa` y el usuario sigue sin ver facturas ni botón crear.
   - *Fix de raíz:* `UsuarioMapper.toDto()` debe inyectar en `roles` los roles de empresa mapeados (misma lógica que `CustomUserDetailsService`) — una sola fuente de verdad.
   - *Workaround:* asignar además el rol **global** `EMPRESA_ADMIN`.
2. **RBAC-2 🔴 — Usuario nuevo sin rol.** Ni `UsuarioService.crearUsuario()` ni el auto-registro Auth0 (`CustomJwtAuthenticationConverter`) asignan rol por defecto → `roles=[]`, sin acceso hasta vinculación manual.
3. **RBAC-3 🟠 — Vincular no refresca la sesión activa.** El frontend cachea `currentUser` en NgRx; tras vincular hay que re-loguear. (Ver [empresas-multiempresa.md](empresas-multiempresa.md).)
4. **RBAC-4 🟡 — Inconsistencia `ADMINISTRADOR` vs `EMPRESA_ADMIN`.** `rolEmpresa` usa `ADMINISTRADOR`, el rol global es `EMPRESA_ADMIN`. El mapeo vive **solo** en `CustomUserDetailsService`; si se duplica en otro lado, riesgo de divergencia.

Bugs menores relacionados: RBAC-5 (escritura de usuarios reservada a `ADMIN`, no `EMPRESA_ADMIN`), RBAC-6 (`SecurityConfig` protege `/usuarios/admin/**` inexistente). Ver [../reference/known-bugs.md](../reference/known-bugs.md).

---

## Auditoría de accesos
Toda entidad hereda `creadoPor`/`actualizadoPor`; el `AuditLog` (esquema `auditoria`) registra acciones — ver [dashboard-reportes-auditoria.md](dashboard-reportes-auditoria.md).

## Frontend
`features/usuarios/`: `usuarios-list/detail/form.component.ts`, `user-profile.component.ts`, `vincular-usuario-dialog.component.ts`, `reset-password-dialog.component.ts`. APIs `core/api/usuario-api.service.ts` y `profile-api.service.ts`. Guards: `auth`, `no-auth`, `role`, `empresa-access`, `empresa-selected`.
