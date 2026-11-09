# API de integración de FRC eFact

API para que **otros sistemas** usen FRC eFact como servicio de facturación electrónica.
Versión actual: **v1**. Base URL de producción: `https://efact.frc-ecommerce.com/api/v1/integracion`.

> Contrato estable: dentro de `v1` solo se **agregan** campos y endpoints. Un cambio
> incompatible va a `/v2`. Los sistemas cliente deben ignorar los campos que no conozcan.

## Modos de cliente

| Modo | Qué hace | Estado |
|---|---|---|
| **Consultor** | Consulta en SIFEN cualquier DE por su CDC, de cualquier emisor. No necesita tener nada en eFact. | ✅ v1 (`de:consultar`) |
| **Pasarela** | El cliente manda los datos, eFact firma y envía a SIFEN sin guardar la transacción. | Pendiente |
| **Gestión completa** | La empresa del cliente factura en eFact y opera sus documentos por API. | Pendiente |

Cada modo nuevo se habilita como un **scope** más de la key, sin cambiar la autenticación.

---

## Autenticación

Header `X-API-Key` en cada request:

```
X-API-Key: frc_45qhuaewvo9m_Zk3...  (60 caracteres)
```

- La key pertenece a una **cuenta de servicio** (el cliente). Una cuenta puede tener varias
  keys (por ejemplo una por ambiente o por sistema), cada una con sus scopes y su vencimiento.
- **La key se muestra una sola vez**, al crearla. eFact guarda solo su hash SHA-256: si se
  pierde, se crea otra y se revoca la anterior.
- Rotación sin corte: crear la key nueva, desplegarla en el cliente, revocar la vieja.
- Desactivar la cuenta bloquea todas sus keys en el acto.
- La key **solo** sirve en `/v1/integracion/**`. No da acceso a la API que usa el frontend.
- Límite por key: **60 solicitudes por minuto** (configurable). Superado → `429` con `Retry-After: 60`.
- Usar siempre HTTPS. La key es una credencial: no va en URLs, logs ni repositorios.

### Scopes

| Scope | Permite |
|---|---|
| `de:consultar` | `GET /documentos/{cdc}`, `/documentos/{cdc}/xml`, `/documentos/{cdc}/kude` |

---

## Endpoints

### `GET /documentos/{cdc}` — Consultar un DE

Consulta el DE **en SIFEN en ese momento** y devuelve sus datos en JSON. eFact no guarda nada del
documento consultado.

| Parámetro | Dónde | Descripción |
|---|---|---|
| `cdc` | path | 44 dígitos. Se valida el dígito verificador antes de consultar SIFEN. |
| `incluir` | query, opcional | Partes extra separadas por coma: `items` (detalle de ítems), `xml` (rDE firmado como texto). |

```bash
curl -H "X-API-Key: $FRC_API_KEY" \
  "https://efact.frc-ecommerce.com/api/v1/integracion/documentos/01800994825001002000003822025120417284341967?incluir=items"
```

```json
{
  "cdc": "01800994825001002000003822025120417284341967",
  "estado": "APROBADO",
  "respuestaSifen": { "codigo": "0422", "mensaje": "CDC encontrado", "fechaProceso": "2026-11-09T10:00:00", "protocoloAutorizacion": "..." },
  "tipoDocumento": 1,
  "tipoDocumentoDescripcion": "Factura electrónica",
  "timbrado": { "numero": "18270044", "fechaInicioVigencia": "2025-08-29", "establecimiento": "001", "puntoExpedicion": "002", "numeroDocumento": "0000038", "numeroCompleto": "001-002-0000038" },
  "fechaEmision": "2025-12-04T18:20:33",
  "emisor": { "ruc": "80099482", "dv": "5", "razonSocial": "FRANCO AREVALOS S.A.", "actividadesEconomicas": ["46304 - COMERCIO AL POR MAYOR DE BEBIDAS"] },
  "receptor": { "contribuyente": true, "ruc": "4043581", "dv": "4", "nombre": "..." },
  "operacion": { "moneda": "PYG", "condicion": 1, "condicionDescripcion": "Contado" },
  "totales": { "subtotalExento": 0, "subtotal5": 0, "subtotal10": 96000, "totalGeneral": 96000, "iva10": 8727, "totalIva": 8727 },
  "urlQr": "https://ekuatia.set.gov.py/consultas/qr?...",
  "items": [
    { "codigo": "001", "descripcion": "...", "unidadMedida": "77", "cantidad": 12, "precioUnitario": 8000.00, "totalOperacion": 96000, "afectacionIva": 1, "tasaIva": 10 }
  ]
}
```

- `estado`: `APROBADO`, o `CANCELADO` si SIFEN informa un evento de cancelación. Los eventos
  vienen en `eventos[]` (`CANCELACION`, `CONFORMIDAD`, `DISCONFORMIDAD`, ...).
- Todos los valores salen **del XML aprobado por la SET**, sin recalcular. Un campo que el XML
  no trae se **omite** en el JSON: nunca se completa con un valor por defecto.
