# FRC eFact Frontend

SPA Angular del sistema de facturación electrónica **FRC eFact** (Paraguay / SIFEN).
Multi-empresa, multi-usuario, con dashboards, generación de documentos electrónicos,
notas de crédito/débito/remisión y descarga de KuDE PDF.

Backend asociado: `frc-efact-backend` (Spring Boot, API en `/api`).

---

## Stack tecnológico

| Área | Tecnología | Versión (package.json) |
|------|------------|------------------------|
| Framework | Angular (standalone components) | 17.3 |
| UI | Angular Material | 17.3 |
| Estado | NgRx (store, effects, entity, devtools) | 17.2 |
| Auth | @auth0/auth0-angular | 2.3 |
| Gráficos | Chart.js | 4.5 |
| Lenguaje | TypeScript | 5.4 |
| Reactivo | RxJS | 7.8 |
| Node | 18+ | — |
| App version | — | 1.0.3 |

Autenticación: **doble vía** — login local con **JWT** (usuario/contraseña contra
`/auth/login`) **y** login con **Auth0** (`provideAuth0` configurado en `app.config.ts`).

---

## Estructura del proyecto

```
src/
├── app/
│   ├── components/          # Componentes sueltos (login, etc.)
│   │   └── login/
│   ├── layout/             # main-layout (sidebar/topbar) tras autenticación
│   ├── features/           # Páginas por dominio (lazy-loaded)
│   │   ├── dashboard/
│   │   ├── empresas/
│   │   ├── timbrados/
│   │   ├── clientes/
│   │   ├── productos/
│   │   ├── facturacion/
│   │   ├── notas/
│   │   ├── transporte/     # vehículos + choferes
│   │   ├── documentos/
│   │   ├── usuarios/       # + perfil
│   │   ├── reportes/       # (ruta usa placeholder — ver nota)
│   │   ├── auditoria/      # (ruta usa placeholder — ver nota)
│   │   └── test-page.component.ts
│   ├── core/
│   │   ├── api/            # 18 servicios HTTP por entidad
│   │   ├── state/          # NgRx: 8 ramas (no todas las entidades)
│   │   ├── services/       # 4 servicios cross-cutting
│   │   └── interceptors/   # error.interceptor.ts (NO registrado — ver nota)
│   ├── interceptors/       # Interceptores HTTP registrados en app.config.ts
│   ├── guards/             # Guards de routing
│   ├── models/             # Interfaces TypeScript (espejo de DTOs)
│   ├── shared/             # Componentes/dialogos reutilizables
│   ├── app.component.ts
│   ├── app.config.ts       # Providers: router, Auth0, HttpClient+interceptors, NgRx
│   └── app.routes.ts
├── environments/
│   ├── environment.ts      # dev (apiUrl detecta localhost / IP LAN)
│   └── environment.prod.ts # prod (apiUrl backend en Render)
├── _redirects              # SPA fallback (respaldo local)
├── styles.scss
└── index.html
```

### Features (12 dominios)

`dashboard`, `empresas`, `timbrados`, `clientes`, `productos`, `facturacion`,
`notas` (crédito/débito/remisión), `transporte` (vehículos + choferes),
`documentos` (documentos electrónicos), `usuarios` (+ perfil), `reportes`, `auditoria`.

> **Nota real (código):** en `app.routes.ts`, las rutas `reportes` y `auditoria`
> cargan actualmente `TestPageComponent` (placeholder), no un componente de feature
> propio. La UI de esos módulos todavía no está cableada a la ruta.

### NgRx (`core/state/`) — 8 ramas

Store por entidad: `auth`, `empresas`, `facturacion`, `documentos`, `usuarios`,
`timbrados`, `timbrado-detalles`, `notas`.

> **No todas las entidades tienen store.** `clientes`, `productos`, `vehiculos` y
> `choferes` se manejan **solo vía servicios API** (`core/api/`), sin NgRx.

### `core/api/` — 18 servicios HTTP

`audit`, `chofer`, `cliente`, `dashboard`, `documento-electronico`, `empresa`,
`factura`, `nota-credito`, `nota-debito`, `nota-remision`, `producto`, `profile`,
`reporte`, `sifen`, `timbrado`, `timbrado-detalle`, `usuario`, `vehiculo`.

### `core/services/` — 4 servicios cross-cutting

- `permissions.service.ts` — control de visibilidad de menús/botones por rol.
- `notification.service.ts` — notificaciones/snackbars.
- `pdf-share.service.ts` — compartir/descargar PDFs (KuDE).
- `theme.service.ts` — tema claro/oscuro.

### Guards (`guards/`)

- `auth.guard.ts` — protege rutas autenticadas.
- `no-auth.guard.ts` — evita que un usuario logueado entre a `/login`.
- `empresa-selected.guard.ts` — exige empresa seleccionada (clientes, productos,
  facturación, notas, transporte, documentos, reportes, auditoría).
- `empresa-access.guard.ts`, `role.guard.ts` — presentes en el código (no todos
  cableados en `app.routes.ts` hoy).

### Interceptores HTTP

Registrados en `app.config.ts` (`withInterceptors([...])`), **en este orden**:

