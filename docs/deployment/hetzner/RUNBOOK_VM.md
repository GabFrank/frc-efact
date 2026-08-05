# Runbook: migración FRC eFact a la VM Hetzner (compartida)

> **✅ MIGRACIÓN COMPLETADA 2026-07-07.** Pasos 1-7 ejecutados. Producción:
> `https://efact.frc-ecommerce.com`. Validado end-to-end: login local y Auth0,
> históricos, KuDE, CSC, consulta SIFEN mTLS, email y **una factura real APROBADA
> por SIFEN**. Render: backend+frontend **suspendidos** (rollback 1-2 semanas),
> DB de Render sigue corriendo. Backup diario: systemd timer
> `frc-efact-backup.timer` 03:30 UTC (no cron: Fedora sin cronie).
> Pendientes post-cutover: revocar el PAT filtrado, copia off-site de backups,
> monitoreo externo, y tras el período de rollback dar de baja Render +
> quitar `*.onrender.com` del CORS + actualizar CLAUDE.md/skill.

Guía ejecutable paso a paso, **actualizada 2026-07-06** tras inspeccionar la VM real y
Render. El contexto y los riesgos están en [PLAN_MIGRACION_HETZNER.md](PLAN_MIGRACION_HETZNER.md).

## La VM real (verificado 2026-07-06)

- `deploy@178.105.107.171` — **Fedora 42**, 4 vCPU, 7.6 GB RAM, 131 GB libres.
  `deploy` tiene sudo (wheel). Acceso solo por clave SSH.
- ⚠️ **VM COMPARTIDA con servicios productivos que NO se tocan:**
  - nginx del host (dueño de **80/443**, vhosts `farmacia-franco.conf`, `headscale.conf`)
  - PostgreSQL **nativo** en 5432 (localhost) — no es el nuestro
  - farmacia Next.js (127.0.0.1:3000), headscale (**127.0.0.1:8080**), mediamtx (8554, 8889…)
  - firewalld + fail2ban ya activos; certbot ya instalado (certs `*.farmaciafrancopy.com`)
- Consecuencias de diseño (vs. el plan original):
  - **Sin Caddy** — el TLS/routing lo hace el nginx del host + certbot (patrón ya usado en la VM).
  - Stack Docker expone **solo loopback**: backend `127.0.0.1:8081`, frontend `127.0.0.1:8082`
    (8080 del host está ocupado por headscale). Postgres del stack sin puerto al host.
  - `setup-vm.sh` NO instala ufw/fail2ban ni toca SSH — solo Docker CE (Fedora), grupo docker
    y directorio de backups.

## Render (verificado 2026-07-06, solo lectura)

- Backend `frc-efact-backend` (`srv-d61m4p4hg0os73fpbjm0`), plan starter, Oregon.
  **Tiene disco persistente de 1 GB en `/app/certificates`** — los `.pfx` sí sobreviven
  redeploys de Render (el plan original asumía filesystem efímero).
- DB `frc-efact-db` (`dpg-d75teee3jp1c73djsip0-a`): **PostgreSQL 16.13**, base
  `frc_efact_db_7koy`, user `frc_efact_db_7koy_user`, **13 MB** → dump/restore en segundos.
- Counts de referencia (2026-07-06): 10 `factura_legal`, 12 `documento_electronico`,
  12 `lote_de` (8 PROCESADO, 4 ERROR_PERMANENTE, 0 EN_PROCESO), 3 `nota_credito`,
  2 `empresa`, 6 `usuario`, 5 `cliente`, 4 `producto`, 35 filas `flyway_schema_history`.
- Certificados en `empresa.empresa`:
  - `empresa_1_1774969696647.pfx` — ANATOLE DEINZER DUARTE, vence 2026-11-18
  - `empresa_2_1777486604669.pfx` — FRANCO AREVALOS S.A., **vence 2026-08-20**
