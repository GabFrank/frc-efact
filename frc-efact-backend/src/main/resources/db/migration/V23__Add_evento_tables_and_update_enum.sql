-- Migración para agregar tablas de eventos de nominación e inutilización
-- Nota: Los estados se almacenan como VARCHAR según V22

-- Table: financiero.evento_nominacion_de
-- Nominación events for electronic documents
CREATE TABLE IF NOT EXISTS financiero.evento_nominacion_de (
    id BIGSERIAL PRIMARY KEY,
    documento_electronico_id BIGINT NOT NULL REFERENCES financiero.documento_electronico(id) ON DELETE CASCADE,
    cliente_id BIGINT NOT NULL REFERENCES clientes.cliente(id) ON DELETE CASCADE,
    
    evento_id VARCHAR(50) UNIQUE NOT NULL,
    fecha_firma TIMESTAMP NOT NULL,
    cdc_documento VARCHAR(44) NOT NULL,
    
    -- Datos del receptor nominado
    nombre_receptor VARCHAR(200),
    documento_receptor VARCHAR(50),
    tipo_receptor VARCHAR(50),
    total_factura DECIMAL(15,2),
    fecha_emision TIMESTAMP,
    fecha_recepcion TIMESTAMP,
    xml_evento TEXT,
    
    -- SIFEN response
    estado VARCHAR(50) DEFAULT 'PENDIENTE' NOT NULL,
    fecha_procesamiento TIMESTAMP,
    protocolo_autorizacion VARCHAR(50),
    codigo_respuesta VARCHAR(10),
    mensaje_respuesta TEXT,
    respuesta_bruta TEXT,
    
    activo BOOLEAN DEFAULT true,
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    creado_por VARCHAR(50),
    actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    actualizado_por VARCHAR(50)
);

COMMENT ON TABLE financiero.evento_nominacion_de IS 'Eventos de nominación de receptor de documentos electrónicos';
COMMENT ON COLUMN financiero.evento_nominacion_de.evento_id IS 'Identificador único del evento de nominación';
COMMENT ON COLUMN financiero.evento_nominacion_de.cdc_documento IS 'CDC del documento que se está nominando';
COMMENT ON COLUMN financiero.evento_nominacion_de.nombre_receptor IS 'Nombre del receptor nominado';
COMMENT ON COLUMN financiero.evento_nominacion_de.documento_receptor IS 'Documento del receptor (RUC o documento de identidad)';
COMMENT ON COLUMN financiero.evento_nominacion_de.tipo_receptor IS 'Tipo de receptor: CONTRIBUYENTE o NO_CONTRIBUYENTE';

-- Table: financiero.evento_inutilizacion_de
-- Inutilización events for document numbering ranges
CREATE TABLE IF NOT EXISTS financiero.evento_inutilizacion_de (
    id BIGSERIAL PRIMARY KEY,
    timbrado_id BIGINT NOT NULL REFERENCES financiero.timbrado(id) ON DELETE CASCADE,
    timbrado_detalle_id BIGINT REFERENCES financiero.timbrado_detalle(id),
    
    evento_id VARCHAR(50) UNIQUE NOT NULL,
    fecha_firma TIMESTAMP NOT NULL,
    
    establecimiento VARCHAR(10) NOT NULL,
    punto_expedicion VARCHAR(10) NOT NULL,
    numero_inicio INTEGER NOT NULL,
    numero_fin INTEGER NOT NULL,
    tipo_de VARCHAR(50) NOT NULL,
    motivo_inutilizacion TEXT NOT NULL,
    xml_evento TEXT,
    
    -- SIFEN response
    estado VARCHAR(50) DEFAULT 'PENDIENTE' NOT NULL,
    fecha_procesamiento TIMESTAMP,
    protocolo_autorizacion VARCHAR(50),
    codigo_respuesta VARCHAR(10),
    mensaje_respuesta TEXT,
    respuesta_bruta TEXT,
    
    activo BOOLEAN DEFAULT true,
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    creado_por VARCHAR(50),
    actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    actualizado_por VARCHAR(50),
    
    CONSTRAINT chk_rango_inutilizacion CHECK (numero_inicio <= numero_fin)
);

COMMENT ON TABLE financiero.evento_inutilizacion_de IS 'Eventos de inutilización de numeración de documentos electrónicos';
COMMENT ON COLUMN financiero.evento_inutilizacion_de.evento_id IS 'Identificador único del evento de inutilización';
COMMENT ON COLUMN financiero.evento_inutilizacion_de.establecimiento IS 'Establecimiento (ej: "001")';
COMMENT ON COLUMN financiero.evento_inutilizacion_de.punto_expedicion IS 'Punto de expedición';
COMMENT ON COLUMN financiero.evento_inutilizacion_de.numero_inicio IS 'Número inicial del rango a inutilizar';
COMMENT ON COLUMN financiero.evento_inutilizacion_de.numero_fin IS 'Número final del rango a inutilizar';
COMMENT ON COLUMN financiero.evento_inutilizacion_de.tipo_de IS 'Tipo de documento electrónico (ej: "FACTURA_ELECTRONICA")';
COMMENT ON COLUMN financiero.evento_inutilizacion_de.motivo_inutilizacion IS 'Motivo de la inutilización';

-- Create indexes for evento tables
CREATE INDEX IF NOT EXISTS idx_evento_nominacion_de ON financiero.evento_nominacion_de(documento_electronico_id);
CREATE INDEX IF NOT EXISTS idx_evento_nominacion_cliente ON financiero.evento_nominacion_de(cliente_id);
CREATE INDEX IF NOT EXISTS idx_evento_nominacion_estado ON financiero.evento_nominacion_de(estado);
CREATE INDEX IF NOT EXISTS idx_evento_nominacion_cdc ON financiero.evento_nominacion_de(cdc_documento);
CREATE INDEX IF NOT EXISTS idx_evento_nominacion_activo ON financiero.evento_nominacion_de(activo);

CREATE INDEX IF NOT EXISTS idx_evento_inutilizacion_timbrado ON financiero.evento_inutilizacion_de(timbrado_id);
CREATE INDEX IF NOT EXISTS idx_evento_inutilizacion_detalle ON financiero.evento_inutilizacion_de(timbrado_detalle_id);
CREATE INDEX IF NOT EXISTS idx_evento_inutilizacion_estado ON financiero.evento_inutilizacion_de(estado);
CREATE INDEX IF NOT EXISTS idx_evento_inutilizacion_activo ON financiero.evento_inutilizacion_de(activo);

-- Create triggers for evento tables
CREATE TRIGGER trigger_actualizar_evento_nominacion_timestamp
    BEFORE UPDATE ON financiero.evento_nominacion_de
    FOR EACH ROW
    EXECUTE FUNCTION actualizar_timestamp_modificacion();

CREATE TRIGGER trigger_actualizar_evento_inutilizacion_timestamp
    BEFORE UPDATE ON financiero.evento_inutilizacion_de
    FOR EACH ROW
    EXECUTE FUNCTION actualizar_timestamp_modificacion();

