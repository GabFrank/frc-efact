# Manual de implementación — Nota de Remisión Electrónica (NRE) + Evento de Cancelación (SIFEN v150)

> Este manual asume que **ya tenés implementado**: (1) construcción y firma del **DE**, (2) **envío en lote** (siRecepLoteDE) y (3) **consulta** por **lote** y/o por **CDC** (siResultLoteDE / siConsDE).  
> En NRE, el cambio principal es **qué campos** se informan (y cuáles **no**), más un **flujo operativo** para transporte y la **cancelación** (siRecepEvento).

---

## 1) Qué es una NRE en SIFEN

La **Nota de Remisión Electrónica** es un **Documento Electrónico (DE)** cuyo tipo se identifica con:

- `C002 = 7`  → “Nota de remisión electrónica”  citeturn5view1

Para este tipo, el manual define un grupo específico:

- **E500 = gCamNRE** “Campos que componen la Nota de Remisión Electrónica”  
  - **Obligatorio si `C002=7`** y **no debe informarse** si `C002≠7`. citeturn6view1

---

## 2) Flujo funcional completo (end-to-end)

### 2.1 Flujo “feliz” (emisión y aprobación)

1) **Crear el DE** de tipo NRE (`C002=7`)
2) **Completar grupos obligatorios NRE**
   - `E500 (gCamNRE)` con motivo/responsable/etc. citeturn7view0turn9view0
   - Grupos de **Transporte** obligatorios (sección 4) citeturn5view1
3) **Aplicar reglas de no-información** (precios/totales/IVA por tipo NRE)
4) **Firmar el XML** (igual que tus otros DE)
5) **Agregar al lote** (igual que tus otros DE)
6) **Enviar lote** (igual que tus otros DE)
7) **Consultar resultado del lote** hasta recibir aprobación/rechazo (igual que tus otros DE)
8) **Persistir**:
   - XML enviado
   - respuesta de SIFEN / estado
   - CDC y metadatos (para trazabilidad y eventos posteriores)

### 2.2 Flujo con rechazo (corrección)

- Si el lote retorna **rechazo**, corregís el DE, regenerás firma y reenviás.
- Si se “saltó” numeración por error de sistema, aplica **Inutilización** (evento distinto).

---

## 3) Requisitos específicos de contenido para una NRE (C002=7)

### 3.1 Grupo E500 (gCamNRE) — obligatorio

E500 contiene los campos E501…E506: citeturn7view0turn8view2turn9view0

- `E501 (iMotEmiNR)` Motivo de emisión (1..14, 99=Otro).  
  - Observación importante: si es operación interna (ej. traslado entre locales), **RUC receptor = RUC emisor**. citeturn8view0turn6view1
- `E502 (dDesMotEmiNR)` Descripción del motivo (referente a E501). citeturn8view2
- `E503 (iRespEmiNR)` Responsable de la emisión (emisor / poseedor / transportista / despachante / agente). citeturn8view2
- `E504 (dDesRespEmiNR)` Descripción del responsable (referente a E503). citeturn8view2
- `E505 (dKmR)` Kilómetros estimados (opcional). citeturn9view0
- `E506 (dFecEm)` Fecha futura estimada de emisión de la factura (opcional con reglas). citeturn9view0turn6view1

#### Regla práctica clave (venta sin factura asociada)
Si el motivo es **Traslado por venta** (`E501=1`) **y no informás documentos asociados**, entonces `E506` pasa a ser **obligatorio** (para estimar la emisión de la FE). citeturn6view1

### 3.2 Reglas “NO informar” (para evitar rechazos típicos)

Para NRE (`C002=7`) el manual indica que **no deben informarse**:

- El grupo de **precios/descuentos/valor por ítem** `E720` **no debe informarse**. citeturn5view1
- El grupo de **IVA de la operación** `E730` **no debe informarse**. citeturn5view1
- El grupo de **subtotales y totales de la transacción** `F001` **no es permitido** en NRE. citeturn5view1

> Consecuencia de diseño: en NRE tus ítems describen **mercadería/servicio y cantidades**, pero **no valores** (precio, IVA, totales).

### 3.3 Documentos asociados (muy común en NRE)

