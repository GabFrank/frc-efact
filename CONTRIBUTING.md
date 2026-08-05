# Flujo de desarrollo — FRC eFact

Adaptación del modelo que ya usan los 4 repos de `frc-comercial`, ajustado a que **este
proyecto tiene un solo entorno**: no hay alpha ni beta, producción es la VM Hetzner.

---

## Ramas

```
feature/*  --PR-->  develop  --PR-->  main  --(semantic-release)-->  tag + CHANGELOG
hotfix/*   --PR-->  main     --PR-->  develop   (obligatorio post-hotfix)
```

| Rama | Rol |
|---|---|
| `main` | Rama de release. Cada merge dispara `semantic-release`. **Nunca push directo.** |
| `develop` | Integración. Es donde se acumulan las features antes de un release. |
| `feature/*`, `fix/*`, `refactor/*`, `chore/*` | Trabajo del día a día. Salen de `develop`. |
| `hotfix/*` | Sale de **`main`**, no de `develop`. |

> Este repo usa **`main`**, no `master` (a diferencia de `frc-comercial`).
> **No hay canal de prerelease**: `develop` no genera tags. Sería ruido — no existe un
> entorno donde desplegar un `v1.2.0-alpha.N`. Si en algún momento se levanta un stack de
> staging en la VM, ahí sí tiene sentido darle canal `alpha` a `develop`.

## Día a día

```bash
git checkout develop && git pull
git checkout -b feature/facturacion-descuento-global
# … commits convencionales …
git push -u origin feature/facturacion-descuento-global
# PR a develop → CI verde → merge
```

Cuando `develop` está listo para salir: **PR de `develop` a `main`**. Al mergear,
`semantic-release` calcula la versión, crea el tag, actualiza `CHANGELOG.md` y bumpea
`frc-efact-backend/pom.xml` + `frc-efact-frontend/package.json`.

### ⚠️ Después de CADA release: PR `main` → `develop`

`semantic-release` commitea el bump de versión **solo en `main`** (`chore(release): vX.Y.Z
[skip ci]`). Si no se baja a `develop`, esa rama se queda con `pom.xml` y los `package.json` en
la versión anterior, y la divergencia crece con cada release hasta que un PR `develop → main`
empieza a mostrar historia cruzada.

Es el mismo patrón que en `frc-comercial` obliga a un PR `master → develop` post-hotfix — acá
aplica **después de cada release**, porque el bot siempre commitea en `main`.

```bash
gh pr create --base develop --head main --title "chore: sincronizar develop con main tras el release vX.Y.Z"
```

## Mensajes de commit

Conventional Commits. Lo que libera versión (ver `.releaserc.json`):

| Prefijo | Efecto |
|---|---|
| `feat:` | minor (`1.1.0` → `1.2.0`) |
| `fix:` | patch (`1.1.0` → `1.1.1`) |
| `perf:` | patch |
| `feat!:` / `BREAKING CHANGE:` | major |
| `docs:` `chore:` `refactor:` `test:` `ci:` `style:` | **no libera** |

Scope opcional y en minúsculas: `feat(facturacion): …`, `fix(sifen): …`.

## ⚠️ Merge commit, NO squash

En el PR de `develop` a `main` usar **merge commit**. El squash colapsa todos los
`feat:`/`fix:` en un solo mensaje y `semantic-release` calcula mal el bump — el mismo
motivo por el que en `frc-comercial` está prohibido.

De `feature/*` a `develop` el squash es tolerable si el mensaje resultante es convencional,
pero por consistencia entre repos: **merge commit siempre**.

## CI

`.github/workflows/ci.yml` corre en PRs a `main` y `develop`, y en push a `develop`.

| Job | Bloqueante | Qué hace |
|---|---|---|
| `backend` | ✅ | `./mvnw clean package -DskipTests` |
| `backend-tests` | ❌ **por ahora** | `./mvnw test` — hoy fallan 6 tests de validación de RUC |
| `frontend` | ✅ | `npm ci && npm run build:prod` |

**Por qué `backend-tests` no bloquea:** al 2026-08-05 fallan 6 tests en
`RucCalculatorTest`, `RucValidatorTest` y `ValidadoresParaguayosTest`. No son tests
podridos — son la evidencia del bug conocido del algoritmo de dígito verificador de RUC
(el mismo que tiene la validación del frontend deshabilitada sirviendo mocks). Ver
`docs/TAREAS_PENDIENTES.md` §4. **Cuando ese bug se arregle, sacar el `continue-on-error`
y hacer el job bloqueante.**

**Por qué no se corre lint:** el target `lint` **no existe** en `angular.json` — falta
`@angular-eslint/schematics`. El gate real del frontend es el build AOT de producción.

### Branch protection: no está disponible

Verificado el 2026-08-05: en repo privado con plan Free, tanto
`/repos/{owner}/{repo}/branches/{branch}/protection` como `/repos/{owner}/{repo}/rulesets`
devuelven `403 Upgrade to GitHub Pro or make this repository public`.

