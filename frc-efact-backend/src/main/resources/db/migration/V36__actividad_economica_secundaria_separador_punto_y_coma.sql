-- V36: migrar el separador de las listas de actividad económica secundaria de "," a ";"
--
-- Motivo: las descripciones oficiales del catálogo de la SET contienen comas
-- (ej. "Otras actividades profesionales, científicas y técnicas n.c.p."). Al estar
-- guardadas como lista separada por comas, el split partía la descripción al medio y
-- desalineaba el pareo código↔descripción en SifenService.construirActividadesEconomicas().
-- Resultado: la SET rechazaba el DE con 1262 "Descripción de la actividad económica no
-- corresponde al código". El código ahora splitea/joinea por ";", que no aparece en el
-- catálogo de la SET.
--
-- Idempotente: si ya no quedan comas, los UPDATE no afectan filas.

-- 1) Códigos: la coma es SIEMPRE separador. El XSD de SIFEN restringe cActEco a
--    [0-9A-Z]{1,8} (tcActEco en DE_Types_v150.xsd), así que un código nunca puede
--    contener una coma. Conversión incondicional.
UPDATE empresa.empresa
SET list_codigo_actividad_economica_secundaria =
        replace(list_codigo_actividad_economica_secundaria, ',', ';')
WHERE list_codigo_actividad_economica_secundaria LIKE '%,%';

-- 2) Descripciones: acá la coma es ambigua — puede ser separador o parte del texto.
--    Solo se convierte cuando la cantidad de comas en las descripciones coincide con la
--    cantidad de separadores en los códigos (que ya son ";" tras el paso 1). Si coinciden,
--    todas las comas son separadores y la conversión es segura.
--    Las filas donde NO coinciden tienen al menos una coma dentro de una descripción: su
--    dato ya está ambiguo y no se puede reconstruir automáticamente, así que se dejan
--    intactas para corrección manual (ver la query de diagnóstico más abajo).
UPDATE empresa.empresa
SET list_descripcion_actividad_economica_secundaria =
        replace(list_descripcion_actividad_economica_secundaria, ',', ';')
WHERE list_descripcion_actividad_economica_secundaria LIKE '%,%'
  AND list_codigo_actividad_economica_secundaria IS NOT NULL
  AND (length(list_descripcion_actividad_economica_secundaria)
       - length(replace(list_descripcion_actividad_economica_secundaria, ',', '')))
      = (length(list_codigo_actividad_economica_secundaria)
         - length(replace(list_codigo_actividad_economica_secundaria, ';', '')));

-- Diagnóstico post-migración: si esta query devuelve filas, esas empresas quedaron con
-- una coma dentro de una descripción y hay que reescribir el campo a mano separando con ";".
--
--   SELECT id, razon_social,
--          list_codigo_actividad_economica_secundaria      AS cods,
--          list_descripcion_actividad_economica_secundaria AS descs
--   FROM empresa.empresa
--   WHERE list_descripcion_actividad_economica_secundaria LIKE '%,%';
--
-- Y para verificar que el pareo quedó alineado en todas las empresas
-- (n_cods debe ser igual a n_descs):
--
--   SELECT id, razon_social,
--          array_length(string_to_array(list_codigo_actividad_economica_secundaria, ';'), 1)      AS n_cods,
--          array_length(string_to_array(list_descripcion_actividad_economica_secundaria, ';'), 1) AS n_descs
--   FROM empresa.empresa
--   WHERE list_codigo_actividad_economica_secundaria IS NOT NULL;
