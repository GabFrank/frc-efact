# Workflow: Generar y enviar un DE a SIFEN

Flujo operativo desde una `FacturaLegal` ya creada hasta el DE **APROBADO** con KuDE PDF. Detalle del dominio en [../domains/documento-electronico-lote.md](../domains/documento-electronico-lote.md). Endpoints en [../reference/endpoints-index.md](../reference/endpoints-index.md).

> **Prerrequisitos:** empresa con certificado `.pfx` cargado + timbrado electrónico con CSC + punto de expedición + `FacturaLegal` numerada. Ver [../domains/empresas-multiempresa.md](../domains/empresas-multiempresa.md) y [../domains/timbrados.md](../domains/timbrados.md).

---

## 1. Generar el DE (y enviarlo) — `POST /facturas/{id}/generar-de`
**Roles:** `ADMIN`, `EMPRESA_ADMIN`, `FACTURADOR`.

⚠️ **Importante:** este endpoint hace **más que generar**. Internamente `DocumentoElectronicoService.generarYEnviarDesdeFactura(id)`:
1. Arma el XML SIFEN (vía `SifenService`), calcula el **CDC de 44 chars**, **firma** con el certificado de la empresa y genera el **QR**.
2. Crea el `DocumentoElectronico` (estado inicial `PENDIENTE`).
3. Lo **agrupa en un `LoteDE`** y lo **envía a SIFEN** en el mismo call.

La respuesta (`GenerarDeResponse`) trae el `DocumentoElectronicoDto` **y** el `LoteDeDto`. O sea: en el camino feliz **no necesitás llamar a `/sifen/lotes/.../enviar` a mano** — ya se envió. Los pasos 2–3 de abajo son para **reenvíos** o flujos manuales.

- Notas de crédito/débito/remisión tienen su propio `POST /{id}/generar-de` (`/notas-credito`, `/notas-debito`, `/notas-remision`). Remisión además expone `POST /notas-remision/{id}/generar-y-enviar`. Ver [../domains/notas-cdr.md](../domains/notas-cdr.md).

## 2. (Reenvío / manual) Agrupar en lote y enviar
Si el DE quedó desvinculado o querés reenviarlo en un lote nuevo:
- `POST /sifen/documentos/{deId}/reenviar` → crea un `LoteDE` nuevo con ese DE y lo envía. **Roles:** `ADMIN`, `EMPRESA_ADMIN`, `FACTURADOR`.
- O si ya tenés un lote armado: `POST /sifen/lotes/{loteId}/enviar` (`SifenController`, base `/sifen`). Mismos roles.
- Para poder reenviar desde la factura hay que desvincular primero el DE errado: `POST /facturas/{id}/desvincular-de` (solo si el DE está en `ERROR`/`RECHAZADO`).

## 3. Conocer el resultado del envío
SIFEN suele responder el lote de forma **asíncrona**: el lote queda `EN_PROCESO` hasta que SET lo procesa. Dos vías:

**a) Automática (scheduler)** — `SifenSchedulerService`:
- `consultarLotesEnProceso()` corre cada **~5 min** (`fixedDelay` `sifen.scheduler.consulta-lotes.delay:300000`) y consulta los lotes `EN_PROCESO`, actualizando cada DE a `APROBADO`/`RECHAZADO`.
- `consultarDocumentosPendientes()` (~10 min) y `procesarEventosPendientes()` (~15 min) cubren DEs sueltos y eventos.

**b) Manual (forzar consulta ya)** — **Roles:** `ADMIN`, `EMPRESA_ADMIN`, `FACTURADOR`:
- Por lote: `POST /sifen/lotes/{loteId}/consultar`.
- Por documento (id local): `POST /documentos-electronicos/{id}/consultar` → `consultarYActualizarEstado`.
- Por CDC: `POST /sifen/documentos/{cdc}/consultar`.

Estados del DE: `PENDIENTE` → `EN_PROCESO` → `APROBADO` | `RECHAZADO` | `ERROR`. El resultado queda en `DocumentoElectronico`: `estado`, `codigoRespuestaSifen`, `mensajeRespuestaSifen`, `respuestaSifen` (XML crudo).

## 4. Si sale RECHAZADO / ERROR
Leé `mensajeRespuestaSifen` / `codigoRespuestaSifen` y andá a [debug-sifen-errors.md](debug-sifen-errors.md). Errores típicos: `E605b`, `E644a`/`E1706`, y los de NRE (receptor, chofer, `cCiuSal`/`cCiuEnt`). Corregí la factura/datos, desvinculá el DE (`POST /facturas/{id}/desvincular-de`) y regenerá desde el paso 1.

## 5. APROBADO → KuDE PDF y email
Con el DE en `APROBADO`:
- **KuDE PDF:** `GET /facturas/{id}/kude-pdf` (JasperReports; notas: `/notas-credito/{id}/kude-pdf`, etc.).
- **Reenviar por email al cliente:** `POST /facturas/{id}/reenviar-email`. Requiere DE en estado `APROBADO`, cliente **nominado** (no "SIN NOMBRE") y con email configurado; si falta algo devuelve `400` con el motivo. El envío es async (`emailFacturaElectronicaService.enviarFacturaAlClienteAsync`).

---

## Resumen de endpoints y roles
| Paso | Endpoint | Roles |
|---|---|---|
| Generar + enviar DE | `POST /facturas/{id}/generar-de` | ADMIN, EMPRESA_ADMIN, FACTURADOR |
| Reenviar DE en lote nuevo | `POST /sifen/documentos/{deId}/reenviar` | ADMIN, EMPRESA_ADMIN, FACTURADOR |
| Enviar lote | `POST /sifen/lotes/{loteId}/enviar` | ADMIN, EMPRESA_ADMIN, FACTURADOR |
| Consultar lote | `POST /sifen/lotes/{loteId}/consultar` | ADMIN, EMPRESA_ADMIN, FACTURADOR |
| Consultar DE (id) | `POST /documentos-electronicos/{id}/consultar` | ADMIN, EMPRESA_ADMIN, FACTURADOR |
| Desvincular DE | `POST /facturas/{id}/desvincular-de` | ADMIN, EMPRESA_ADMIN, FACTURADOR |
| KuDE PDF | `GET /facturas/{id}/kude-pdf` | (ver controller) |
| Reenviar email | `POST /facturas/{id}/reenviar-email` | (ver controller) |

> El frontend cachea `currentUser` en NgRx: si un usuario recién vinculado a la empresa no ve estos botones, que **cierre sesión y vuelva a entrar** (bug RBAC conocido, [../reference/known-bugs.md](../reference/known-bugs.md)). El backend igual autoriza por `rolEmpresa` en cada request.
