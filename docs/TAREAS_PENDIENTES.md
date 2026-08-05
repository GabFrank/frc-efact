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

## 7. Funcionalidades pendientes / mejoras

_(Añadir nuevas funcionalidades pendientes o mejoras sugeridas aquí.)_
