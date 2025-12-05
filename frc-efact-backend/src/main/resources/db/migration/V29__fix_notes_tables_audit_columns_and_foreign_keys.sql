-- Corregir columnas de auditoría y foreign keys en tablas de notas
-- Esta migración corrige los problemas de V28:
-- 1. Renombra columnas de auditoría de inglés a español
-- 2. Corrige referencias de esquemas en foreign keys

-- ============================================
-- NOTA CRÉDITO
-- ============================================

-- Renombrar columnas de auditoría si existen
DO $$
BEGIN
    -- Renombrar columnas de nota_credito
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_schema = 'financiero' 
               AND table_name = 'nota_credito' 
               AND column_name = 'created_at') THEN
        ALTER TABLE financiero.nota_credito RENAME COLUMN created_at TO creado_en;
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_schema = 'financiero' 
               AND table_name = 'nota_credito' 
               AND column_name = 'updated_at') THEN
        ALTER TABLE financiero.nota_credito RENAME COLUMN updated_at TO actualizado_en;
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_schema = 'financiero' 
               AND table_name = 'nota_credito' 
               AND column_name = 'created_by') THEN
        ALTER TABLE financiero.nota_credito RENAME COLUMN created_by TO creado_por;
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_schema = 'financiero' 
               AND table_name = 'nota_credito' 
               AND column_name = 'updated_by') THEN
        ALTER TABLE financiero.nota_credito RENAME COLUMN updated_by TO actualizado_por;
    END IF;
    
    -- Asegurar que las columnas sean NOT NULL con default
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_schema = 'financiero' 
               AND table_name = 'nota_credito' 
               AND column_name = 'creado_en') THEN
        ALTER TABLE financiero.nota_credito 
            ALTER COLUMN creado_en SET NOT NULL,
            ALTER COLUMN creado_en SET DEFAULT CURRENT_TIMESTAMP;
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_schema = 'financiero' 
               AND table_name = 'nota_credito' 
               AND column_name = 'actualizado_en') THEN
        ALTER TABLE financiero.nota_credito 
            ALTER COLUMN actualizado_en SET NOT NULL,
            ALTER COLUMN actualizado_en SET DEFAULT CURRENT_TIMESTAMP;
    END IF;
END $$;

-- Corregir foreign keys si existen
DO $$
BEGIN
    -- Eliminar y recrear FK de empresa si apunta al esquema incorrecto
    IF EXISTS (SELECT 1 FROM information_schema.table_constraints 
               WHERE constraint_schema = 'financiero' 
               AND table_name = 'nota_credito' 
               AND constraint_name = 'fk_nc_empresa') THEN
        ALTER TABLE financiero.nota_credito DROP CONSTRAINT IF EXISTS fk_nc_empresa;
        ALTER TABLE financiero.nota_credito 
            ADD CONSTRAINT fk_nc_empresa FOREIGN KEY (empresa_id) REFERENCES empresa.empresa(id);
    END IF;
    
    -- Eliminar y recrear FK de cliente si apunta al esquema incorrecto
    IF EXISTS (SELECT 1 FROM information_schema.table_constraints 
               WHERE constraint_schema = 'financiero' 
               AND table_name = 'nota_credito' 
               AND constraint_name = 'fk_nc_cliente') THEN
        ALTER TABLE financiero.nota_credito DROP CONSTRAINT IF EXISTS fk_nc_cliente;
        ALTER TABLE financiero.nota_credito 
            ADD CONSTRAINT fk_nc_cliente FOREIGN KEY (cliente_id) REFERENCES clientes.cliente(id);
    END IF;
END $$;

