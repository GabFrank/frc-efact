# Frontend — capas (`frc-efact-frontend/src/app`)

Angular 17.3 standalone + Angular Material 17 + SCSS + NgRx 17.2. **Sin Tailwind.** JWT en memoria. Bootstrap por `app.config.ts` (providers) + `app.routes.ts` (rutas). Verificado 2026-08-05.

## Capas (flujo de un dato)

```
features/{dominio}/          componentes standalone (list / form / routes)   ── 12 features
   │  inyecta
core/api/{entidad}-api.service.ts   HTTP a ${apiUrl}/...  (apiUrl incluye /api)  ── 18 servicios
   │  (a través de interceptores)
core/state/{entidad}/        NgRx: actions/effects/reducer/selectors           ── SOLO 8 ramas
   │                         (no todas las entidades tienen store)
models/                      interfaces TS, espejo de los DTOs backend
```

## `models/`
Interfaces TypeScript espejo de los DTOs del backend (`Cliente`, `FacturaLegal`, `DocumentoElectronico`, `UsuarioDto`, etc.). No hay lógica; solo tipos.

## `core/api/` — 18 servicios HTTP (`*-api.service.ts`)
Uno por entidad: `audit, chofer, cliente, dashboard, documento-electronico, empresa, factura, nota-credito, nota-debito, nota-remision, producto, profile, reporte, sifen, timbrado, timbrado-detalle, usuario, vehiculo`. Todos llaman `${environment.apiUrl}/...` (apiUrl = `http://localhost:8080/api`). ⚠️ Los servicios de `geografia`/`audit`/`reporte` consumen el doble prefijo `/api/api/...` para matchear el bug de routing del backend.

## `core/state/` — NgRx, **solo 8 ramas**
Registradas en `app.config.ts` (`provideStore` + `provideEffects`): `auth`, `empresas`, `facturacion`, `documentos`, `usuarios`, `timbrados`, `timbradoDetalles`, `notas`. Cada una con `actions/effects/reducer/selectors`.
⚠️ La convención "un store por entidad principal" **no se cumple**: `clientes, productos, reportes, dashboard, auditoria, transporte` usan el API service directo, sin store. Al agregar entidad, **decidir explícitamente** si lleva store. Ver [reference/ngrx-state-index.md](../reference/ngrx-state-index.md).

## `core/services/` — cross-cutting (4)
- `permissions` — ⚠️ **solo lee `user.roles` globales, NO `rolEmpresa`** → oculta menús/botones aunque el backend autorice (bug RBAC-1, ver [auth-seguridad.md](auth-seguridad.md) y [known-bugs.md](../reference/known-bugs.md)).
- `notification`, `pdf-share`, `theme`.

## `src/app/services/` — servicios de negocio
`auth`, `connection-status`, `dashboard`, `sifen`, `ruc-validation` (⚠️ **validación de DV deshabilitada**; el backend sí valida).

## `interceptors/` (registrados en `app.config.ts`, en orden)
`httpsInterceptor → authInterceptor (Bearer) → errorInterceptor → mockRucInterceptor`.
⚠️ `mockRucInterceptor` sirve RUCs ficticios hardcodeados, **incluso en prod** (SEC-4). Interceptores **muertos/no registrados**: `ruc-workaround.interceptor.ts`, `core/interceptors/error.interceptor.ts` (duplicado).

## `guards/`
`auth`, `no-auth`, `role`, `empresa-access`, `empresa-selected`.

## `layout/` — shell
`main-layout.component.ts` — layout principal (sidebar + topbar) que envuelve las rutas autenticadas. El login (`components/login`) queda fuera del layout.

## `features/` — 12
`auditoria, clientes, dashboard, documentos, empresas, facturacion, notas, productos, reportes, timbrados, transporte, usuarios`. Componentes standalone, separando `*-list.component.ts` de `*-form.component.ts` cuando aplica. Features con submódulos exponen `*.routes.ts` (`empresas`, `timbrados`, `facturacion`, `notas`, `transporte`, `documentos`, `usuarios`).

## Navegación (`app.routes.ts`)
Raíz `''` → redirect a login; `login` fuera del layout; el resto cuelga de `MainLayoutComponent` con `loadComponent`/`loadChildren` (lazy). Rutas: `dashboard, empresas, timbrados, clientes, productos, facturacion, notas, transporte, documentos, reportes, usuarios, perfil, auditoria, test`.
⚠️ **`reportes` y `auditoria` cargan `TestPageComponent`** (`features/test-page.component`), no sus features reales — que existen completas pero no están cableadas (bug QA-2 en [known-bugs.md](../reference/known-bugs.md)). También hay una ruta `test` explícita al mismo componente.
