# Diseño Actualizado: Integración SIFEN Multi-Empresa

## Basado en Implementación de Referencia

Este documento actualiza el diseño de integración con SIFEN basándose en la implementación funcional del repositorio:
https://github.com/GabFrank/franco-system-backend-filial (branch: facturacion-electronica)

## Arquitectura de Servicios

### 1. SifenService - Servicio Principal

**Responsabilidades**:
- Envío de lotes de DEs a SIFEN
- Consulta de estado de lotes
- Consulta de estado de DEs individuales
- Procesamiento de respuestas XML
- Actualización de estados en BD

**Adaptación Multi-Empresa**:

```java
@Service
@Transactional
public class SifenService {
    
    private final DocumentoElectronicoRepository documentoElectronicoRepository;
    private final LoteDERepository loteDERepository;
    private final EmpresaRepository empresaRepository;
    private final EncryptionService encryptionService;
    private final EventoCancelacionDEService eventoCancelacionDEService;
    
    @Value("${sifen.ambiente}") // DEV, TEST, PRODUCTION
    private String ambiente;
    
    // Lock para sincronizar acceso a configuración global de jsifenlib
    private final Object sifenLock = new Object();
    
    /**
     * Envía un lote de DEs a SIFEN.
     * IMPORTANTE: Configura jsifenlib dinámicamente con certificado de la empresa.
     */
    @Transactional
    public void enviarLote(LoteDE lote, Long empresaId) throws SifenException {
        log.info("📤 Enviando lote ID: {} para empresa ID: {}", lote.getId(), empresaId);
        
        synchronized (sifenLock) {
            // 1. Configurar jsifenlib para esta empresa
            configurarSifenParaEmpresa(empresaId);
            
            try {
                // 2. Obtener documentos del lote
                List<DocumentoElectronico> documentos = 
                    documentoElectronicoRepository.findByLoteDe(lote);
                
                // 3. Reconstruir DEs de SIFEN desde XML original
                List<com.roshka.sifen.core.beans.DocumentoElectronico> deSifenLote = 
                    new ArrayList<>();
                
                for (DocumentoElectronico de : documentos) {
                    com.roshka.sifen.core.beans.DocumentoElectronico deSifen;
                    
                    if (de.getXmlOriginal() != null && !de.getXmlOriginal().isEmpty()) {
                        // Reconstruir desde XML guardado (método preferido)
                        deSifen = new com.roshka.sifen.core.beans.DocumentoElectronico(
                            de.getXmlOriginal()
                        );
                    } else {
                        // Fallback: regenerar desde factura
                        deSifen = regenerarDEDesdeFactura(de);
                    }
                    
                    deSifenLote.add(deSifen);
                }
                
                // 4. Enviar lote a SIFEN usando jsifenlib
                RespuestaRecepcionLoteDE respuesta = Sifen.recepcionLoteDE(deSifenLote);
                
                // 5. Actualizar lote con respuesta
                lote.setFechaUltimoIntento(LocalDateTime.now());
                lote.setRespuestaSifen(respuesta.getRespuestaBruta());
                lote.setIntentos(lote.getIntentos() + 1);
                
                String codigoRespuesta = respuesta.getdCodRes();
                
                // 6. Determinar estado según respuesta
                if ("0300".equals(codigoRespuesta)) {
                    // Lote recibido exitosamente
                    lote.setEstado(EstadoLoteDE.EN_PROCESO);
                    String protocolo = extraerProtocoloDeRespuesta(respuesta.getRespuestaBruta());
                    lote.setProtocolo(protocolo);
                    log.info("✅ Lote enviado. Protocolo: {}", protocolo);
                } else {
                    // Error en envío
                    lote.setEstado(EstadoLoteDE.ERROR_ENVIO);
                    log.error("❌ Error al enviar lote: {} - {}", 
                        codigoRespuesta, respuesta.getdMsgRes());
                }
                
                loteDERepository.save(lote);
                
            } finally {
                // Limpiar configuración (opcional, pero buena práctica)
                // jsifenlib mantiene configuración global, pero limpiamos referencias locales
            }
        }
    }
    
    /**
     * Consulta el estado de un lote en SIFEN.
     */
    @Transactional
    public void consultarLote(LoteDE lote, Long empresaId) throws SifenException {
        log.info("🔍 Consultando lote ID: {} para empresa ID: {}", lote.getId(), empresaId);
        
        if (lote.getProtocolo() == null || lote.getProtocolo().isEmpty()) {
            throw new IllegalArgumentException("El lote no tiene protocolo para consultar");
        }
        
        synchronized (sifenLock) {
            // Configurar jsifenlib para esta empresa
            configurarSifenParaEmpresa(empresaId);
            
            try {
                // Consultar estado en SIFEN
                RespuestaConsultaLoteDE respuesta = Sifen.consultaLoteDE(lote.getProtocolo());
                
                lote.setFechaUltimoIntento(LocalDateTime.now());
                lote.setRespuestaSifen(respuesta.getRespuestaBruta());
                
                String codigoRespuesta = respuesta.getdCodResLot();
                
                // Procesar según código de respuesta
                switch (codigoRespuesta) {
                    case "0360": // Lote no existe
                        lote.setEstado(EstadoLoteDE.ERROR_PERMANENTE);
                        actualizarDocumentosLote(lote, EstadoDE.RECHAZADO, 
                            codigoRespuesta, respuesta.getdMsgResLot());
                        break;
                        
                    case "0361": // Lote en procesamiento
                        lote.setEstado(EstadoLoteDE.EN_PROCESO);
                        break;
                        
                    case "0362": // Procesamiento concluido
                        procesarRespuestaLoteConcluido(lote, respuesta);
                        break;
                        
                    default:
                        lote.setEstado(EstadoLoteDE.ERROR_PERMANENTE);
                        break;
                }
                
                loteDERepository.save(lote);
            } finally {
                // Cleanup
            }
        }
    }
    
    /**
     * Consulta el estado de un DE individual en SIFEN.
     * IMPORTANTE: También procesa eventos asociados (cancelación).
     */
    @Transactional
    public RespuestaConsultaDE consultarDE(String cdc, Long empresaId) {
        log.info("🔍 Consultando DE con CDC: {} para empresa ID: {}", cdc, empresaId);
        
        synchronized (sifenLock) {
            configurarSifenParaEmpresa(empresaId);
            
            try {
                RespuestaConsultaDE respuesta = Sifen.consultaDE(cdc);
                
                // Si el DE fue encontrado (0422), procesar estado y eventos
                if ("0422".equals(respuesta.getdCodRes())) {
                    // Paso 1: Actualizar estado base del DE
                    actualizarEstadoDesdeRespuesta(cdc, respuesta.getRespuestaBruta());
                    
                    // Paso 2: Procesar eventos asociados (puede sobrescribir estado)
                    procesarEventosAsociados(cdc, respuesta.getRespuestaBruta());
                }
                
                return respuesta;
            } catch (SifenException e) {
                log.error("❌ Error al consultar DE: {}", e.getMessage());
                throw new RuntimeException("Error al consultar DE en SIFEN", e);
            }
        }
    }
    
    /**
     * Configura jsifenlib dinámicamente para una empresa específica.
     * CRÍTICO: Este método debe ser llamado dentro de un bloque sincronizado.
     */
    private void configurarSifenParaEmpresa(Long empresaId) {
        Empresa empresa = empresaRepository.findById(empresaId)
            .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada"));
        
        // Desencriptar datos sensibles
        String csc = encryptionService.decrypt(empresa.getCscEncrypted());
        String certPassword = encryptionService.decrypt(
            empresa.getCertificadoPasswordEncrypted()
        );
        
        try {
            // Crear configuración de jsifenlib
            SifenConfig config = new SifenConfig(
                TipoAmbiente.valueOf(ambiente.toUpperCase()),
                empresa.getCscId(),
                csc,
                TipoCertificadoCliente.PFX,
                empresa.getCertificadoPath(),
                certPassword
            );
            
            // Configurar jsifenlib globalmente (thread-unsafe, por eso el lock)
            Sifen.setSifenConfig(config);
            
            log.debug("✅ jsifenlib configurado para empresa: {}", empresa.getRazonSocial());
            
        } catch (Exception e) {
            log.error("❌ Error al configurar jsifenlib para empresa {}: {}", 
                empresaId, e.getMessage());
            throw new RuntimeException("Error al configurar SIFEN", e);
        } finally {
            // Limpiar datos sensibles de memoria
            csc = null;
            certPassword = null;
        }
    }
    
    // ... métodos auxiliares (extraerProtocoloDeRespuesta, procesarRespuestaLoteConcluido, etc.)
}
```

