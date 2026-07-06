# Runbook: configuración de la VM Hetzner y migración

Guía ejecutable paso a paso. Pensada para correrse desde una **sesión local de
Claude Code con acceso SSH a la VM** (o a mano). El contexto y los riesgos están en
[PLAN_MIGRACION_HETZNER.md](PLAN_MIGRACION_HETZNER.md) — leerlo antes.

**Artefactos ya listos en el repo** (rama `claude/frc-efact-expert-skill-cbsx4z`):

| Archivo | Qué es |
|---|---|
| `docker-compose.prod.yml` | Stack completo: postgres + backend + frontend + caddy |
| `deploy/Caddyfile` | Reverse proxy con TLS automático (dominios por env var) |
| `deploy/.env.example` | Plantilla de secretos → copiar a `deploy/.env` (gitignored) |
| `deploy/setup-vm.sh` | Bootstrap: Docker, ufw, fail2ban, unattended-upgrades, SSH hardening |
| `deploy/migrate-db-from-render.sh` | Dump desde Render + restore local + verificación |
| `deploy/backup-db.sh` | Backup diario DB + certificados (instalar en cron) |
| `frc-efact-frontend/Dockerfile` | Build multi-stage Angular → nginx |

## Prerrequisitos (fuera de la VM)

- [ ] **Revocar el PAT filtrado** en `docs/deployment/AGREGAR_VARIABLES_RENDER.md` y crear uno
      nuevo con scope `read:packages`.
- [ ] Exportar del dashboard de Render **todas** las env vars del backend
      (`ENCRYPTION_KEY`, `MAIL_PASSWORD`, `SIFEN_*`, …) y la **External Database URL** de `frc-efact-db`.
- [ ] Reunir los `.pfx` de todas las empresas activas + passwords.
- [ ] DNS: registros A de `API_DOMAIN` y `APP_DOMAIN` → IP de la VM (TTL bajo, ej. 300).
- [ ] Auth0 dashboard → aplicación SPA: agregar `https://<APP_DOMAIN>` a Allowed Callback URLs,
      Allowed Logout URLs, Allowed Web Origins y Allowed Origins (CORS). **No quitar** las de onrender todavía.

## Cambio de código pendiente de dominio

Cuando el dominio esté definido, en `frc-efact-frontend/src/environments/environment.prod.ts`:
`apiUrl: 'https://<API_DOMAIN>/api'` y `redirect_uri: 'https://<APP_DOMAIN>'`.
Commitear en la rama (⚠️ recordar que al mergear a `main` esto apunta el build de Render al
dominio nuevo — coordinar con el cutover).

## Paso 1 — Bootstrap de la VM

```bash
ssh root@<IP_VM>
git clone https://github.com/GabFrank/frc-efact.git /root/frc-efact
cd /root/frc-efact
git checkout claude/frc-efact-expert-skill-cbsx4z   # hasta que se mergee a main
chmod +x deploy/*.sh
./deploy/setup-vm.sh
```

Verificar: `docker --version`, `ufw status` (22/80/443), `systemctl status fail2ban`.

## Paso 2 — Secretos

```bash
cp deploy/.env.example deploy/.env
nano deploy/.env      # completar TODO; ENCRYPTION_KEY idéntica a Render
chmod 600 deploy/.env
```

## Paso 3 — Levantar el stack

```bash
docker compose -f docker-compose.prod.yml --env-file deploy/.env up -d --build
docker compose -f docker-compose.prod.yml ps
docker compose -f docker-compose.prod.yml logs -f backend   # esperar "Started FrcEfactBackendApplication"
```

- El primer build del backend tarda (descarga Maven + jsifenlib con el PAT).
- En DB vacía, Flyway aplica las migraciones V1..V<N> — está bien **solo hasta** la migración
  de datos; el restore del paso 4 pisa todo con `--clean`.
- Probar TLS: `curl -I https://<API_DOMAIN>/api/actuator/health` → 200 y certificado válido.
- Probar SPA: `https://<APP_DOMAIN>` carga el login.

## Paso 4 — Migración de datos (ventana de mantenimiento)

Seguir Fase 1 del plan (congelar escrituras en Render, sin lotes `EN_PROCESO`). Luego:

```bash
./deploy/migrate-db-from-render.sh --dump-only   # ensayo días antes
./deploy/migrate-db-from-render.sh               # migración real (pide confirmación)
docker compose -f docker-compose.prod.yml restart backend
docker compose -f docker-compose.prod.yml logs backend | grep -i flyway   # debe VALIDAR, no migrar
```

Comparar los counts que imprime el script contra los mismos queries en Render.

## Paso 5 — Certificados .pfx

```bash
# desde tu máquina local, por cada certificado:
scp empresa_X.pfx root@<IP_VM>:/root/
# en la VM — el nombre de archivo debe coincidir con empresa.certificado_path:
docker compose -f docker-compose.prod.yml cp /root/empresa_X.pfx backend:/app/certificates/
```

Alternativa más segura: re-subir cada certificado desde la UI (Empresas → certificado),
que regenera el path y valida el password contra la `ENCRYPTION_KEY`.

**Smoke test de cifrado (bloqueante):** abrir en la UI un timbrado con CSC. Si falla el
descifrado, la `ENCRYPTION_KEY` no es la de Render — corregir antes de seguir.

## Paso 6 — Validación end-to-end

- [ ] Login local (`admin`) y login Auth0
- [ ] Listados históricos: facturas, documentos, notas, clientes
- [ ] Descarga de un KuDE PDF histórico
- [ ] Consulta de estado contra SIFEN de un DE ya aprobado (prueba mTLS sin emitir)
- [ ] Envío de email de prueba
- [ ] Emitir **un** documento real de bajo valor → APROBADO (firma, CDC, QR, lote, scheduler)

## Paso 7 — Cutover y post-migración

1. Anunciar URL nueva. **Suspender el backend de Render** (no borrarlo: es el rollback;
   nunca dejar dos schedulers activos contra SIFEN).
2. Instalar backup: `crontab -e` → `30 3 * * * /root/frc-efact/deploy/backup-db.sh >> /var/log/frc-efact-backup.log 2>&1`.
   Completar la copia off-site en el script (Storage Box / rclone). Probar una restauración.
3. Activar Hetzner Backups de la VM en la consola de Hetzner.
4. Monitoreo externo a `https://<API_DOMAIN>/api/actuator/health`.
5. Tras 1-2 semanas estables: dar de baja Render, quitar `*.onrender.com` del CORS,
   retirar `render.yaml`, actualizar CLAUDE.md y la skill (la regla "push = deploy" pasa a
   ser via GitHub Actions por SSH, si se configura).

## Troubleshooting rápido

| Síntoma | Causa probable |
|---|---|
| Build backend falla con 401 en jsifenlib | `GITHUB_TOKEN` sin `read:packages` o no pasado como build arg |
| Backend no arranca: "DATABASE_URL is not set" | `.env` no cargado (`--env-file deploy/.env`) |
| Flyway valida con error tras el restore | Restore incompleto o versión distinta de migraciones entre rama y DB |
| Error GCM / "Tag mismatch" al abrir timbrado | `ENCRYPTION_KEY` distinta a la de Render |
| DE falla al firmar | `.pfx` ausente en `/app/certificates` o nombre ≠ `certificado_path` |
| Caddy no emite certificado | DNS aún no propagado o puerto 80/443 cerrado |
| CORS bloqueado desde el dominio nuevo | `CORS_ALLOWED_ORIGINS` ausente en el backend |
