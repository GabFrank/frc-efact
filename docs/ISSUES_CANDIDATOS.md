# Issues candidatos — deuda técnica y bugs de FRC eFact

> Registro consolidado de problemas detectados durante la auditoría de fidelidad documentación↔código (**2026-08-05**). Cada ítem está redactado para convertirse en un **issue de GitHub**. Verificados contra el código salvo donde se indica "reconfirmar".
>
> **Este documento NO arregla el código** — solo lo cataloga. Los fixes se atacan como issues cuando se vuelva a trabajar el repo.

Leyenda severidad: 🔴 alta · 🟠 media · 🟡 baja

---

## 🔒 Seguridad

### SEC-1 · 🔴 Secret filtrado: app password de Gmail commiteada
`frc-efact-backend/CONFIGURACION_IDE.md` contiene una contraseña de aplicación de Gmail en texto plano (~6 ocurrencias). Está en el repo y en el historial git.
**Acción:** revocar en la cuenta de Google, reemplazar por placeholder en el doc, purgar del historial (`git filter-repo`/BFG). Rotar el `MAIL_PASSWORD`.
**Labels sugeridos:** `security`, `priority:high`

### SEC-2 · 🔴 Secret filtrado: token de GitHub commiteado
`docs/deployment/AGREGAR_VARIABLES_RENDER.md` y `docs/deployment/RENDER_DOCKER_BUILD_ARGS.md` contienen un token `ghp_...` en texto plano.
**Acción:** revocar el token en GitHub, reemplazar por placeholder, purgar del historial.
**Labels:** `security`, `priority:high`

### SEC-3 · 🔴 `ENCRYPTION_KEY` con default inseguro
`application.yml`: `encryption.secret-key: ${ENCRYPTION_KEY:<clave rotada 2026-08-05 — valor no documentado: los backups previos siguen cifrados con ella>}`. Si la var no se setea en prod, se cifran datos sensibles (CSC, password del certificado `.pfx`) con una clave AES pública y conocida.
**Acción:** quitar el default; fallar el arranque si falta en prod. Confirmar que Render la tenga seteada.
**Labels:** `security`, `priority:high`, `backend`

### SEC-4 · 🔴 Validación de RUC del frontend deshabilitada + mock en producción
`ruc-validation.service.ts` no valida dígito verificador ni duplicados (retorna `valid:true`), y `mock-ruc.interceptor.ts` (registrado en `app.config.ts`, **sin guarda de entorno**) intercepta `/api/empresas/validate-ruc` devolviendo RUCs ficticios hardcodeados — también en prod. El backend RUC (`CalcularVerificadorRuc`, módulo-11) ya es correcto.
**Acción:** quitar/guardar por entorno el mock, reactivar la validación real, borrar `ruc-workaround.interceptor.ts` (muerto). **Addendum:** el endpoint backend `GET /empresas/validate-ruc` que el mock simula **no existe** — reactivar la validación live requiere **crearlo** primero (no es solo trabajo de frontend).
**Labels:** `security`, `frontend`, `backend`, `priority:high`

### SEC-5 · 🟠 `ReporteController` sin `@PreAuthorize` a nivel método
Todos los endpoints de `/api/reportes` (facturas/clientes/productos/usuarios en JSON/excel/pdf) quedan accesibles a cualquier usuario autenticado, sin distinción de rol (a diferencia del resto del sistema).
**Acción:** confirmar si es intencional; si no, agregar `@PreAuthorize` acorde.
**Labels:** `security`, `backend`

### SEC-6 · 🟠 Security headers ausentes (frontend)
`render.yaml` (servicio frontend) no define ningún header (X-Frame-Options, X-Content-Type-Options, Referrer-Policy, Permissions-Policy, CSP, HSTS). La doc previa afirmaba lo contrario.
**Acción:** agregar sección `headers` en `render.yaml` o meta-equivalente.
**Labels:** `security`, `frontend`, `deployment`

---

## 👤 Permisos / RBAC

### RBAC-1 · 🔴 `PermissionsService` (frontend) ignora `rolEmpresa`
`core/services/permissions.service.ts` (`hasAnyRole`/`hasAnyRoleSync`) solo lee `user.roles` (roles globales), nunca `rolEmpresa`. Un usuario con `rolEmpresa=ADMINISTRADOR` es autorizado por el backend pero el frontend le oculta menús/botones.
**Fix recomendado:** que `UsuarioMapper.toDto()` (backend) incluya en `roles` los roles de empresa mapeados (misma lógica que `CustomUserDetailsService`), una sola fuente de verdad.
**Labels:** `bug`, `frontend`, `backend`, `priority:high`

### RBAC-2 · 🔴 Usuario nuevo no recibe rol por defecto
`UsuarioService.crearUsuario()` y el auto-registro Auth0 (`CustomJwtAuthenticationConverter`) no asignan rol → `roles=[]`, sin acceso a nada hasta vinculación manual.
**Labels:** `bug`, `backend`, `priority:high`

