# Bugs conocidos y deuda técnica

Verificado 2026-08-05. Registro completo y accionable (formato issue de GitHub) en [`docs/ISSUES_CANDIDATOS.md`](../../../../docs/ISSUES_CANDIDATOS.md). Acá va el resumen operativo para no romper cosas al trabajar.

## 🔴 Tener MUY presente
- **`PermissionsService` (frontend) no mira `rolEmpresa`** — solo lee `user.roles`. Aunque el backend autorice con `rolEmpresa`, el frontend oculta menús/botones. Workaround: asignar también el rol global. Fix de raíz: `UsuarioMapper.toDto()` debe inyectar los roles de empresa. (RBAC-1)
- **Validación de RUC del frontend deshabilitada** — `ruc-validation.service.ts` no valida DV; `mock-ruc.interceptor.ts` sirve RUCs ficticios hardcodeados, **también en prod**. El backend (`CalcularVerificadorRuc`) sí es correcto. No confiar en la validación de RUC del front. (SEC-4)
- **Usuario nuevo queda sin rol** — ni `crearUsuario()` ni el auto-registro Auth0 asignan rol por defecto. (RBAC-2)
- **`ENCRYPTION_KEY` y `MAIL_PASSWORD`** — si faltan en prod, la primera cifra con clave pública conocida y la segunda puede romper el arranque. (SEC-3, CFG-1)

## 🟠 Trampas al editar
- **Routing `/api/api/...`** — `GeografiaController`, `AuditLogController`, `ReporteController` usan `@RequestMapping("/api/...")` sobre context-path `/api`. El frontend ya los consume con doble prefijo; corregir exige tocar controller **y** api-service juntos. (API-1)
- **`SifenService`: `iTipCont` emisor hardcodeado `PERSONA_JURIDICA`** (~L1351/1881/2780) — emisor persona física generaría XML incorrecto. (SIFEN-1)
- **`SifenReceptorHelper` existe pero no se usa** — la lógica de receptor está duplicada a mano en `SifenService` (~L1415/1931/2831). Si tocás el receptor, mirá los 3 lugares. (SIFEN-2)
- **Datos del chofer se envían siempre** (~L2689), incluso en transporte propio — decisión abierta, monitorear rechazos. (SIFEN-4)
- **Inutilización de números: parcial** — solo desvinculada de `FacturaLegal`. (QA-1)
- **`reportes` y `auditoria` cargan `TestPageComponent`** en `app.routes.ts` aunque las features existen completas — no están cableadas. (QA-2)

## 🟡 Menores / limpieza
- Interceptores muertos: `ruc-workaround.interceptor.ts`, `core/interceptors/error.interceptor.ts` (duplicado). (QA-3)
- `System.out.println`/`printStackTrace` de debug en `UserProfileController.actualizarDesdeAuth0()`. (QA-4)
- `SecurityConfig` protege `/usuarios/admin/**` inexistente. (RBAC-6)
- `JWT_EXPIRATION`/`LOG_LEVEL` en `render.yaml` son inertes. (CFG-3)
- `docs/franco-system-backend-*/` vacías pero enlazadas en CLAUDE.md. (QA-5)
- **Secrets filtrados** en `CONFIGURACION_IDE.md` (Gmail) y `AGREGAR_VARIABLES_RENDER.md`/`RENDER_DOCKER_BUILD_ARGS.md` (GitHub token) — revocar + purgar historial. (SEC-1, SEC-2)
