-- V22__Alter_documento_estado_to_varchar.sql
-- Ajusta las columnas de estado para utilizar VARCHAR en lugar de enums nativos de PostgreSQL.

BEGIN;

-- Documento Electronico
ALTER TABLE financiero.documento_electronico
    DROP COLUMN IF EXISTS estado;

ALTER TABLE financiero.documento_electronico
    ADD COLUMN estado VARCHAR(50) DEFAULT 'PENDIENTE' NOT NULL;

-- Lote DE
ALTER TABLE financiero.lote_de
    DROP COLUMN IF EXISTS estado;

ALTER TABLE financiero.lote_de
    ADD COLUMN estado VARCHAR(50) DEFAULT 'PENDIENTE' NOT NULL;

-- Evento Cancelacion DE
ALTER TABLE financiero.evento_cancelacion_de
    DROP COLUMN IF EXISTS estado;

ALTER TABLE financiero.evento_cancelacion_de
    ADD COLUMN estado VARCHAR(50) DEFAULT 'PENDIENTE' NOT NULL;

-- Remover los tipos ENUM originales si ya no se utilizan
DROP TYPE IF EXISTS financiero.estado_de_enum;
DROP TYPE IF EXISTS financiero.estado_lote_enum;
DROP TYPE IF EXISTS financiero.estado_evento_enum;

COMMIT;
