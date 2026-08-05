# Workflow: Debuggear un rechazo SIFEN

Cuando un DE queda `RECHAZADO` o `ERROR`. El XML SIFEN es **estrictísimo**: un campo de más o de menos rompe la validación. Trampas de campo en [../conventions/sifen-gotchas.md](../conventions/sifen-gotchas.md); dominio del envío en [../domains/documento-electronico-lote.md](../domains/documento-electronico-lote.md).

> Antes de tocar `SifenService` leé [../conventions/sifen-gotchas.md](../conventions/sifen-gotchas.md), `docs/ANALISIS_DIFERENCIAS_SIFEN_SERVICE.md` y los manuales de `docs/sifen/`.

---

## 1. Leer el motivo del rechazo
La respuesta de SIFEN queda persistida en el `DocumentoElectronico`:
- `estado` (`RECHAZADO` / `ERROR`)
- `codigoRespuestaSifen` — el código (`E605b`, `E644a`, `E962`, …)
- `mensajeRespuestaSifen` — texto legible del error
- `respuestaSifen` — **XML crudo** de SET (para parsear campo exacto)

Forzá una consulta si el estado quedó viejo: `POST /documentos-electronicos/{id}/consultar` o `POST /sifen/documentos/{cdc}/consultar`.

**Helpers para extraer datos del XML** (`sifen/util/`):
- `SifenResponseParser` (estático): `extractCodigoRespuesta(xml)`, `extractMensajeRespuesta(xml)`, `extractEstadoResultado(xml)`, `extractDocumentResults(xml)` (lista de `DocumentResult{cdc,codigo,estado,mensaje,protocolo}`), `extractEventResults(xml)`.
- `SifenDocumentoLogger.logDatosCdc(de, config)` — vuelca al log los datos que compusieron el CDC (útil para cotejar timbrado/fecha/tipo DE).

## 2. Mapear código → causa → fix
| Código / síntoma | Causa | Fix |
|---|---|---|
| **E644a / 1706** | Se envió `dCuotas` con `iCondCred=1` (Plazo) | No mandar `dCuotas` cuando la condición es a plazo (commit `e9db5de`). |
| **E605b / 1552** | Se envió `gPaConEIni` en factura a **crédito sin entrega inicial** | No emitir `gPaConEIni` sin entrega inicial (commit `de5542c`). |
| **E962 – `dMarVeh` inválido** (NRE) | Marca de vehículo no aceptada | Ver `docs/sifen/analisis-errores-nre-sifen-v150.md` §5. |
| **`cCiuSal` / `cCiuEnt` = 0 inválido** (NRE) | Ciudad de salida/entrega no seteada o `0` | Cargar IDs de ciudad reales en la nota de remisión (§3/§4 del mismo doc). |
| **`dDirRec` / `dInfoFisc` inválido** (NRE) | Dirección / info fiscal del receptor mal armada | §1/§2 del doc de errores NRE. |
| **Motivo de nota de crédito inválido** | Motivo no está en el catálogo SIFEN | Usar motivos validados; heredar moneda de la factura referenciada (commits `1680341`, `21f4a89`, `7a8e215`). |

Fuentes completas: `docs/sifen/analisis-errores-nre-sifen-v150.md`, `docs/sifen/manual-implementacion-nre-y-cancelacion-sifen-v150.md`, `docs/sifen/implementacion_monedas_sifen_v150.md`.

## 3. Checklist de campos a revisar (los que más rompen)
- [ ] **Moneda:** notas heredan la moneda de la factura referenciada; moneda extranjera exige `tipo_cambio` y totales en guaraníes. Ver `docs/sifen/implementacion_monedas_sifen_v150.md`.
- [ ] **Condición de crédito:** `iCondCred=1` (plazo) → sin `dCuotas`; crédito sin entrega inicial → sin `gPaConEIni`.
- [ ] **Receptor:** tipo de contribuyente/documento, RUC con DV correcto (⚠️ la validación de RUC del **frontend está deshabilitada** y `mock-ruc.interceptor` sirve RUCs ficticios **incluso en prod**, SEC-4 — validá contra el backend, no confíes en el front).
- [ ] **Transporte / chofer (NRE):** ciudades salida/entrega, marca/matrícula del vehículo, datos del chofer.
- [ ] **Totales:** cálculo de IVA (0/5/10) y sumatorias — ver `SifenTotalsHelper` y migración `V24`.

## 4. Deuda vigente que puede causar rechazos/inconsistencias
- 🐛 **`iTipCont` del emisor hardcodeado `PERSONA_JURIDICA`** en `SifenService` (~L1351 factura, ~L1881 nota crédito, ~L2780 nota remisión). Si el emisor es **persona física**, el XML sale con `iTipCont` incorrecto → posible rechazo/inconsistencia fiscal (SIFEN-1). El fix de raíz es derivarlo de un campo de `Empresa`; mientras tanto, tenelo en cuenta al diagnosticar emisores PF.
- 🐛 **`SifenReceptorHelper` existe pero NO se usa** — la lógica del receptor está **duplicada a mano** en `SifenService` (~L1415/1931/2831). Si tocás el receptor, mirá los **3 lugares** o la corrección no se replica a notas (SIFEN-2).
- 🐛 **Datos del chofer se envían siempre** (~L2689), incluso en transporte propio — monitorear rechazos (SIFEN-4).

Detalle en [../reference/known-bugs.md](../reference/known-bugs.md) y `docs/ANALISIS_DIFERENCIAS_SIFEN_SERVICE.md`.

## 5. Reintentar tras el fix
1. Corregí los datos de la factura/nota (o el código de `SifenService` con cuidado).
2. Desvinculá el DE errado: `POST /facturas/{id}/desvincular-de` (solo permitido si el DE está en `ERROR`/`RECHAZADO`).
3. Regenerá y reenviá: `POST /facturas/{id}/generar-de` (o `POST /sifen/documentos/{deId}/reenviar` para reenviar el mismo DE en un lote nuevo). Ver [generar-y-enviar-de.md](generar-y-enviar-de.md).
4. Consultá estado hasta `APROBADO`.
