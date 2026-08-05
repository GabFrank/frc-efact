# Tareas Pendientes y Deuda Técnica

Registro accionable de funcionalidades incompletas, bugs conocidos y deuda técnica del sistema
FRC eFact. Todo lo de acá está verificado contra el código.

**Leyenda de estado:** ✅ Completo · ⚠️ Parcial · ❌ No implementado · 🐛 Con errores
**Prioridad:** Alta / Media / Baja

---

## 1. ⚠️ Inutilización de números (parcial)

**Estado:** ⚠️ Parcial · **Prioridad:** Media

La inutilización de numeración funciona **solo** en su versión desvinculada de una factura legal.
Cuando el número a inutilizar está vinculado a una `FacturaLegal`, no completa correctamente.

**Archivos:**
- Frontend: `frc-efact-frontend/src/app/features/facturacion/inutilizar-numeros-dialog.component.ts`
- Backend: `EventoInutilizacionDE` + `SifenEventoService` (endpoint `POST /sifen/timbrados/{id}/inutilizar`)

**TODO:** revisar la implementación completa; cubrir el caso vinculado a factura legal.

---

## 2. 🐛 Sistema de roles / permisos (4 bugs)

El sistema tiene **dos capas de roles** que no están bien integradas:
- **Roles globales**: `persona.usuario_rol` → `Usuario.usuarioRoles` → `Rol`
  (`ADMIN`, `EMPRESA_ADMIN`, `FACTURADOR`, `LECTOR`).
- **Roles por empresa**: `persona.usuario_empresa.rol_empresa` → `UsuarioEmpresa.rolEmpresa`
  (string `ADMINISTRADOR` / `FACTURADOR` / `LECTOR`).

El backend (`CustomUserDetailsService`) mapea dinámicamente en **cada request** los `rolEmpresa`
activos a authorities Spring (`ADMINISTRADOR → ROLE_EMPRESA_ADMIN`, etc.), por lo que los
`@PreAuthorize` **pasan** con tener solo `rolEmpresa`. El problema está en el frontend y en el alta.

### 2.1 🐛 PermissionsService del frontend ignora `rolEmpresa` — Prioridad Alta
El `PermissionsService` solo lee `user.roles` (roles globales) y **no** mira los `rolEmpresa`.
Resultado: aunque el backend autoriza, el frontend oculta menús/botones y muestra "sin permisos".
**Síntoma:** asignás `rolEmpresa = ADMINISTRADOR` y el usuario sigue sin ver la lista de facturas
ni el botón crear.
- **Archivo:** `frc-efact-frontend/src/app/core/services/permissions.service.ts`
- **Fix recomendado (backend, una sola fuente de verdad):** que `UsuarioMapper.toDto()` agregue al
  array `roles` los roles de empresa mapeados, con la misma lógica que `CustomUserDetailsService`
  (`frc-efact-backend/.../dto/mapper/UsuarioMapper.java`).
- **Workaround:** asignar también el rol global `EMPRESA_ADMIN` vía endpoint admin de usuarios.

### 2.2 🐛 Usuario nuevo no recibe ningún rol — Prioridad Alta
Ni `UsuarioService.crearUsuario()` ni el auto-registro Auth0 en `CustomJwtAuthenticationConverter`
asignan un rol por defecto. El usuario queda con `roles=[]` y sin acceso hasta que un admin lo vincule.
- **Archivos:** `frc-efact-backend/.../service/UsuarioService.java`,
  `frc-efact-backend/.../security/CustomJwtAuthenticationConverter.java`

### 2.3 ⚠️ Vincular usuario a empresa no refresca la sesión — Prioridad Media
El backend remapea bien en cada request, pero el frontend cachea el `currentUser` en NgRx con el
array `roles` viejo. El usuario receptor debe **cerrar sesión y volver a entrar** para ver el
`UsuarioDto` actualizado.
- **Fix sugerido:** emitir un evento o forzar refresh del `currentUser` tras la vinculación.

### 2.4 ⚠️ Inconsistencia de nombres `ADMINISTRADOR` vs `EMPRESA_ADMIN` — Prioridad Baja
A nivel `rol_empresa` el valor es `ADMINISTRADOR`; a nivel rol global es `EMPRESA_ADMIN`. El mapping
vive **solo** en `CustomUserDetailsService`. Si se duplica en otro lado (p. ej. frontend), riesgo de
divergencia. Centralizar el mapeo.

