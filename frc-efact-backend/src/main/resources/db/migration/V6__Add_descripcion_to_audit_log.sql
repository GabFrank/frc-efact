-- V6 Migration: Add descripcion column to audit_log table
-- This migration adds the missing descripcion column to the audit_log table

-- Add descripcion column to auditoria.audit_log
ALTER TABLE auditoria.audit_log 
ADD COLUMN descripcion TEXT;

-- Add comment for the new column
COMMENT ON COLUMN auditoria.audit_log.descripcion IS 'Descripción adicional de la acción realizada';