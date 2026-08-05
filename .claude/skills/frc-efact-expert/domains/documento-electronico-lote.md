# Dominio: DocumentoElectronico + LoteDE

El DE es la representación firmable/enviable a SIFEN de una factura o nota. Se agrupa en lotes para el envío. Modelos: `model/DocumentoElectronico.java`, `model/LoteDE.java`. Generación: `service/sifen/SifenService.java` (arma el XML — ver [sifen-core.md](sifen-core.md)), `service/XmlGeneratorService.java`, `service/DocumentoElectronicoService.java`. Controllers: `DocumentoElectronicoController` (`/documentos-electronicos`), `SifenController` (`/sifen`).

## DocumentoElectronico

Un DE referencia exactamente **una** fuente vía `@OneToOne`: `facturaLegal`, `notaCredito`, `notaDebito` **o** `notaRemision`. Campos clave:

- `cdc` (varchar 44, **unique**) — Código de Control, 44 caracteres. Generado por jsifenlib al armar el XML.
- `tipoDocumento` (varchar 10, default `"1"` = Factura).
- `xmlOriginal` / `xmlFirmado` (TEXT) — XML SIFEN y su versión con firma digital XAdES.
- `urlQr` (TEXT) — extraída del XML por `SifenResponseParser.extractUrlQr()`.
- `estado` (`EstadoDE`, `@Enumerated(STRING)`, default `PENDIENTE`).
- `codigoRespuestaSifen` / `mensajeRespuestaSifen` / `respuestaSifen` / `protocoloAutorizacion`.
- `fechaEmision`, `fechaRecepcionSifen`, `fechaEstadoActualizado`, `intentos`, `activo`.
- `loteDE` (`@ManyToOne`) — lote en el que fue enviado.

### EstadoDE
`PENDIENTE` → `EN_PROCESO` → `APROBADO` / `RECHAZADO` / `CANCELADO` / `ERROR`
- `PENDIENTE`: DE generado, aún no enviado.
- `EN_PROCESO`: enviado en lote, esperando respuesta de SIFEN.
- `APROBADO`: aceptado (tiene `protocoloAutorizacion`).
- `RECHAZADO`: SIFEN rechazó (ver `codigoRespuestaSifen`, ej. `E605b`, `E644a`).
- `CANCELADO`: cancelado por evento (ver [eventos.md](eventos.md)).
- `ERROR`: fallo técnico de envío/generación.

## LoteDE

Agrupa varios DE de una `empresa` para envío en bloque a SIFEN. Campos: `empresa`, `estado` (`EstadoLoteDE`), `protocolo`, `respuestaSifen`, `codigoRespuesta`, `mensajeRespuesta`, `fechaProcesado`, `fechaUltimoIntento`, `intentos`, `documentos` (`@OneToMany` a DE).

### EstadoLoteDE
`PENDIENTE`, `EN_PROCESO`, `APROBADO`, `RECHAZADO`, `ERROR`, `PROCESADO`, `ERROR_ENVIO`, `ERROR_PERMANENTE`
(más estados que `EstadoDE`: distingue error transitorio `ERROR_ENVIO` de `ERROR_PERMANENTE`, y `PROCESADO` = lote resuelto).

## Flujo completo

```
FacturaLegal ──POST /facturas/{id}/generar-de──▶ DocumentoElectronico (PENDIENTE, con CDC+XML firmado+QR)
      │
      ├─ (agrupar DE en un LoteDE)
      ▼
POST /sifen/lotes/{loteId}/enviar ──▶ SIFEN recibe lote ──▶ DE pasa a EN_PROCESO
      │
      ▼
Scheduler (SifenSchedulerService) hace polling:
   consultarLotesEnProceso()      @Scheduled delay 300000ms  → sifenService.consultarLote()
   consultarDocumentosPendientes() @Scheduled delay 600000ms  → sifenService.consultarDocumento(cdc)
      │
      ▼
DE ──▶ APROBADO (con protocolo) / RECHAZADO (con código de error)
```

El **envío nunca ocurre en `generar-de`**: `generar-de` solo produce el DE en `PENDIENTE`. El polling automático evita tener que consultar manualmente, pero también existen endpoints de consulta on-demand.

## Endpoints

### `/documentos-electronicos` (todos salvo indicado)
`GET` (lista) · `GET /{id}` · `GET /cdc/{cdc}` · `GET /{id}/xml` · `POST /{id}/consultar` [A,EA,F]

### `/sifen` — envío y consulta de lotes/DE [A,EA,F]
| Método | Ruta | Acción |
|---|---|---|
| POST | `/sifen/lotes/{loteId}/enviar` | Envía el lote a SIFEN |
| POST | `/sifen/lotes/{loteId}/consultar` | Consulta estado del lote |
| POST | `/sifen/documentos/{cdc}/consultar` | Consulta estado del DE por CDC |
| GET | `/sifen/documentos/factura/{facturaId}` | DE de una factura |
| GET | `/sifen/documentos/nota-credito/{id}` · `/nota-remision/{id}` | DE de una nota |
| POST | `/sifen/documentos/{deId}/reenviar` | Reenvía el DE en un **nuevo** lote |

Eventos (cancelar/nominar/inutilizar) también cuelgan de `/sifen` → ver [eventos.md](eventos.md).

## KuDE PDF

Representación gráfica imprimible del DE (JasperReports, plantillas en `resources/reports/`). Se descarga por la entidad de origen: `GET /facturas/{id}/kude-pdf`, `GET /notas-credito/{id}/kude-pdf`, `GET /notas-remision/{id}/kude-pdf` (todos: rol `todos`). Incluye el QR con `urlQr`.