- Env vars reales del backend en Render (verificado 2026-07-06):
  `MAIL_PASSWORD`, `JWT_SECRET`, `JWT_EXPIRATION`, `LOG_LEVEL`, `GITHUB_TOKEN`,
  `GITHUB_USERNAME`, `SPRING_PROFILES_ACTIVE`, `DATABASE_URL`. **No hay
  `ENCRYPTION_KEY`** → prod usa el default de `application.yml` (poner ese mismo
  valor en `deploy/.env`; rotar recién post-migración). **No hay `SIFEN_*`** →
  defaults de `SifenProperties` (enabled=false, TEST, scheduler off); no
  activarlos en la VM salvo decisión explícita.
- Ensayo de dump OK (2026-07-06, desde máquina local): 316 KB, ~39 s,
  31 tablas con datos, 9 esquemas (`persona`, `empresa`, `financiero`,
  `productos`, `clientes`, `auditoria`, `geografia`, `transporte`, `public`).
- Esquemas reales: `persona`, `empresa`, `financiero`, `productos`, `clientes`,
  `auditoria`, `geografia`, `transporte`, `catalogo`(si existe) + `public.flyway_schema_history`.

## Dominio

**`efact.frc-ecommerce.com`** (dominio único: `/` → SPA, `/api` → backend).
`app.frc-ecommerce.com` apunta a **otra máquina** (159.203.86.103) — no confundir.

**Artefactos en el repo** (rama `docs/integracion-hetzner`; originalmente `claude/frc-efact-expert-skill-cbsx4z`):

| Archivo | Qué es |
|---|---|
| `docker-compose.prod.yml` | postgres:16 + backend (127.0.0.1:8081) + frontend (127.0.0.1:8082) |
| `deploy/nginx-vhost-efact.conf` | vhost para el nginx del HOST (reemplaza al Caddyfile) |
| `deploy/.env.example` | Plantilla de secretos → copiar a `deploy/.env` (gitignored) |
| `deploy/setup-vm.sh` | Instala Docker CE en Fedora; NO toca firewall/SSH/nginx |
| `deploy/migrate-db-from-render.sh` | Dump desde Render + restore local + verificación |
| `deploy/backup-db.sh` | Backup diario DB + certificados (cron de deploy) |
| `frc-efact-frontend/Dockerfile` | Build multi-stage Angular → nginx |

## Prerrequisitos (fuera de la VM)

- [ ] **Revocar el PAT filtrado** en `docs/deployment/AGREGAR_VARIABLES_RENDER.md` y crear uno
      nuevo con scope `read:packages`.
- [ ] Exportar del dashboard de Render **todas** las env vars del backend
      (`ENCRYPTION_KEY`, `MAIL_PASSWORD`, `SIFEN_*`, …) y la **External Database URL** de `frc-efact-db`.
- [ ] Reunir los `.pfx` de las 2 empresas activas + passwords (nombres exactos arriba).
- [ ] DNS: **registro A `efact.frc-ecommerce.com` → `178.105.107.171`** (TTL bajo, ej. 300).
- [ ] Auth0 dashboard → aplicación SPA (`ozA1x7MTVu8yVuOhYc3nUHWdLCPr6L6s`): agregar
      `https://efact.frc-ecommerce.com` a Allowed Callback URLs, Allowed Logout URLs,
      Allowed Web Origins y Allowed Origins (CORS). **No quitar** las de onrender todavía.

## Cambios de código (ya hechos en la rama)

- `environment.prod.ts`: `apiUrl: 'https://efact.frc-ecommerce.com/api'`,
  `redirect_uri: 'https://efact.frc-ecommerce.com'`.
  ⚠️ Al mergear a `main`, el build de Render frontend queda apuntando al dominio nuevo —
  coordinar el merge con el cutover.
- `SecurityConfig.java`: soporta `CORS_ALLOWED_ORIGINS` (commit `f58aac4`), el compose la setea.

## Paso 1 — Bootstrap de la VM