---

## 3. 🐛 Routing `/api/api` en 3 controllers — Prioridad Media

El `context-path` de la app es `/api`, por lo que `@RequestMapping` **no** debe incluir `/api/`
(ver `CONTROLLER_ROUTING_RULE.md`). Estos 3 controllers lo violan y resuelven a `/api/api/...`:

- `frc-efact-backend/.../controller/GeografiaController.java` → `@RequestMapping("/api/geografia")`
- `frc-efact-backend/.../controller/AuditLogController.java` → `@RequestMapping("/api/auditoria")`
- `frc-efact-backend/.../controller/ReporteController.java` → `@RequestMapping("/api/reportes")`

**Fix:** sacar el prefijo `/api` de cada `@RequestMapping`. Ojo: hay que ajustar en paralelo las
URLs que consumen estos endpoints desde el frontend (`audit-api.service`, `reporte-api.service`,
y el servicio de geografía) para que no se rompan.

---

## 4. 🐛 Validación de RUC deshabilitada en el frontend — Prioridad Media

`RucValidationService` tiene la validación real **deshabilitada** y devuelve resultados mock:
- `validateRucFormat()` valida solo el **formato** (`\d{6,8}-\d`); el chequeo de dígito verificador
  (`validateRucCheckDigit`) está comentado ("TEMPORALMENTE DESHABILITADO: Backend usa algoritmo incorrecto").
- `validateRucLive()` retorna siempre `of({ valid: true, exists: false })` sin llamar al backend.

- **Archivo:** `frc-efact-frontend/src/app/services/ruc-validation.service.ts`
- **Causa raíz:** el algoritmo de dígito verificador del backend rechaza RUCs válidos.
- **TODO:** corregir el algoritmo en el backend y reactivar `validateRucCheckDigit()` +
  la llamada real a `/empresas/validate-ruc`.

---

## 5. ⚠️ Cierre post-migración Hetzner — Prioridad Alta

**Estado:** ⚠️ Parcial (migración ejecutada el 2026-07-07; quedan tareas de cierre)

Producción corre en la VM Hetzner (`https://efact.frc-ecommerce.com`) desde el
2026-07-07. Render quedó **suspendido** como ventana de rollback, no dado de baja.
Detalle completo en [deployment/hetzner/RUNBOOK_VM.md](deployment/hetzner/RUNBOOK_VM.md).

- [ ] Revocar el PAT de GitHub filtrado (sigue válido; Render suspendido ya no lo usa)
- [ ] Copia off-site de backups (sección rclone/rsync de `deploy/backup-db.sh`)
- [ ] Monitoreo externo a `https://efact.frc-ecommerce.com/api/actuator/health`
- [ ] **Renovar certificado de FRANCO AREVALOS S.A. — vence `2026-08-20`** y
      re-subirlo desde la UI
- [ ] Tras 1-2 semanas estables: dar de baja Render (incl. DB), quitar
      `*.onrender.com` del CORS, retirar `render.yaml`, y decidir si se arma
      deploy por GitHub Actions via SSH a la VM

---

## 6. API pública de facturación electrónica (roadmap)

**Estado:** ❌ No implementado · **Prioridad:** Media · _(anotado 2026-07-07)_

Exponer las capacidades SIFEN de frc-efact (emisión de DE, notas C/D/R, eventos,
consulta de estado, KuDE) como API para que otras apps del ecosistema (Franco
Systems central/filial, e-commerce, etc.) facturen sin reimplementar SIFEN.

Consideraciones de diseño relevadas:
- **Auth M2M**: hoy el API usa JWT de usuario. Para apps consumidoras usar
  OAuth2 client_credentials (Auth0 ya está integrado → apps M2M de Auth0) o
  API keys por app, siempre scopeadas a una `empresa` (el modelo multi-empresa
  ya existe: `usuario_empresa` → algo análogo `app_empresa`).
- **Versionado**: prefijo `/v1` dentro del context-path y contratos DTO estables;
  publicar el OpenAPI (springdoc ya lo genera).
- **Idempotencia**: header `Idempotency-Key` en la emisión — un retry del
  cliente NO debe duplicar una factura/DE.
- **Async**: la aprobación SIFEN es asíncrona (lote + polling). Ofrecer webhook
  de callback (`APROBADO`/`RECHAZADO`) además del polling del cliente.
