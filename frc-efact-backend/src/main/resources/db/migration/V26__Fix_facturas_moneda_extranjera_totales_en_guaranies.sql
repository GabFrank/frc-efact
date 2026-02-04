-- Migración para corregir facturas con moneda extranjera que tienen totales en moneda extranjera
-- Los totales deben estar siempre en guaraníes, independientemente de la moneda de la factura

-- Primero, identificar facturas con moneda extranjera que necesitan corrección
-- Si una factura tiene moneda extranjera y cambio, los totales deben estar en guaraníes
-- Si los totales parecen estar en moneda extranjera (valores muy pequeños), los convertimos

UPDATE financiero.factura_legal fl
SET 
    -- Recalcular totales desde items (que deben estar en guaraníes)
    total_parcial_0 = COALESCE((
        SELECT SUM(fli.total)
        FROM financiero.factura_legal_item fli
        LEFT JOIN productos.producto p ON fli.producto_id = p.id
        WHERE fli.factura_legal_id = fl.id AND (p.iva = 0 OR p.iva IS NULL)
    ), 0),
    
    total_parcial_5 = COALESCE((
        SELECT SUM(fli.total)
        FROM financiero.factura_legal_item fli
        LEFT JOIN productos.producto p ON fli.producto_id = p.id
        WHERE fli.factura_legal_id = fl.id AND p.iva = 5
    ), 0),
    
    total_parcial_10 = COALESCE((
        SELECT SUM(fli.total)
        FROM financiero.factura_legal_item fli
        LEFT JOIN productos.producto p ON fli.producto_id = p.id
        WHERE fli.factura_legal_id = fl.id AND p.iva = 10
    ), 0),
    
    iva_parcial_5 = COALESCE((
        SELECT SUM(fli.total / 21)
        FROM financiero.factura_legal_item fli
        LEFT JOIN productos.producto p ON fli.producto_id = p.id
        WHERE fli.factura_legal_id = fl.id AND p.iva = 5
    ), 0),
    
    iva_parcial_10 = COALESCE((
        SELECT SUM(fli.total / 11)
        FROM financiero.factura_legal_item fli
        LEFT JOIN productos.producto p ON fli.producto_id = p.id
        WHERE fli.factura_legal_id = fl.id AND p.iva = 10
    ), 0),
    
    total_parcial = COALESCE((
        SELECT SUM(fli.total)
        FROM financiero.factura_legal_item fli
        WHERE fli.factura_legal_id = fl.id
    ), 0),
    
    total_final = COALESCE((
        SELECT SUM(fli.total)
        FROM financiero.factura_legal_item fli
        WHERE fli.factura_legal_id = fl.id
    ), 0) - COALESCE(fl.descuento_final, 0)
WHERE 
    fl.moneda_extranjera IS NOT NULL 
    AND fl.moneda_extranjera != 'PYG'
    AND fl.cambio IS NOT NULL
    AND fl.cambio > 0
    AND fl.activo = true;

-- Redondear valores a 2 decimales
UPDATE financiero.factura_legal
SET 
    iva_parcial_5 = ROUND(iva_parcial_5, 2),
    iva_parcial_10 = ROUND(iva_parcial_10, 2),
    total_parcial_0 = ROUND(total_parcial_0, 2),
    total_parcial_5 = ROUND(total_parcial_5, 2),
    total_parcial_10 = ROUND(total_parcial_10, 2),
    total_parcial = ROUND(total_parcial, 2),
    total_final = ROUND(total_final, 2)
WHERE 
    moneda_extranjera IS NOT NULL 
    AND moneda_extranjera != 'PYG'
    AND cambio IS NOT NULL
    AND cambio > 0
    AND activo = true;

COMMENT ON COLUMN financiero.factura_legal.total_parcial IS 'Total parcial en guaraníes. Siempre en guaraníes, independientemente de la moneda de la factura.';
COMMENT ON COLUMN financiero.factura_legal.total_final IS 'Total final en guaraníes. Siempre en guaraníes, independientemente de la moneda de la factura.';
COMMENT ON COLUMN financiero.factura_legal.iva_parcial_5 IS 'IVA parcial 5% en guaraníes. Siempre en guaraníes, independientemente de la moneda de la factura.';
COMMENT ON COLUMN financiero.factura_legal.iva_parcial_10 IS 'IVA parcial 10% en guaraníes. Siempre en guaraníes, independientemente de la moneda de la factura.';

