# FRC eFact — Sistema de Facturación Electrónica (Paraguay / SIFEN)

Sistema web full-stack para emitir y gestionar **Documentos Electrónicos (DE)** conforme a la normativa **SIFEN** (Sistema Integrado de Facturación Electrónica Nacional) de la **SET** paraguaya. Multi-empresa, multi-usuario, con firma digital, generación de XML, envío a SIFEN, KuDE PDF y QR.

---

## Stack

### Backend ([frc-efact-backend/](frc-efact-backend/))
- **Java 17**, **Spring Boot 3.2.1**, Maven
- **PostgreSQL 15+** con **Flyway** (migraciones versionadas en [frc-efact-backend/src/main/resources/db/migration/](frc-efact-backend/src/main/resources/db/migration/))
- **Spring Security 6** + **JWT (jjwt 0.12.3)** + **Auth0 OAuth2 Resource Server** (ambas vías de autenticación)
- **JPA / Hibernate 6** (`ddl-auto: validate` — los cambios de esquema van por Flyway)
- **jsifenlib `0.2.4-frc.13`** (fork interno: `io.github.gabfrank` desde GitHub Packages `GabFrank/rshk-jsifenlib`) — librería SIFEN
- **JasperReports 6.20.0** para PDFs (KuDE)
- **ZXing** para QR
- **Apache POI** para Excel
- **AspectJ AOP** para auditoría (annotation-driven: `AuditAspect` intercepta métodos anotados con `@Auditable`, no todo write automáticamente)
- **Lombok**, **springdoc-openapi 2.3.0** (Swagger en `/swagger-ui.html`)
- SOAP (`spring-boot-starter-web-services` + `javax.xml.soap` + `saaj-impl`) requerido por jsifenlib

### Frontend ([frc-efact-frontend/](frc-efact-frontend/))
- **Angular 17** (standalone components)
- **Angular Material 17** + SCSS
- **NgRx 17** (store, effects, entity, devtools) — un store por entidad principal
- **@auth0/auth0-angular 2.3** para login Auth0
- **Chart.js 4.5** para dashboards
- **TypeScript 5.4**, RxJS 7.8

---

## Comandos esenciales

### Backend ([frc-efact-backend/](frc-efact-backend/))
```bash
./dev.sh                       # Modo desarrollo (recomendado)
./mvnw spring-boot:run         # Run normal
./mvnw clean package           # Build
./mvnw test                    # Tests
./setup-local-db.sh            # Setup PostgreSQL local
./check-migrations.sh          # Validar migraciones Flyway
```
Puerto: **8080** — context-path: **`/api`** — Swagger: http://localhost:8080/swagger-ui.html

### Frontend ([frc-efact-frontend/](frc-efact-frontend/))
```bash
npm start                      # ng serve → http://localhost:4200
npm run build:prod             # Build producción
npm run test:ci                # Tests headless
npm run lint                   # ⚠️ NO FUNCIONA: el target `lint` no existe en angular.json
```

### Credenciales por defecto (dev)
> ⚠️ **Verificado contra migraciones Flyway (V3/V4):** solo existen **dos usuarios sembrados**. Los roles `EMPRESA_ADMIN` / `FACTURADOR` / `LECTOR` existen (V5) pero **no** como usuarios pre-cargados — se asignan vinculando usuarios a empresas (`rolEmpresa`).

| Usuario | Password | Rol | Sembrado en |
|---------|----------|-----|-------------|
| `admin` | `admin123` | ADMIN | V4 |
| `testuser` | `test123` | (sin rol global) | V3/V4 |

---

## Arquitectura general

```
frc-efact/
├── frc-efact-backend/         # Spring Boot API (puerto 8080, context /api)
├── frc-efact-frontend/        # Angular SPA (puerto 4200)
├── certificates/              # Certificados .pfx para firma SIFEN
├── deployment-data/           # Datos para deployment
├── deploy/                    # Stack VM Hetzner: .env.example, nginx vhost, backup systemd
├── docker-compose.prod.yml    # Stack de producción (VM Hetzner)
├── docs/                      # Documentación funcional/técnica
│   ├── sifen/                 # Manuales y XML de ejemplo SIFEN v150
│   └── deployment/hetzner/    # Plan y runbook de la migración a la VM
├── .kiro/specs/               # Specs (requirements/design/tasks) por feature
└── render.yaml                # Blueprint Render (legacy — Render descartado)
```