### RBAC-3 · 🟠 Vincular usuario a empresa no refresca la sesión activa
El frontend cachea `currentUser` en NgRx; tras vincular, el usuario debe re-loguear para ver los roles nuevos.
**Acción:** emitir evento / forzar refresh del `UsuarioDto` tras vinculación.
**Labels:** `bug`, `frontend`

### RBAC-4 · 🟡 Inconsistencia de nombres `ADMINISTRADOR` vs `EMPRESA_ADMIN`
`rolEmpresa` usa `ADMINISTRADOR`; el rol global es `EMPRESA_ADMIN`. El mapeo vive solo en `CustomUserDetailsService`; riesgo de divergencia si se duplica.
**Labels:** `tech-debt`, `backend`

### RBAC-5 · 🟡 Escritura de usuarios reservada a ADMIN, no EMPRESA_ADMIN
`POST /usuarios` acepta ADMIN+EMPRESA_ADMIN, pero PUT/DELETE/reset-password/activar/desactivar/desbloquear son solo ADMIN. Un EMPRESA_ADMIN crea usuarios pero no puede editarlos.
**Acción:** confirmar si es el diseño deseado.
**Labels:** `tech-debt`, `backend`

### RBAC-6 · 🟡 Regla de seguridad muerta `/usuarios/admin/**`
`SecurityConfig` protege ese patrón pero no existe endpoint bajo esa ruta.
**Labels:** `tech-debt`, `backend`

---

## 🛣️ Routing / API

### API-1 · 🟠 Doble prefijo `/api/api/...` en 3 controllers
`GeografiaController` (`/api/geografia`), `AuditLogController` (`/api/auditoria`) y `ReporteController` (`/api/reportes`) declaran `@RequestMapping("/api/...")` mientras el context-path ya es `/api` → resuelven a `/api/api/...`, violando `CONTROLLER_ROUTING_RULE.md`. El frontend los consume con ese doble prefijo.
**Acción:** corregir controller **y** el api-service del frontend en conjunto (cambio coordinado).
**Labels:** `bug`, `backend`, `frontend`

---

## 🧾 SIFEN

### SIFEN-1 · 🟠 `iTipCont` del emisor hardcodeado `PERSONA_JURIDICA`
`SifenService.java` ~L1351 (factura), ~L1881 (NC), ~L2780 (NR) fijan el tipo de contribuyente del emisor a persona jurídica. Un emisor persona física generaría XML incorrecto / posible rechazo.
**Fix:** derivar de un campo de `Empresa`.
**Labels:** `bug`, `sifen`, `backend`

### SIFEN-2 · 🟠 `SifenReceptorHelper` existe pero no se usa
`SifenService` no importa el helper; los 3 métodos `construirDatosReceptor*` (~L1415/L1931/L2831) resuelven naturaleza/tipo del receptor a mano. Riesgo de divergencia entre facturas y notas.
**Fix:** delegar en el helper o eliminarlo.
**Labels:** `tech-debt`, `sifen`, `backend`

### SIFEN-3 · 🟡 Falta validación de plazo de cancelación (48h/168h)
`SifenEventoService.cancelarDocumento` no compara la fecha del DE contra la ventana normativa del MT NRE v150; cancelaciones fuera de plazo se envían y SIFEN las rechaza.
**Labels:** `enhancement`, `sifen`, `backend`

### SIFEN-4 · 🟡 Decisión abierta: datos del chofer en transporte propio
`SifenService.java` ~L2689 informa `dNomChof/dNumIDChof/dDirChof` siempre que estén cargados, incluso en transporte propio (`iTipTrans=PROPIO`), en contra de una recomendación histórica de omitirlos (RUC/cédula inactiva → rechazo). El comentario "se restauran los datos del chofer" sugiere que se revirtió a propósito.
**Acción:** decidir la regla definitiva monitoreando rechazos SIFEN reales.
**Labels:** `discussion`, `sifen`

---

## ⚙️ Configuración / Deploy

### CFG-1 · 🔴 `MAIL_PASSWORD` sin default puede romper el arranque
`application.yml`: `spring.mail.password: ${MAIL_PASSWORD}` sin default. Si falta en Render, la autoconfiguración de mail puede impedir el arranque del backend.
**Acción:** documentar como obligatoria (hecho) y/o hacer el mail opcional/lazy.
**Labels:** `bug`, `deployment`, `priority:high`

### CFG-2 · 🟡 Render suspendido conserva `autoDeploy` sobre `main`
_(reformulado 2026-08-05 tras la migración a Hetzner)_ El servicio `srv-d61m4p4hg0os73fpbjm0` está **suspendido**, no dado de baja, y mantiene `autoDeploy: yes` / `autoDeployTrigger: commit` sobre `main`. Si alguien lo reanuda, vuelve a auto-desplegar desde `main` — y quedarían **dos schedulers SIFEN activos** (Render + VM) consultando y emitiendo en paralelo.
**Acción:** al cerrar la ventana de rollback, dar de baja el servicio y retirar `render.yaml`. Mientras tanto, no reanudarlo. Ver `TAREAS_PENDIENTES.md` §5.
**Labels:** `tech-debt`, `deployment`, `priority:high`