- **Rate limiting y auditoría por app** (base ya existe: RateLimiting + AuditLog).
- **Primer consumidor confirmado: frc-gourmet** (2026-07-07). Si funciona bien,
  se suman más apps del ecosistema. A futuro también reemplazaría el flujo manual
  de la skill `migrate-de-central-to-frc-efact` (central emitiría NC/ND via API).

---

## 7. 🐛 `npm run lint` no existe — Prioridad Media

**Estado:** ❌ No implementado · _(detectado 2026-08-05 al armar el CI)_

`angular.json` solo declara los targets `build`, `serve`, `extract-i18n` y `test`. El script
`lint` de `package.json` ejecuta `ng lint`, que falla con *"Cannot find lint target for the
specified project"*. Falta `@angular-eslint/schematics`.

La instrucción "correr `npm run lint` antes de commitear" figuraba en `CLAUDE.md`, en la skill
y en varios docs — era imposible de cumplir. Ya corregida en la doc; falta el fix real.

**TODO:** `ng add @angular-eslint/schematics`, revisar el ruido inicial, y recién entonces
agregar el job de lint al CI.

---

## 8. ⚠️ CI: tests del backend no bloqueantes — Prioridad Media

**Estado:** ⚠️ Parcial · _(anotado 2026-08-05)_

El job `backend-tests` de `.github/workflows/ci.yml` tiene `continue-on-error: true` porque
fallan **6 de 19 tests**, todos de validación de RUC:

| Suite | Resultado |
|---|---|
| `RucCalculatorTest` | 4 tests, 3 fallan |
| `RucValidatorTest` | 5 tests, 1 falla |
| `ValidadoresParaguayosTest` | 8 tests, 2 fallan |

Es el **mismo bug de la sección 4** (algoritmo de dígito verificador del backend). Los tests
venían señalándolo correctamente.

**TODO:** arreglar el algoritmo → los 6 tests pasan → sacar el `continue-on-error` y hacer el
job bloqueante. Es un solo fix que destraba tres cosas: los tests, el CI y la validación de
RUC del frontend.

---

## 9. 🔴 Secretos y certificados versionados en el repo — Prioridad Alta

**Estado:** 🐛 Con errores · _(auditoría 2026-08-05)_

Auditoría del historial completo (132 commits). Todo esto está **tracked en `HEAD`**, no solo
en el historial:

| Qué | Dónde |
|---|---|
| **6 certificados `.pfx` de firma digital SIFEN** | `frc-efact-backend/certificates/` |
| `client_secret_…apps.googleusercontent.com.json` (OAuth Google) | raíz del repo |
| PAT de GitHub `ghp_SUuAN9…` | `docs/deployment/AGREGAR_VARIABLES_RENDER.md`, `RENDER_DOCKER_BUILD_ARGS.md` |
| App password de Gmail | `frc-efact-backend/CONFIGURACION_IDE.md` |
| Defaults de `JWT_SECRET` y `ENCRYPTION_KEY` | `application.yml:81,86` |

Dos de los `.pfx` son los certificados de firma **en producción** (ANATOLE DEINZER DUARTE y
GUILLERMO FRANCO AREVALOS, ver [deployment/hetzner/RUNBOOK_VM.md](deployment/hetzner/RUNBOOK_VM.md)
paso 5). Son PKCS12 legacy (RC2), crackeables offline. Y el runbook confirma que producción usa
el `ENCRYPTION_KEY` **default**, que está en el repo.

`.gitignore` no cubre `*.pfx` ni `client_secret*`.

**Bloqueante para hacer el repo público.** TODO, en orden:
- [ ] Agregar `*.pfx`, `*.p12`, `client_secret*.json` a `.gitignore` y `git rm --cached`
- [ ] Revocar el PAT de GitHub y el client secret de Google; rotar el app password de Gmail
- [ ] Rotar `JWT_SECRET`
- [ ] Rotar `ENCRYPTION_KEY` — implica **re-cifrar en la DB** los CSC y los passwords de
      certificados, no es solo cambiar la env var
- [ ] Re-emitir los `.pfx` ante la SET (el de FRANCO AREVALOS vence el 2026-08-20 igual)
- [ ] Purgar el historial con `git-filter-repo` — reescribe los 132 commits, force-push a
      todas las ramas, y hay que rehacer tags y releases de `semantic-release`

---

## 10. Funcionalidades pendientes / mejoras

_(Añadir nuevas funcionalidades pendientes o mejoras sugeridas aquí.)_
