-- Migración para agregar tipo de transacción SIFEN y unidad de medida a productos
-- Según Manual Técnico SIFEN v1.50 - Campos D011 (iTipTra) y D012 (dDesTipTra)

-- Crear ENUM type para tipo de transacción de producto
CREATE TYPE productos.tipo_transaccion_producto_enum AS ENUM (
    'VENTA_MERCADERIA',
    'PRESTACION_SERVICIOS',
    'MIXTO',
    'VENTA_ACTIVO_FIJO',
    'VENTA_DIVISAS',
    'COMPRA_DIVISAS',
    'PROMOCION_MUESTRAS',
    'DONACION',
    'ANTICIPO',
    'COMPRA_PRODUCTOS',
    'COMPRA_SERVICIOS',
    'VENTA_CREDITO_FISCAL',
    'MUESTRAS_MEDICAS'
);

COMMENT ON TYPE productos.tipo_transaccion_producto_enum IS 'Tipos de transacción u operación comercial según Manual Técnico SIFEN v1.50';

-- Agregar columna tipo_transaccion a la tabla producto
ALTER TABLE productos.producto
ADD COLUMN tipo_transaccion productos.tipo_transaccion_producto_enum;

-- Agregar columna unidad_medida para bienes físicos y servicios
-- Ejemplos: "UNI" (unidad), "KG" (kilogramo), "L" (litro), "H" (hora), "SERV" (servicio)
ALTER TABLE productos.producto
ADD COLUMN unidad_medida VARCHAR(10);

-- Agregar índice para búsqueda por tipo de transacción
CREATE INDEX idx_producto_tipo_transaccion ON productos.producto(tipo_transaccion);

-- Agregar comentarios
COMMENT ON COLUMN productos.producto.tipo_transaccion IS 'Tipo de transacción según SIFEN v1.50 (D011 iTipTra)';
COMMENT ON COLUMN productos.producto.unidad_medida IS 'Unidad de medida del producto. Ejemplos: UNI, KG, L, H, SERV';

-- Actualizar productos existentes con valores por defecto
-- Asignar VENTA_MERCADERIA como tipo por defecto para productos existentes
UPDATE productos.producto
SET tipo_transaccion = 'VENTA_MERCADERIA'
WHERE tipo_transaccion IS NULL;

UPDATE productos.producto
SET unidad_medida = 'UNI'
WHERE unidad_medida IS NULL;

-- Hacer tipo_transaccion NOT NULL después de asignar valores por defecto
ALTER TABLE productos.producto
ALTER COLUMN tipo_transaccion SET NOT NULL;

-- Hacer unidad_medida NOT NULL después de asignar valores por defecto
ALTER TABLE productos.producto
ALTER COLUMN unidad_medida SET NOT NULL;

-- Verificar que todos los productos tengan valores válidos
DO $$
BEGIN
    -- Verificar que no haya productos sin tipo_transaccion
    IF EXISTS (SELECT 1 FROM productos.producto WHERE tipo_transaccion IS NULL) THEN
        RAISE EXCEPTION 'Existen productos sin tipo_transaccion asignado';
    END IF;
    
    -- Verificar que no haya productos sin unidad_medida
    IF EXISTS (SELECT 1 FROM productos.producto WHERE unidad_medida IS NULL OR unidad_medida = '') THEN
        RAISE EXCEPTION 'Existen productos sin unidad_medida asignada';
    END IF;
END $$;

