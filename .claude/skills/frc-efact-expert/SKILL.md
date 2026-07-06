---
name: frc-efact-expert
description: >
  Experto en el proyecto FRC eFact (facturación electrónica SIFEN Paraguay).
  Usar SIEMPRE que se trabaje en este repo sobre: generación de Documentos
  Electrónicos (DE/XML/CDC/firma/QR), errores SIFEN (E605b, E644a, 1706, 1552,
  etc.), notas de crédito/débito/remisión, timbrados y puntos de expedición,
  migraciones Flyway, nuevos endpoints o entidades, NgRx en el frontend, roles
  y permisos, o deploys a Render. Contiene checklists operativos, playbook de
  errores SIFEN y el mapa de archivos clave.
---

# FRC eFact Expert

Guía operativa experta para trabajar en este repo. Complementa `CLAUDE.md`
(leerlo siempre) con procedimientos paso a paso y un playbook SIFEN.

## Reglas de oro (violarlas rompe producción)

1. **Push a `main` = deploy a producción en Render.** Nunca pushear sin
   confirmación explícita del usuario. Para forzar redeploy:
   `git commit --allow-empty -m "chore: trigger redeploy"` + push. Jamás usar
   la API/MCP de Render para mutar deploys (solo para inspeccionar logs/estado).
2. **Compilar antes de commitear.** Backend: `cd frc-efact-backend && ./mvnw compile`.
   Frontend: `cd frc-efact-frontend && npm run build:dev` (o `npm run lint`).
   Si falla, no se commitea.
3. **`@RequestMapping` sin `/api/`** — el context-path ya lo agrega
   (`CONTROLLER_ROUTING_RULE.md`). El frontend sí incluye `/api/` en
   `environment.apiUrl`.
4. **Nunca modificar una migración Flyway ya aplicada** — crear `V<N+1>__*.sql`.
   Ver el último `V<N>` con: `ls db/migration | sort -V | tail -1`.
5. **Nada en el esquema `public`.** Esquemas: `persona`, `empresa`,
   `financiero`, `productos`, `clientes`, `auditoria`, `catalogo`.
6. **No commitear secretos ni `.pfx`** (certificados en `certificates/`).
7. Idioma: dominio y UI en **español** (`razon_social`, `numero_factura`);
   campos genéricos en inglés (`id`, `is_active`).

## Playbook de errores SIFEN

El XML SIFEN v150 es estricto: un campo de más o de menos rechaza el DE.
**Antes de tocar `SifenService`**, leer
`docs/ANALISIS_DIFERENCIAS_SIFEN_SERVICE.md` y los manuales en `docs/sifen/`.

| Código | Causa | Fix conocido |
|---|---|---|
| E644a / 1706 | Se envía `dCuotas` con `iCondCred=1` (Plazo) | No enviar `dCuotas` en ese caso (commit `e9db5de`) |
| E605b / 1552 | Se envía `gPaConEIni` en factura a crédito sin entrega inicial | Omitir `gPaConEIni` (commit `de5542c`) |
| Errores NRE | Ver análisis dedicado | `docs/sifen/analisis-errores-nre-sifen-v150.md` |

Reglas de dominio validadas contra SIFEN:
- **Notas de crédito**: heredan moneda e items de la factura referenciada,
  motivos validados según catálogo SIFEN, fecha de firma correcta y
  numeración propia (commits `1680341`, `21f4a89`, `7a8e215`).
- **Moneda extranjera**: totales siempre en guaraníes con tipo de cambio;
  ver `docs/sifen/implementacion_monedas_sifen_v150.md` y
  `docs/sifen/ejemplo_de_moneda_extranjera.xml`.
- **CDC**: 44 caracteres; la firma usa el certificado `.pfx` por empresa
  (multi-empresa: `.kiro/specs/electronic-invoicing-system/MULTI_EMPRESA_CERTIFICADOS.md`).
- Comparar XML generado contra los ejemplos aprobados en `docs/sifen/*.xml`
  antes de enviar a SIFEN.

Código SIFEN clave:
- `service/sifen/` → `SifenService` (construcción DE), `SifenEventoService`
  (cancelación/inutilización/nominación), `SifenSchedulerService` (polling).
- `sifen/util/` → `SifenReceptorHelper`, `SifenTotalsHelper`,
  `SifenResponseParser`, `SifenDocumentoLogger`.
- `sifen/config/` → `SifenConfigFactory`, `SifenProperties`.
- Librería: fork `jsifenlib` (`io.github.gabfrank`, GitHub Packages;
  auth: `frc-efact-backend/GITHUB_PACKAGES_SETUP.md`).

## Checklist: nueva entidad (end-to-end)

