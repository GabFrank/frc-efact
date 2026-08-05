# Plan de Migración: Render → VM Hetzner

**Fecha de investigación:** 2026-07-06
**Objetivo:** migrar todo el sistema FRC eFact (backend + frontend + PostgreSQL) desde Render
a una VM de Hetzner Cloud **sin pérdida de datos**.

> **⚠️ Addendum 2026-07-06 (verificación real — prevalece sobre lo de abajo):**
> - La VM destino **ya existe y es compartida** (`deploy@178.105.107.171`, Fedora 42) con
>   servicios productivos: nginx (80/443), farmacia Next.js, headscale (127.0.0.1:8080),
>   mediamtx, PostgreSQL nativo. **Se descarta Caddy**: TLS/routing con el nginx del host +
>   certbot; el stack Docker expone solo loopback (backend 8081, frontend 8082).
> - Render: el backend **sí tiene disco persistente** (1 GB en `/app/certificates`) — el
>   riesgo "filesystem efímero" de §1 no aplica. La DB es **PostgreSQL 16.13** (no 15),
>   base `frc_efact_db_7koy`, **13 MB**. 0 lotes EN_PROCESO al 2026-07-06.
> - Dominio elegido: **`efact.frc-ecommerce.com`** (único, `/api` → backend, `/` → SPA).
> - Pasos ejecutables actualizados: [RUNBOOK_VM.md](RUNBOOK_VM.md) (versión 2026-07-06).

---

## 1. Arquitectura actual (Render) — inventario verificado

| Componente | Hoy en Render | Evidencia |
|---|---|---|
| Backend | Web service Docker (`frc-efact-backend/Dockerfile`, multi-stage Maven→JRE 17 Alpine), plan free, región Oregon | `render.yaml` |
| Frontend | Static site (`npm run build:prod`, publica `dist/frc-efact-frontend/browser`, rewrite SPA a `index.html`) | `render.yaml` |
| Base de datos | PostgreSQL managed `frc-efact-db` (db: `frc_efact_db`, user: `frc_efact_user`), plan free | `render.yaml` |
| Deploy | Auto-deploy en push a `main` + semantic-release en GitHub Actions | CLAUDE.md, workflows |
| URLs prod | `https://frc-efact-backend.onrender.com/api` · `https://frc-efact-frontend.onrender.com` | `environment.prod.ts` |

### Dónde vive cada dato (crítico para no perder nada)

| Dato | Ubicación | Riesgo en migración |
|---|---|---|
| Todo el dominio: empresas, timbrados, facturas, DE (XML firmado incluido), notas, clientes, productos, eventos, geografía, usuarios, audit log | **PostgreSQL** (esquemas `persona`, `empresa`, `financiero`, `productos`, `clientes`, `auditoria`, `catalogo` + `public.flyway_schema_history`) | Bajo con dump/restore correcto |
| CSC de timbrados y passwords de certificados | PostgreSQL, **cifrados AES-256-GCM** con `ENCRYPTION_KEY` (`EncryptionService`, `encryption.secret-key`) | **ALTO**: si la key de Hetzner ≠ key de Render, quedan indescifrables |
| Certificados `.pfx` | **Filesystem** `/app/certificates/` del contenedor (`certificates.upload-dir`); la DB solo guarda `empresa.certificado_path` (ej: `/certificates/empresa_123_xxx.pfx`) | **ALTO**: `render.yaml` NO define disco persistente → el filesystem de Render es efímero. Los `.pfx` deben re-subirse o copiarse manualmente |
| Secretos runtime | Env vars de Render: `JWT_SECRET` (generado por Render), `ENCRYPTION_KEY`, `MAIL_PASSWORD`, `GITHUB_USERNAME/TOKEN`, posibles `SIFEN_*` | **ALTO**: hay que exportarlos del dashboard antes de apagar nada |
| KuDE PDF / QR | Generados on-the-fly (JasperReports + ZXing, solo `createTempFile`) | Ninguno |
| Logs | `logs/` del contenedor | Aceptable perderlos |

### Detalles técnicos relevantes ya verificados en el código

