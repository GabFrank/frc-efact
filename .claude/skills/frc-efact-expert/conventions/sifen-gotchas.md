# SIFEN — trampas que rompen la validación

Verificado 2026-08-05. El XML SIFEN es **muy estricto**: un campo de más o de menos rompe la validación con códigos `E605b`, `E644a`, `E962`, etc.

## 🥇 Regla de oro
**Antes de tocar `SifenService`** leer, en orden:
1. Este archivo
2. [../../../../docs/ANALISIS_DIFERENCIAS_SIFEN_SERVICE.md](../../../../docs/ANALISIS_DIFERENCIAS_SIFEN_SERVICE.md)
3. Los manuales de [../../../../docs/sifen/](../../../../docs/sifen/)

## Trampas con código de error

| Regla | Código | Detalle |
|---|---|---|
| **No enviar `dCuotas`** cuando `iCondCred = 1` (Plazo) | `E644a` / `1706` | Solo va en cuotas reales. Commit `e9db5de`. |
| **No enviar `gPaConEIni`** en factura a crédito **sin entrega inicial** | `E605b` / `1552` | El grupo de pago-condición-entrega-inicial no debe existir si no hubo pago inicial. Commit `de5542c`. |
| **`dMarVeh` (marca vehículo) ≤ 10 caracteres** (NRE) | `E962` | Usar abreviatura: `MERCEDES`, no `MERCEDES BENZ`. Ver `docs/sifen/analisis-errores-nre-sifen-v150.md:108`. |

## Reglas de moneda
- **`dTiCam` (y `dCondTiCam`, `dTotOpeGs`) SOLO si moneda ≠ `PYG`.** En guaraníes (`cMoneOpe = PYG`) esos campos **no se informan**. Fuente: `docs/sifen/implementacion_monedas_sifen_v150.md`.
- **Notas de crédito/débito heredan la moneda de la factura referenciada** — no elegir moneda propia. Commits `1680341`, `21f4a89`, `7a8e215`.

## Totales — los maneja la librería
- **`dTotalGs` / `dBasExe` (y demás totales) los calcula jsifenlib** — **no setearlos a mano** en `SifenService`. Sobrescribirlos genera XML inconsistente.

## Receptor en Notas
- En **NC/ND** (y NRE según notas técnicas recientes) el **receptor NO puede ser innominado** (`iTipIDRec ≠ INNOMINADO`): debe informar documento de identidad. Fuente: `docs/sifen/manual-notas-credito-debito-remision-sifen-jsifenlib.md`.

## ⚠️ Deuda técnica en SifenService (mirar antes de editar)
- **`iTipCont` del emisor hardcodeado a `PERSONA_JURIDICA`** (~L1351/1881/2780). Un emisor persona física genera XML incorrecto. (SIFEN-1)
- **`SifenReceptorHelper` existe pero NO se usa** — la lógica de receptor está **duplicada a mano** en 3 lugares de `SifenService` (~L1415/1931/2831). Si tocás el receptor, revisá los 3. (SIFEN-2)
- **Sin validación del plazo de cancelación** — no se valida la ventana temporal para cancelar un DE.
- **Datos del chofer se envían siempre** (~L2689), incluso en transporte propio. (SIFEN-4)

Registro completo: [../reference/known-bugs.md](../reference/known-bugs.md) y `docs/ISSUES_CANDIDATOS.md`.
