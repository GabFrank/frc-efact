-- Migración para permitir valores NULL en rangos para timbrados electrónicos
-- Los timbrados electrónicos no requieren rangos de numeración

-- Eliminar constraints existentes
ALTER TABLE financiero.timbrado_detalle
DROP CONSTRAINT IF EXISTS chk_rango;

ALTER TABLE financiero.timbrado_detalle
DROP CONSTRAINT IF EXISTS chk_numero_actual;

-- Hacer las columnas nullable para permitir timbrados electrónicos sin rangos
ALTER TABLE financiero.timbrado_detalle
ALTER COLUMN cantidad DROP NOT NULL;

ALTER TABLE financiero.timbrado_detalle
ALTER COLUMN rango_desde DROP NOT NULL;

ALTER TABLE financiero.timbrado_detalle
ALTER COLUMN rango_hasta DROP NOT NULL;

ALTER TABLE financiero.timbrado_detalle
ALTER COLUMN numero_actual DROP NOT NULL;

-- Recrear constraints que permitan NULL o valores válidos
-- Para timbrados no electrónicos: rango_desde < rango_hasta
-- Para timbrados electrónicos: pueden ser NULL
ALTER TABLE financiero.timbrado_detalle
ADD CONSTRAINT chk_rango CHECK (
    (rango_desde IS NULL AND rango_hasta IS NULL) OR
    (rango_desde IS NOT NULL AND rango_hasta IS NOT NULL AND rango_desde < rango_hasta)
);

-- Constraint para numero_actual: debe estar en el rango si hay rango, o puede ser NULL si no hay rango
ALTER TABLE financiero.timbrado_detalle
ADD CONSTRAINT chk_numero_actual CHECK (
    (rango_desde IS NULL AND rango_hasta IS NULL AND numero_actual IS NULL) OR
    (rango_desde IS NOT NULL AND rango_hasta IS NOT NULL AND numero_actual IS NOT NULL AND numero_actual >= rango_desde AND numero_actual <= rango_hasta)
);

COMMENT ON COLUMN financiero.timbrado_detalle.cantidad IS 'Cantidad de números disponibles. NULL para timbrados electrónicos';
COMMENT ON COLUMN financiero.timbrado_detalle.rango_desde IS 'Número inicial del rango autorizado. NULL para timbrados electrónicos';
COMMENT ON COLUMN financiero.timbrado_detalle.rango_hasta IS 'Número final del rango autorizado. NULL para timbrados electrónicos';
COMMENT ON COLUMN financiero.timbrado_detalle.numero_actual IS 'Número actual de factura. NULL para timbrados electrónicos';

