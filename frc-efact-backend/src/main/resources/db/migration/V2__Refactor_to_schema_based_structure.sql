-- V2 Migration: Refactor to schema-based structure with Spanish/English naming conventions
-- This migration implements the new database standards:
-- - Tables organized in schemas (e.g., persona.usuario)
-- - Bilingual naming: Spanish for specific fields, English for generic ones
-- - Standard audit fields: id, creado_en, creado_por, actualizado_en, actualizado_por

-- ============================================================================
-- 1. Create schemas
-- ============================================================================
CREATE SCHEMA IF NOT EXISTS persona;

-- ============================================================================
-- 2. Create new usuario table in persona schema
-- ============================================================================
CREATE TABLE persona.usuario (
    -- Primary key
    id BIGSERIAL PRIMARY KEY,
    
    -- User identification
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    
    -- User status
    is_active BOOLEAN DEFAULT true,
    intentos_fallidos_login INTEGER DEFAULT 0,
    bloqueado_hasta TIMESTAMP,
    ultimo_login TIMESTAMP,
    
    -- Audit fields (standard for all tables)
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    creado_por VARCHAR(50),
    actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    actualizado_por VARCHAR(50)
);

-- ============================================================================
-- 3. Create indexes for performance
-- ============================================================================
CREATE INDEX idx_usuario_username ON persona.usuario(username);
CREATE INDEX idx_usuario_email ON persona.usuario(email);
CREATE INDEX idx_usuario_is_active ON persona.usuario(is_active);
CREATE INDEX idx_usuario_creado_en ON persona.usuario(creado_en);

-- ============================================================================
-- 4. Create audit trigger function (reusable for all tables)
-- ============================================================================
CREATE OR REPLACE FUNCTION actualizar_timestamp_modificacion()
RETURNS TRIGGER AS $$
BEGIN
    NEW.actualizado_en = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- 5. Create trigger for usuario table
-- ============================================================================
CREATE TRIGGER trigger_actualizar_usuario_timestamp
    BEFORE UPDATE ON persona.usuario
    FOR EACH ROW
    EXECUTE FUNCTION actualizar_timestamp_modificacion();

-- ============================================================================
-- 6. Migrate data from old users table to new persona.usuario table
-- ============================================================================
INSERT INTO persona.usuario (
    username, 
    email, 
    password_hash, 
    is_active, 
    intentos_fallidos_login, 
    bloqueado_hasta, 
    ultimo_login,
    creado_en,
    creado_por,
    actualizado_en,
    actualizado_por
)
SELECT 
    username,
    email,
    password_hash,
    is_active,
    failed_login_attempts,
    locked_until,
    last_login,
    created_at,
    'SYSTEM', -- Default creator for migrated data
    updated_at,
    'SYSTEM'  -- Default updater for migrated data
FROM users;

-- ============================================================================
-- 7. Drop old users table and related objects
-- ============================================================================
DROP TRIGGER IF EXISTS update_users_updated_at ON users;
DROP FUNCTION IF EXISTS update_updated_at_column();
DROP TABLE IF EXISTS users;

-- ============================================================================
-- 8. Insert default admin user if not exists
-- ============================================================================
-- Password: admin123 (BCrypt encoded)
INSERT INTO persona.usuario (
    username, 
    email, 
    password_hash, 
    is_active,
    creado_por,
    actualizado_por
) 
VALUES (
    'admin', 
    'admin@frcefact.com', 
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 
    true,
    'SYSTEM',
    'SYSTEM'
) ON CONFLICT (username) DO NOTHING;

-- ============================================================================
-- 9. Add comments for documentation
-- ============================================================================
COMMENT ON SCHEMA persona IS 'Schema para entidades relacionadas con personas y usuarios';
COMMENT ON TABLE persona.usuario IS 'Tabla de usuarios del sistema con información de autenticación';
COMMENT ON COLUMN persona.usuario.id IS 'Identificador único del usuario';
COMMENT ON COLUMN persona.usuario.username IS 'Nombre de usuario único para login';
COMMENT ON COLUMN persona.usuario.email IS 'Correo electrónico único del usuario';
COMMENT ON COLUMN persona.usuario.password_hash IS 'Hash BCrypt de la contraseña';
COMMENT ON COLUMN persona.usuario.is_active IS 'Indica si el usuario está activo en el sistema';
COMMENT ON COLUMN persona.usuario.intentos_fallidos_login IS 'Contador de intentos fallidos de login';
COMMENT ON COLUMN persona.usuario.bloqueado_hasta IS 'Fecha hasta la cual el usuario está bloqueado';
COMMENT ON COLUMN persona.usuario.ultimo_login IS 'Fecha y hora del último login exitoso';
COMMENT ON COLUMN persona.usuario.creado_en IS 'Fecha y hora de creación del registro';
COMMENT ON COLUMN persona.usuario.creado_por IS 'Usuario que creó el registro';
COMMENT ON COLUMN persona.usuario.actualizado_en IS 'Fecha y hora de última actualización';
COMMENT ON COLUMN persona.usuario.actualizado_por IS 'Usuario que realizó la última actualización';
