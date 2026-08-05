# Nota de Remisión Electrónica (NRE) – Transportista y datos del conductor
**SIFEN – Manual Técnico v150**

> ⚠️ **Documento revisado 2026-08-05 para reflejar el código real.**
> Este archivo contenía originalmente una *recomendación* (omitir los datos del
> conductor en transporte propio). El código de `SifenService` hace lo **contrario**:
> informa los datos del chofer siempre que estén cargados, sin importar el tipo de
> transporte. Este documento ahora describe primero **lo que el código hace hoy** y
> luego reencuadra la recomendación original como una **decisión de diseño abierta**.

---

## 1. Qué hace el código HOY

Construcción del bloque de transporte en
`frc-efact-backend/src/main/java/com/frcefact/service/sifen/SifenService.java`
(método de construcción del `TgTransp` de la NRE, aprox. **líneas 2626-2705**):

### 1.1. Transportista (`gCamTrans`) — sí depende del tipo de transporte

- **Transporte PROPIO** (`iTipTrans = PROPIO`, ~L2629-2658): el transportista se toma
  del **emisor** (`notaRemision.getEmpresa()`): razón social, RUC/DV y domicilio fiscal
  de la empresa. Naturaleza = CONTRIBUYENTE.
- **Transporte de TERCEROS** (~L2659-2687): se usan los campos manuales de la nota
  (`transportistaNombre`, `transportistaRuc`, `transportistaDireccion`).

Esto está alineado con la normativa: el **transportista** es el sujeto fiscal que SIFEN
valida (RUC activo), y en transporte propio ese sujeto es el propio emisor.

### 1.2. Datos del conductor (chofer) — se informan SIEMPRE que existan

Justo después del `if/else` del transportista, el código setea los datos del chofer
**de forma incondicional respecto del tipo de transporte** (~L2689-2701):

```java
// DATOS DEL CHOFER (OBLIGATORIOS SEGÚN SIFEN EN ALGUNOS ESCENARIOS)
// Se restauran los datos del chofer usando los campos conductor...
if (notaRemision.getConductorNombre() != null && !notaRemision.getConductorNombre().isBlank()) {
    gCamTrans.setdNomChof(notaRemision.getConductorNombre());
}
if (notaRemision.getConductorDoc() != null && !notaRemision.getConductorDoc().isBlank()) {
    gCamTrans.setdNumIDChof(notaRemision.getConductorDoc());
}
if (notaRemision.getConductorDireccion() != null && !notaRemision.getConductorDireccion().isBlank()) {
    gCamTrans.setdDirChof(notaRemision.getConductorDireccion());
}
```

- La única condición para informar cada campo es que **el dato esté cargado** en la nota.
- **No** hay una rama que omita el conductor cuando `iTipTrans = PROPIO`.
- El comentario `"Se restauran los datos del chofer"` indica que este bloque fue
  **reintroducido a propósito** después de una versión previa que los omitía.

**Comportamiento efectivo:** si la NRE trae `conductorNombre/Doc/Direccion`, el XML lleva
`dNomChof / dNumIDChof / dDirChof` incluso en transporte propio.

---

## 2. ⚠️ Divergencia a decidir (doc histórico vs. código actual)

> **Hay un conflicto no resuelto entre la recomendación normativa original y el código.**
> Ambas posturas se documentan aquí para que la decisión sea explícita.

| | Recomendación histórica (sección 6) | Código actual (`SifenService` ~L2689) |
|---|---|---|
| Conductor en transporte propio | **NO informar** | **Informar si está cargado** |
| Motivo | Evitar rechazos por RUC/cédula inactiva del conductor | Los campos de conductor pueden ser obligatorios en ciertos escenarios de traslado |
| Riesgo | Perder trazabilidad interna del chofer en el DE | Rechazo si el documento del conductor está inactivo / mal informado |

**Argumento a favor de omitir (histórico):** `dNumIDChof`, `dNomChof`, `dDirChof` no son
sujetos fiscales; informar el documento del conductor abre la puerta a rechazos del tipo
*"El RUC del transportista se encuentra inactivo"* cuando por error se cruza el
documento del conductor con el del transportista.

