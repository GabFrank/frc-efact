-- Migración para corregir el cálculo de totales en facturas existentes
-- Problema: Se estaba sumando el IVA al total cuando el total ya incluye el IVA
-- Solución: El total parcial debe ser la suma de los totales de los items (sin sumar IVA)
--           El IVA se calcula como: total / 21 para 5% y total / 11 para 10%

-- Script para corregir todas las facturas existentes
-- Recalcula los totales parciales y el IVA basándose en los items de cada factura

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

COMMENT ON TABLE financiero.factura_legal IS 'Facturas legales emitidas con desglose de IVA. NOTA: El total del item ya incluye el IVA, por lo que el total parcial es la suma de los totales de los items. El IVA se calcula como total/21 para 5% y total/11 para 10%.';