### Backend — capas (paquete `com.frcefact`)
- [model/](frc-efact-backend/src/main/java/com/frcefact/model/) — Entidades JPA. Hereda de [base/](frc-efact-backend/src/main/java/com/frcefact/model/base/) (`AuditableEntity` con `creadoEn/creadoPor/actualizadoEn/actualizadoPor`)
- [repository/](frc-efact-backend/src/main/java/com/frcefact/repository/) — Spring Data JPA + [specification/](frc-efact-backend/src/main/java/com/frcefact/repository/specification/)
- [dto/](frc-efact-backend/src/main/java/com/frcefact/dto/) — DTOs + [mapper/](frc-efact-backend/src/main/java/com/frcefact/dto/mapper/)
- [service/](frc-efact-backend/src/main/java/com/frcefact/service/) — Lógica de negocio. SIFEN aislado en [service/sifen/](frc-efact-backend/src/main/java/com/frcefact/service/sifen/) (`SifenService`, `SifenEventoService`, `SifenSchedulerService`)
- [controller/](frc-efact-backend/src/main/java/com/frcefact/controller/) — REST controllers
- [security/](frc-efact-backend/src/main/java/com/frcefact/security/) — JWT, OAuth2 Auth0, RateLimiting
- [config/](frc-efact-backend/src/main/java/com/frcefact/config/) — `SecurityConfig`, `JpaAuditingConfig`, `OpenApiConfig`, `MailConfig`, `AsyncConfig`, `AopConfig`
- [aspect/](frc-efact-backend/src/main/java/com/frcefact/aspect/) — `AuditAspect` (AOP) para audit log
- [validation/](frc-efact-backend/src/main/java/com/frcefact/validation/) — Validaciones dominio (RUC, CDC)
- [exception/](frc-efact-backend/src/main/java/com/frcefact/exception/) — `GlobalExceptionHandler`
- [sifen/util/](frc-efact-backend/src/main/java/com/frcefact/sifen/util/) — Helpers: `SifenReceptorHelper`, `SifenTotalsHelper`, `SifenResponseParser`, `SifenDocumentoLogger`
- [sifen/config/](frc-efact-backend/src/main/java/com/frcefact/sifen/config/) — `SifenConfigFactory`, `SifenProperties`

### Frontend — capas ([frc-efact-frontend/src/app/](frc-efact-frontend/src/app/))
- [models/](frc-efact-frontend/src/app/models/) — Interfaces TS (espejo de DTOs)
- [core/api/](frc-efact-frontend/src/app/core/api/) — Servicios HTTP por entidad
- [core/state/](frc-efact-frontend/src/app/core/state/) — NgRx (`actions/effects/reducer/selectors`) por entidad: `auth`, `usuarios`, `empresas`, `facturacion`, `documentos`, `notas`, `timbrados`, `timbrado-detalles`
- [core/services/](frc-efact-frontend/src/app/core/services/) — Servicios cross-cutting
- [core/interceptors/](frc-efact-frontend/src/app/core/interceptors/) — Auth, errores, HTTPS
- [features/](frc-efact-frontend/src/app/features/) — Páginas por dominio: `dashboard`, `facturacion`, `documentos`, `notas`, `clientes`, `productos`, `empresas`, `usuarios`, `timbrados`, `transporte` (vehículos/choferes), `auditoria`, `reportes`
- [shared/components/](frc-efact-frontend/src/app/shared/components/) — Diálogos, tablas, autocompletes, charts, etc.
- [layout/](frc-efact-frontend/src/app/layout/) — Layout principal (sidebar/topbar)
- [guards/](frc-efact-frontend/src/app/guards/) — Protección de rutas

Detalle completo del flujo entidad-por-entidad: [docs/FLUJO_SISTEMA_ENTIDADES.md](docs/FLUJO_SISTEMA_ENTIDADES.md)

---

## Dominio funcional

El sistema gestiona **Empresas → Timbrados → Puntos de expedición → Facturas legales → Documentos Electrónicos (DE) → Lotes → Eventos**.

