-- Agregar campo tipo_contribuyente a la tabla empresa
-- PF = Persona Física, PJ = Persona Jurídica

ALTER TABLE empresa.empresa 
ADD COLUMN tipo_contribuyente VARCHAR(2) NOT NULL DEFAULT 'PF';

-- Agregar comentario para documentar los valores válidos
COMMENT ON COLUMN empresa.empresa.tipo_contribuyente IS 'Tipo de contribuyente: PF = Persona Física, PJ = Persona Jurídica';

-- Crear índice para mejorar consultas por tipo de contribuyente
CREATE INDEX idx_empresa_tipo_contribuyente ON empresa.empresa(tipo_contribuyente);