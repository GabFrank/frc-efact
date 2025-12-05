# Manual de implementación de **Nota de Crédito, Nota de Débito y Nota de Remisión**  
## usando `rshk-jsifenlib` con SIFEN v150

> **Stack asumido:** Java 8+, `rshk-jsifenlib` ≥ 0.2.4, mismo esquema que ya uses para **Factura Electrónica (FE)**.  
> La idea es partir de tu flujo actual de FE y extenderlo a NCE, NDE y NRE.

---

## 1. Prerrequisitos

### 1.1. Dependencia

**Maven**

```xml
<dependency>
    <groupId>com.roshka.sifen</groupId>
    <artifactId>rshk-jsifenlib</artifactId>
    <version>0.2.4</version>
</dependency>
```

**Gradle**

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation 'com.roshka.sifen:rshk-jsifenlib:0.2.4'
}
```

### 1.2. Configuración de SIFEN

Usar la misma configuración que para FE:

```java
import com.roshka.sifen.core.Sifen;
import com.roshka.sifen.core.SifenConfig;

SifenConfig config = SifenConfig.loadFromFileName("conf/sifen.properties");
Sifen.setSifenConfig(config);
```

En `sifen.properties` recordá:

```properties
sifen.ambiente=DEV        # o PROD
sifen.certificado_cliente.usar=true
sifen.certificado_cliente.tipo=PFX
sifen.certificado_cliente.archivo=/ruta/certificado.pfx
sifen.certificado_cliente.contrasena=xxxx

sifen.csc=ABCD0000000000000000000000000000
sifen.csc.id=0001

# Después de puesta en producción de la NT 13:
sifen.habilitar_nota_tecnica_13=true
```

---

## 2. Conceptos clave SIFEN para estos documentos

### 2.1. Código de tipo de documento (campo `C002 / iTiDE`)

- `1` – Factura electrónica  
- `4` – Autofactura electrónica  
- `5` – Nota de crédito electrónica  
- `6` – Nota de débito electrónica  
- `7` – Nota de remisión electrónica  

En `jsifenlib` esto se maneja a través de un **enum** de tipo de documento, por ejemplo algo similar a:

```java
gTimb.setiTiDE(TiTiDE.NOTA_DE_CREDITO_ELECTRONICA);
```

(ajustá al enum real de tu versión).

### 2.2. Reglas específicas importantes

- Para **Nota de Crédito (C002 = 5)** y **Nota de Débito (C002 = 6)**:
  - Es **obligatorio informar un documento asociado** (grupo **H**, `gCamDEAsoc`), normalmente la FE de origen. citeturn3search4  
  - El receptor **no puede ser innominado** (`iTipIDRec ≠ INNOMINADO`) y se debe informar documento de identidad. citeturn3search1  

- Para **Nota de Remisión (C002 = 7)**:
  - Es obligatorio informar:
    - Tipo de transporte (`E901`), fechas de inicio/fin de traslado (`E909`, `E910`), vehículo (`E960`) y transportista (`E980`) según corresponda. citeturn3search4turn3search11  
  - Notas técnicas recientes extienden la regla de **receptor no innominado** también a la Nota de Remisión (C002 = 7). citeturn3search6  

---

## 3. Flujo general de construcción de un DE con `jsifenlib`

Sea cual sea el tipo (FE, NCE, NDE, NRE), el esqueleto es:

```java
import com.roshka.sifen.core.beans.DocumentoElectronico;
import com.roshka.sifen.core.beans.response.RespuestaRecepcionDE;

DocumentoElectronico de = new DocumentoElectronico();

// Grupo A / cabecera
de.setdFecFirma(LocalDateTime.now());
de.setdSisFact(TiSisFact.PRE_IMPRESO_O_FACTURADOR); // según tu caso

// Grupo B – Operación
TgOpeDE gOpeDE = new TgOpeDE();
// ... setiTipEmi, dCodSeg, etc.
de.setgOpeDE(gOpeDE);

// Grupo C – Timbrado
TgTimb gTimb = new TgTimb();
// ... dNumTim, dEst, dPunExp, dNumDoc, dFeIniT, iTiDE (TIPO DE DOCUMENTO)
de.setgTimb(gTimb);

