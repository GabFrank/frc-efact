# Estado real de `SifenService` — Diferencias resueltas y deuda técnica vigente

> ⚠️ **Documento reescrito 2026-08-05.** El análisis especulativo original
> (2025-01-27), que comparaba `SifenService` contra el repo de referencia
> `franco-system-backend-filial`, **quedó obsoleto**: la mayoría de las 9 "diferencias"
> que planteaba ya se resolvieron en el código, nunca aplicaron, o fueron conjeturas
> sobre tipos de datos que hoy están zanjadas. Este documento reemplaza aquel análisis
> por el **estado verificado del código actual** (`frc-efact-backend`), con líneas
> aproximadas de `SifenService.java`.

---

## 1. Cómo leer este documento

- **Sección 2** — diferencias del análisis viejo que ya están **cerradas** (no requieren
  acción).
- **Sección 3** — **deuda técnica real y vigente** (candidatos a issue).
- Las líneas citadas corresponden a
  `frc-efact-backend/src/main/java/com/frcefact/service/sifen/SifenService.java`
  a fecha de esta revisión; pueden desplazarse con futuras ediciones.

---

## 2. Diferencias del análisis 2025-01-27 que YA están cerradas

### 2.1. `dEst` — Código de establecimiento (Diff #2 del doc viejo) — ✅ FALSO / resuelto

El análisis viejo afirmaba que el proyecto tomaba `dEst` de un lugar distinto al repo de
referencia (`getSucursal().getCodigoEstablecimientoFactura()`). **Es incorrecto.** El
código toma el código directamente de `TimbradoDetalle` y lo formatea a 3 dígitos:

```java
// SifenService ~L1242-1243 (factura)
String codEstablecimiento = factura.getTimbradoDetalle().getCodigoEstablecimientoFactura().trim();
gTimb.setdEst(String.format("%03d", Integer.parseInt(codEstablecimiento)));
```

El mismo patrón se repite para Nota de Crédito (~L1724-1726) y Nota de Remisión
(~L2167-2169). No hay dependencia de `getSucursal()`. **Sin acción.**

### 2.2. `dBasExe` en ítems gravados (Diff #4) — ✅ resuelto

El código **ya no** setea `dBasExe` explícitamente en los tramos gravados de IVA. Se deja
que la librería lo maneje, con comentario explícito:

```java
// SifenService ~L1659 y ~L1673
// dBasExe no se setea - la librería lo maneja automáticamente (como en versión anterior)
```

Coincide con el repo de referencia. **Sin acción.**

### 2.3. Tipos numéricos `Double` vs `BigDecimal` (Diff #3, #5, #6) — ✅ zanjado

Las conjeturas del doc viejo sobre `getCantidad()`, `getPrecioUnitario()` y
`getTotalFinal()` retornando `Float`/`Double` ya no aplican: el modelo trabaja con
`BigDecimal` de punta a punta y las normalizaciones (`setScale(...)`) operan sobre
`BigDecimal`. No hay riesgo de `ClassCastException` ni de conversión imprecisa por esos
métodos. **Sin acción.**

### 2.4. Fuente de datos del emisor y geografía (Diff #7 parcial, #8) — ✅ intencional

Que el proyecto tome datos del emisor desde `factura.getEmpresa()` y la geografía desde
relaciones JPA (`Ciudad → Distrito → Departamento`) es una **decisión de arquitectura
multi-empresa**, no un bug. La diferencia con el repo de referencia (una sola empresa,
todo en `Timbrado`) es esperada. **Sin acción** — salvo el sub-punto de `iTipCont`
hardcodeado, que sí es deuda real (ver §3.1).

> **Nota:** el único fragmento del Diff #7 que sigue vigente es el **tipo de
> contribuyente del emisor hardcodeado** — se trata aparte en §3.1.

---

## 3. Deuda técnica REAL y vigente (candidatos a issue)

### 3.1. 🐛 `iTipCont` del emisor hardcodeado como `PERSONA_JURIDICA` (3 puntos)

El tipo de contribuyente del **emisor** se fija incondicionalmente a persona jurídica en
los tres flujos de documento:

```java
gEmis.setiTipCont(TiTipCont.PERSONA_JURIDICA);
```

- **Factura:** `SifenService` **~L1351**
- **Nota de Crédito:** `SifenService` **~L1881**
- **Nota de Remisión:** `SifenService` **~L2780**

**Impacto:** si el emisor (la empresa) fuese **persona física**, el DE saldría con
`iTipCont` incorrecto, lo que puede provocar rechazo o inconsistencia fiscal en SIFEN. El
repo de referencia lo hacía configurable (`tipoContribuyenteEmisor == 1 ? PERSONA_FISICA :
PERSONA_JURIDICA`).

