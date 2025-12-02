-- Add moneda_extranjera and cambio fields to factura_legal table
-- These fields are used for foreign currency invoices according to SIFEN v150 specification

ALTER TABLE financiero.factura_legal ADD COLUMN moneda_extranjera VARCHAR(3) DEFAULT NULL;

ALTER TABLE financiero.factura_legal ADD COLUMN cambio NUMERIC(10,4) DEFAULT NULL;

-- Add comments to document the fields
COMMENT ON COLUMN financiero.factura_legal.moneda_extranjera IS 'Código ISO 4217 de la moneda (USD, EUR, BRL, etc.). NULL o "PYG" indica moneda local.';

COMMENT ON COLUMN financiero.factura_legal.cambio IS 'Tipo de cambio respecto al guaraní. Solo aplica cuando moneda_extranjera no es NULL ni "PYG".';