// Grupo D – Datos generales: emisor + receptor
TgDatGralOpe gDatGralOpe = new TgDatGralOpe();
//   gEmis (emisor)
TgEmis gEmis = new TgEmis();
//   gDatRec (receptor)
TgDatRec gDatRec = new TgDatRec();
// ... llenar ambos grupos
gDatGralOpe.setgEmis(gEmis);
gDatGralOpe.setgDatRec(gDatRec);
de.setgDatGralOpe(gDatGralOpe);

// Grupo E – Detalle del DE (tipo de documento, ítems, totales, etc.)
TgDtipDE gDtipDE = new TgDtipDE();
// ... llenar según tipo de documento
de.setgDtipDE(gDtipDE);

// Envío
RespuestaRecepcionDE resp = Sifen.recepcionDE(de);
```

> El **NullPointerException** típico (como se ve en el issue de notas de crédito) viene de dejar grupos obligatorios nulos (por ej. no setear `gCamDEAsoc`, `gCamItemList`, totales, etc.). citeturn9view0  

---

## 4. Nota de Crédito Electrónica (NCE)

### 4.1. Reglas SIFEN que tenés que respetar

- `iTiDE / C002 = 5` (Nota de Crédito).  
- Documento asociado **obligatorio** (grupo H). Usualmente:
  - Tipo documento asociado = FE electrónica
  - CDC o timbrado + establecimiento + punto + número (según corresponda). citeturn3search4  
- Receptor **identificado** (no innominado, con número de documento). citeturn3search1  
- Motivo de emisión (`iMotEmi`) en grupo **gCamNCDE**:
  - Ej.: Devolución, descuento, bonificación, error en la factura, etc.

### 4.2. Grupos relevantes en `jsifenlib`

Aparte de lo que ya usás para FE:

- `gTimb.iTiDE = NOTA_DE_CREDITO_ELECTRONICA`
- `gDtipDE.gCamNCDE` → clase `TgCamNCDE` (ver issue #17). citeturn9view0  
- `gDtipDE.gCamDEAsocList` (o similar) → documentos asociados.  
- `gDtipDE.gCamItemList` → ítems sobre los que se aplica la NC.  
- Totales coherentes (mismo signo/reglas que el manual técnico indica para cada campo).

### 4.3. Ejemplo de construcción (Java)

> Este ejemplo asume que ya tenés una FE original y sus datos (CDC, timbrado, ítems, etc.).

```java
import com.roshka.sifen.core.beans.DocumentoElectronico;
import com.roshka.sifen.core.beans.de.TgDtipDE;
import com.roshka.sifen.core.beans.de.TgCamNCDE;
import com.roshka.sifen.core.types.TiMotEmi;
// ... otros imports de grupos que ya usás para FE

