-- V37: persistir la tasa de IVA en cada ítem de factura legal
--
-- ¿Por qué? `financiero.factura_legal_item` era la ÚNICA tabla de ítems sin columna `iva`.
-- Sus hermanas ya la tienen desde la V28:
--
--   nota_credito_item   iva INTEGER NOT NULL DEFAULT 10   ✓
--   nota_debito_item    iva INTEGER NOT NULL DEFAULT 10   ✓
--   nota_remision_item  (sin iva, correcto: la NRE no lleva precios ni IVA)
--   factura_legal_item  ← faltaba
--
-- Al no estar guardada, la tasa se derivaba del catálogo de productos en tiempo de lectura,
-- en los dos extremos del sistema:
--
--   SifenService:1652  producto.getIva(), y si el producto es null cae a 10 por default
--   SifenService:2086  item.getIva()   ← el path de nota de crédito, que lo hace bien
--
-- Eso produce tres fallas reales:
--
-- 1) El formulario de factura solo carga los primeros 20 productos de la empresa
--    (getByEmpresa con size=20 por default, ordenado por descripción). Si el producto de un
--    ítem queda fuera de esa página, el front resolvía `iva: producto?.iva ?? 0` y la factura
--    entera se mostraba como EXENTA. Guardar desde esa pantalla sobreescribía los totales
--    correctos de la cabecera con ceros.
-- 2) Los documentos históricos no eran reproducibles: cambiar el IVA de un producto alteraba
--    en silencio cómo se re-renderiza y cómo se regenera el KuDE de toda factura pasada,
--    quedando distinto de lo que SIFEN aprobó. El DE es inmutable allá; nuestra vista derivaba.
-- 3) Si el producto era null al emitir, se emitía con tasa 10 sin ningún aviso.
--
-- La tasa de IVA es un dato del hecho imponible en el momento de la emisión, no una propiedad
-- viva del catálogo. Va guardada en el ítem, igual que la descripción y el precio unitario —que
-- ya se snapshotean acá justamente por la misma razón.
--
-- Tipo INTEGER (no SMALLINT) para ser consistente con nota_credito_item y nota_debito_item.
-- Idempotente: se puede correr sobre una base que ya la tenga.

-- ─────────────────────────────────────────────────────────────────────────────────────────
-- 1) La columna, nullable por ahora para poder rellenarla
-- ─────────────────────────────────────────────────────────────────────────────────────────
ALTER TABLE financiero.factura_legal_item
    ADD COLUMN IF NOT EXISTS iva INTEGER;

-- ─────────────────────────────────────────────────────────────────────────────────────────
-- 2) Backfill desde el catálogo de productos
--
--    Es la mejor fuente disponible para las filas existentes: es exactamente el valor que el
--    sistema venía usando al renderizar y al emitir. Para la enorme mayoría de los ítems el
--    producto no cambió de tasa desde que se facturó, así que el snapshot queda fiel.
-- ─────────────────────────────────────────────────────────────────────────────────────────
UPDATE financiero.factura_legal_item fli
SET iva = p.iva
FROM productos.producto p
WHERE fli.producto_id = p.id
  AND fli.iva IS NULL;

-- ─────────────────────────────────────────────────────────────────────────────────────────
-- 3) Ítems sin producto: derivar de los totales de la cabecera cuando no hay ambigüedad
--
--    `factura_legal` sí persiste total_parcial_0 / _5 / _10. Si la factura tiene exactamente
--    UN bucket con monto, todos sus ítems son de esa tasa y la inferencia es exacta. Cubre las
--    facturas de tasa única, que son la mayoría. Esto es mejor que asumir 10: usa lo que el
--    sistema efectivamente calculó y guardó cuando la factura se emitió.
-- ─────────────────────────────────────────────────────────────────────────────────────────
UPDATE financiero.factura_legal_item fli
SET iva = CASE
              WHEN COALESCE(fl.total_parcial_10, 0) <> 0 THEN 10
              WHEN COALESCE(fl.total_parcial_5, 0) <> 0 THEN 5
              ELSE 0
          END