Y **hacer el repo público quedó descartado** — el historial contiene certificados `.pfx` de firma
SIFEN de producción que **no se pueden re-emitir**, así que un archivo olvidado en una purga sería
una exposición permanente sin remedio. Detalle en
[docs/TAREAS_PENDIENTES.md](docs/TAREAS_PENDIENTES.md) §9.

**Consecuencia práctica: todo lo que sigue en este documento es una convención, no una regla que
GitHub haga cumplir.** Nada impide hoy un `git push` directo a `main`. Las dos formas de cerrar
eso son GitHub Pro (protección server-side real) o un hook `pre-push` local, que cubre el
descuido propio pero no es server-side.

### Required status checks (cuando haya branch protection)

Marcar como obligatorios **solo** estos dos:

- `Backend · compile + package`
- `Frontend · build prod`

**No** marcar `Backend · tests (no bloqueante)` mientras tenga `continue-on-error` — para eso
está el flag, y hacerlo required lo volvería bloqueante por la puerta de atrás.

### Secrets que necesita el CI

| Secret | Para qué |
|---|---|
| `GH_PACKAGES_USER` | Usuario de GitHub Packages (típicamente `GabFrank`) |
| `GH_PACKAGES_TOKEN` | PAT con `read:packages` — `jsifenlib` vive en `GabFrank/rshk-jsifenlib`, así que el `GITHUB_TOKEN` automático del job **no alcanza** |

## Deploy

**No es automático.** `.github/workflows/deploy.yml` es `workflow_dispatch`: hay que
lanzarlo a mano, elegir servicio y escribir `DEPLOY` como confirmación.

Alternativa manual (equivalente): SSH a la VM y `docker compose up -d --build`.
Procedimiento y gotchas en
[docs/deployment/hetzner/RUNBOOK_VM.md](docs/deployment/hetzner/RUNBOOK_VM.md).

### Secrets que necesita el deploy

| Secret | Valor |
|---|---|
| `VM_HOST` | `178.105.107.171` |
| `VM_USER` | `deploy` |
| `VM_SSH_KEY` | Clave privada SSH **dedicada al CI** (`github-actions-deploy@frc-efact`), no una personal |

La VM necesita además `origin` apuntando a GitHub por el alias SSH `github-frc-efact` (deploy
key read-only) — **no** `github.com`, que en esa máquina ya está tomado por otro proyecto. Ver
[RUNBOOK_VM.md](docs/deployment/hetzner/RUNBOOK_VM.md), sección *Git en la VM*.

⚠️ **La VM es compartida** con servicios productivos ajenos (nginx del host, PostgreSQL
nativo, farmacia Next.js, headscale, mediamtx). El workflow está acotado a `~/frc-efact` y
al stack de `docker-compose.prod.yml`: no hace `down`, no borra volúmenes, no toca nginx.

⚠️ **`--env-file deploy/.env` no es opcional en NINGÚN comando de compose**, ni en un `ps`
inocente. Sin él, compose falla al interpolar `${POSTGRES_PASSWORD:?}` y sale distinto de cero.
Eso hizo fallar el primer run de `deploy.yml` (#7) **después** de haber desplegado bien, y se
salteó el health check.

### Antes de un deploy que incluya migración Flyway

Flyway corre al arrancar el backend, así que una migración rota deja producción **caída**, no
degradada. Y si la migración muta datos, revertir el código no alcanza — hay que revertir los
datos también.

```bash
ssh deploy@178.105.107.171 'cd ~/frc-efact && ./deploy/backup-db.sh'
```

El backup automático es diario a las 03:30 UTC; antes de una migración conviene uno fresco.

## Migraciones Flyway

Mismas reglas que en `frc-comercial`, con un matiz propio: acá **Flyway corre al arrancar
el backend**, así que una migración rota deja producción caída, no degradada.

- **Siempre aditivas.** Nada de `DROP`/`RENAME` de columnas sin estrategia de 2 versiones.
- **Nunca** modificar una migración ya aplicada — crear `V<N+1>__*.sql`.
- `ddl-auto: validate`: la entidad JPA tiene que calzar exacto con la migración.
- Validar con `./check-migrations.sh` antes del PR.

## Lo que NUNCA hacer

1. Push directo a `main` o `develop` — siempre vía PR.
2. `git push --force` a ramas compartidas.
3. Squash merge en el PR de `develop` a `main`.
4. Modificar migraciones Flyway ya aplicadas.
5. Commitear secretos, `.pfx`, `.env`, tokens o service accounts.
6. Reanudar el servicio de Render sin decisión explícita — conserva `autoDeploy` sobre
   `main` y dejaría **dos schedulers SIFEN activos en paralelo**.
7. Deployar a la VM sin confirmación explícita — es compartida.
8. Saltear el CI con `--no-verify`.