El MT contempla asociaciones entre documentos. Por ejemplo:
- Si el DE es **Factura electrónica** (`C002=1`), puede asociar una **Nota de Remisión**.  
- Si el DE es **Nota de remisión** (`C002=7`), cuando se informan **documentos asociados**, estos se validan según el tipo (ej. referenciar FE). citeturn5view1

**Recomendación de implementación**:
- En NRE, soportá “referencia a FE existente” (cuando ya existe) y “venta futura” (cuando aún no existe FE) usando `E506` según regla anterior.

---

## 4) Transporte en NRE — qué grupos son obligatorios

El MT trae validaciones explícitas: para NRE (`C002=7`) es obligatorio informar:

- `E901` **Tipo de transporte** (obligatorio para NRE). citeturn5view1
- `E920` **Local de salida de mercaderías** (obligatorio para NRE). citeturn5view1
- `E940` **Local de entrega de mercaderías** (obligatorio para NRE). citeturn5view1
- `E960` **Vehículo de traslado** (obligatorio para NRE). citeturn5view1
- `E980` **Transportista (persona física o jurídica)** (obligatorio para NRE). citeturn5view1

> En tu modelo de datos, tratá Transporte como un sub-módulo con “plantillas” y reutilización (transportistas frecuentes, vehículos frecuentes, locales frecuentes).

---

## 5) Checklist mínimo de campos para emitir una NRE (C002=7)

### 5.1 Encabezado y grupos base (igual que tu DE actual)
- Grupos A/B/C/D… necesarios para cualquier DE (emisor, receptor, timbrado/serie, etc.)

### 5.2 Específico NRE
- ✅ `E500` con `E501..E506` citeturn7view0turn9view0
- ✅ Transporte: `E901`, `E920`, `E940`, `E960`, `E980` citeturn5view1
- ✅ Ítems:
  - descripción y cantidades
  - ❌ sin precios/descuentos/IVA/totales (ver 3.2) citeturn5view1

---

## 6) Ejemplo: esqueleto XML de una NRE (extracto orientativo)

> **Nota:** este esqueleto es “conceptual” (para ubicar grupos). Vos ya tenés la construcción real de tu DE.

```xml
<rDE>
  <DE Id="...">
    <gTimb>... C002=7 ...</gTimb>

    <gDatGralOpe>
      <!-- emisor, receptor, etc. -->
    </gDatGralOpe>

    <!-- E500: Nota de Remisión -->
    <gCamNRE>
      <iMotEmiNR>1</iMotEmiNR>
      <dDesMotEmiNR>Traslado por ventas</dDesMotEmiNR>
      <iRespEmiNR>1</iRespEmiNR>
      <dDesRespEmiNR>Emisor de la factura</dDesRespEmiNR>
      <dKmR>120</dKmR>
      <dFecEm>2026-01-31</dFecEm>
    </gCamNRE>

    <!-- Items: cantidades / descripción / unidad / etc. -->
    <gCamItem>
      <!-- NO informar E720 (precios) -->
      <!-- NO informar E730 (IVA) -->
    </gCamItem>

    <!-- Transporte (E900...) -->
    <gTransp>
      <iTipTrans>...</iTipTrans> <!-- E901 -->
      ...
      <gLocSal>...</gLocSal>     <!-- E920 -->
      <gLocEnt>...</gLocEnt>     <!-- E940 -->
      <gVehTras>...</gVehTras>   <!-- E960 -->
      <gCamTrans>...</gCamTrans> <!-- E980 -->
    </gTransp>

    <!-- NO informar F001 (totales) -->
  </DE>

  <!-- Firma -->
  <Signature>...</Signature>
</rDE>
```

---

## 7) Evento de Cancelación (Emisor) — implementación

### 7.1 Cuándo podés cancelar y plazos

- Para **Factura Electrónica (FE)**: hasta **48 horas** desde la aprobación. citeturn1view2turn2search2
- Para **cualquier otro DTE distinto a FE**: hasta **168 horas (7 días)** desde la aprobación. citeturn1view2turn2search2

👉 Una **NRE** entra en “cualquier otro DTE distinto a FE”, por lo que el plazo operativo típico es **168 horas** desde la aprobación (salvo que una normativa posterior lo cambie).

### 7.2 Servicio a usar

