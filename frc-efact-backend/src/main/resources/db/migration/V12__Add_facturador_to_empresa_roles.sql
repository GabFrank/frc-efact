-- V12 Migration: Add FACTURADOR role to empresa.usuario_empresa table
-- This migration adds FACTURADOR as a valid role for users within companies
-- FACTURADOR users can create invoices and manage electronic documents but cannot manage company users

-- Drop the existing CHECK constraint
ALTER TABLE empresa.usuario_empresa DROP CONSTRAINT usuario_empresa_rol_empresa_check;

-- Add the new CHECK constraint with FACTURADOR included
ALTER TABLE empresa.usuario_empresa
    ADD CONSTRAINT usuario_empresa_rol_empresa_check
    CHECK (rol_empresa IN ('ADMINISTRADOR', 'FACTURADOR', 'LECTOR'));

-- Update the comment to reflect the new role
COMMENT ON COLUMN empresa.usuario_empresa.rol_empresa IS 'Rol del usuario en la empresa (ADMINISTRADOR, FACTURADOR o LECTOR)';