### 2. SifenEventoService - Gestión de Eventos

**Responsabilidades**:
- Cancelación de DEs
- Inutilización de números
- Nominación de receptores
- Manejo de reintentos

```java
@Service
@Transactional
public class SifenEventoService {
    
    private final DocumentoElectronicoRepository documentoElectronicoRepository;
    private final EventoCancelacionDERepository eventoCancelacionDERepository;
    private final EmpresaRepository empresaRepository;
    private final EncryptionService encryptionService;
    
    @Value("${sifen.ambiente}")
    private String ambiente;
    
    private final Object sifenLock = new Object();
    
    /**
     * Cancela un Documento Electrónico aprobado.
     * 
     * CARACTERÍSTICAS:
     * - Maneja reintentos automáticos
     * - Previene cancelaciones duplicadas
     * - Marca eventos previos como inactivos (histórico)
     * 
     * PLAZOS:
     * - Factura Electrónica: Hasta 48 horas desde aprobación
     * - Otros DTE: Hasta 168 horas (7 días) desde aprobación
     */
    @Transactional
    public RespuestaRecepcionEvento cancelarDE(String cdc, String motivo, Long empresaId) 
            throws SifenException {
        log.info("🚫 Cancelando DE con CDC: {} para empresa ID: {}", cdc, empresaId);
        
        // 1. Validar que el DE existe
        DocumentoElectronico de = documentoElectronicoRepository.findByCdc(cdc)
            .orElseThrow(() -> new IllegalArgumentException("DE no encontrado: " + cdc));
        
        // 2. Verificar si ya tiene cancelación aprobada
        if (tieneCancelacionAprobada(de.getId())) {
            throw new IllegalStateException("El DE ya fue cancelado exitosamente");
        }
        
        // 3. Marcar eventos previos como inactivos (reintentos)
        List<EventoCancelacionDE> eventosActivos = 
            eventoCancelacionDERepository.findActivosByCdcDocumento(cdc);
        
        for (EventoCancelacionDE eventoAnterior : eventosActivos) {
            eventoAnterior.setActivo(false);
            eventoCancelacionDERepository.save(eventoAnterior);
        }
        
        synchronized (sifenLock) {
            // Configurar jsifenlib para esta empresa
            configurarSifenParaEmpresa(empresaId);
            
            try {
                // 4. Crear evento de cancelación
                TrGeVeCan cancelacion = new TrGeVeCan();
                cancelacion.setId(cdc);
                cancelacion.setmOtEve(motivo);
                
                TgGroupTiEvt tipoEvento = new TgGroupTiEvt();
                tipoEvento.setrGeVeCan(cancelacion);
                
                // 5. Crear gestión de evento
                String eventoId = String.valueOf(new Random().nextInt(99999999) + 1);
                LocalDateTime fechaFirma = LocalDateTime.now();
                
                TrGesEve gestionEvento = new TrGesEve();
                gestionEvento.setId(eventoId);
                gestionEvento.setdFecFirma(fechaFirma);
                gestionEvento.setgGroupTiEvt(tipoEvento);
                
                List<TrGesEve> listaEventos = new ArrayList<>();
                listaEventos.add(gestionEvento);
                
                EventosDE eventosDE = new EventosDE();
                eventosDE.setrGesEveList(listaEventos);
                
                // 6. Crear registro en BD (antes de enviar)
                EventoCancelacionDE eventoCancelacion = new EventoCancelacionDE();
                eventoCancelacion.setDocumentoElectronico(de);
                eventoCancelacion.setEventoId(eventoId);
                eventoCancelacion.setFechaFirma(fechaFirma);
                eventoCancelacion.setCdcDocumento(cdc);
                eventoCancelacion.setMotivoCancelacion(motivo);
                eventoCancelacion.setEstado(EstadoEvento.PENDIENTE);
                eventoCancelacion.setActivo(true);
                
                // 7. Enviar a SIFEN
                RespuestaRecepcionEvento respuesta = Sifen.recepcionEvento(eventosDE);
                
                // 8. Procesar respuesta
                String xmlRespuesta = respuesta.getRespuestaBruta();
                String estadoResultado = extraerValorXML(xmlRespuesta, "<dEstRes>", "</dEstRes>");
                String protocolo = extraerValorXML(xmlRespuesta, "<dProtAut>", "</dProtAut>");
                
                eventoCancelacion.setRespuestaBruta(xmlRespuesta);
                eventoCancelacion.setProtocoloAutorizacion(protocolo);
                
                // 9. Actualizar estado según respuesta
                if ("Aprobado".equalsIgnoreCase(estadoResultado)) {
                    eventoCancelacion.setEstado(EstadoEvento.APROBADO);
                    eventoCancelacion.setFechaProcesamiento(LocalDateTime.now());
                    
                    // Actualizar DE a CANCELADO
                    de.setEstado(EstadoDE.CANCELADO);
                    documentoElectronicoRepository.save(de);
                    
                    log.info("✅ Evento APROBADO - DE actualizado a CANCELADO");
                    
                } else if ("Rechazado".equalsIgnoreCase(estadoResultado)) {
                    eventoCancelacion.setEstado(EstadoEvento.RECHAZADO);
                    eventoCancelacion.setFechaProcesamiento(LocalDateTime.now());
                    
                    log.error("❌ Evento RECHAZADO por SIFEN");
                } else {
                    eventoCancelacion.setEstado(EstadoEvento.PENDIENTE);
                }
                
                eventoCancelacionDERepository.save(eventoCancelacion);
                
                return respuesta;
                
            } finally {
                // Cleanup
            }
        }
    }
    
    private void configurarSifenParaEmpresa(Long empresaId) {
        // Mismo método que en SifenService
        // Considerar extraer a un componente compartido
    }
}
```

