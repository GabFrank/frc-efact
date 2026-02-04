-- Insertar Paraguay con ID 1 si no existe
INSERT INTO geografia.pais (id, codigo, nombre) 
SELECT 1, 'PY', 'PARAGUAY' 
WHERE NOT EXISTS (SELECT 1 FROM geografia.pais WHERE id = 1 OR codigo = 'PY');

-- Ajustar la secuencia para que el próximo ID auto-generado sea correcto
SELECT setval('geografia.pais_id_seq', (SELECT COALESCE(MAX(id), 0) FROM geografia.pais), true);
