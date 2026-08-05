# Backend — capas (paquete `com.frcefact`)

Spring Boot 3.2.1 / Java 17. Arquitectura en capas clásica: **controller → service → repository → JPA**, con **DTO + mapper** en los bordes. Verificado 2026-08-05.

## Capas (flujo de una request)

```
controller/   REST, @RequestMapping (SIN /api/), @PreAuthorize   ── 22 controllers
   │  recibe/devuelve DTOs
dto/          DTOs + dto/mapper/ (entidad ⇄ DTO)                  ── 15 mappers
   │
service/      lógica de negocio, @Transactional                  ── ~34 services
   │          SIFEN aislado en service/sifen/
repository/   Spring Data JPA + repository/specification/         ── ~33 repos
   │
model/        entidades @Entity, heredan de model/base/           ── 30 entidades
              AuditableEntity
   ▼
PostgreSQL (esquemas de dominio, gestionados por Flyway)
```

## Paquetes

### `model/` — entidades JPA
30 `@Entity` (+ 6 enums). Todas heredan de **`model/base/AuditableEntity`** (`@MappedSuperclass` + `AuditingEntityListener`): campos `creadoEn` (`@CreatedDate`), `creadoPor` (`@CreatedBy`), `actualizadoEn` (`@LastModifiedDate`), `actualizadoPor` (`@LastModifiedBy`), con `@PrePersist`/`@PreUpdate` de respaldo. La `id BIGSERIAL` la define cada entidad. Auditing habilitado por `config/JpaAuditingConfig`. Detalle: [reference/entities-index.md](../reference/entities-index.md).

### `repository/`
Interfaces Spring Data JPA (~33). Consultas dinámicas en **`repository/specification/`** (JPA Criteria / `Specification`). Repos SIFEN exponen queries del scheduler (`findLotesEnProceso`, `findPendientesConCdc`, `findEventosPendientes`).

### `dto/` + `dto/mapper/`
DTOs de entrada/salida. Las entidades **nunca** se serializan crudas: los controllers devuelven DTOs vía mappers (`ClienteMapper`, `FacturaLegalMapper`, `EmpresaMapper`, `DocumentoElectronicoMapper`, `NotaCredito/Debito/RemisionMapper`, `ProductoMapper`, `ChoferMapper`, `RolMapper`, etc., 15 en total).
⚠️ `UsuarioMapper.toDto()` es el punto de fix del bug RBAC-1: no inyecta los `rolEmpresa` mapeados al array `roles` → ver [auth-seguridad.md](auth-seguridad.md) y [known-bugs.md](../reference/known-bugs.md).

### `service/`
Lógica de negocio, mayormente `@Transactional`. Servicios cross-cutting: `EncryptionService` (AES-256, CSC/passwords), `CertificadoService` (paths `.pfx`), `EmpresaSecurityService` (2ª capa de autorización multi-empresa, ver [auth-seguridad.md](auth-seguridad.md)) y el **email**:
- **`EmailService`** — service genérico de envío (SMTP Gmail); **no existe `MailService`**. La config de correo vive en `config/MailConfig`.
- **`EmailFacturaElectronicaService`** y **`EmailNotaRemisionService`** — especializados: arman el correo con el KuDE PDF adjunto de la factura/nota.

⚠️ `service/UserService.java` existe pero es **código muerto** (sin referencias); el service real de usuarios es `UsuarioService`. Ver [known-bugs.md](../reference/known-bugs.md) (QA-6).

Generación de DE: además de SIFEN, `XmlGeneratorService` produce un **XML simplificado** de la factura (más CDC de 44 chars con DV módulo 11, URL QR y código de seguridad). El XML **completo y firmado** que se envía a SIFEN lo arma `SifenService` con jsifenlib — `XmlGeneratorService` es un generador básico/preliminar, no el envío real.

**SIFEN aislado en `service/sifen/`** → ver [sifen-integracion.md](sifen-integracion.md):
- `SifenService` (3457 líneas / 176 KB) — armado del XML, envío, consulta.
- `SifenEventoService` (844 líneas) — cancelación / inutilización / nominación.
- `SifenSchedulerService` (101 líneas) — 3 jobs `@Scheduled`, polling de lotes/documentos/eventos.

### `controller/`
22 controllers REST. Regla dura: `@RequestMapping` **sin** prefijo `/api/` (el context-path lo agrega). ⚠️ `GeografiaController`, `AuditLogController`, `ReporteController` la violan → resuelven a `/api/api/...`. Autorización por método con `@PreAuthorize` (`@EnableMethodSecurity` en `SecurityConfig`). Endpoints + roles: [reference/endpoints-index.md](../reference/endpoints-index.md).

### `security/`
JWT local + Auth0 + rate limiting. `JwtAuthenticationFilter`, `JwtTokenProvider`, `CustomUserDetailsService`, `CustomJwtAuthenticationConverter`, `OAuth2TokenFilter`, `RateLimitingFilter`. Detalle completo en [auth-seguridad.md](auth-seguridad.md).

### `config/`
`SecurityConfig`, `JpaAuditingConfig`, `OpenApiConfig` (Swagger), `MailConfig`, `AsyncConfig` (`@Async`), `AopConfig`, `JacksonConfig`, `DatabaseConfig`, `DatabaseInitializer`, `FlywayConfig`.

### `aspect/` — `AuditAspect`
AOP (AspectJ): intercepta operaciones y persiste `AuditLog` (esquema `auditoria`, JSONB antes/después). Habilitado por `AopConfig`.

### `validation/`
Validadores de dominio con anotaciones custom (`@ValidRuc`/`RucValidator`, `@ValidCdc`/`CdcValidator`, `@ValidIva`, `@ValidTimbrado`, `@ValidRangoTimbrado`, `@ValidFechasTimbrado`, `@ValidClienteRuc`). El cálculo del DV de RUC del backend es correcto (a diferencia del front — ver known-bugs SEC-4).

### `exception/` — `GlobalExceptionHandler`
`@RestControllerAdvice` centraliza el manejo de errores. Excepciones de dominio: `BusinessException`, `ResourceNotFoundException`, `DuplicateResourceException`, `CertificadoException`.

### `sifen/util/` y `sifen/config/`
Helpers y config de la integración SIFEN (paquete `com.frcefact.sifen`, separado de `service/sifen/`):
- `sifen/util/`: `SifenTotalsHelper`, `SifenResponseParser`, `SifenDocumentoLogger`, `SifenReceptorHelper` (⚠️ existe pero **no se usa** — receptor duplicado a mano en `SifenService`).
- `sifen/config/`: `SifenConfigFactory` (config dinámica por empresa/timbrado, thread-safe con `synchronized`), `SifenProperties`.

Ver [sifen-integracion.md](sifen-integracion.md).