```bash
ssh deploy@178.105.107.171
git clone https://github.com/GabFrank/frc-efact.git ~/frc-efact
cd ~/frc-efact
git checkout docs/integracion-hetzner   # hasta que se mergee a main
chmod +x deploy/*.sh
./deploy/setup-vm.sh
# si el script agregó al grupo docker: salir y volver a entrar por SSH
```

Verificar: `docker --version`, `docker compose version`, y que los servicios existentes
siguen sanos: `systemctl is-active nginx farmacia headscale mediamtx postgresql`.

## Paso 2 — Secretos

```bash
cp deploy/.env.example deploy/.env
nano deploy/.env      # completar TODO; ENCRYPTION_KEY idéntica a Render
chmod 600 deploy/.env
```

## Paso 3 — Levantar el stack (solo loopback, aún sin dominio)

```bash
docker compose -f docker-compose.prod.yml --env-file deploy/.env up -d --build
docker compose -f docker-compose.prod.yml ps
docker compose -f docker-compose.prod.yml logs -f backend   # esperar "Started FrcEfactBackendApplication"
```

- El primer build del backend tarda (descarga Maven + jsifenlib con el PAT).
- En DB vacía, Flyway aplica V1..V<N> — está bien **solo hasta** la migración de datos;
  el restore del paso 4 pisa todo con `--clean`.
- Smoke local sin TLS: `curl -s http://127.0.0.1:8081/api/actuator/health` → `{"status":"UP"}`
  y `curl -sI http://127.0.0.1:8082` → 200.

## Paso 3b — nginx del host + TLS (requiere DNS ya propagado)

```bash
# Verificar DNS primero:
dig +short efact.frc-ecommerce.com   # debe devolver 178.105.107.171
sudo cp deploy/nginx-vhost-efact.conf /etc/nginx/conf.d/frc-efact.conf
sudo nginx -t && sudo systemctl reload nginx
sudo certbot --nginx -d efact.frc-ecommerce.com
# Probar:
curl -I https://efact.frc-ecommerce.com/api/actuator/health   # 200, cert válido
# https://efact.frc-ecommerce.com carga el login
```

Si nginx devuelve 502 con SELinux: `getsebool httpd_can_network_connect` debe ser `on`
(debería estarlo: el host ya proxya a farmacia/headscale).
⚠️ `nginx -t` falla o el reload rompe otro vhost → restaurar sacando `frc-efact.conf` y
`sudo systemctl reload nginx`; los demás servicios de la VM tienen prioridad.

## Paso 4 — Migración de datos (ventana de mantenimiento)

Seguir Fase 1 del plan (congelar escrituras en Render, sin lotes `EN_PROCESO` —
al 2026-07-06 no había ninguno). Luego:

```bash
./deploy/migrate-db-from-render.sh --dump-only   # ensayo días antes
./deploy/migrate-db-from-render.sh               # migración real (pide confirmación)
docker compose -f docker-compose.prod.yml restart backend
docker compose -f docker-compose.prod.yml logs backend | grep -i flyway   # debe VALIDAR, no migrar
```

Comparar los counts que imprime el script contra los de referencia de arriba
(re-consultar Render el día de la migración).

## Paso 5 — Certificados .pfx — ✅ HECHO 2026-07-07 (leer el gotcha)

**Ya copiados al volumen** con los nombres que exige `empresa.certificado_path`.
⚠️ **Gotcha descubierto:** las copias locales en `frc-efact-backend/certificates/`
tienen los nombres **CRUZADOS** respecto al contenido real:

| Archivo local | Contenido real (verificado con openssl `-legacy` + password descifrado de la DB) | Va al volumen como |
|---|---|---|
| `empresa_2_1769116174461.pfx` | ANATOLE DEINZER DUARTE (vence 2026-11-18) | `empresa_1_1774969696647.pfx` |
| `empresa_1_1769110350171.pfx` | GUILLERMO FRANCO AREVALOS (vence 2026-08-20) | `empresa_2_1777486604669.pfx` |