public RespuestaRecepcionDE emitirNotaCredito(FacturaOrigen factura,
                                              List<ItemNotaCredito> itemsNC) {

    DocumentoElectronico de = new DocumentoElectronico();

    // 1) Cabecera y operación
    de.setdFecFirma(LocalDateTime.now());
    de.setdSisFact(TiSisFact.SIFEN); // o el que corresponda

    TgOpeDE gOpeDE = new TgOpeDE();
    gOpeDE.setiTipEmi(TiTipEmi.NORMAL);
    gOpeDE.setdCodSeg(generarCodigoSeguridad());
    de.setgOpeDE(gOpeDE);

    // 2) Timbrado – tipo NCE
    TgTimb gTimb = new TgTimb();
    gTimb.setiTiDE(TiTiDE.NOTA_DE_CREDITO_ELECTRONICA);
    gTimb.setdNumTim(factura.getNumTimbradoNC()); // timbrado específico para NC
    gTimb.setdEst(factura.getEstablecimientoNC());
    gTimb.setdPunExp(factura.getPuntoExpedicionNC());
    gTimb.setdNumDoc(generarSiguienteNumeroNC());
    gTimb.setdFeIniT(factura.getFechaInicioTimbradoNC());
    de.setgTimb(gTimb);

    // 3) Datos generales (emisor + receptor)
    TgDatGralOpe gDatGralOpe = new TgDatGralOpe();
    TgEmis gEmis = mapearEmisor(factura.getEmisor());
    TgDatRec gDatRec = mapearReceptorDesdeFactura(factura.getReceptor());
    // Asegurate de que gDatRec NO sea innominado y tenga documento
    gDatGralOpe.setgEmis(gEmis);
    gDatGralOpe.setgDatRec(gDatRec);
    de.setgDatGralOpe(gDatGralOpe);

    // 4) Grupo E – Detalle y grupo específico de Nota de Crédito
    TgDtipDE gDtipDE = new TgDtipDE();

    // 4.1) Grupo de Documento Asociado (H)
    List<TgCamDEAsoc> asociados = new ArrayList<>();
    TgCamDEAsoc asoc = new TgCamDEAsoc();
    asoc.setiTipDocAso(TiTipDocAso.CDC);          // o impreso, según tu caso
    asoc.setdCdCDERef(factura.getCdc());          // CDC de la factura origen
    // si fuera impreso: H005..H011
    asociados.add(asoc);
    gDtipDE.setgCamDEAsocList(asociados);

    // 4.2) Grupo específico de NC
    TgCamNCDE gNCDE = new TgCamNCDE();
    gNCDE.setiMotEmi(TiMotEmi.DEVOLUCION_Y_AJUSTES_DE_PRECIOS);
    gNCDE.setdDesMotEmi("Devolución parcial de mercaderías");
    gDtipDE.setgCamNCDE(gNCDE);

    // 4.3) Ítems sobre los que aplica la NC
    List<TgCamItem> items = new ArrayList<>();
    for (ItemNotaCredito itemNC : itemsNC) {
        TgCamItem item = new TgCamItem();
        item.setdCodInt(itemNC.getCodigoInterno());
        item.setdDesProSer(itemNC.getDescripcion());
        item.setcUniMed(itemNC.getUnidadMedida());
        item.setdCantProSer(itemNC.getCantidad());
        item.setdPUniProSer(itemNC.getPrecioUnitario());
        // IVA
        TgCamIVA gCamIVA = new TgCamIVA();
        gCamIVA.setiAfecIVA(itemNC.getTipoImpuesto());
        gCamIVA.setdTasaIVA(itemNC.getTasaIva());
        gCamIVA.setdPropIVA(itemNC.getProporcionIva());
        item.setgCamIVA(gCamIVA);

        items.add(item);
    }
    gDtipDE.setgCamItemList(items);

    // 4.4) Totales
    calcularTotalesNotaCredito(gDtipDE); // tu método que llena campos C-XXX

    de.setgDtipDE(gDtipDE);

    // 5) Enviar a SIFEN
    return Sifen.recepcionDE(de);
}
```

> **Tip:** empezá clonando el código de generación de FE y solo cambiá:  
> `iTiDE`, grupo `gCamNCDE`, documento asociado y reglas de totales.

---

## 5. Nota de Débito Electrónica (NDE)

La NDE es muy parecida a la NCE, pero con sentido **“a favor del emisor”** (aumenta el saldo del cliente).

### 5.1. Reglas SIFEN

- `iTiDE / C002 = 6` (Nota de Débito).  
- Documento asociado obligatorio (grupo H) igual que en la NC. citeturn3search4  
- Receptor identificado (no innominado, con documento). citeturn3search1  
- Se usa para:
  - Intereses por mora  
  - Diferencias de precio a favor del emisor  
  - Otros cargos adicionales sobre una FE ya emitida  

### 5.2. Mapeo general en `jsifenlib`

La estructura es análoga a la NCE, cambiando:

- `gTimb.iTiDE = NOTA_DE_DEBITO_ELECTRONICA`  
- Grupo específico de NDE (en la librería es un grupo del estilo `gCamNDE` / `TgCamNDE`, revisar el paquete `com.roshka.sifen.core.beans.de` en tu proyecto para el nombre exacto de la clase).  
- Sigue existiendo:
  - `gCamDEAsocList` (documento asociado)  
  - `gCamItemList` (ítems)  
  - Totales C-XXX coherentes  

### 5.3. Esqueleto de implementación

```java
public RespuestaRecepcionDE emitirNotaDebito(FacturaOrigen factura,
                                             List<ItemNotaDebito> itemsND) {

    DocumentoElectronico de = new DocumentoElectronico();

    // 1) Cabecera + operación (igual que FE/NC)
    // ...

    // 2) Timbrado
    gTimb.setiTiDE(TiTiDE.NOTA_DE_DEBITO_ELECTRONICA);
    // resto de campos de timbrado para NDE
    // ...

    // 3) Emisor + receptor (mismas restricciones que NC)
    // ...

    // 4) Grupo E
    TgDtipDE gDtipDE = new TgDtipDE();

    // 4.1) Documento asociado (grupo H) – obligatorio
    // (igual que en NC, referenciando la FE original)
    // ...

    // 4.2) Grupo específico de NDE
    TgCamNDE gNDE = new TgCamNDE(); // nombre aproximado, revisar clase concreta
    gNDE.setiMotEmi(TiMotEmi.DIFERENCIA_EN_PRECIO); // según enum disponible
    gNDE.setdDesMotEmi("Diferencia de precio a favor del emisor");
    gDtipDE.setgCamNDE(gNDE);

    // 4.3) Ítems / importes
    //   – normal es que trabajes con importes **positivos**
    //     y dejes que los totales representen el aumento del monto.
    // ...

    // 4.4) Totales
    calcularTotalesNotaDebito(gDtipDE);

    de.setgDtipDE(gDtipDE);

    // 5) Envío
    return Sifen.recepcionDE(de);
}
```

> **Recomendación:** implementar primero NC 100% funcional, luego clonar la lógica para NDE cambiando solamente reglas de negocio (signo, motivos, mensajes).

---

## 6. Nota de Remisión Electrónica (NRE)

La NRE tiene un foco distinto: **traslado de mercaderías** (ventas, consignación, devolución al proveedor, etc.) y está muy cargada en la parte de **transporte**.

### 6.1. Reglas SIFEN importantes

- `iTiDE / C002 = 7` (Nota de Remisión electrónica). citeturn3search3  
- Obligatorio informar:
  - Tipo de transporte (`E901`) y descripción (`E902`). citeturn3search4turn3search11  
  - Fechas estimadas de inicio y fin de traslado (`E909`, `E910`). citeturn3search4  
  - Vehículo de traslado (`E960` y subcampos). citeturn3search4turn3search11  
  - Transportista (grupo `E980` y siguientes). citeturn3search4turn3search11  
- Receptor no innominado, igual que en NC/ND, según notas técnicas recientes. citeturn3search6  

### 6.2. Grupos relevantes en `jsifenlib`

Además de los grupos comunes:

- `gTimb.iTiDE = NOTA_DE_REMISION_ELECTRONICA`
- `gDtipDE.gCamNRE` → grupo específico de la NRE (motivo de emisión, responsable, km, fecha de emisión de la factura, etc.). El issue #68 muestra un JSON ejemplo con este grupo. citeturn8view0  
- `gDtipDE.gTransp` con subgrupos:
  - `gCamSal` → datos de salida  
  - `gCamEntList` → direcciones de entrega  
  - `gVehTrasList` → vehículos  
  - `gCamTrans` → datos del transportista / chofer  

### 6.3. Ejemplo basado en el JSON del issue #68

A nivel JSON (para tener referencia de estructura), una NRE puede verse así: citeturn8view0  

```json
"gDtipDE": {
  "gCamItemList": [ /* mercaderías trasladadas */ ],
  "gCamNRE": {
    "iMotEmiNR": "TRASLADO_POR_VENTAS",
    "iRespEmiNR": "EMISOR_FACTURA",
    "dKmR": 0,
    "dFecEm": "2025-09-01"
  },
  "gTransp": {
    "iTipTrans": "PROPIO",
    "iModTrans": "TERRESTRE",
    "iRespFlete": "EMISOR_FACTURA_ELECTRONICA",
    "cCondNeg": "CFR",
    "dIniTras": "2025-09-30",
    "dFinTras": "2025-09-30",
    "cPaisDest": "PRY",
    "gCamSal": { /* datos de salida */ },
    "gCamEntList": [ /* uno o varios destinos */ ],
    "gVehTrasList": [ /* vehículos utilizados */ ],
    "gCamTrans": { /* datos del transportista/chofer */ }
  }
}
```

En Java sería algo así:

```java
public RespuestaRecepcionDE emitirNotaRemision(Remision remision) {

    DocumentoElectronico de = new DocumentoElectronico();

    // 1) Cabecera + operación (igual que FE)
    // ...

    // 2) Timbrado
    gTimb.setiTiDE(TiTiDE.NOTA_DE_REMISION_ELECTRONICA);
    // ... timbrado específico de NRE
    de.setgTimb(gTimb);

    // 3) Emisor + receptor
    //    – Asegurate de que el receptor tenga documento, no innominado.
    // ...

    // 4) Grupo E
    TgDtipDE gDtipDE = new TgDtipDE();

    // 4.1) Ítems trasladados
    List<TgCamItem> items = new ArrayList<>();
    for (RemisionItem it : remision.getItems()) {
        TgCamItem item = new TgCamItem();
        item.setdCodInt(it.getCodigoInterno());
        item.setdDesProSer(it.getDescripcion());
        item.setcUniMed(it.getUnidadMedida());
        item.setdCantProSer(it.getCantidad());
        // En muchas NRE los ítems van exentos de IVA (traslado sin venta),
        // pero seguí el modelo definido en tu negocio.
        TgCamIVA iva = new TgCamIVA();
        iva.setiAfecIVA(TiAfecIVA.EXENTO);
        iva.setdTasaIVA(BigDecimal.ZERO);
        iva.setdPropIVA(BigDecimal.ZERO);
        item.setgCamIVA(iva);

        items.add(item);
    }
    gDtipDE.setgCamItemList(items);

    // 4.2) Grupo específico NRE
    TgCamNRE gNRE = new TgCamNRE();
    gNRE.setiMotEmiNR(TiMotEmiNR.TRASLADO_POR_VENTAS);
    gNRE.setiRespEmiNR(TiRespEmiNR.EMISOR_FACTURA);
    gNRE.setdKmR(remision.getKmEstimado());
    gNRE.setdFecEm(remision.getFechaFacturaRelacionada());
    gDtipDE.setgCamNRE(gNRE);

    // 4.3) Transporte
    TgTransp gTransp = new TgTransp();
    gTransp.setiTipTrans(TiTipTrans.PROPIO);
    gTransp.setiModTrans(TiModTrans.TERRESTRE);
    gTransp.setiRespFlete(TiRespFlete.EMISOR_FACTURA_ELECTRONICA);
    gTransp.setcCondNeg(remision.getCondicionNegociacion());
    gTransp.setdIniTras(remision.getFechaInicioTraslado());
    gTransp.setdFinTras(remision.getFechaFinTraslado());
    gTransp.setcPaisDest("PRY");

    // 4.3.1) Salida
    TgCamSal gCamSal = new TgCamSal();
    gCamSal.setdDirLocSal(remision.getDireccionSalida());
    // ... resto de campos (departamento, distrito, ciudad, teléfono)
    gTransp.setgCamSal(gCamSal);

    // 4.3.2) Destinos
    List<TgCamEnt> destinos = new ArrayList<>();
    for (RemisionDestino dest : remision.getDestinos()) {
        TgCamEnt ent = new TgCamEnt();
        ent.setdDirLocEnt(dest.getDireccion());
        // ... resto de campos
        destinos.add(ent);
    }
    gTransp.setgCamEntList(destinos);

    // 4.3.3) Vehículos
    List<TgVehTras> vehiculos = new ArrayList<>();
    TgVehTras veh = new TgVehTras();
    veh.setdTiVehTras(TiTiVehTras.VEHICULO);
    veh.setdMarVeh(remision.getVehiculo().getMarca());
    veh.setdTipIdenVeh(TiTipIdenVeh.NRO_MATRICULA);
    veh.setdNroMatVeh(remision.getVehiculo().getMatricula());
    // ...
    vehiculos.add(veh);
    gTransp.setgVehTrasList(vehiculos);

    // 4.3.4) Transportista / chofer
    TgCamTrans gCamTrans = new TgCamTrans();
    gCamTrans.setiNatTrans(remision.getTransportista().getNaturaleza());
    gCamTrans.setdNomTrans(remision.getTransportista().getNombre());
    gCamTrans.setiTipIDTrans(remision.getTransportista().getTipoDoc());
    gCamTrans.setdNumIDTrans(remision.getTransportista().getNumeroDoc());
    gCamTrans.setdNumIDChof(remision.getChofer().getNumeroDoc());
    gCamTrans.setdNomChof(remision.getChofer().getNombre());
    gCamTrans.setdDomFisc(remision.getTransportista().getDomicilioFiscal());
    gCamTrans.setdDirChof(remision.getChofer().getDireccion());
    gTransp.setgCamTrans(gCamTrans);

    gDtipDE.setgTransp(gTransp);

    // 4.4) Totales (si corresponde, muchas NRE son informativas)
    calcularTotalesNotaRemision(gDtipDE);

    de.setgDtipDE(gDtipDE);

    // 5) Enviar
    return Sifen.recepcionDE(de);
}
```

---

## 7. Buenas prácticas generales

1. **Reusar el flujo de FE**  
   Implementá todo primero para Factura Electrónica. Para NC, ND y NRE:
   - Reusá `gOpeDE`, `gTimb`, `gEmis`, `gDatRec`, `gCamItemList`.
   - Cambiá solo:
     - `iTiDE`
     - Grupo específico (`gCamNCDE`, `gCamNDE`, `gCamNRE`)
     - Regla de negocio (totales, motivo, transporte).

2. **Validar con el manual antes de probar en producción**  
   - Revisar secciones:
     - Campos de NCE / NDE (grupo H + C-XXX). citeturn3search0turn3search1  
     - Campos de Nota de Remisión (E500–E599 + E9/E10 transporte). citeturn3search5turn3search11  

3. **Loguear siempre la respuesta de SIFEN**  
   - Código de respuesta  
   - Mensajes de error  
   - XML enviado  
   Así podés ajustar rápidamente campos que no cumplan alguna validación.

4. **Mantener clases de dominio limpias**  
   - Trabajá internamente con tus entidades (Factura, NotaCredito, NotaDebito, Remision, etc).  
   - Tené una capa de *mapper* que traduzca tu modelo → beans de `jsifenlib`.  
   Eso evita mezclar lógica de negocio con detalles de SIFEN.

5. **Automatizar pruebas de integración**  
   - Crear escenarios:
     - NCE completa (devolución parcial y total).  
     - NDE por intereses.  
     - NRE por traslado de ventas / consignación.  
   - Probar contra ambiente **DEV** de SIFEN hasta que no haya errores.

---

## 8. Checklist rápida por tipo de documento

### Nota de Crédito (NCE)

- [ ] `iTiDE = NOTA_DE_CREDITO_ELECTRONICA (C002 = 5)`  
- [ ] Documento asociado (`gCamDEAsoc`) obligatorio  
- [ ] Receptor identificado, no innominado  
- [ ] Motivo de emisión (`gCamNCDE.iMotEmi`) correcto  
- [ ] Totales coherentes con los ítems y regla de negocio  

### Nota de Débito (NDE)

- [ ] `iTiDE = NOTA_DE_DEBITO_ELECTRONICA (C002 = 6)`  
- [ ] Documento asociado (`gCamDEAsoc`) obligatorio  
- [ ] Receptor identificado, no innominado  
- [ ] Motivo de emisión de débito correcto  
- [ ] Totales reflejan aumento de saldo a favor del emisor  

### Nota de Remisión (NRE)

- [ ] `iTiDE = NOTA_DE_REMISION_ELECTRONICA (C002 = 7)`  
- [ ] Ítems trasladados (`gCamItemList`) correctamente informados  
- [ ] Grupo `gCamNRE` con motivo y responsable del traslado  
- [ ] Grupo `gTransp` completo:
  - [ ] Fechas de traslado  
  - [ ] Origen (`gCamSal`)  
  - [ ] Destinos (`gCamEntList`)  
  - [ ] Vehículo (`gVehTrasList`)  
  - [ ] Transportista / chofer (`gCamTrans`)  

---

Con este manual deberías poder extender tu implementación actual de Factura Electrónica hacia **Notas de Crédito**, **Notas de Débito** y **Notas de Remisión**, manteniendo una arquitectura limpia y alineada con la ficha técnica SIFEN v150 y la librería `rshk-jsifenlib`.