- Importes en la moneda de la operación (`operacion.moneda`). En moneda extranjera,
  `totales.totalGuaranies` trae el total en Gs. que informó el emisor.

### `GET /documentos/{cdc}/xml` — XML firmado

Devuelve el `rDE` (DE firmado + QR) tal como lo tiene SIFEN. `Content-Type: application/xml`.

### `GET /documentos/{cdc}/kude` — KuDE PDF

Genera el KuDE a partir del XML de SIFEN. `Content-Type: application/pdf`.
Disponible para **factura electrónica (1)** y **nota de crédito (5)**. Otros tipos → `501 KUDE_NO_DISPONIBLE`.

---

## Errores

Formato único:

```json
{ "error": { "codigo": "DE_NO_ENCONTRADO", "mensaje": "SIFEN no devolvió el documento: ...", "codigoSifen": "0420" } }
```

Decidir por `codigo`, no por `mensaje` (el texto puede cambiar).

| HTTP | `codigo` | Significado | ¿Reintentar? |
|---|---|---|---|
| 400 | `CDC_INVALIDO` | Largo o dígito verificador incorrecto | No |
| 400 | `PARAMETRO_INVALIDO` | Valor de `incluir` desconocido | No |
| 401 | `API_KEY_FALTANTE` / `API_KEY_INVALIDA` | Sin key, o key inválida, vencida o revocada | No |
| 403 | `SCOPE_INSUFICIENTE` | La key no tiene el scope de la operación | No |
| 404 | `DE_NO_ENCONTRADO` | SIFEN no tiene ese CDC (ver `codigoSifen`) | No |
| 429 | `LIMITE_EXCEDIDO` | Límite por minuto superado | Sí, tras `Retry-After` |
| 501 | `KUDE_NO_DISPONIBLE` | Tipo de DE sin plantilla KuDE | No |
| 502 | `SIFEN_NO_DISPONIBLE` / `SIFEN_SIN_RESPUESTA` / `SIFEN_RESPUESTA_INVALIDA` | SIFEN caído o respuesta ilegible | Sí, con espera |
| 503 | `SERVICIO_NO_CONFIGURADO` | La cuenta no tiene certificado disponible para consultar | No — avisar a FRC |
| 500 | `ERROR_INTERNO` | Error inesperado | Sí, con espera |

---

## Operación (FRC)

### Alta de un cliente

Con un usuario `ADMIN` (JWT en `Authorization: Bearer`), vía Swagger o curl:

```bash
# 1. Cuenta de servicio (empresaId es opcional: un consultor no tiene)
curl -X POST "$API/admin/integracion/cuentas" -H "Authorization: Bearer $JWT" \
  -H 'Content-Type: application/json' \
  -d '{"nombre":"ERP Cliente S.A.","ruc":"80000000-1","emailContacto":"ti@cliente.com.py"}'

# 2. Key (la respuesta trae "clave": es la única vez que se ve)
curl -X POST "$API/admin/integracion/cuentas/{id}/keys" -H "Authorization: Bearer $JWT" \
  -H 'Content-Type: application/json' \
  -d '{"nombre":"producción","scopes":["de:consultar"],"expiraEn":"2027-12-31T23:59:59"}'

# Revocar (efecto inmediato)
curl -X POST "$API/admin/integracion/keys/{keyId}/revocar" -H "Authorization: Bearer $JWT"
```

Entregar la clave al cliente por un canal seguro, nunca por el chat ni por email en claro.

### Certificado con el que se consulta SIFEN

SIFEN exige certificado digital (mTLS) para consultar. eFact usa, en este orden:

1. el certificado de la **empresa de la cuenta**, si tiene empresa y la empresa tiene certificado;
2. el de la **empresa de plataforma** (`INTEGRACION_EMPRESA_PLATAFORMA_ID`);
3. si no hay ninguno → `503 SERVICIO_NO_CONFIGURADO`.

El **ambiente** (test/producción) es el `sifen_ambiente` de esa empresa. Un CDC de producción no
existe en el ambiente de test.

### Rastro de uso

Cada llamada autenticada queda en `integracion.solicitud_api` (key, operación, CDC, status HTTP,
código SIFEN, duración, IP). Base para auditoría y para medir/facturar el servicio.

### Pendientes conocidos

- **Validar contra SIFEN real** qué devuelve `siConsDE` cuando quien consulta no es ni emisor ni
  receptor del DE: si SIFEN no entrega el `xContenDE` a terceros, el modo consultor queda limitado a
  los DE donde la cuenta (o la plataforma) es parte. Sin esa prueba no hay que prometerle a un
  cliente consultas de DE ajenos.
- Las consultas a SIFEN de toda la app pasan por un único lock global de jsifenlib
  (`SifenConfigFactory.executeWithConfig`): con mucho tráfico de consultas, compiten con la emisión.
- El límite por minuto vive en memoria: se reinicia con la app y no se comparte entre réplicas
  (hoy hay una sola).
- No hay pantalla en el frontend para administrar cuentas y keys; se hace por API/Swagger.
