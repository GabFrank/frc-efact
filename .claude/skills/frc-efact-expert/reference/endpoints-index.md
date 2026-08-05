# Índice de endpoints REST (backend)

Verificado 2026-08-05. **context-path = `/api`** → toda ruta cuelga de `/api`. Leyenda de roles: **[todos]** = ADMIN, EMPRESA_ADMIN, FACTURADOR, LECTOR · **[A,EA,F]** = ADMIN, EMPRESA_ADMIN, FACTURADOR · **[A,EA]** = ADMIN, EMPRESA_ADMIN · **[A]** = ADMIN.

> ⚠️ **Bug vigente:** `GeografiaController`, `AuditLogController`, `ReporteController` declaran `@RequestMapping("/api/...")` → resuelven a `/api/api/...` (doble prefijo). Ver [known-bugs.md](known-bugs.md).

## AuthController `/auth` — públicos
`POST /login` · `POST /refresh` · `POST /logout`
> `AuthResponse` real: `{ token, refreshToken, type, usuario }` (no existe `expiresIn` ni `user`).

## UsuarioController `/usuarios`
`GET /perfil` · `GET /asignables` [todos] · `GET` [todos] · `GET /buscar` [todos] · `GET /{id}` [todos] · `POST` [A,EA] · `PUT /{id}` [A] · `DELETE /{id}` [A] · `POST /reset-password` [A] · `POST /{id}/activar|desactivar|desbloquear` [A] · `GET /{id}/roles` [todos] · `GET /roles` [todos] · `GET /estadisticas` [A] · `GET /search` [A,EA] · `GET /check-username|check-email` [A]
> SecurityConfig protege `/usuarios/admin/**` pero ese endpoint **no existe**.

## UserProfileController `/perfil`
`POST /vincular-auth0` · `POST /desvincular-auth0` · `PUT /actualizar` · `POST /cambiar-password` · `GET /actividad` · `POST /actualizar-desde-auth0`

## RolController `/roles`
`GET` [A]

## EmpresaController `/empresas`
`POST` (multipart/json) [A,EA] · `PUT /{id}` (multipart/json) [A,EA] · `GET /{id}` · `GET` [todos] · `GET /mis-empresas` · `DELETE /{id}` [A,EA] · `PUT /{id}/certificado/password` [A,EA] · `GET /{id}/usuarios` [todos] · `POST /{id}/usuarios` [A,EA] · `DELETE /{empresaId}/usuarios/{usuarioId}` [A,EA] · `GET /buscar` · `POST /{id}/certificado` (multipart) [A,EA] · `GET /diagnostico` [A]

## TimbradoController `/timbrados`
`POST` [A,EA] · `PUT /{id}` [A,EA] · `GET /{id}` [todos] · `GET /empresa/{empresaId}` [todos] · `GET /empresa/{empresaId}/activos` [todos] · `GET /{id}/vigente` [todos] · `GET /empresa/{empresaId}/vigentes` [todos] · `GET /empresa/{empresaId}/electronicos-vigentes` [todos] · `GET /empresa/{empresaId}/por-vencer` [todos] · `DELETE /{id}` [A,EA]

## TimbradoDetalleController (base sin path)
`POST /timbrados/{timbradoId}/detalles` [A,EA] · `GET /timbrados/{timbradoId}/detalles` [todos] · `GET /timbrados/{timbradoId}/detalles/activos` [todos] · `GET /timbrado-detalles/{id}` [todos] · `PUT /timbrado-detalles/{id}` [A,EA] · `DELETE /timbrado-detalles/{id}` [A,EA] · `GET /timbrados/{timbradoId}/detalles/por-agotarse` [todos] · `GET /timbrado-detalles/empresa/{empresaId}` [todos]

## ClienteController `/clientes` (scoped `/empresa/{empresaId}`)
`POST /empresa/{empresaId}` [A,EA,F] · `PUT .../{clienteId}` [A,EA,F] · `GET .../{clienteId}` [todos] · `GET /empresa/{empresaId}` [todos] · `.../paginado` · `.../buscar` · `.../buscar/paginado` · `.../filtrar` · `.../ruc/{ruc}` · `.../ruc/{ruc}/existe` [todos] · `DELETE .../{clienteId}` [A,EA] · `PATCH .../{clienteId}/reactivar` [A,EA] · `GET .../count` [todos]

## ProductoController `/productos`
`POST` [A,EA,F] · `PUT /{id}` [A,EA,F] · `GET /{id}` [todos] · `GET` [todos] · `GET /buscar` [todos] · `DELETE /{id}` [A,EA,F] · `POST /importar` (multipart) [A,EA,F] · `GET /verificar-codigo` [A,EA,F] · `GET /verificar-descripcion` [A,EA,F]

