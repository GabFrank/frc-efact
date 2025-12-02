-- Diagnóstico de discrepancia entre COUNT y query principal
-- Fechas: 2024-10-01 a 2024-11-01, Sucursal: 1

-- 1. Total de facturas según el COUNT simple
SELECT COUNT(*) as total_facturas_simple
FROM financiero.factura_legal c 
WHERE c.creado_en BETWEEN '2024-10-01' AND '2024-11-01' 
  AND c.sucursal_id = 1;

-- 2. Facturas que tienen timbrado_detalle_id NULL
SELECT COUNT(*) as facturas_sin_timbrado_detalle
FROM financiero.factura_legal c 
WHERE c.creado_en BETWEEN '2024-10-01' AND '2024-11-01' 
  AND c.sucursal_id = 1
  AND c.timbrado_detalle_id IS NULL;

-- 3. Facturas que tienen cliente_id NULL
SELECT COUNT(*) as facturas_sin_cliente
FROM financiero.factura_legal c 
WHERE c.creado_en BETWEEN '2024-10-01' AND '2024-11-01' 
  AND c.sucursal_id = 1
  AND c.cliente_id IS NULL;

-- 4. Facturas que NO tienen timbrado_detalle válido (no existe en la tabla)
SELECT COUNT(*) as facturas_timbrado_detalle_no_existe
FROM financiero.factura_legal c 
WHERE c.creado_en BETWEEN '2024-10-01' AND '2024-11-01' 
  AND c.sucursal_id = 1
  AND c.timbrado_detalle_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 
    FROM financiero.timbrado_detalle td 
    WHERE td.id = c.timbrado_detalle_id 
      AND td.sucursal_id = c.sucursal_id
  );

-- 5. Facturas que NO tienen cliente válido (cliente_id existe pero no hay registro en tabla cliente)
SELECT COUNT(*) as facturas_cliente_no_existe
FROM financiero.factura_legal c 
WHERE c.creado_en BETWEEN '2024-10-01' AND '2024-11-01' 
  AND c.sucursal_id = 1
  AND c.cliente_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 
    FROM personas.cliente cl 
    WHERE cl.id = c.cliente_id
  );

-- 6. Facturas que tienen cliente pero NO tienen persona asociada
SELECT COUNT(*) as facturas_cliente_sin_persona
FROM financiero.factura_legal c 
INNER JOIN personas.cliente cl ON c.cliente_id = cl.id
WHERE c.creado_en BETWEEN '2024-10-01' AND '2024-11-01' 
  AND c.sucursal_id = 1
  AND cl.persona_id IS NULL;

-- 7. Facturas que tienen cliente con persona_id pero la persona NO existe
SELECT COUNT(*) as facturas_persona_no_existe
FROM financiero.factura_legal c 
INNER JOIN personas.cliente cl ON c.cliente_id = cl.id
WHERE c.creado_en BETWEEN '2024-10-01' AND '2024-11-01' 
  AND c.sucursal_id = 1
  AND cl.persona_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 
    FROM personas.persona p 
    WHERE p.id = cl.persona_id
  );

-- 8. Facturas que NO tienen timbrado válido (timbrado_detalle existe pero timbrado no)
SELECT COUNT(*) as facturas_timbrado_no_existe
FROM financiero.factura_legal c 
INNER JOIN financiero.timbrado_detalle td ON c.timbrado_detalle_id = td.id AND c.sucursal_id = td.sucursal_id
WHERE c.creado_en BETWEEN '2024-10-01' AND '2024-11-01' 
  AND c.sucursal_id = 1
  AND NOT EXISTS (
    SELECT 1 
    FROM financiero.timbrado t 
    WHERE t.id = td.timbrado_id
  );

-- 9. Resumen: Facturas que se perderían con los JOINs actuales
WITH facturas_base AS (
  SELECT c.id, c.numero_factura, c.timbrado_detalle_id, c.cliente_id, c.sucursal_id
  FROM financiero.factura_legal c 
  WHERE c.creado_en BETWEEN '2024-10-01' AND '2024-11-01' 
    AND c.sucursal_id = 1
),
facturas_con_joins AS (
  SELECT DISTINCT fb.id
  FROM facturas_base fb
  INNER JOIN financiero.timbrado_detalle td ON fb.timbrado_detalle_id = td.id AND fb.sucursal_id = td.sucursal_id
  INNER JOIN financiero.timbrado t ON td.timbrado_id = t.id
  INNER JOIN empresarial.sucursal s ON fb.sucursal_id = s.id
  INNER JOIN personas.cliente c ON fb.cliente_id = c.id
  INNER JOIN personas.persona p ON c.persona_id = p.id
)
SELECT 
  (SELECT COUNT(*) FROM facturas_base) as total_facturas,
  (SELECT COUNT(*) FROM facturas_con_joins) as facturas_con_joins_exitosos,
  (SELECT COUNT(*) FROM facturas_base) - (SELECT COUNT(*) FROM facturas_con_joins) as facturas_perdidas;



