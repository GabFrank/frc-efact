# Dominio: Clientes y Productos

Entidades de soporte de la facturación. Ambas **scoped por empresa** (FK `empresa_id` obligatoria) y **sin store NgRx** (usan el API service directo — ver [../reference/ngrx-state-index.md](../reference/ngrx-state-index.md)).

---

## Cliente — esquema `clientes.cliente`

Entidad `com.frcefact.model.Cliente` (`AuditableEntity`). Representa el **Receptor** del DE (bloque B400 del MT SIFEN v1.50). Campos SIFEN agregados en migración **V20**.

### Campos clave
| Campo | Columna | Notas |
|---|---|---|
| `empresa` | `empresa_id` (FK, `NOT NULL`) | scope multi-empresa |
| `nombre` | `nombre` (200) | dNomRec (B406) |
| `razonSocial` | `razon_social` (200) | opcional |
| `ruc` | `ruc` (20) | formato `99999999-9` (incluye DV). dRucRec (B404) |
| `direccion` / `numeroCasa` / `telefono` / `celular` / `email` | | B407/B411/B412/B413/B414 |
| `pais` / `ciudad` | `pais_id` / `ciudad_id` (FK) | códigos geográficos B408-B415 |
| `tipoClienteSifen` | `tipo_cliente_sifen` (enum STRING) | **fuente de verdad** del receptor |
| `tributa` (`Boolean`) | `tributa` | ⚠️ `@deprecated` — usar `tipoClienteSifen` |
| `tipoContribuyente` (`String` PF/PJ/EG) | `tipo_contribuyente` | ⚠️ `@deprecated` — se autopobla desde el setter de `tipoClienteSifen` |
| `activo` | `activo` | baja lógica (reactivable) |

### TipoClienteSifen (naturaleza del receptor)
Enum en `model/TipoClienteSifen.java`. Constructor `(naturalezaReceptor, tipoContribuyente, tipoOperacion, descripcion, esContribuyente)`; `requiereRuc()` devuelve el mismo `esContribuyente`.

| Constante | iNatRec | iTiContRec | iTiOpe | requiereRuc |
|---|---|---|---|---|
| `PERSONA_FISICA` (PF) | 1 | 1 | 1 | sí |
| `PERSONA_JURIDICA` (PJ) | 1 | 2 | 1 | sí |
| `NO_CONTRIBUYENTE` (Consumidor Final) | 2 | null | 2 | no |
| `EXTRANJERO` | 2 | null | 4 | no |
| `GUBERNAMENTAL` (EG) | 1 | 2 | 3 | sí |

Los getters (`getNaturalezaReceptor`, `getTipoContribuyente`, `getTipoOperacion`, `requiereRuc`) los usa `SifenReceptorHelper` — pero ⚠️ ese helper **no se invoca desde `SifenService`** (lógica duplicada a mano, SIFEN-2). Ver [../reference/known-bugs.md](../reference/known-bugs.md).

### Endpoints — `ClienteController` `/clientes`, todo scoped `/empresa/{empresaId}`
`POST /empresa/{empresaId}` `[A,EA,F]` · `PUT .../{clienteId}` `[A,EA,F]` · `GET .../{clienteId}` · `GET /empresa/{empresaId}` · `.../paginado` · `.../buscar` · `.../buscar/paginado` · `.../filtrar` · `.../ruc/{ruc}` · `.../ruc/{ruc}/existe` · `.../count` `[todos]` · `DELETE .../{clienteId}` `[A,EA]` · `PATCH .../{clienteId}/reactivar` `[A,EA]`.

### ⚠️ RUC — validación frontend rota (SEC-4)
El backend (`CalcularVerificadorRuc`, módulo-11) valida bien el DV. Pero en el **frontend**: `ruc-validation.service.ts` retorna siempre `valid:true` y `mock-ruc.interceptor.ts` (registrado en `app.config.ts` **sin guarda de entorno**) intercepta `/api/empresas/validate-ruc` sirviendo RUCs ficticios hardcodeados, **también en prod**. No confiar en la validación de RUC del front. Detalle en [../reference/known-bugs.md](../reference/known-bugs.md) y [../../../../docs/ISSUES_CANDIDATOS.md](../../../../docs/ISSUES_CANDIDATOS.md) (SEC-4).

### Frontend
`features/clientes/`: `clientes-list.component.ts`, `cliente-form.component.ts`, `clientes.routes.ts`. API en `core/api/cliente-api.service.ts`. Sin NgRx.

---

## Producto — esquema `productos.producto`

Entidad `com.frcefact.model.Producto` (`AuditableEntity`). Scoped por `empresa_id`.

### Campos clave
| Campo | Columna | Notas |
|---|---|---|
| `empresa` | `empresa_id` (FK, `NOT NULL`) | scope |
| `codigo` | `codigo` (50) | código interno |
| `descripcion` | `descripcion` (500, `NOT NULL`) | |
| `precio` | `precio` (`NUMERIC(15,2)`) | |
| `iva` | `iva` (`Integer`, `NOT NULL`) | **0 / 5 / 10** (tasa IVA SIFEN) |
| `tipoTransaccion` | `tipo_transaccion` (enum STRING, `NOT NULL`) | default `VENTA_MERCADERIA` (V18/V19) |
| `unidadMedida` | `unidad_medida` (10, `NOT NULL`) | default `"UNI"` |
| `balanza` | `balanza` | producto pesable |
| `activo` | `activo` | baja lógica |

### TipoTransaccionProducto (SIFEN D011 · iTipTra)
Enum `model/TipoTransaccionProducto.java`, constructor `(codigo, descripcion)`. Códigos 1-13: `VENTA_MERCADERIA(1)`, `PRESTACION_SERVICIOS(2)`, `MIXTO(3)`, `VENTA_ACTIVO_FIJO(4)`, `VENTA_DIVISAS(5)`, `COMPRA_DIVISAS(6)`, `PROMOCION_MUESTRAS(7)`, `DONACION(8)`, `ANTICIPO(9)`, `COMPRA_PRODUCTOS(10)`, `COMPRA_SERVICIOS(11)`, `VENTA_CREDITO_FISCAL(12)`, `MUESTRAS_MEDICAS(13)`. Tabla completa en [../reference/enums-index.md](../reference/enums-index.md).
> ⚠️ `SifenService` fija `iTipTra(VENTA_MERCADERIA)` por defecto al armar el XML (no lee `producto.tipoTransaccion`).

### Endpoints — `ProductoController` `/productos` (no scoped por path; filtra por empresa vía contexto/DTO)
`POST` `[A,EA,F]` · `PUT /{id}` `[A,EA,F]` · `GET /{id}` · `GET` · `GET /buscar` `[todos]` · `DELETE /{id}` `[A,EA,F]` · `POST /importar` (multipart Excel) `[A,EA,F]` · `GET /verificar-codigo` · `GET /verificar-descripcion` `[A,EA,F]`.

### Frontend
`features/productos/`: `productos-list.component.ts`, `producto-form.component.ts`, `productos.routes.ts`. API en `core/api/producto-api.service.ts`. Sin NgRx.

---

Índices generales: [../reference/entities-index.md](../reference/entities-index.md) · [../reference/endpoints-index.md](../reference/endpoints-index.md). Consumidos por facturación → ver [facturacion-de.md](facturacion-de.md) si existe.