## FacturaLegalController `/facturas`
`POST` [A,EA,F] · `GET /{id}` [todos] · `PUT /{id}` [A,EA,F] · `GET` [todos] · `GET /resumen` [todos] · `POST /{id}/items` [A,EA,F] · `DELETE /{facturaId}/items/{itemId}` [A,EA,F] · `PUT /{id}/descuento` [A,EA,F] · `POST /{id}/recalcular` [A,EA,F] · `DELETE /{id}` [A,EA] · `GET /estadisticas` [todos] · `POST /{id}/generar-de` [A,EA,F] · `POST /{id}/desvincular-de` [A,EA,F] · `GET /{id}/kude-pdf` [todos] · `POST /{id}/reenviar-email` [A,EA,F]

## DocumentoElectronicoController `/documentos-electronicos`
`GET` [todos] · `GET /{id}` [todos] · `GET /cdc/{cdc}` [todos] · `GET /{id}/xml` [todos] · `POST /{id}/consultar` [A,EA,F]

## NotaCreditoController `/notas-credito`
`POST` [A,EA,F] · `GET /{id}` [todos] · `GET` [todos] · `DELETE /{id}` [A,EA] · `POST /{id}/generar-de` [A,EA,F] · `GET /{id}/kude-pdf` [todos]

## NotaDebitoController `/notas-debito`
`POST` [A,EA,F] · `GET /{id}` [todos] · `GET` [todos] · `DELETE /{id}` [A,EA]

## NotaRemisionController `/notas-remision`
`POST` [A,EA,F] · `GET /{id}` [todos] · `GET` [todos] · `DELETE /{id}` [A,EA] · `POST /{id}/generar-de` [A,EA,F] · `POST /{id}/generar-y-enviar` [A,EA,F] · `POST /{id}/vincular-lote` [A,EA,F] · `GET /{id}/kude-pdf` [todos] · `POST /{id}/enviar-email` [A,EA,F]

## SifenController `/sifen`
`POST /lotes/{loteId}/enviar` · `POST /lotes/{loteId}/consultar` · `POST /documentos/{cdc}/consultar` · `POST /documentos/{cdc}/cancelar` · `GET /documentos/factura/{facturaId}` · `GET /documentos/nota-credito/{id}` · `GET /documentos/nota-remision/{id}` · `POST /documentos/{deId}/reenviar` · `POST /documentos/{cdc}/nominar` · `POST /timbrados/{timbradoId}/inutilizar` — todos [A,EA,F] · `GET /eventos/cancelacion|nominacion|inutilizacion` [todos]

## DashboardController `/dashboard`
`GET /usuario/{usuarioId}` · `GET /empresa/{empresaId}` · `GET /general` — [todos]

## VehiculoController `/vehiculos` (scoped `/empresa/{empresaId}`)
`POST` [A,EA,F] · `PUT .../{vehiculoId}` [A,EA,F] · `GET .../{vehiculoId}` [todos] · `GET /empresa/{empresaId}` [todos] · `.../paginado` · `.../buscar` · `.../filtrar` [todos] · `DELETE .../{vehiculoId}` [A,EA] · `PATCH .../{vehiculoId}/reactivar` [A,EA] · `GET .../count` [todos]

## ChoferController `/choferes` (mismo patrón y roles que Vehiculo)

## ⚠️ GeografiaController `/api/geografia` → `/api/api/geografia`
`GET /departamentos`, `/departamentos/buscar`, `/departamentos/{codigo}`, `/departamentos/{depCodigo}/distritos`, `/distritos/buscar`, `/distritos/{codigo}`, `/distritos/{distritoCodigo}/ciudades`, `/ciudades/buscar`, `/ciudades/{codigo}`, `/ciudades/id/{id}`, `/ciudades/{ciudadCodigo}/barrios`, `/barrios/buscar`, `/barrios/{codigo}` — [todos]

## ⚠️ AuditLogController `/api/auditoria` → `/api/api/auditoria`
`GET` [A,EA] · `GET /entidad/{tipo}/{id}` [todos] · `GET /ultimas-actividades` [A,EA] · `GET /usuario/{usuarioId}` [A,EA] · `GET /estadisticas` [A,EA] · `GET /count` [A] · `GET /count/empresa/{empresaId}` [A,EA]

## ⚠️ ReporteController `/api/reportes` → `/api/api/reportes` (sin `@PreAuthorize` de método)
`GET /facturas|clientes|productos|usuarios` (+ `/excel` y `/pdf` de cada uno)
