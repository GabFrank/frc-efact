-- Crear tabla Nota Credito
CREATE TABLE IF NOT EXISTS financiero.nota_credito (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    timbrado_detalle_id BIGINT NOT NULL,
    cliente_id BIGINT,
    factura_legal_id BIGINT, -- Documento asociado
    numero_nota_credito INTEGER NOT NULL,
    fecha TIMESTAMP NOT NULL,
    
    -- Datos del cliente (snapshot)
    nombre VARCHAR(200),
    ruc VARCHAR(20),
    direccion TEXT,
    
    -- Motivo
    motivo_emision VARCHAR(50),
    descripcion_motivo VARCHAR(255),
    
    -- Totales
    iva_parcial_0 NUMERIC(15, 2) DEFAULT 0,
    iva_parcial_5 NUMERIC(15, 2) DEFAULT 0,
    iva_parcial_10 NUMERIC(15, 2) DEFAULT 0,
    total_parcial_0 NUMERIC(15, 2) DEFAULT 0,
    total_parcial_5 NUMERIC(15, 2) DEFAULT 0,
    total_parcial_10 NUMERIC(15, 2) DEFAULT 0,
    descuento_final NUMERIC(15, 2) DEFAULT 0,
    total_parcial NUMERIC(15, 2) NOT NULL DEFAULT 0,
    total_final NUMERIC(15, 2) NOT NULL DEFAULT 0,
    
    -- Moneda
    moneda_extranjera VARCHAR(3),
    cambio NUMERIC(10, 4),
    
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),

    CONSTRAINT uk_nc_numero UNIQUE (timbrado_detalle_id, numero_nota_credito)
);

CREATE INDEX IF NOT EXISTS idx_nc_empresa ON financiero.nota_credito(empresa_id);
CREATE INDEX IF NOT EXISTS idx_nc_cliente ON financiero.nota_credito(cliente_id);
CREATE INDEX IF NOT EXISTS idx_nc_fecha ON financiero.nota_credito(fecha);

-- Agregar constraints de foreign keys si no existen
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_schema = 'financiero' AND table_name = 'nota_credito' AND constraint_name = 'fk_nc_empresa') THEN
        ALTER TABLE financiero.nota_credito ADD CONSTRAINT fk_nc_empresa FOREIGN KEY (empresa_id) REFERENCES empresa.empresa(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_schema = 'financiero' AND table_name = 'nota_credito' AND constraint_name = 'fk_nc_timbrado') THEN
        ALTER TABLE financiero.nota_credito ADD CONSTRAINT fk_nc_timbrado FOREIGN KEY (timbrado_detalle_id) REFERENCES financiero.timbrado_detalle(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_schema = 'financiero' AND table_name = 'nota_credito' AND constraint_name = 'fk_nc_cliente') THEN
        ALTER TABLE financiero.nota_credito ADD CONSTRAINT fk_nc_cliente FOREIGN KEY (cliente_id) REFERENCES clientes.cliente(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_schema = 'financiero' AND table_name = 'nota_credito' AND constraint_name = 'fk_nc_factura') THEN
        ALTER TABLE financiero.nota_credito ADD CONSTRAINT fk_nc_factura FOREIGN KEY (factura_legal_id) REFERENCES financiero.factura_legal(id);
    END IF;
END $$;

-- Crear tabla Nota Credito Item
CREATE TABLE IF NOT EXISTS financiero.nota_credito_item (
    id BIGSERIAL PRIMARY KEY,
    nota_credito_id BIGINT NOT NULL,
    producto_id BIGINT,
    codigo VARCHAR(50),
    descripcion VARCHAR(255) NOT NULL,
    cantidad NUMERIC(15, 4) NOT NULL,
    precio_unitario NUMERIC(15, 2) NOT NULL,
    descuento NUMERIC(15, 2) DEFAULT 0,
    total NUMERIC(15, 2) NOT NULL,
    iva INTEGER NOT NULL DEFAULT 10, -- 0, 5, 10
    
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50)
);