### 3. SifenSchedulerService - Tareas Programadas

**Responsabilidades**:
- Consultar lotes en proceso periódicamente
- Consultar DEs pendientes
- Alertar certificados por vencer

```java
@Component
public class SifenSchedulerService {
    
    private final LoteDERepository loteDERepository;
    private final DocumentoElectronicoRepository documentoElectronicoRepository;
    private final SifenService sifenService;
    
    /**
     * Consulta estado de lotes EN_PROCESO cada 5 minutos.
     */
    @Scheduled(fixedDelay = 300000) // 5 minutos
    public void consultarEstadoLotes() {
        log.info("⏰ Scheduler: Consultando lotes en proceso...");
        
        List<LoteDE> lotesEnProceso = loteDERepository.findByEstado(EstadoLoteDE.EN_PROCESO);
        
        for (LoteDE lote : lotesEnProceso) {
            try {
                // Obtener empresaId del lote
                Long empresaId = lote.getDocumentosElectronicos().get(0)
                    .getFacturaLegal().getEmpresa().getId();
                
                sifenService.consultarLote(lote, empresaId);
                
            } catch (Exception e) {
                log.error("Error al consultar lote {}: {}", lote.getId(), e.getMessage());
            }
        }
    }
    
    /**
     * Consulta DEs pendientes cada 10 minutos.
     */
    @Scheduled(fixedDelay = 600000) // 10 minutos
    public void consultarDEsPendientes() {
        log.info("⏰ Scheduler: Consultando DEs pendientes...");
        
        List<DocumentoElectronico> desPendientes = 
            documentoElectronicoRepository.findByEstadoIn(
                Arrays.asList(EstadoDE.PENDIENTE, EstadoDE.EN_PROCESO)
            );
        
        for (DocumentoElectronico de : desPendientes) {
            if (de.getCdc() != null && !de.getCdc().isEmpty()) {
                try {
                    Long empresaId = de.getFacturaLegal().getEmpresa().getId();
                    sifenService.consultarDE(de.getCdc(), empresaId);
                    
                } catch (Exception e) {
                    log.error("Error al consultar DE {}: {}", de.getCdc(), e.getMessage());
                }
            }
        }
    }
}
```

