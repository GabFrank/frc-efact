# Estándares DB — resumen accionable

Verificado 2026-08-05. Resumen de `frc-efact-backend/DATABASE_STANDARDS.md`. Índice de migraciones: [../reference/migrations-index.md](../reference/migrations-index.md).

## Esquemas — nada en `public`
Toda tabla vive en un esquema lógico. **Esquemas realmente creados por migraciones** (grep de `CREATE SCHEMA`):

`persona` · `empresa` · `financiero` · `productos` · `clientes` · `auditoria` · `geografia` · `transporte`

⚠️ El doc `DATABASE_STANDARDS.md` menciona `catalogo` y `factura` como ejemplos, pero **NO existen** — no los uses. Nombres de esquema/tabla/columna: **singular, snake_case**. Formato `esquema.tabla` (ej. `persona.usuario`).

## Campos de auditoría obligatorios (TODA tabla)
```sql
id BIGSERIAL PRIMARY KEY,
-- campos de negocio (español dominio / inglés genérico)
creado_en       TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
creado_por      VARCHAR(50),
actualizado_en  TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
actualizado_por VARCHAR(50)
```
Las entidades JPA heredan estos campos de `AuditableEntity` (`model/base/`).

## Trigger de timestamp
Función reutilizable `actualizar_timestamp_modificacion()` + un trigger por tabla:
```sql
CREATE TRIGGER trigger_actualizar_<tabla>_timestamp
    BEFORE UPDATE ON <esquema>.<tabla>
    FOR EACH ROW
    EXECUTE FUNCTION actualizar_timestamp_modificacion();
```

## Flyway — versionado, nunca modificar lo aplicado
- Archivos `V<N>__Descripcion.sql` en `db/migration/`.
- **Nunca** editar una migración ya aplicada — siempre crear una **nueva**.
- Hay 35 migraciones (`V1`–`V35`). **Próxima = `V36__...`**. Ver [../reference/migrations-index.md](../reference/migrations-index.md).
- Antes de crear una, revisar la última `V<N>` para no chocar el número.

## `ddl-auto: validate`
Hibernate **solo valida** el esquema contra las entidades (`application.yml:17`). No genera ni altera tablas. Cualquier cambio de esquema va exclusivamente por Flyway.

## Tipos recomendados
PK `BIGSERIAL`, FK `BIGINT`; dinero `NUMERIC(p,s)`; fechas `TIMESTAMP`; JSON `JSONB` (audit log usa JSONB antes/después).

## Checklist nueva tabla
- [ ] En un esquema (no `public`)
- [ ] `id BIGSERIAL PK` + 4 campos de auditoría
- [ ] Trigger `actualizar_timestamp_modificacion()`
- [ ] Índices en columnas de búsqueda/JOIN/FK
- [ ] Constraints (FK, UNIQUE, CHECK, NOT NULL)
- [ ] Migración `V36__...` versionada, sin tocar las previas
- [ ] Nomenclatura español/inglés correcta
