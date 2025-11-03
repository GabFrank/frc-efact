-- Migración para agregar campos SIFEN v1.50 a la tabla cliente
-- Según Manual Técnico SIFEN v1.50 - Bloque Receptor (B400-B499)
-- Nota: El dígito verificador (B405 dDVRec) está incluido en el RUC (B404 dRucRec)
-- en formato 99999999-9, por lo que no se almacena en una columna separada

-- Agregar columna para número de casa (B411 dNumCasRec)
ALTER TABLE clientes.cliente
ADD COLUMN numero_casa VARCHAR(50);

-- Agregar columna para celular (B413 dCelRec)
ALTER TABLE clientes.cliente
ADD COLUMN celular VARCHAR(50);

-- Agregar columna para tipo de cliente SIFEN (combina naturaleza, tipo contribuyente y tipo operación)
ALTER TABLE clientes.cliente
ADD COLUMN tipo_cliente_sifen VARCHAR(50);

-- Agregar comentario para tipo_cliente_sifen
COMMENT ON COLUMN clientes.cliente.tipo_cliente_sifen IS 'Tipo de cliente según SIFEN v1.50. Valores: PERSONA_FISICA, PERSONA_JURIDICA, NO_CONTRIBUYENTE, EXTRANJERO, GUBERNAMENTAL';

-- Agregar columna para país del receptor (B415 cPaisRec)
ALTER TABLE clientes.cliente
ADD COLUMN pais_id BIGINT;

-- Agregar foreign key para país
ALTER TABLE clientes.cliente
ADD CONSTRAINT fk_cliente_pais
FOREIGN KEY (pais_id) REFERENCES geografia.pais(id);

-- Agregar columna para ciudad del receptor (B410 cCiuRec)
-- A través de ciudad se puede acceder a distrito y departamento
ALTER TABLE clientes.cliente
ADD COLUMN ciudad_id BIGINT;

-- Agregar foreign key para ciudad
ALTER TABLE clientes.cliente
ADD CONSTRAINT fk_cliente_ciudad
FOREIGN KEY (ciudad_id) REFERENCES geografia.ciudad(id);

-- Agregar índices para mejorar búsquedas
CREATE INDEX idx_cliente_ciudad ON clientes.cliente(ciudad_id);
CREATE INDEX idx_cliente_pais ON clientes.cliente(pais_id);
CREATE INDEX idx_cliente_tipo_sifen ON clientes.cliente(tipo_cliente_sifen);

-- Agregar comentarios a las nuevas columnas
COMMENT ON COLUMN clientes.cliente.numero_casa IS 'Número de casa (B411 dNumCasRec)';
COMMENT ON COLUMN clientes.cliente.celular IS 'Número de celular (B413 dCelRec)';
COMMENT ON COLUMN clientes.cliente.pais_id IS 'País del receptor (B415 cPaisRec). Referencia a geografia.pais';
COMMENT ON COLUMN clientes.cliente.ciudad_id IS 'Ciudad del receptor (B410 cCiuRec). A través de ciudad se accede a distrito y departamento.';

-- Migrar datos existentes: establecer país por defecto como Paraguay
-- Primero verificamos que existe el país Paraguay en la tabla geografia.pais
-- Si no existe, se debe crear previamente
UPDATE clientes.cliente c
SET pais_id = (
    SELECT p.id 
    FROM geografia.pais p 
    WHERE p.codigo = 'PY' 
    LIMIT 1
)
WHERE c.pais_id IS NULL;

-- Migrar datos existentes: intentar inferir tipo_cliente_sifen desde tipo_contribuyente
-- Persona Física Contribuyente
UPDATE clientes.cliente
SET tipo_cliente_sifen = 'PERSONA_FISICA'
WHERE tributa = true 
  AND tipo_contribuyente = 'PF'
  AND tipo_cliente_sifen IS NULL;

-- Persona Jurídica Contribuyente
UPDATE clientes.cliente
SET tipo_cliente_sifen = 'PERSONA_JURIDICA'
WHERE tributa = true 
  AND tipo_contribuyente = 'PJ'
  AND tipo_cliente_sifen IS NULL;

-- Entidad Gubernamental
UPDATE clientes.cliente
SET tipo_cliente_sifen = 'GUBERNAMENTAL'
WHERE tributa = true 
  AND tipo_contribuyente = 'EG'
  AND tipo_cliente_sifen IS NULL;

-- No Contribuyente (Consumidor Final) - por defecto para clientes que no tributan
UPDATE clientes.cliente
SET tipo_cliente_sifen = 'NO_CONTRIBUYENTE'
WHERE tributa = false 
  AND tipo_cliente_sifen IS NULL;

-- Si aún hay nulls, establecer NO_CONTRIBUYENTE como valor por defecto
UPDATE clientes.cliente
SET tipo_cliente_sifen = 'NO_CONTRIBUYENTE'
WHERE tipo_cliente_sifen IS NULL;

