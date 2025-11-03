
# Tipos de Clientes y Configuración (Receptor - SIFEN v1.50)

Este documento describe los **tipos de clientes (receptor del Documento Electrónico)** que pueden formar parte de una factura electrónica conforme al **Manual Técnico del SIFEN versión 1.50 (SET - Paraguay)**, incluyendo los campos requeridos, validaciones y configuraciones recomendadas para sistemas de facturación electrónica.

---

## 1. Campos principales del receptor (cliente)

Los datos del cliente se encuentran en el bloque **Receptor (B400 - B499)** del Documento Electrónico.

| Campo | Descripción | Tipo / Valores | Obligatorio |
|--------|--------------|----------------|--------------|
| `B401 iNatRec` | Naturaleza del receptor | 1 = Contribuyente<br>2 = No contribuyente | Sí |
| `B402 iTiOpe` | Tipo de operación | 1 = B2B (empresa a empresa)<br>2 = B2C (empresa a consumidor)<br>3 = B2G (empresa a gobierno)<br>4 = B2F (empresa a extranjero) | Sí |
| `B403 iTiContRec` | Tipo de contribuyente | 1 = Persona física<br>2 = Persona jurídica | Condicional (si `iNatRec=1`) |
| `B404 dRucRec` | RUC del receptor | Formato 99999999-9 | Obligatorio si contribuyente |
| `B405 dDVRec` | Dígito verificador del RUC | Numérico | Obligatorio si contribuyente |
| `B406 dNomRec` | Nombre o razón social del receptor | Texto | Sí |
| `B407 dDirRec` | Dirección | Texto | Condicional (local) |
| `B408 cDepRec` | Código de departamento | Numérico (según catálogo SET) | Condicional |
| `B409 cDisRec` | Código de distrito | Numérico (según catálogo SET) | Condicional |
| `B410 cCiuRec` | Código de ciudad | Numérico (según catálogo SET) | Condicional |
| `B411 dNumCasRec` | Número de casa | Texto / opcional | No |
| `B412 dTelRec` | Teléfono | Texto / opcional | No |
| `B413 dCelRec` | Celular | Texto / opcional | No |
| `B414 dEmailRec` | Correo electrónico | Texto / opcional | No |
| `B415 cPaisRec` | País del receptor | Código ISO (PY, BR, AR, etc.) | Sí |
| `B416 dDesPaisRec` | Descripción del país | Texto | Sí |

---

## 2. Tipos de Clientes y Configuración

| Código | Tipo de cliente | Características | Campos obligatorios | Tipo de operación sugerido |
|--------|----------------|-----------------|----------------------|-----------------------------|
| **1** | Persona Física Contribuyente | Tiene RUC, persona natural registrada en la SET | RUC, DV, Nombre, Dirección | `iTiOpe = 1 (B2B)` o `2 (B2C)` |
| **2** | Persona Jurídica Contribuyente | Empresa registrada con RUC y razón social | RUC, DV, Razón social, Dirección | `iTiOpe = 1 (B2B)` |
| **3** | No Contribuyente (Consumidor Final) | No posee RUC. Solo nombre y cédula opcional | Nombre, País (PY), tipo contribuyente no informado | `iTiOpe = 2 (B2C)` |
| **4** | Cliente Extranjero | Cliente fuera de Paraguay | Nombre/Razón social, País ≠ PY, dirección opcional | `iTiOpe = 4 (B2F)` |
| **5** | Entidad Gubernamental | Organismo público paraguayo | RUC, Razón social, Dirección | `iTiOpe = 3 (B2G)` |

---

## 3. Validaciones Recomendadas

1. **Contribuyente:**  
   - Si `iNatRec = 1`, deben existir `dRucRec`, `dDVRec`, y `iTiContRec`.
   - Validar formato RUC (`XXXXXXXX-X`).

2. **No contribuyente:**  
   - `iNatRec = 2`.  
   - Campos `dRucRec` y `dDVRec` no deben informarse.  
   - `iTiContRec` debe omitirse.

3. **Cliente extranjero:**  
   - `cPaisRec ≠ PY`.  
   - Dirección puede ser opcional.  
   - Operación usualmente exenta de IVA.

4. **Entidad pública (B2G):**  
   - `iTiOpe = 3`.  
   - Debe incluir RUC y razón social.  
   - Puede requerir formato especial en eventos posteriores (notas de crédito, débito, etc.).

---

## 4. Ejemplo de Configuración en Base de Datos

### Tabla `tipo_cliente`
```sql
CREATE TABLE tipo_cliente (
    codigo INT PRIMARY KEY,
    descripcion VARCHAR(100),
    tipo_operacion_default INT,
    es_contribuyente BOOLEAN
);

INSERT INTO tipo_cliente (codigo, descripcion, tipo_operacion_default, es_contribuyente) VALUES
(1, 'Persona Física Contribuyente', 1, TRUE),
(2, 'Persona Jurídica Contribuyente', 1, TRUE),
(3, 'No Contribuyente (Consumidor Final)', 2, FALSE),
(4, 'Cliente Extranjero', 4, FALSE),
(5, 'Entidad Gubernamental', 3, TRUE);
```

### Tabla `cliente`
```sql
CREATE TABLE cliente (
    id SERIAL PRIMARY KEY,
    tipo_cliente INT REFERENCES tipo_cliente(codigo),
    ruc VARCHAR(15),
    dv VARCHAR(1),
    nombre VARCHAR(200),
    direccion TEXT,
    pais CHAR(2) DEFAULT 'PY',
    telefono VARCHAR(20),
    email VARCHAR(100),
    tipo_contribuyente SMALLINT,  -- 1: Físico, 2: Jurídico
    tipo_operacion SMALLINT       -- 1: B2B, 2: B2C, 3: B2G, 4: B2F
);
```

---

## 5. Recomendaciones de Implementación

- **Validar RUC y DV** automáticamente contra la base de la SET si es posible (API de verificación).  
- **Autocompletar datos** (razón social, tipo de contribuyente) al ingresar RUC.  
- En facturas mixtas (productos y servicios), no cambia la configuración del cliente.  
- El sistema debe **ajustar dinámicamente los campos requeridos** según el tipo de cliente elegido.  
- Mantener actualizada la tabla de países conforme el catálogo ISO oficial (SET).  
- Guardar siempre el tipo de operación (`iTiOpe`) y naturaleza del receptor (`iNatRec`) en la cabecera del Documento Electrónico.

---

## 6. Fuentes Oficiales

- Manual Técnico SIFEN v1.50 – DNIT / SET Paraguay  
  [Descargar PDF oficial](https://www.dnit.gov.py/documents/20123/420595/NT_E_KUATIA_010_MT_V150.pdf)  
- XSD de Definiciones de Tipos (DE_Types_v150.xsd)  
  [Ver esquema XML](https://ekuatia.set.gov.py/sifen/xsd/DE_Types_v150.xsd)  
- Notas Técnicas complementarias (SET, 2023–2025)

---
