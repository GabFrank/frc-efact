# Dominio: Dashboard, Reportes y Auditoría

Tres features transversales de solo lectura sobre los datos del sistema. Ninguna tiene store NgRx (usan API service directo). ⚠️ **Reportes y Auditoría existen completas pero no están cableadas a la navegación** (QA-2, ver abajo).

---

## Dashboard — `DashboardController` `/dashboard`

KPIs agregados. Tres alcances, todos `[todos]` (`ADMIN, EMPRESA_ADMIN, FACTURADOR, LECTOR`):
- `GET /dashboard/usuario/{usuarioId}` → `DashboardUsuarioDto`
- `GET /dashboard/empresa/{empresaId}` → `DashboardEmpresaDto`
- `GET /dashboard/general` → `DashboardGeneralDto`

Lógica en `DashboardService`. El frontend (`features/dashboard/`: `dashboard.component.ts`, `dashboard-usuario/empresa.component.ts`, `date-filter.component.ts`) renderiza los KPIs con **Chart.js 4.5**. API `core/api/dashboard-api.service.ts` (+ `services/dashboard.service.ts`). Sin NgRx. Ruta **sí** cableada.

---

## Reportes — `ReporteController`

⚠️ **`@RequestMapping("/api/reportes")` sobre context-path `/api` → resuelve a `/api/api/reportes`** (doble prefijo, API-1). El frontend ya lo consume así.

⚠️ **Sin `@PreAuthorize` — ni a nivel clase ni a nivel método (SEC-5).** A diferencia del resto del sistema, todos los endpoints quedan accesibles a **cualquier usuario autenticado** sin distinción de rol. Confirmar si es intencional.

Cuatro reportes — cada uno en **JSON** (`GET`), **Excel** (`/…/excel`, Apache POI) y **PDF** (`/…/pdf`, JasperReports):
- `/facturas` (con `FacturaFiltroDto`: fecha/cliente/estado/monto, paginado)
- `/clientes` (ranking)
- `/productos`
- `/usuarios`

Servicios: `ReporteService` (datos) + `ReporteExportService` (Excel/PDF). Frontend `features/reportes/`: `reportes-list.component.ts`, `reporte-facturas/clientes/productos/usuarios.component.ts`. API `core/api/reporte-api.service.ts`. Sin NgRx.

---

## Auditoría — `AuditLog` + `AuditAspect` (AOP)

### Entidad `AuditLog` — esquema `auditoria.audit_log`
| Campo | Columna | Notas |
|---|---|---|
| `usuario` | `usuario_id` (FK, `NOT NULL`) | autor de la acción |
| `empresa` | `empresa_id` (FK) | contexto (nullable) |
| `entidadTipo` / `entidadId` | | entidad afectada |
| `accion` | `accion` (`auditoria.accion_enum`) | `AccionEnum`: `CREATE/UPDATE/DELETE/READ` |
| `fechaHora` | `fecha_hora` | |
| `valoresAnteriores` / `valoresNuevos` | `jsonb` | **snapshot antes/después** (`Map<String,Object>`) |
| `ipAddress` / `userAgent` / `descripcion` | | contexto de la request |

### `AuditAspect` (AOP) — `aspect/AuditAspect.java`
`@Around` sobre la anotación **`@com.frcefact.annotation.Auditable`**. No es automático en todo write: solo los métodos anotados con `@Auditable(accion=…)` se auditan. Para `UPDATE` captura los valores previos, ejecuta el método, y calcula el diff antes/después que persiste como JSONB.

### `AuditLogController`
⚠️ **`@RequestMapping("/api/auditoria")` → `/api/api/auditoria`** (doble prefijo, API-1).
`GET` `[A,EA]` · `GET /entidad/{tipo}/{id}` `[todos]` · `GET /ultimas-actividades` `[A,EA]` · `GET /usuario/{usuarioId}` `[A,EA]` · `GET /estadisticas` `[A,EA]` · `GET /count` `[A]` · `GET /count/empresa/{empresaId}` `[A,EA]`.

Frontend `features/auditoria/`: `auditoria-list/detail/entidad.component.ts`. API `core/api/audit-api.service.ts`. Sin NgRx.

---

## ⚠️ QA-2 — Reportes y Auditoría no cableadas
En `app.routes.ts` las rutas `reportes` (L65) y `auditoria` (L78), más otra ~L84, cargan `TestPageComponent` (placeholder) **en lugar** de las features reales — que existen completas con sus `*.routes.ts`. Para habilitarlas hay que reemplazar esos `loadComponent` por los `loadChildren`/`loadComponent` reales. Ver [../reference/known-bugs.md](../reference/known-bugs.md) y [../../../../docs/ISSUES_CANDIDATOS.md](../../../../docs/ISSUES_CANDIDATOS.md) (QA-2).

Índices: [../reference/entities-index.md](../reference/entities-index.md) · [../reference/endpoints-index.md](../reference/endpoints-index.md) · [../reference/enums-index.md](../reference/enums-index.md).