- `DatabaseConfig.java` (perfil `prod`) parsea `DATABASE_URL` en formato
  `postgresql://user:pass@host:port/db` → **compatible sin cambios** con Hetzner
  (solo cuidar caracteres especiales en el password: se parsea con `URI`, conviene password alfanumérico).
- `application-prod.yml`: `flyway.baseline-on-migrate: true`, `clean-disabled: true`,
  `ddl-auto: validate`, `forward-headers-strategy: framework` (**ya preparado para reverse proxy**),
  actuator `health/info/metrics` expuestos en `/api/actuator/*`.
- `application-prod.yml` **no tiene sección `sifen:`** → los defaults de `SifenProperties` son
  `enabled=false`, `ambiente=TEST`, `scheduler.enabled=false`. Si prod emite DE hoy, es porque hay
  env vars (`SIFEN_ENABLED`, `SIFEN_AMBIENTE`, `SIFEN_SCHEDULER_ENABLED` via relaxed binding) o
  configuración por empresa en DB. **Exportar TODAS las env vars del dashboard de Render**, no solo
  las de `render.yaml`.
- CORS está **hardcodeado** en `SecurityConfig.corsConfigurationSource()` con `https://*.onrender.com`
  (la propiedad `cors.allowed-origins` del yml es config muerta) → **requiere cambio de código** para el dominio nuevo.
- Frontend: `environment.prod.ts` hardcodea `apiUrl` y el `redirect_uri` de Auth0 → **requiere cambio + rebuild**.
- Auth0: tenant `dev-gp1w0u2bgw35q6v5.us.auth0.com`, audience `https://api.frcefact.com`,
  clientId SPA `ozA1x7MTVu8yVuOhYc3nUHWdLCPr6L6s`. El backend valida por `issuer-uri` (no cambia),
  pero en el **dashboard de Auth0** hay que agregar el dominio nuevo a Allowed Callback URLs,
  Allowed Logout URLs, Allowed Web Origins y CORS.
- Build del backend necesita `GITHUB_USERNAME`/`GITHUB_TOKEN` (GitHub Packages, fork `jsifenlib`).
- SIFEN se consume saliente (SOAP + mTLS con el `.pfx` de la empresa); no hay allowlist de IP
  conocida → emitir desde Hetzner (Alemania/Finlandia) funciona, solo cambia latencia (~250ms).
- JVM/DB en UTC (`hibernate.jdbc.time_zone: UTC`, Render corre UTC) → **mantener UTC en la VM**
  para no alterar el comportamiento de fechas de firma ya validado contra SIFEN.

### 🔴 Hallazgo de seguridad (acción inmediata, independiente de la migración)

`docs/deployment/AGREGAR_VARIABLES_RENDER.md` contiene un **GitHub PAT en texto plano**
(`ghp_SUuA...`) commiteado al repo. Está en el historial de git, así que borrarlo del archivo no
alcanza: **revocar ese token en GitHub y generar uno nuevo** (scope mínimo `read:packages`).
El token nuevo nunca se commitea.

---

## 2. Arquitectura destino propuesta (Hetzner)

Una sola VM con Docker Compose y reverse proxy con TLS automático:

```
Internet ──► Caddy (80/443, Let's Encrypt automático)
              ├── app.<dominio>      → nginx (Angular estático)
              └── api.<dominio>/api  → backend:8080 (Spring Boot)
                                        └── postgres:15 (red interna, sin puerto público)
Volúmenes persistentes: pgdata, certificates, caddy_data
```

- **VM recomendada:** Hetzner CX22 o CPX21 (2 vCPU / 4 GB / 40-80 GB) — sobra para esta carga;
  Ubuntu 24.04 LTS. Elegir región y activar **Backups** de Hetzner (7 snapshots rotativos, +20% del precio).
- **Firewall Hetzner Cloud:** entrante solo 22 (idealmente restringido por IP), 80, 443.
  PostgreSQL **nunca** expuesto públicamente.
- **Hardening básico:** SSH solo con clave, `fail2ban`, `unattended-upgrades`.
- **Dominio:** se necesita un dominio propio (ej. `frcefact.com`) con registros A
  `app.` y `api.` apuntando a la IP de la VM. Sin dominio no hay TLS válido ni Auth0 funcional.