FROM financiero.factura_legal fl
WHERE fli.factura_legal_id = fl.id
  AND fli.iva IS NULL
  -- Exactamente un bucket con monto ⇒ sin ambigüedad
  AND (CASE WHEN COALESCE(fl.total_parcial_0, 0) <> 0 THEN 1 ELSE 0 END
     + CASE WHEN COALESCE(fl.total_parcial_5, 0) <> 0 THEN 1 ELSE 0 END
     + CASE WHEN COALESCE(fl.total_parcial_10, 0) <> 0 THEN 1 ELSE 0 END) = 1;

-- ─────────────────────────────────────────────────────────────────────────────────────────
-- 4) Reportar lo que quedó sin resolver ANTES de aplicarle un default
--
--    Un ítem que llega acá no tiene producto y pertenece a una factura con más de una tasa:
--    su tasa no se puede reconstruir automáticamente. Se avisa por log con los IDs concretos
--    para que se revise a mano contra el XML del DE, que es la fuente autoritativa.
-- ─────────────────────────────────────────────────────────────────────────────────────────
DO $$
DECLARE
    pendientes INTEGER;
    ids TEXT;
BEGIN
    SELECT count(*), string_agg(id::TEXT, ', ' ORDER BY id)
    INTO pendientes, ids
    FROM financiero.factura_legal_item
    WHERE iva IS NULL;

    IF pendientes > 0 THEN
        RAISE WARNING 'V37: % ítem(s) de factura sin IVA reconstruible (sin producto y factura con varias tasas). Se les asigna 10; verificar contra el XML del DE. IDs: %',
            pendientes, ids;
    ELSE
        RAISE NOTICE 'V37: todos los ítems de factura quedaron con su IVA reconstruido.';
    END IF;
END $$;

UPDATE financiero.factura_legal_item
SET iva = 10
WHERE iva IS NULL;

-- ─────────────────────────────────────────────────────────────────────────────────────────
-- 5) Cerrar el invariante
--
--    El DEFAULT 10 replica el de las tablas hermanas: es la tasa general en Paraguay y evita
--    que un INSERT que omita la columna falle. La aplicación siempre la manda explícita.
-- ─────────────────────────────────────────────────────────────────────────────────────────
ALTER TABLE financiero.factura_legal_item
    ALTER COLUMN iva SET DEFAULT 10;

ALTER TABLE financiero.factura_legal_item
    ALTER COLUMN iva SET NOT NULL;

-- Solo 0, 5 y 10 son tasas válidas en SIFEN (dTasaIVA). El CHECK ataja de raíz la clase de bug
-- que este proyecto ya pagó dos veces: un valor fiscal inválido que solo se descubre cuando la
-- SET rechaza el DE con un código que no explica nada. Se crea con guarda para que, si alguna
-- fila trae un valor inesperado, el error diga exactamente cuál en vez de reventar opaco.
DO $$
DECLARE
    invalidos TEXT;
BEGIN
    SELECT string_agg(DISTINCT iva::TEXT, ', ')
    INTO invalidos
    FROM financiero.factura_legal_item
    WHERE iva NOT IN (0, 5, 10);

    IF invalidos IS NOT NULL THEN
        RAISE EXCEPTION 'V37: hay ítems de factura con tasas de IVA que SIFEN no acepta (%). Corregir esas filas antes de reintentar la migración.', invalidos;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_schema = 'financiero'
          AND table_name = 'factura_legal_item'
          AND constraint_name = 'chk_fli_iva'
    ) THEN
        ALTER TABLE financiero.factura_legal_item
            ADD CONSTRAINT chk_fli_iva CHECK (iva IN (0, 5, 10));
    END IF;
END $$;

COMMENT ON COLUMN financiero.factura_legal_item.iva IS
    'Tasa de IVA del ítem (0, 5 o 10) fijada al emitir la factura. NO se deriva de productos.producto.iva: si el catálogo cambia de tasa, este documento tiene que seguir reflejando lo que SIFEN aprobó.';
