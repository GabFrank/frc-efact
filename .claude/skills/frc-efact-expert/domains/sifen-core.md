# Dominio: SIFEN core — cómo `SifenService` arma el XML

`service/sifen/SifenService.java` (~3400 líneas) construye el objeto `DE` de **jsifenlib** (fork `io.github.gabfrank` `0.2.4-frc.13`) y llama `deSifen.generarXml(ctx)`. Hay **tres rutas de construcción paralelas** (factura, y variantes para notas) con lógica muy similar y en gran parte **duplicada a mano** — por eso muchos fixes hay que aplicarlos en 3 lugares. El XML SIFEN es estrictísimo: un campo de más/menos produce rechazos `E605b`, `E644a`, etc. Antes de tocar: leer [../../../../docs/ANALISIS_DIFERENCIAS_SIFEN_SERVICE.md](../../../../docs/ANALISIS_DIFERENCIAS_SIFEN_SERVICE.md) y [conventions/sifen-gotchas.md](../conventions/sifen-gotchas.md).

## Estructura del XML que se arma

### Datos generales de operación (`gOpeCom`)
- `iTipTra` (tipo de transacción, D011): **hardcodeado** `TTipTra.VENTA_MERCADERIA` (~L1266, 1774, 2204) con comentario "puede ajustarse según producto". No lee `producto.tipoTransaccion`. Ver enum `TipoTransaccionProducto` en [../reference/enums-index.md](../reference/enums-index.md).
- **Moneda** (D015–D019, bloque ~L1268-1293):
  - Local: `cMoneOpe = CMondT.PYG`.
  - Extranjera (`monedaExtranjera` ≠ null/`PYG`): `cMoneOpe = CMondT.valueOf(moneda.toUpperCase())`, `dCondTiCam = TdCondTiCam.GLOBAL`, `dTiCam = cambio.setScale(6, HALF_UP)`. Exige `cambio > 0` o lanza `BusinessException`.

### Emisor (`gEmis`, `construirDatosEmisor`)
Datos fiscales de la empresa + timbrado. **`iTipCont` (tipo de contribuyente del emisor) está hardcodeado `TiTipCont.PERSONA_JURIDICA`** en las 3 rutas (**L1351, L1881, L2780**). ⚠️ Deuda real (SIFEN-1): un emisor persona física generaría XML incorrecto. Ver [../reference/known-bugs.md](../reference/known-bugs.md).

### Receptor (`gDatRec`, `construirDatosReceptor` ~L1415)
Lógica **manual duplicada** según tipo de cliente:
- Sin cliente → innominado: `iNatRec=NO_CONTRIBUYENTE`, `iTiOpe=B2C`, `iTipIDRec=INNOMINADO`, `dNumIDRec="0"`, `dNomRec="Sin Nombre"`, `cPaisRec=PRY`.
- Contribuyente (`cliente.requiereRuc()` && RUC presente) → `iNatRec=CONTRIBUYENTE`, `iTiOpe=B2B`, `iTiContRec` según `tipoContribuyenteCodigo` (1=PF, else PJ; default PF).
- ⚠️ **`SifenReceptorHelper` existe pero NO se usa** — la lógica está copiada a mano en las 3 rutas (~L1415, L1931, L2831). Si tocás el receptor, revisá los 3 lugares. El helper mapea vía enum `TipoClienteSifen` (SIFEN-2). Ver [../reference/enums-index.md](../reference/enums-index.md).

### Ítems (`gCamItem`)
Por ítem: descripción, cantidad, precio, y grupo IVA. `dBasExe` **no se setea** (~L1659, L1673) — la librería lo maneja. `iTipTra` a nivel operación (no por ítem) como arriba.

### Totales
**Los calcula la librería jsifenlib, no `SifenService`.** No se setean manualmente `dTotalGs` ni `dBasExe`. Para moneda extranjera, `dTotalGs = dTotGralOpe × dTiCam` se calcula **después** del redondeo oficial (Resolución 314/2014 SEDECO) — comportamiento aceptado por SIFEN, no corregir (comentario ~L986).

## Lecciones aprendidas ya aplicadas en el código

- **E644a / 1706 — condición de pago a crédito:** cuando `iCondCred = PLAZO` (E641=1) **no** se envía `dCuotas` (E644); solo `dPlazoCre`. Ver ~L1544-1548. jsifenlib enum `TiCondCred.PLAZO`.
- **E605b / 1552 — crédito sin entrega inicial:** en factura a crédito **no** se arma el grupo `gPaConEIni` (~L1548 "No se setea gPaConEIniList porque no hay entrega inicial"). En **contado** sí se arma `gPaConEIni` con el pago completo (~L1551-1600).
- **Moneda extranjera en el pago:** cuando hay moneda extranjera también se setea `cMoneTiPag` y `dTiCamTiPag` en `gPaConEIni` (~L1561-1568), no solo en `gOpeCom`.

## Deuda técnica (⚠️ NO romper al editar)

| Item | Ubicación | Referencia |
|---|---|---|
| `iTipCont` emisor hardcodeado `PERSONA_JURIDICA` | L1351 / L1881 / L2780 | SIFEN-1 |
| `SifenReceptorHelper` sin usar (lógica receptor duplicada) | ~L1415 / L1931 / L2831 | SIFEN-2 |
| `iTipTra` hardcodeado `VENTA_MERCADERIA` | L1266 / L1774 / L2204 | enums-index |
| Datos del chofer se envían siempre (incluso transporte propio) | ~L2689 | SIFEN-4, ver [notas-cdr.md](notas-cdr.md) |

Helpers de apoyo: `sifen/util/{SifenReceptorHelper, SifenTotalsHelper, SifenResponseParser, SifenDocumentoLogger}`, `sifen/config/{SifenConfigFactory, SifenProperties}`.
