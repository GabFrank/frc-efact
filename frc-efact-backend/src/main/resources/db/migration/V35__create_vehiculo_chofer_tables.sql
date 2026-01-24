-- V35__create_vehiculo_chofer_tables.sql
-- Crear schema transporte si no existe
CREATE SCHEMA IF NOT EXISTS transporte;

-- Crear tabla Vehiculo
CREATE TABLE IF NOT EXISTS transporte.vehiculo (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    marca VARCHAR(100) NOT NULL,
    matricula VARCHAR(20) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    creado_por VARCHAR(50),
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_por VARCHAR(50),
    
    CONSTRAINT uk_vehiculo_empresa_matricula UNIQUE (empresa_id, matricula),
    CONSTRAINT fk_vehiculo_empresa FOREIGN KEY (empresa_id) REFERENCES empresa.empresa(id)
);

-- Crear índices para Vehiculo
CREATE INDEX IF NOT EXISTS idx_vehiculo_empresa ON transporte.vehiculo(empresa_id);
CREATE INDEX IF NOT EXISTS idx_vehiculo_activo ON transporte.vehiculo(activo);
CREATE INDEX IF NOT EXISTS idx_vehiculo_empresa_matricula ON transporte.vehiculo(empresa_id, matricula);

-- Comentarios para Vehiculo
COMMENT ON TABLE transporte.vehiculo IS 'Tabla de vehículos vinculados a empresas';
COMMENT ON COLUMN transporte.vehiculo.empresa_id IS 'ID de la empresa propietaria del vehículo';
COMMENT ON COLUMN transporte.vehiculo.marca IS 'Marca del vehículo';
COMMENT ON COLUMN transporte.vehiculo.matricula IS 'Matrícula del vehículo (única por empresa)';
COMMENT ON COLUMN transporte.vehiculo.activo IS 'Indica si el vehículo está activo';

-- Crear tabla Chofer
CREATE TABLE IF NOT EXISTS transporte.chofer (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    nombre VARCHAR(200) NOT NULL,
    documento VARCHAR(20),
    direccion TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    creado_por VARCHAR(50),
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_por VARCHAR(50),
    
    CONSTRAINT fk_chofer_empresa FOREIGN KEY (empresa_id) REFERENCES empresa.empresa(id)
);

-- Crear índices para Chofer
CREATE INDEX IF NOT EXISTS idx_chofer_empresa ON transporte.chofer(empresa_id);
CREATE INDEX IF NOT EXISTS idx_chofer_activo ON transporte.chofer(activo);
CREATE INDEX IF NOT EXISTS idx_chofer_nombre ON transporte.chofer(nombre);

-- Comentarios para Chofer
COMMENT ON TABLE transporte.chofer IS 'Tabla de choferes/conductores vinculados a empresas';
COMMENT ON COLUMN transporte.chofer.empresa_id IS 'ID de la empresa que emplea al chofer';
COMMENT ON COLUMN transporte.chofer.nombre IS 'Nombre completo del chofer';
COMMENT ON COLUMN transporte.chofer.documento IS 'Número de documento del chofer';
COMMENT ON COLUMN transporte.chofer.direccion IS 'Dirección del chofer';
COMMENT ON COLUMN transporte.chofer.activo IS 'Indica si el chofer está activo';

-- Agregar columnas opcionales a nota_remision para relaciones con Vehiculo y Chofer
ALTER TABLE financiero.nota_remision 
ADD COLUMN IF NOT EXISTS vehiculo_id BIGINT,
ADD COLUMN IF NOT EXISTS chofer_id BIGINT;

-- Agregar foreign keys para las relaciones
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                   WHERE constraint_schema = 'financiero' 
                   AND table_name = 'nota_remision' 
                   AND constraint_name = 'fk_nr_vehiculo') THEN
        ALTER TABLE financiero.nota_remision 
        ADD CONSTRAINT fk_nr_vehiculo FOREIGN KEY (vehiculo_id) REFERENCES transporte.vehiculo(id);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                   WHERE constraint_schema = 'financiero' 
                   AND table_name = 'nota_remision' 
                   AND constraint_name = 'fk_nr_chofer') THEN
        ALTER TABLE financiero.nota_remision 
        ADD CONSTRAINT fk_nr_chofer FOREIGN KEY (chofer_id) REFERENCES transporte.chofer(id);
    END IF;
END $$;

-- Crear índices para las relaciones
CREATE INDEX IF NOT EXISTS idx_nr_vehiculo ON financiero.nota_remision(vehiculo_id);
CREATE INDEX IF NOT EXISTS idx_nr_chofer ON financiero.nota_remision(chofer_id);

-- Comentarios para las nuevas columnas
COMMENT ON COLUMN financiero.nota_remision.vehiculo_id IS 'ID del vehículo (opcional, mantiene compatibilidad con campos legacy)';
COMMENT ON COLUMN financiero.nota_remision.chofer_id IS 'ID del chofer (opcional, mantiene compatibilidad con campos legacy)';
