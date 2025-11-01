-- Migración para refactorizar ubicación de TimbradoDetalle
-- Reemplaza campos de texto por relaciones con Ciudad y Barrio

-- Agregar nuevas columnas para las relaciones
ALTER TABLE financiero.timbrado_detalle 
ADD COLUMN ciudad_id BIGINT,
ADD COLUMN barrio_id BIGINT;

-- Agregar foreign keys
ALTER TABLE financiero.timbrado_detalle
ADD CONSTRAINT fk_timbrado_detalle_ciudad 
FOREIGN KEY (ciudad_id) REFERENCES geografia.ciudad(id);

ALTER TABLE financiero.timbrado_detalle
ADD CONSTRAINT fk_timbrado_detalle_barrio 
FOREIGN KEY (barrio_id) REFERENCES geografia.barrio(id);

-- Migrar datos existentes (si los hay)
-- Intentar encontrar la ciudad por código
UPDATE financiero.timbrado_detalle td
SET ciudad_id = (
    SELECT c.id 
    FROM geografia.ciudad c
    WHERE c.codigo = td.codigo_ciudad
    LIMIT 1
)
WHERE td.codigo_ciudad IS NOT NULL;

-- Eliminar columnas antiguas
ALTER TABLE financiero.timbrado_detalle
DROP COLUMN IF EXISTS departamento,
DROP COLUMN IF EXISTS ciudad,
DROP COLUMN IF EXISTS codigo_ciudad,
DROP COLUMN IF EXISTS localidad,
DROP COLUMN IF EXISTS barrio;

-- Hacer ciudad_id NOT NULL después de la migración
ALTER TABLE financiero.timbrado_detalle
ALTER COLUMN ciudad_id SET NOT NULL;

-- Crear índices para mejorar performance
CREATE INDEX IF NOT EXISTS idx_timbrado_detalle_ciudad ON financiero.timbrado_detalle(ciudad_id);
CREATE INDEX IF NOT EXISTS idx_timbrado_detalle_barrio ON financiero.timbrado_detalle(barrio_id);

