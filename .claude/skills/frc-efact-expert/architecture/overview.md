# Overview — mapa mental de FRC eFact

Sistema **full-stack de facturación electrónica SIFEN** (SET Paraguay): emite y gestiona Documentos Electrónicos (DE) con firma digital, XML SIFEN, KuDE PDF y QR. Multi-empresa, multi-usuario, RBAC.

## Monorepo

```
frc-efact/
├── frc-efact-backend/    Spring Boot 3.2.1 · Java 17 · puerto 8080 · context-path /api
├── frc-efact-frontend/   Angular 17 (standalone) · NgRx 17 · puerto 4200
├── certificates/         .pfx para firma SIFEN (no se commitean)
├── docs/ · docs/sifen/   Documentación + manuales/XML SIFEN v150
├── deploy/               Stack VM Hetzner: .env.example, nginx vhost, backup systemd
├── docker-compose.prod.yml  Stack de producción (VM Hetzner)
├── .kiro/specs/          Specs por feature
└── render.yaml           Blueprint Render (legacy — Render suspendido)
```

- **Backend**: REST API bajo `/api`. PostgreSQL + Flyway (`V1`–`V35`, `ddl-auto: validate`). Seguridad JWT local + Auth0. Librería SIFEN `jsifenlib` (fork `io.github.gabfrank`).
- **Frontend**: SPA Angular Material. HTTP con interceptores; estado NgRx solo en 8 ramas (no todas las entidades). Auth0 + JWT local, JWT **en memoria**.
- **Deploy**: producción en **VM Hetzner** (`https://efact.frc-ecommerce.com`) desde el 2026-07-07; el deploy es **manual por SSH** (`docker compose up -d --build`). Un push a `main` solo corre `semantic-release`. Render suspendido como rollback. Ver [../workflows/deploy-hetzner.md](../workflows/deploy-hetzner.md).

## Cómo viaja un dato de punta a punta

Ejemplo: listar clientes de una empresa.

```
[Frontend]
  Component (features/clientes)
    → ClienteApiService (core/api)  →  HTTP GET ${apiUrl}/clientes/empresa/{id}
       (apiUrl = http://localhost:8080/api)  · interceptores: https → auth (Bearer) → error → mockRuc
        │
        ▼  red
[Backend]  context-path /api
  Filtros seguridad: RateLimiting → JwtAuthenticationFilter (valida token local,
     loadUserByUsername → mapea rolEmpresa→authorities EN CADA REQUEST) → OAuth2 (Auth0)
    → ClienteController  @RequestMapping("/clientes")  @PreAuthorize(...)
      → ClienteService (lógica de negocio, @Transactional)
        → ClienteRepository (Spring Data JPA + specification/)
          → Hibernate → PostgreSQL (esquema clientes)
      ← entidad Cliente
    ← ClienteMapper.toDto() → ClienteDto
  ← JSON
        │
        ▼
[Frontend]
  ApiService (Observable) → NgRx effect/reducer (si la entidad tiene store) o directo al component
    → template renderiza
```

La vuelta siempre pasa por un **DTO + mapper** (`dto/mapper/`): las entidades JPA nunca se serializan crudas. El frontend tiene interfaces en `models/` que son espejo de esos DTOs.

## Reglas de oro (ver §3 de SKILL.md)

- `@RequestMapping` **sin** `/api/` (context-path ya lo agrega). 3 controllers lo violan → `/api/api/...` (known-bugs).
- Esquemas DB de dominio, nada en `public`.
- Migración nueva = `V36__...`, nunca modificar una aplicada.
- URLs del front **sí** incluyen `/api/`.

## Los demás docs de architecture

| Doc | Tema |
|---|---|
| [backend-capas.md](backend-capas.md) | Paquetes `com.frcefact`: model→repo→dto/mapper→service→controller |
| [frontend-capas.md](frontend-capas.md) | `src/app`: models→core/api→NgRx→features, layout, rutas |
| [auth-seguridad.md](auth-seguridad.md) | JWT local + Auth0, `rolEmpresa`→authorities, SecurityConfig, `@PreAuthorize` |
| [base-datos-flyway.md](base-datos-flyway.md) | PostgreSQL, esquemas, Flyway, `AuditableEntity`, perfiles |
| [sifen-integracion.md](sifen-integracion.md) | jsifenlib, config por empresa, `SifenService`, scheduler |

Índices verificados: [reference/entities-index.md](../reference/entities-index.md), [endpoints-index.md](../reference/endpoints-index.md), [migrations-index.md](../reference/migrations-index.md), [ngrx-state-index.md](../reference/ngrx-state-index.md), [known-bugs.md](../reference/known-bugs.md).