### docker-compose.prod.yml propuesto (esqueleto)

```yaml
services:
  postgres:
    image: postgres:15-alpine
    restart: unless-stopped
    environment:
      POSTGRES_DB: frc_efact_db
      POSTGRES_USER: frc_efact_user
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
    volumes:
      - pgdata:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U frc_efact_user -d frc_efact_db"]
      interval: 10s
      retries: 5

  backend:
    build:
      context: ./frc-efact-backend
      args:
        GITHUB_USERNAME: ${GITHUB_USERNAME}
        GITHUB_TOKEN: ${GITHUB_TOKEN}
    restart: unless-stopped
    depends_on:
      postgres:
        condition: service_healthy
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DATABASE_URL: postgresql://frc_efact_user:${POSTGRES_PASSWORD}@postgres:5432/frc_efact_db
      JWT_SECRET: ${JWT_SECRET}
      ENCRYPTION_KEY: ${ENCRYPTION_KEY}      # ⚠️ EXACTAMENTE la misma que en Render
      MAIL_PASSWORD: ${MAIL_PASSWORD}
      # SIFEN_* según lo exportado de Render
    volumes:
      - certificates:/app/certificates        # ⚠️ por fin persistente

  frontend:
    image: nginx:alpine
    restart: unless-stopped
    volumes:
      - ./frc-efact-frontend/dist/frc-efact-frontend/browser:/usr/share/nginx/html:ro
      - ./deploy/nginx-spa.conf:/etc/nginx/conf.d/default.conf:ro

  caddy:
    image: caddy:2-alpine
    restart: unless-stopped
    ports: ["80:80", "443:443"]
    volumes:
      - ./deploy/Caddyfile:/etc/caddy/Caddyfile:ro
      - caddy_data:/data

volumes:
  pgdata:
  certificates:
  caddy_data:
```

Caddyfile mínimo:

```
api.<dominio> {
    reverse_proxy backend:8080
}
app.<dominio> {
    reverse_proxy frontend:80
}
```

(nginx del frontend con `try_files $uri $uri/ /index.html;` para el rewrite SPA.)

---

## 3. Cambios de código necesarios ANTES de migrar

1. **`SecurityConfig.java`**: agregar `https://app.<dominio>` (y opcionalmente `https://*.<dominio>`)
   a `setAllowedOriginPatterns`. Mantener `*.onrender.com` durante la transición; quitarlo después.
2. **`environment.prod.ts`**: `apiUrl: 'https://api.<dominio>/api'` y
   `auth0.authorizationParams.redirect_uri: 'https://app.<dominio>'`.
3. **Auth0 dashboard** (no es código): agregar `https://app.<dominio>` a Allowed Callback URLs,
   Logout URLs, Web Origins, CORS. No quitar las URLs de onrender hasta terminar el cutover.
4. **Nuevos archivos**: `docker-compose.prod.yml`, `deploy/Caddyfile`, `deploy/nginx-spa.conf`,
   script de backup (§6).
5. Opcional (post-migración): workflow de GitHub Actions para deploy por SSH
   (`docker compose build && up -d`) y así conservar la convención "push a main = deploy".

---

## 4. Runbook de migración (ventana de mantenimiento)

> Regla: **Render no se toca/destruye hasta validar Hetzner.** Es el rollback.

### Fase 0 — Preparación (días antes, sin downtime)

1. **Rotar el PAT filtrado** (§1) y crear PAT nuevo `read:packages`.
2. **Exportar del dashboard de Render** todas las env vars del backend a un lugar seguro
   (password manager). Crítico: `ENCRYPTION_KEY` (si nunca se seteó, el sistema usó el default
   `<REDACTADO-ENCRYPTION-KEY-ROTADA>` de `application.yml` — verificar), `MAIL_PASSWORD`, `SIFEN_*`.
   `JWT_SECRET` puede regenerarse (solo invalida sesiones activas).
3. **Verificar acceso externo a la DB de Render**: dashboard → `frc-efact-db` → External Database URL.
   Probar: `psql "<EXTERNAL_URL>" -c "select version();"` y anotar la versión de PostgreSQL
   (elegir en Hetzner la misma major o superior).
