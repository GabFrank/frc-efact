---
name: frc-efact-expert
description: Experto integral del sistema FRC eFact — facturación electrónica SIFEN (Paraguay/SET). Spring Boot 3.2.1 + Angular 17 + PostgreSQL/Flyway, multi-empresa, firma digital, XML SIFEN, KuDE PDF, notas C/D/R, eventos, transporte. Conoce arquitectura, dominios, convenciones, endpoints, migraciones y bugs conocidos. Activar al trabajar en cualquier parte de este repo.
license: Proprietary
---

# FRC eFact Expert

Soy el experto interno del sistema **FRC eFact**, la plataforma web de emisión de Documentos Electrónicos (DE) conforme a **SIFEN** (SET Paraguay). Al activarme asumo contexto completo: no redescubro patrones ni pregunto dónde está cada cosa.

> **Cómo está organizada esta skill:** este archivo es el índice. Cada tema apunta a un documento dedicado en `architecture/`, `domains/`, `conventions/`, `workflows/` o `reference/`. Cargá **solo** los documentos relevantes a la tarea, no todos a la vez.

> **Fidelidad:** esta skill fue verificada contra el código el **2026-08-05**. Los datos de `reference/` salen directo del código. Aun así, antes de editar un archivo o afirmar la firma de un símbolo, **leelo** — los nombres pueden cambiar.

---

## 1. Quick facts (siempre verdadero)

- **Stack backend:** Java 17, **Spring Boot 3.2.1**, Maven, **PostgreSQL 15+ con Flyway** (35 migraciones, `V1`–`V35`; `ddl-auto: validate`). Seguridad: Spring Security 6 + **JWT local** + **Auth0** (ambas vías). Puerto **8080**, **context-path `/api`**.
- **Stack frontend:** **Angular 17.3 (standalone) + Angular Material 17 + SCSS + NgRx 17.2**. **NO usa Tailwind.** Chart.js 4.5, @auth0/auth0-angular 2.3. Puerto **4200**.
- **Librería SIFEN:** fork interno `io.github.gabfrank:rshk-jsifenlib:0.2.4-frc.13` desde **GitHub Packages** (requiere `GITHUB_USERNAME`/`GITHUB_TOKEN` en el build). KuDE PDF con JasperReports, QR con ZXing.
- **Dominio:** Empresa → Timbrado → Punto de expedición → **FacturaLegal** → **DocumentoElectronico** (CDC 44 chars, firma, QR) → **LoteDE** → envío SIFEN → polling scheduler → APROBADO/RECHAZADO → **Eventos** (cancelación / inutilización / nominación). Notas de Crédito/Débito/Remisión con numeración propia. Multi-empresa con certificado `.pfx` por empresa.
- **Credenciales dev (verificado en Flyway V4):** `admin` / **`admin123`** y `testuser` / `test123`. Los roles EMPRESA_ADMIN/FACTURADOR/LECTOR **no** son usuarios sembrados — se asignan vía `rolEmpresa` al vincular usuario↔empresa.
- **Esquemas DB reales:** `persona`, `empresa`, `financiero`, `productos`, `clientes`, `auditoria`, `geografia`, `transporte`. **Nada en `public`. No existe `catalogo`.**
- **Deploy:** producción corre en una **VM Hetzner** (`https://efact.frc-ecommerce.com`) desde el **2026-07-07**. El deploy es **manual por SSH** (`docker compose up -d --build`), **no** por git. Un push a `main` solo dispara `semantic-release` en GitHub Actions. **Render fue descartado** (2026-08-05). El rollback hoy es el repo `frc-efact-legacy`, que la VM tiene como remoto `legacy`.
- **Comandos:**
  - Backend: `./dev.sh` (dev), `./mvnw compile` (**verificar compilación antes de commit**), `./mvnw spring-boot:run`, `./mvnw test`.
  - Frontend: `npm start` (dev), `npm run build:dev` / `npm run build:prod`, `npm run test:ci`. ⚠️ `npm run lint` **no funciona** — el target no existe en `angular.json`.
  - Swagger: `http://localhost:8080/swagger-ui.html`. Health: `http://localhost:8080/api/actuator/health`.

---

## 2. Cómo navegar esta skill

**Si la tarea es sobre…**

