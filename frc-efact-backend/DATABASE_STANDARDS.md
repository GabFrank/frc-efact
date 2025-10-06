# Estándares de Base de Datos - FRC eFact

## Convenciones de Nomenclatura

### Idioma
- **Campos específicos del dominio**: Español
  - Ejemplos: `nombre_completo`, `fecha_nacimiento`, `numero_documento`, `razon_social`
- **Campos genéricos/técnicos**: Inglés
  - Ejemplos: `id`, `username`, `email`, `is_active`, `password_hash`

### Esquemas
- Todas las tablas deben estar organizadas en esquemas lógicos
- Nombres en singular, snake_case
- Ejemplos:
  - `persona` - Para entidades relacionadas con personas y usuarios
  - `factura` - Para documentos de facturación electrónica
  - `catalogo` - Para catálogos y tablas de configuración
  - `auditoria` - Para logs y auditoría del sistema

### Tablas
- Nombres en singular, snake_case
- Ejemplos: `usuario`, `documento_fiscal`, `producto`, `cliente`
- Formato completo: `esquema.tabla` (ej: `persona.usuario`)

### Columnas
- snake_case para todos los nombres
- Ejemplos: `creado_en`, `actualizado_por`, `numero_factura`

## Campos de Auditoría Obligatorios

Todas las tablas deben incluir estos campos:

```sql
-- Identificador único
id BIGSERIAL PRIMARY KEY,

-- Campos de auditoría (obligatorios)
creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
creado_por VARCHAR(50),
actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
actualizado_por VARCHAR(50)
```

## Función de Trigger para Auditoría

Función reutilizable para actualizar automáticamente `actualizado_en`:

```sql
CREATE OR REPLACE FUNCTION actualizar_timestamp_modificacion()
RETURNS TRIGGER AS $$
BEGIN
    NEW.actualizado_en = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
```

### Aplicación del Trigger

Para cada tabla, crear un trigger:

```sql
CREATE TRIGGER trigger_actualizar_[nombre_tabla]_timestamp
    BEFORE UPDATE ON [esquema].[nombre_tabla]
    FOR EACH ROW
    EXECUTE FUNCTION actualizar_timestamp_modificacion();
```

## Estructura de Esquemas

### Crear Esquemas con Documentación

```sql
-- Esquema para personas y usuarios
CREATE SCHEMA IF NOT EXISTS persona;
COMMENT ON SCHEMA persona IS 'Schema para entidades relacionadas con personas y usuarios';

-- Esquema para facturación
CREATE SCHEMA IF NOT EXISTS factura;
COMMENT ON SCHEMA factura IS 'Schema para documentos de facturación electrónica';

-- Esquema para catálogos
CREATE SCHEMA IF NOT EXISTS catalogo;
COMMENT ON SCHEMA catalogo IS 'Schema para catálogos y tablas de configuración';

-- Esquema para auditoría
CREATE SCHEMA IF NOT EXISTS auditoria;
COMMENT ON SCHEMA auditoria IS 'Schema para logs y auditoría del sistema';
```

## Ejemplo Completo de Tabla

```sql
-- Crear tabla con todos los estándares
CREATE TABLE persona.usuario (
    -- Primary key
    id BIGSERIAL PRIMARY KEY,
    
    -- Campos de negocio (mezcla español/inglés según corresponda)
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    nombre_completo VARCHAR(200),
    
    -- Campos de estado
    is_active BOOLEAN DEFAULT true,
    intentos_fallidos_login INTEGER DEFAULT 0,
    bloqueado_hasta TIMESTAMP,
    ultimo_login TIMESTAMP,
    
    -- Campos de auditoría (obligatorios)
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    creado_por VARCHAR(50),
    actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    actualizado_por VARCHAR(50)
);

-- Índices para performance
CREATE INDEX idx_usuario_username ON persona.usuario(username);
CREATE INDEX idx_usuario_email ON persona.usuario(email);
CREATE INDEX idx_usuario_is_active ON persona.usuario(is_active);
CREATE INDEX idx_usuario_creado_en ON persona.usuario(creado_en);

-- Trigger para actualización automática
CREATE TRIGGER trigger_actualizar_usuario_timestamp
    BEFORE UPDATE ON persona.usuario
    FOR EACH ROW
    EXECUTE FUNCTION actualizar_timestamp_modificacion();

-- Comentarios para documentación
COMMENT ON TABLE persona.usuario IS 'Tabla de usuarios del sistema con información de autenticación';
COMMENT ON COLUMN persona.usuario.id IS 'Identificador único del usuario';
COMMENT ON COLUMN persona.usuario.username IS 'Nombre de usuario único para login';
COMMENT ON COLUMN persona.usuario.email IS 'Correo electrónico único del usuario';
COMMENT ON COLUMN persona.usuario.password_hash IS 'Hash BCrypt de la contraseña';
COMMENT ON COLUMN persona.usuario.nombre_completo IS 'Nombre completo del usuario';
COMMENT ON COLUMN persona.usuario.is_active IS 'Indica si el usuario está activo en el sistema';
COMMENT ON COLUMN persona.usuario.intentos_fallidos_login IS 'Contador de intentos fallidos de login';
COMMENT ON COLUMN persona.usuario.bloqueado_hasta IS 'Fecha hasta la cual el usuario está bloqueado';
COMMENT ON COLUMN persona.usuario.ultimo_login IS 'Fecha y hora del último login exitoso';
COMMENT ON COLUMN persona.usuario.creado_en IS 'Fecha y hora de creación del registro';
COMMENT ON COLUMN persona.usuario.creado_por IS 'Usuario que creó el registro';
COMMENT ON COLUMN persona.usuario.actualizado_en IS 'Fecha y hora de última actualización';
COMMENT ON COLUMN persona.usuario.actualizado_por IS 'Usuario que realizó la última actualización';
```