4. **Ensayo de dump**: `pg_dump "<EXTERNAL_URL>" -Fc --no-owner --no-privileges -f ensayo.dump`
   y `pg_restore --list ensayo.dump` para validar contenido (deben aparecer los 7 esquemas +
   `flyway_schema_history`). Anotar tamaño y duración.
5. **Reunir los `.pfx`**: confirmar que existen copias locales de TODOS los certificados de todas
   las empresas activas (carpeta local `certificates/`, no está en git). Query de control:
   `SELECT id, razon_social, certificado_path, certificado_fecha_expiracion FROM empresa.empresas;`
6. Crear VM Hetzner, firewall, DNS (`app.`/`api.` → IP), instalar Docker; subir compose/Caddyfile;
   levantar **postgres vacío** y probar que Caddy emite certificados TLS.
7. Mergear los cambios de código de §3 (CORS + environment.prod.ts) — son aditivos, no rompen Render.

### Fase 1 — Congelamiento (inicio de ventana; elegir horario sin facturación)

1. Avisar a los usuarios; dejar de emitir documentos.
2. Verificar que no queden lotes `EN_PROCESO` pendientes de respuesta SIFEN:
   `SELECT estado, count(*) FROM financiero.lotes_de GROUP BY estado;` — si hay pendientes,
   esperar al scheduler o consultarlos manualmente antes de congelar.
3. (Opcional, congelamiento duro) suspender el servicio backend en Render para garantizar
   cero escrituras durante el dump.

### Fase 2 — Migración de datos

```bash
# 1. Dump final desde Render
pg_dump "<EXTERNAL_URL_RENDER>" -Fc --no-owner --no-privileges -f frc_efact_final.dump

# 2. Copiar a la VM
scp frc_efact_final.dump root@<ip-vm>:/root/

# 3. Restaurar en el postgres del compose
docker compose exec -T postgres pg_restore -U frc_efact_user -d frc_efact_db \
  --no-owner --no-privileges --clean --if-exists /dev/stdin < /root/frc_efact_final.dump

# 4. Verificación de integridad (comparar contra los mismos counts en Render)
docker compose exec postgres psql -U frc_efact_user -d frc_efact_db -c "
  SELECT 'facturas', count(*) FROM financiero.facturas_legales
  UNION ALL SELECT 'documentos', count(*) FROM financiero.documentos_electronicos
  UNION ALL SELECT 'empresas', count(*) FROM empresa.empresas
  UNION ALL SELECT 'usuarios', count(*) FROM persona.usuarios
  UNION ALL SELECT 'clientes', count(*) FROM clientes.clientes
  UNION ALL SELECT 'flyway', count(*) FROM flyway_schema_history;"
```

(ajustar nombres de tabla si difieren; sacarlos de las migraciones Flyway)

### Fase 3 — Levantar backend + certificados

1. Completar `.env` de la VM con los secretos exportados (**`ENCRYPTION_KEY` idéntica**).
2. `docker compose up -d backend` → en logs: Flyway debe **validar** (no migrar nada) y
   `Started FrcEfactBackendApplication`.
3. **Certificados**: copiar cada `.pfx` al volumen con el **nombre exacto** que figura en
   `empresa.certificado_path`:
   `docker compose cp ./certificados-locales/<archivo>.pfx backend:/app/certificates/`
   Alternativa más robusta: re-subir cada certificado desde la UI de administración de empresas
   (regenera path y valida password contra la `ENCRYPTION_KEY`).
4. Smoke test de cifrado: abrir en la UI la config de un timbrado con CSC — si el CSC se muestra/
   usa sin error, la `ENCRYPTION_KEY` es correcta. Si truena el descifrado GCM, la key está mal:
   **detener y corregir antes de seguir**.

### Fase 4 — Frontend + cutover

1. Build con los nuevos endpoints: `npm ci && npm run build:prod`; subir `dist/` a la VM
   (o buildear en la VM/CI).
