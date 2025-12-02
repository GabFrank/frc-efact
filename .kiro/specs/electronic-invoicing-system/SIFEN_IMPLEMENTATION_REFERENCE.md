# Referencia de Implementación SIFEN

## Fuente

**Repositorio**: https://github.com/GabFrank/franco-system-backend-filial  
**Branch**: 3.0.7-2 
**Path**: `src/main/java/com/franco/dev/service/sifen`

## Estructura de Archivos

```
sifen/
├── config/
│   ├── SifenConfiguration.java    # Configuración Spring Boot de jsifenlib
│   └── SifenProperties.java       # Properties para configuración
├── dto/
│   └── response/
│       ├── ConsultaRucResponse.java
│       └── SifenResponseBase.java
├── service/
│   ├── SifenService.java          # Servicio principal de integración
│   ├── SifenEventoService.java    # Gestión de eventos (cancelación, etc.)
│   └── SifenSchedulerService.java # Tareas programadas
└── util/
    ├── SifenEventoParser.java     # Parser de eventos desde XML
    ├── SifenReceptorHelper.java   # Helper para configurar receptores
    ├── SifenXmlParser.java        # Parser general de XML
    └── CodigosGeograficos.java    # Mapeo de departamentos/ciudades
```

## Componentes Clave

### 1. SifenConfiguration.java

**Propósito**: Configuración global de jsifenlib como bean de Spring.

**Características**:
- Usa `@ConfigurationProperties` para leer desde `application.yml`
- Inicializa `Sifen.setSifenConfig()` al arrancar la aplicación
- **IMPORTANTE**: Configuración GLOBAL para una sola empresa

```java
@Bean
public SifenConfig sifenConfig(SifenProperties properties) {
    SifenConfig config = new SifenConfig(
        TipoAmbiente.valueOf(properties.getAmbiente()),
        properties.getCscId(),
        properties.getCsc(),
        TipoCertificadoCliente.PFX,
        certPath,
        certPassword
    );
    
    Sifen.setSifenConfig(config);  // ← Configuración global
    return config;
}
```

**Adaptación Multi-Empresa**:
- ❌ NO usar bean singleton
- ✅ Crear configuración dinámica por empresa en cada operación
- ✅ Pasar empresaId a cada método de servicio

### 2. SifenService.java

**Propósito**: Servicio principal para operaciones con SIFEN.

**Métodos Principales**:

#### enviarLote(LoteDE lote)
- Reconstruye DEs desde XML original guardado
- Envía lote usando `Sifen.recepcionLoteDE()`
- Procesa respuesta y actualiza estado del lote
- Extrae protocolo para consultas posteriores

**Códigos de Respuesta**:
- `0300`: Lote recibido exitosamente (EN_PROCESO)
- Otros: Error en envío

#### consultarLote(LoteDE lote)
- Consulta estado usando `Sifen.consultaLoteDE(protocolo)`
- Procesa respuesta según código:
  - `0360`: Lote no existe (ERROR_PERMANENTE)
  - `0361`: Lote en procesamiento (EN_PROCESO)
  - `0362`: Procesamiento concluido (PROCESADO/RECHAZADO)
- Actualiza estado individual de cada DE en el lote

#### consultarDE(String cdc)
- Consulta estado individual usando `Sifen.consultaDE(cdc)`
- Actualiza estado del DE (APROBADO/RECHAZADO)
- **IMPORTANTE**: Procesa eventos asociados (cancelación)
- Si hay evento de cancelación aprobado, sobrescribe estado a CANCELADO

**Flujo de Estados**:
```
1. Actualiza estado base del DE (APROBADO/RECHAZADO)
2. Procesa eventos asociados
3. Si evento cancelación APROBADO → Estado final: CANCELADO
```

#### Métodos Auxiliares Importantes

**reconstruirDEDesdeFactura()**:
- Regenera DE desde FacturaLegal cuando no hay XML original
- Usa `SifenReceptorHelper` para configurar receptor correctamente
- Aplica fix para bug de totales IVA en jsifenlib