CREATE INDEX IF NOT EXISTS idx_nci_nota_credito ON financiero.nota_credito_item(nota_credito_id);

-- Agregar constraints de foreign keys si no existen
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_schema = 'financiero' AND table_name = 'nota_credito_item' AND constraint_name = 'fk_nci_nota_credito') THEN
        ALTER TABLE financiero.nota_credito_item ADD CONSTRAINT fk_nci_nota_credito FOREIGN KEY (nota_credito_id) REFERENCES financiero.nota_credito(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_schema = 'financiero' AND table_name = 'nota_credito_item' AND constraint_name = 'fk_nci_producto') THEN
        ALTER TABLE financiero.nota_credito_item ADD CONSTRAINT fk_nci_producto FOREIGN KEY (producto_id) REFERENCES productos.producto(id);
    END IF;
END $$;

-- Crear tabla Nota Debito
CREATE TABLE IF NOT EXISTS financiero.nota_debito (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    timbrado_detalle_id BIGINT NOT NULL,
    cliente_id BIGINT,
    factura_legal_id BIGINT,
    numero_nota_debito INTEGER NOT NULL,
    fecha TIMESTAMP NOT NULL,
    
    nombre VARCHAR(200),
    ruc VARCHAR(20),
    direccion TEXT,
    
    motivo_emision VARCHAR(50),
    descripcion_motivo VARCHAR(255),
    
    iva_parcial_0 NUMERIC(15, 2) DEFAULT 0,
    iva_parcial_5 NUMERIC(15, 2) DEFAULT 0,
    iva_parcial_10 NUMERIC(15, 2) DEFAULT 0,
    total_parcial_0 NUMERIC(15, 2) DEFAULT 0,
    total_parcial_5 NUMERIC(15, 2) DEFAULT 0,
    total_parcial_10 NUMERIC(15, 2) DEFAULT 0,
    descuento_final NUMERIC(15, 2) DEFAULT 0,
    total_parcial NUMERIC(15, 2) NOT NULL DEFAULT 0,
    total_final NUMERIC(15, 2) NOT NULL DEFAULT 0,
    
    moneda_extranjera VARCHAR(3),
    cambio NUMERIC(10, 4),
    
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),

    CONSTRAINT uk_nd_numero UNIQUE (timbrado_detalle_id, numero_nota_debito)
);

CREATE INDEX IF NOT EXISTS idx_nd_empresa ON financiero.nota_debito(empresa_id);
CREATE INDEX IF NOT EXISTS idx_nd_cliente ON financiero.nota_debito(cliente_id);

-- Agregar constraints de foreign keys si no existen
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_schema = 'financiero' AND table_name = 'nota_debito' AND constraint_name = 'fk_nd_empresa') THEN
        ALTER TABLE financiero.nota_debito ADD CONSTRAINT fk_nd_empresa FOREIGN KEY (empresa_id) REFERENCES empresa.empresa(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_schema = 'financiero' AND table_name = 'nota_debito' AND constraint_name = 'fk_nd_timbrado') THEN
        ALTER TABLE financiero.nota_debito ADD CONSTRAINT fk_nd_timbrado FOREIGN KEY (timbrado_detalle_id) REFERENCES financiero.timbrado_detalle(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_schema = 'financiero' AND table_name = 'nota_debito' AND constraint_name = 'fk_nd_cliente') THEN
        ALTER TABLE financiero.nota_debito ADD CONSTRAINT fk_nd_cliente FOREIGN KEY (cliente_id) REFERENCES clientes.cliente(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_schema = 'financiero' AND table_name = 'nota_debito' AND constraint_name = 'fk_nd_factura') THEN
        ALTER TABLE financiero.nota_debito ADD CONSTRAINT fk_nd_factura FOREIGN KEY (factura_legal_id) REFERENCES financiero.factura_legal(id);
    END IF;
END $$;