## Modelo de Datos Actualizado

### Empresa - Campos Adicionales

```java
@Entity
public class Empresa {
    // ... campos existentes
    
    // Certificado PFX
    private String certificadoPath;
    private String certificadoPasswordEncrypted;
    private LocalDate certificadoFechaExpiracion;
    
    // CSC (Código de Seguridad del Contribuyente)
    @Column(name = "csc_id")
    private String cscId;
    
    @Column(name = "csc_encrypted", columnDefinition = "TEXT")
    private String cscEncrypted;
    
    // Ambiente SIFEN
    @Column(name = "sifen_ambiente")
    private String sifenAmbiente; // DEV, TEST, PRODUCTION
}
```

## Configuración (application.yml)

```yaml
sifen:
  ambiente: DEV  # DEV, TEST, PRODUCTION
  # NO configurar certificado ni CSC aquí (son por empresa)
```

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

## Diferencias Clave con Implementación Single-Tenant

| Aspecto | Single-Tenant (Referencia) | Multi-Tenant (Nuestro Sistema) |
|---------|---------------------------|--------------------------------|
| Configuración | Bean singleton global | Dinámica por empresa |
| Certificado | Uno solo en application.yml | Uno por empresa en BD |
| CSC | Uno solo en application.yml | Uno por empresa en BD |
| Thread Safety | No crítico | Requiere sincronización |
| Métodos de Servicio | `enviarLote(lote)` | `enviarLote(lote, empresaId)` |
| Configuración jsifenlib | Una vez al inicio | Antes de cada operación |

## Conclusión

La adaptación principal consiste en:

1. **Eliminar configuración global** de certificados y CSC
2. **Agregar empresaId** a todos los métodos de servicio
3. **Configurar jsifenlib dinámicamente** antes de cada operación
4. **Sincronizar acceso** con lock para thread-safety
5. **Almacenar certificado y CSC** por empresa (encriptados)

El resto de la lógica de negocio (generación de DEs, procesamiento de respuestas, manejo de eventos) se mantiene igual que en la implementación de referencia.