### Entidades principales
- **Empresa**, **UsuarioEmpresa** (multi-empresa)
- **Timbrado**, **TimbradoDetalle** (puntos de expedición físico/electrónico)
- **Cliente** (PF/PJ/EG, contribuyente o no, con campos SIFEN)
- **Producto** (con `tipo_transaccion`, `unidad_medida`, IVA 0/5/10)
- **FacturaLegal**, **FacturaLegalItem** (soporta moneda extranjera con tipo de cambio, totales en guaraníes)
- **DocumentoElectronico**, **LoteDE** (estados: PENDIENTE / EN_PROCESO / APROBADO / RECHAZADO / CANCELADO)
- **EventoCancelacionDE**, **EventoInutilizacionDE**, **EventoNominacionDE**
- **NotaCredito**, **NotaDebito**, **NotaRemision** (+ items)
- **Vehiculo**, **Chofer** (para nota de remisión / transportistas)
- **Geografia**: `Pais`, `Departamento`, `Ciudad`, `Distrito`, `Barrio` (precargados desde [resources/data/](frc-efact-backend/src/main/resources/) y `*.sql`)
- **AuditLog** (esquema `auditoria`, JSONB de valores antes/después)
- **Usuario**, **Rol**, **UsuarioRol** (RBAC)

### Roles
`ADMIN`, `EMPRESA_ADMIN`, `FACTURADOR`, `LECTOR` (más roles internos por empresa via `UsuarioEmpresa.rolEmpresa`).

### Flujo de facturación electrónica
1. Configurar empresa + certificado digital `.pfx`
2. Crear timbrado electrónico (con CSC) y puntos de expedición
3. Cargar productos y clientes
4. Crear `FacturaLegal` (autonumera por timbrado)
5. Generar `DocumentoElectronico` (XML SIFEN, CDC de 44 chars, firma digital, QR)
6. Agrupar en `LoteDE` y enviar a SIFEN
7. Polling de estado (scheduler) → APROBADO/RECHAZADO
8. Eventos: cancelación, inutilización, nominación
9. KuDE PDF descargable, envío por email

---

## Convenciones críticas (no romper)

### Backend
- **NUNCA** prefijar `@RequestMapping` con `/api/` — el `context-path: /api` ya lo agrega. Ver [frc-efact-backend/CONTROLLER_ROUTING_RULE.md](frc-efact-backend/CONTROLLER_ROUTING_RULE.md). Usar `@RequestMapping("/clientes")`, no `"/api/clientes"`.
  - **🐛 Deuda vigente (verificado 2026-08-05):** `GeografiaController`, `AuditLogController` y `ReporteController` **todavía** usan `@RequestMapping("/api/...")` → resuelven a `/api/api/...`. El frontend los consume con ese doble prefijo; corregir requiere cambiar controller **y** el api-service del front en conjunto.
- **Idioma de campos**: español para dominio (`razon_social`, `numero_factura`), inglés para genéricos (`id`, `username`, `is_active`, `password_hash`).
- **Esquemas DB**: nada en `public`. Esquemas realmente creados por migraciones: `persona`, `empresa`, `financiero`, `productos`, `clientes`, `auditoria`, `geografia`, `transporte` (**no** existe `catalogo`). Ver [frc-efact-backend/DATABASE_STANDARDS.md](frc-efact-backend/DATABASE_STANDARDS.md).
- **Auditoría obligatoria** en toda tabla: `id BIGSERIAL PK`, `creado_en`, `creado_por`, `actualizado_en`, `actualizado_por` + trigger `actualizar_timestamp_modificacion()`.
- **Migraciones Flyway** versionadas (`V36__...`); **nunca** modificar una migración ya aplicada — crear una nueva.
- **`ddl-auto: validate`** — Hibernate sólo valida; el esquema lo gestiona Flyway.
- Tests JPA con H2 (dependencia ya incluida).

### Frontend
- **URLs API** desde el frontend incluyen `/api/`: `${environment.apiUrl}/clientes`.
- Archivos kebab-case, clases PascalCase, servicios sufijo `Service`, guards sufijo `Guard`.
- Una rama NgRx por entidad principal (ver [core/state/](frc-efact-frontend/src/app/core/state/)).
- Components: separar `*-list.component.ts` y `*-form.component.ts`.
- JWT en memoria (no `localStorage`).

