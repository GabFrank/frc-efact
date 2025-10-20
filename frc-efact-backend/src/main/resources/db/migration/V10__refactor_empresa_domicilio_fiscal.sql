-- Migración para refactorizar domicilio fiscal de Empresa
-- Reemplaza campos de texto por relaciones con Ciudad y Barrio

-- Agregar nuevas columnas para las relaciones
ALTER TABLE empresa.empresa 
ADD COLUMN ciudad_id BIGINT,
ADD COLUMN barrio_id BIGINT;

-- Agregar foreign keys
ALTER TABLE empresa.empresa
ADD CONSTRAINT fk_empresa_ciudad 
FOREIGN KEY (ciudad_id) REFERENCES geografia.ciudad(id);

ALTER TABLE empresa.empresa
ADD CONSTRAINT fk_empresa_barrio 
FOREIGN KEY (barrio_id) REFERENCES geografia.barrio(id);

-- Migrar datos existentes (si los hay)
-- Intentar encontrar la ciudad por código
UPDATE empresa.empresa e
SET ciudad_id = (
    SELECT c.id 
    FROM geografia.ciudad c
    WHERE c.codigo = e.domicilio_fiscal_codigo_ciudad
    LIMIT 1
)
WHERE e.domicilio_fiscal_codigo_ciudad IS NOT NULL;

-- Eliminar columnas antiguas
ALTER TABLE empresa.empresa
DROP COLUMN IF EXISTS domicilio_fiscal_departamento,
DROP COLUMN IF EXISTS domicilio_fiscal_ciudad,
DROP COLUMN IF EXISTS domicilio_fiscal_codigo_ciudad,
DROP COLUMN IF EXISTS domicilio_fiscal_localidad,
DROP COLUMN IF EXISTS domicilio_fiscal_barrio;

-- Hacer ciudad_id NOT NULL después de la migración
ALTER TABLE empresa.empresa
ALTER COLUMN ciudad_id SET NOT NULL;

-- Crear índices para mejorar performance
CREATE INDEX idx_empresa_ciudad ON empresa.empresa(ciudad_id);
CREATE INDEX idx_empresa_barrio ON empresa.empresa(barrio_id);
