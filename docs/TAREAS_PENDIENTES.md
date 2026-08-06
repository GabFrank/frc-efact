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
2026-07-07. Detalle completo en [deployment/hetzner/RUNBOOK_VM.md](deployment/hetzner/RUNBOOK_VM.md).

- [x] **Render descartado** (2026-08-05).
- [x] **Deploy por GitHub Actions vía SSH**: `.github/workflows/deploy.yml`, `workflow_dispatch`
      con confirmación. Validado end-to-end el 2026-08-06.
- [x] **Copia off-site de certificados**: existen en la PC del desarrollador y en un Drive,
      además del volumen Docker y los backups diarios de la VM.
- [ ] Formalizar la copia off-site **de la base** en `deploy/backup-db.sh` (sección rclone/rsync).
      Hoy los dumps viven solo en el disco de la VM.
- [ ] Monitoreo externo a `https://efact.frc-ecommerce.com/api/actuator/health`
- [ ] **Renovar certificado de FRANCO AREVALOS S.A. — vence `2026-08-20`** y re-subirlo desde la UI
- [ ] Quitar `*.onrender.com` del CORS y del `connect-src` de la CSP en `SecurityConfig`, y
      retirar `render.yaml` — ya no hay motivo para conservarlos
- [ ] Sacar `RENDER_DATABASE_URL` de `deploy/.env` en la VM: apunta a una base que ya no existe

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

**Bloqueante para hacer el repo público.**

### Hecho (2026-08-05)

- [x] **`.gitignore`** cubre `*.pfx`, `*.p12`, `*.jks`, `*.keystore`, `client_secret*.json`,
      `*service-account*.json`.
- [x] **Destrackeados los 7 archivos** (`git rm --cached`, siguen en el working tree). Verificado
      antes: el `Dockerfile` del backend **no** copia `certificates/` — solo hace
      `mkdir -p /app/certificates`, y en producción los `.pfx` vienen del volumen Docker
      `certificates` cargado a mano (`RUNBOOK_VM.md` paso 5). Estaban versionados por error, sin
      que nada del build ni del runtime los usara.
- [x] **Redactados los secretos en texto plano de `HEAD`** (12 ocurrencias): el PAT en
      `AGREGAR_VARIABLES_RENDER.md` y `RENDER_DOCKER_BUILD_ARGS.md`, el app password de Gmail en
      `CONFIGURACION_IDE.md`.
- [x] Verificado que el `GITHUB_TOKEN` del `deploy/.env` de la VM **no es** el PAT filtrado, así
      que revocarlo no rompe el build del backend.

### Pendiente — requiere acción del usuario (no hay API)

- [ ] **Revocar el PAT** en https://github.com/settings/tokens. Sigue vivo desde el 2026-07-07 y
      ya no cumple ninguna función: el CI usa `GH_PACKAGES_TOKEN` y la VM tiene otro token.
- [ ] **Revocar el client secret de Google** en la consola de Google Cloud.
- [ ] **Rotar el app password de Gmail** de `frcsistemasinformaticos@gmail.com`.
- [ ] Rotar `JWT_SECRET` (invalida las sesiones activas).
- [x] **`ENCRYPTION_KEY` rotada el 2026-08-05.** Era el default hardcodeado de
      `application.yml`, o sea la clave que cifra los CSC y los passwords de certificados
      estaba versionada. Se re-cifraron los 6 valores (CSC de 3 timbrados +
      `certificado_password_encrypted` de 3 empresas) en una transacción, con verificación
      antes y después. Los defaults de `JWT_SECRET` y `ENCRYPTION_KEY` se quitaron de
      `application.yml` — ahora la app **no arranca** si faltan; los valores de desarrollo
      viven en `application-dev.yml`.
      ⚠️ Los backups previos al 2026-08-05 siguen cifrados con la clave vieja, que sobrevive
      solo en `deploy/.env.bak-20260805_194154` de la VM. Ver
      [deployment/hetzner/RUNBOOK_VM.md](deployment/hetzner/RUNBOOK_VM.md).
- [ ] Re-emitir los `.pfx` ante la SET (el de FRANCO AREVALOS vence el 2026-08-20 igual).
- [ ] Purgar el historial con `git-filter-repo` — reescribe los 132 commits, force-push a todas
      las ramas, y hay que rehacer tags y releases de `semantic-release`.

> ⚠️ **Destrackear y redactar NO borra nada del historial.** Los `.pfx`, el `client_secret` y el
> PAT siguen recuperables desde cualquier commit anterior. Lo hecho hasta acá evita que el
> problema crezca.

### ✅ RESUELTO: repo migrado y publicado (2026-08-06)

En vez de purgar el repo existente, se creó uno nuevo y se volcó el historial filtrado. Eso evita
el problema de los objetos que GitHub conserva tras un force-push, porque el repo viejo se
descarta entero.

**Procedimiento ejecutado:**

1. El repo original se renombró a **`frc-efact-legacy`** (privado, conservado como rollback) y se
   creó **`frc-efact`** público con el mismo nombre, así ninguna URL externa se rompió.
2. `git filter-repo` sobre un clon fresco: eliminó **7 binarios** (6 `.pfx` + el `client_secret`
   de Google) y reemplazó **4 cadenas** — dos PAT, el app password de Gmail y la `ENCRYPTION_KEY`
   vieja.
3. Verificación sobre **todos los blobs del object store** (25 MB, `git cat-file
   --batch-all-objects`) contra 9 familias de patrones de credencial. Además: 0 objetos con path
   sensible, y el tree hash de `HEAD` **idéntico** al del clon sin filtrar, o sea que el estado
   del código no cambió — solo la historia.
