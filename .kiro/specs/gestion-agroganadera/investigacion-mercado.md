# Módulo de Gestión Agroganadera — Investigación de mercado y alcance propuesto

> **Estado: investigación cerrada, alcance NO decidido.** Fecha: 2026-08-07.
> Documento de insumo para elegir el MVP. Nada de esto está implementado.

## Contexto y motivación

El 90% de los clientes de frc-efact son **estancieros, productores y agroganaderos**. Hoy el
sistema solo les resuelve la facturación electrónica; la gestión del campo la hacen en planillas
o en software de terceros que no factura.

El módulo se vendería como **extra**, habilitable/deshabilitable por empresa desde la
configuración general, y **solo un usuario ADMIN** puede activarlo.

### Requisitos declarados por el usuario

- Configuración de monedas y cotizaciones cruzadas — **por empresa, no global**
- Registro de gastos por categorías
- Registro de compras (animales, semillas, etc.)
- ⚠️ **"una compra puede ser un gasto o puede ser una compra de algo que será vendido nuevamente"**
- Reportes en general
- Manejable desde desktop o mobile

### Decisiones de alcance ya tomadas

| Pregunta | Respuesta |
|---|---|
| ¿Foco ganadero o agrícola? | **Ambos por igual** |
| ¿Operación offline? | **Deseable pero no bloqueante** |
| ¿Integración SENACSA? | **No en el MVP, pero preparar el terreno** |
| ¿Arquitectura? | **Dentro de frc-efact** |

---

## Sistemas relevados

### Ganadería