**procesarEventosAsociados()**:
- Extrae eventos de cancelación desde XML de respuesta
- Crea/actualiza registros de EventoCancelacionDE
- Actualiza estado del DE si hay cancelación aprobada

### 3. SifenEventoService.java

**Propósito**: Gestión de eventos del emisor (cancelación, inutilización, nominación).

#### cancelarDE(String cdc, String motivo)

**Características**:
- Maneja reintentos automáticos (marca eventos previos como inactivos)
- Previene cancelaciones duplicadas
- Genera ID de evento aleatorio
- Envía usando `Sifen.recepcionEvento()`

**Flujo**:
```
1. Validar que DE existe
2. Verificar si ya tiene cancelación aprobada
3. Marcar eventos previos como inactivos (histórico)
4. Crear TrGeVeCan con CDC y motivo
5. Crear EventosDE y enviar a SIFEN
6. Guardar evento en BD con estado PENDIENTE
7. Procesar respuesta:
   - "Aprobado" → Actualizar DE a CANCELADO
   - "Rechazado" → Mantener estado actual del DE
8. Persistir evento con estado final
```

**Estados de Evento**:
- `PENDIENTE`: Enviado, esperando procesamiento
- `APROBADO`: Cancelación exitosa
- `RECHAZADO`: Cancelación rechazada por SIFEN
- `ERROR_ENVIO`: Error al enviar

### 4. Utilidades

#### SifenReceptorHelper.java
- Determina configuración correcta del receptor según tipo de cliente
- Maneja casos: Contribuyente, No Contribuyente, Extranjero, Innominado
- Detecta entidades gubernamentales (B2G)

#### SifenEventoParser.java
- Parsea eventos de cancelación desde XML de respuesta
- Extrae: eventoId, fechaFirma, protocolo, estado, etc.

#### SifenXmlParser.java
- Parser general para extraer valores de XML SIFEN

## Librería jsifenlib

### Clases Principales

**Sifen** (clase estática):
```java
// Configuración global
Sifen.setSifenConfig(config);

// Operaciones
RespuestaRecepcionLoteDE respuesta = Sifen.recepcionLoteDE(listaDEs);
RespuestaConsultaLoteDE respuesta = Sifen.consultaLoteDE(protocolo);
RespuestaConsultaDE respuesta = Sifen.consultaDE(cdc);
RespuestaRecepcionEvento respuesta = Sifen.recepcionEvento(eventosDE);
```

**DocumentoElectronico** (bean de jsifenlib):
```java
// Crear desde XML
DocumentoElectronico de = new DocumentoElectronico(xmlOriginal);

// Obtener CDC
String cdc = de.obtenerCDC();

// Configurar campos
de.setdFecFirma(LocalDateTime.now());
de.setgOpeDE(gOpeDE);
de.setgTimb(gTimb);
// ... etc
```

**EventosDE**:
```java
EventosDE eventosDE = new EventosDE();
eventosDE.setrGesEveList(listaEventos);
```

### Tipos de Datos (Enums)

- `TipoAmbiente`: DEV, TEST, PRODUCTION
- `TipoCertificadoCliente`: PFX, JKS
- `TTiDE`: FACTURA_ELECTRONICA, NOTA_CREDITO, etc.
- `TiNatRec`: CONTRIBUYENTE, NO_CONTRIBUYENTE
- `TiAfecIVA`: GRAVADO, EXONERADO, EXENTO

## Configuración (application.yml)

```yaml
sifen:
  enabled: true
  ambiente: DEV  # DEV, TEST, PRODUCTION
  csc: "CODIGO_SEGURIDAD_CONTRIBUYENTE"
  csc-id: "ID_CSC"
  habilitar-nota-tecnica-13: false
  certificado:
    usar: true
    tipo: PFX
    archivo: "/path/to/certificado.pfx"
    contrasena: "password"
```

## Adaptaciones Necesarias para Multi-Empresa

### 1. Eliminar Configuración Global

❌ **NO HACER**:
```java
@Bean
public SifenConfig sifenConfig() {
    // Configuración global única
    Sifen.setSifenConfig(config);
}
```