-- ============================================
-- NOTA CRÉDITO ITEM
-- ============================================

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_schema = 'financiero' 
               AND table_name = 'nota_credito_item' 
               AND column_name = 'created_at') THEN
        ALTER TABLE financiero.nota_credito_item RENAME COLUMN created_at TO creado_en;
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_schema = 'financiero' 
               AND table_name = 'nota_credito_item' 
               AND column_name = 'updated_at') THEN
        ALTER TABLE financiero.nota_credito_item RENAME COLUMN updated_at TO actualizado_en;
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_schema = 'financiero' 
               AND table_name = 'nota_credito_item' 
               AND column_name = 'created_by') THEN
        ALTER TABLE financiero.nota_credito_item RENAME COLUMN created_by TO creado_por;
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_schema = 'financiero' 
               AND table_name = 'nota_credito_item' 
               AND column_name = 'updated_by') THEN
        ALTER TABLE financiero.nota_credito_item RENAME COLUMN updated_by TO actualizado_por;
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_schema = 'financiero' 
               AND table_name = 'nota_credito_item' 
               AND column_name = 'creado_en') THEN
        ALTER TABLE financiero.nota_credito_item 
            ALTER COLUMN creado_en SET NOT NULL,
            ALTER COLUMN creado_en SET DEFAULT CURRENT_TIMESTAMP;
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_schema = 'financiero' 
               AND table_name = 'nota_credito_item' 
               AND column_name = 'actualizado_en') THEN
        ALTER TABLE financiero.nota_credito_item 
            ALTER COLUMN actualizado_en SET NOT NULL,
            ALTER COLUMN actualizado_en SET DEFAULT CURRENT_TIMESTAMP;
    END IF;
    
    -- Corregir FK de producto
    IF EXISTS (SELECT 1 FROM information_schema.table_constraints 
               WHERE constraint_schema = 'financiero' 
               AND table_name = 'nota_credito_item' 
               AND constraint_name = 'fk_nci_producto') THEN
        ALTER TABLE financiero.nota_credito_item DROP CONSTRAINT IF EXISTS fk_nci_producto;
        ALTER TABLE financiero.nota_credito_item 
            ADD CONSTRAINT fk_nci_producto FOREIGN KEY (producto_id) REFERENCES productos.producto(id);
    END IF;
END $$;

-- ============================================
-- NOTA DÉBITO
-- ============================================

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_schema = 'financiero' 
               AND table_name = 'nota_debito' 
               AND column_name = 'created_at') THEN
        ALTER TABLE financiero.nota_debito RENAME COLUMN created_at TO creado_en;
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_schema = 'financiero' 
               AND table_name = 'nota_debito' 
               AND column_name = 'updated_at') THEN
        ALTER TABLE financiero.nota_debito RENAME COLUMN updated_at TO actualizado_en;
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_schema = 'financiero' 
               AND table_name = 'nota_debito' 
               AND column_name = 'created_by') THEN
        ALTER TABLE financiero.nota_debito RENAME COLUMN created_by TO creado_por;
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_schema = 'financiero' 
               AND table_name = 'nota_debito' 
               AND column_name = 'updated_by') THEN
        ALTER TABLE financiero.nota_debito RENAME COLUMN updated_by TO actualizado_por;
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_schema = 'financiero' 
               AND table_name = 'nota_debito' 
               AND column_name = 'creado_en') THEN
        ALTER TABLE financiero.nota_debito 
            ALTER COLUMN creado_en SET NOT NULL,
            ALTER COLUMN creado_en SET DEFAULT CURRENT_TIMESTAMP;
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_schema = 'financiero' 
               AND table_name = 'nota_debito' 
               AND column_name = 'actualizado_en') THEN
        ALTER TABLE financiero.nota_debito 
            ALTER COLUMN actualizado_en SET NOT NULL,
            ALTER COLUMN actualizado_en SET DEFAULT CURRENT_TIMESTAMP;
    END IF;
    
    -- Corregir FKs
    IF EXISTS (SELECT 1 FROM information_schema.table_constraints 
               WHERE constraint_schema = 'financiero' 
               AND table_name = 'nota_debito' 
               AND constraint_name = 'fk_nd_empresa') THEN
        ALTER TABLE financiero.nota_debito DROP CONSTRAINT IF EXISTS fk_nd_empresa;
        ALTER TABLE financiero.nota_debito 
            ADD CONSTRAINT fk_nd_empresa FOREIGN KEY (empresa_id) REFERENCES empresa.empresa(id);
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.table_constraints 
               WHERE constraint_schema = 'financiero' 
               AND table_name = 'nota_debito' 
               AND constraint_name = 'fk_nd_cliente') THEN
        ALTER TABLE financiero.nota_debito DROP CONSTRAINT IF EXISTS fk_nd_cliente;
        ALTER TABLE financiero.nota_debito 
            ADD CONSTRAINT fk_nd_cliente FOREIGN KEY (cliente_id) REFERENCES clientes.cliente(id);
    END IF;