1. `httpsInterceptor` (`interceptors/https.interceptor.ts`) — fuerza HTTPS en prod.
2. `authInterceptor` (`interceptors/auth.interceptor.ts`) — inyecta el JWT local.
3. `errorInterceptor` (`interceptors/error.interceptor.ts`) — manejo global de errores (401/403).
4. `mockRucInterceptor` (`interceptors/mock-ruc.interceptor.ts`) — **intercepta
   `/api/empresas/validate-ruc` y devuelve RUCs mock.** Deuda técnica: sigue activo
   incluso en producción. Ver [docs/ESTADO_VALIDACION_RUC.md](./docs/ESTADO_VALIDACION_RUC.md).

> **Interceptores muertos (no registrados):**
> - `interceptors/ruc-workaround.interceptor.ts` — sin referencias.
> - `core/interceptors/error.interceptor.ts` — duplicado; el que se usa es
>   `interceptors/error.interceptor.ts`.

---

## Requisitos previos

- Node.js 18+ y npm 9+
- Angular CLI 17 (opcional global): `npm install -g @angular/cli@17`

## Configuración de entorno

`src/environments/environment.ts` (dev) resuelve `apiUrl` dinámicamente:
`http://localhost:8080/api` en localhost, o `http://<ip-lan>:8080/api` si se accede
por IP de red local.

`src/environments/environment.prod.ts` (prod) apunta a
`https://frc-efact-backend.onrender.com/api`.

Ambos incluyen la config de Auth0 (`domain`, `clientId`, `audience`).

> `apiUrl` es **compile-time**: se resuelve al construir el bundle según la
> configuración (`development` / `production`), no por variables de entorno en runtime.

## Scripts npm

```bash
npm start                 # ng serve → http://localhost:4200
npm run start:network     # ng serve accesible por IP de red local
npm run start:prod        # ng serve con configuración production
npm run build             # ng build (config por defecto)
npm run build:dev         # build desarrollo
npm run build:prod        # build producción (usado en deploy)
npm run watch             # build dev en watch mode
npm test                  # tests unitarios (Karma)
npm run test:ci           # tests headless (ChromeHeadless, sin watch)
npm run test:coverage     # tests con cobertura
npm run lint              # ESLint
npm run lint:fix          # ESLint con --fix
npm run analyze           # análisis de tamaño de bundle
```

## Build para producción

```bash
npm run build:prod
```

Salida en `dist/frc-efact-frontend/browser/`.

## Deployment (Render)

Sitio estático en Render, definido en [`render.yaml`](../render.yaml) (raíz del repo):

- **Build command:** `cd frc-efact-frontend && npm ci && npm run build:prod`
- **Publish path:** `frc-efact-frontend/dist/frc-efact-frontend/browser`
- **SPA routing:** `render.yaml` usa `routes` con `rewrite` de `/*` → `/index.html`
  (más `src/_redirects` como respaldo).
- **Auto-deploy:** cada push a `main` dispara deploy. **No** disparar deploys por API
  ni por el botón manual del dashboard — solo por `git push` (convención del proyecto).

Detalle completo: [DEPLOYMENT.md](./DEPLOYMENT.md).

## Seguridad

Ver [SECURITY.md](./SECURITY.md). En resumen: JWT en memoria (no `localStorage`),
interceptor HTTPS, guards de routing, sanitización automática de Angular.

> **Deuda de seguridad conocida:** los security headers (X-Frame-Options, CSP, etc.)
> **no están configurados** en `render.yaml`. Ver SECURITY.md.

## Convenciones de código

- **Archivos:** kebab-case (`login.component.ts`).
- **Clases:** PascalCase (`LoginComponent`).
- **Servicios:** sufijo `Service`; **Guards:** sufijo `Guard`.
- **Interfaces:** PascalCase, en `models/`.
- **Componentes:** separar `*-list.component.ts` y `*-form.component.ts`.
- **NgRx:** una rama por entidad principal (ver `core/state/`).
- **URLs API:** siempre con `/api/`: `${environment.apiUrl}/clientes`.
- **JWT:** en memoria, nunca `localStorage`.
- **Idioma:** UI y dominio en español.

## Issues conocidos

- **PermissionsService no lee `rolEmpresa`.** `core/services/permissions.service.ts`
  solo mira `user.roles` (roles globales). Aunque el backend autorice a un usuario con
  `rolEmpresa = ADMINISTRADOR`, el frontend le oculta menús/botones. Workaround: asignar
  también el rol global correspondiente (`EMPRESA_ADMIN`). Ver `CLAUDE.md` (raíz).
- **Validación de RUC deshabilitada + mock interceptor activo.** Ver
  [docs/ESTADO_VALIDACION_RUC.md](./docs/ESTADO_VALIDACION_RUC.md).
- **Security headers ausentes** en `render.yaml`. Ver [SECURITY.md](./SECURITY.md).

## Documentación relacionada

- [DEPLOYMENT.md](./DEPLOYMENT.md) — Guía de deployment.
- [SECURITY.md](./SECURITY.md) — Consideraciones de seguridad.
- [docs/ESTADO_VALIDACION_RUC.md](./docs/ESTADO_VALIDACION_RUC.md) — Estado real de la validación RUC.
- `CLAUDE.md` (raíz del repo) — Arquitectura general y issues de roles/permisos.

## Recursos

- [Angular](https://angular.io/docs) · [Angular Material](https://material.angular.io/)
  · [NgRx](https://ngrx.io/) · [RxJS](https://rxjs.dev/) · [Auth0 Angular](https://github.com/auth0/auth0-angular)