### Generales
- Idioma de UI y dominio: **español**. Comentarios técnicos pueden ser español o inglés.
- No commitear secretos ni `.pfx` (ver [certificates/](certificates/)).

---

## SIFEN — referencias clave

- Manual implementación NRE/cancelación v150: [docs/sifen/manual-implementacion-nre-y-cancelacion-sifen-v150.md](docs/sifen/manual-implementacion-nre-y-cancelacion-sifen-v150.md)
- Notas crédito/débito/remisión con jsifenlib: [docs/sifen/manual-notas-credito-debito-remision-sifen-jsifenlib.md](docs/sifen/manual-notas-credito-debito-remision-sifen-jsifenlib.md)
- Análisis errores NRE v150: [docs/sifen/analisis-errores-nre-sifen-v150.md](docs/sifen/analisis-errores-nre-sifen-v150.md)
- Implementación monedas SIFEN v150: [docs/sifen/implementacion_monedas_sifen_v150.md](docs/sifen/implementacion_monedas_sifen_v150.md)
- Tipos clientes / productos SIFEN v150: [docs/sifen/tipos_clientes_sifen_v150.md](docs/sifen/tipos_clientes_sifen_v150.md), [docs/sifen/tipos_productos_sifen_v150.md](docs/sifen/tipos_productos_sifen_v150.md)
- XMLs de ejemplo: [docs/sifen/ejemplo_de_aprobado.xml](docs/sifen/ejemplo_de_aprobado.xml), [docs/sifen/ejemplo_de_moneda_extranjera.xml](docs/sifen/ejemplo_de_moneda_extranjera.xml), [docs/sifen/ejemplo_nota_credito.xml](docs/sifen/ejemplo_nota_credito.xml)
- Plantillas KuDE Jasper: [docs/sifen/KuDE_NotaCredito.jrxml](docs/sifen/KuDE_NotaCredito.jrxml) y reportes en [frc-efact-backend/src/main/resources/reports/](frc-efact-backend/src/main/resources/reports/)
- Comparativa con repo de referencia (errores SifenService): [docs/ANALISIS_DIFERENCIAS_SIFEN_SERVICE.md](docs/ANALISIS_DIFERENCIAS_SIFEN_SERVICE.md)
- Repo de referencia (otro proyecto del mismo autor): [docs/franco-system-backend-filial/](docs/franco-system-backend-filial/), [docs/franco-system-backend-servidor/](docs/franco-system-backend-servidor/), [docs/rshk-jsifenlib/](docs/rshk-jsifenlib/)

### Lecciones aprendidas SIFEN (de commits recientes)
- **E644a/1706**: no enviar `dCuotas` cuando `iCondCred=1` (Plazo) — commit `e9db5de`
- **E605b/1552**: no enviar `gPaConEIni` en facturas a crédito sin entrega inicial — commit `de5542c`
- **Notas de crédito**: heredar moneda de la factura referenciada, motivos validados según SIFEN, fecha firma correcta, numeración propia — commits `1680341`, `21f4a89`, `7a8e215`
- **Multi-empresa**: certificados por empresa, ver [.kiro/specs/electronic-invoicing-system/MULTI_EMPRESA_CERTIFICADOS.md](.kiro/specs/electronic-invoicing-system/MULTI_EMPRESA_CERTIFICADOS.md)

### ⚠️ La regla que más caro salió: **los valores fiscales se fijan al emitir, no se derivan al leer**

El proyecto pagó esta misma lección **cuatro veces**, siempre con la misma forma: un dato que se
recalcula en tiempo de lectura a partir de una fuente que puede cambiar, en vez de guardarse en el
documento cuando se emite. Como el DE es **inmutable en SIFEN**, cualquier deriva posterior hace
que lo que muestra el sistema deje de coincidir con lo que la SET aprobó.

