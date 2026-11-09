-- V38: normalizar productos.producto.unidad_medida al catálogo real de SIFEN
--
-- El selector del formulario de producto ofrecía 14 opciones inventadas, de las cuales solo
-- CUATRO existen en TcUniMed (el catálogo de la SET): UNI (77), ML (88), M2 (109) y M3 (110).
-- Las otras diez se guardaban en la base y, al emitir, caían en el catch y salían como UNI.
--
-- Además el formulario forzaba mayúsculas, y como los códigos kg, g, m, ml, ha, racion, pm, Hs,
-- Km, Mi, Ya, Se y Di son minúsculas o mixtas, eran estructuralmente inalcanzables: aunque se
-- eligiera "kilogramo", se guardaba "KG" y el mapeo fallaba.
--
-- Esta migración reescribe los valores guardados a los códigos reales.
--
--   KG   → kg    (83)   Kilogramos
--   G    → g     (86)   Gramos
--   L    → LT    (89)   Litros
--   M    → m     (87)   Metros
--   H    → Hs   (100)   Hora
--   SERV → UNI   (77)   sin equivalente en SIFEN
--   PAR  → UNI   (77)   sin equivalente en SIFEN
--   CAJ  → UNI   (77)   sin equivalente en SIFEN
--   BOL  → UNI   (77)   sin equivalente en SIFEN
--   TUB  → UNI   (77)   sin equivalente en SIFEN
--
-- Los cinco últimos ya se venían emitiendo como UNI al no existir en el catálogo, así que la
-- migración NO cambia ningún documento respecto del comportamiento actual: solo hace explícito
-- en los datos lo que hasta ahora pasaba por accidente en el código.
--
-- ⚠️ CUIDADO CON EL CASE. ML (88) es Mililitros y ml (660) es Metro lineal: dos unidades
-- distintas que solo difieren en mayúsculas. Por eso todas las comparaciones de abajo son
-- EXACTAS (=), nunca ILIKE ni upper(). Un matcheo case-insensitive convertiría metros lineales
-- en mililitros sin que nadie se entere.
--
-- Idempotente: los valores ya normalizados no matchean ninguna condición.

-- 1) Alias que tienen equivalente exacto en el catálogo
UPDATE productos.producto SET unidad_medida = 'kg' WHERE unidad_medida = 'KG';
UPDATE productos.producto SET unidad_medida = 'g'  WHERE unidad_medida = 'G';
UPDATE productos.producto SET unidad_medida = 'LT' WHERE unidad_medida = 'L';
UPDATE productos.producto SET unidad_medida = 'm'  WHERE unidad_medida = 'M';
UPDATE productos.producto SET unidad_medida = 'Hs' WHERE unidad_medida = 'H';

-- 2) Sin equivalente en SIFEN. Ya se emitían como UNI.
UPDATE productos.producto
SET unidad_medida = 'UNI'
WHERE unidad_medida IN ('SERV', 'PAR', 'CAJ', 'BOL', 'TUB');

-- 3) Reportar lo que quedó fuera del catálogo
--
--    No se toca automáticamente: un valor desconocido puede venir de una importación por Excel y
--    solo el usuario sabe qué unidad quiso decir. Se emite como UNI (igual que hoy) y se avisa
--    con los datos concretos para poder corregirlo desde la UI.
DO $$
DECLARE
    fuera_de_catalogo TEXT;
BEGIN
    SELECT string_agg(DISTINCT unidad_medida, ', ')
    INTO fuera_de_catalogo
    FROM productos.producto
    WHERE unidad_medida IS NOT NULL
      AND unidad_medida <> ''
      AND unidad_medida NOT IN (
          -- Los 34 códigos de TcUniMed, por nombre de constante
          'm', 'CPM', 'UI', 'M3', 'UNI', 'g', 'LT', 'MG', 'CM', 'CM2', 'CM3', 'PUL', 'MM2',
          'kg_m2', 'AA', 'ME', 'TN', 'Hs', 'Mi', 'DET', 'Ya', 'MT', 'M2', 'MM', 'Se', 'Di',
          'kg', 'ML', 'Km', 'ml', 'GL', 'pm', 'ha', 'racion',
          -- y las dos abreviaturas que difieren del nombre de la constante
          'kg/m2', 'ración'
      );

    IF fuera_de_catalogo IS NOT NULL THEN
        RAISE WARNING 'V38: hay productos con unidades que no están en el catálogo de SIFEN (%). Se emiten como UNI; corregirlas desde el formulario de producto.',
            fuera_de_catalogo;
    ELSE
        RAISE NOTICE 'V38: todas las unidades de medida quedaron dentro del catálogo de SIFEN.';
    END IF;
END $$;

COMMENT ON COLUMN productos.producto.unidad_medida IS
    'Código de unidad de medida de SIFEN (cUniMed). Debe coincidir EXACTAMENTE, respetando mayúsculas y minúsculas, con una constante de TcUniMed o con su abreviatura. Ojo: ML (88) es Mililitros y ml (660) es Metro lineal.';