**Fix sugerido:** derivar `iTipCont` del emisor desde un campo de `Empresa`
(p. ej. tipo de contribuyente / naturaleza del RUC) en lugar de la constante. Aplicar el
mismo cambio en los 3 puntos.

**Severidad:** media-alta (sólo afecta emisores persona física; hoy el parque de empresas
puede ser todo PJ, pero es una bomba latente para el primer emisor PF).

---

### 3.2. 🐛 `SifenReceptorHelper` existe pero `SifenService` NO lo usa (lógica duplicada)

El helper `com.frcefact.sifen.util.SifenReceptorHelper` expone
`getNaturalezaReceptor`, `getTipoContribuyente`, `getTipoOperacion` y `requiereRuc` a
partir de `Cliente.getTipoClienteSifen()`. **`SifenService` no lo importa ni lo invoca.**
En su lugar, la lógica de armado del receptor está **escrita a mano y duplicada** en tres
métodos:

- `construirDatosReceptor(FacturaLegal)` — **~L1415**
- `construirDatosReceptorNotaCredito(NotaCredito)` — **~L1931**
- `construirDatosReceptorNotaRemision(NotaRemision)` — **~L2831**

Los tres resuelven la naturaleza/tipo de operación por su cuenta (p. ej. usan
`cliente.requiereRuc()` directo, ramas manuales para innominado / contribuyente / no
contribuyente), en vez de centralizar en el helper.

**Impacto:** riesgo de **divergencia** entre los tres flujos (una corrección en el
receptor de facturas puede no replicarse en notas), y un helper muerto que aparenta ser la
fuente de verdad pero no lo es.

**Fix sugerido:** refactorizar los tres métodos para delegar en `SifenReceptorHelper`
(unificando la determinación de `iNatRec` / `iTiOpe` / `iTiContRec` / `requiereRuc`), o —
si se decide no usarlo — eliminar el helper para no confundir. Preferible lo primero.

**Severidad:** media (mantenibilidad; no rompe SIFEN hoy, pero facilita bugs futuros).

---

### 3.3. ⚠️ Sin validación de plazo de cancelación (48 h / 168 h)

El Manual de Implementación NRE/cancelación v150 exige respetar la ventana temporal para
el **evento de cancelación** (plazos del orden de 48 h para algunos DE y 168 h para NRE,
según el tipo de documento). El flujo de cancelación en
`SifenEventoService.cancelarDocumento(...)` **no valida ningún plazo** antes de construir y
enviar el evento: verifica que no exista cancelación aprobada previa e invalida eventos
activos anteriores, pero no compara la fecha de emisión/firma del DE contra el límite
normativo.

**Impacto:** cancelaciones fuera de plazo se arman y se envían a SIFEN, que las rechaza; el
rechazo se descubre recién en la respuesta en lugar de bloquearse localmente con un mensaje
claro.

**Fix sugerido:** agregar una validación previa en `cancelarDocumento` que compare la fecha
de emisión/firma del DE contra la ventana permitida según el tipo de documento y falle
temprano (`BusinessException`) con un mensaje explícito antes de enviar.

**Severidad:** baja-media (no corrompe datos; degrada UX y gasta un round-trip a SIFEN).

---

## 4. Resumen de candidatos a issue

| # | Título | Ubicación (`SifenService`/`SifenEventoService`) | Severidad |
|---|--------|--------------------------------------------------|-----------|
| 3.1 | `iTipCont` emisor hardcodeado `PERSONA_JURIDICA` | `SifenService` ~L1351, ~L1881, ~L2780 | Media-alta |
| 3.2 | `SifenReceptorHelper` sin usar; receptor duplicado a mano | `SifenService` ~L1415, ~L1931, ~L2831 | Media |
| 3.3 | Falta validación de plazo de cancelación (48 h / 168 h) | `SifenEventoService.cancelarDocumento` | Baja-media |

> Conflicto adicional relacionado (documentado aparte):
> [`docs/sifen/correcion-transportista-chofer.md`](sifen/correcion-transportista-chofer.md)
> — el código informa datos del conductor siempre (`SifenService` ~L2689-2701), en contra
> de la recomendación histórica de omitirlos en transporte propio. Decisión abierta.

---

## 5. Anexo — historial

- **2025-01-27** — Análisis original (especulativo) comparando `construirDEDesdeFactura`
  contra `generarDEDesdeFacturaDatosReales` del repo `franco-system-backend-filial`
  (branch 3.0.7-3). Planteaba 9 diferencias con conjeturas sobre tipos de datos y fuentes
  de campos. La mayoría resultó ya resuelta o inaplicable (ver §2).
- **2026-08-05** — Reescritura sobre el estado verificado del código. Se conservan sólo las
  3 deudas técnicas reales (§3) y se cierran las diferencias falsas/resueltas (§2).