| Caso | Consecuencia | Resuelto en |
|---|---|---|
| Actividades económicas separadas por `,` cuando las descripciones de la SET traen comas | La SET rechazaba con `1262` | `V36` |
| `dDVEmi = ""` cuando el RUC no traía guion | DE rechazado sin explicación útil | `RucParaguayo` |
| IVA del ítem derivado de `producto.iva` | Facturas mostradas como EXENTAS; guardar corrompía los totales | `V37` |
| `dCantProSer` truncado con `setScale(0)` | DE emitido por USD 86.250 en vez de 86.279,25; el cliente tuvo que cancelarlo | `CantidadSifen` |

**Antes de tocar cualquier campo del XML, preguntarse: ¿este valor está guardado en el documento,
o se está reconstruyendo?** Si se reconstruye, es un bug esperando el momento.

Corolario aprendido con el `1262` y repetido con el IVA: **un default silencioso en un campo fiscal
es peor que un error**. `producto.getIva()` caía a `10`, `KudePdfService` caía a `0` (exento) y
`dDVEmi` caía a `""`. Los tres producían documentos creíbles y equivocados. Cortar con excepción.

### ⚠️ Unidad de medida: usar siempre `UnidadMedidaSifen`, nunca `balanza`

**El case de los códigos de SIFEN es significativo.** `ML` (88) es *Mililitros* y `ml` (660) es
*Metro lineal*: unidades distintas que solo difieren en mayúsculas. **Nunca aplicar `.toUpperCase()`
ni matcheo case-insensitive** a una unidad de medida — convierte metros lineales en mililitros en
silencio. Además `kg_m2` vale `"kg/m2"` y `racion` vale `"ración"`, o sea que el nombre de la
constante no siempre es el código.

Toda resolución de `cUniMed` pasa por
[`UnidadMedidaSifen`](frc-efact-backend/src/main/java/com/frcefact/sifen/util/UnidadMedidaSifen.java),
y toda normalización de `dCantProSer` por
[`CantidadSifen`](frc-efact-backend/src/main/java/com/frcefact/sifen/util/CantidadSifen.java).
**No usar `Producto.balanza` para decidir la unidad ni la precisión decimal**: ese booleano existe
para integración con balanza física, y que además definiera `cUniMed` y el `setScale` fue el
accidente histórico que causó el DE mal emitido de la tabla de arriba. Sigue disponible solo como
fallback dentro del helper, para no cambiarle la unidad a productos viejos sin `unidadMedida`.

Corregido el 2026-08-07 (V38). Antes: el selector ofrecía 14 opciones inventadas de las cuales solo
4 existían en el catálogo, el formulario forzaba mayúsculas volviendo inalcanzable la mitad de los
códigos, factura y NC ignoraban `unidadMedida`, y NRE la leía con `valueOf(toUpperCase())`.

---

## Documentación adicional

### Funcional / usuario
- [README.md](README.md) — Resumen general
- [START_HERE.md](START_HERE.md) — Quick start
- [MANUAL_DE_USUARIO.md](MANUAL_DE_USUARIO.md) — Manual de usuario completo
- [docs/TAREAS_PENDIENTES.md](docs/TAREAS_PENDIENTES.md) — Funcionalidades incompletas / known issues (⚠️ Inutilización de números funciona parcialmente)
- [docs/MEJORA_ACTIVIDAD_ECONOMICA.md](docs/MEJORA_ACTIVIDAD_ECONOMICA.md)

### Técnica
- [frc-efact-backend/API_DOCUMENTATION.md](frc-efact-backend/API_DOCUMENTATION.md) — Endpoints REST
- [frc-efact-backend/DATABASE_STANDARDS.md](frc-efact-backend/DATABASE_STANDARDS.md) — Estándares DB
- [frc-efact-backend/CONTROLLER_ROUTING_RULE.md](frc-efact-backend/CONTROLLER_ROUTING_RULE.md) — Regla de `@RequestMapping`
- [frc-efact-backend/SECURITY.md](frc-efact-backend/SECURITY.md) — Seguridad backend
- [frc-efact-backend/CONFIGURACION_GMAIL.md](frc-efact-backend/CONFIGURACION_GMAIL.md) — SMTP envío de facturas
- [frc-efact-backend/GITHUB_PACKAGES_SETUP.md](frc-efact-backend/GITHUB_PACKAGES_SETUP.md) — Auth para `jsifenlib`
- [frc-efact-frontend/SECURITY.md](frc-efact-frontend/SECURITY.md)
- [docs/AUTH0_SETUP.md](docs/AUTH0_SETUP.md) — Configuración Auth0