### CFG-3 · 🟡 `JWT_EXPIRATION` / `LOG_LEVEL` inertes
Declaradas en `render.yaml` pero el código no las lee (`JwtTokenProvider` usa `jwt.expiration-ms` de `application-prod.yml`; los niveles de log están fijos). Falsa sensación de configurabilidad. Se arrastran a `deploy/.env.example` de la VM — verificar antes de confiar en ellas.
**Labels:** `tech-debt`, `deployment`

---

## 🧹 Calidad / Limpieza

### QA-1 · 🟠 Inutilización de números funciona parcialmente
Solo opera desvinculada de `FacturaLegal`. Ver `inutilizar-numeros-dialog.component.ts` y `SifenEventoService`.
**Labels:** `bug`, `sifen`

### QA-2 · 🟠 Features `reportes` y `auditoria` no cableadas a la navegación
`app.routes.ts` (L65 `reportes`, L78 `auditoria`) cargan `TestPageComponent` (placeholder) aunque los componentes de feature y sus `.routes.ts` existen completos. (La ruta `test` ~L84 también usa `TestPageComponent`, pero es un placeholder **intencional**, no una feature rota.)
**Acción:** cablear las rutas reales de `reportes` y `auditoria`.
**Labels:** `bug`, `frontend`

### QA-3 · 🟡 Interceptores muertos / duplicados
`interceptors/ruc-workaround.interceptor.ts` sin referencias; `core/interceptors/error.interceptor.ts` duplicado del activo `interceptors/error.interceptor.ts`.
**Labels:** `tech-debt`, `frontend`

### QA-4 · 🟡 Logs de debug en producción
`System.out.println("DEBUG: ...")` / `e.printStackTrace()` en 4 archivos: `controller/UserProfileController.java` (~L131-188), `service/sifen/SifenEventoService.java`, `config/DatabaseConfig.java` y `security/OAuth2TokenFilter.java`.
**Acción:** reemplazar por logger (SLF4J).
**Labels:** `tech-debt`, `backend`

### QA-5 · 🟡 Carpetas de referencia vacías enlazadas
`docs/franco-system-backend-filial/` y `docs/franco-system-backend-servidor/` están vacías pero `CLAUDE.md` las enlaza como si tuvieran contenido.
**Acción:** poblar, quitar el enlace, o documentar que son placeholders.
**Labels:** `docs`

### QA-6 · 🟡 Estándar de trigger de auditoría no seguido en migraciones nuevas
`DATABASE_STANDARDS.md` exige el trigger `actualizar_timestamp_modificacion()` en toda tabla, pero las migraciones nuevas (p. ej. V35 Vehiculo/Chofer) **no lo crean**: los timestamps se manejan vía JPA (`AuditableEntity` con `@PrePersist`/`@PreUpdate`) + `DEFAULT CURRENT_TIMESTAMP` en el DDL. El estándar documentado y la práctica real divergen.
**Acción:** decidir el patrón oficial (trigger SQL vs JPA auditing) y alinear el estándar con la realidad.
**Labels:** `tech-debt`, `docs`, `backend`

### QA-7 · 🟡 TODOs de datos incompletos en KuDE / Dashboard / Email
Varios datos quedan sin implementar (retornan vacío/placeholder): logo de empresa en KuDE (`KudePdfService.java:627`), nombre de distrito partida/llegada en remisión (`KudePdfService.java:682/688`), consulta directa de facturas (`DashboardService.java:152`), `xml_firmado` por separado (`EmailFacturaElectronicaService.java:138`).
**Acción:** implementar o documentar como no soportado en la UI.
**Labels:** `tech-debt`, `backend`

### QA-8 · 🟡 `UserService.java` código muerto
Existe `service/UserService.java` sin ninguna referencia en el código (gemelo confuso del real `UsuarioService`).
**Acción:** eliminar o documentar su propósito.
**Labels:** `tech-debt`, `backend`

## 📊 Reportes

### REP-1 · 🟠 Exportación de reportes a Excel/PDF no implementada (expuesta como funcional)
`ReporteExportService.java` lanza `UnsupportedOperationException("...no implementada...")` en todos los `exportar*Excel/*Pdf`. Los 8 endpoints `/api/reportes/{facturas,clientes,productos,usuarios}/{excel,pdf}` **fallan en runtime**. Apache POI se usa solo para *importar* productos, no para exportar reportes.
**Acción:** implementar la exportación o quitar/ocultar los endpoints y la UI hasta entonces.
**Labels:** `bug`, `backend`, `reportes`

---

## Resumen por severidad
- 🔴 **Alta (7):** SEC-1, SEC-2, SEC-3, SEC-4, RBAC-1, RBAC-2, CFG-1
- 🟠 **Media (9):** SEC-5, SEC-6, API-1, SIFEN-1, SIFEN-2, RBAC-3, QA-1, QA-2, REP-1
- 🟡 **Baja (13):** RBAC-4, RBAC-5, RBAC-6, SIFEN-3, SIFEN-4, CFG-2, CFG-3, QA-3, QA-4, QA-5, QA-6, QA-7, QA-8

_Total: 29 issues candidatos. Los SEC-1/SEC-2 (secrets) requieren acción del dueño (revocación) además del fix de repo._
