# Workflow: Deploy a producción (VM Hetzner)

> ⚠️ **El deploy es MANUAL por SSH.** Desde el cutover del **2026-07-07**, `git push` **no**
> despliega nada. **Preguntá SIEMPRE al usuario antes de `commit` + `push`, y con más razón
> antes de tocar la VM.** No hay staging.

Producción: **`https://efact.frc-ecommerce.com`** (dominio único: `/` → SPA, `/api` → backend).
Runbook completo y verificado: [`docs/deployment/hetzner/RUNBOOK_VM.md`](../../../../docs/deployment/hetzner/RUNBOOK_VM.md).

## La VM (verificado 2026-07-06)

`deploy@178.105.107.171` — Fedora 42, 4 vCPU, 7.6 GB RAM. Acceso solo por clave SSH.

⚠️ **VM COMPARTIDA con servicios productivos ajenos que NO se tocan:** nginx del host (dueño de
80/443), PostgreSQL **nativo** en 5432, farmacia Next.js (:3000), headscale (**:8080**),
mediamtx. Consecuencias de diseño:

- El stack de eFact expone **solo loopback**: backend `127.0.0.1:8081`, frontend `127.0.0.1:8082`.
  **Nunca usar 8080** (headscale) ni mapear el 5432 (postgres nativo del host).
- TLS y routing los hace el **nginx del host + certbot**, no el stack. No hay Caddy.
- `deploy/setup-vm.sh` solo instala Docker CE; no toca firewall, SSH ni nginx.

## Qué pasa cuando pusheás a `main`

1. **No se despliega nada.** Ni la VM ni Render (suspendido) reaccionan al push.
2. **GitHub Actions corre `semantic-release`** (`.github/workflows/release.yml`, `on: push
   branches:[main]`). Con `feat`/`fix`/`perf` genera tag + `CHANGELOG.md` + bump de
   `frc-efact-backend/pom.xml` y `frc-efact-frontend/package.json`. Con `docs`/`chore`/
   `refactor`/`test`/`ci` **no libera**. Se saltea si el commit contiene `[skip ci]`.

## Deploy de un cambio (procedimiento)

```bash
ssh deploy@178.105.107.171
cd ~/frc-efact
git pull
docker compose -f docker-compose.prod.yml --env-file deploy/.env up -d --build
docker compose -f docker-compose.prod.yml ps
docker compose -f docker-compose.prod.yml logs -f backend   # esperar "Started FrcEfactBackendApplication"
```

- Podés limitar el rebuild a un servicio: `... up -d --build backend` (o `frontend`).
- El build del backend descarga `jsifenlib` de GitHub Packages con el PAT del `.env` — tarda.
- **Flyway corre al arrancar el backend.** Si una migración falla, el backend no levanta:
  mirar logs antes de asumir otra causa.
- El `--env-file deploy/.env` **no es opcional**: sin él el backend muere con
  "DATABASE_URL is not set".

## Variables de entorno (VM: `deploy/.env`, plantilla `deploy/.env.example`, gitignored)

| Var | Para qué | Si falta |
|---|---|---|
| `POSTGRES_PASSWORD` | Password del postgres del stack | El compose aborta (`:?`) |
| `GITHUB_USERNAME` + `GITHUB_TOKEN` | **Build del backend**: bajar `jsifenlib` de GitHub Packages (scope `read:packages`) | `mvn package` no resuelve la dependencia → **build falla con 401** |
| `PORT=8080` | **Obligatoria.** El `ENTRYPOINT` del Dockerfile es forma exec y pasa el literal `-Dserver.port=${PORT:-8080}` sin expandir; Spring lo resuelve como placeholder con default `-8080` | Tomcat arranca en puerto **-1** → conector HTTP deshabilitado (commit `4354836`) |
| `ENCRYPTION_KEY` | AES-256 (32 chars) para cifrar CSC y password del `.pfx` | Error GCM / "Tag mismatch" al abrir un timbrado con CSC. **Debe ser idéntica a la que usaba Render** |
| `MAIL_PASSWORD` | Gmail SMTP (`frcsistemasinformaticos@gmail.com`) | Rompe el envío de facturas por email |
| `JWT_SECRET` | Firma HS512 (mín. 512 bits) | Tokens inválidos |
| `CORS_ALLOWED_ORIGINS` | Orígenes extra, coma-separados; se suman a los del código (`SecurityConfig`, commit `f58aac4`) | CORS bloqueado desde el dominio nuevo |
| `BACKEND_PORT` / `FRONTEND_PORT` | Puertos loopback (default 8081 / 8082) | Choque con otro servicio del host |