**[BIGCAMPO](https://play.google.com/store/apps/details?id=com.fecoprod.fecomeat)** — el
competidor más directo: 100% paraguayo, de **FECOPROD LTDA**. Módulos separados: **Fecomeat**
(ganadería de carne), **Siscole** (tambo), **Agrícola**, **Finanzas** y **Maquinarias**. Tiene
parcelas con NDVI, avisos de vacas secas y vacas a parir, e informes comparativos entre
establecimientos.

**[Physis](https://www.physis.com.ar/aplicacion.php?id=6)** (Argentina) — organiza todo
alrededor de la **planilla de existencias**. Además: producción de carne, servicios, resultado
por tenencia, caravanas.

**[Calipso ERP](https://www.calipso.com/industrias/software-agropecuario/ganadero/)** —
nacimientos, historia clínica, trazabilidad por animal, sanidad, **remesas de compra**,
**liquidación de venta de hacienda**, procesos diferenciados por categoría.

**[Software GANADERO SG](https://www.softwareganadero.com/)** y
**[PROGAN](https://progansoftware.com/)** — cría, engorde, doble propósito, lechería.

### Agricultura

**[Agricloud](https://www.agricloud.es/)**, **[Agroptima](https://www.agroptima.com/)**,
**[AgroWin](https://www.agrowin.com/)**, **[SYNAgro](https://synagroweb.com/nuestro-software/)**
— todos convergen en la misma cadena: **campaña/zafra → lote → labores → insumos → cosecha →
rendimiento y margen bruto por lote**. Costos por hectárea, horas de maquinaria, dosis de
insumos, rentabilidad por cultivo/lote/campaña.

---

## Cuatro conceptos que se repiten en todos los sistemas

Son los que un diseño ingenuo se pierde y después no se pueden agregar sin migrar datos.

### 1. La planilla de existencias es el corazón, no un reporte

```
existencia inicial por categoría
  + entradas  (nacimientos, compras, recategorizaciones que entran)
  − salidas   (ventas, muertes, traslados, recategorizaciones que salen)
  = existencia final
```

Todo lo demás cuelga de esto. Si el modelo no cierra la planilla, el módulo no sirve.

### 2. La recategorización es un movimiento, no un `UPDATE`

Un ternero se vuelve vaquilla y después vaca. Si la categoría es un campo mutable del animal (o
del lote), se pierde la historia y **la planilla no cierra nunca**. Tiene que ser un movimiento
con fecha, categoría origen y categoría destino.

### 3. El resultado por tenencia va separado del margen operativo

El ganado se valoriza por engorde y por variación de precio. Eso no es ganancia por venta. Es lo
que distingue la contabilidad agropecuaria de la común, y es la razón por la que un ERP genérico
no sirve para el rubro.

### 4. La zafra no es el año calendario

El período contable agrícola es la **campaña**, y cruza años. Cualquier reporte agrícola agrupado
por año calendario está mal.

---

## Marco regulatorio paraguayo (terreno a preparar, no a integrar)

Verificado contra SENACSA:

- El propietario y el establecimiento se registran en
  **[SIGOR](https://senacsa.gov.py/servicios/sanidad-animal-identidad-y-trazabilidad/sistema-de-idenfificacion-animal-del-paraguay/)**
  ante la **Unidad Zonal**.
- Los movimientos de animales usan:
  - **[COTA](https://senacsa.gov.py/servicios/sanidad-animal-identidad-y-trazabilidad/cuarentena/control-de-transito-de-animales/certificado-oficial-de-transito-de-animales/)**
    — 8 días de validez, **revalidable una sola vez**, no se emite durante períodos de vacunación.
  - **Guía Simple de Traslado** — cuando no hay cambio de propietario.
  - **[Guía de Traslado Pagada](https://senacsa.gov.py/ciefa/instructivos/bovinos-y-bubalinos/guia-de-traslado-pagada/)**
    — cuando sí hay cambio de propietario.
- Identificación individual: **SIAP** (caravanas), obligatoria y de implantación **gradual**.
- **SITRAP** aplica a exportación.

### Traducción a modelo de datos (decidir ahora, implementar después)

- `establecimiento` con número SENACSA y unidad zonal
- cada movimiento con origen, destino, tipo de guía, número y vigencia
- identificación individual (caravana/SIAP) **opcional desde el día uno** — hoy la trazabilidad
  es grupal, pero el campo tiene que existir o después hay que migrar todo

---

## La pregunta de compra vs gasto

Es la decisión más importante del modelo. La respuesta: **se clasifica por línea, no por
documento**. Una misma factura de compra puede tener las cuatro clases.

| Destino de la línea | Ejemplo | Cuándo impacta el resultado |
|---|---|---|
| **Gasto directo** | combustible, flete, honorarios | al comprarlo |
| **Insumo a stock** | semilla, agroquímico, sal, vacuna | **al aplicarlo**, no al comprarlo |
| **Activo biológico** | animales | al venderlos (más resultado por tenencia) |
| **Bien de uso** | maquinaria, mejoras | por depreciación |

Si esto se define bien al principio, los reportes de costo salen solos. Si se define mal, no hay
reporte que lo arregle después.

---

## El diferenciador

BIGCAMPO y los demás **gestionan**, pero después el productor tiene que facturar en otro sistema.
frc-efact **ya emite DE aprobados por SIFEN**.

> "Vendo 50 novillos → descuenta existencias → emite la factura electrónica"

Ningún competidor de la lista puede ofrecer eso. Es el puente entre el módulo nuevo y lo que ya
está construido, y debería estar **en el centro del MVP, no como extra**.

---

## MVP propuesto (a confirmar)

En orden de dependencia:

1. **Habilitación del módulo por empresa** — flag en `empresa`, solo `ADMIN`
2. **Moneda y cotizaciones por empresa** — tabla de cotizaciones por fecha y par de monedas
3. **Establecimientos y unidades productivas** (potreros y parcelas) — la columna vertebral
   espacial que ganadería y agricultura comparten
4. **Categorías de ganado + planilla de existencias**, con los seis movimientos: nacimiento,
   compra, venta, muerte, recategorización, traslado
5. **Compras con clasificación por línea** — los cuatro destinos de la tabla de arriba
6. **Gastos por categoría**, imputables a establecimiento y actividad
7. **Agrícola mínimo**: parcela con hectáreas, campaña, cultivo, siembra, cosecha en kg →
   costo por hectárea y margen por lote
8. **Venta de hacienda que genera la `FacturaLegal`** — el diferenciador
9. **Reportes**: existencias, costo por actividad y establecimiento, margen por lote y campaña

### Fuera del MVP (con fundamento)

| Excluido | Por qué |
|---|---|
| Tambo / lechería | Otro dominio. BIGCAMPO lo tiene como módulo aparte (Siscole) |
| Maquinarias con horas y combustible | Mucho valor, pero modelo propio. Candidato a v2 |
| Detalle agronómico (NDVI, dosis, labores mecanizadas) | Profundidad que no cambia el margen |
| Reproducción y sanidad individual (servicios, tacto, partos) | Requiere identificación individual madura |
| Integración SENACSA | Decidido: solo preparar el terreno |
| Offline real | Decidido: deseable, no bloqueante |

---

## Dos decisiones técnicas a tomar antes de escribir código

### IDs — habilitar offline sin romper el estándar

`DATABASE_STANDARDS.md` manda `id BIGSERIAL PK`. Para habilitar offline más adelante sin migrar
todo, agregar además una columna `uuid` con índice único en las tablas del módulo: el cliente
puede generarla sin conexión y el PK sigue siendo serial. Respeta el estándar y deja la puerta
abierta.

### Tiempos — dos marcas distintas

- `registrado_en` — cuándo lo anotó el usuario (posiblemente en el campo, sin señal)
- `creado_en` — cuándo llegó al servidor (ya existe en `AuditableEntity`)

Sin esa separación, la reconciliación offline es imposible después.

---

## Preguntas abiertas (bloquean el plan de implementación)

1. **¿La venta de hacienda genera factura electrónica en el MVP, o solo registra la salida de
   existencias?** Es el diferenciador, pero acopla el módulo nuevo al `SifenService`, que es la
   parte más delicada del sistema.
2. **¿`Producto` se reutiliza para insumos, o va una entidad `agro.insumo` aparte?** El `Producto`
   actual carga campos SIFEN (`tipo_transaccion`, IVA) que un insumo no necesita, pero reusarlo
   evita duplicar catálogo.
3. **¿Maquinarias entra al MVP?** Está en BIGCAMPO y para estancieros es un costo grande. Quedó
   afuera por alcance; si los clientes lo piden fuerte, sube.

Cuando estas tres se respondan, se escribe `requirements.md` / `design.md` / `tasks.md` siguiendo
la convención del resto de `.kiro/specs/`.
