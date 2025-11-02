-- Migración para convertir tipo_transaccion de enum a VARCHAR
-- Esto es necesario porque Hibernate funciona mejor con VARCHAR para enums usando @Enumerated(EnumType.STRING)

-- Convertir la columna tipo_transaccion de enum a VARCHAR
ALTER TABLE productos.producto 
ALTER COLUMN tipo_transaccion TYPE VARCHAR(50) USING tipo_transaccion::text;

-- Eliminar el tipo enum que ya no se necesita
DROP TYPE IF EXISTS productos.tipo_transaccion_producto_enum;

-- Actualizar comentario de la columna
COMMENT ON COLUMN productos.producto.tipo_transaccion IS 'Tipo de transacción según SIFEN v1.50 (D011 iTipTra). Valores: VENTA_MERCADERIA, PRESTACION_SERVICIOS, MIXTO, VENTA_ACTIVO_FIJO, VENTA_DIVISAS, COMPRA_DIVISAS, PROMOCION_MUESTRAS, DONACION, ANTICIPO, COMPRA_PRODUCTOS, COMPRA_SERVICIOS, VENTA_CREDITO_FISCAL, MUESTRAS_MEDICAS';

