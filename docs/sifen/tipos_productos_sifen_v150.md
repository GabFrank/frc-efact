
# Tipos de Productos y Configuración para Facturación Electrónica SIFEN v1.50

Este documento describe los **tipos de productos y servicios** que pueden formar parte de una **factura electrónica** según el Manual Técnico del SIFEN versión 1.50 de la SET (Subsecretaría de Estado de Tributación - Paraguay), y cómo deben configurarse en un sistema de facturación electrónica adaptable a cualquier tipo de empresa.

---

## 1. Tipos de Operación (Campos D011 y D012)

El campo `D011 (iTipTra)` indica el **tipo de transacción u operación comercial**, mientras que `D012 (dDesTipTra)` contiene la **descripción textual**.  
A continuación se detallan los tipos válidos:

| Código | Descripción | Cuándo usarlo / Configuración |
|--------|--------------|-------------------------------|
| **1** | Venta de mercadería | Para **bienes físicos** o mercaderías. Configurar el producto como “bien” con IVA y unidad de medida. |
| **2** | Prestación de servicios | Para **servicios puros**, sin entrega de bienes. Configurar como “servicio”. |
| **3** | Mixto (Venta de mercadería y servicios) | Cuando una factura incluye bienes y servicios. Configurar para permitir ambos tipos en la misma transacción. |
| **4** | Venta de activo fijo | Para la **venta de activos fijos** (equipos, maquinarias, vehículos). Configurar como “activo fijo”. |
| **5** | Venta de divisas | Para operaciones de cambio o venta de moneda extranjera. Configurar como “divisas”. |
| **6** | Compra de divisas | Compra de moneda extranjera. Configurar como “compra de divisas”. |
| **7** | Promoción o entrega de muestras | Para entrega gratuita o promocional de bienes. Configurar como “promoción/muestras”. |
| **8** | Donación | Para entrega sin contraprestación. Configurar como “donación”. |
| **9** | Anticipo | Cuando se cobra un anticipo de una operación futura. Configurar como “anticipo”. |
| **10** | Compra de productos | Facturación de compras de bienes. Configurar como “compra productos”. |
| **11** | Compra de servicios | Facturación de compras de servicios. Configurar como “compra servicios”. |
| **12** | Venta de crédito fiscal | Venta de derechos de crédito fiscal. Configurar como “crédito fiscal”. |
| **13** | Muestras médicas | Entrega de muestras médicas (Art. 3 RG 24/2014). Configurar como “muestras médicas”. |

---

## 2. Cómo Configurarlo en el Sistema de Facturación

### 2.1 Catálogo de Productos y Servicios

Cada ítem en la base de datos debe incluir:

- Tipo de artículo: **bien físico / servicio / activo fijo / promoción / donación / etc.**
- Código de tipo de transacción (`iTipTra`).
- Descripción (`dDesTipTra`).
- Precio unitario, unidad de medida, impuestos aplicables (IVA, ISC).
- Para bienes físicos: unidad de medida, cantidad, lote si aplica.
- Para servicios: unidad de medida (“hora”, “servicio”), cantidad (1 o más).
- Para promociones/donaciones: precio cero o simbólico.

---

### 2.2 Configuración del Documento Electrónico (Factura)

En la cabecera (`D010-D099`):

- `D011 iTipTra`: código según tabla anterior.
- `D012 dDesTipTra`: descripción textual del tipo.
- `D013 iTImp`: tipo de impuesto (IVA, ISC, etc.).
- `D015 cMoneOpe`: moneda de la operación.

En los ítems (`E700-E899`):

- Cada producto o servicio debe registrar cantidad, descripción, precio unitario, base imponible e impuestos.
- Los ítems deben diferenciar si son **bienes** o **servicios** para aplicar correctamente los impuestos.

---

### 2.3 Validaciones Recomendadas

- Si `iTipTra = 3` (Mixto), debe existir al menos un bien y un servicio en los ítems.
- Para códigos 7 (Promoción) y 8 (Donación), validar que el precio e IVA correspondan a su régimen.
- Para ventas de activo fijo (4), registrar correctamente la categoría contable del bien.
- Mantener actualizada la tabla de códigos conforme nuevas notas técnicas del SIFEN.

---

### 2.4 Implementación en Base de Datos

Ejemplo de estructura de tabla `tipo_transaccion`:

```sql
CREATE TABLE tipo_transaccion (
    codigo INT PRIMARY KEY,
    descripcion VARCHAR(150),
    observacion TEXT
);
```

Ejemplo de inserción inicial:

```sql
INSERT INTO tipo_transaccion (codigo, descripcion) VALUES
(1, 'Venta de mercadería'),
(2, 'Prestación de servicios'),
(3, 'Mixto (Venta de mercadería y servicios)'),
(4, 'Venta de activo fijo'),
(5, 'Venta de divisas'),
(6, 'Compra de divisas'),
(7, 'Promoción o entrega de muestras'),
(8, 'Donación'),
(9, 'Anticipo'),
(10, 'Compra de productos'),
(11, 'Compra de servicios'),
(12, 'Venta de crédito fiscal'),
(13, 'Muestras médicas');
```

---

### 2.5 Interfaz del Sistema

- Permitir al usuario seleccionar el tipo de transacción al crear la factura.
- O determinarlo automáticamente según los tipos de ítems incluidos.
- Mostrar descripción completa y código en pantalla o en tooltips.
- Al exportar a XML, mapear correctamente `iTipTra` y `dDesTipTra`.

---

### 2.6 Recomendaciones Finales

- Mantener compatibilidad con la **versión del Manual Técnico vigente (v1.50 o superior)**.
- Registrar el código de transacción con cada factura para trazabilidad.
- Permitir agregar nuevos códigos mediante actualización de parámetros del sistema.

---

**Fuente:** Manual Técnico SIFEN v1.50 – DNIT / SET Paraguay  
(https://www.dnit.gov.py/documents/20123/420595/NT_E_KUATIA_010_MT_V150.pdf)

---