**Argumento a favor de informar (código actual):** la NRE identifica al conductor por su
documento de identidad, no por RUC, de modo que informar `dNumIDChof` con la **cédula**
del chofer no debería disparar la validación de transportista. Además, algunos escenarios
de traslado sí requieren el conductor, y mantenerlo da trazabilidad completa en el KuDE.

**Estado:** decisión **abierta**. Si SIFEN rechaza NRE por datos del conductor, la
mitigación mínima es condicionar el bloque de chofer (por ejemplo, no informarlo cuando
`iTipTrans = PROPIO`, replicando la lógica de la recomendación histórica). Mientras no se
observe ese rechazo, el código mantiene el comportamiento de informarlo siempre.

---

## 3. Recomendación histórica (NO aplicada) — se conserva como referencia normativa

> El contenido siguiente es la guía original. Refleja una lectura conservadora del
> MT v150 pensada para minimizar rechazos. **No coincide con el código actual**
> (ver secciones 1 y 2). Se mantiene por su valor normativo.

### 3.1. Transportista ≠ Conductor

- **Transportista:** responsable legal y fiscal del traslado. 👉 **SIFEN valida a este
  sujeto** (RUC activo).
- **Conductor (chofer):** persona que opera el vehículo. 👉 **NO es un sujeto fiscal**
  dentro de la NRE.

**Conclusión normativa:** SIFEN **valida al transportista**, **NO al conductor**.

### 3.2. Transporte propio

Se considera **transporte propio** cuando:

```xml
<iTipTrans>1</iTipTrans>
<dDesTipTrans>Propio</dDesTipTrans>
```

En este escenario el transportista es el propio emisor del documento.

### 3.3. Campos obligatorios en transporte propio

El bloque `gCamTrans` debe identificar al emisor como transportista:

```xml
<gCamTrans>
  <iNatTrans>1</iNatTrans>
  <dNomTrans>NOMBRE O RAZÓN SOCIAL DEL EMISOR</dNomTrans>
  <dRucTrans>RUC_DEL_EMISOR</dRucTrans>
  <dDVTrans>DV_DEL_EMISOR</dDVTrans>
</gCamTrans>
```

✔️ Esto cumple con el Manual Técnico SIFEN v150 (y coincide con el código actual, §1.1).

### 3.4. Datos del conductor: ¿son obligatorios?

Según esta recomendación histórica, **NO** son exigidos por SIFEN en NRE, ni siquiera en
transporte propio:

- `dNumIDChof` – Documento del conductor
- `dNomChof` – Nombre del conductor
- `dDirChof` – Dirección del conductor

Argumento: no son validados fiscalmente ni requeridos para aprobación, y pueden generar
confusión y rechazos si se usan incorrectamente.

### 3.5. Recomendación (histórica, no aplicada)

Para transporte propio 👉 **NO informar datos del conductor**, para:

- Evitar rechazos por RUC/cédula inactiva
- Simplificar el XML
- Cumplir estrictamente el MT v150
- Reducir exposición a validaciones más estrictas de la SET

### 3.6. Error común (ejemplo real)

❌ Error típico:

```xml
<dRucTrans>4043581</dRucTrans> <!-- RUC del conductor -->
```

Resultado: *"El RUC del transportista se encuentra inactivo"*.

📌 Causa: se informó el RUC del **conductor** en lugar del RUC del **transportista**.
(Nótese que en el código actual el conductor se informa vía `dNumIDChof`, no vía
`dRucTrans`, por lo que este error específico no aplica al flujo actual — pero la
confusión conductor/transportista sigue siendo el riesgo a vigilar.)

### 3.7. Regla de oro propuesta (histórica)

```text
if (iTipTrans == PROPIO) {
    transportista = emisor
    NO enviar datos de conductor
} else {
    transportista = empresa transportista activa
    datos del conductor = opcionales
}
```

> El código actual implementa la primera línea (`transportista = emisor`) pero **no** la
> segunda (`NO enviar datos de conductor`): informa el conductor en ambos casos.

### 3.8. Buenas prácticas normativas

- Validar que `dRucTrans` esté activo en SET y corresponda al responsable del traslado.
- No asumir que conductor = transportista.
- Tratar los datos del chofer como información de traslado (documento de identidad),
  nunca como RUC de transportista.
