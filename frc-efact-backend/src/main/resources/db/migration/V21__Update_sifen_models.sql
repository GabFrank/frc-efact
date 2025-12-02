-- Actualiza modelos relacionados a SIFEN para multi-empresa y almacenamiento de respuestas

-- Empresa: almacenar CSC por empresa
ALTER TABLE empresa.empresa
    ADD COLUMN IF NOT EXISTS csc_id VARCHAR(50);

ALTER TABLE empresa.empresa
    ADD COLUMN IF NOT EXISTS csc_encrypted TEXT;

-- Timbrado: almacenar identificador de CSC
ALTER TABLE financiero.timbrado
    ADD COLUMN IF NOT EXISTS csc_id VARCHAR(50);

-- Documento electrónico: nuevos campos de tracking
ALTER TABLE financiero.documento_electronico
    ADD COLUMN IF NOT EXISTS respuesta_sifen TEXT;

ALTER TABLE financiero.documento_electronico
    ADD COLUMN IF NOT EXISTS protocolo_autorizacion VARCHAR(50);

ALTER TABLE financiero.documento_electronico
    ADD COLUMN IF NOT EXISTS fecha_estado_actualizado TIMESTAMP;

ALTER TABLE financiero.documento_electronico
    ADD COLUMN IF NOT EXISTS intentos INTEGER NOT NULL DEFAULT 0;

-- Asegurar que intentos existente tenga valor por defecto
ALTER TABLE financiero.documento_electronico
    ALTER COLUMN intentos SET DEFAULT 0;

-- Lote DE: almacenar código y mensaje de respuesta
ALTER TABLE financiero.lote_de
    ADD COLUMN IF NOT EXISTS codigo_respuesta VARCHAR(10);

ALTER TABLE financiero.lote_de
    ADD COLUMN IF NOT EXISTS mensaje_respuesta TEXT;

-- Extender enum de estado de lote con nuevos valores si aún no existen
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_type t
        JOIN pg_enum e ON t.oid = e.enumtypid
        WHERE t.typname = 'estado_lote_enum' AND e.enumlabel = 'PROCESADO'
    ) THEN
        ALTER TYPE financiero.estado_lote_enum ADD VALUE 'PROCESADO';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_type t
        JOIN pg_enum e ON t.oid = e.enumtypid
        WHERE t.typname = 'estado_lote_enum' AND e.enumlabel = 'ERROR_ENVIO'
    ) THEN
        ALTER TYPE financiero.estado_lote_enum ADD VALUE 'ERROR_ENVIO';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_type t
        JOIN pg_enum e ON t.oid = e.enumtypid
        WHERE t.typname = 'estado_lote_enum' AND e.enumlabel = 'ERROR_PERMANENTE'
    ) THEN
        ALTER TYPE financiero.estado_lote_enum ADD VALUE 'ERROR_PERMANENTE';
    END IF;
END
$$;








