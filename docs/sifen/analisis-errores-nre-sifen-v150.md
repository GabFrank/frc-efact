# Análisis de errores – Nota de Remisión Electrónica (NRE) SIFEN v150

Este documento explica **cada error recibido**, su **causa real según el Manual Técnico SIFEN v150** y la **solución práctica** que debés aplicar en el XML de la Nota de Remisión Electrónica (C002 = 7).

---

## 1) Error: `dInfoFisc es inválido`

### Causa
En una **Nota de Remisión Electrónica**, el campo:

- `B006 – dInfoFisc`

es **OBLIGATORIO (1–1)** cuando `C002 = 7`.

Además, el Manual Técnico exige que se informe un **mensaje fiscal** conforme a la **RG N.º 41/2014 – Art. 3 Inc. 7**.

Este error aparece cuando:
- El campo se envía vacío (`""`)
- Se envía `null`
- Contiene saltos de línea o caracteres no válidos
- El builder lo omite por tratarlo como opcional (error común)

### Solución
- Informar **siempre** `dInfoFisc` en NRE.
- Usar **texto plano**, una sola línea, sin saltos de línea ni caracteres especiales.

Ejemplo seguro:
```xml
<dInfoFisc>Documento emitido como Nota de Remisión Electrónica conforme RG 41/2014.</dInfoFisc>
```

---

## 2) Error: `dDirRec es inválido`

### Causa
Cuando se informa la **dirección del receptor**, el MT valida coherencia entre:
- Dirección
- Número de casa
- Ciudad / distrito / departamento

Errores comunes:
- `dDirRec` informado sin número de casa cuando corresponde
- Valor vacío o con espacios
- Caracteres no permitidos
- Inconsistencia con datos geográficos

### Solución
- Si informás `dDirRec`, asegurate de:
  - Informar número de casa (si aplica)
  - Usar texto válido y no vacío
- Si no conocés la dirección exacta, es preferible **no informar** el campo antes que enviarlo inválido.

Ejemplo válido:
```xml
<dDirRec>Av. San Blas</dDirRec>
<dNumCasRec>1234</dNumCasRec>
```

---

## 3) Error: `El valor 0 del elemento: cCiuSal es inválido`

### Causa
`cCiuSal` (Ciudad del local de salida) es:
- **Obligatorio en NRE**
- Debe ser un **código válido de ciudad** (Tabla de Ciudades SIFEN)

El valor `0` **NO es permitido**.

### Solución
- Reemplazar `0` por un **código real de ciudad**.
- El código debe existir en la tabla oficial y ser coherente con:
  - Departamento
  - Distrito (si informado)

Ejemplo:
```xml
<cCiuSal>1101</cCiuSal>
<dDesCiuSal>Ciudad del Este</dDesCiuSal>
```

---

## 4) Error: `El valor 0 del elemento: cCiuEnt es inválido`

### Causa
Idéntica al error anterior, pero aplicado al **local de entrega**.

`cCiuEnt`:
- Es obligatorio en NRE
- No acepta `0`
- Debe existir en la tabla de ciudades

### Solución
- Usar código real de ciudad
- Mantener coherencia geográfica

Ejemplo:
```xml
<cCiuEnt>1001</cCiuEnt>
<dDesCiuEnt>Asunción</dDesCiuEnt>
```

---

## 5) Error: `El valor MERCEDEZ BENZ del elemento: dMarVeh es inválido`

### Causa
El campo:
- `E962 – dMarVeh (Marca del vehículo)`

tiene **longitud máxima de 10 caracteres**.

`MERCEDEZ BENZ` excede ese límite.

### Solución
Usar una abreviatura válida (≤10 caracteres).

Ejemplos válidos:
- `MERCEDES`
- `MBENZ`
- `BENZ`

Ejemplo XML:
```xml
<dMarVeh>MERCEDES</dMarVeh>
```

---

## Checklist final para evitar estos errores en NRE

- [ ] `dInfoFisc` presente y con texto válido
- [ ] `cCiuSal` ≠ 0 y código real
- [ ] `cCiuEnt` ≠ 0 y código real
- [ ] Coherencia entre departamento / distrito / ciudad
- [ ] `dDirRec` coherente o no informado
- [ ] `dMarVeh` ≤ 10 caracteres

---

## Recomendación técnica

Implementar **validaciones previas al envío** específicas para NRE:

- Reglas de longitud (vehículo, textos)
- Catálogos obligatorios (ciudades)
- Reglas condicionales por tipo de documento (`C002=7`)
- Sanitización de strings (trim, sin saltos de línea)

Esto reduce drásticamente rechazos del SIFEN y acelera la aprobación.

---

Documento preparado para depuración e implementación práctica en sistemas de facturación electrónica SIFEN v150.