Renombrar "por nombre" habría dejado los certificados intercambiados y la firma
de DE fallaría. Los `.pfx` son PKCS12 legacy (RC2): `openssl pkcs12` necesita
`-legacy` para abrirlos (Java 17 los lee sin problema).
El descifrado de los passwords desde la DB con la ENCRYPTION_KEY default también
**confirmó que esa key es la correcta** (pre-validación del smoke test del paso 5).

```bash
# desde tu máquina local, por cada certificado:
scp empresa_1_1774969696647.pfx empresa_2_1777486604669.pfx deploy@178.105.107.171:~/
# en la VM — el nombre debe coincidir EXACTO con empresa.certificado_path:
docker compose -f docker-compose.prod.yml cp ~/empresa_1_1774969696647.pfx backend:/app/certificates/
docker compose -f docker-compose.prod.yml cp ~/empresa_2_1777486604669.pfx backend:/app/certificates/
```

Alternativa más segura: re-subir cada certificado desde la UI (Empresas → certificado),
que regenera el path y valida el password contra la `ENCRYPTION_KEY`.

**Smoke test de cifrado (bloqueante):** abrir en la UI un timbrado con CSC. Si falla el
descifrado (error GCM / tag mismatch), la `ENCRYPTION_KEY` no es la de Render — corregir
antes de seguir.

## Paso 6 — Validación end-to-end

- [ ] Login local (`admin`) y login Auth0
- [ ] Listados históricos: facturas, documentos, notas, clientes
- [ ] Descarga de un KuDE PDF histórico
- [ ] Consulta de estado contra SIFEN de un DE ya aprobado (prueba mTLS sin emitir)
- [ ] Envío de email de prueba
- [ ] Emitir **un** documento real de bajo valor → APROBADO (firma, CDC, QR, lote, scheduler)

## Paso 7 — Cutover y post-migración

1. Anunciar URL nueva. **Suspender el backend de Render** (no borrarlo: es el rollback;
   nunca dejar dos schedulers SIFEN activos a la vez).
2. Backup: `crontab -e` (como deploy) →
   `30 3 * * * /home/deploy/frc-efact/deploy/backup-db.sh >> /var/backups/frc-efact/backup.log 2>&1`.
   Completar la copia off-site en el script (Storage Box / rclone). Probar una restauración.
3. Backups/snapshots de la VM: coordinar con los demás servicios de la VM (es compartida).
4. Monitoreo externo a `https://efact.frc-ecommerce.com/api/actuator/health`.
5. Tras 1-2 semanas estables: dar de baja Render, quitar `*.onrender.com` del CORS y del
   `connect-src` de la CSP en `SecurityConfig`, retirar `render.yaml`, y decidir si se arma
   deploy por GitHub Actions via SSH a la VM.
   ✅ CLAUDE.md, README y la skill `frc-efact-expert` ya fueron actualizados (2026-08-05):
   la regla vigente es **deploy manual por SSH**; `git push` solo corre `semantic-release`.

## Troubleshooting rápido

| Síntoma | Causa probable |
|---|---|
| Build backend falla con 401 en jsifenlib | `GITHUB_TOKEN` sin `read:packages` o no pasado como build arg |
| Backend no arranca: "DATABASE_URL is not set" | `.env` no cargado (`--env-file deploy/.env`) |
| Flyway valida con error tras el restore | Restore incompleto o versión distinta de migraciones entre rama y DB |
| Error GCM / "Tag mismatch" al abrir timbrado | `ENCRYPTION_KEY` distinta a la de Render |
| DE falla al firmar | `.pfx` ausente en `/app/certificates` o nombre ≠ `certificado_path` |
| certbot no emite certificado | DNS de `efact.` aún no propagado |
| nginx 502 hacia el stack | contenedor caído, o SELinux `httpd_can_network_connect` off |
| CORS bloqueado desde el dominio nuevo | `CORS_ALLOWED_ORIGINS` ausente en el backend |
| Puerto 8081/8082 ya en uso | otro servicio del host lo tomó — elegir otro en `deploy/.env` |
