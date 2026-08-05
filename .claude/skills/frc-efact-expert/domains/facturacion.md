# Dominio: Facturación legal (FacturaLegal + FacturaLegalItem)

`FacturaLegal` es la factura de origen que luego se convierte en Documento Electrónico. Esquema `financiero`. Modelo: `model/FacturaLegal.java`, `model/FacturaLegalItem.java`. Service: `service/FacturaLegalService.java`. Controller: `controller/FacturaLegalController.java` (`@RequestMapping("/facturas")`).

## Numeración

Autonumera **por punto de expedición** (no por empresa ni por timbrado global). Unique constraint `uk_factura_timbrado_numero` sobre `(timbrado_detalle_id, numero_factura)`.

- Al crear (`crearFactura()`, ~L72), el número lo asigna `timbradoDetalleService.incrementarNumeroFactura(timbradoDetalleId)` (~L102) — el correlativo vive en el `TimbradoDetalle`, no se calcula con `MAX(...)`.
- Formato de presentación: `getNumeroFacturaFormateado()` → `EEE-PPP-NNNNNNN` (`codigoEstablecimientoFactura`-`puntoExpedicion`-`numeroFactura` a 7 dígitos).

## Moneda extranjera y totales

Los **totales SIEMPRE se guardan en guaraníes** (columnas `precision=15, scale=2`), incluso en factura en moneda extranjera. La moneda solo se registra como metadato para el DE:

- `monedaExtranjera` (varchar 3, ej. `USD`; `null`/`PYG` = local) + `cambio` (`precision=10, scale=4`). Migraciones V25/V26.
- El backend **no re-multiplica** los totales por el cambio al persistir — `crearFactura()` NO recalcula si los valores ya vienen del DTO en guaraníes (ver comentarios ~L152-205: "NO recalcular ... esto asegura que siempre se guarden en guaraníes"). El frontend envía los montos ya convertidos.
- Al generar el DE, `SifenService` propaga `monedaExtranjera`/`cambio` al XML (`cMoneOpe`, `dCondTiCam=GLOBAL`, `dTiCam`); la librería recalcula `dTotalGs = dTotGralOpe × dTiCam`. Ver [documento-electronico-lote.md](documento-electronico-lote.md) y [sifen-core.md](sifen-core.md).

## Ítems, IVA y recálculo

`recalcularTotales()` (en la entidad) reparte por tasa de IVA. **El `total` del ítem YA incluye el IVA:**
- IVA 0 → suma a `totalParcial0`.
- IVA 5 → IVA = `total / 21`; `totalParcial5 += total`.
- IVA 10 → IVA = `total / 11`; `totalParcial10 += total`.
- `totalParcial = totalParcial0 + 5 + 10`; `totalFinal = totalParcial − descuentoFinal`.

`aplicarDescuento(descuento)` fija `descuentoFinal` y recalcula `totalFinal` (descuento global, no por ítem). El IVA del producto se lee de `item.getProducto().getIva()`.

## Estados

No hay enum de estado en `FacturaLegal`. Se controla con:
- `activo` (boolean, borrado lógico / anulación) — `DELETE /facturas/{id}` es baja lógica.
- Relación `documentoElectronico` (`@OneToOne`) — presente = ya tiene DE generado. El estado SIFEN vive en el DE (`EstadoDE`), no en la factura.
- `credito` (boolean) — contado vs. crédito, afecta el XML (condición de pago, ver E644a/E605b en [sifen-core.md](sifen-core.md)).

## Endpoints `/facturas` (roles: ver [../reference/endpoints-index.md](../reference/endpoints-index.md))

| Método | Ruta | Roles | Nota |
|---|---|---|---|
| POST | `/facturas` | A,EA,F | Crea + autonumera |
| GET | `/facturas` · `/{id}` · `/resumen` · `/estadisticas` | todos | |
| PUT | `/facturas/{id}` | A,EA,F | |
| POST | `/facturas/{id}/items` | A,EA,F | |
| DELETE | `/facturas/{facturaId}/items/{itemId}` | A,EA,F | |
| PUT | `/facturas/{id}/descuento` | A,EA,F | |
| POST | `/facturas/{id}/recalcular` | A,EA,F | `recalcularTotalesFactura()` |
| DELETE | `/facturas/{id}` | A,EA | Baja lógica |
| POST | `/facturas/{id}/generar-de` | A,EA,F | → crea `DocumentoElectronico` (XML/CDC/firma/QR) |
| POST | `/facturas/{id}/desvincular-de` | A,EA,F | Desvincula el DE de la factura |
| GET | `/facturas/{id}/kude-pdf` | todos | KuDE (JasperReports) |
| POST | `/facturas/{id}/reenviar-email` | A,EA,F | Reenvío del KuDE por SMTP |

`generar-de` NO envía a SIFEN: solo produce el DE. El envío es aparte vía lote (`SifenController`). Flujo completo en [documento-electronico-lote.md](documento-electronico-lote.md).
