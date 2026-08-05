# Reglas duras — Backend (Spring Boot)

Verificado 2026-08-05 contra código. Reglas que **no** se rompen. Fuente: `frc-efact-backend/CONTROLLER_ROUTING_RULE.md`, `SECURITY.md`, CLAUDE.md.

## 1. `@RequestMapping` SIN `/api/`
El `context-path: /api` está en `application.yml:60` — ya agrega el prefijo. Duplicarlo da rutas `/api/api/...` y `NoResourceFoundException`.

```java
@RequestMapping("/clientes")   // ✅  → resuelve a /api/clientes
@RequestMapping("/api/clientes") // ❌ → /api/api/clientes
```

### ⚠️ Deuda vigente — 3 controllers todavía violan la regla
Confirmado por grep (2026-08-05): resuelven a `/api/api/...` y el **frontend ya los consume con ese doble prefijo**. Corregir exige tocar controller **y** su api-service juntos, en un solo cambio:

| Controller | Actual (bug) | Ruta efectiva |
|---|---|---|
| `AuditLogController.java:28` | `/api/auditoria` | `/api/api/auditoria` |
| `GeografiaController.java:20` | `/api/geografia` | `/api/api/geografia` |
| `ReporteController.java:33` | `/api/reportes` | `/api/api/reportes` |

Al **crear** un controller nuevo: base sin `/api/`. Caso atípico válido: `TimbradoDetalleController` no define path base (cada método pone la ruta completa) — no viola la regla.

## 2. Idioma de campos
- **Español** para dominio: `razon_social`, `numero_factura`, `fecha_nacimiento`.
- **Inglés** para genéricos/técnicos: `id`, `username`, `email`, `is_active`, `password_hash`.

## 3. Nada en el esquema `public`
Toda tabla vive en un esquema lógico. Ver [database-standards.md](database-standards.md) para la lista real de esquemas.

## 4. Auditoría obligatoria en TODA tabla
`id BIGSERIAL PK` + `creado_en`, `creado_por`, `actualizado_en`, `actualizado_por` + trigger `actualizar_timestamp_modificacion()`. Las entidades JPA heredan de `AuditableEntity` (`model/base/`). Detalle en [database-standards.md](database-standards.md).

## 5. DTOs + mapper — no exponer entidades
El controller devuelve DTOs (`dto/`), nunca entidades JPA. El mapeo va en `dto/mapper/` (ej. `UsuarioMapper.toDto()`). Flujo entidad completo: `model → repository → dto → service → controller`.

## 6. `@PreAuthorize` por endpoint
Autorización a nivel método con roles Spring. Los `rolEmpresa` se mapean a authorities en cada request (`CustomUserDetailsService`), por eso basta el rol de empresa para pasar. `SecurityConfig` además protege por path (`/admin/**`, `/roles/**` → `hasRole("ADMIN")`; el resto `authenticated()`).

Referencia de roles por operación (ver CLAUDE.md):
| Operación | Roles |
|---|---|
| `GET /facturas` | ADMIN, EMPRESA_ADMIN, FACTURADOR, LECTOR |
| `POST /facturas` / notas / generar-DE | ADMIN, EMPRESA_ADMIN, FACTURADOR |
| Anular factura | ADMIN, EMPRESA_ADMIN |

## 7. Tests JPA con H2
Dependencia H2 ya incluida. Los tests de repositorio/JPA corren contra H2, no Postgres.

## 8. `ddl-auto: validate` — el esquema lo gestiona Flyway
Hibernate solo valida (`application.yml:17`). Cambios de esquema **únicamente** por migración Flyway versionada. Ver [database-standards.md](database-standards.md).

## 9. Compilar antes de commit
`cd frc-efact-backend && ./mvnw compile`. Si falla, **no** commitear — arreglar primero. (Push = deploy a producción vía Render.)
