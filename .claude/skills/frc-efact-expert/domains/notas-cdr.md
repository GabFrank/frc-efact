# Dominio: Notas (Crédito / Débito / Remisión)

Documentos electrónicos derivados de una `FacturaLegal` referenciada. Modelos: `model/NotaCredito.java` (+`NotaCreditoItem`), `model/NotaDebito.java` (+`NotaDebitoItem`), `model/NotaRemision.java` (+`NotaRemisionItem`). Controllers: `/notas-credito`, `/notas-debito`, `/notas-remision`. XML: `SifenService` (rutas de construcción propias — ver [sifen-core.md](sifen-core.md)).

## Común a las tres

- Referencian `facturaLegal` (`@ManyToOne`) + `empresa`, `timbradoDetalle` (EAGER), `cliente`.
- **Numeración propia** por punto de expedición: `numeroNotaCredito` / `numeroNotaDebito` / `numeroNotaRemision` (autonumera vía `TimbradoDetalle`, igual que factura).
- **Heredan moneda de la factura referenciada**: `monedaExtranjera` + `cambio` (mismos campos que `FacturaLegal`; totales en guaraníes). Los ítems también se heredan de la factura.
- `@OneToOne(mappedBy=...)` a `DocumentoElectronico`; `activo` (baja lógica).

## NotaCredito / NotaDebito

Estructura casi idéntica a `FacturaLegal` (mismos parciales por IVA: `ivaParcial0/5/10`, `totalParcial0/5/10`, `descuentoFinal`, `totalParcial`, `totalFinal`). Además:
- `motivoEmision` (varchar 50) + `descripcionMotivo` (varchar 255). Los **motivos deben ser válidos según el catálogo SIFEN** (devolución, descuento, bonificación, etc.) — no texto libre en el código de motivo.
- Nota de crédito: hereda moneda/ítems de la factura; fecha de firma correcta; numeración independiente (lecciones de commits `1680341`, `21f4a89`, `7a8e215`).

### Endpoints
- `/notas-credito`: `POST` [A,EA,F] · `GET` · `GET /{id}` [todos] · `DELETE /{id}` [A,EA] · `POST /{id}/generar-de` [A,EA,F] · `GET /{id}/kude-pdf` [todos]
- `/notas-debito`: `POST` [A,EA,F] · `GET` · `GET /{id}` [todos] · `DELETE /{id}` [A,EA] (sin generar-de/kude en el controller — ver [../reference/endpoints-index.md](../reference/endpoints-index.md))

## NotaRemision

Documento de traslado de mercadería. No lleva totales de IVA como las otras; su carga está en la **logística de transporte**. Campos SIFEN clave:

- **Geografía origen (partida)** — V31: `direccionPartida`, `ciudadPartida`(+`ciudadPartidaId`), `departamentoPartida`(+`departamentoPartidaId`), `distritoPartidaId`.
- **Geografía destino (destinatario)** — V32: `nombreDestinatario`, `rucDestinatario`, `direccionDestinatario`, `ciudadDestinatario`(+id), `departamentoDestinatario`(+id), `distritoDestinatarioId`.
- **Traslado**: `motivoEmision`, `fechaInicioTraslado`/`fechaFinTraslado` (`LocalDate`), `kmEstimado`, `tipoTransporte`, `modalidadTransporte`.
- **Transportista** — V33: `vehiculo` (FK) o manual (`vehiculoMarca`, `vehiculoMatricula`); `transportistaNombre/Ruc/Direccion`; `chofer` (FK) o manual (`conductorNombre`, `conductorDoc`, `conductorDireccion`).
- **Fecha estimada** — V34: `fechaEstimadaFactura` (`LocalDate`).

### ⚠️ Divergencia chofer (SIFEN-4)
En `SifenService` (~L2689) los **datos del chofer (`gCamTrans.setdNomChof/dNumIDChof/dDirChof`) se envían siempre**, incluso en transporte propio, donde SIFEN podría no requerirlos. Decisión abierta — monitorear rechazos. Ver [../reference/known-bugs.md](../reference/known-bugs.md) y [sifen-core.md](sifen-core.md).

### Endpoints `/notas-remision`
`POST` [A,EA,F] · `GET` · `GET /{id}` [todos] · `DELETE /{id}` [A,EA] · `POST /{id}/generar-de` [A,EA,F] · **`POST /{id}/generar-y-enviar`** [A,EA,F] (genera DE **y** envía en un paso) · **`POST /{id}/vincular-lote`** [A,EA,F] (asocia a un `LoteDE` existente) · `GET /{id}/kude-pdf` [todos] · `POST /{id}/enviar-email` [A,EA,F].

> `generar-y-enviar` y `vincular-lote` son propios de remisión; para factura/NC el envío es siempre vía `SifenController` (`/sifen/lotes/{id}/enviar`). Ver [documento-electronico-lote.md](documento-electronico-lote.md).
