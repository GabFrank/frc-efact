# Flujo del Sistema — Mapa de Entidades

Mapa real, entidad por entidad, del sistema FRC eFact: qué entidades existen, cómo se agrupan por
dominio, cómo fluye la facturación electrónica hacia SIFEN, y el patrón de capas que usan backend
y frontend. Todo lo de acá está verificado contra el código.

---

## 1. Entidades por dominio

Las entidades JPA viven en `frc-efact-backend/src/main/java/com/frcefact/model/`. Todas heredan de
la clase base de auditoría en `model/base/` (`creadoEn / creadoPor / actualizadoEn / actualizadoPor`).

### RBAC / usuarios
- **Usuario** — cuenta de acceso (username, email, `password_hash` BCrypt, campos Auth0).
- **Rol** — catálogo de roles globales: `ADMIN`, `EMPRESA_ADMIN`, `FACTURADOR`, `LECTOR`.
- **UsuarioRol** — vincula `Usuario` ↔ `Rol` (roles **globales**).
- **UsuarioEmpresa** — vincula `Usuario` ↔ `Empresa` con un `rolEmpresa` (string:
  `ADMINISTRADOR` / `FACTURADOR` / `LECTOR`), es decir el rol **por empresa** (multi-empresa).

> ⚠️ Hay **dos capas de roles** (global y por empresa) mapeadas dinámicamente en
> `CustomUserDetailsService`. Ver los bugs asociados en [TAREAS_PENDIENTES.md](TAREAS_PENDIENTES.md).

### Empresa y timbrados
- **Empresa** — emisor (RUC, razón social, certificado `.pfx`, CSC, actividad económica).
- **Timbrado** — timbrado SET (físico o electrónico) asociado a una empresa.
- **TimbradoDetalle** — punto de expedición / establecimiento con su rango de numeración.

### Clientes y productos
- **Cliente** — receptor del DE (PF/PJ/EG, contribuyente o no, campos SIFEN). Scoped por empresa.
- **Producto** — ítem facturable (`tipoTransaccion`, unidad de medida, IVA 0/5/10).

### Facturación
- **FacturaLegal** — factura legal (autonumera por timbrado; soporta moneda extranjera con tipo de
  cambio, totales en guaraníes).
- **FacturaLegalItem** — línea de la factura.

### Documento electrónico y lotes
- **DocumentoElectronico** — el DE en sí: XML SIFEN, **CDC** de 44 caracteres, firma digital,
  QR y estado (`EstadoDE`).
- **LoteDE** — agrupación de DE para enviar a SIFEN (`EstadoLoteDE`).

### Eventos SIFEN
- **EventoCancelacionDE** — cancelación de un DE aprobado.
- **EventoInutilizacionDE** — inutilización de un rango de numeración.
- **EventoNominacionDE** — nominación (asignar receptor) de un DE.

### Notas
- **NotaCredito** + **NotaCreditoItem**
- **NotaDebito** + **NotaDebitoItem**
- **NotaRemision** + **NotaRemisionItem**

> Las notas heredan moneda/ítems de la factura referenciada y tienen su propia numeración y KuDE PDF.

### Transporte (para Nota de Remisión)
- **Vehiculo** — datos del vehículo transportador. Scoped por empresa.
- **Chofer** — datos del chofer/transportista. Scoped por empresa.

### Geografía (catálogo precargado)
- **Pais**, **Departamento**, **Ciudad**, **Distrito**, **Barrio** — jerarquía territorial paraguaya
  usada para direcciones de receptor y para la Nota de Remisión.

### Auditoría
- **AuditLog** — registro de auditoría (esquema `auditoria`, JSONB con valores antes/después),
  poblado automáticamente por `AuditAspect` (AOP).

### Enums de dominio (en `model/`)
- **EstadoDE**: `PENDIENTE, EN_PROCESO, APROBADO, RECHAZADO, CANCELADO, ERROR`
- **EstadoLoteDE**: `PENDIENTE, EN_PROCESO, APROBADO, RECHAZADO, ERROR, PROCESADO, ERROR_ENVIO, ERROR_PERMANENTE`
- **EstadoEvento**: `PENDIENTE, APROBADO, RECHAZADO, ERROR_ENVIO`
- **TipoClienteSifen**: `PERSONA_FISICA, PERSONA_JURIDICA, NO_CONTRIBUYENTE, EXTRANJERO, GUBERNAMENTAL`
- **TipoTransaccionProducto**: 13 valores (`VENTA_MERCADERIA`(1) … `VENTA_CREDITO_FISCAL`(12), `MUESTRAS_MEDICAS`(13))
- **AccionEnum**: `CREATE, UPDATE, DELETE, READ` (para auditoría)

---

## 2. Flujo de facturación electrónica (SIFEN)

```
Empresa (+ certificado .pfx + CSC)
   └─ Timbrado electrónico ─ TimbradoDetalle (punto de expedición)
        └─ FacturaLegal (autonumera) + FacturaLegalItem
             │  POST /facturas/{id}/generar-de
             ▼
        DocumentoElectronico  ── XML SIFEN + CDC(44) + firma digital + QR   [EstadoDE.PENDIENTE]
             │  se agrupa en
             ▼
        LoteDE  ── POST /sifen/lotes/{loteId}/enviar ──►  SIFEN (SET)
             │
             │  SifenSchedulerService hace polling de estado
             │  POST /sifen/lotes/{loteId}/consultar  ·  POST /documentos/{cdc}/consultar
             ▼
        EstadoDE = APROBADO  ó  RECHAZADO
             │
             ├─ KuDE PDF:  GET /facturas/{id}/kude-pdf
             ├─ Email:     POST /facturas/{id}/reenviar-email
             └─ Eventos:
                  ├─ Cancelación:   POST /sifen/documentos/{cdc}/cancelar   → EventoCancelacionDE
                  ├─ Inutilización: POST /sifen/timbrados/{id}/inutilizar   → EventoInutilizacionDE
                  └─ Nominación:    POST /sifen/documentos/{cdc}/nominar    → EventoNominacionDE
```

