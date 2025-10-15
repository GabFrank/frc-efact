-- Crear esquema de geografía
CREATE SCHEMA IF NOT EXISTS geografia;

-- Tabla de países
CREATE TABLE geografia.pais (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(3) UNIQUE NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de departamentos
CREATE TABLE geografia.departamento (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(10) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    pais_id BIGINT NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (pais_id) REFERENCES geografia.pais(id),
    UNIQUE(codigo, pais_id)
);

-- Tabla de distritos
CREATE TABLE geografia.distrito (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(10) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    departamento_id BIGINT NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (departamento_id) REFERENCES geografia.departamento(id),
    UNIQUE(codigo, departamento_id)
);

-- Tabla de ciudades
CREATE TABLE geografia.ciudad (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(10) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    distrito_id BIGINT NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (distrito_id) REFERENCES geografia.distrito(id),
    UNIQUE(codigo, distrito_id)
);

-- Tabla de barrios
CREATE TABLE geografia.barrio (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(10) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    ciudad_id BIGINT NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ciudad_id) REFERENCES geografia.ciudad(id),
    UNIQUE(codigo, ciudad_id)
);

-- Índices para mejorar rendimiento
CREATE INDEX idx_departamento_pais ON geografia.departamento(pais_id);
CREATE INDEX idx_departamento_codigo ON geografia.departamento(codigo);
CREATE INDEX idx_departamento_nombre ON geografia.departamento(nombre);

CREATE INDEX idx_distrito_departamento ON geografia.distrito(departamento_id);
CREATE INDEX idx_distrito_codigo ON geografia.distrito(codigo);
CREATE INDEX idx_distrito_nombre ON geografia.distrito(nombre);

CREATE INDEX idx_ciudad_distrito ON geografia.ciudad(distrito_id);
CREATE INDEX idx_ciudad_codigo ON geografia.ciudad(codigo);
CREATE INDEX idx_ciudad_nombre ON geografia.ciudad(nombre);

CREATE INDEX idx_barrio_ciudad ON geografia.barrio(ciudad_id);
CREATE INDEX idx_barrio_codigo ON geografia.barrio(codigo);
CREATE INDEX idx_barrio_nombre ON geografia.barrio(nombre);

-- Insertar Paraguay como país base
INSERT INTO geografia.pais (codigo, nombre) VALUES ('PY', 'PARAGUAY');

-- Comentarios en las tablas
COMMENT ON TABLE geografia.pais IS 'Tabla de países del sistema geográfico';
COMMENT ON TABLE geografia.departamento IS 'Tabla de departamentos según SIFEN';
COMMENT ON TABLE geografia.distrito IS 'Tabla de distritos según SIFEN';
COMMENT ON TABLE geografia.ciudad IS 'Tabla de ciudades/localidades según SIFEN';
COMMENT ON TABLE geografia.barrio IS 'Tabla de barrios según SIFEN';