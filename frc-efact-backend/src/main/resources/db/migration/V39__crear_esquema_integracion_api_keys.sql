-- V39__crear_esquema_integracion_api_keys.sql
--
-- Acceso de sistemas externos a FRC eFact por API key.
--
-- eFact pasa a ofrecer servicios de facturación electrónica a otros sistemas. Un cliente
-- del servicio (cuenta_servicio) puede ser:
--   1. un consultor, que solo consulta DE en SIFEN y no tiene empresa en eFact;
--   2. una pasarela, que usa eFact como canal hacia SIFEN sin guardar la transacción;
--   3. una empresa que gestiona toda su facturación en eFact.
-- Por eso la empresa es OPCIONAL en la cuenta. Lo que cada key puede hacer lo definen sus
-- scopes; esta versión solo implementa el modo 1 (scope `de:consultar`).
--
-- La migración es puramente aditiva: esquema y tablas nuevas, sin tocar nada existente.

CREATE SCHEMA IF NOT EXISTS integracion;

-- ----------------------------------------------------------------------------
-- Cuenta de servicio: el cliente de FRC eFact que consume la API
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS integracion.cuenta_servicio (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    ruc VARCHAR(20),
    email_contacto VARCHAR(150),
    empresa_id BIGINT,
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    observacion TEXT,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    creado_por VARCHAR(50),
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_por VARCHAR(50),

    CONSTRAINT fk_cuenta_servicio_empresa FOREIGN KEY (empresa_id) REFERENCES empresa.empresa(id)
);

CREATE INDEX IF NOT EXISTS idx_cuenta_servicio_empresa ON integracion.cuenta_servicio(empresa_id);
CREATE INDEX IF NOT EXISTS idx_cuenta_servicio_activa ON integracion.cuenta_servicio(activa);

COMMENT ON TABLE integracion.cuenta_servicio IS 'Cliente de FRC eFact que consume la API de integración';
COMMENT ON COLUMN integracion.cuenta_servicio.empresa_id IS 'Empresa de eFact asociada (opcional: un consultor no tiene). Si tiene certificado, las consultas a SIFEN salen con él';

CREATE TRIGGER trigger_actualizar_cuenta_servicio_timestamp
    BEFORE UPDATE ON integracion.cuenta_servicio
    FOR EACH ROW
    EXECUTE FUNCTION actualizar_timestamp_modificacion();

-- ----------------------------------------------------------------------------
-- API key: credencial de una cuenta. Solo se guarda el hash del secreto.
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS integracion.api_key (
    id BIGSERIAL PRIMARY KEY,
    cuenta_servicio_id BIGINT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    prefijo VARCHAR(16) NOT NULL,
    hash_secreto VARCHAR(64) NOT NULL,
    scopes VARCHAR(500) NOT NULL,
    expira_en TIMESTAMP,
    revocada_en TIMESTAMP,
    revocada_por VARCHAR(50),
    ultimo_uso_en TIMESTAMP,
    ultimo_uso_ip VARCHAR(64),
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    creado_por VARCHAR(50),
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_por VARCHAR(50),

    CONSTRAINT uk_api_key_prefijo UNIQUE (prefijo),
    CONSTRAINT fk_api_key_cuenta FOREIGN KEY (cuenta_servicio_id) REFERENCES integracion.cuenta_servicio(id)
);

CREATE INDEX IF NOT EXISTS idx_api_key_cuenta ON integracion.api_key(cuenta_servicio_id);

COMMENT ON TABLE integracion.api_key IS 'Credenciales de acceso de las cuentas de servicio';
COMMENT ON COLUMN integracion.api_key.prefijo IS 'Parte pública de la key, usada para buscarla. Formato de la key: frc_<prefijo>_<secreto>';
COMMENT ON COLUMN integracion.api_key.hash_secreto IS 'SHA-256 hex del secreto. El secreto en claro se muestra una única vez, al crearla';
COMMENT ON COLUMN integracion.api_key.scopes IS 'Operaciones permitidas, separadas por espacio (ej: de:consultar)';

CREATE TRIGGER trigger_actualizar_api_key_timestamp
    BEFORE UPDATE ON integracion.api_key
    FOR EACH ROW
    EXECUTE FUNCTION actualizar_timestamp_modificacion();

-- ----------------------------------------------------------------------------
-- Registro de solicitudes: rastro de uso por key (auditoría y futura facturación del servicio)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS integracion.solicitud_api (
    id BIGSERIAL PRIMARY KEY,
    api_key_id BIGINT NOT NULL,
    operacion VARCHAR(50) NOT NULL,
    recurso VARCHAR(100),
    codigo_http INTEGER NOT NULL,
    codigo_sifen VARCHAR(10),
    duracion_ms BIGINT,
    ip VARCHAR(64),
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    creado_por VARCHAR(50),
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_por VARCHAR(50),

    CONSTRAINT fk_solicitud_api_key FOREIGN KEY (api_key_id) REFERENCES integracion.api_key(id)
);

CREATE INDEX IF NOT EXISTS idx_solicitud_api_key_fecha ON integracion.solicitud_api(api_key_id, creado_en);

COMMENT ON TABLE integracion.solicitud_api IS 'Una fila por llamada a la API de integración';
COMMENT ON COLUMN integracion.solicitud_api.recurso IS 'Identificador consultado (ej: CDC)';

CREATE TRIGGER trigger_actualizar_solicitud_api_timestamp
    BEFORE UPDATE ON integracion.solicitud_api
    FOR EACH ROW
    EXECUTE FUNCTION actualizar_timestamp_modificacion();