| Tema | Cargar |
|---|---|
| Estructura general, capas backend/frontend, cómo viaja un dato | [architecture/overview.md](architecture/overview.md) |
| Capas backend (model→repo→dto/mapper→service→controller), paquetes | [architecture/backend-capas.md](architecture/backend-capas.md) |
| Capas frontend (models→core/api→NgRx→features), shell, layout | [architecture/frontend-capas.md](architecture/frontend-capas.md) |
| Login, JWT local, Auth0, roles globales vs `rolEmpresa`, `@PreAuthorize` | [architecture/auth-seguridad.md](architecture/auth-seguridad.md) |
| PostgreSQL, esquemas, Flyway, auditoría, `AuditableEntity` | [architecture/base-datos-flyway.md](architecture/base-datos-flyway.md) |
| Cómo se integra jsifenlib, `SifenService`, config por empresa, scheduler | [architecture/sifen-integracion.md](architecture/sifen-integracion.md) |
| **Facturación** (FacturaLegal, ítems, moneda extranjera, generar-de) | [domains/facturacion.md](domains/facturacion.md) |
| **Documento Electrónico + Lote** (CDC, firma, QR, envío, polling, KuDE) | [domains/documento-electronico-lote.md](domains/documento-electronico-lote.md) |
| **SIFEN core** (armado del XML, receptor, totales, monedas, gotchas E605b/E644a) | [domains/sifen-core.md](domains/sifen-core.md) |
| **Notas** de crédito/débito/remisión (herencia de moneda, numeración) | [domains/notas-cdr.md](domains/notas-cdr.md) |
| **Eventos** (cancelación, inutilización, nominación) | [domains/eventos.md](domains/eventos.md) |
| **Clientes y Productos** (tipos SIFEN, RUC, tipo transacción, IVA) | [domains/clientes-productos.md](domains/clientes-productos.md) |
| **Timbrados** y puntos de expedición (físico/electrónico, CSC) | [domains/timbrados.md](domains/timbrados.md) |
| **Empresas / multi-empresa** (certificados `.pfx`, branding, vinculación usuarios) | [domains/empresas-multiempresa.md](domains/empresas-multiempresa.md) |
| **Transporte** (vehículos, choferes, nota de remisión) | [domains/transporte-remision.md](domains/transporte-remision.md) |
| **Usuarios, roles y permisos** (RBAC, el bug de permisos frontend) | [domains/usuarios-roles-permisos.md](domains/usuarios-roles-permisos.md) |
| **Dashboard, reportes y auditoría** | [domains/dashboard-reportes-auditoria.md](domains/dashboard-reportes-auditoria.md) |
| Reglas de código backend (routing, idioma, esquemas) | [conventions/backend-rules.md](conventions/backend-rules.md) |
| Reglas de código frontend (URLs API, NgRx, JWT en memoria) | [conventions/frontend-rules.md](conventions/frontend-rules.md) |
| Estándares DB (auditoría, triggers, esquemas, Flyway) | [conventions/database-standards.md](conventions/database-standards.md) |
| Trampas SIFEN (campos que rompen validación) | [conventions/sifen-gotchas.md](conventions/sifen-gotchas.md) |
| Agregar una entidad de punta a punta | [workflows/add-new-entity.md](workflows/add-new-entity.md) |
| Generar y enviar un DE a SIFEN | [workflows/generar-y-enviar-de.md](workflows/generar-y-enviar-de.md) |
| Deploy a producción (VM Hetzner, env vars, SSH) | [workflows/deploy-hetzner.md](workflows/deploy-hetzner.md) |
| Debuggear un rechazo SIFEN (E605b, E644a, NRE…) | [workflows/debug-sifen-errors.md](workflows/debug-sifen-errors.md) |
| Índice de entidades | [reference/entities-index.md](reference/entities-index.md) |
| Índice de endpoints + roles | [reference/endpoints-index.md](reference/endpoints-index.md) |
| Catálogo de enums | [reference/enums-index.md](reference/enums-index.md) |
| Índice de migraciones Flyway | [reference/migrations-index.md](reference/migrations-index.md) |
| Índice NgRx / servicios / interceptores frontend | [reference/ngrx-state-index.md](reference/ngrx-state-index.md) |
| **Bugs conocidos y deuda técnica** | [reference/known-bugs.md](reference/known-bugs.md) |

---

## 3. Reglas duras del proyecto (no negociables)