## Migraciones con Flyway

### Nomenclatura de Archivos

```
V1__Initial_schema.sql
V2__Create_persona_schema.sql
V3__Create_usuario_table.sql
V4__Add_usuario_indexes.sql
V5__Create_factura_schema.sql
```

### Estructura de Migración

```sql
-- V[número]__[descripción].sql
-- Ejemplo: V2__Refactor_to_schema_based_structure.sql

-- 1. Crear esquemas
CREATE SCHEMA IF NOT EXISTS [nombre_esquema];
COMMENT ON SCHEMA [nombre_esquema] IS '[descripción]';

-- 2. Crear tablas con campos de auditoría
CREATE TABLE [esquema].[tabla] (
    id BIGSERIAL PRIMARY KEY,
    -- campos de negocio
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    creado_por VARCHAR(50),
    actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    actualizado_por VARCHAR(50)
);

-- 3. Crear índices
CREATE INDEX idx_[tabla]_[campo] ON [esquema].[tabla]([campo]);

-- 4. Crear triggers
CREATE TRIGGER trigger_actualizar_[tabla]_timestamp
    BEFORE UPDATE ON [esquema].[tabla]
    FOR EACH ROW
    EXECUTE FUNCTION actualizar_timestamp_modificacion();

-- 5. Agregar comentarios
COMMENT ON TABLE [esquema].[tabla] IS '[descripción]';
COMMENT ON COLUMN [esquema].[tabla].[columna] IS '[descripción]';
```

## Tipos de Datos Recomendados

### Identificadores
- `BIGSERIAL` para PKs (auto-incremento)
- `BIGINT` para FKs

### Texto
- `VARCHAR(n)` para texto con límite conocido
- `TEXT` para texto sin límite definido

### Números
- `INTEGER` para enteros pequeños
- `BIGINT` para enteros grandes
- `NUMERIC(p,s)` para decimales precisos (dinero)
- `DECIMAL(p,s)` equivalente a NUMERIC

### Fechas y Tiempo
- `TIMESTAMP` para fecha y hora (sin zona horaria)
- `TIMESTAMPTZ` para fecha y hora con zona horaria
- `DATE` solo para fechas
- `TIME` solo para horas

### Booleanos
- `BOOLEAN` para true/false

### JSON
- `JSONB` para datos JSON (indexable y más eficiente)
- `JSON` para datos JSON simples

## Índices

### Cuándo Crear Índices
- Columnas usadas en WHERE frecuentemente
- Columnas usadas en JOIN
- Columnas usadas en ORDER BY
- Columnas UNIQUE

### Tipos de Índices
```sql
-- Índice simple
CREATE INDEX idx_tabla_columna ON esquema.tabla(columna);

-- Índice compuesto
CREATE INDEX idx_tabla_col1_col2 ON esquema.tabla(col1, col2);

-- Índice único
CREATE UNIQUE INDEX idx_tabla_columna_unique ON esquema.tabla(columna);

-- Índice parcial
CREATE INDEX idx_tabla_activos ON esquema.tabla(columna) WHERE is_active = true;
```

## Constraints

### Primary Key
```sql
id BIGSERIAL PRIMARY KEY
```

### Foreign Key
```sql
usuario_id BIGINT NOT NULL,
CONSTRAINT fk_tabla_usuario FOREIGN KEY (usuario_id) 
    REFERENCES persona.usuario(id) 
    ON DELETE CASCADE
```

### Unique
```sql
email VARCHAR(100) UNIQUE NOT NULL
```

### Check
```sql
edad INTEGER CHECK (edad >= 0 AND edad <= 150)
```

### Not Null
```sql
nombre VARCHAR(100) NOT NULL
```

## Mejores Prácticas

1. **Siempre usar esquemas** - No crear tablas en el esquema `public`
2. **Campos de auditoría obligatorios** - Todas las tablas deben tenerlos
3. **Comentarios SQL** - Documentar tablas y columnas importantes
4. **Índices estratégicos** - Crear índices basados en queries reales
5. **Nomenclatura consistente** - Seguir las convenciones establecidas
6. **Migraciones versionadas** - Usar Flyway para todos los cambios
7. **No modificar migraciones existentes** - Crear nuevas migraciones para cambios
8. **Triggers para auditoría** - Automatizar actualización de timestamps
9. **Constraints apropiados** - Usar FK, UNIQUE, CHECK según corresponda
10. **Tipos de datos correctos** - Elegir el tipo más apropiado para cada campo

## Checklist para Nueva Tabla

- [ ] Tabla creada en un esquema (no en `public`)
- [ ] Campo `id BIGSERIAL PRIMARY KEY`
- [ ] Campos de auditoría: `creado_en`, `creado_por`, `actualizado_en`, `actualizado_por`
- [ ] Trigger para actualizar `actualizado_en`
- [ ] Índices en columnas de búsqueda frecuente
- [ ] Constraints apropiados (FK, UNIQUE, CHECK, NOT NULL)
- [ ] Comentarios en tabla y columnas principales
- [ ] Migración Flyway versionada correctamente
- [ ] Nomenclatura consistente (español/inglés según corresponda)
