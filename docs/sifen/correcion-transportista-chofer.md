# Nota de Remisión Electrónica (NRE) – Transporte propio y datos del conductor  
**SIFEN – Manual Técnico v150**

Este documento aclara **cuándo es obligatorio informar el transportista y cuándo NO es necesario informar los datos del conductor (chofer)** en una **Nota de Remisión Electrónica (C002 = 7)**.

Está pensado para **implementación técnica**, evitando rechazos comunes del SIFEN.

---

## 1. Conceptos clave (muy importante)

### Transportista ≠ Conductor

- **Transportista**:  
  Es el **responsable legal y fiscal del traslado** de la mercadería.  
  👉 **SIFEN valida a este sujeto** (RUC activo).

- **Conductor (chofer)**:  
  Es la persona que **opera el vehículo**.  
  👉 **NO es un sujeto fiscal** dentro de la NRE.

**Conclusión:**  
SIFEN **valida al transportista**, **NO al conductor**.

---

## 2. Caso: Transporte propio

Se considera **transporte propio** cuando:

```xml
<iTipTrans>1</iTipTrans>
<dDesTipTrans>Propio</dDesTipTrans>
En este escenario, el transportista es el propio emisor del documento.

3. Qué es obligatorio informar en transporte propio
Cuando iTipTrans = 1 (Propio):

Campos OBLIGATORIOS (mínimos)
El bloque gCamTrans debe identificar al emisor como transportista:

xml
Copiar código
<gCamTrans>
  <iNatTrans>1</iNatTrans>
  <dNomTrans>NOMBRE O RAZÓN SOCIAL DEL EMISOR</dNomTrans>
  <dRucTrans>RUC_DEL_EMISOR</dRucTrans>
  <dDVTrans>DV_DEL_EMISOR</dDVTrans>
</gCamTrans>
✔️ Esto cumple 100% con el Manual Técnico SIFEN v150.

4. Datos del conductor: ¿son obligatorios?
❌ NO son obligatorios
Los siguientes campos NO son exigidos por SIFEN en NRE, incluso en transporte propio:

dNumIDChof – Documento del conductor

dNomChof – Nombre del conductor

dDirChof – Dirección del conductor

Estos campos:

No son validados fiscalmente

No son requeridos para aprobación

Pueden generar confusión y rechazos si se usan incorrectamente

5. Recomendación técnica (muy importante)
Para transporte propio
👉 NO informar datos del conductor

Motivos:

Evita rechazos por RUC/Cédula inactiva

Simplifica el XML

Cumple estrictamente el MT v150

Reduce futuras validaciones más estrictas de la SET

6. Error común (ejemplo real)
❌ Error típico:

xml
Copiar código
<dRucTrans>4043581</dRucTrans> <!-- RUC del conductor -->
Resultado:

“El RUC del transportista se encuentra inactivo”

📌 Causa: se informó el RUC del conductor en lugar del RUC del transportista.

7. Regla de oro para implementar en código
text
Copiar código
if (iTipTrans == PROPIO) {
    transportista = emisor
    NO enviar datos de conductor
}
else {
    transportista = empresa transportista activa
    datos del conductor = opcionales
}
8. Buenas prácticas de implementación
Validar que dRucTrans:

esté activo en SET

corresponda al responsable del traslado

No asumir que conductor = transportista

Mantener los datos del chofer solo a nivel interno, no fiscal

9. Resumen final
✔️ En transporte propio:

Transportista = Emisor

RUC transportista = RUC del emisor

Datos del conductor = NO obligatorios

✔️ Eliminar datos innecesarios reduce rechazos y errores.