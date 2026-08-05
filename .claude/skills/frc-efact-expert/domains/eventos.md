# Dominio: Eventos SIFEN (Cancelación / Inutilización / Nominación)

Eventos que modifican el estado de un DE ya emitido o de un rango de numeración. Orquestador: `service/sifen/SifenEventoService.java`. Persistencia por servicio dedicado (`EventoCancelacionDEService`, `EventoInutilizacionDEService`, `EventoNominacionDEService`). Modelos: `model/Evento{Cancelacion,Inutilizacion,Nominacion}DE.java`. Migración V23. Endpoints en `SifenController` (`/sifen`).

## Campos comunes a los 3 eventos
`eventoId` (varchar 50, **unique**), `fechaFirma`, `xmlEvento` (TEXT), `estado` (`EstadoEvento`), `fechaProcesamiento`, `protocoloAutorizacion`, `codigoRespuesta`, `mensajeRespuesta`, `respuestaBruta`, `activo`. Al re-emitir, los eventos activos previos del mismo CDC se desactivan (`findActivosByCdcDocumento`).

### EstadoEvento
`PENDIENTE`, `APROBADO`, `RECHAZADO`, `ERROR_ENVIO`.

## EventoCancelacionDE
Cancela un DE ya aprobado, identificándolo por **CDC + motivo**. Campos propios: `documentoElectronico` (FK), `cdcDocumento` (varchar 44), `motivoCancelacion` (TEXT, obligatorio). Es un evento de tipo `dTiGDE=1` (cancelación) en la nomenclatura SIFEN. Método: `cancelarDE(cdc, motivo)` (~L90) → tras aprobación, el DE pasa a `EstadoDE.CANCELADO`.

## EventoInutilizacionDE  ⚠️ parcial (QA-1)
Inutiliza un **rango de numeración** no usado (huecos de correlativo). Referencia `timbrado` + `timbradoDetalle`, no un DE. Campos: `establecimiento`, `puntoExpedicion`, `numeroInicio`, `numeroFin`, `tipoDE`, `motivoInutilizacion` (obligatorio). Método: `inutilizarNumeros(...)` (~L245).

⚠️ **Implementación parcial** — solo queda desvinculada de `FacturaLegal` (no reserva/consume correlativos reales del `TimbradoDetalle`). Ver [../reference/known-bugs.md](../reference/known-bugs.md) (QA-1).

## EventoNominacionDE
Asigna un **receptor a un DE innominado** (factura emitida sin cliente identificado). Campos: `documentoElectronico` (FK), `cliente` (FK), `cdcDocumento`, `nombreReceptor`, `documentoReceptor`, `tipoReceptor`, `totalFactura`, `fechaEmision`, `fechaRecepcion`. Método: `nominarReceptor(cdc, cliente)` (~L379).

## Endpoints `/sifen`

| Método | Ruta | Roles | Evento |
|---|---|---|---|
| POST | `/sifen/documentos/{cdc}/cancelar` | A,EA,F | Cancelación (body `{motivo}`) |
| POST | `/sifen/documentos/{cdc}/nominar` | A,EA,F | Nominación (body `{clienteId}`) |
| POST | `/sifen/timbrados/{timbradoId}/inutilizar` | A,EA,F | Inutilización (body: establecimiento, puntoExpedicion, numeroInicio, numeroFin, tipoDE, motivo, timbradoDetalleId) |
| GET | `/sifen/eventos/cancelacion` · `/nominacion` · `/inutilizacion` | todos | Listados paginados |

## ⚠️ Sin validación de plazo (SIFEN-3)
`SifenEventoService` **no valida los plazos legales SIFEN** antes de enviar: cancelación 48h desde la emisión, inutilización 168h. Un evento fuera de plazo se enviará y será rechazado por SIFEN (no bloqueado localmente). Ver [../reference/known-bugs.md](../reference/known-bugs.md).
