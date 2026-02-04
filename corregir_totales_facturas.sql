-- Script SQL para corregir manualmente los totales de las facturas existentes
-- Este script recalcula los totales parciales y el IVA basándose en los items de cada factura
-- 
-- Problema corregido: Se estaba sumando el IVA al total cuando el total ya incluye el IVA
-- Solución: El total parcial debe ser la suma de los totales de los items (sin sumar IVA)
--           El IVA se calcula como: total / 21 para 5% y total / 11 para 10%
--
-- Ejemplo: Si un item tiene total 30.000 Gs con IVA 5%:
--   - IVA = 30.000 / 21 = 1.428,57 Gs
--   - Total parcial 5 = 30.000 Gs (no 30.000 + 1.428,57)

BEGIN;

-- Crear una tabla temporal con los totales recalculados
WITH factura_totales AS (
    SELECT 
        fl.id AS factura_id,
        -- Sumar totales por tipo de IVA
        COALESCE(SUM(CASE WHEN COALESCE(p.iva, 0) = 0 THEN fli.total ELSE 0 END), 0) AS total_parcial_0,
        COALESCE(SUM(CASE WHEN p.iva = 5 THEN fli.total ELSE 0 END), 0) AS total_parcial_5,
        COALESCE(SUM(CASE WHEN p.iva = 10 THEN fli.total ELSE 0 END), 0) AS total_parcial_10,
        -- Calcular IVA correctamente: total / 21 para 5%, total / 11 para 10%
        COALESCE(SUM(CASE WHEN p.iva = 5 THEN fli.total / 21.0 ELSE 0 END), 0) AS iva_parcial_5,
        COALESCE(SUM(CASE WHEN p.iva = 10 THEN fli.total / 11.0 ELSE 0 END), 0) AS iva_parcial_10
    FROM financiero.factura_legal fl
    LEFT JOIN financiero.factura_legal_item fli ON fl.id = fli.factura_legal_id
    LEFT JOIN productos.producto p ON fli.producto_id = p.id
    WHERE fl.activo = true
    GROUP BY fl.id
)
-- Actualizar las facturas con los totales corregidos
UPDATE financiero.factura_legal fl
SET 
    total_parcial_0 = ft.total_parcial_0,
    total_parcial_5 = ft.total_parcial_5,
    total_parcial_10 = ft.total_parcial_10,
    iva_parcial_0 = 0, -- Siempre 0 para items exentos
    iva_parcial_5 = ROUND(ft.iva_parcial_5::numeric, 2),
    iva_parcial_10 = ROUND(ft.iva_parcial_10::numeric, 2),
    total_parcial = ft.total_parcial_0 + ft.total_parcial_5 + ft.total_parcial_10,
    total_final = (ft.total_parcial_0 + ft.total_parcial_5 + ft.total_parcial_10) - COALESCE(fl.descuento_final, 0),
    actualizado_en = CURRENT_TIMESTAMP
FROM factura_totales ft
WHERE fl.id = ft.factura_id;

-- Mostrar resumen de facturas corregidas
SELECT 
    COUNT(*) AS facturas_corregidas,
    SUM(total_parcial_5) AS total_parcial_5_corregido,
    SUM(total_parcial_10) AS total_parcial_10_corregido,
    SUM(iva_parcial_5) AS iva_parcial_5_corregido,
    SUM(iva_parcial_10) AS iva_parcial_10_corregido
FROM financiero.factura_legal
WHERE activo = true;

-- Verificar la factura con ID 15 como ejemplo
SELECT 
    id,
    numero_factura,
    total_parcial_5,
    iva_parcial_5,
    total_parcial_10,
    iva_parcial_10,
    total_parcial,
    total_final
FROM financiero.factura_legal
WHERE id = 15;

COMMIT;