### Specs por feature ([.kiro/specs/](.kiro/specs/))
- `electronic-invoicing-system/` — `requirements.md`, `design.md`, `tasks.md`, `SIFEN_DESIGN_UPDATED.md`, `SIFEN_IMPLEMENTATION_REFERENCE.md`
- `frc-efact-webapp/` — Spec aplicación web
- `user-administration-panel/` — Panel admin de usuarios

### Deployment
- **Producción actual: VM Hetzner** (desde 2026-07-07). Runbook operativo:
  [docs/deployment/hetzner/RUNBOOK_VM.md](docs/deployment/hetzner/RUNBOOK_VM.md) ·
  contexto y riesgos: [docs/deployment/hetzner/PLAN_MIGRACION_HETZNER.md](docs/deployment/hetzner/PLAN_MIGRACION_HETZNER.md)
- URL prod única: `https://efact.frc-ecommerce.com` (`/` → SPA, `/api` → backend)
- Stack: `docker-compose.prod.yml` + `deploy/` (nginx vhost del host, backup por systemd timer)
- **Render: descartado** (2026-08-05). [docs/deployment/render/](docs/deployment/render/) y
  [render.yaml](render.yaml) quedan solo como referencia histórica

---

## Variables de entorno relevantes

### Backend
- `DATABASE_URL` (PostgreSQL)
- `JWT_SECRET` — ⚠️ **el sistema firma con HS256, no HS512.** `JwtTokenProvider` usa
  `Keys.hmacShaKeyFor(secret.getBytes())`, y jjwt elige el algoritmo por el largo de la clave
  (≥64 bytes → HS512, ≥48 → HS384, ≥32 → HS256). La clave de producción tiene 44 bytes → HS256.
  No es una vulnerabilidad (HS256 es adecuado para JWT), pero la doc afirmaba HS512. Para pasar
  a HS512 hace falta una clave de ≥64 caracteres, y rotarla invalida las sesiones activas.
- `MAIL_PASSWORD` (Gmail SMTP `frcsistemasinformaticos@gmail.com`)
- `ENCRYPTION_KEY` (AES-256, 32 chars) — para datos sensibles (CSC, password de certificado)
- `SPRING_PROFILES_ACTIVE` (`dev` | `prod`)
- `PORT` — **obligatoria en la VM** (`PORT=8080`): el `ENTRYPOINT` del Dockerfile es forma
  exec y no expande `${PORT:-8080}`; sin ella Tomcat arranca en puerto `-1`
- `CORS_ALLOWED_ORIGINS` — orígenes extra separados por coma, se suman a los del código
  (`cors.allowed-origins` en `SecurityConfig`)
- Auth0: configurado en `application.yml` (`issuer-uri: dev-gp1w0u2bgw35q6v5.us.auth0.com`, `audiences: https://api.frcefact.com`)
- Valores reales de prod: `deploy/.env` en la VM (plantilla: `deploy/.env.example`, no commitear)

### Frontend
- `environment.ts` (`apiUrl: http://localhost:8080/api`)
- `environment.prod.ts` (`apiUrl: https://efact.frc-ecommerce.com/api`)

---

## Issues conocidos pendientes de resolver

### Sistema de roles / permisos (investigado 2026-04-08)

El sistema tiene **dos capas de roles** que no están bien integradas:
- **Roles globales**: tabla `persona.usuario_rol` → `Usuario.usuarioRoles` → mapea a `Rol` (`ADMIN`, `EMPRESA_ADMIN`, `FACTURADOR`, `LECTOR`)
- **Roles por empresa**: tabla `persona.usuario_empresa.rol_empresa` → `UsuarioEmpresa.rolEmpresa` (string: `ADMINISTRADOR` / `FACTURADOR` / `LECTOR`)

