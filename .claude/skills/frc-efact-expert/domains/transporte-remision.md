# Dominio: Transporte (Vehículo y Chofer)

Entidades de soporte de la **Nota de Remisión**: identifican el vehículo y el conductor del transportista. Esquema **`transporte`** (migración **V35**). Ambas **scoped por empresa** y **sin store NgRx** (API service directo).

---

## Vehiculo — `transporte.vehiculo`

Entidad `com.frcefact.model.Vehiculo` (`AuditableEntity`).

| Campo | Columna | Notas |
|---|---|---|
| `empresa` | `empresa_id` (FK, `NOT NULL`) | scope multi-empresa |
| `marca` | `marca` (100, `NOT NULL`) | |
| `matricula` | `matricula` (20, `NOT NULL`) | chapa/patente |
| `activo` | `activo` | baja lógica (reactivable) |

## Chofer — `transporte.chofer`

Entidad `com.frcefact.model.Chofer` (`AuditableEntity`).

| Campo | Columna | Notas |
|---|---|---|
| `empresa` | `empresa_id` (FK, `NOT NULL`) | scope |
| `nombre` | `nombre` (200, `NOT NULL`) | |
| `documento` | `documento` (20) | RUC/cédula del chofer |
| `direccion` | `direccion` (TEXT) | |
| `activo` | `activo` | baja lógica |

---

## Endpoints — patrón idéntico, scoped `/empresa/{empresaId}`

### `VehiculoController` `/vehiculos`
`POST` `[A,EA,F]` · `PUT .../{vehiculoId}` `[A,EA,F]` · `GET .../{vehiculoId}` · `GET /empresa/{empresaId}` · `.../paginado` · `.../buscar` · `.../filtrar` · `.../count` `[todos]` · `DELETE .../{vehiculoId}` `[A,EA]` · `PATCH .../{vehiculoId}/reactivar` `[A,EA]`.

### `ChoferController` `/choferes`
Mismo patrón (CRUD + `paginado`/`buscar`/`filtrar`/`count`/`reactivar`) y mismos roles que Vehículo.

---

## Relación con la Nota de Remisión

`NotaRemision` referencia el **transportista** (datos de transportista agregados en V33) y los datos de vehículo/chofer se vuelcan al XML SIFEN al generar el DE de remisión. Vehículo y Chofer alimentan esos campos.

> ⚠️ **SIFEN-4 (decisión abierta):** `SifenService` (~L2689) informa `dNomChof/dNumIDChof/dDirChof` **siempre que estén cargados**, incluso en transporte propio (`iTipTrans=PROPIO`), en contra de la recomendación histórica de omitirlos (documento/cédula inactiva del chofer → rechazo SIFEN). Monitorear rechazos. Ver [../reference/known-bugs.md](../reference/known-bugs.md).

Flujo completo de la nota de remisión (items, geografía origen/destino V31/V32, fecha estimada V34, generar-y-enviar): ver **[notas-cdr.md](notas-cdr.md)**.

---

## Frontend
`features/transporte/`: `vehiculos-list.component.ts` (+ `vehiculos/`), `choferes-list.component.ts` (+ `choferes/`), `transporte.routes.ts`. APIs `core/api/vehiculo-api.service.ts` y `chofer-api.service.ts`. Sin NgRx (ver [../reference/ngrx-state-index.md](../reference/ngrx-state-index.md)).

Índices: [../reference/entities-index.md](../reference/entities-index.md) · [../reference/endpoints-index.md](../reference/endpoints-index.md).