-- Crear tabla Nota Debito Item
CREATE TABLE IF NOT EXISTS financiero.nota_debito_item (
    id BIGSERIAL PRIMARY KEY,
    nota_debito_id BIGINT NOT NULL,
    producto_id BIGINT,
    codigo VARCHAR(50),
    descripcion VARCHAR(255) NOT NULL,
    cantidad NUMERIC(15, 4) NOT NULL,
    precio_unitario NUMERIC(15, 2) NOT NULL,
    total NUMERIC(15, 2) NOT NULL,
    iva INTEGER NOT NULL DEFAULT 10,
    
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50)
);

CREATE INDEX IF NOT EXISTS idx_ndi_nota_debito ON financiero.nota_debito_item(nota_debito_id);

-- Agregar constraints de foreign keys si no existen
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_schema = 'financiero' AND table_name = 'nota_debito_item' AND constraint_name = 'fk_ndi_nota_debito') THEN
        ALTER TABLE financiero.nota_debito_item ADD CONSTRAINT fk_ndi_nota_debito FOREIGN KEY (nota_debito_id) REFERENCES financiero.nota_debito(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_schema = 'financiero' AND table_name = 'nota_debito_item' AND constraint_name = 'fk_ndi_producto') THEN
        ALTER TABLE financiero.nota_debito_item ADD CONSTRAINT fk_ndi_producto FOREIGN KEY (producto_id) REFERENCES productos.producto(id);
    END IF;
END $$;

-- Crear tabla Nota Remision
CREATE TABLE IF NOT EXISTS financiero.nota_remision (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    timbrado_detalle_id BIGINT NOT NULL,
    cliente_id BIGINT, -- Destinatario principal
    factura_legal_id BIGINT, -- Factura asociada (opcional)
    numero_nota_remision INTEGER NOT NULL,
    fecha TIMESTAMP NOT NULL,
    
    -- Salida
    direccion_partida TEXT,
    ciudad_partida VARCHAR(100),
    departamento_partida VARCHAR(100),
    
    -- Llegada (Destinatario)
    nombre_destinatario VARCHAR(200),
    ruc_destinatario VARCHAR(20),
    direccion_destinatario TEXT,
    ciudad_destinatario VARCHAR(100),
    departamento_destinatario VARCHAR(100),
    
    motivo_emision VARCHAR(50),
    fecha_inicio_traslado DATE,
    fecha_fin_traslado DATE,
    km_estimado NUMERIC(10, 2),
    
    -- Transporte
    tipo_transporte VARCHAR(50), -- PROPIO, TERCERO
    modalidad_transporte VARCHAR(50), -- TERRESTRE, ETC
    
    -- Vehiculo (Simplificado)
    vehiculo_marca VARCHAR(100),
    vehiculo_matricula VARCHAR(20),
    
    -- Conductor / Transportista
    conductor_nombre VARCHAR(200),
    conductor_doc VARCHAR(20),
    conductor_direccion TEXT,
    
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),

    CONSTRAINT uk_nr_numero UNIQUE (timbrado_detalle_id, numero_nota_remision)
);

CREATE INDEX IF NOT EXISTS idx_nr_empresa ON financiero.nota_remision(empresa_id);

-- Agregar constraints de foreign keys si no existen
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_schema = 'financiero' AND table_name = 'nota_remision' AND constraint_name = 'fk_nr_empresa') THEN
        ALTER TABLE financiero.nota_remision ADD CONSTRAINT fk_nr_empresa FOREIGN KEY (empresa_id) REFERENCES empresa.empresa(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_schema = 'financiero' AND table_name = 'nota_remision' AND constraint_name = 'fk_nr_timbrado') THEN
        ALTER TABLE financiero.nota_remision ADD CONSTRAINT fk_nr_timbrado FOREIGN KEY (timbrado_detalle_id) REFERENCES financiero.timbrado_detalle(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_schema = 'financiero' AND table_name = 'nota_remision' AND constraint_name = 'fk_nr_cliente') THEN
        ALTER TABLE financiero.nota_remision ADD CONSTRAINT fk_nr_cliente FOREIGN KEY (cliente_id) REFERENCES clientes.cliente(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_schema = 'financiero' AND table_name = 'nota_remision' AND constraint_name = 'fk_nr_factura') THEN
        ALTER TABLE financiero.nota_remision ADD CONSTRAINT fk_nr_factura FOREIGN KEY (factura_legal_id) REFERENCES financiero.factura_legal(id);
    END IF;
