# Dominio: Timbrados y Puntos de expedición

El **Timbrado** es la autorización de la SET; sus **TimbradoDetalle** son los puntos de expedición (establecimiento + punto) que numeran las facturas. Ambos en esquema **`financiero`**. Tienen store NgRx (`timbrados`, `timbrado-detalles`).

---

## Timbrado — `financiero.timbrado`

Entidad `com.frcefact.model.Timbrado` (`AuditableEntity`).

| Campo | Columna | Notas |
|---|---|---|
| `empresa` | `empresa_id` (FK, `NOT NULL`) | scope multi-empresa |
| `numero` | `numero` (20, `NOT NULL`) | nº de timbrado SET |
| `isElectronico` | `is_electronico` (`NOT NULL`) | `false`=físico / `true`=electrónico |
| `cscEncrypted` | `csc_encrypted` (TEXT) | **CSC cifrado** (AES) — solo timbrado electrónico |
| `cscId` | `csc_id` (50) | identificador del CSC |
| `fechaInicio` | `fecha_inicio` (`NOT NULL`) | vigencia desde |
| `fechaFin` | `fecha_fin` (`NOT NULL`) | vigencia hasta |
| `activo` | `activo` | baja lógica |
| `timbradoDetalles` | (1:N) | puntos de expedición |

El **CSC vive en el timbrado** (migración V14 lo movió desde Empresa). ⚠️ Nota: la entidad `Empresa` **todavía conserva** columnas `csc_id`/`csc_encrypted` residuales — el flujo real de firma electrónica usa el CSC del timbrado.

Rangos de numeración: los timbrados electrónicos **no** llevan rango a nivel timbrado (los rangos son opcionales y viven en el detalle, V17).

---

## TimbradoDetalle — `financiero.timbrado_detalle`

Entidad `com.frcefact.model.TimbradoDetalle` (`AuditableEntity`). Punto de expedición + control de numeración.

| Campo | Columna | Notas |
|---|---|---|
| `timbrado` | `timbrado_id` (FK, `NOT NULL`) | timbrado padre |
| `codigoEstablecimientoFactura` | `codigo_establecimiento_factura` (10, `NOT NULL`) | establecimiento (ej. `001`) |
| `puntoExpedicion` | `punto_expedicion` (10, `NOT NULL`) | punto (ej. `001`) |
| `cantidad` / `rangoDesde` / `rangoHasta` | (nullable) | rangos **opcionales** (V17: null permitido para electrónicos) |
| `numeroActual` | `numero_actual` (nullable) | contador de **autonumeración por punto** |
| `ciudad` | `ciudad_id` (FK, `NOT NULL`, EAGER) | geografía del punto (V15) |
| `barrio` | `barrio_id` (FK, EAGER) | opcional |
| `direccion` / `telefono` | | del establecimiento |
| `activo` | `activo` | |
| `version` | `version` | `@Version` optimistic locking |

### Autonumeración
La `FacturaLegal` se autonumera contra el `TimbradoDetalle` (avanza `numeroActual` dentro de `[rangoDesde, rangoHasta]`). El formato SIFEN del número de factura es `establecimiento-punto-secuencial` (ej. `001-001-0000123`).

---

## Endpoints

### `TimbradoController` `/timbrados`
`POST` `[A,EA]` · `PUT /{id}` `[A,EA]` · `GET /{id}` · `GET /empresa/{empresaId}` · `GET /empresa/{empresaId}/activos` · `GET /{id}/vigente` · `GET /empresa/{empresaId}/vigentes` · `GET /empresa/{empresaId}/electronicos-vigentes` · `GET /empresa/{empresaId}/por-vencer` `[todos]` · `DELETE /{id}` `[A,EA]`.

### `TimbradoDetalleController` — **`@RequestMapping` sin path** (rutas absolutas mixtas)
Este controller no tiene prefijo; declara dos familias de rutas:
- Anidadas al timbrado: `POST /timbrados/{timbradoId}/detalles` `[A,EA]` · `GET /timbrados/{timbradoId}/detalles` · `GET /timbrados/{timbradoId}/detalles/activos` · `GET /timbrados/{timbradoId}/detalles/por-agotarse` `[todos]`.
- Por detalle: `GET /timbrado-detalles/{id}` `[todos]` · `PUT /timbrado-detalles/{id}` `[A,EA]` · `DELETE /timbrado-detalles/{id}` `[A,EA]` · `GET /timbrado-detalles/empresa/{empresaId}` `[todos]`.

**Vigencia / alertas:** `/{id}/vigente` y `/vigentes` filtran por fecha; `/por-vencer` (timbrado próximo a `fechaFin`) y `/detalles/por-agotarse` (numeración próxima a `rangoHasta`) alimentan avisos en el dashboard.

---

## Frontend
`features/timbrados/`: `timbrado-list/form.component.ts`, `timbrado-detalle-list/form/dialog.component.ts`. APIs `core/api/timbrado-api.service.ts` y `timbrado-detalle-api.service.ts`. NgRx: ramas `timbrados` y `timbrado-detalles` (`core/state/`).

Índices: [../reference/entities-index.md](../reference/entities-index.md) · [../reference/endpoints-index.md](../reference/endpoints-index.md) · [../reference/migrations-index.md](../reference/migrations-index.md).
