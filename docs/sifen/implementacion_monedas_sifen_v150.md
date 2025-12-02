
# Implementación de Monedas en Facturación Electrónica SIFEN v1.50

Este documento describe cómo configurar, validar y emitir facturas electrónicas en **moneda local o extranjera** según el **Manual Técnico del SIFEN versión 1.50 (SET - Paraguay)**, incluyendo campos XML relevantes, estructura de base de datos y validaciones recomendadas.

---

## 1. Conceptos Generales

En el SIFEN (Sistema Integrado de Facturación Electrónica Nacional), cada **Documento Electrónico (DE)** debe declararse en **una sola moneda de operación**.  
Se permite utilizar monedas extranjeras, pero **no se pueden mezclar varias monedas en una misma factura**.

---

## 2. Campos Relevantes en el XML

Los campos relacionados con moneda se encuentran en el bloque **D010 - D099 (Campos inherentes a la operación comercial)**.

| Campo | Descripción | Tipo / Formato | Obligatorio |
|--------|--------------|----------------|--------------|
| `D015 cMoneOpe` | Código de la moneda de la operación | Código ISO 4217 (Ej: PYG, USD, BRL, EUR) | Sí |
| `D016 dDesMoneOpe` | Descripción de la moneda | Texto (Ej: “Guaraní”, “Dólar americano”) | Sí |
| `D017 dCondTiCam` | Condición del tipo de cambio | 1 = Fijo<br>2 = Variable | Condicional (si moneda ≠ PYG) |
| `D018 dTiCam` | Tipo de cambio utilizado | Decimal (Ej: 7250.00) | Condicional (si moneda ≠ PYG) |
| `D019 dTotOpeGs` | Total equivalente en guaraníes | Decimal | Condicional (si moneda ≠ PYG) |

---

## 3. Reglas de Uso

- **Factura en moneda extranjera:** se debe indicar `cMoneOpe` ≠ `PYG` y completar los campos de tipo de cambio (`dCondTiCam`, `dTiCam`) y equivalencia (`dTotOpeGs`).  
- **Factura en guaraníes:** usar `cMoneOpe = 'PYG'`; los campos de tipo de cambio no se informan.  
- **Prohibido:** incluir ítems con diferentes monedas dentro del mismo DE.  
- **Tipo de cambio:** se puede definir fijo (valor único para toda la factura) o variable (si se define por ítem, aunque en la práctica la mayoría usa global fijo).

---

## 4. Ejemplo de Estructura XML (Fragmento)

```xml
<D010GCamC>
  <D015>USD</D015>
  <D016>Dólar americano</D016>
  <D017>1</D017>
  <D018>7250.00</D018>
  <D019>700000.00</D019>
</D010GCamC>
```

En este ejemplo:
- La factura fue emitida en **USD**.  
- Tipo de cambio utilizado: 1 USD = 7.250 PYG.  
- Total de la operación expresado en guaraníes: 700.000 PYG.

---

## 5. Configuración en Base de Datos

### Tabla `moneda`
```sql
CREATE TABLE moneda (
  codigo CHAR(3) PRIMARY KEY,        -- ISO 4217 (PYG, USD, BRL, EUR...)
  descripcion VARCHAR(100),
  simbolo VARCHAR(5),
  tipo_cambio_base DECIMAL(18,6)
);

INSERT INTO moneda (codigo, descripcion, simbolo) VALUES
('PYG', 'Guaraní', '₲'),
('USD', 'Dólar Americano', '$'),
('BRL', 'Real Brasileño', 'R$'),
('EUR', 'Euro', '€');
```

### Campos en la tabla `factura`
```sql
ALTER TABLE factura ADD COLUMN moneda_operacion CHAR(3) DEFAULT 'PYG';
ALTER TABLE factura ADD COLUMN tipo_cambio DECIMAL(18,6);
ALTER TABLE factura ADD COLUMN total_equivalente_gs DECIMAL(18,2);
```

### Reglas de validación SQL
```sql
-- Verificar que la moneda sea única por factura
ALTER TABLE factura
  ADD CONSTRAINT chk_moneda_unica CHECK (moneda_operacion IN ('PYG','USD','BRL','EUR'));

-- Si moneda no es PYG, tipo de cambio debe ser > 0
ALTER TABLE factura
  ADD CONSTRAINT chk_tipo_cambio CHECK (
    (moneda_operacion = 'PYG' AND tipo_cambio IS NULL)
    OR (moneda_operacion <> 'PYG' AND tipo_cambio > 0)
  );
```

---

## 6. Validaciones en el Sistema

1. **Moneda única por factura:**  
   Todos los ítems deben tener la misma moneda (`moneda_operacion`).

2. **Tipo de cambio obligatorio:**  
   Si la moneda ≠ PYG, se debe informar el tipo de cambio y el total equivalente en guaraníes.

3. **Cálculo automático:**  
   - Total en moneda extranjera × tipo de cambio = total en guaraníes.  
   - Redondear según política del sistema (normalmente 2 decimales).

4. **Visualización:**  
   Mostrar ambos totales (en moneda y en guaraníes) en reportes e impresión.

5. **API / UI:**  
   - Selección de moneda por factura.  
   - Campo editable de tipo de cambio (o tomado de fuente oficial).  
   - Validar automáticamente cuando la moneda sea distinta de PYG.

---

## 7. Ejemplo Práctico

**Factura en dólares (USD):**
- Moneda operación: USD  
- Tipo de cambio: 7.250  
- Total: USD 100  
- Equivalente en guaraníes: 725.000 PYG

**Factura en guaraníes (PYG):**
- Moneda operación: PYG  
- Tipo de cambio: no informado  
- Total: ₲ 725.000  
- Equivalente en guaraníes: ₲ 725.000

---

## 8. Recomendaciones de Implementación

- Sincronizar el tipo de cambio diario con fuentes oficiales (Banco Central del Paraguay).  
- No permitir facturas mixtas con más de una moneda.  
- Mantener histórico de tipos de cambio utilizados para trazabilidad.  
- Configurar decimales según la moneda:  
  - PYG → 0 decimales.  
  - USD/BRL/EUR → 2 decimales.  
- Implementar validaciones automáticas antes de firmar el XML.  
- Asegurar compatibilidad con el esquema XSD `DE_Types_v150.xsd` (bloques D015-D019).

---

## 9. Fuentes Oficiales

- Manual Técnico SIFEN v1.50 – DNIT / SET Paraguay  
  [Descargar PDF oficial](https://www.dnit.gov.py/documents/20123/420595/NT_E_KUATIA_010_MT_V150.pdf)  
- XSD de tipos y campos: [DE_Types_v150.xsd](https://ekuatia.set.gov.py/sifen/xsd/DE_Types_v150.xsd)  
- Preguntas Frecuentes – e-Kuatia (SET)  
  [https://www.dnit.gov.py/web/e-kuatia/preguntas-frecuentes](https://www.dnit.gov.py/web/e-kuatia/preguntas-frecuentes)

---
