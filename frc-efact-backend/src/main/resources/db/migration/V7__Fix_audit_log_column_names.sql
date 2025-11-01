-- V7 Migration: Fix audit_log column names to match entity mapping
-- This migration renames columns to match the Java entity mapping

-- Rename entidad to entidad_tipo
ALTER TABLE auditoria.audit_log 
RENAME COLUMN entidad TO entidad_tipo;

-- Update comment for the renamed column
COMMENT ON COLUMN auditoria.audit_log.entidad_tipo IS 'Tipo de entidad afectada (ej: "Factura", "Cliente", "Producto")';

-- Rename creado_en to fecha_hora to match entity mapping
ALTER TABLE auditoria.audit_log 
RENAME COLUMN creado_en TO fecha_hora;

-- Update comment for the renamed column
COMMENT ON COLUMN auditoria.audit_log.fecha_hora IS 'Fecha y hora en que se realizó la acción';