Las **Notas** (crédito/débito/remisión) siguen el mismo patrón: se crean referenciando una factura,
se genera su DE (`POST /notas-*/{id}/generar-de`) y se envían a SIFEN. La Nota de Remisión suma
datos de **Vehiculo**/**Chofer** y geografía de origen/destino.

Lógica SIFEN aislada en `service/sifen/`: `SifenService`, `SifenEventoService`,
`SifenSchedulerService`. Helpers en `sifen/util/` y configuración en `sifen/config/`.

---

## 3. Patrón de capas — Backend (Spring Boot)

Para cada entidad, el flujo es:

```
model/                → Entidad JPA (hereda de model/base para auditoría)
repository/           → Spring Data JPA (+ repository/specification/ para filtros dinámicos)
dto/ + dto/mapper/    → DTOs de transporte y mappers Entity ↔ DTO
service/              → Lógica de negocio, @Transactional, validaciones
controller/           → REST controller con @RequestMapping y @PreAuthorize
```

Referencia por entidad (ejemplos reales): `Usuario` → `UsuarioRepository` → `UsuarioDto`/
`UsuarioMapper` → `UsuarioService` → `UsuarioController`. El mismo esquema aplica a `Empresa`,
`Cliente`, `Producto`, `FacturaLegal`, `Timbrado`, `DocumentoElectronico`, notas, etc.

**Componentes transversales:** `security/` (JWT + Auth0 + rate limiting), `aspect/AuditAspect`
(auditoría automática), `validation/` (RUC, CDC), `exception/GlobalExceptionHandler`, `config/`.

> ⚠️ Regla de routing: el `context-path` es `/api`, así que `@RequestMapping` NO debe llevar `/api/`.
> Hoy 3 controllers la violan (`GeografiaController`, `AuditLogController`, `ReporteController`) y
> resuelven a `/api/api/...`. Ver [TAREAS_PENDIENTES.md](TAREAS_PENDIENTES.md).

---

## 4. Patrón de capas — Frontend (Angular 17)

```
models/            → Interfaces TS (espejo de los DTOs del backend)
core/api/          → Servicios HTTP por entidad (*-api.service.ts)  [18 servicios]
core/state/        → NgRx (actions/effects/reducer/selectors)       [solo 8 ramas]
features/          → Páginas por dominio (*-list / *-form / dialogs) [13 features]
```

### ⚠️ La convención "un store NgRx por entidad" NO se cumple

Solo **8 ramas** de NgRx existen en `core/state/`:

```
auth · empresas · facturacion · documentos · usuarios · timbrados · timbrado-detalles · notas
```

El resto de las features **no tiene store** y consume directamente el API service:

| Feature      | Store NgRx | Acceso a datos |
|--------------|:----------:|----------------|
| facturacion  | ✅ | `factura-api.service` + store `facturacion` |
| documentos   | ✅ | store `documentos` |
| empresas     | ✅ | store `empresas` |
| usuarios     | ✅ | store `usuarios` |
| timbrados    | ✅ | store `timbrados` (+ `timbrado-detalles`) |
| notas        | ✅ | store `notas` (crédito/débito/remisión) |
| **clientes** | ❌ | `cliente-api.service` directo |
| **productos**| ❌ | `producto-api.service` directo |
| **transporte** (vehículos/choferes) | ❌ | `vehiculo-api.service` / `chofer-api.service` directos |
| **reportes** | ❌ | `reporte-api.service` directo |
| **dashboard**| ❌ | `dashboard-api.service` directo |
| **auditoria**| ❌ | `audit-api.service` directo |

> Al agregar una feature nueva, decidí explícitamente si necesita store; no asumas que ya existe.

**Servicios cross-cutting** en `core/services/` y `services/` (ej. `auth.service`,
`permissions.service`, `ruc-validation.service`). **Interceptors** en `core/interceptors/`
(auth, errores). **Guards** en `guards/`.

---

## 5. Flujo de datos de punta a punta

```
Lectura (GET):
  Component → (NgRx Action → Effect →)? API Service → HTTP →
  Controller → Service → Repository → DB → Entity → DTO → JSON →
  Model TS → (NgRx State →)? Component

Escritura (POST/PUT):
  Form → (NgRx Action → Effect →)? API Service → HTTP →
  Controller (@Valid, @PreAuthorize) → Service (validación + @Transactional) → Repository → DB →
  Entity → DTO → JSON → (NgRx State →)? Navegación
```

El paso por NgRx es opcional y depende de si la feature tiene store (ver tabla de la sección 4).

---

## 6. Al agregar una entidad nueva

1. **Backend**: `model` → `repository` (+ specification si hace falta filtro dinámico) →
   `dto` + `mapper` → `service` → `controller` (sin `/api/` en `@RequestMapping`, con
   `@PreAuthorize`). Cambios de esquema **solo por migración Flyway** nueva (`ddl-auto: validate`).
2. **Frontend**: `models` → `core/api` service → (opcional) `core/state` NgRx → `features`
   (`*-list.component` y `*-form.component`).
3. Respetar estándares de DB ([../frc-efact-backend/DATABASE_STANDARDS.md](../frc-efact-backend/DATABASE_STANDARDS.md))
   y la regla de routing ([../frc-efact-backend/CONTROLLER_ROUTING_RULE.md](../frc-efact-backend/CONTROLLER_ROUTING_RULE.md)).
