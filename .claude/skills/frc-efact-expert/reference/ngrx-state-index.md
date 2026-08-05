# Índice NgRx + servicios frontend

Verificado 2026-08-05. **Regla no cumplida:** la convención "un store NgRx por entidad principal" (CLAUDE.md) **no se respeta** — solo 8 entidades tienen store; el resto usa el API service directo.

## Ramas NgRx (`core/state/`) — 8
Cada una con `actions / effects / reducer / selectors`:
`auth`, `empresas`, `facturacion`, `documentos`, `usuarios`, `timbrados`, `timbrado-detalles`, `notas`

## Features SIN store NgRx (usan API service directo)
`clientes`, `productos`, `reportes`, `dashboard`, `auditoria`, `transporte` (vehículos/choferes)
→ Al agregar una entidad nueva, **decidir explícitamente** si lleva store o no; no asumir que todas lo tienen.

## Servicios API (`core/api/`) — 18 (`*-api.service.ts`)
audit, chofer, cliente, dashboard, documento-electronico, empresa, factura, nota-credito,
nota-debito, nota-remision, producto, profile, reporte, sifen, timbrado, timbrado-detalle,
usuario, vehiculo

## Servicios cross-cutting (`core/services/`) — 4
`permissions` (⚠️ solo lee `user.roles` globales, no `rolEmpresa` — ver known-bugs), `notification`, `pdf-share`, `theme`

## Servicios de negocio (`src/app/services/`)
`auth`, `connection-status`, `dashboard`, `ruc-validation` (⚠️ validación deshabilitada), `sifen`

## Interceptores (`src/app/interceptors/`, registrados en `app.config.ts`)
`httpsInterceptor`, `authInterceptor`, `errorInterceptor`, `mockRucInterceptor` (⚠️ sirve RUCs mock hardcodeados)
- **Muertos / no registrados:** `ruc-workaround.interceptor.ts`, `core/interceptors/error.interceptor.ts` (duplicado)

## Guards (`src/app/guards/`)
`auth`, `no-auth`, `role`, `empresa-access`, `empresa-selected`

## Features (`features/`) — 12
auditoria, clientes, dashboard, documentos, empresas, facturacion, notas, productos, reportes, timbrados, transporte, usuarios