2. `docker compose up -d` completo. Probar por dominio nuevo:
   - Login local (`admin`) y login Auth0.
   - Listado de facturas/documentos históricos (verifica DB).
   - Descarga de un KuDE PDF histórico (verifica Jasper + XML en DB).
   - **Consulta de estado de un DE ya aprobado contra SIFEN** (verifica mTLS con `.pfx` sin
     emitir nada nuevo).
   - Envío de email de prueba (SMTP Gmail).
3. Emitir **un** documento real de bajo valor y confirmar APROBADO (verifica firma, CDC, QR,
   scheduler y lote end-to-end).
4. Anunciar la URL nueva / actualizar bookmarks. Si se conserva el mismo dominio con redirect,
   configurar redirect en Render frontend.

### Fase 5 — Post-cutover

1. Mantener Render **suspendido pero intacto** 1-2 semanas como rollback.
2. Activar backups (§6) y verificar la primera restauración de prueba.
3. Recién después: bajar servicios de Render, quitar `*.onrender.com` del CORS, retirar
   `render.yaml` o marcarlo como legacy, actualizar CLAUDE.md/skill (la regla
   "push = deploy a Render" cambia).

### Rollback (si algo falla en Fase 3/4)

Reactivar el servicio de Render y volver el DNS/URLs a onrender. Como Render nunca se tocó y el
congelamiento evitó escrituras nuevas en Hetzner, no hay divergencia de datos. **Nunca operar los
dos backends con scheduler activo a la vez** (doble envío de lotes a SIFEN).

---

## 5. Checklist de datos — "cero pérdida"

- [ ] Dump `-Fc` verificado con counts idénticos a Render (facturas, DE, empresas, usuarios, notas, eventos, audit log)
- [ ] `flyway_schema_history` migrado (Flyway valida, no re-migra)
- [ ] `ENCRYPTION_KEY` idéntica → CSC y passwords de certificados descifrables
- [ ] Todos los `.pfx` presentes en el volumen y `certificado_path` consistente
- [ ] Env vars completas (incluidas las no documentadas en `render.yaml`)
- [ ] Ningún lote `EN_PROCESO` perdido durante la ventana
- [ ] Secuencias/autonumeración de facturas continúan correctamente (verificar `last_value` de las secuencias tras el restore)

## 6. Operación continua en Hetzner (lo que Render hacía solo)

- **Backups DB**: cron diario `pg_dump -Fc` a `/var/backups` + copia off-site
  (Hetzner Storage Box via rsync/borg, o restic a S3/Backblaze). Retención ≥ 30 días.
  Los DE son documentos fiscales: SET exige conservación por años → backup también de
  `certificates/` y del `.env` (cifrados).
- **Snapshots**: activar Hetzner Backups de la VM.
- **Monitoreo**: uptime check externo a `https://api.<dominio>/api/actuator/health`
  (UptimeRobot/Better Stack free tier) + alertas por email.
- **Actualizaciones**: `unattended-upgrades` + actualizar imágenes Docker mensualmente.
- **Renovación TLS**: automática con Caddy (verificar los primeros días).
- **Deploy**: GitHub Actions por SSH para conservar "merge a main = deploy".

## 7. Preguntas abiertas (bloquean fases concretas)

1. **¿Dominio?** ¿Ya existe un dominio propio (ej. `frcefact.com` — el audience de Auth0 sugiere
   que sí) o hay que registrar uno? Bloquea DNS/TLS/Auth0 (Fase 0.6).
2. **¿`ENCRYPTION_KEY` en Render?** ¿Fue seteada explícitamente o corre con el default?
   Bloquea Fase 3.
3. **¿Copias locales de todos los `.pfx` con sus passwords?** Bloquea Fase 3.3.
4. **¿Cómo sobreviven hoy los certificados a los redeploys de Render** si no hay disco
   persistente? (¿se re-suben a mano tras cada deploy?) — confirma el modelo de riesgo actual.
5. **¿Qué env vars `SIFEN_*` existen en el dashboard?** Bloquea Fase 0.2.
6. **¿Tamaño actual de la DB?** (`SELECT pg_size_pretty(pg_database_size(current_database()));`)
   — define duración de la ventana y tamaño de VM/volumen.
