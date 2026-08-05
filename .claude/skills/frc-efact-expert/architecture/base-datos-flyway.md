# Base de datos y Flyway

PostgreSQL 15+ gestionado íntegramente por **Flyway** (migraciones versionadas). Hibernate en `ddl-auto: validate` — **solo valida**, nunca crea/altera esquema. Verificado 2026-08-05.

## Esquemas reales (nada en `public`)

Creados por las migraciones — **no existe `catalogo`**:

| Esquema | Contenido |
|---|---|
| `persona` | `Usuario`, `Rol`, `UsuarioRol`, `UsuarioEmpresa` (RBAC + multi-empresa) |
| `empresa` | `Empresa`, `Timbrado`, `TimbradoDetalle` |
| `clientes` | `Cliente` |
| `productos` | `Producto` |
| `financiero` | `FacturaLegal`, `FacturaLegalItem`, `DocumentoElectronico`, `LoteDE`, eventos, notas |
| `geografia` | `Pais`, `Departamento`, `Ciudad`, `Distrito`, `Barrio` (precargados, V9) |
| `transporte` | `Vehiculo`, `Chofer` (V35) |
| `auditoria` | `AuditLog` (JSONB antes/después) |

Regla dura: toda tabla en un esquema de dominio, jamás en `public`. Estándares completos: [conventions/database-standards.md](../conventions/database-standards.md) y `frc-efact-backend/DATABASE_STANDARDS.md`.

## Auditoría obligatoria

Toda tabla lleva: `id BIGSERIAL PK`, `creado_en`, `creado_por`, `actualizado_en`, `actualizado_por`, más el trigger **`actualizar_timestamp_modificacion()`** que refresca `actualizado_en` en cada UPDATE.

En Java, las entidades heredan de **`model/base/AuditableEntity`** (`@MappedSuperclass` + `AuditingEntityListener`): `@CreatedDate`/`@CreatedBy`/`@LastModifiedDate`/`@LastModifiedBy` pueblan esos campos automáticamente. El auditing lo habilita `config/JpaAuditingConfig`. Aparte, `aspect/AuditAspect` (AOP) escribe el registro histórico en `auditoria.AuditLog`.

## Flyway — migraciones

- **35 migraciones aplicadas, `V1`–`V35`**, en `frc-efact-backend/src/main/resources/db/migration/`. Índice comentado: [reference/migrations-index.md](../reference/migrations-index.md).
- `baseline-on-migrate: true`.
- **Nunca** modificar una migración ya aplicada (rompe el checksum). Siempre crear la siguiente.
- Hitos: V2 (esquemas), V5 (estructura completa + roles), V9 (geografía), V22/V23 (DE varchar + eventos), V25/V26 (moneda extranjera), V28/V29 (notas), V35 (transporte).

### Cómo agregar una migración (`V36__...`)
1. Revisar el último `V35__*.sql` y respetar [DATABASE_STANDARDS.md](../conventions/database-standards.md) (esquema correcto, columnas de auditoría, trigger).
2. Crear `V36__descripcion_snake_case.sql` en `db/migration/`.
3. Reflejar el cambio en la entidad JPA (recordá `ddl-auto: validate`: si la entidad y el esquema no coinciden, la app **no arranca**).
4. Validar con `./check-migrations.sh` y `./mvnw compile` antes de commitear.

## Perfiles (`SPRING_PROFILES_ACTIVE`)

- **dev** (`application-dev.yml`): PostgreSQL en `jdbc:postgresql://172.25.0.36:5551/frc_efact_dev`.
- **prod** (`application-prod.yml`): `DATABASE_URL` (Render), `forward-headers-strategy: framework`, rate limiting habilitado.
- Base común en `application.yml`.

Tests JPA usan **H2** (dependencia incluida).
