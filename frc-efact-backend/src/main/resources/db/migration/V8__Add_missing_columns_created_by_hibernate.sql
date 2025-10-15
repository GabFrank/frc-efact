-- V8 Migration: Add missing columns that were created automatically by Hibernate
-- This migration ensures all environments have the same database structure

-- Add missing columns to empresa.empresa table
ALTER TABLE empresa.empresa 
ADD COLUMN IF NOT EXISTS csc_encrypted TEXT;

ALTER TABLE empresa.empresa 
ADD COLUMN IF NOT EXISTS csc_id VARCHAR(50);

ALTER TABLE empresa.empresa 
ADD COLUMN IF NOT EXISTS sifen_ambiente VARCHAR(20);

-- Add missing column to financiero.timbrado_detalle table
ALTER TABLE financiero.timbrado_detalle 
ADD COLUMN IF NOT EXISTS version BIGINT;

-- Add comments for the new columns
COMMENT ON COLUMN empresa.empresa.csc_encrypted IS 'Código de Seguridad del Contribuyente encriptado con AES-256';
COMMENT ON COLUMN empresa.empresa.csc_id IS 'Identificador del CSC para timbrados electrónicos';
COMMENT ON COLUMN empresa.empresa.sifen_ambiente IS 'Ambiente SIFEN (test/production)';
COMMENT ON COLUMN financiero.timbrado_detalle.version IS 'Versión para control de concurrencia optimista';