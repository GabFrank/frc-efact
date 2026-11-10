-- V40: corregir el IVA de los ítems de factura con el XML de su DE
--
-- ¿Por qué? V37 rellenó `factura_legal_item.iva` con el IVA ACTUAL del producto. Si al producto
-- le cambiaron la tasa después de facturar, el ítem queda con una tasa distinta de la que la SET
-- aprobó. Pasó en producción: SOJA GRANEL se facturó al 10 % (factura 16, cabecera también al
-- 10 %) y el producto hoy está al 5 %; V37 le habría puesto 5. Detectado el 2026-11-10 cruzando
-- una copia de producción contra los XML.
--
-- V37 ya está aplicada en desarrollo y no se modifica: esta migración corre después y pisa el
-- IVA con el del XML, que es la fuente autoritativa (el DE es inmutable en SIFEN).
--
-- Cruce ítem ↔ XML: por posición (los ítems se emiten en orden de id, igual que gCamItem) y
-- exigiendo además que la descripción coincida. Si la descripción no coincide no se toca nada:
-- mejor un ítem sin corregir que corregirlo con el IVA de otro.
--
-- Tasa: iAfecIVA 1 (gravado) o 4 (gravado parcial) → dTasaIVA; 2 (exonerado) o 3 (exento) → 0.
-- XML mal formados se saltean (xml_is_well_formed_document) para que la migración no falle.
-- Idempotente.

WITH ns AS (
    SELECT ARRAY[ARRAY['s', 'http://ekuatia.set.gov.py/sifen/xsd']] AS ns
),
xml_item AS (
    SELECT de.factura_legal_id,
           x.ord,
           btrim((xpath('/s:gCamItem/s:dDesProSer/text()', x.it, ns.ns))[1]::text) AS descripcion,
           (xpath('/s:gCamItem/s:gCamIVA/s:iAfecIVA/text()', x.it, ns.ns))[1]::text::int AS afectacion,
           (xpath('/s:gCamItem/s:gCamIVA/s:dTasaIVA/text()', x.it, ns.ns))[1]::text::numeric::int AS tasa
    FROM financiero.documento_electronico de
    CROSS JOIN ns
    CROSS JOIN LATERAL unnest(xpath('//s:gCamItem', de.xml_original::xml, ns.ns)) WITH ORDINALITY AS x(it, ord)
    WHERE de.factura_legal_id IS NOT NULL
      AND de.xml_original IS NOT NULL
      AND xml_is_well_formed_document(de.xml_original)
),
item AS (
    SELECT fli.id,
           fli.factura_legal_id,
           row_number() OVER (PARTITION BY fli.factura_legal_id ORDER BY fli.id) AS ord,
           btrim(fli.descripcion) AS descripcion,
           fli.iva
    FROM financiero.factura_legal_item fli
),
corregir AS (
    SELECT i.id,
           CASE WHEN x.afectacion IN (2, 3) THEN 0 ELSE x.tasa END AS iva_xml
    FROM item i
    JOIN xml_item x ON x.factura_legal_id = i.factura_legal_id AND x.ord = i.ord
    WHERE x.descripcion = i.descripcion
      AND x.afectacion IS NOT NULL
      AND (x.afectacion IN (2, 3) OR x.tasa IS NOT NULL)
)
UPDATE financiero.factura_legal_item fli
SET iva = c.iva_xml
FROM corregir c
WHERE fli.id = c.id
  AND fli.iva IS DISTINCT FROM c.iva_xml;
