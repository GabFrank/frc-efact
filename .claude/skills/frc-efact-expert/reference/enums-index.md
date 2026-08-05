# Catálogo de enums (backend `com.frcefact.model`)

Verificado directo del código 2026-08-05.

## `AccionEnum` (auditoría)
`CREATE`, `UPDATE`, `DELETE`, `READ`

## `EstadoDE` (DocumentoElectronico)
`PENDIENTE` → `EN_PROCESO` → `APROBADO` / `RECHAZADO` / `CANCELADO` / `ERROR`

## `EstadoEvento` (eventos cancelación/inutilización/nominación)
`PENDIENTE`, `APROBADO`, `RECHAZADO`, `ERROR_ENVIO`

## `EstadoLoteDE` (LoteDE)
`PENDIENTE`, `EN_PROCESO`, `APROBADO`, `RECHAZADO`, `ERROR`, `PROCESADO`, `ERROR_ENVIO`, `ERROR_PERMANENTE`

## `TipoClienteSifen`
Constructor: `(iNatRec, iTiContRec, iTiOpe, descripcion, esContribuyente)`. El método `requiereRuc()` devuelve ese mismo flag `esContribuyente`.

| Constante | iNatRec | iTiContRec | iTiOpe | esContribuyente / requiereRuc() |
|---|---|---|---|---|
| `PERSONA_FISICA` | 1 | 1 | 1 | true |
| `PERSONA_JURIDICA` | 1 | 2 | 1 | true |
| `NO_CONTRIBUYENTE` (Consumidor Final) | 2 | null | 2 | false |
| `EXTRANJERO` | 2 | null | 4 | false |
| `GUBERNAMENTAL` | 1 | 2 | 3 | true |

> Usado por `SifenReceptorHelper` (`getNaturalezaReceptor`, `getTipoContribuyente`, `getTipoOperacion`, `requiereRuc`). ⚠️ Ojo: el helper existe pero `SifenService` no lo usa (lógica manual duplicada) — ver [known-bugs.md](known-bugs.md).

## `TipoTransaccionProducto` (SIFEN D011 · iTipTra)
Constructor: `(codigo, descripcion)`

| Código | Constante |
|---|---|
| 1 | `VENTA_MERCADERIA` (default en el código) |
| 2 | `PRESTACION_SERVICIOS` |
| 3 | `MIXTO` |
| 4 | `VENTA_ACTIVO_FIJO` |
| 5 | `VENTA_DIVISAS` |
| 6 | `COMPRA_DIVISAS` |
| 7 | `PROMOCION_MUESTRAS` |
| 8 | `DONACION` |
| 9 | `ANTICIPO` |
| 10 | `COMPRA_PRODUCTOS` |
| 11 | `COMPRA_SERVICIOS` |
| 12 | `VENTA_CREDITO_FISCAL` |
| 13 | `MUESTRAS_MEDICAS` |

> `SifenService` fija `iTipTra(VENTA_MERCADERIA)` por defecto (comentario "puede ajustarse según producto").
