ALTER TABLE financiero.nota_remision ADD COLUMN fecha_estimada_factura DATE;
COMMENT ON COLUMN financiero.nota_remision.fecha_estimada_factura IS 'Fecha estimada en la que se emitirá la factura relacionada al traslado (dFecEm)';
