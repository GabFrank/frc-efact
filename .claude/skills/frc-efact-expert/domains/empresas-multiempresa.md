# Dominio: Empresa y Multi-empresa

Una **Empresa** es el emisor de los DE. El sistema es **multi-empresa**: cada request opera en el contexto de una empresa, y el vínculo usuario↔empresa (`UsuarioEmpresa`) define qué empresas ve cada usuario y con qué rol. Esquema **`empresa`**. Store NgRx: `empresas`.

---

## Empresa — `empresa.empresa`

Entidad `com.frcefact.model.Empresa` (`AuditableEntity`).

### Datos fiscales / identidad
| Campo | Columna | Notas |
|---|---|---|
| `razonSocial` | `razon_social` (200, `NOT NULL`) | |
| `ruc` | `ruc` (20, **unique**, `NOT NULL`) | emisor |
| `nombreFantasia` | `nombre_fantasia` (200) | |
| `email` / `telefono` / `direccion` | | |
| `tipoContribuyente` | `tipo_contribuyente` (2, `NOT NULL`) | `PF` (default) / `PJ` (V11). Ver ⚠️ SIFEN-1 |
| `tipoSociedad` | `tipo_sociedad` (50) | |
| `ciudad` / `barrio` | `ciudad_id` / `barrio_id` (FK, EAGER) | geografía |
| `domicilioFiscalDireccion` | `domicilio_fiscal_direccion` | V10 |

### Actividades económicas
`codActividadEconomicaPrincipal` / `descActividadEconomicaPrincipal`, más las secundarias serializadas como texto: `listCodigoActividadEconomicaSecundaria` y `listDescripcionActividadEconomicaSecundaria` (columnas TEXT).

### Certificado digital `.pfx` — **por empresa**
| Campo | Columna | Notas |
|---|---|---|
| `certificadoPath` | `certificado_path` (500) | ruta al `.pfx` |
| `certificadoPasswordEncrypted` | `certificado_password_encrypted` (TEXT) | password **cifrada** (AES, `ENCRYPTION_KEY`) |
| `certificadoFechaExpiracion` | `certificado_fecha_expiracion` | |
| `sifenAmbiente` | `sifen_ambiente` (20) | `DEV` (default) / `TEST` / `PRODUCTION` |

Cada empresa firma con su propio certificado. Detalle multi-empresa en [../../../../.kiro/specs/electronic-invoicing-system/MULTI_EMPRESA_CERTIFICADOS.md](../../../../.kiro/specs/electronic-invoicing-system/MULTI_EMPRESA_CERTIFICADOS.md).

> ⚠️ Empresa conserva columnas residuales `csc_id`/`csc_encrypted`, pero el CSC operativo vive en el **timbrado** (ver [timbrados.md](timbrados.md)).
> ⚠️ **Branding/logo NO está implementado**: la entidad no tiene campo de logo; `KudePdfService.obtenerLogoPath()` es un TODO que retorna `null`. El KuDE pasa un parámetro Jasper `logo` vacío. (Discrepancia vs. CLAUDE.md, que menciona "branding/logo".)

---

## UsuarioEmpresa — `empresa.usuario_empresa`

Entidad `com.frcefact.model.UsuarioEmpresa` (`AuditableEntity`). Tabla puente N:M usuario↔empresa **con rol de contexto**.

| Campo | Columna | Notas |
|---|---|---|
| `usuario` | `usuario_id` (FK) | |
| `empresa` | `empresa_id` (FK) | |
| `rolEmpresa` | `rol_empresa` (20, `NOT NULL`) | **`ADMINISTRADOR` / `FACTURADOR` / `LECTOR`** |
| `activo` | `activo` | |

El `rolEmpresa` es un **String** (no enum). En cada request, `CustomUserDetailsService` lo mapea a authority Spring: `ADMINISTRADOR→ROLE_EMPRESA_ADMIN`, `FACTURADOR→ROLE_FACTURADOR`, `LECTOR→ROLE_LECTOR`. Detalle de la doble capa de roles en [usuarios-roles-permisos.md](usuarios-roles-permisos.md).

---

## Endpoints — `EmpresaController` `/empresas`
`POST` (multipart/json) `[A,EA]` · `PUT /{id}` (multipart/json) `[A,EA]` · `GET /{id}` · `GET` `[todos]` · `GET /mis-empresas` · `GET /buscar` · `DELETE /{id}` `[A,EA]`.

**Certificado:** `POST /{id}/certificado` (multipart `.pfx`) `[A,EA]` · `PUT /{id}/certificado/password` `[A,EA]`.

**Usuarios de la empresa:** `GET /{id}/usuarios` `[todos]` · `POST /{id}/usuarios` (vincular) `[A,EA]` · `DELETE /{empresaId}/usuarios/{usuarioId}` (desvincular) `[A,EA]`.

**Diagnóstico:** `GET /diagnostico` `[A]` (chequeo de configuración/certificado).

`GET /mis-empresas` devuelve las empresas del usuario autenticado — base de la selección de contexto activo en el frontend (guards `empresa-access`, `empresa-selected`).

---

## ⚠️ Vincular usuario no refresca la sesión (RBAC-3)
Al vincular un usuario a una empresa (`POST /{id}/usuarios`), el backend remapea roles correctamente en el siguiente request, pero el **frontend cachea `currentUser` en NgRx** con el array `roles` viejo. El usuario receptor debe **cerrar sesión y volver a entrar** para ver los permisos nuevos. Ver [../reference/known-bugs.md](../reference/known-bugs.md) y [../../../../docs/ISSUES_CANDIDATOS.md](../../../../docs/ISSUES_CANDIDATOS.md) (RBAC-3).

---

## Frontend
`features/empresas/`: `empresas-list.component.ts`, `empresa-form.component.ts`, `empresa-dashboard.component.ts`, `empresa-info/timbrados/usuarios.component.ts`, diálogos de asignación de usuarios. API `core/api/empresa-api.service.ts`. NgRx: rama `empresas`.

Índices: [../reference/entities-index.md](../reference/entities-index.md) · [../reference/endpoints-index.md](../reference/endpoints-index.md).