END $$;

-- ============================================
-- NOTA DÉBITO ITEM
-- ============================================

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_schema = 'financiero' 
               AND table_name = 'nota_debito_item' 
               AND column_name = 'created_at') THEN
        ALTER TABLE financiero.nota_debito_item RENAME COLUMN created_at TO creado_en;
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_schema = 'financiero' 
               AND table_name = 'nota_debito_item' 
               AND column_name = 'updated_at') THEN
        ALTER TABLE financiero.nota_debito_item RENAME COLUMN updated_at TO actualizado_en;
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_schema = 'financiero' 
               AND table_name = 'nota_debito_item' 
               AND column_name = 'created_by') THEN
        ALTER TABLE financiero.nota_debito_item RENAME COLUMN created_by TO creado_por;
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_schema = 'financiero' 
               AND table_name = 'nota_debito_item' 
               AND column_name = 'updated_by') THEN
        ALTER TABLE financiero.nota_debito_item RENAME COLUMN updated_by TO actualizado_por;
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_schema = 'financiero' 
               AND table_name = 'nota_debito_item' 
               AND column_name = 'creado_en') THEN
        ALTER TABLE financiero.nota_debito_item 
            ALTER COLUMN creado_en SET NOT NULL,
            ALTER COLUMN creado_en SET DEFAULT CURRENT_TIMESTAMP;
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_schema = 'financiero' 
               AND table_name = 'nota_debito_item' 
               AND column_name = 'actualizado_en') THEN
        ALTER TABLE financiero.nota_debito_item 
            ALTER COLUMN actualizado_en SET NOT NULL,
            ALTER COLUMN actualizado_en SET DEFAULT CURRENT_TIMESTAMP;
    END IF;
    
    -- Corregir FK de producto
    IF EXISTS (SELECT 1 FROM information_schema.table_constraints 
               WHERE constraint_schema = 'financiero' 
               AND table_name = 'nota_debito_item' 
               AND constraint_name = 'fk_ndi_producto') THEN
        ALTER TABLE financiero.nota_debito_item DROP CONSTRAINT IF EXISTS fk_ndi_producto;
        ALTER TABLE financiero.nota_debito_item 
            ADD CONSTRAINT fk_ndi_producto FOREIGN KEY (producto_id) REFERENCES productos.producto(id);
    END IF;
END $$;