✅ **HACER**:
```java
@Service
public class SifenService {
    
    public void enviarLote(LoteDE lote, Long empresaId) {
        // Cargar certificado de la empresa
        Empresa empresa = empresaRepository.findById(empresaId)...;
        
        // Crear configuración temporal
        SifenConfig config = new SifenConfig(
            ambiente,
            empresa.getCscId(),
            encryptionService.decrypt(empresa.getCscEncrypted()),
            TipoCertificadoCliente.PFX,
            empresa.getCertificadoPath(),
            encryptionService.decrypt(empresa.getCertificadoPasswordEncrypted())
        );
        
        // Usar configuración temporal
        Sifen.setSifenConfig(config);
        
        try {
            // Operación con SIFEN
            RespuestaRecepcionLoteDE respuesta = Sifen.recepcionLoteDE(deSifenLote);
        } finally {
            // Limpiar configuración
            config = null;
        }
    }
}
```

### 2. Agregar empresaId a Todos los Métodos

```java
// Antes (single-tenant)
public void enviarLote(LoteDE lote)
public void consultarDE(String cdc)
public void cancelarDE(String cdc, String motivo)

// Después (multi-tenant)
public void enviarLote(LoteDE lote, Long empresaId)
public void consultarDE(String cdc, Long empresaId)
public void cancelarDE(String cdc, String motivo, Long empresaId)
```

### 3. Modelo Empresa - Campos Adicionales

```java
@Entity
public class Empresa {
    // Certificado PFX
    private String certificadoPath;
    private String certificadoPasswordEncrypted;
    private LocalDate certificadoFechaExpiracion;
    
    // CSC (Código de Seguridad del Contribuyente)
    private String cscId;
    private String cscEncrypted;
    
    // Configuración SIFEN
    private String sifenAmbiente; // DEV, TEST, PRODUCTION
}
```

### 4. Thread Safety

**PROBLEMA**: `Sifen.setSifenConfig()` es estático y global.

**SOLUCIÓN**: Sincronizar acceso en entorno multi-threaded:

```java
private final Object sifenLock = new Object();

public void enviarLote(LoteDE lote, Long empresaId) {
    synchronized (sifenLock) {
        // Configurar para esta empresa
        configurarSifenParaEmpresa(empresaId);
        
        // Ejecutar operación
        RespuestaRecepcionLoteDE respuesta = Sifen.recepcionLoteDE(deSifenLote);
        
        // Procesar respuesta
        procesarRespuesta(respuesta);
    }
}
```

**ALTERNATIVA**: Si jsifenlib lo permite, usar instancias separadas en lugar de clase estática.

## Flujos Completos

### Flujo 1: Generar y Enviar DE

```
1. Usuario crea FacturaLegal
2. Sistema genera DocumentoElectronico:
   a. Genera XML original según especificación SIFEN
   b. Firma XML con certificado de la empresa
   c. Genera CDC (44 caracteres)
   d. Genera URL QR
   e. Estado inicial: PENDIENTE
3. Usuario crea LoteDE y agrega DEs
4. Sistema envía lote:
   a. Reconstruye DEs desde XML guardado
   b. Llama Sifen.recepcionLoteDE()
   c. Extrae protocolo de respuesta
   d. Estado lote: EN_PROCESO
5. Scheduler consulta estado periódicamente:
   a. Llama Sifen.consultaLoteDE(protocolo)
   b. Si código 0362 (concluido):
      - Extrae detalles individuales
      - Actualiza cada DE (APROBADO/RECHAZADO)
      - Estado lote: PROCESADO
```

### Flujo 2: Cancelar DE

```
1. Usuario solicita cancelación de DE aprobado
2. Sistema valida:
   a. DE existe y está APROBADO
   b. No tiene cancelación aprobada previa
   c. Está dentro del plazo (48h para facturas)
3. Sistema crea evento:
   a. Genera ID de evento aleatorio
   b. Crea TrGeVeCan con CDC y motivo
   c. Envía Sifen.recepcionEvento()
4. Sistema procesa respuesta:
   a. Si "Aprobado": DE → CANCELADO
   b. Si "Rechazado": DE mantiene estado
   c. Guarda evento con estado final
5. Scheduler consulta DEs periódicamente:
   a. Llama Sifen.consultaDE(cdc)
   b. Procesa eventos asociados
   c. Actualiza estado si hay cambios
```

