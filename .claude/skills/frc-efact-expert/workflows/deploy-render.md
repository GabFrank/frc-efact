# Workflow: Deploy a producción (Render)

> ⚠️ **Push a `main` = deploy a producción.** No hay staging. **Preguntá SIEMPRE al usuario antes de `commit` + `push`.** Nunca asumas que se quiere pushear.

## Qué pasa cuando pusheás a `main`
Dos cosas se disparan en paralelo:
1. **Render auto-despliega.** Esto ocurre por el **default de Render** (auto-deploy sobre la branch conectada), **NO** está pineado en `render.yaml` — no hay claves `autoDeploy`/`branch`/`autoDeployTrigger` en el blueprint.
2. **GitHub Actions corre `semantic-release`** (`.github/workflows/release.yml`, `on: push branches:[main]`): genera el próximo tag/version y changelog desde los mensajes de commit convencionales. Se saltea si el commit contiene `[skip ci]`.

## Blueprint (`render.yaml`)
Tres recursos en region `oregon`, plan `free`:
- **`frc-efact-backend`** — `runtime: docker`, `dockerfilePath: ./frc-efact-backend/Dockerfile`, context `./frc-efact-backend`. Multi-stage: Maven build → JRE Alpine.
- **`frc-efact-frontend`** — `runtime: static`, `buildCommand: cd frc-efact-frontend && npm ci && npm run build:prod`, publish `frc-efact-frontend/dist/frc-efact-frontend/browser`, rewrite `/* → /index.html` (SPA).
- **`frc-efact-db`** — PostgreSQL, inyectado al backend como `DATABASE_URL` vía `fromDatabase`.

## Variables de entorno OBLIGATORIAS (Render Dashboard → Environment)
El `render.yaml` genera `JWT_SECRET` (`generateValue`) e inyecta `DATABASE_URL`, pero **estas hay que cargarlas a mano** o el deploy falla o queda inseguro:

| Var | Para qué | Si falta |
|---|---|---|
| `GITHUB_USERNAME` + `GITHUB_TOKEN` | **Build del backend**: bajar `jsifenlib` de GitHub Packages | El `mvn package` no resuelve la dependencia → **build falla**. Se pasan como **Docker Build Arguments** en Render (Settings → Build & Deploy → Docker Build Arguments), no solo como env vars. Valor típico usuario: `GabFrank`. |
| `ENCRYPTION_KEY` | AES-256 (32 chars) para cifrar CSC y password del `.pfx` | Cifra con una **clave pública conocida** → datos sensibles comprometidos (SEC-3). |
| `MAIL_PASSWORD` | Gmail SMTP (`frcsistemasinformaticos@gmail.com`) para enviar facturas | Puede **romper el arranque** o el envío de email (CFG-1). |
| `JWT_SECRET` | Firma HS512 (mín. 512 bits) | Generado por Render; si lo fijás a mano, respetá el largo. |
| `DATABASE_URL` | Conexión Postgres | Inyectado por el blueprint desde `frc-efact-db`. |
| `SPRING_PROFILES_ACTIVE=prod` | Perfil prod | Ya seteado en `render.yaml` (y default del Dockerfile). |

> `JWT_EXPIRATION` y `LOG_LEVEL` figuran en `render.yaml` pero son **inertes** para la app (CFG-3). No dependas de ellos.

## Reglas de disparo de deploy (no negociables)
- **Deploys SOLO por `git push`.** **NO** uses `mcp__render__*`, la API de Render, ni el botón "Manual Deploy" del dashboard para lanzar deploys — rompe la trazabilidad commit↔deploy.
- Para forzar un redeploy del mismo commit: `git commit --allow-empty -m "chore: trigger redeploy"` + push.
- Las tools de Render MCP (`list_deploys`, `get_deploy`, `get_service`, `list_logs`, env vars) **sí** se usan para **inspeccionar/diagnosticar** — nunca para mutar deploys.

## Antes de pushear (checklist)
1. **Compilar.** Backend: `cd frc-efact-backend && ./mvnw compile`. Frontend: `cd frc-efact-frontend && npm run build:dev` (o `npm run lint`). Si falla → **no commitees**, arreglá.
2. **No commitear secretos** ni `.pfx`.
3. **Preguntar al usuario** si quiere `commit` + `push` (= deploy a prod). Esperar confirmación explícita.
4. Mensaje de commit **convencional** (`feat:`, `fix:`, `chore:`…) para que `semantic-release` versione bien.

## Después del deploy
- **Health backend:** `GET https://frc-efact-backend.onrender.com/api/actuator/health` (local: `http://localhost:8080/api/actuator/health`).
- Frontend: `https://frc-efact-frontend.onrender.com`.
- Diagnóstico: Render MCP `list_deploys` / `get_deploy` / `list_logs` (solo lectura).
- Flyway corre las migraciones nuevas al arrancar el backend. Si una migración falla, el backend no levanta — revisá logs.

> Docs de referencia: `docs/deployment/PRODUCTION_DEPLOYMENT.md`, `docs/deployment/DEPLOYMENT_CHECKLIST.md`, `docs/deployment/STEP_BY_STEP_GUIDE.md`. ⚠️ `docs/deployment/AGREGAR_VARIABLES_RENDER.md` y `RENDER_DOCKER_BUILD_ARGS.md` tienen **secretos filtrados** (token GitHub) — a revocar/purgar (SEC-2, ver [../reference/known-bugs.md](../reference/known-bugs.md)).