-- ============================================
-- NOTA REMISIÓN
-- ============================================

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_schema = 'financiero' 
               AND table_name = 'nota_remision' 
               AND column_name = 'created_at') THEN
        ALTER TABLE financiero.nota_remision RENAME COLUMN created_at TO creado_en;
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_schema = 'financiero' 
               AND table_name = 'nota_remision' 
               AND column_name = 'updated_at') THEN
        ALTER TABLE financiero.nota_remision RENAME COLUMN updated_at TO actualizado_en;
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_schema = 'financiero' 
               AND table_name = 'nota_remision' 
               AND column_name = 'created_by') THEN
        ALTER TABLE financiero.nota_remision RENAME COLUMN created_by TO creado_por;
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_schema = 'financiero' 
               AND table_name = 'nota_remision' 
               AND column_name = 'updated_by') THEN
        ALTER TABLE financiero.nota_remision RENAME COLUMN updated_by TO actualizado_por;
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_schema = 'financiero' 
               AND table_name = 'nota_remision' 
               AND column_name = 'creado_en') THEN
        ALTER TABLE financiero.nota_remision 
            ALTER COLUMN creado_en SET NOT NULL,
            ALTER COLUMN creado_en SET DEFAULT CURRENT_TIMESTAMP;
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_schema = 'financiero' 
               AND table_name = 'nota_remision' 
               AND column_name = 'actualizado_en') THEN
        ALTER TABLE financiero.nota_remision 
            ALTER COLUMN actualizado_en SET NOT NULL,
            ALTER COLUMN actualizado_en SET DEFAULT CURRENT_TIMESTAMP;
    END IF;
    
    -- Corregir FKs
    IF EXISTS (SELECT 1 FROM information_schema.table_constraints 
               WHERE constraint_schema = 'financiero' 
               AND table_name = 'nota_remision' 
               AND constraint_name = 'fk_nr_empresa') THEN
        ALTER TABLE financiero.nota_remision DROP CONSTRAINT IF EXISTS fk_nr_empresa;
        ALTER TABLE financiero.nota_remision 
            ADD CONSTRAINT fk_nr_empresa FOREIGN KEY (empresa_id) REFERENCES empresa.empresa(id);
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.table_constraints 
               WHERE constraint_schema = 'financiero' 
               AND table_name = 'nota_remision' 
               AND constraint_name = 'fk_nr_cliente') THEN
        ALTER TABLE financiero.nota_remision DROP CONSTRAINT IF EXISTS fk_nr_cliente;
        ALTER TABLE financiero.nota_remision 
            ADD CONSTRAINT fk_nr_cliente FOREIGN KEY (cliente_id) REFERENCES clientes.cliente(id);
    END IF;
END $$;

-- ============================================
-- NOTA REMISIÓN ITEM
-- ============================================

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_schema = 'financiero' 
               AND table_name = 'nota_remision_item' 
               AND column_name = 'created_at') THEN
        ALTER TABLE financiero.nota_remision_item RENAME COLUMN created_at TO creado_en;
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_schema = 'financiero' 
               AND table_name = 'nota_remision_item' 
               AND column_name = 'updated_at') THEN
        ALTER TABLE financiero.nota_remision_item RENAME COLUMN updated_at TO actualizado_en;
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_schema = 'financiero' 
               AND table_name = 'nota_remision_item' 
               AND column_name = 'created_by') THEN
        ALTER TABLE financiero.nota_remision_item RENAME COLUMN created_by TO creado_por;
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_schema = 'financiero' 
               AND table_name = 'nota_remision_item' 
               AND column_name = 'updated_by') THEN
        ALTER TABLE financiero.nota_remision_item RENAME COLUMN updated_by TO actualizado_por;
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_schema = 'financiero' 
               AND table_name = 'nota_remision_item' 
               AND column_name = 'creado_en') THEN
        ALTER TABLE financiero.nota_remision_item 
            ALTER COLUMN creado_en SET NOT NULL,
            ALTER COLUMN creado_en SET DEFAULT CURRENT_TIMESTAMP;
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_schema = 'financiero' 
               AND table_name = 'nota_remision_item' 
               AND column_name = 'actualizado_en') THEN
        ALTER TABLE financiero.nota_remision_item 
            ALTER COLUMN actualizado_en SET NOT NULL,
            ALTER COLUMN actualizado_en SET DEFAULT CURRENT_TIMESTAMP;
    END IF;
    
    -- Corregir FK de producto
    IF EXISTS (SELECT 1 FROM information_schema.table_constraints 
               WHERE constraint_schema = 'financiero' 
               AND table_name = 'nota_remision_item' 
               AND constraint_name = 'fk_nri_producto') THEN
        ALTER TABLE financiero.nota_remision_item DROP CONSTRAINT IF EXISTS fk_nri_producto;
        ALTER TABLE financiero.nota_remision_item 
            ADD CONSTRAINT fk_nri_producto FOREIGN KEY (producto_id) REFERENCES productos.producto(id);
    END IF;
END $$;

