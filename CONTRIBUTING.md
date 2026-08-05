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
| `VM_SSH_KEY` | Clave privada SSH con acceso a ese usuario |

⚠️ **La VM es compartida** con servicios productivos ajenos (nginx del host, PostgreSQL
nativo, farmacia Next.js, headscale, mediamtx). El workflow está acotado a `~/frc-efact` y
al stack de `docker-compose.prod.yml`: no hace `down`, no borra volúmenes, no toca nginx.

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