## Reglas de disparo (no negociables)

- **Nunca deployar sin confirmación explícita del usuario.** La VM es compartida con
  servicios productivos ajenos.
- **Render está suspendido, no dado de baja** (`srv-d61m4p4hg0os73fpbjm0`) — es la ventana
  de rollback. **Conserva `autoDeploy: yes` sobre `main`**, así que reanudarlo lo vuelve a
  poner a auto-desplegar. No reanudarlo sin decisión explícita, y **nunca dejar dos
  schedulers SIFEN activos a la vez** (Render + VM emitirían/consultarían en paralelo).
- Las tools de Render MCP (`list_deploys`, `get_deploy`, `get_service`, `list_logs`) siguen
  sirviendo para **inspeccionar** el Render suspendido — nunca para mutar estado.

## Antes de deployar (checklist)

1. **Compilar.** Backend: `cd frc-efact-backend && ./mvnw compile`. Frontend:
   `cd frc-efact-frontend && npm run build:dev` (o `npm run lint`). Si falla → **no commitees**.
2. **No commitear secretos** ni `.pfx`. `deploy/.env` está gitignored — verificarlo.
3. **Preguntar al usuario** antes de `commit` + `push` **y** antes de tocar la VM.
4. Mensaje de commit **convencional** para que `semantic-release` versione bien.

## Después del deploy

- **Health:** `curl -I https://efact.frc-ecommerce.com/api/actuator/health` → 200.
  Loopback en la VM: `curl -s http://127.0.0.1:8081/api/actuator/health` → `{"status":"UP"}`.
- **Backup diario:** systemd timer `frc-efact-backup.timer` a las 03:30 UTC (Fedora sin
  cronie → **no** es cron). `systemctl status frc-efact-backup.timer`.
- **Certificados `.pfx`:** viven en el volumen del backend en `/app/certificates`. El nombre
  del archivo debe coincidir **exacto** con `empresa.certificado_path` en la DB. Preferí
  re-subirlos desde la UI (Empresas → certificado) antes que copiarlos a mano.

## Troubleshooting rápido

| Síntoma | Causa probable |
|---|---|
| Build backend falla con 401 en jsifenlib | `GITHUB_TOKEN` sin `read:packages` o no pasado como build arg |
| "DATABASE_URL is not set" | Falta `--env-file deploy/.env` |
| "Tomcat started on port -1" | Falta `PORT=8080` en el `.env` |
| Flyway valida con error | Migraciones de la rama desalineadas con la DB |
| Error GCM / "Tag mismatch" al abrir timbrado | `ENCRYPTION_KEY` distinta a la de Render |
| DE falla al firmar | `.pfx` ausente en `/app/certificates` o nombre ≠ `certificado_path` |
| nginx 502 hacia el stack | Contenedor caído, o SELinux `httpd_can_network_connect` off |
| CORS bloqueado desde el dominio nuevo | Falta `CORS_ALLOWED_ORIGINS` |
| Puerto 8081/8082 en uso | Otro servicio del host lo tomó — cambiarlo en `deploy/.env` |

> **Legacy:** la operación en Render está en [`docs/deployment/render/`](../../../../docs/deployment/render/)
> y `render.yaml`, conservados hasta dar de baja el servicio.
> ⚠️ `docs/deployment/AGREGAR_VARIABLES_RENDER.md` y `RENDER_DOCKER_BUILD_ARGS.md` tienen
> **secretos filtrados** (token GitHub) — a revocar/purgar (SEC-2, ver
> [../reference/known-bugs.md](../reference/known-bugs.md)).