END $$;

-- Crear tabla Nota Remision Item
CREATE TABLE IF NOT EXISTS financiero.nota_remision_item (
    id BIGSERIAL PRIMARY KEY,
    nota_remision_id BIGINT NOT NULL,
    producto_id BIGINT,
    codigo VARCHAR(50),
    descripcion VARCHAR(255) NOT NULL,
    cantidad NUMERIC(15, 4) NOT NULL,
    unidad_medida VARCHAR(10),
    
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50)
);

CREATE INDEX IF NOT EXISTS idx_nri_nota_remision ON financiero.nota_remision_item(nota_remision_id);

-- Agregar constraints de foreign keys si no existen
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_schema = 'financiero' AND table_name = 'nota_remision_item' AND constraint_name = 'fk_nri_nota_remision') THEN
        ALTER TABLE financiero.nota_remision_item ADD CONSTRAINT fk_nri_nota_remision FOREIGN KEY (nota_remision_id) REFERENCES financiero.nota_remision(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_schema = 'financiero' AND table_name = 'nota_remision_item' AND constraint_name = 'fk_nri_producto') THEN
        ALTER TABLE financiero.nota_remision_item ADD CONSTRAINT fk_nri_producto FOREIGN KEY (producto_id) REFERENCES productos.producto(id);
    END IF;
END $$;

-- Modificar Documento Electronico
DO $$
BEGIN
    -- Hacer factura_legal_id nullable si no lo es ya
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'financiero' 
        AND table_name = 'documento_electronico' 
        AND column_name = 'factura_legal_id' 
        AND is_nullable = 'NO'
    ) THEN
        ALTER TABLE financiero.documento_electronico 
            ALTER COLUMN factura_legal_id DROP NOT NULL;
    END IF;
    
    -- Agregar columnas si no existen
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'financiero' 
        AND table_name = 'documento_electronico' 
        AND column_name = 'nota_credito_id'
    ) THEN
        ALTER TABLE financiero.documento_electronico 
            ADD COLUMN nota_credito_id BIGINT;
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'financiero' 
        AND table_name = 'documento_electronico' 
        AND column_name = 'nota_debito_id'
    ) THEN
        ALTER TABLE financiero.documento_electronico 
            ADD COLUMN nota_debito_id BIGINT;
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'financiero' 
        AND table_name = 'documento_electronico' 
        AND column_name = 'nota_remision_id'
    ) THEN
        ALTER TABLE financiero.documento_electronico 
            ADD COLUMN nota_remision_id BIGINT;
    END IF;
    
    -- Agregar constraints si no existen
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_schema = 'financiero' 
        AND table_name = 'documento_electronico' 
        AND constraint_name = 'fk_de_nota_credito'
    ) THEN
        ALTER TABLE financiero.documento_electronico
            ADD CONSTRAINT fk_de_nota_credito FOREIGN KEY (nota_credito_id) REFERENCES financiero.nota_credito(id);
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_schema = 'financiero' 
        AND table_name = 'documento_electronico' 
        AND constraint_name = 'fk_de_nota_debito'
    ) THEN
        ALTER TABLE financiero.documento_electronico
            ADD CONSTRAINT fk_de_nota_debito FOREIGN KEY (nota_debito_id) REFERENCES financiero.nota_debito(id);
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_schema = 'financiero' 
        AND table_name = 'documento_electronico' 
        AND constraint_name = 'fk_de_nota_remision'
    ) THEN
        ALTER TABLE financiero.documento_electronico
            ADD CONSTRAINT fk_de_nota_remision FOREIGN KEY (nota_remision_id) REFERENCES financiero.nota_remision(id);
    END IF;
END $$;