4. Push de `main`, `develop` y los 8 tags. 156 commits preservados: `blame` y `bisect` intactos.

**El segundo PAT lo encontró GitHub, no yo.** El primer push fue rechazado por su push
protection: `informativo.md` —el documento de especificación original, borrado hace meses—
contenía un PAT que el barrido inicial no detectó porque la salida se truncó con `head`. Por eso
la verificación final se hizo sobre el object store completo y sin truncar. Defensa en
profundidad que funcionó.

**Lo que se pierde y no vuelve:** los PRs e issues del repo legacy, el historial de runs de
Actions, y los objetos Release de GitHub (los tags sí migraron; las notas están en
`CHANGELOG.md`). Los links a commits del `CHANGELOG` apuntan a SHA que ya no existen.

**Re-emitir los `.pfx` sigue siendo imposible**, pero dejó de importar para esto: al no estar en
la historia del repo nuevo, nunca se publicaron. Los certificados de producción viven en el
volumen Docker de la VM, en los backups diarios, en la PC del desarrollador y en un Drive.

Branch protection quedó disponible al ser público — detalle y el límite de `require pull request`
en [CONTRIBUTING.md](../CONTRIBUTING.md).

**Pendiente de higiene, sin urgencia:** los dos PAT siguen vivos (ya no están en la historia
pública), el client secret de Google está muerto en la práctica (cero referencias en código), y el
app password de Gmail sigue en uso. Borrar `frc-efact-legacy` es el último paso: mientras exista
es el rollback.

---

## 10. ⚠️ Manejo del CSC — 3 problemas de diseño — Prioridad Media

_(detectados 2026-08-05 al diagnosticar el error SIFEN 2501)_

Ninguno rompe nada hoy, pero los tres invitan a un bug futuro.

### 10.1 La condición "si el CSC cambió" es inútil
`TimbradoService.actualizar()` compara el **plaintext entrante** contra el **ciphertext guardado**:

```java
&& !timbradoActualizado.getCscEncrypted().equals(timbradoExistente.getCscEncrypted())
```

Nunca son iguales, así que re-cifra en cada update. Inocuo pero engañoso: el `if` sugiere una
optimización que no existe.

### 10.2 El plaintext viaja en un campo llamado `cscEncrypted`
`TimbradoMapper.toEntity()` asigna el CSC **sin cifrar** a `timbrado.cscEncrypted`, y recién
`TimbradoService` lo cifra. Si algún día un path guarda un `Timbrado` sin pasar por ese servicio,
el CSC queda en la DB en texto plano y nada lo detecta. El nombre del campo miente sobre su
contenido en ese tramo.

### 10.3 `csc_id` está NULL en los 3 timbrados
Cae al default `"001"` de `SifenConfigFactory`, que jsifenlib pad-ea a `"0001"`. Hoy coincide con
lo que espera la SET, así que funciona por casualidad. Si a alguna empresa le asignan otro ID de
CSC, hay que cargarlo o el QR se firma con el ID equivocado.

### 10.4 No hay validación de largo del CSC
El CSC de SIFEN son **32 caracteres**. El form no valida largo (`csc: ['']`, sin `maxLength` ni
`pattern`) y el backend tampoco, así que un CSC incompleto entra sin protestar y recién se
descubre cuando la SET rechaza el DE con **2501** ("El hash del código QR ... es inválido") —
que no dice nada sobre la causa. **Pasó en producción el 2026-08-05:** el CSC de LANGER MARIO se
cargó con 31 caracteres. Agregar `Validators.pattern(/^[A-Za-z0-9]{32}$/)` en el form y una
validación equivalente en el backend ahorra ese diagnóstico entero.

---

## 11. ⚠️ Descripciones de actividad económica: la SET compara texto exacto — Prioridad Media

El error **1262** ("Descripción de la actividad económica no corresponde al código") confirma que
el servidor de la SET **valida la descripción contra su catálogo**, no solo el código. El XSD no
lo restringe (`tdDesActEco` es `noEmptyString` con `maxLength 300`, sin `pattern`), así que la
validación es de negocio y solo se descubre al emitir.

Hoy la descripción es **texto libre** tipeado en el form, sin catálogo que la respalde. Riesgos
concretos:

- ~~**Mayúsculas/minúsculas.**~~ **DESCARTADO — verificado 2026-08-05:** la SET compara la
  descripción **insensible al caso**. LANGER MARIO emite con descripciones en minúsculas
  (`Cultivo de productos agrícolas…`) y ANATOLE con MAYÚSCULAS, y **ambas aprueban**. Se había
  sospechado del caso al ver esa diferencia; la evidencia lo refuta. El `1262` original venía
  exclusivamente del separador partiendo la descripción al medio (resuelto en `V36`).
- Lo que **sí** rompe la emisión: cualquier typo, tilde faltante o espacio de más — la
  comparación es exacta salvo por el caso.

**Fix recomendado:** tabla de catálogo (`catalogo.actividad_economica`: `codigo`, `descripcion`)
precargada con la lista oficial, y guardar en `empresa` **solo el código**. La descripción se
resuelve por join al armar el `gActEco`. Elimina de raíz tanto este problema como el del
separador (§7 del historial de este doc / migración `V36`), porque la lista deja de existir como
texto.

---

## 12. Funcionalidades pendientes / mejoras

_(Añadir nuevas funcionalidades pendientes o mejoras sugeridas aquí.)_