- **WS Recepción Evento**: `siRecepEvento` (mismo canal tecnológico que ya usás, solo cambia payload). citeturn4view0

### 7.3 Estructura del evento (v150)

En la estructura base de eventos:

- `GDE006 (dTiGDE)` = **1** para **Cancelación** citeturn7view1
- El grupo del tipo de evento debe corresponder a `dTiGDE` (o sea, debe incluir el grupo de cancelación). citeturn7view1

Formato específico **Evento Cancelación**:
- `rGeVeCan` (raíz cancelación)
- `GEC002` = **CDC del DTE** a cancelar
- `GEC003` = **motivo** (texto libre 5..500) citeturn7view1

### 7.4 Flujo de cancelación recomendado (operativo)

1) Validar que el DE esté **aprobado** (estado DTE “válido” en SIFEN)
2) Verificar **plazo** (48h FE / 168h resto) citeturn2search2turn1view2
3) Construir XML de evento:
   - `dTiGDE=1`
   - `dFecFirma` con timestamp actual
   - `dVerFor` versión del formato
   - `rGeVeCan` con CDC + motivo citeturn7view1
4) Firmar el evento (firma XML del `rEve`) citeturn5view2
5) Enviar `siRecepEvento`
6) Consultar/guardar respuesta y marcar el DTE como **CANCELADO** en tu sistema (estado final)

### 7.5 Ejemplo de esqueleto XML del evento de cancelación (extracto)

```xml
<rEve>
  <gGroupGesEve Id="1">
    <dFecFirma>2026-01-22T14:25:00</dFecFirma>
    <dVerFor>150</dVerFor>
    <dTiGDE>1</dTiGDE>

    <gGroupTiEvt>
      <rGeVeCan>
        <Id>CDC_DEL_DTE_A_CANCELAR</Id>
        <mOtEve>Error en la operación, no se concretó el traslado.</mOtEve>
      </rGeVeCan>
    </gGroupTiEvt>

    <Signature>...</Signature>
  </gGroupGesEve>
</rEve>
```

*(Los nombres exactos de nodos dependen del XSD, pero el MT define los campos esenciales: CDC y motivo.)* citeturn7view1

---

## 8) Recomendaciones de implementación (arquitectura)

### 8.1 Dominio y persistencia
- Tabla `de_documento` (ya la tenés) + `de_evento`
  - `tipo_evento` (cancelación/inutilización/etc.)
  - `cdc_ref` (para eventos que referencian DTE)
  - `payload_xml` firmado
  - `fecha_firma`, `estado_envio`, `respuesta_sifen`

### 8.2 Validaciones internas antes de enviar
Para minimizar rechazos:
- Si `C002=7`:
  - exigir `E500`
  - exigir `E901/E920/E940/E960/E980`
  - impedir `E720`, `E730`, `F001` citeturn5view1
  - regla `E501=7` ⇒ RUC receptor = RUC emisor citeturn6view1
  - regla `E501=1` y sin asociados ⇒ exigir `E506` citeturn6view1

### 8.3 “Contrato” con UI / backoffice
- Pantalla NRE debe pedir explícitamente:
  - Motivo (E501) y Responsable (E503) con combos
  - Datos de transporte (vehículo, chofer/transportista, salida y entrega)
  - Items con cantidades (sin precios)

---

## 9) Troubleshooting rápido

- **Rechazo por precios/totales**: revisá que no estés enviando `E720`, `E730`, `F001`. citeturn5view1
- **Rechazo por transporte**: falta alguno de `E901/E920/E940/E960/E980`. citeturn5view1
- **Rechazo por traslado interno**: `E501=7` y RUC receptor ≠ emisor. citeturn6view1
- **Rechazo por venta sin factura asociada**: `E501=1` sin asociados y sin `E506`. citeturn6view1
- **Cancelación fuera de plazo**: FE 48h / demás 168h. citeturn2search2turn1view2

---

## 10) Fuentes consultadas

- Manual Técnico SIFEN v150 (DNIT) citeturn4view0
- Preguntas Frecuentes e-Kuatia (DNIT) — plazos cancelación citeturn2search2
- Proyecto RG SIFEN (OAS mirror) — plazos 48h/168h y reglas generales de cancelación citeturn1view2
