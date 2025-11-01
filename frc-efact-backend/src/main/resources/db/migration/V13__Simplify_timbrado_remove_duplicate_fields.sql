-- V13: Simplificar tabla timbrado eliminando campos duplicados con empresa
-- Fecha: 2024-01-XX
-- Descripción: Eliminar campos que están duplicados en la tabla empresa

-- Eliminar campos duplicados de la tabla timbrado
-- Estos datos se obtendrán de la relación con empresa

-- Eliminar datos de identificación de empresa
ALTER TABLE financiero.timbrado DROP COLUMN IF EXISTS razon_social;
ALTER TABLE financiero.timbrado DROP COLUMN IF EXISTS ruc;

-- Eliminar datos de contacto
ALTER TABLE financiero.timbrado DROP COLUMN IF EXISTS email;
ALTER TABLE financiero.timbrado DROP COLUMN IF EXISTS telefono;

-- Eliminar datos de domicilio fiscal
ALTER TABLE financiero.timbrado DROP COLUMN IF EXISTS domicilio_fiscal_departamento;
ALTER TABLE financiero.timbrado DROP COLUMN IF EXISTS domicilio_fiscal_ciudad;
ALTER TABLE financiero.timbrado DROP COLUMN IF EXISTS domicilio_fiscal_codigo_ciudad;
ALTER TABLE financiero.timbrado DROP COLUMN IF EXISTS domicilio_fiscal_localidad;
ALTER TABLE financiero.timbrado DROP COLUMN IF EXISTS domicilio_fiscal_barrio;
ALTER TABLE financiero.timbrado DROP COLUMN IF EXISTS domicilio_fiscal_direccion;

-- Eliminar datos de actividad económica
ALTER TABLE financiero.timbrado DROP COLUMN IF EXISTS cod_actividad_economica_principal;
ALTER TABLE financiero.timbrado DROP COLUMN IF EXISTS desc_actividad_economica_principal;
ALTER TABLE financiero.timbrado DROP COLUMN IF EXISTS list_codigo_actividad_economica_secundaria;
ALTER TABLE financiero.timbrado DROP COLUMN IF EXISTS list_descripcion_actividad_economica_secundaria;

-- Eliminar datos de sociedad
ALTER TABLE financiero.timbrado DROP COLUMN IF EXISTS tipo_sociedad;

-- Comentario: Los campos que permanecen en timbrado son:
-- - id, empresa_id, numero, is_electronico, csc_encrypted
-- - fecha_inicio, fecha_fin, activo
-- - campos de auditoría (creado_en, actualizado_en, etc.)