Flujo completo en `docs/FLUJO_SISTEMA_ENTIDADES.md`. Orden:

**Backend** (`com.frcefact`):
1. Migración Flyway `V<N+1>__*.sql`: tabla con `id BIGSERIAL PK`,
   `creado_en`, `creado_por`, `actualizado_en`, `actualizado_por` + trigger
   `actualizar_timestamp_modificacion()`, en el esquema correcto.
2. `model/` — entidad JPA extendiendo `AuditableEntity` (`ddl-auto: validate`:
   la entidad debe calzar exacto con la migración).
3. `repository/` (+ `specification/` si hay filtros dinámicos).
4. `dto/` + `dto/mapper/`.
5. `service/` con `@PreAuthorize` según la tabla de roles de `CLAUDE.md`.
6. `controller/` — ruta sin `/api/`.
7. `./mvnw compile` y correr `./check-migrations.sh`.

**Frontend** (`src/app/`):
1. `models/` — interfaz espejo del DTO.
2. `core/api/` — servicio HTTP (`${environment.apiUrl}/<ruta>`).
3. `core/state/<entidad>/` — actions/effects/reducer/selectors (una rama
   NgRx por entidad, como `documentos`, `notas`, `timbrados`).
4. `features/<dominio>/` — separar `*-list.component.ts` y
   `*-form.component.ts`; standalone components, Material, kebab-case.
5. `npm run lint` / `npm run build:dev`.

## Trampas conocidas (no redescubrirlas)

- **Roles en dos capas mal integradas**: los `@PreAuthorize` del backend pasan
  con `rolEmpresa` (remapeo dinámico en `CustomUserDetailsService`), pero el
  `PermissionsService` del frontend solo mira `user.roles` globales → el menú
  se oculta aunque el backend autorice. Detalle y fixes en la sección
  "Issues conocidos" de `CLAUDE.md`. Workaround: asignar también el rol global
  `EMPRESA_ADMIN`; tras vincular empresa, el usuario debe re-loguearse.
- **Usuarios nuevos quedan con `roles=[]`** (ni registro local ni auto-registro
  Auth0 asignan rol por defecto).
- **Inutilización de números funciona parcialmente** — falla cuando está
  vinculada a una factura legal (`docs/TAREAS_PENDIENTES.md`).
- **`dEst`/código de establecimiento**: validar no-null antes de armar el DE;
  el repo de referencia lo toma de `Sucursal`, este proyecto de
  `TimbradoDetalle` (ver `docs/ANALISIS_DIFERENCIAS_SIFEN_SERVICE.md`).
- JWT del frontend vive **en memoria**, no en `localStorage`.
- Datos sensibles (CSC, password de certificado) se cifran con
  `ENCRYPTION_KEY` (AES-256); `JWT_SECRET` mínimo 512 bits (HS512).

## Comandos rápidos

```bash
# Backend
cd frc-efact-backend && ./dev.sh          # dev (recomendado)
./mvnw compile && ./mvnw test             # verificación pre-commit
./check-migrations.sh                     # valida Flyway
# Swagger: http://localhost:8080/swagger-ui.html  (context-path /api)

# Frontend
cd frc-efact-frontend && npm start        # http://localhost:4200
npm run lint && npm run test:ci
```

Credenciales dev: `admin/Admin123!`, `empresa_admin/Empresa123!`,
`facturador/Facturador123!`, `lector/Lector123!`.

## Dónde leer más, por tema

| Tema | Documento |
|---|---|
| Endpoints REST | `frc-efact-backend/API_DOCUMENTATION.md` |
| Estándares DB | `frc-efact-backend/DATABASE_STANDARDS.md` |
| NRE y cancelación v150 | `docs/sifen/manual-implementacion-nre-y-cancelacion-sifen-v150.md` |
| Notas C/D/R con jsifenlib | `docs/sifen/manual-notas-credito-debito-remision-sifen-jsifenlib.md` |
| Tipos de clientes/productos SIFEN | `docs/sifen/tipos_clientes_sifen_v150.md`, `docs/sifen/tipos_productos_sifen_v150.md` |
| KuDE Jasper | `frc-efact-backend/src/main/resources/reports/`, `docs/sifen/KuDE_NotaCredito.jrxml` |
| Auth0 | `docs/AUTH0_SETUP.md` |
| SMTP / envío de facturas | `frc-efact-backend/CONFIGURACION_GMAIL.md` |
| Deployment Render | `render.yaml`, `docs/deployment/` |
| Specs por feature | `.kiro/specs/` |
| Repo de referencia (comparar SifenService) | `docs/franco-system-backend-filial/`, `docs/rshk-jsifenlib/` |