1. **`@RequestMapping` sin `/api/`** — el context-path `/api` ya lo agrega. Usar `@RequestMapping("/clientes")`, no `"/api/clientes"`. (Hay 3 controllers que aún lo violan → ver known-bugs.)
2. **Idioma de campos:** español para dominio (`razon_social`, `numero_factura`), inglés para genéricos (`id`, `username`, `is_active`).
3. **Nada en el esquema `public`** — usar los esquemas de dominio. Toda tabla con campos de auditoría (`id BIGSERIAL`, `creado_en/por`, `actualizado_en/por`) + trigger `actualizar_timestamp_modificacion()`.
4. **Migraciones Flyway versionadas** (`V36__...`); **nunca** modificar una ya aplicada. `ddl-auto: validate`.
5. **URLs API desde el frontend incluyen `/api/`**: `${environment.apiUrl}/clientes`.
6. **JWT en memoria** (no `localStorage`). Una rama NgRx por entidad principal (pero **no todas la tienen** — ver ngrx-index).
7. **Compilar antes de commit/push.** Backend: `./mvnw compile`. Frontend: `npm run build:dev` (⚠️ `npm run lint` no existe). Si falla, **no commitear**.
8. **⚠️ El deploy a prod es manual por SSH a la VM Hetzner** — `git push` no despliega. Preguntar SIEMPRE antes de `commit`+`push`, y con más razón antes de tocar la VM (es **compartida** con otros servicios productivos). Ver [workflows/deploy-hetzner.md](workflows/deploy-hetzner.md).
9. **No commitear secretos** ni `.pfx`.
10. **Antes de tocar `SifenService`:** leer [conventions/sifen-gotchas.md](conventions/sifen-gotchas.md) y los manuales de `docs/sifen/`. El XML SIFEN es estrictísimo — un campo de más/menos rompe la validación (E605b, E644a, etc.).

---

## 4. Estado actual del repo (snapshot 2026-08-05)

> Esta sección puede quedar desactualizada. Si preguntan por estado actual, revisar `git log` antes de responder.

- **Rama de trabajo actual:** `docs/integracion-hetzner` (integra la auditoría de documentación sobre la rama de la migración a Hetzner). Rama de releases: **`main`**, último release **v1.0.3** — ⚠️ `main` **todavía no tiene** la migración a Hetzner ni esta auditoría; ambas están pendientes de merge.
- **Infra:** producción en VM Hetzner desde el 2026-07-07 (`https://efact.frc-ecommerce.com`). Deploy por el workflow `deploy.yml` (`workflow_dispatch`) o SSH manual — nunca automático. Render descartado. Ver [workflows/deploy-hetzner.md](workflows/deploy-hetzner.md) y `docs/deployment/hetzner/RUNBOOK_VM.md`.
- **`CLAUDE.md` es la fuente de verdad de más alto nivel** — actualizado y confiable. Esta skill lo complementa con detalle verificado por dominio.
- **Feature más activa:** Notas de crédito/débito/remisión (heredan moneda/items de la factura referenciada, KuDE vía endpoint `kude-pdf`). Transporte (vehículo/chofer, V35) es lo más reciente.
- **Deuda técnica conocida y verificada** (detalle en [reference/known-bugs.md](reference/known-bugs.md)):
  - 🐛 `PermissionsService` (frontend) solo lee `user.roles`, no `rolEmpresa` → oculta menús aunque el backend autorice.
  - 🐛 Validación de RUC del frontend **deshabilitada**; `mock-ruc.interceptor` sirve RUCs ficticios (incluso en prod). El backend RUC ya es correcto.
  - 🐛 Routing `/api/api/...` en `Geografia`/`AuditLog`/`Reporte` controllers.
  - 🐛 `iTipCont` del emisor hardcodeado `PERSONA_JURIDICA` en `SifenService`.
  - ⚠️ Inutilización de números funciona parcialmente.
- **Documentación:** auditada y corregida el 2026-08-05 para ser fiel al código. Manuales normativos SIFEN v150 en `docs/sifen/` son confiables.

---

## 5. Antes de actuar, recordá

- **Verificá el código antes de afirmar:** esta skill describe el sistema en un momento; nombres de archivos/símbolos pueden haber cambiado. Si vas a editar o recomendar, **leé primero**.
- **CLAUDE.md gana** si algo acá lo contradice — y luego actualizá esta skill.
- **No toques `SifenService` a ciegas** — leé los gotchas y los manuales primero.

---

## 6. Modo de trabajo con el usuario (Gabriel)

- Habla **español** (rioplatense/paraguayo). Respondé en español salvo que escriba en otro idioma.
- Prefiere **respuestas cortas y directas**, sin resúmenes redundantes al final.
- **Deploy manual por SSH a la VM:** nunca pushear ni deployar sin confirmación explícita.
- Antes de marcar un fix como "resuelto", esperar validación del usuario probándolo.

---

*Este es el índice. Para cada tarea concreta, cargá el documento específico que aplique.*
