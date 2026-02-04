-- V33__add_transportista_fields_to_nota_remision.sql
ALTER TABLE financiero.nota_remision 
ADD COLUMN transportista_nombre VARCHAR(200),
ADD COLUMN transportista_ruc VARCHAR(20),
ADD COLUMN transportista_direccion TEXT;