## Códigos de Respuesta SIFEN

### Recepción de Lote
- `0300`: Lote recibido exitosamente
- `0301`: Error en estructura del lote
- `0302`: Error en firma digital

### Consulta de Lote
- `0360`: Lote no existe
- `0361`: Lote en procesamiento
- `0362`: Procesamiento concluido

### Consulta de DE
- `0422`: DE encontrado
- `0423`: DE no encontrado

### Estados de Resultado
- `Aprobado`: Documento/evento aprobado
- `Aprobado con observación`: Aprobado pero con advertencias
- `Rechazado`: Documento/evento rechazado

## Bugs Conocidos en jsifenlib

### Bug de Totales IVA

**Problema**: Los campos `dLiqTotIVA10` y `dLiqTotIVA5` no se calculan correctamente.

**Solución**: Aplicar fix usando reflexión:

```java
private void aplicarFixTotalesIVA(TgTotSub totales) {
    try {
        if (totales.getdIVA10() != null && totales.getdIVA10().compareTo(BigDecimal.ZERO) > 0) {
            Field field = TgTotSub.class.getDeclaredField("dLiqTotIVA10");
            field.setAccessible(true);
            field.set(totales, totales.getdIVA10());
        }
        // Similar para IVA 5%
    } catch (Exception e) {
        log.error("Error al aplicar fix de totales IVA", e);
    }
}
```

## Mejores Prácticas

### 1. Guardar XML Original
- Siempre guardar `xmlOriginal` en DocumentoElectronico
- Permite reconstruir DE sin regenerar desde factura
- Evita inconsistencias en CDC

### 2. Manejo de Eventos
- Marcar eventos previos como inactivos (histórico)
- Permitir reintentos automáticos
- Mantener auditoría completa

### 3. Parsing de XML
- No confiar solo en objetos parseados de jsifenlib
- Extraer valores críticos directamente del XML
- Usar métodos auxiliares para parsing robusto

### 4. Logging Detallado
- Loguear cada paso del proceso
- Incluir CDC, protocolo, códigos de respuesta
- Usar emojis para facilitar lectura (✅ ❌ ⏳ 🔍)

### 5. Estados Transitorios
- Manejar estados intermedios (PENDIENTE, EN_PROCESO)
- Implementar schedulers para consultas periódicas
- Actualizar estados basándose en respuestas de SIFEN

## Dependencias Maven

```xml
<repositories>
    <repository>
        <id>github-jsifenlib</id>
        <url>https://maven.pkg.github.com/GabFrank/jsifenlib</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.roshka</groupId>
        <artifactId>jsifenlib</artifactId>
        <version>0.2.4-frc.13</version>
    </dependency>
</dependencies>
```

**Autenticación GitHub Packages**:
```xml
<servers>
    <server>
        <id>github-jsifenlib</id>
        <username>GITHUB_USERNAME</username>
        <password>GITHUB_TOKEN</password>
    </server>
</servers>
```

## Conclusión

La implementación de referencia proporciona una base sólida para integración con SIFEN. Las adaptaciones principales para multi-empresa son:

1. **Eliminar configuración global** → Configuración dinámica por empresa
2. **Agregar empresaId** a todos los métodos de servicio
3. **Sincronizar acceso** a `Sifen.setSifenConfig()` para thread-safety
4. **Almacenar CSC y certificado** por empresa en BD (encriptados)
5. **Cargar certificado dinámicamente** según la empresa que opera

El código de referencia maneja correctamente:
- ✅ Generación de DEs desde facturas
- ✅ Envío y consulta de lotes
- ✅ Consulta individual de DEs
- ✅ Eventos de cancelación con reintentos
- ✅ Parsing robusto de respuestas XML
- ✅ Manejo de estados transitorios
- ✅ Workarounds para bugs de jsifenlib

Estos patrones deben ser adaptados manteniendo la lógica de negocio pero agregando la capa de multi-tenancy.