El backend en [CustomUserDetailsService.java:115-139](frc-efact-backend/src/main/java/com/frcefact/security/CustomUserDetailsService.java#L115-L139) mapea **dinámicamente en cada request** los `rolEmpresa` activos a authorities Spring:
- `ADMINISTRADOR` → `ROLE_EMPRESA_ADMIN`
- `FACTURADOR` → `ROLE_FACTURADOR`
- `LECTOR` → `ROLE_LECTOR`

Esto funciona tanto para JWT local ([JwtAuthenticationFilter.java:50](frc-efact-backend/src/main/java/com/frcefact/security/JwtAuthenticationFilter.java#L50) llama a `loadUserByUsername` en cada request) como para Auth0 ([CustomJwtAuthenticationConverter.java:72](frc-efact-backend/src/main/java/com/frcefact/security/CustomJwtAuthenticationConverter.java#L72)). Por lo tanto, **los `@PreAuthorize` del backend pasan** correctamente solo con tener `rolEmpresa`.

#### Bugs / gaps a resolver

1. **🐛 [PermissionsService](frc-efact-frontend/src/app/core/services/permissions.service.ts) del frontend NO mira los `rolEmpresa`.** Solo lee `user.roles` (global). Resultado: aunque el backend autoriza, el frontend oculta menús/botones y muestra "sin permisos". **Síntoma reportado:** asignás `rolEmpresa = ADMINISTRADOR` y el usuario sigue sin ver lista de facturas ni botón crear.
   - **Fix recomendado (backend):** que [UsuarioMapper.toDto()](frc-efact-backend/src/main/java/com/frcefact/dto/mapper/UsuarioMapper.java#L46-L58) agregue al array `roles` los roles de empresa mapeados (misma lógica que `CustomUserDetailsService`). Una sola fuente de verdad.
   - **Workaround temporal:** asignar también el rol global `EMPRESA_ADMIN` vía endpoint admin de usuarios.

2. **🐛 Usuario nuevo no recibe ningún rol al registrarse.** Ni `UsuarioService.crearUsuario()` ni el auto-registro de Auth0 en [CustomJwtAuthenticationConverter.java:91-110](frc-efact-backend/src/main/java/com/frcefact/security/CustomJwtAuthenticationConverter.java#L91-L110) asignan rol por defecto. Quedan con `roles=[]` y sin acceso a nada hasta que un admin los vincule.

3. **⚠️ Vincular usuario a empresa no refresca la sesión activa del usuario receptor.** El backend en cada request remapea correctamente, pero el frontend cachea el `currentUser` en NgRx (con su array `roles` viejo). El usuario debe **cerrar sesión y volver a entrar** para que el frontend reciba el `UsuarioDto` actualizado. Considerar emitir un evento o forzar refresh tras vinculación.

4. **Falta consistencia de nombres**: a nivel rol_empresa el valor es `ADMINISTRADOR`, pero a nivel rol global es `EMPRESA_ADMIN`. El mapping vive solo en `CustomUserDetailsService`. Si se duplica en otro lado (frontend), riesgo de divergencia.

#### Roles requeridos por endpoint operativo (referencia)
| Operación | Roles backend (`@PreAuthorize`) |
|---|---|
| Listar facturas (`GET /facturas`) | ADMIN, EMPRESA_ADMIN, FACTURADOR, LECTOR |
| Crear factura (`POST /facturas`) | ADMIN, EMPRESA_ADMIN, FACTURADOR |
| Crear nota crédito / débito / remisión | ADMIN, EMPRESA_ADMIN, FACTURADOR |
| Generar DE (`POST /facturas/{id}/generar-de`) | ADMIN, EMPRESA_ADMIN, FACTURADOR |
| Anular factura | ADMIN, EMPRESA_ADMIN |

---

## Workflow / reglas de colaboración

**Flujo de ramas, commits, CI y deploy: [CONTRIBUTING.md](CONTRIBUTING.md)** — leerlo antes
de abrir un PR. Resumen:

```
feature/* --PR--> develop --PR--> main --(semantic-release)--> tag + CHANGELOG
hotfix/*  --PR--> main    --PR--> develop  (obligatorio post-hotfix)
```

- `main` = rama de release · `develop` = integración · **sin canal de prerelease** (no hay
  entorno alpha/beta donde desplegarlo).
- **PR de `develop` a `main`: merge commit, NO squash** — el squash colapsa los
  `feat:`/`fix:` y `semantic-release` calcula mal el bump.
- CI (`.github/workflows/ci.yml`) corre en PRs a `main`/`develop`: build de backend y
  frontend bloqueantes; los tests del backend **no** bloquean todavía (6 tests de RUC
  fallan por un bug conocido — ver [docs/TAREAS_PENDIENTES.md](docs/TAREAS_PENDIENTES.md) §4).

- **⚠️ El deploy a producción NO es automático.** `git push` no despliega nada. Hay dos vías:
  el workflow **`deploy.yml`** (`workflow_dispatch`, pide servicio + escribir `DEPLOY`), o SSH
  manual a la VM (`deploy@178.105.107.171`) con `git pull` + `docker compose -f
  docker-compose.prod.yml --env-file deploy/.env up -d --build`. Procedimiento y gotchas:
  [docs/deployment/hetzner/RUNBOOK_VM.md](docs/deployment/hetzner/RUNBOOK_VM.md).
  **Nunca desplegar sin confirmación explícita del usuario** — la VM es compartida con otros
  servicios productivos.
- **Qué sí dispara un `git push` a `main`:** `.github/workflows/release.yml` corre
  `semantic-release`. Con commits `feat`/`fix`/`perf` genera tag, `CHANGELOG.md` y bump de
  `pom.xml` + `package.json`; con `docs`/`chore`/`refactor` no libera nada. **Igual,
  preguntar siempre antes de `commit` + `push`.**
- **Render fue descartado** (2026-08-05). El rollback hoy es el repo privado
  `frc-efact-legacy`, que la VM tiene configurado como remoto `legacy`.
- **Siempre compilar antes de commit/push.** Si tocaste backend Java: `cd frc-efact-backend && ./mvnw compile`. Si tocaste frontend: `cd frc-efact-frontend && npm run build:dev`. Si la compilación falla, **no commitear** — arreglar primero.
  - ⚠️ **`npm run lint` no funciona** (verificado 2026-08-05): `angular.json` solo declara los targets `build`, `serve`, `extract-i18n` y `test` — falta `@angular-eslint/schematics`. El gate real del frontend es el build AOT.
- **El repo es público desde el 2026-08-06** (`GabFrank/frc-efact`), migrado a uno nuevo con
  historial filtrado. Hay un ruleset activo que bloquea force-push y borrado de `main`/`develop`;
  `require pull request` **no** está activo porque rompería `semantic-release` — detalle en
  [CONTRIBUTING.md](CONTRIBUTING.md). **Nunca commitear secretos:** ahora cualquier cosa que entre
  es pública de inmediato.
- Antes de marcar un fix como "resuelto" en este documento, **esperar validación del usuario** ejecutando/probando el cambio.

---

## Notas para trabajar en este proyecto

- **Antes de tocar SifenService**: leer [docs/ANALISIS_DIFERENCIAS_SIFEN_SERVICE.md](docs/ANALISIS_DIFERENCIAS_SIFEN_SERVICE.md) y los manuales de [docs/sifen/](docs/sifen/). El XML SIFEN es muy estricto — un campo de más o menos rompe la validación con códigos como `E605b`, `E644a`, etc.
- **Antes de crear migración**: revisar el último `V<N>__*.sql` en [db/migration/](frc-efact-backend/src/main/resources/db/migration/) y respetar los estándares de [DATABASE_STANDARDS.md](frc-efact-backend/DATABASE_STANDARDS.md).
- **Antes de crear un controller**: leer [CONTROLLER_ROUTING_RULE.md](frc-efact-backend/CONTROLLER_ROUTING_RULE.md) — sin `/api/` en `@RequestMapping`.
- **Antes de añadir entidad**: seguir el flujo completo descripto en [docs/FLUJO_SISTEMA_ENTIDADES.md](docs/FLUJO_SISTEMA_ENTIDADES.md) (model → repo → DTO → service → controller en backend; model → api → state → component en frontend).
- **Notas de crédito** son la feature actualmente más activa (ver últimos commits) — heredan moneda/items de la factura referenciada, KuDE PDF disponible vía endpoint `kude-pdf`.
