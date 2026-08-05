# Integración SIFEN (jsifenlib)

Toda la interacción con SIFEN está aislada en `service/sifen/` (lógica) + `sifen/config/` y `sifen/util/` (config y helpers). El XML SIFEN es **estrictísimo** — antes de tocar nada, leer [conventions/sifen-gotchas.md](../conventions/sifen-gotchas.md) y los manuales de `docs/sifen/`. Verificado 2026-08-05.

## La librería: jsifenlib

Fork interno **`io.github.gabfrank:rshk-jsifenlib:0.2.4-frc.13`** (paquete Java `com.roshka.sifen`), publicado en **GitHub Packages** (`GabFrank/rshk-jsifenlib`). El build requiere `GITHUB_USERNAME`/`GITHUB_TOKEN` (ver `GITHUB_PACKAGES_SETUP.md`). Provee `Sifen.*` (envío/consulta), `SifenConfig`, y el modelo de objetos DE. Requiere el stack SOAP (`saaj-impl`, `javax.xml.soap`).

## Config dinámica por empresa: `SifenConfigFactory` + `SifenProperties`

Multi-tenancy: **cada empresa tiene su propio certificado `.pfx` y CSC**, pero jsifenlib usa un **contexto global compartido** (`Sifen.setSifenConfig`).

`sifen/config/SifenConfigFactory` resuelve esto:
- `buildForEmpresa(empresaId)` / `buildForTimbrado(timbradoId)` construyen un `SifenConfig` con: ambiente (`DEV`/`PROD`, de `empresa.sifenAmbiente`), `cscId` + CSC desencriptado (prioriza el del timbrado, cae al de la empresa), path absoluto del certificado (`CertificadoService`) y password desencriptado. CSC y password se descifran con `EncryptionService` (AES-256).
- **`executeWithConfig(config, operation)`**: envuelve toda llamada a jsifenlib en un bloque **`synchronized (sifenLock)`** — setea el contexto global, ejecuta, y libera para el siguiente hilo. **Toda** operación jsifenlib debe pasar por acá para no cruzar certificados entre empresas.

`SifenProperties` expone flags configurables (p.ej. `habilitarNotaTecnica13`).

## Servicios (`service/sifen/`)

- **`SifenService`** (3457 líneas / 176 KB) — el núcleo: arma el XML SIFEN del DE (emisor, receptor, ítems, totales, condición de venta, moneda), calcula el CDC (44 chars), firma, genera QR, y ejecuta envío/consulta vía `SifenConfigFactory.executeWithConfig`. ⚠️ Trampas verificadas: `iTipCont` del emisor **hardcodeado `PERSONA_JURIDICA`** (SIFEN-1); lógica de receptor **duplicada a mano** (no usa `SifenReceptorHelper`, SIFEN-2); datos del chofer se envían siempre (SIFEN-4). Ver [conventions/sifen-gotchas.md](../conventions/sifen-gotchas.md).
- **`SifenEventoService`** (844 líneas) — eventos: **cancelación**, **inutilización** (⚠️ parcial, QA-1), **nominación**.
- **`SifenSchedulerService`** (101 líneas) — `@Scheduled` de polling:
  - `consultarLotesEnProceso` (cada ~5 min, `findLotesEnProceso`).
  - `consultarDocumentosPendientes` (cada ~10 min, `findPendientesConCdc`).
  - `procesarEventosPendientes` (cada ~15 min) — hoy **placeholder** (solo loguea; el envío a SIFEN no está implementado).
  Delays configurables por `sifen.scheduler.*.delay`.

## Helpers (`sifen/util/`)

| Helper | Uso |
|---|---|
| `SifenTotalsHelper` | Cálculo de totales / IVA del DE |
| `SifenResponseParser` | Parseo de respuestas SIFEN (códigos, estado) |
| `SifenDocumentoLogger` | Logging estructurado del DE enviado |
| `SifenReceptorHelper` | ⚠️ **existe pero NO se usa** — el receptor está duplicado a mano en `SifenService` (~L1415/1931/2831). Si tocás el receptor, mirá los 3 lugares. |

## Flujo resumido

FacturaLegal → `SifenService` arma DE (XML + CDC + firma + QR) → agrupar en `LoteDE` → `executeWithConfig` envía a SIFEN → `SifenSchedulerService` hace polling → estado `APROBADO`/`RECHAZADO` → KuDE PDF (JasperReports) / eventos.

Profundizar: [domains/sifen-core.md](../domains/sifen-core.md) (armado del XML, receptor, totales, monedas, gotchas E605b/E644a) y [conventions/sifen-gotchas.md](../conventions/sifen-gotchas.md).
