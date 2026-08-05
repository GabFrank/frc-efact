# Índice de entidades JPA (backend `com.frcefact.model`)

30 entidades `@Entity` + 6 enums. Todas heredan de `AuditableEntity` (`base/`) con `id BIGSERIAL`, `creadoEn/creadoPor/actualizadoEn/actualizadoPor`. Verificado 2026-08-05.

## RBAC / Personas / Multi-empresa (esquema `persona`)
| Entidad | Notas |
|---|---|
| `Usuario` | Login local + Auth0. Campos Auth0 (V27), `imagenPerfil` (V30) |
| `Rol` | ADMIN, EMPRESA_ADMIN, FACTURADOR, LECTOR |
| `UsuarioRol` | Rol global del usuario (tabla `persona.usuario_rol`) |
| `UsuarioEmpresa` | Vínculo usuario↔empresa con `rolEmpresa` (string: ADMINISTRADOR/FACTURADOR/LECTOR) |

## Empresa / Timbrado (esquema `empresa`)
| Entidad | Notas |
|---|---|
| `Empresa` | Datos fiscales, certificado `.pfx` por empresa, actividades económicas. ⚠️ Conserva columnas CSC residuales (`csc_id`, `csc_encrypted`) pese a V14 — el CSC real vive en `Timbrado`. ⚠️ **No** tiene campo branding/logo (el logo del KuDE es un TODO que retorna `null`) |
| `Timbrado` | Físico o electrónico (con CSC). Rangos opcionales para electrónicos (V17) |
| `TimbradoDetalle` | Puntos de expedición (establecimiento + punto). Geografía (V15) |

## Clientes / Productos
| Entidad | Notas |
|---|---|
| `Cliente` | (esquema `clientes`) PF/PJ/EG, campos SIFEN (V20), scoped por empresa |
| `Producto` | (esquema `productos`) `tipoTransaccion` (V18/V19), `unidadMedida`, IVA 0/5/10 |

## Facturación (esquema `financiero`)
| Entidad | Notas |
|---|---|
| `FacturaLegal` | Autonumera por timbrado. Moneda extranjera + tipo de cambio (V25/V26), totales en guaraníes |
| `FacturaLegalItem` | Ítems de la factura |

## Documento Electrónico / Lote
| Entidad | Notas |
|---|---|
| `DocumentoElectronico` | XML SIFEN, CDC 44 chars, firma, QR. Estado varchar (V22) |
| `LoteDE` | Agrupa DEs para envío a SIFEN |

## Eventos SIFEN (V23)
| Entidad | Notas |
|---|---|
| `EventoCancelacionDE` | Cancelación por CDC + motivo |
| `EventoInutilizacionDE` | Inutilización de rango de números (⚠️ parcial) |
| `EventoNominacionDE` | Nominación (asignar receptor a DE innominado) |

## Notas (V28/V29)
| Entidad | Notas |
|---|---|
| `NotaCredito` / `NotaCreditoItem` | Heredan moneda/items de la factura referenciada, numeración propia |
| `NotaDebito` / `NotaDebitoItem` | |
| `NotaRemision` / `NotaRemisionItem` | Geografía origen/destino (V31/V32), transportista (V33), fecha estimada (V34) |

## Transporte (esquema `transporte`, V35)
| Entidad | Notas |
|---|---|
| `Vehiculo` | Para nota de remisión |
| `Chofer` | Conductor / transportista |

## Geografía (esquema `geografia`, V9) — precargada
`Pais`, `Departamento`, `Ciudad`, `Distrito`, `Barrio`

## Auditoría (esquema `auditoria`)
`AuditLog` — JSONB antes/después, poblado por `AuditAspect` (AOP)

## Enums (no entidades)
`AccionEnum`, `EstadoDE`, `EstadoEvento`, `EstadoLoteDE`, `TipoClienteSifen`, `TipoTransaccionProducto` → ver [enums-index.md](enums-index.md)
