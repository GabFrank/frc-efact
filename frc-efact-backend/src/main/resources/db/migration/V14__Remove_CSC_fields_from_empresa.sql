-- V14: Eliminar campos CSC de tabla empresa
-- Fecha: 2024-01-XX
-- Descripción: Los campos CSC pertenecen específicamente a cada timbrado, no a la empresa general

-- Eliminar campos CSC de la tabla empresa
-- Estos campos deben estar solo en la tabla timbrado para timbrados electrónicos específicos

ALTER TABLE empresa.empresa DROP COLUMN IF EXISTS csc_id;
ALTER TABLE empresa.empresa DROP COLUMN IF EXISTS csc_encrypted;

-- Comentario: Los campos CSC ahora solo existen en financiero.timbrado
-- para timbrados electrónicos específicos, siguiendo la lógica de negocio correcta
