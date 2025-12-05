package com.frcefact.service.sifen;

import com.frcefact.exception.BusinessException;
import com.frcefact.model.Cliente;
import com.frcefact.model.DocumentoElectronico;
import com.frcefact.model.Empresa;
import com.frcefact.model.EstadoDE;
import com.frcefact.model.EstadoLoteDE;
import com.frcefact.model.FacturaLegal;
import com.frcefact.model.NotaCredito;
import com.frcefact.model.LoteDE;
import com.frcefact.model.Timbrado;
import com.frcefact.model.TimbradoDetalle;
import com.frcefact.repository.DocumentoElectronicoRepository;
import com.frcefact.repository.FacturaLegalItemRepository;
import com.frcefact.repository.NotaCreditoItemRepository;
import com.frcefact.repository.LoteDERepository;
import com.frcefact.repository.EventoCancelacionDERepository;
import com.frcefact.repository.EventoNominacionDERepository;
import com.frcefact.repository.FacturaLegalRepository;
import com.frcefact.sifen.config.SifenConfigFactory;
import com.frcefact.sifen.util.SifenResponseParser;
import com.frcefact.sifen.util.SifenResponseParser.DocumentResult;
import com.frcefact.sifen.util.SifenResponseParser.EventResult;
import com.frcefact.service.XmlGeneratorService;
import com.frcefact.sifen.util.SifenDocumentoLogger;
import com.frcefact.sifen.util.SifenTotalsHelper;
import com.frcefact.model.FacturaLegalItem;
import com.frcefact.model.NotaCreditoItem;
import com.frcefact.model.Producto;
import com.roshka.sifen.Sifen;
import com.roshka.sifen.core.SifenConfig;
import com.roshka.sifen.core.beans.response.RespuestaConsultaDE;
import com.roshka.sifen.core.beans.response.RespuestaConsultaLoteDE;
import com.roshka.sifen.core.beans.response.RespuestaRecepcionLoteDE;
import com.roshka.sifen.core.fields.request.de.*;
import com.roshka.sifen.core.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio principal para operaciones con SIFEN.
 * Gestiona envío de lotes, consultas y actualización de estados.
 */
@Service
@Transactional
public class SifenService {

    private static final Logger log = LoggerFactory.getLogger(SifenService.class);

    private final DocumentoElectronicoRepository documentoElectronicoRepository;
    private final LoteDERepository loteDERepository;
    private final FacturaLegalItemRepository facturaLegalItemRepository;
    private final NotaCreditoItemRepository notaCreditoItemRepository;
    private final EventoCancelacionDERepository eventoCancelacionDERepository;
    private final EventoNominacionDERepository eventoNominacionDERepository;
    private final FacturaLegalRepository facturaLegalRepository;
    private final XmlGeneratorService xmlGeneratorService;
    private final SifenConfigFactory sifenConfigFactory;
    private final com.frcefact.service.EmailFacturaElectronicaService emailFacturaElectronicaService;

    public SifenService(DocumentoElectronicoRepository documentoElectronicoRepository,
                        LoteDERepository loteDERepository,
                        FacturaLegalItemRepository facturaLegalItemRepository,
                        NotaCreditoItemRepository notaCreditoItemRepository,
                        EventoCancelacionDERepository eventoCancelacionDERepository,
                        EventoNominacionDERepository eventoNominacionDERepository,
                        FacturaLegalRepository facturaLegalRepository,
                        XmlGeneratorService xmlGeneratorService,
                        SifenConfigFactory sifenConfigFactory,
                        com.frcefact.service.EmailFacturaElectronicaService emailFacturaElectronicaService) {
        this.documentoElectronicoRepository = documentoElectronicoRepository;
        this.loteDERepository = loteDERepository;
        this.facturaLegalItemRepository = facturaLegalItemRepository;
        this.notaCreditoItemRepository = notaCreditoItemRepository;
        this.eventoCancelacionDERepository = eventoCancelacionDERepository;
        this.eventoNominacionDERepository = eventoNominacionDERepository;
        this.facturaLegalRepository = facturaLegalRepository;
        this.xmlGeneratorService = xmlGeneratorService;
        this.sifenConfigFactory = sifenConfigFactory;
        this.emailFacturaElectronicaService = emailFacturaElectronicaService;
    }

    /**
     * Envía un lote de documentos electrónicos a SIFEN.
     *
     * @param loteId identificador del lote
     * @return lote actualizado
     */
    public LoteDE enviarLote(Long loteId) {
        log.info("📤 Iniciando envío de lote ID: {}", loteId);
        
        LoteDE lote = loteDERepository.findById(loteId)
                .orElseThrow(() -> new BusinessException("Lote no encontrado: " + loteId));

        List<DocumentoElectronico> documentos = documentoElectronicoRepository.findByLoteId(loteId);
        if (documentos.isEmpty()) {
            throw new BusinessException("El lote no contiene documentos para enviar");
        }

        log.info("📋 Lote contiene {} documento(s)", documentos.size());

        // Obtener timbrado y validar configuración
        // El lote puede contener facturas, notas de crédito, débito o remisión
        DocumentoElectronico primerDoc = documentos.get(0);
        TimbradoDetalle timbradoDetalle = null;
        String tipoDoc = primerDoc.getTipoDocumento();
        
        // Determinar el tipo de documento y obtener el timbrado detalle
        if (primerDoc.getFacturaLegal() != null) {
            timbradoDetalle = primerDoc.getFacturaLegal().getTimbradoDetalle();
            if (timbradoDetalle == null) {
                throw new BusinessException("La factura " + primerDoc.getFacturaLegal().getId() + " no tiene timbrado detalle configurado");
            }
        } else if (primerDoc.getNotaCredito() != null) {
            timbradoDetalle = primerDoc.getNotaCredito().getTimbradoDetalle();
            if (timbradoDetalle == null) {
                throw new BusinessException("La nota de crédito " + primerDoc.getNotaCredito().getId() + " no tiene timbrado detalle configurado");
            }
        } else if (primerDoc.getNotaDebito() != null) {
            timbradoDetalle = primerDoc.getNotaDebito().getTimbradoDetalle();
            if (timbradoDetalle == null) {
                throw new BusinessException("La nota de débito " + primerDoc.getNotaDebito().getId() + " no tiene timbrado detalle configurado");
            }
        } else if (primerDoc.getNotaRemision() != null) {
            timbradoDetalle = primerDoc.getNotaRemision().getTimbradoDetalle();
            if (timbradoDetalle == null) {
                throw new BusinessException("La nota de remisión " + primerDoc.getNotaRemision().getId() + " no tiene timbrado detalle configurado");
            }
        } else {
            throw new BusinessException("El documento electrónico ID: " + primerDoc.getId() + " no tiene ningún documento asociado (factura, nota de crédito, débito o remisión)");
        }
        
        Timbrado timbrado = timbradoDetalle.getTimbrado();
        if (timbrado == null) {
            throw new BusinessException("El timbrado detalle no tiene timbrado asociado");
        }
        
        log.info("🔧 Configurando SIFEN para timbrado ID: {} (empresa ID: {}, tipo documento: {})", 
                timbrado.getId(), timbrado.getEmpresa().getId(), tipoDoc);
        
        // Validar configuración antes de continuar
        SifenConfig config;
        try {
            config = sifenConfigFactory.buildForTimbrado(timbrado.getId());
            log.info("✅ Configuración SIFEN validada correctamente para timbrado ID: {}", timbrado.getId());
        } catch (Exception e) {
            log.error("❌ Error al construir configuración SIFEN para timbrado ID: {}: {}", 
                    timbrado.getId(), e.getMessage());
            throw new BusinessException("Error en configuración SIFEN: " + e.getMessage());
        }

        List<com.roshka.sifen.core.beans.DocumentoElectronico> sifenDocs = new ArrayList<>();
        for (DocumentoElectronico de : documentos) {
            String tipoDocDe = de.getTipoDocumento();
            log.info("📄 Procesando DE ID: {} (tipo: {})", de.getId(), tipoDocDe);
            
            String xmlOriginal = de.getXmlOriginal();
            if (xmlOriginal == null || xmlOriginal.isBlank()) {
                // Si no hay XML, necesitamos regenerarlo según el tipo de documento
                if (de.getFacturaLegal() != null) {
                    log.warn("⚠️ DE ID: {} no tiene XML original, generando nuevo XML desde factura", de.getId());
                    xmlOriginal = xmlGeneratorService.generarXmlOriginal(de.getFacturaLegal());
                    de.setXmlOriginal(xmlOriginal);
                    documentoElectronicoRepository.save(de);
                } else if (de.getNotaCredito() != null) {
                    log.warn("⚠️ DE ID: {} no tiene XML original, no se puede regenerar automáticamente para nota de crédito", de.getId());
                    throw new BusinessException("El DE de nota de crédito ID: " + de.getId() + " no tiene XML original y no se puede regenerar automáticamente. Debe generarse el DE nuevamente.");
                } else {
                    throw new BusinessException("El DE ID: " + de.getId() + " no tiene XML original y no se puede regenerar automáticamente para este tipo de documento.");
                }
            }
            
            final String xmlFinal = xmlOriginal; // Hacer final para usar en lambda
            
            log.debug("   - XML original length: {} caracteres", xmlFinal != null ? xmlFinal.length() : 0);
            if (xmlFinal != null && xmlFinal.length() > 0) {
                log.debug("   - XML preview (primeros 200 chars): {}", 
                        xmlFinal.substring(0, Math.min(200, xmlFinal.length())));
            }

            com.roshka.sifen.core.beans.DocumentoElectronico deSifen = null;
            
            // ESTRATEGIA: Intentar reconstruir desde XML guardado (método preferido)
            // IMPORTANTE: Aunque el XML ya está generado, envolver en execute para asegurar
            // que el contexto global esté configurado correctamente durante la reconstrucción
            if (xmlFinal != null && !xmlFinal.isBlank()) {
                try {
                    log.debug("🔨 Intentando crear objeto DocumentoElectronico de jsifenlib desde XML guardado...");
                    deSifen = execute(config, () -> {
                        // Reconstruir desde XML dentro del contexto configurado
                        return new com.roshka.sifen.core.beans.DocumentoElectronico(xmlFinal);
                    });
                    log.debug("✅ Objeto DocumentoElectronico creado exitosamente desde XML");
                } catch (Exception e) {
                    log.warn("⚠️ Error al reconstruir desde XML guardado: {}. Intentando construir desde factura...", 
                            e.getMessage());
                    deSifen = null; // Continuar para intentar construir desde factura
                }
            }
            
            // FALLBACK: Construir directamente desde datos del documento asociado (método robusto)
            if (deSifen == null) {
                log.info("🔨 Construyendo objeto DocumentoElectronico de jsifenlib directamente desde datos del documento...");
                try {
                    // Construir según el tipo de documento
                    if (de.getFacturaLegal() != null) {
                        List<FacturaLegalItem> items = facturaLegalItemRepository.findByFacturaLegalId(de.getFacturaLegal().getId());
                        if (items == null || items.isEmpty()) {
                            throw new IllegalArgumentException("Factura sin items - no se puede construir DE");
                        }
                        deSifen = construirDEDesdeFactura(de.getFacturaLegal(), items, config);
                        log.debug("✅ Objeto DocumentoElectronico construido exitosamente desde datos de factura");
                    } else if (de.getNotaCredito() != null) {
                        List<NotaCreditoItem> items = notaCreditoItemRepository.findByNotaCreditoId(de.getNotaCredito().getId());
                        if (items == null || items.isEmpty()) {
                            throw new IllegalArgumentException("Nota de crédito sin items - no se puede construir DE");
                        }
                        deSifen = construirDEDesdeNotaCredito(de.getNotaCredito(), items, config);
                        log.debug("✅ Objeto DocumentoElectronico construido exitosamente desde datos de nota de crédito");
                    } else {
                        throw new BusinessException("No se puede construir el DE automáticamente para el documento ID: " + de.getId() + ". Tipo de documento no soportado para construcción automática.");
                    }
                } catch (Exception e) {
                    log.error("❌ Error al construir DE desde documento ID: {}: {}", 
                            de.getId(), e.getMessage(), e);
                    throw new BusinessException("No se pudo construir el DE para el documento "
                            + de.getId() + ": " + e.getMessage());
                }
            }
            
            // Aplicar fix de IVA si es necesario
            if (deSifen.getgTotSub() != null) {
                log.debug("🔧 Aplicando fix de IVA...");
                SifenTotalsHelper.applyIvaFix(deSifen.getgTotSub());
            }
            
            sifenDocs.add(deSifen);
            log.info("✅ DE ID: {} agregado al lote para envío", de.getId());
        }

        log.info("📤 Enviando {} documento(s) a SIFEN...", sifenDocs.size());
        RespuestaRecepcionLoteDE respuesta = execute(config, () -> Sifen.recepcionLoteDE(sifenDocs));
        procesarRespuestaEnvioLote(lote, documentos, respuesta);
        return loteDERepository.save(lote);
    }

    /**
     * Consulta el estado de un lote en SIFEN y actualiza los documentos asociados.
     */
    public LoteDE consultarLote(Long loteId) {
        LoteDE lote = loteDERepository.findById(loteId)
                .orElseThrow(() -> new BusinessException("Lote no encontrado: " + loteId));

        if (lote.getProtocolo() == null || lote.getProtocolo().isBlank()) {
            throw new BusinessException("El lote no cuenta con protocolo para consultar");
        }

        List<DocumentoElectronico> documentos = documentoElectronicoRepository.findByLoteId(loteId);
        if (documentos.isEmpty()) {
            throw new BusinessException("El lote no contiene documentos asociados");
        }

        // Identificar el tipo de documento y obtener el timbrado
        DocumentoElectronico primerDoc = documentos.get(0);
        TimbradoDetalle timbradoDetalle = null;

        if (primerDoc.getFacturaLegal() != null) {
            timbradoDetalle = primerDoc.getFacturaLegal().getTimbradoDetalle();
        } else if (primerDoc.getNotaCredito() != null) {
            timbradoDetalle = primerDoc.getNotaCredito().getTimbradoDetalle();
        } else if (primerDoc.getNotaDebito() != null) {
            timbradoDetalle = primerDoc.getNotaDebito().getTimbradoDetalle();
        } else if (primerDoc.getNotaRemision() != null) {
            timbradoDetalle = primerDoc.getNotaRemision().getTimbradoDetalle();
        }

        if (timbradoDetalle == null) {
            throw new BusinessException("El documento electrónico ID: " + primerDoc.getId() + " no tiene ningún documento asociado (factura, nota de crédito, débito o remisión)");
        }

        SifenConfig config = sifenConfigFactory.buildForTimbrado(timbradoDetalle.getTimbrado().getId());

        log.info("🔍 Consultando lote ID: {} con protocolo: {}", lote.getId(), lote.getProtocolo());
        
        // Validar que el protocolo no esté vacío o sea inválido
        if (lote.getProtocolo() == null || lote.getProtocolo().isBlank()) {
            throw new BusinessException("El lote no cuenta con protocolo válido para consultar");
        }
        
        // Validar formato básico del protocolo (debe ser numérico y tener longitud razonable)
        String protocolo = lote.getProtocolo().trim();
        if (!protocolo.matches("\\d+") || protocolo.length() < 5) {
            log.error("⚠️ Protocolo inválido para lote ID {}: '{}'. Debe ser numérico y tener al menos 5 dígitos.", 
                    lote.getId(), protocolo);
            throw new BusinessException("El protocolo del lote no tiene un formato válido: " + protocolo);
        }
        
        log.debug("   Protocolo validado: {} (longitud: {})", protocolo, protocolo.length());
        
        // Llamar consultaLoteDE sin pasar config explícitamente - executeWithConfig ya configuró el contexto global
        // Esto coincide con cómo se hace en el proyecto de referencia
        RespuestaConsultaLoteDE respuesta = execute(config, () -> {
            try {
                return Sifen.consultaLoteDE(protocolo);
            } catch (Exception e) {
                log.error("❌ Error al consultar lote con protocolo {}: {}", protocolo, e.getMessage(), e);
                throw e;
            }
        });
        
        String raw = respuesta != null ? respuesta.getRespuestaBruta() : null;
        String codigo = raw != null ? SifenResponseParser.extractCodigoRespuesta(raw) : null;
        
        procesarRespuestaConsultaLote(lote, documentos, respuesta);

        documentoElectronicoRepository.saveAll(documentos);
        
        // Después de procesar la respuesta del lote, consultar cada DE individual
        // para detectar eventos de cancelación que no aparecen en la consulta del lote
        if (("0362".equalsIgnoreCase(codigo) || "0260".equalsIgnoreCase(codigo)) 
                && lote.getEstado() == EstadoLoteDE.PROCESADO) {
            log.info("🔍 Consultando cada DE individual para detectar eventos de cancelación...");
            consultarDocumentosIndividuales(documentos, config);
        }
        
        return loteDERepository.save(lote);
    }
    
    /**
     * Consulta cada documento electrónico individual por su CDC para detectar eventos
     * de cancelación que no aparecen en la consulta del lote.
     * 
     * Esto es necesario porque la consulta de lote puede no incluir información detallada
     * sobre eventos de cancelación, pero la consulta individual sí la incluye.
     */
    private void consultarDocumentosIndividuales(List<DocumentoElectronico> documentos, SifenConfig config) {
        if (documentos == null || documentos.isEmpty()) {
            return;
        }
        
        log.info("   📋 Consultando {} documento(s) individual(es)...", documentos.size());
        
        for (DocumentoElectronico documento : documentos) {
            if (documento.getCdc() == null || documento.getCdc().isBlank()) {
                log.warn("   ⚠️ DE ID {} no tiene CDC - omitiendo consulta individual", documento.getId());
                continue;
            }
            
            try {
                log.info("   🔍 Consultando DE individual - CDC: {}", documento.getCdc());
                RespuestaConsultaDE respuesta = execute(config, () -> Sifen.consultaDE(documento.getCdc()));
                procesarRespuestaConsultaDocumento(documento, respuesta);
                documentoElectronicoRepository.save(documento);
                log.info("   ✅ DE {} consultado y actualizado", documento.getCdc());
            } catch (Exception e) {
                log.error("   ❌ Error al consultar DE individual {}: {}", documento.getCdc(), e.getMessage(), e);
                // Continuar con el siguiente documento aunque uno falle
            }
        }
        
        log.info("   ✅ Consulta individual de documentos completada");
    }

    /**
     * Reenvía un documento electrónico en un nuevo lote.
     * Útil cuando un DE necesita ser reenviado después de un error.
     * 
     * @param deId ID del documento electrónico a reenviar
     * @return El nuevo lote creado y enviado
     */
    public LoteDE reenviarDEEnNuevoLote(Long deId) {
        log.info("🔄 Reenviando DE ID: {} en un nuevo lote", deId);
        
        DocumentoElectronico de = documentoElectronicoRepository.findById(deId)
                .orElseThrow(() -> new BusinessException("Documento electrónico no encontrado: " + deId));
        
        // Verificar que el DE tenga XML original (necesario para reenvío)
        if (de.getXmlOriginal() == null || de.getXmlOriginal().isBlank()) {
            throw new BusinessException("El documento electrónico no tiene XML original. No se puede reenviar.");
        }
        
        // Verificar que el DE tenga CDC
        if (de.getCdc() == null || de.getCdc().isBlank()) {
            throw new BusinessException("El documento electrónico no tiene CDC. No se puede reenviar.");
        }
        
        // Obtener empresa desde la factura
        FacturaLegal factura = de.getFacturaLegal();
        if (factura == null) {
            throw new BusinessException("El documento electrónico no tiene factura asociada.");
        }
        
        Empresa empresa = factura.getEmpresa();
        
        // Remover del lote anterior si existe
        LoteDE loteAnterior = de.getLoteDE();
        if (loteAnterior != null) {
            log.info("   🔗 Removiendo DE del lote anterior ID: {}", loteAnterior.getId());
            de.setLoteDE(null);
            documentoElectronicoRepository.save(de);
        }
        
        // Resetear estado y limpiar errores previos
        de.setEstado(EstadoDE.PENDIENTE);
        de.setCodigoRespuestaSifen(null);
        de.setMensajeRespuestaSifen(null);
        de.setRespuestaSifen(null);
        de.setFechaEstadoActualizado(LocalDateTime.now());
        documentoElectronicoRepository.save(de);
        log.info("   ✅ Estado del DE reseteado a PENDIENTE");
        
        // Crear nuevo lote
        LoteDE nuevoLote = new LoteDE();
        nuevoLote.setEmpresa(empresa);
        nuevoLote.setEstado(EstadoLoteDE.PENDIENTE);
        nuevoLote.setIntentos(0);
        nuevoLote = loteDERepository.save(nuevoLote);
        log.info("   ✅ Nuevo lote creado con ID: {}", nuevoLote.getId());
        
        // Asociar DE al nuevo lote
        de.setLoteDE(nuevoLote);
        de.setEstado(EstadoDE.EN_PROCESO);
        documentoElectronicoRepository.save(de);
        log.info("   ✅ DE asociado al nuevo lote");
        
        // Enviar el lote
        log.info("   📤 Enviando nuevo lote a SIFEN...");
        return enviarLote(nuevoLote.getId());
    }

    /**
     * Obtiene un documento electrónico por ID de factura.
     * Si la URL del QR no está guardada, la extrae del XML original.
     */
    @Transactional(readOnly = true)
    public DocumentoElectronico obtenerDocumentoPorFacturaId(Long facturaId) {
        DocumentoElectronico documento = documentoElectronicoRepository.findByFacturaLegalIdWithRelations(facturaId)
                .orElseThrow(() -> new BusinessException("No se encontró documento electrónico para la factura ID: " + facturaId));
        
        // Si no tiene URL del QR guardada, intentar extraerla del XML original
        if ((documento.getUrlQr() == null || documento.getUrlQr().isBlank()) 
                && documento.getXmlOriginal() != null && !documento.getXmlOriginal().isBlank()) {
            try {
                String urlQr = com.frcefact.sifen.util.SifenResponseParser.extractUrlQr(documento.getXmlOriginal());
                if (urlQr != null && !urlQr.isBlank()) {
                    // Guardar la URL extraída (solo lectura, pero podemos actualizar en memoria)
                    documento.setUrlQr(urlQr);
                    log.debug("✅ URL QR extraída del XML original para DE ID: {}", documento.getId());
                }
            } catch (Exception e) {
                log.warn("⚠️ No se pudo extraer URL QR del XML original para DE ID: {}: {}", documento.getId(), e.getMessage());
            }
        }
        
        return documento;
    }

    @Transactional(readOnly = true)
    public DocumentoElectronico obtenerDocumentoPorNotaCreditoId(Long notaCreditoId) {
        DocumentoElectronico documento = documentoElectronicoRepository.findByNotaCreditoId(notaCreditoId)
                .orElseThrow(() -> new BusinessException("No se encontró documento electrónico para la nota de crédito ID: " + notaCreditoId));
        
        // Si no tiene URL del QR guardada, intentar extraerla del XML original
        if ((documento.getUrlQr() == null || documento.getUrlQr().isBlank()) 
                && documento.getXmlOriginal() != null && !documento.getXmlOriginal().isBlank()) {
            try {
                String urlQr = com.frcefact.sifen.util.SifenResponseParser.extractUrlQr(documento.getXmlOriginal());
                if (urlQr != null && !urlQr.isBlank()) {
                    // Guardar la URL extraída (solo lectura, pero podemos actualizar en memoria)
                    documento.setUrlQr(urlQr);
                    log.debug("✅ URL QR extraída del XML original para DE ID: {}", documento.getId());
                }
            } catch (Exception e) {
                log.warn("⚠️ No se pudo extraer URL QR del XML original para DE ID: {}: {}", documento.getId(), e.getMessage());
            }
        }
        
        return documento;
    }

    /**
     * Consulta y actualiza el estado de un documento electrónico en SIFEN.
     */
    public DocumentoElectronico consultarDocumento(String cdc) {
        DocumentoElectronico documento = documentoElectronicoRepository.findByCdc(cdc)
                .orElseThrow(() -> new BusinessException("Documento electrónico no encontrado para CDC: " + cdc));

        var timbrado = documento.getFacturaLegal().getTimbradoDetalle().getTimbrado();
        SifenConfig config = sifenConfigFactory.buildForTimbrado(timbrado.getId());

        RespuestaConsultaDE respuesta = execute(config, () -> Sifen.consultaDE(cdc));
        procesarRespuestaConsultaDocumento(documento, respuesta);

        return documentoElectronicoRepository.save(documento);
    }

    private void procesarRespuestaEnvioLote(LoteDE lote,
                                            List<DocumentoElectronico> documentos,
                                            RespuestaRecepcionLoteDE respuesta) {
        String raw = respuesta != null ? respuesta.getRespuestaBruta() : null;
        String codigo = raw != null ? SifenResponseParser.extractCodigoRespuesta(raw) : null;
        String mensaje = raw != null ? SifenResponseParser.extractMensajeRespuesta(raw) : null;
        // Para respuesta de recepción de lote, el protocolo está en dProtConsLote
        String protocolo = raw != null ? SifenResponseParser.extractProtocoloLote(raw) : null;

        log.info("   📥 Respuesta de envío de lote - Código: {}, Mensaje: {}, Protocolo: {}", 
                codigo, mensaje, protocolo);

        lote.setRespuestaSifen(raw);
        lote.setCodigoRespuesta(codigo);
        lote.setMensajeRespuesta(mensaje);
        lote.setProtocolo(protocolo);
        lote.setFechaUltimoIntento(LocalDateTime.now());
        lote.setIntentos(Optional.ofNullable(lote.getIntentos()).orElse(0) + 1);

        if ("0300".equalsIgnoreCase(codigo)) {
            lote.setEstado(EstadoLoteDE.EN_PROCESO);
            log.info("✅ Lote {} enviado exitosamente. Protocolo: {}", lote.getId(), protocolo);
        } else {
            lote.setEstado(EstadoLoteDE.ERROR_ENVIO);
            log.error("❌ Error al enviar lote {}: {} - {}", lote.getId(), codigo, mensaje);
        }

        documentos.forEach(de -> {
            de.setIntentos(Optional.ofNullable(de.getIntentos()).orElse(0) + 1);
            de.setFechaEstadoActualizado(LocalDateTime.now());
            de.setRespuestaSifen(raw);
            if (EstadoLoteDE.EN_PROCESO.equals(lote.getEstado())) {
                de.setEstado(EstadoDE.EN_PROCESO);
            } else {
                de.setEstado(EstadoDE.ERROR);
                de.setCodigoRespuestaSifen(codigo);
                de.setMensajeRespuestaSifen(mensaje);
            }
        });
    }

    private void procesarRespuestaConsultaLote(LoteDE lote,
                                               List<DocumentoElectronico> documentos,
                                               RespuestaConsultaLoteDE respuesta) {
        String raw = respuesta != null ? respuesta.getRespuestaBruta() : null;
        String codigo = raw != null ? SifenResponseParser.extractCodigoRespuesta(raw) : null;
        String mensaje = raw != null ? SifenResponseParser.extractMensajeRespuesta(raw) : null;

        log.info("   📥 Respuesta recibida - Código: {}, Mensaje: {}", codigo, mensaje);

        lote.setRespuestaSifen(raw);
        lote.setCodigoRespuesta(codigo);
        lote.setMensajeRespuesta(mensaje);
        lote.setFechaUltimoIntento(LocalDateTime.now());

        if ("0360".equalsIgnoreCase(codigo)) {
            // Lote no existe o no encontrado
            log.error("❌ Lote {} no existe en SIFEN", lote.getId());
            lote.setEstado(EstadoLoteDE.ERROR_PERMANENTE);
            documentos.forEach(de -> actualizarEstadoDocumento(de, EstadoDE.RECHAZADO, codigo, mensaje, raw, null));
            
        } else if ("0361".equalsIgnoreCase(codigo)) {
            // Lote en procesamiento
            log.info("⏳ Lote {} aún en procesamiento", lote.getId());
            lote.setEstado(EstadoLoteDE.EN_PROCESO);
            
        } else if ("0362".equalsIgnoreCase(codigo) || "0260".equalsIgnoreCase(codigo)) {
            // Procesamiento concluido o Lote Aprobado
            log.info("✅ Lote {} procesamiento concluido (Código: {})", lote.getId(), codigo);
            lote.setEstado(EstadoLoteDE.PROCESADO);
            
            List<DocumentResult> results = SifenResponseParser.extractDocumentResults(raw);
            if (results.isEmpty()) {
                // Si no hay resultados individuales, asumimos que el lote fue aprobado completamente
                // y actualizamos todos los documentos al estado APROBADO si el lote fue aprobado
                if ("0260".equalsIgnoreCase(codigo)) {
                    log.info("✅ Lote {} aprobado sin detalles individuales - Actualizando documentos a APROBADO", lote.getId());
                    documentos.forEach(de -> actualizarEstadoDocumento(de, EstadoDE.APROBADO, codigo, mensaje, raw, null));
                } else {
                log.warn("No se encontraron resultados individuales en la respuesta de lote {}", lote.getId());
                }
                return;
            }

            Map<String, DocumentoElectronico> byCdc = documentos.stream()
                    .filter(de -> de.getCdc() != null)
                    .collect(java.util.stream.Collectors.toMap(DocumentoElectronico::getCdc, de -> de));

            for (DocumentResult result : results) {
                DocumentoElectronico de = byCdc.get(result.getCdc());
                if (de == null) {
                    log.warn("Se recibió resultado para CDC {} que no corresponde al lote {}", result.getCdc(), lote.getId());
                    continue;
                }

                EstadoDE estadoDoc = determinarEstadoDocumento(result);
                actualizarEstadoDocumento(de, estadoDoc, result.getCodigo(), result.getMensaje(), raw, result.getProtocolo());
            }
        } else {
            // Otros códigos de error
            log.warn("⚠️ Código de respuesta inesperado para lote: {}", codigo);
            lote.setEstado(EstadoLoteDE.ERROR_PERMANENTE);
            documentos.forEach(de -> actualizarEstadoDocumento(de, EstadoDE.ERROR, codigo, mensaje, raw, null));
        }
    }

    private void procesarRespuestaConsultaDocumento(DocumentoElectronico documento,
                                                    RespuestaConsultaDE respuesta) {
        String raw = respuesta != null ? respuesta.getRespuestaBruta() : null;
        String codigo = raw != null ? SifenResponseParser.extractCodigoRespuesta(raw) : null;
        String estado = raw != null ? SifenResponseParser.extractEstadoResultado(raw) : null;
        String mensaje = raw != null ? SifenResponseParser.extractMensajeRespuesta(raw) : null;
        String protocolo = raw != null ? SifenResponseParser.extractProtocolo(raw) : null;

        // Imprimir respuesta completa en logs para debugging
        log.info("═══════════════════════════════════════════════════════════════");
        log.info("🔍 RESPUESTA COMPLETA DE CONSULTA SIFEN - CDC: {}", documento.getCdc());
        log.info("═══════════════════════════════════════════════════════════════");
        log.info("📋 Código respuesta: {}", codigo);
        log.info("📋 Estado resultado: {}", estado);
        log.info("📋 Mensaje: {}", mensaje);
        log.info("📋 Protocolo: {}", protocolo);
        if (raw != null) {
            log.info("📄 Tamaño respuesta XML: {} bytes", raw.length());
            log.info("📄 Respuesta XML completa:");
            log.info("{}", raw);
        } else {
            log.warn("⚠️ Respuesta raw es null");
        }
        log.info("═══════════════════════════════════════════════════════════════");

        // Determinar estado inicial desde la respuesta SIFEN
        // Si hay protocolo de autorización, es una señal fuerte de que está aprobado
        EstadoDE estadoDocumento = determinarEstadoDesdeDescripcion(estado, codigo, protocolo);
        log.info("📊 Estado inicial del documento desde respuesta SIFEN: {}", estadoDocumento);
        
        // Verificar eventos si hay respuesta raw
        if (raw != null) {
            List<EventResult> eventos = SifenResponseParser.extractEventResults(raw);
            if (!eventos.isEmpty()) {
                log.info("📅 Se encontraron {} evento(s) asociado(s) al documento {}", eventos.size(), documento.getCdc());
                
                for (EventResult evento : eventos) {
                    log.info("   📅 Evento ID: {}, Tipo: {}, Estado: {}, Protocolo: {}, Mensaje: {}", 
                            evento.getId(), evento.getTipoEvento(), evento.getEstado(), evento.getProtocolo(), evento.getMensaje());
                    
                    // Verificar si es un evento de cancelación aprobado
                    boolean esCancelacion = "CANCELACION".equalsIgnoreCase(evento.getTipoEvento());
                    // Verificar si es un evento de nominación aprobado
                    boolean esNominacion = "NOMINACION".equalsIgnoreCase(evento.getTipoEvento());
                    
                    if (esCancelacion && "Aprobado".equalsIgnoreCase(evento.getEstado())) {
                        log.info("   🚫 Evento de cancelación APROBADO detectado - ID: {}, Protocolo: {}", 
                                evento.getId(), evento.getProtocolo());
                        
                        // Verificar si el evento existe en BD local
                        var eventoLocal = eventoCancelacionDERepository.findByEventoId(evento.getId());
                        if (eventoLocal.isPresent()) {
                            log.info("   ✅ Evento encontrado en BD local - actualizando estado");
                            var eventoCancelacion = eventoLocal.get();
                            if (evento.getProtocolo() != null && !evento.getProtocolo().isEmpty()) {
                                eventoCancelacion.setProtocoloAutorizacion(evento.getProtocolo());
                            }
                            if (evento.getCodigo() != null) {
                                eventoCancelacion.setCodigoRespuesta(evento.getCodigo());
                            }
                            if (evento.getMensaje() != null) {
                                eventoCancelacion.setMensajeRespuesta(evento.getMensaje());
                            }
                            eventoCancelacion.setEstado(com.frcefact.model.EstadoEvento.APROBADO);
                            eventoCancelacion.setFechaProcesamiento(LocalDateTime.now());
                            // No guardamos aquí porque el evento puede estar en otra transacción
                        } else {
                            log.warn("   ⚠️ Evento de cancelación aprobado encontrado en SIFEN pero no existe en BD local - ID: {}", 
                                    evento.getId());
                        }
                        
                        // Marcar el documento como CANCELADO independientemente de si existe en BD local
                        // porque SIFEN confirma que está cancelado
                        estadoDocumento = EstadoDE.CANCELADO;
                        log.info("   ✅ Documento marcado como CANCELADO por evento de cancelación aprobado");
                    } else if (esCancelacion) {
                        log.info("   ⚠️ Evento de cancelación encontrado pero no está aprobado - Estado: {}", evento.getEstado());
                    } else if (esNominacion && "Aprobado".equalsIgnoreCase(evento.getEstado())) {
                        log.info("   👤 Evento de nominación APROBADO detectado - ID: {}, Protocolo: {}", 
                                evento.getId(), evento.getProtocolo());
                        
                        // Verificar si el evento existe en BD local
                        var eventoLocal = eventoNominacionDERepository.findByEventoId(evento.getId());
                        if (eventoLocal.isPresent()) {
                            log.info("   ✅ Evento de nominación encontrado en BD local - actualizando estado");
                            var eventoNominacion = eventoLocal.get();
                            if (evento.getProtocolo() != null && !evento.getProtocolo().isEmpty()) {
                                eventoNominacion.setProtocoloAutorizacion(evento.getProtocolo());
                            }
                            if (evento.getCodigo() != null) {
                                eventoNominacion.setCodigoRespuesta(evento.getCodigo());
                            }
                            if (evento.getMensaje() != null) {
                                eventoNominacion.setMensajeRespuesta(evento.getMensaje());
                            }
                            eventoNominacion.setEstado(com.frcefact.model.EstadoEvento.APROBADO);
                            eventoNominacion.setFechaProcesamiento(LocalDateTime.now());
                            
                            // Actualizar la factura legal con el cliente nominado y sus datos
                            FacturaLegal factura = documento.getFacturaLegal();
                            if (factura != null && eventoNominacion.getCliente() != null) {
                                Cliente cliente = eventoNominacion.getCliente();
                                factura.setCliente(cliente);
                                factura.setNombre(cliente.getNombreCompleto());
                                factura.setRuc(cliente.getRuc());
                                facturaLegalRepository.save(factura);
                                log.info("   ✅ Factura Legal ID {} actualizada con cliente nominado - Nombre: {} - RUC: {}", 
                                        factura.getId(), factura.getNombre(), factura.getRuc());
                            }
                            // No guardamos el evento aquí porque puede estar en otra transacción
                        } else {
                            log.warn("   ⚠️ Evento de nominación aprobado encontrado en SIFEN pero no existe en BD local - ID: {}", 
                                    evento.getId());
                        }
                        
                        log.info("   ✅ Evento de nominación procesado - El documento mantiene su estado actual");
                    } else if (esNominacion) {
                        log.info("   ⚠️ Evento de nominación encontrado pero no está aprobado - Estado: {}", evento.getEstado());
                    }
                }
            } else {
                log.info("📅 No se encontraron eventos asociados al documento {} - El documento mantendrá su estado actual: {}", 
                        documento.getCdc(), estadoDocumento);
                log.info("   ℹ️ Esto es normal para documentos aprobados que no han sido cancelados, nominados o inutilizados");
            }
        } else {
            log.warn("⚠️ No hay respuesta raw disponible para verificar eventos");
        }

        log.info("📊 Estado final del documento después de procesar eventos: {}", estadoDocumento);
        actualizarEstadoDocumento(documento, estadoDocumento, codigo, mensaje, raw, protocolo);
    }

    private EstadoDE determinarEstadoDocumento(DocumentResult result) {
        return determinarEstadoDesdeDescripcion(result.getEstado(), result.getCodigo(), result.getProtocolo());
    }

    /**
     * Determina el estado del documento electrónico basándose en la respuesta de SIFEN.
     * 
     * @param estado Estado de resultado (dEstRes) - puede ser "Aprobado", "Rechazado", etc.
     * @param codigo Código de respuesta (dCodRes) - códigos como "0300", "0422", "0360", etc.
     * @param protocolo Protocolo de autorización (dProtAut) - si existe, indica que está aprobado
     * @return Estado del documento electrónico
     */
    private EstadoDE determinarEstadoDesdeDescripcion(String estado, String codigo, String protocolo) {
        String normalizedEstado = estado != null ? estado.toLowerCase() : "";
        
        // Si hay protocolo de autorización, es una señal fuerte de que está aprobado
        if (protocolo != null && !protocolo.isEmpty() && !"0".equals(protocolo)) {
            // Verificar primero si hay indicadores de cancelación o rechazo
            if (normalizedEstado.contains("cancel")) {
                return EstadoDE.CANCELADO;
            }
            if (normalizedEstado.contains("rechaz")) {
                return EstadoDE.RECHAZADO;
            }
            // Si hay protocolo y no hay indicadores negativos, está aprobado
            return EstadoDE.APROBADO;
        }
        
        // Verificar estado explícito
        if (normalizedEstado.contains("aprob")) {
            return EstadoDE.APROBADO;
        }
        if (normalizedEstado.contains("cancel")) {
            return EstadoDE.CANCELADO;
        }
        if (normalizedEstado.contains("rechaz")) {
            return EstadoDE.RECHAZADO;
        }
        
        // Verificar códigos de respuesta
        if ("0300".equalsIgnoreCase(codigo) || "0422".equalsIgnoreCase(codigo)) {
            // 0300: Aprobado (código común en consultas individuales)
            // 0422: CDC encontrado (éxito en consulta)
            return EstadoDE.APROBADO;
        }
        if ("0360".equalsIgnoreCase(codigo) || "0420".equalsIgnoreCase(codigo) || "0421".equalsIgnoreCase(codigo)) {
            // 0360: Lote no existe o no encontrado
            // 0420: Documento no existe o rechazado
            // 0421: Error en CDC (formato inválido)
            return EstadoDE.RECHAZADO;
        }
        if ("0361".equalsIgnoreCase(codigo)) {
            return EstadoDE.EN_PROCESO;
        }
        
        // Si no hay información suficiente, retornar ERROR solo si realmente hay un problema
        // Si el estado y código son null/vacíos pero venimos de una consulta exitosa, 
        // podría ser un documento aprobado sin eventos
        if ((estado == null || estado.isEmpty()) && (codigo == null || codigo.isEmpty())) {
            log.warn("⚠️ No se pudo determinar el estado: estado y código son null/vacíos");
            return EstadoDE.ERROR;
        }
        
        // Si hay código pero no coincide con ningún caso conocido, retornar ERROR
        log.warn("⚠️ No se pudo determinar el estado: estado='{}', código='{}', protocolo='{}'", 
                estado, codigo, protocolo);
        return EstadoDE.ERROR;
    }

    private void actualizarEstadoDocumento(DocumentoElectronico documento,
                                           EstadoDE nuevoEstado,
                                           String codigo,
                                           String mensaje,
                                           String respuesta,
                                           String protocolo) {
        EstadoDE estadoAnterior = documento.getEstado();
        documento.setEstado(nuevoEstado);
        documento.setCodigoRespuestaSifen(codigo);
        documento.setMensajeRespuestaSifen(mensaje);
        documento.setRespuestaSifen(respuesta);
        documento.setProtocoloAutorizacion(protocolo);
        documento.setFechaEstadoActualizado(LocalDateTime.now());

        if (EstadoDE.APROBADO.equals(nuevoEstado) && documento.getFechaRecepcionSifen() == null) {
            documento.setFechaRecepcionSifen(LocalDateTime.now());
        }
        
        // Guardar el documento
        documentoElectronicoRepository.save(documento);
        
        // Enviar email si el estado cambió a APROBADO
        if (nuevoEstado == EstadoDE.APROBADO && estadoAnterior != EstadoDE.APROBADO) {
            log.info("📧 Estado cambió a APROBADO para DE ID: {}, enviando email al cliente...", documento.getId());
            try {
                emailFacturaElectronicaService.enviarFacturaAlClienteAsync(documento);
            } catch (Exception e) {
                log.error("❌ Error al enviar email para DE {}: {}", documento.getId(), e.getMessage(), e);
                // No lanzar excepción para no afectar el flujo principal
            }
        }
    }

    /**
     * Crea un objeto DocumentoElectronico de SIFEN desde una factura y genera su XML.
     * Este método sigue el patrón del proyecto de referencia, pero adaptado para múltiples empresas.
     * 
     * IMPORTANTE: Configura el contexto global de SIFEN dinámicamente según la empresa/timbrado
     * antes de generar el XML, ya que jsifenlib usa el contexto global internamente.
     * 
     * @param factura La factura legal con todos sus datos
     * @return Resultado con el objeto DE de SIFEN, CDC y XML generado
     * @throws BusinessException Si hay error en la construcción
     */
    public CrearDEResult crearDocumentoElectronicoSifen(FacturaLegal factura) throws BusinessException {
        log.info("📝 Creando Documento Electrónico de SIFEN para factura ID: {}", factura.getId());
        
        try {
            // 1. Obtener items de la factura (siguiendo patrón del proyecto de referencia)
            List<FacturaLegalItem> items = facturaLegalItemRepository.findByFacturaLegalId(factura.getId());
            if (items == null || items.isEmpty()) {
                throw new IllegalArgumentException("Factura sin items - no se puede crear DE");
            }
            
            // 2. Obtener configuración SIFEN para la empresa/timbrado
            Timbrado timbrado = factura.getTimbradoDetalle().getTimbrado();
            SifenConfig config = sifenConfigFactory.buildForTimbrado(timbrado.getId());
            
            log.debug("🔧 Configuración SIFEN obtenida para empresa {} (timbrado {})", 
                    factura.getEmpresa().getId(), timbrado.getId());
            
            // 3. Construir el objeto DE de SIFEN completo (no requiere contexto global)
            com.roshka.sifen.core.beans.DocumentoElectronico deSifen = 
                construirDEDesdeFactura(factura, items, config);
            
            log.debug("   ✅ Objeto DE de SIFEN construido correctamente");
            
            // Validar que el DE tenga todos los grupos requeridos
            validarDECompleto(deSifen, factura);
            
            // 4. Obtener CDC del objeto DE (no requiere contexto global)
            String cdc = deSifen.obtenerCDC();
            deSifen.setId(cdc);
            log.info("   CDC generado: {}", cdc);
            
            if (cdc == null || cdc.isBlank()) {
                log.error("❌ Error: No se pudo generar el CDC del documento electrónico");
                throw new BusinessException("No se pudo generar el CDC del documento electrónico. Verificar datos del documento.");
            }
            
            // 5. Generar XML original usando el contexto de generación
            // IMPORTANTE: Envolver en executeWithConfig para asegurar que el contexto global
            // esté configurado correctamente antes de generar el XML, ya que jsifenlib puede
            // usar el contexto global internamente durante la generación del XML.
            log.debug("   🔧 Preparando generación de XML con contexto SIFEN...");
            String xmlOriginal = execute(config, () -> {
                try {
                    com.roshka.sifen.internal.ctx.GenerationCtx ctx = 
                        com.roshka.sifen.internal.ctx.GenerationCtx.getDefaultFromConfig(config);
                    log.debug("   ✅ Contexto de generación creado correctamente");
                    
                    // Imprimir estructura completa del DE para debugging
                    imprimirEstructuraDE(deSifen);
                    
                    log.debug("   🔨 Llamando a generarXml()...");
                    // Log adicional para debug de CDC / Nota Técnica 13
                    SifenDocumentoLogger.logDatosCdc(deSifen, config);

                    String xml = deSifen.generarXml(ctx);
                    
                    if (xml == null) {
                        log.error("❌ Error: generarXml() retornó null");
                        log.error("   - Verificar que todos los campos requeridos del DE estén completos");
                        log.error("   - Verificar configuración SIFEN (certificado, CSC, ambiente)");
                        throw new BusinessException("No se pudo generar el XML del documento electrónico. generarXml() retornó null. Verificar configuración SIFEN y datos del documento.");
                    }
                    
                    if (xml.isBlank()) {
                        log.error("❌ Error: generarXml() retornó XML vacío");
                        throw new BusinessException("No se pudo generar el XML del documento electrónico. El XML generado está vacío.");
                    }
                    
                    log.info("   ✅ XML original generado ({} caracteres)", xml.length());
                    
                    // NOTA: dTotalGs es calculado automáticamente por la librería como dTotGralOpe × dTiCam
                    // después de aplicar el redondeo oficial (Resolución 314/2014 de SEDECO).
                    // Este comportamiento es correcto y aceptado por SIFEN, por lo que no necesitamos corregirlo.
                    
                    return xml;
                } catch (Exception e) {
                    log.error("❌ Excepción al generar XML: {}", e.getMessage(), e);
                    throw new BusinessException("Error al generar XML: " + e.getMessage(), e);
                }
            });
            
            // 6. Extraer URL QR del XML (si está disponible)
            String urlQr = null;
            try {
                urlQr = com.frcefact.sifen.util.SifenResponseParser.extractUrlQr(xmlOriginal);
                if (urlQr != null && !urlQr.isBlank()) {
                    log.info("   ✅ URL QR extraída del XML ({} caracteres)", urlQr.length());
                } else {
                    log.warn("   ⚠️ URL QR no encontrada en XML - continuando sin URL QR");
                }
            } catch (Exception e) {
                log.warn("   ⚠️ Error al extraer URL QR del XML: {} - continuando sin URL QR", e.getMessage());
            }
            
            return new CrearDEResult(deSifen, cdc, xmlOriginal, urlQr);
            
        } catch (Exception e) {
            log.error("❌ Error al crear DE de SIFEN para factura ID: {}: {}", factura.getId(), e.getMessage(), e);
            throw new BusinessException("Error al crear DE de SIFEN: " + e.getMessage());
        }
    }

    /**
     * Crea un objeto DocumentoElectronico de SIFEN desde una nota de crédito y genera su XML.
     * Similar a crearDocumentoElectronicoSifen para factura, pero para nota de crédito.
     * 
     * @param notaCredito La nota de crédito con todos sus datos
     * @return Resultado con el objeto DE de SIFEN, CDC y XML generado
     * @throws BusinessException Si hay error en la construcción
     */
    public CrearDEResult crearDocumentoElectronicoSifenDesdeNotaCredito(NotaCredito notaCredito) throws BusinessException {
        log.info("📝 Creando Documento Electrónico de SIFEN para nota de crédito ID: {}", notaCredito.getId());
        
        try {
            // 1. Obtener items de la nota de crédito
            List<NotaCreditoItem> items = notaCreditoItemRepository.findByNotaCreditoId(notaCredito.getId());
            if (items == null || items.isEmpty()) {
                throw new IllegalArgumentException("Nota de crédito sin items - no se puede crear DE");
            }
            
            // 2. Obtener configuración SIFEN para la empresa/timbrado
            Timbrado timbrado = notaCredito.getTimbradoDetalle().getTimbrado();
            SifenConfig config = sifenConfigFactory.buildForTimbrado(timbrado.getId());
            
            log.debug("🔧 Configuración SIFEN obtenida para empresa {} (timbrado {})", 
                    notaCredito.getEmpresa().getId(), timbrado.getId());
            
            // 3. Construir el objeto DE de SIFEN completo
            com.roshka.sifen.core.beans.DocumentoElectronico deSifen = 
                construirDEDesdeNotaCredito(notaCredito, items, config);
            
            log.debug("   ✅ Objeto DE de SIFEN construido correctamente");
            
            // Validar que el DE tenga todos los grupos requeridos
            validarDECompletoNotaCredito(deSifen, notaCredito);
            
            // 4. Obtener CDC del objeto DE
            String cdc = deSifen.obtenerCDC();
            deSifen.setId(cdc);
            log.info("   CDC generado: {}", cdc);
            
            if (cdc == null || cdc.isBlank()) {
                log.error("❌ Error: No se pudo generar el CDC del documento electrónico");
                throw new BusinessException("No se pudo generar el CDC del documento electrónico. Verificar datos del documento.");
            }
            
            // 5. Generar XML original usando el contexto de generación
            String xmlOriginal = execute(config, () -> {
                try {
                    com.roshka.sifen.internal.ctx.GenerationCtx ctx = 
                        com.roshka.sifen.internal.ctx.GenerationCtx.getDefaultFromConfig(config);
                    log.debug("   ✅ Contexto de generación creado correctamente");
                    
                    log.debug("   🔨 Llamando a generarXml()...");
                    String xml = deSifen.generarXml(ctx);
                    
                    if (xml == null || xml.isBlank()) {
                        log.error("❌ Error: generarXml() retornó XML vacío");
                        throw new BusinessException("No se pudo generar el XML del documento electrónico. El XML generado está vacío.");
                    }
                    
                    log.info("   ✅ XML original generado ({} caracteres)", xml.length());
                    return xml;
                } catch (Exception e) {
                    log.error("❌ Excepción al generar XML: {}", e.getMessage(), e);
                    throw new BusinessException("Error al generar XML: " + e.getMessage(), e);
                }
            });
            
            // 6. Extraer URL QR del XML (si está disponible)
            String urlQr = null;
            try {
                urlQr = com.frcefact.sifen.util.SifenResponseParser.extractUrlQr(xmlOriginal);
                if (urlQr != null && !urlQr.isBlank()) {
                    log.info("   ✅ URL QR extraída del XML ({} caracteres)", urlQr.length());
                } else {
                    log.warn("   ⚠️ URL QR no encontrada en XML - continuando sin URL QR");
                }
            } catch (Exception e) {
                log.warn("   ⚠️ Error al extraer URL QR del XML: {} - continuando sin URL QR", e.getMessage());
            }
            
            return new CrearDEResult(deSifen, cdc, xmlOriginal, urlQr);
            
        } catch (Exception e) {
            log.error("❌ Error al crear DE de SIFEN para nota de crédito ID: {}: {}", notaCredito.getId(), e.getMessage(), e);
            throw new BusinessException("Error al crear DE de SIFEN: " + e.getMessage());
        }
    }

    /**
     * Resultado de la creación de un DE de SIFEN.
     */
    public record CrearDEResult(
        com.roshka.sifen.core.beans.DocumentoElectronico deSifen,
        String cdc,
        String xmlOriginal,
        String urlQr
    ) {}

    /**
     * Construye un objeto DocumentoElectronico de jsifenlib directamente desde los datos de la factura.
     * Este método sigue el patrón del repositorio de referencia y construye todos los grupos necesarios.
     * 
     * @param factura La factura legal con todos sus datos
     * @param items Lista de items de la factura
     * @param config Configuración SIFEN (necesaria para algunos cálculos)
     * @return Objeto DocumentoElectronico de jsifenlib listo para enviar
     * @throws BusinessException Si hay error en la construcción
     */
    private com.roshka.sifen.core.beans.DocumentoElectronico construirDEDesdeFactura(
            FacturaLegal factura, List<FacturaLegalItem> items, SifenConfig config) throws BusinessException {
        
        log.debug("🔨 Construyendo DE desde FacturaLegal ID: {}", factura.getId());
        
        try {
            // Grupo A - Identificación del DE
            com.roshka.sifen.core.beans.DocumentoElectronico DE = 
                new com.roshka.sifen.core.beans.DocumentoElectronico();
            DE.setdFecFirma(factura.getFecha() != null ? factura.getFecha() : LocalDateTime.now());
            DE.setdSisFact((short) 1);

            // Grupo B - Operación del DE
            TgOpeDE gOpeDE = new TgOpeDE();
            gOpeDE.setiTipEmi(TTipEmi.NORMAL);
        String codigoSeguridad = xmlGeneratorService.generarCodigoSeguridad();
        gOpeDE.setdCodSeg(codigoSeguridad);
        log.debug("   Código de seguridad generado (dCodSeg): {}", codigoSeguridad);
            DE.setgOpeDE(gOpeDE);

            // Grupo C - Timbrado
            TgTimb gTimb = new TgTimb();
            gTimb.setiTiDE(TTiDE.FACTURA_ELECTRONICA);
            
            Timbrado timbrado = factura.getTimbradoDetalle().getTimbrado();
            gTimb.setdNumTim(Integer.parseInt(timbrado.getNumero().trim()));
            
            // Formatear código de establecimiento con padding de 3 dígitos
            String codEstablecimiento = factura.getTimbradoDetalle().getCodigoEstablecimientoFactura().trim();
            gTimb.setdEst(String.format("%03d", Integer.parseInt(codEstablecimiento)));
            
            // Formatear punto de expedición con padding de 3 dígitos
            String puntoExpedicion = factura.getTimbradoDetalle().getPuntoExpedicion().trim();
            gTimb.setdPunExp(String.format("%03d", Integer.parseInt(puntoExpedicion)));
            
            gTimb.setdNumDoc(String.format("%07d", factura.getNumeroFactura()));
            
            // Fecha de inicio del timbrado (requerida)
            if (timbrado.getFechaInicio() != null) {
                gTimb.setdFeIniT(timbrado.getFechaInicio());
            } else {
                log.warn("⚠️ Timbrado sin fecha de inicio - usando fecha actual");
                gTimb.setdFeIniT(LocalDateTime.now().toLocalDate());
            }
            
            DE.setgTimb(gTimb);

            // Grupo D - Datos Generales de la Operación
            TdDatGralOpe dDatGralOpe = new TdDatGralOpe();
            dDatGralOpe.setdFeEmiDE(factura.getFecha());

            TgOpeCom gOpeCom = new TgOpeCom();
            gOpeCom.setiTipTra(TTipTra.VENTA_MERCADERIA); // Por defecto, puede ajustarse según producto
            gOpeCom.setiTImp(TTImp.IVA);
            
            // Configurar moneda de operación
            String monedaExtranjera = factura.getMonedaExtranjera();
            BigDecimal cambio = factura.getCambio();
            
            if (monedaExtranjera != null && !monedaExtranjera.trim().isEmpty() && !monedaExtranjera.equals("PYG")) {
                // Moneda extranjera
                try {
                    CMondT moneda = CMondT.valueOf(monedaExtranjera.toUpperCase());
                    gOpeCom.setcMoneOpe(moneda);
                    // Tipo de cambio fijo (GLOBAL)
                    gOpeCom.setdCondTiCam(TdCondTiCam.GLOBAL);
                    if (cambio != null && cambio.compareTo(BigDecimal.ZERO) > 0) {
                        // SIFEN permite de 4 a 8 decimales. Usamos 6 para mayor precisión
                        gOpeCom.setdTiCam(cambio.setScale(6, RoundingMode.HALF_UP));
                    } else {
                        throw new BusinessException("Tipo de cambio es requerido y debe ser mayor a 0 para moneda extranjera");
                    }
                    log.debug("✅ Moneda extranjera configurada: {} con tipo de cambio: {}", monedaExtranjera, cambio);
                } catch (IllegalArgumentException e) {
                    log.error("❌ Código de moneda no válido: {}", monedaExtranjera);
                    throw new BusinessException("Código de moneda no válido: " + monedaExtranjera);
                }
            } else {
                // Moneda local (PYG)
                gOpeCom.setcMoneOpe(CMondT.PYG);
            }
            
            dDatGralOpe.setgOpeCom(gOpeCom);

            // Datos del Emisor
            TgEmis gEmis = construirDatosEmisor(factura);
            dDatGralOpe.setgEmis(gEmis);

            // Datos del Receptor
            TgDatRec gDatRec = construirDatosReceptor(factura);
            dDatGralOpe.setgDatRec(gDatRec);
            
            DE.setgDatGralOpe(dDatGralOpe);

            // Grupo E - Items y condiciones
            TgDtipDE gDtipDE = construirDatosItems(factura, items);
            DE.setgDtipDE(gDtipDE);

            // Grupo F - Totales (delegamos el cálculo a jsifenlib y aplicamos el fix de IVA si corresponde)
            // NOTA: La librería calculará automáticamente dTotalGs = dTotGralOpe × dTiCam
            // después de aplicar el redondeo oficial según Resolución 314/2014 de SEDECO.
            // Este comportamiento es correcto y aceptado por SIFEN.
            TgTotSub gTotSub = new TgTotSub();
            DE.setgTotSub(gTotSub);
            aplicarFixTotalesIVA(gTotSub);
            
            log.debug("✅ DE construido exitosamente desde factura ID: {}", factura.getId());
            log.debug("   - Items procesados: {}", items.size());
            log.debug("   - TgTotSub inicializado (jsifenlib calculará totales al generar el XML)");
            if (esMonedaExtranjera(factura)) {
                log.debug("   - Moneda extranjera: {} (cambio: {}), dTotalGs será calculado como dTotGralOpe × dTiCam después del redondeo oficial", 
                    factura.getMonedaExtranjera(), factura.getCambio());
            }
            
            return DE;
            
        } catch (Exception e) {
            log.error("❌ Error al construir DE desde factura ID: {}: {}", factura.getId(), e.getMessage(), e);
            throw new BusinessException("Error al construir DE desde factura: " + e.getMessage());
        }
    }

    /**
     * Construye los datos del emisor desde la factura.
     */
    private TgEmis construirDatosEmisor(FacturaLegal factura) {
        TgEmis gEmis = new TgEmis();
        
        Empresa empresa = factura.getEmpresa();
        
        // RUC del emisor (desde empresa)
        String rucCompleto = empresa.getRuc();
        String[] rucPartes = rucCompleto.split("-");
        gEmis.setdRucEm(rucPartes[0]);
        gEmis.setdDVEmi(rucPartes.length > 1 ? rucPartes[1] : "");
        
        // Tipo de contribuyente emisor (por defecto PJ, puede ajustarse según configuración)
        gEmis.setiTipCont(TiTipCont.PERSONA_JURIDICA);
        
        // Nombre y datos del emisor (desde empresa)
        gEmis.setdNomEmi(empresa.getRazonSocial());
        gEmis.setdDirEmi(factura.getTimbradoDetalle().getDireccion() != null 
                ? factura.getTimbradoDetalle().getDireccion() : "");
        gEmis.setdNumCas("0");
        gEmis.setdTelEmi(factura.getTimbradoDetalle().getTelefono() != null 
                ? factura.getTimbradoDetalle().getTelefono() : "");
        gEmis.setdEmailE(empresa.getEmail() != null ? empresa.getEmail() : "");
        
        // Datos geográficos - usando las tablas geográficas del proyecto
        // Relación: ciudad -> distrito -> departamento
        if (factura.getTimbradoDetalle().getCiudad() != null) {
            com.frcefact.model.Ciudad ciudad = factura.getTimbradoDetalle().getCiudad();
            
            // Obtener departamento desde la relación: ciudad -> distrito -> departamento
            if (ciudad.getDistrito() != null && ciudad.getDistrito().getDepartamento() != null) {
                com.frcefact.model.Departamento departamento = ciudad.getDistrito().getDepartamento();
                
                // Usar el nombre del departamento de la tabla para obtener el enum TDepartamento
                TDepartamento tdep = mapearDepartamento(departamento.getNombre());
                gEmis.setcDepEmi(tdep);
                log.debug("   Departamento desde tabla: {} (código: {}) -> {}", 
                        departamento.getNombre(), departamento.getCodigo(), tdep);
            } else {
                log.warn("⚠️ No se pudo obtener departamento desde ciudad - usando CAPITAL por defecto");
                gEmis.setcDepEmi(TDepartamento.CAPITAL);
            }
            
            // Código de ciudad desde la tabla (jsifenlib espera Integer)
            try {
                String codigoCiudad = ciudad.getCodigo();
                if (codigoCiudad != null && !codigoCiudad.isBlank()) {
                    gEmis.setcCiuEmi(Integer.parseInt(codigoCiudad));
                } else {
                    log.warn("⚠️ Código de ciudad vacío - usando 0 por defecto");
                    gEmis.setcCiuEmi(0);
                }
            } catch (NumberFormatException e) {
                log.warn("⚠️ Código de ciudad inválido '{}' - usando 0 por defecto: {}", 
                        ciudad.getCodigo(), e.getMessage());
                gEmis.setcCiuEmi(0);
            }
            
            // Nombre de ciudad desde la tabla
            gEmis.setdDesCiuEmi(ciudad.getNombre());
        } else {
            log.warn("⚠️ TimbradoDetalle sin ciudad - usando valores por defecto");
            gEmis.setcDepEmi(TDepartamento.CAPITAL);
            gEmis.setcCiuEmi(0);
            gEmis.setdDesCiuEmi("");
        }
        
        // Actividades económicas
        List<TgActEco> gActEcoList = construirActividadesEconomicas(empresa);
        gEmis.setgActEcoList(gActEcoList);
        
        return gEmis;
    }

    /**
     * Construye los datos del receptor desde la factura.
     */
    private TgDatRec construirDatosReceptor(FacturaLegal factura) {
        TgDatRec gDatRec = new TgDatRec();
        
        Cliente cliente = factura.getCliente();
        
        // Si no hay cliente, es innominado
        if (cliente == null) {
            gDatRec.setiNatRec(TiNatRec.NO_CONTRIBUYENTE);
            gDatRec.setiTiOpe(TiTiOpe.B2C);
            gDatRec.setiTipIDRec(TiTipDocRec.INNOMINADO);
            gDatRec.setdNumIDRec("0");
            gDatRec.setdNomRec("Sin Nombre");
            gDatRec.setcPaisRec(PaisType.PRY);
            return gDatRec;
        }
        
        // Determinar si es contribuyente o no
        boolean esContribuyente = cliente.requiereRuc();
        
        if (esContribuyente && cliente.getRuc() != null && !cliente.getRuc().isBlank()) {
            // Contribuyente
            gDatRec.setiNatRec(TiNatRec.CONTRIBUYENTE);
            gDatRec.setiTiOpe(TiTiOpe.B2B);
            
            // Tipo de contribuyente
            Integer tipoContribuyenteCodigo = cliente.getTipoContribuyenteCodigo();
            if (tipoContribuyenteCodigo != null) {
                gDatRec.setiTiContRec(tipoContribuyenteCodigo == 1 
                        ? TiTipCont.PERSONA_FISICA 
                        : TiTipCont.PERSONA_JURIDICA);
            } else {
                gDatRec.setiTiContRec(TiTipCont.PERSONA_FISICA); // Por defecto
            }
            
            // RUC y DV
            String rucCompleto = cliente.getRuc();
            String[] rucPartes = rucCompleto.split("-");
            gDatRec.setdRucRec(rucPartes[0]);
            if (rucPartes.length > 1) {
                gDatRec.setdDVRec(Short.parseShort(rucPartes[1]));
            }
            
            gDatRec.setiTipIDRec(TiTipDocRec.CEDULA_PARAGUAYA);
            gDatRec.setdNumIDRec(rucPartes[0]);
        } else {
            // No contribuyente
            gDatRec.setiNatRec(TiNatRec.NO_CONTRIBUYENTE);
            gDatRec.setiTiOpe(TiTiOpe.B2C);
            gDatRec.setiTipIDRec(TiTipDocRec.CEDULA_PARAGUAYA);
            
            // Número de documento (puede ser CI u otro)
            String documento = cliente.getRuc(); // En nuestro modelo, ruc puede contener CI también
            if (documento != null && !documento.isBlank()) {
                gDatRec.setdNumIDRec(documento.replaceAll("[^0-9]", ""));
            } else {
                gDatRec.setdNumIDRec("0");
            }
        }
        
        // Nombre del receptor
        gDatRec.setdNomRec(cliente.getNombreCompleto());
        gDatRec.setcPaisRec(PaisType.PRY);
        
        return gDatRec;
    }

    /**
     * Construye las actividades económicas del emisor.
     */
    private List<TgActEco> construirActividadesEconomicas(Empresa empresa) {
        List<TgActEco> gActEcoList = new ArrayList<>();
        
        // Actividad principal
        if (empresa.getCodActividadEconomicaPrincipal() != null 
                && !empresa.getCodActividadEconomicaPrincipal().isBlank()) {
            TgActEco gActEco = new TgActEco();
            gActEco.setcActEco(empresa.getCodActividadEconomicaPrincipal());
            gActEco.setdDesActEco(empresa.getDescActividadEconomicaPrincipal() != null 
                    ? empresa.getDescActividadEconomicaPrincipal() 
                    : "");
            gActEcoList.add(gActEco);
        }
        
        // Actividades secundarias (si existen)
        if (empresa.getListCodigoActividadEconomicaSecundaria() != null 
                && !empresa.getListCodigoActividadEconomicaSecundaria().isBlank()
                && empresa.getListDescripcionActividadEconomicaSecundaria() != null
                && !empresa.getListDescripcionActividadEconomicaSecundaria().isBlank()) {
            
            String[] codigosSecundarios = empresa.getListCodigoActividadEconomicaSecundaria().split(",");
            String[] descripcionesSecundarias = empresa.getListDescripcionActividadEconomicaSecundaria().split(",");
            
            for (int i = 0; i < codigosSecundarios.length && i < descripcionesSecundarias.length; i++) {
                TgActEco gActEcoSec = new TgActEco();
                gActEcoSec.setcActEco(codigosSecundarios[i].trim());
                gActEcoSec.setdDesActEco(descripcionesSecundarias[i].trim());
                gActEcoList.add(gActEcoSec);
            }
        }
        
        return gActEcoList;
    }

    /**
     * Construye los datos de items y condiciones de pago.
     */
    private TgDtipDE construirDatosItems(FacturaLegal factura, List<FacturaLegalItem> items) {
        TgDtipDE gDtipDE = new TgDtipDE();

        // Configuración de factura electrónica
        TgCamFE gCamFE = new TgCamFE();
        gCamFE.setiIndPres(TiIndPres.OPERACION_PRESENCIAL);
        gDtipDE.setgCamFE(gCamFE);

        // Condiciones de pago
        TgCamCond gCamCond = new TgCamCond();
        boolean esCredito = factura.getCredito() != null && factura.getCredito();
        gCamCond.setiCondOpe(esCredito ? TiCondOpe.CREDITO : TiCondOpe.CONTADO);

        List<TgPaConEIni> gPaConEIniList = new ArrayList<>();
        TgPaConEIni gPaConEIni = new TgPaConEIni();
        gPaConEIni.setiTiPago(TiTiPago.EFECTIVO);
        
        // Configurar moneda de pago según la moneda de operación
        String monedaExtranjera = factura.getMonedaExtranjera();
        BigDecimal cambio = factura.getCambio();
        
        // Configurar moneda de pago (PYG o extranjera)
        if (monedaExtranjera != null && !monedaExtranjera.trim().isEmpty() && !monedaExtranjera.equals("PYG")) {
            // Moneda extranjera: el monto de pago debe estar en la moneda extranjera
            try {
                CMondT moneda = CMondT.valueOf(monedaExtranjera.toUpperCase());
                gPaConEIni.setcMoneTiPag(moneda);
                
                // Configurar tipo de cambio del pago
                // IMPORTANTE: Cuando hay moneda extranjera, también debemos configurar dTiCamTiPag
                // Si no se configura, la librería intenta leer "null" como BigDecimal y falla
                if (cambio != null && cambio.compareTo(BigDecimal.ZERO) > 0) {
                    // SIFEN permite de 4 a 8 decimales. Usamos 6 para mayor precisión en conversiones
                    gPaConEIni.setdTiCamTiPag(cambio.setScale(6, RoundingMode.HALF_UP));
                }
            } catch (IllegalArgumentException e) {
                // Si hay error, usar PYG por defecto
                log.warn("Código de moneda no válido: {}, usando PYG por defecto", monedaExtranjera);
                gPaConEIni.setcMoneTiPag(CMondT.PYG);
            }
        } else {
            // Moneda local (PYG)
            gPaConEIni.setcMoneTiPag(CMondT.PYG);
        }
        
        // dMonTiPag: SIFEN permite MÁXIMO 4 decimales para montos de pago (campo específico)
        // IMPORTANTE: Si la factura tiene moneda extranjera, el totalFinal está en guaraníes
        // pero el monto del pago debe estar en la moneda extranjera
        // Calculamos con 6 decimales internamente para precisión, pero redondeamos a 4 para el campo
        BigDecimal montoPago;
        if (monedaExtranjera != null && !monedaExtranjera.trim().isEmpty() && !monedaExtranjera.equals("PYG") 
            && cambio != null && cambio.compareTo(BigDecimal.ZERO) > 0) {
            // Convertir monto de guaraníes a moneda extranjera con 6 decimales internamente para precisión
            BigDecimal montoGs = factura.getTotalFinal();
            BigDecimal montoPagoCalculado = montoGs.divide(cambio, 6, RoundingMode.HALF_UP);
            // Redondear a 4 decimales para cumplir con la restricción del campo dMonTiPag
            montoPago = montoPagoCalculado.setScale(4, RoundingMode.HALF_UP);
            log.debug("Monto de pago convertido: {} Gs → {} {} (tipo cambio: {}, calculado con 6 dec, redondeado a 4 dec)", 
                montoGs, montoPago, monedaExtranjera, cambio);
        } else {
            montoPago = normalizarMontoPago(factura.getTotalFinal());
        }
        gPaConEIni.setdMonTiPag(montoPago);
        
        gPaConEIniList.add(gPaConEIni);
        gCamCond.setgPaConEIniList(gPaConEIniList);
        
        if (esCredito) {
            TgPagCred gPagCred = new TgPagCred();
            gPagCred.setiCondCred(TiCondCred.PLAZO);
            gPagCred.setdPlazoCre("30 días");
            gCamCond.setgPagCred(gPagCred);
        }
        
        gDtipDE.setgCamCond(gCamCond);

        // Items
        List<TgCamItem> gCamItemList = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            FacturaLegalItem item = items.get(i);
            TgCamItem gCamItem = new TgCamItem();
            gCamItem.setdCodInt(String.format("%03d", i + 1));
            gCamItem.setdDesProSer(item.getDescripcion());

            // Determinar unidad de medida y cantidad según producto
            Producto producto = item.getProducto();
            BigDecimal cantidad;
            
            if (producto != null && producto.getBalanza() != null && producto.getBalanza()) {
                gCamItem.setcUniMed(TcUniMed.kg);
                cantidad = item.getCantidad().setScale(3, RoundingMode.HALF_UP);
            } else {
                gCamItem.setcUniMed(TcUniMed.UNI);
                cantidad = item.getCantidad().setScale(0, RoundingMode.HALF_UP);
            }
            gCamItem.setdCantProSer(cantidad);
            
            TgValorItem gValorItem = new TgValorItem();
            
            // IMPORTANTE: Si la factura tiene moneda extranjera, los precios de los items están en guaraníes
            // pero deben convertirse a la moneda extranjera antes de pasarlos a SIFEN
            // SIFEN espera que los valores de los items estén en la moneda de operación (extranjera)
            // SIFEN permite de 4 a 8 decimales para precios unitarios. Usamos 6 para mayor precisión
            BigDecimal precioUnitario;
            if (esMonedaExtranjera(factura) && cambio != null && cambio.compareTo(BigDecimal.ZERO) > 0) {
                // Convertir precio de guaraníes a moneda extranjera con 6 decimales para mayor precisión
                BigDecimal precioGs = item.getPrecioUnitario();
                precioUnitario = precioGs.divide(cambio, 6, RoundingMode.HALF_UP);
                log.debug("   Precio convertido: {} Gs → {} {} (tipo cambio: {})", 
                    precioGs, precioUnitario, monedaExtranjera, cambio);
            } else {
                // Moneda local (PYG) - usar precio directamente
                precioUnitario = item.getPrecioUnitario();
            }
            
            gValorItem.setdPUniProSer(precioUnitario);
            
            TgValorRestaItem gValorRestaItem = new TgValorRestaItem();
            gValorItem.setgValorRestaItem(gValorRestaItem);
            gCamItem.setgValorItem(gValorItem);

            // IVA
            TgCamIVA gCamIVA = new TgCamIVA();
            Integer iva = (producto != null && producto.getIva() != null) ? producto.getIva() : 10;
            
            switch (iva) {
                case 5:
                    gCamIVA.setiAfecIVA(TiAfecIVA.GRAVADO);
                    gCamIVA.setdPropIVA(BigDecimal.valueOf(100));
                    gCamIVA.setdTasaIVA(BigDecimal.valueOf(5));
                    // dBasExe no se setea - la librería lo maneja automáticamente (como en versión anterior)
                    break;
                case 0:
                    // Para items exentos, establecer dPropIVA y dTasaIVA para evitar NullPointerException
                    // La librería jsifenlib siempre intenta usar estos valores aunque el item sea exento
                    gCamIVA.setiAfecIVA(TiAfecIVA.EXENTO);
                    gCamIVA.setdPropIVA(BigDecimal.ZERO); // Porcentaje de gravado es 0% para exentos
                    gCamIVA.setdTasaIVA(BigDecimal.ZERO); // Tasa de IVA es 0% para exentos
                    break;
                case 10:
                default:
                    gCamIVA.setiAfecIVA(TiAfecIVA.GRAVADO);
                    gCamIVA.setdPropIVA(BigDecimal.valueOf(100));
                    gCamIVA.setdTasaIVA(BigDecimal.valueOf(10));
                    // dBasExe no se setea - la librería lo maneja automáticamente (como en versión anterior)
                    break;
            }
            gCamItem.setgCamIVA(gCamIVA);

            gCamItemList.add(gCamItem);
        }

        gDtipDE.setgCamItemList(gCamItemList);
        return gDtipDE;
    }

    /**
     * Construye un objeto DocumentoElectronico de jsifenlib directamente desde los datos de la nota de crédito.
     * Similar a construirDEDesdeFactura, pero adaptado para Nota de Crédito.
     * 
     * @param notaCredito La nota de crédito con todos sus datos
     * @param items Lista de items de la nota de crédito
     * @param config Configuración SIFEN (necesaria para algunos cálculos)
     * @return Objeto DocumentoElectronico de jsifenlib listo para enviar
     * @throws BusinessException Si hay error en la construcción
     */
    private com.roshka.sifen.core.beans.DocumentoElectronico construirDEDesdeNotaCredito(
            NotaCredito notaCredito, List<NotaCreditoItem> items, SifenConfig config) throws BusinessException {
        
        log.debug("🔨 Construyendo DE desde NotaCredito ID: {}", notaCredito.getId());
        
        try {
            // Grupo A - Identificación del DE
            com.roshka.sifen.core.beans.DocumentoElectronico DE = 
                new com.roshka.sifen.core.beans.DocumentoElectronico();
            DE.setdFecFirma(notaCredito.getFecha() != null ? notaCredito.getFecha() : LocalDateTime.now());
            DE.setdSisFact((short) 1);

            // Grupo B - Operación del DE
            TgOpeDE gOpeDE = new TgOpeDE();
            gOpeDE.setiTipEmi(TTipEmi.NORMAL);
            String codigoSeguridad = xmlGeneratorService.generarCodigoSeguridad();
            gOpeDE.setdCodSeg(codigoSeguridad);
            log.debug("   Código de seguridad generado (dCodSeg): {}", codigoSeguridad);
            DE.setgOpeDE(gOpeDE);

            // Grupo C - Timbrado (Nota de Crédito)
            TgTimb gTimb = new TgTimb();
            gTimb.setiTiDE(TTiDE.NOTA_DE_CREDITO_ELECTRONICA); // Tipo 5 = Nota de Crédito Electrónica
            
            Timbrado timbrado = notaCredito.getTimbradoDetalle().getTimbrado();
            String numTimbrado = timbrado.getNumero().trim();
            gTimb.setdNumTim(Integer.parseInt(numTimbrado));
            
            // Formatear código de establecimiento con padding de 3 dígitos
            String codEstablecimiento = notaCredito.getTimbradoDetalle().getCodigoEstablecimientoFactura().trim();
            String codEstFormateado = String.format("%03d", Integer.parseInt(codEstablecimiento));
            gTimb.setdEst(codEstFormateado);
            
            // Formatear punto de expedición con padding de 3 dígitos
            String puntoExpedicion = notaCredito.getTimbradoDetalle().getPuntoExpedicion().trim();
            String puntoExpFormateado = String.format("%03d", Integer.parseInt(puntoExpedicion));
            gTimb.setdPunExp(puntoExpFormateado);
            
            String numDocFormateado = String.format("%07d", notaCredito.getNumeroNotaCredito());
            gTimb.setdNumDoc(numDocFormateado);
            
            // Fecha de inicio del timbrado (requerida)
            LocalDate fechaInicioTimbrado;
            if (timbrado.getFechaInicio() != null) {
                fechaInicioTimbrado = timbrado.getFechaInicio();
                gTimb.setdFeIniT(fechaInicioTimbrado);
            } else {
                log.warn("⚠️ Timbrado sin fecha de inicio - usando fecha actual");
                fechaInicioTimbrado = LocalDateTime.now().toLocalDate();
                gTimb.setdFeIniT(fechaInicioTimbrado);
            }
            
            DE.setgTimb(gTimb);
            
            log.info("📋 Timbrado configurado para Nota de Crédito:");
            log.info("   - Tipo DE (iTiDE): {} ({})", TTiDE.NOTA_DE_CREDITO_ELECTRONICA.getVal(), TTiDE.NOTA_DE_CREDITO_ELECTRONICA.getDescripcion());
            log.info("   - Número Timbrado (dNumTim): {} (tipo: {})", numTimbrado, Integer.class.getSimpleName());
            log.info("   - Establecimiento (dEst): '{}' (original: '{}', longitud: {})", codEstFormateado, codEstablecimiento, codEstFormateado.length());
            log.info("   - Punto Expedición (dPunExp): '{}' (original: '{}', longitud: {})", puntoExpFormateado, puntoExpedicion, puntoExpFormateado.length());
            log.info("   - Número Documento (dNumDoc): '{}' (longitud: {})", numDocFormateado, numDocFormateado.length());
            log.info("   - Fecha Inicio Timbrado (dFeIniT): {} (tipo: LocalDate)", fechaInicioTimbrado);
            log.info("   - Fecha Emisión DE: {}", notaCredito.getFecha());
            
            // Validar que los campos tengan el formato correcto
            if (codEstFormateado.length() != 3) {
                log.error("❌ ERROR: dEst debe tener exactamente 3 caracteres, tiene: {}", codEstFormateado.length());
            }
            if (puntoExpFormateado.length() != 3) {
                log.error("❌ ERROR: dPunExp debe tener exactamente 3 caracteres, tiene: {}", puntoExpFormateado.length());
            }
            if (numDocFormateado.length() != 7) {
                log.error("❌ ERROR: dNumDoc debe tener exactamente 7 caracteres, tiene: {}", numDocFormateado.length());
            }

            // Grupo D - Datos Generales de la Operación
            TdDatGralOpe dDatGralOpe = new TdDatGralOpe();
            dDatGralOpe.setdFeEmiDE(notaCredito.getFecha());

            TgOpeCom gOpeCom = new TgOpeCom();
            gOpeCom.setiTipTra(TTipTra.VENTA_MERCADERIA);
            gOpeCom.setiTImp(TTImp.IVA);
            
            // Configurar moneda de operación
            String monedaExtranjera = notaCredito.getMonedaExtranjera();
            BigDecimal cambio = notaCredito.getCambio();
            
            if (monedaExtranjera != null && !monedaExtranjera.trim().isEmpty() && !monedaExtranjera.equals("PYG")) {
                try {
                    CMondT moneda = CMondT.valueOf(monedaExtranjera.toUpperCase());
                    gOpeCom.setcMoneOpe(moneda);
                    gOpeCom.setdCondTiCam(TdCondTiCam.GLOBAL);
                    if (cambio != null && cambio.compareTo(BigDecimal.ZERO) > 0) {
                        gOpeCom.setdTiCam(cambio.setScale(6, RoundingMode.HALF_UP));
                    } else {
                        throw new BusinessException("Tipo de cambio es requerido y debe ser mayor a 0 para moneda extranjera");
                    }
                    log.debug("✅ Moneda extranjera configurada: {} con tipo de cambio: {}", monedaExtranjera, cambio);
                } catch (IllegalArgumentException e) {
                    log.error("❌ Código de moneda no válido: {}", monedaExtranjera);
                    throw new BusinessException("Código de moneda no válido: " + monedaExtranjera);
                }
            } else {
                gOpeCom.setcMoneOpe(CMondT.PYG);
            }
            
            dDatGralOpe.setgOpeCom(gOpeCom);

            // Datos del Emisor (usar método existente pero adaptado)
            TgEmis gEmis = construirDatosEmisorNotaCredito(notaCredito);
            dDatGralOpe.setgEmis(gEmis);

            // Datos del Receptor (usar método existente pero adaptado)
            TgDatRec gDatRec = construirDatosReceptorNotaCredito(notaCredito);
            dDatGralOpe.setgDatRec(gDatRec);
            
            DE.setgDatGralOpe(dDatGralOpe);

            // Grupo E - Items y condiciones (específico para Nota de Crédito)
            TgDtipDE gDtipDE = construirDatosItemsNotaCredito(notaCredito, items);
            DE.setgDtipDE(gDtipDE);

            // Grupo H - Documento Asociado (obligatorio para Nota de Crédito)
            if (notaCredito.getFacturaLegal() == null) {
                throw new BusinessException("Nota de crédito debe tener una factura asociada");
            }
            
            FacturaLegal facturaAsociada = notaCredito.getFacturaLegal();
            List<TgCamDEAsoc> gCamDEAsocList = new ArrayList<>();
            TgCamDEAsoc gCamDEAsoc = new TgCamDEAsoc();
            
            // Buscar el DE de la factura asociada
            DocumentoElectronico deFactura = documentoElectronicoRepository.findByFacturaLegalId(facturaAsociada.getId())
                    .orElseThrow(() -> new BusinessException("La factura asociada no tiene un documento electrónico. " +
                            "La nota de crédito debe referenciar una factura electrónica."));
            
            if (deFactura.getCdc() == null || deFactura.getCdc().isBlank()) {
                throw new BusinessException("La factura asociada no tiene CDC. No se puede crear la nota de crédito.");
            }
            
            gCamDEAsoc.setiTipDocAso(TiTipDocAso.ELECTRONICO); // 1 = Electrónico
            String cdcFacturaAsociada = deFactura.getCdc();
            gCamDEAsoc.setdCdCDERef(cdcFacturaAsociada);
            gCamDEAsocList.add(gCamDEAsoc);
            DE.setgCamDEAsocList(gCamDEAsocList);
            
            log.info("📄 Documento asociado configurado:");
            log.info("   - Tipo documento asociado (iTipDocAso): {} ({})", TiTipDocAso.ELECTRONICO.getVal(), TiTipDocAso.ELECTRONICO.getDescripcion());
            log.info("   - CDC factura asociada (dCdCDERef): '{}' (longitud: {})", cdcFacturaAsociada, cdcFacturaAsociada != null ? cdcFacturaAsociada.length() : 0);
            
            // Validar que el CDC tenga el formato correcto (44 caracteres)
            if (cdcFacturaAsociada == null || cdcFacturaAsociada.length() != 44) {
                log.error("❌ ERROR: CDC de factura asociada debe tener exactamente 44 caracteres, tiene: {}", 
                    cdcFacturaAsociada != null ? cdcFacturaAsociada.length() : 0);
            }

            // Grupo F - Totales
            TgTotSub gTotSub = new TgTotSub();
            DE.setgTotSub(gTotSub);
            aplicarFixTotalesIVA(gTotSub);
            
            log.debug("✅ DE construido exitosamente desde nota de crédito ID: {}", notaCredito.getId());
            log.debug("   - Items procesados: {}", items.size());
            log.debug("   - Documento asociado: CDC {}", deFactura.getCdc());
            
            return DE;
            
        } catch (Exception e) {
            log.error("❌ Error al construir DE desde nota de crédito ID: {}: {}", notaCredito.getId(), e.getMessage(), e);
            throw new BusinessException("Error al construir DE desde nota de crédito: " + e.getMessage());
        }
    }

    /**
     * Construye los datos del emisor desde la nota de crédito.
     */
    private TgEmis construirDatosEmisorNotaCredito(NotaCredito notaCredito) {
        TgEmis gEmis = new TgEmis();
        
        Empresa empresa = notaCredito.getEmpresa();
        
        // RUC del emisor
        String rucCompleto = empresa.getRuc();
        String[] rucPartes = rucCompleto.split("-");
        gEmis.setdRucEm(rucPartes[0]);
        gEmis.setdDVEmi(rucPartes.length > 1 ? rucPartes[1] : "");
        
        gEmis.setiTipCont(TiTipCont.PERSONA_JURIDICA);
        gEmis.setdNomEmi(empresa.getRazonSocial());
        gEmis.setdDirEmi(notaCredito.getTimbradoDetalle().getDireccion() != null 
                ? notaCredito.getTimbradoDetalle().getDireccion() : "");
        gEmis.setdNumCas("0");
        gEmis.setdTelEmi(notaCredito.getTimbradoDetalle().getTelefono() != null 
                ? notaCredito.getTimbradoDetalle().getTelefono() : "");
        gEmis.setdEmailE(empresa.getEmail() != null ? empresa.getEmail() : "");
        
        // Datos geográficos
        if (notaCredito.getTimbradoDetalle().getCiudad() != null) {
            com.frcefact.model.Ciudad ciudad = notaCredito.getTimbradoDetalle().getCiudad();
            
            if (ciudad.getDistrito() != null && ciudad.getDistrito().getDepartamento() != null) {
                com.frcefact.model.Departamento departamento = ciudad.getDistrito().getDepartamento();
                TDepartamento tdep = mapearDepartamento(departamento.getNombre());
                gEmis.setcDepEmi(tdep);
            } else {
                gEmis.setcDepEmi(TDepartamento.CAPITAL);
            }
            
            try {
                String codigoCiudad = ciudad.getCodigo();
                if (codigoCiudad != null && !codigoCiudad.isBlank()) {
                    gEmis.setcCiuEmi(Integer.parseInt(codigoCiudad));
                } else {
                    gEmis.setcCiuEmi(0);
                }
            } catch (NumberFormatException e) {
                gEmis.setcCiuEmi(0);
            }
            
            gEmis.setdDesCiuEmi(ciudad.getNombre());
        } else {
            gEmis.setcDepEmi(TDepartamento.CAPITAL);
            gEmis.setcCiuEmi(0);
            gEmis.setdDesCiuEmi("");
        }
        
        // Actividades económicas
        List<TgActEco> gActEcoList = construirActividadesEconomicas(empresa);
        gEmis.setgActEcoList(gActEcoList);
        
        return gEmis;
    }

    /**
     * Construye los datos del receptor desde la nota de crédito.
     * Para Nota de Crédito, el receptor NO puede ser innominado según SIFEN.
     */
    private TgDatRec construirDatosReceptorNotaCredito(NotaCredito notaCredito) {
        TgDatRec gDatRec = new TgDatRec();
        
        // Usar datos del snapshot del cliente guardado en la nota
        String nombre = notaCredito.getNombre();
        String ruc = notaCredito.getRuc();
        
        if (nombre == null || nombre.isBlank() || ruc == null || ruc.isBlank()) {
            throw new BusinessException("Nota de crédito debe tener cliente identificado (nombre y RUC). No se permite receptor innominado.");
        }
        
        // Determinar si es contribuyente
        boolean esContribuyente = ruc != null && ruc.length() >= 6; // RUC tiene al menos 6 dígitos
        
        if (esContribuyente) {
            gDatRec.setiNatRec(TiNatRec.CONTRIBUYENTE);
            gDatRec.setiTiOpe(TiTiOpe.B2B);
            gDatRec.setiTiContRec(TiTipCont.PERSONA_JURIDICA);
            
            String[] rucPartes = ruc.split("-");
            gDatRec.setdRucRec(rucPartes[0]);
            if (rucPartes.length > 1) {
                gDatRec.setdDVRec(Short.parseShort(rucPartes[1]));
            }
            
            gDatRec.setiTipIDRec(TiTipDocRec.CEDULA_PARAGUAYA);
            gDatRec.setdNumIDRec(rucPartes[0]);
        } else {
            gDatRec.setiNatRec(TiNatRec.NO_CONTRIBUYENTE);
            gDatRec.setiTiOpe(TiTiOpe.B2C);
            gDatRec.setiTipIDRec(TiTipDocRec.CEDULA_PARAGUAYA);
            gDatRec.setdNumIDRec(ruc != null ? ruc.replaceAll("[^0-9]", "") : "0");
        }
        
        gDatRec.setdNomRec(nombre);
        gDatRec.setcPaisRec(PaisType.PRY);
        
        return gDatRec;
    }

    /**
     * Construye los datos de items y condiciones para Nota de Crédito.
     */
    private TgDtipDE construirDatosItemsNotaCredito(NotaCredito notaCredito, List<NotaCreditoItem> items) {
        TgDtipDE gDtipDE = new TgDtipDE();

        // Grupo específico de Nota de Crédito
        TgCamNCDE gCamNCDE = new TgCamNCDE();
        
        // Mapear motivo de emisión
        String motivoEmision = notaCredito.getMotivoEmision();
        TiMotEmi tiMotEmi = TiMotEmi.DEVOLUCION_Y_AJUSTES_DE_PRECIOS; // Por defecto
        
        if (motivoEmision != null && !motivoEmision.isBlank()) {
            try {
                // Intentar mapear el motivo de emisión al enum
                tiMotEmi = TiMotEmi.valueOf(motivoEmision.toUpperCase().replace(" ", "_"));
            } catch (IllegalArgumentException e) {
                log.warn("⚠️ Motivo de emisión '{}' no reconocido, usando DEVOLUCION_Y_AJUSTES_DE_PRECIOS por defecto", motivoEmision);
            }
        }
        
        gCamNCDE.setiMotEmi(tiMotEmi);
        // La descripción se genera automáticamente desde el enum TiMotEmi
        gDtipDE.setgCamNCDE(gCamNCDE);

        // NOTA: El documento asociado (gCamDEAsocList) se agrega directamente al objeto DocumentoElectronico
        // en el método construirDEDesdeNotaCredito, no aquí en TgDtipDE

        // Condiciones de pago (similar a factura)
        TgCamCond gCamCond = new TgCamCond();
        gCamCond.setiCondOpe(TiCondOpe.CONTADO); // Nota de crédito generalmente es contado

        List<TgPaConEIni> gPaConEIniList = new ArrayList<>();
        TgPaConEIni gPaConEIni = new TgPaConEIni();
        gPaConEIni.setiTiPago(TiTiPago.EFECTIVO);
        
        String monedaExtranjera = notaCredito.getMonedaExtranjera();
        BigDecimal cambio = notaCredito.getCambio();
        
        if (monedaExtranjera != null && !monedaExtranjera.trim().isEmpty() && !monedaExtranjera.equals("PYG")) {
            try {
                CMondT moneda = CMondT.valueOf(monedaExtranjera.toUpperCase());
                gPaConEIni.setcMoneTiPag(moneda);
                if (cambio != null && cambio.compareTo(BigDecimal.ZERO) > 0) {
                    gPaConEIni.setdTiCamTiPag(cambio.setScale(6, RoundingMode.HALF_UP));
                }
            } catch (IllegalArgumentException e) {
                gPaConEIni.setcMoneTiPag(CMondT.PYG);
            }
        } else {
            gPaConEIni.setcMoneTiPag(CMondT.PYG);
        }
        
        BigDecimal montoPago;
        if (monedaExtranjera != null && !monedaExtranjera.trim().isEmpty() && !monedaExtranjera.equals("PYG") 
            && cambio != null && cambio.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal montoGs = notaCredito.getTotalFinal();
            BigDecimal montoPagoCalculado = montoGs.divide(cambio, 6, RoundingMode.HALF_UP);
            montoPago = montoPagoCalculado.setScale(4, RoundingMode.HALF_UP);
        } else {
            montoPago = normalizarMontoPago(notaCredito.getTotalFinal());
        }
        gPaConEIni.setdMonTiPag(montoPago);
        
        gPaConEIniList.add(gPaConEIni);
        gCamCond.setgPaConEIniList(gPaConEIniList);
        gDtipDE.setgCamCond(gCamCond);

        // Items
        List<TgCamItem> gCamItemList = new ArrayList<>();
        String monedaExtranjeraItem = monedaExtranjera;
        BigDecimal cambioItem = cambio;
        
        for (int i = 0; i < items.size(); i++) {
            NotaCreditoItem item = items.get(i);
            TgCamItem gCamItem = new TgCamItem();
            gCamItem.setdCodInt(String.format("%03d", i + 1));
            gCamItem.setdDesProSer(item.getDescripcion());

            Producto producto = item.getProducto();
            BigDecimal cantidad;
            
            if (producto != null && producto.getBalanza() != null && producto.getBalanza()) {
                gCamItem.setcUniMed(TcUniMed.kg);
                cantidad = item.getCantidad().setScale(3, RoundingMode.HALF_UP);
            } else {
                gCamItem.setcUniMed(TcUniMed.UNI);
                cantidad = item.getCantidad().setScale(0, RoundingMode.HALF_UP);
            }
            gCamItem.setdCantProSer(cantidad);
            
            TgValorItem gValorItem = new TgValorItem();
            
            BigDecimal precioUnitario;
            if (monedaExtranjeraItem != null && !monedaExtranjeraItem.trim().isEmpty() && !monedaExtranjeraItem.equals("PYG") 
                && cambioItem != null && cambioItem.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal precioGs = item.getPrecioUnitario();
                precioUnitario = precioGs.divide(cambioItem, 6, RoundingMode.HALF_UP);
            } else {
                precioUnitario = item.getPrecioUnitario();
            }
            
            gValorItem.setdPUniProSer(precioUnitario);
            
            TgValorRestaItem gValorRestaItem = new TgValorRestaItem();
            if (item.getDescuento() != null && item.getDescuento().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal descuento;
                if (monedaExtranjeraItem != null && !monedaExtranjeraItem.trim().isEmpty() && !monedaExtranjeraItem.equals("PYG") 
                    && cambioItem != null && cambioItem.compareTo(BigDecimal.ZERO) > 0) {
                    descuento = item.getDescuento().divide(cambioItem, 6, RoundingMode.HALF_UP);
                } else {
                    descuento = item.getDescuento();
                }
                gValorRestaItem.setdDescItem(descuento);
            }
            gValorItem.setgValorRestaItem(gValorRestaItem);
            gCamItem.setgValorItem(gValorItem);

            // IVA
            TgCamIVA gCamIVA = new TgCamIVA();
            Integer iva = item.getIva() != null ? item.getIva() : 10;
            
            switch (iva) {
                case 5:
                    gCamIVA.setiAfecIVA(TiAfecIVA.GRAVADO);
                    gCamIVA.setdPropIVA(BigDecimal.valueOf(100));
                    gCamIVA.setdTasaIVA(BigDecimal.valueOf(5));
                    break;
                case 0:
                    gCamIVA.setiAfecIVA(TiAfecIVA.EXENTO);
                    gCamIVA.setdPropIVA(BigDecimal.ZERO);
                    gCamIVA.setdTasaIVA(BigDecimal.ZERO);
                    break;
                case 10:
                default:
                    gCamIVA.setiAfecIVA(TiAfecIVA.GRAVADO);
                    gCamIVA.setdPropIVA(BigDecimal.valueOf(100));
                    gCamIVA.setdTasaIVA(BigDecimal.valueOf(10));
                    break;
            }
            gCamItem.setgCamIVA(gCamIVA);

            gCamItemList.add(gCamItem);
        }

        gDtipDE.setgCamItemList(gCamItemList);
        return gDtipDE;
    }

    /**
     * Valida que el DE tenga todos los grupos requeridos antes de generar el XML (para Nota de Crédito).
     */
    private void validarDECompletoNotaCredito(com.roshka.sifen.core.beans.DocumentoElectronico de, NotaCredito notaCredito) {
        if (de == null) {
            throw new BusinessException("El objeto DE es null");
        }
        
        if (de.getgOpeDE() == null) {
            throw new BusinessException("Grupo B (gOpeDE) no está configurado");
        }
        
        if (de.getgTimb() == null) {
            throw new BusinessException("Grupo C (gTimb) no está configurado");
        }
        
        if (de.getgTimb().getiTiDE() != TTiDE.NOTA_DE_CREDITO_ELECTRONICA) {
            throw new BusinessException("El tipo de documento debe ser NOTA_DE_CREDITO_ELECTRONICA");
        }
        
        if (de.getgDatGralOpe() == null) {
            throw new BusinessException("Grupo D (gDatGralOpe) no está configurado");
        }
        
        if (de.getgDatGralOpe().getgEmis() == null) {
            throw new BusinessException("Datos del emisor (gEmis) no están configurados");
        }
        
        if (de.getgDatGralOpe().getgDatRec() == null) {
            throw new BusinessException("Datos del receptor (gDatRec) no están configurados");
        }
        
        if (de.getgDtipDE() == null) {
            throw new BusinessException("Grupo E (gDtipDE) no está configurado");
        }
        
        if (de.getgDtipDE().getgCamNCDE() == null) {
            throw new BusinessException("Grupo gCamNCDE (específico de Nota de Crédito) no está configurado");
        }
        
        if (de.getgCamDEAsocList() == null || de.getgCamDEAsocList().isEmpty()) {
            throw new BusinessException("Documento asociado (gCamDEAsocList) es obligatorio para Nota de Crédito");
        }
        
        if (de.getgDtipDE().getgCamItemList() == null || de.getgDtipDE().getgCamItemList().isEmpty()) {
            throw new BusinessException("No hay items en el documento electrónico");
        }
        
        if (de.getgTotSub() == null) {
            throw new BusinessException("Grupo F (gTotSub) no está configurado");
        }
        
        log.debug("   ✅ Validación de DE completa - todos los grupos están presentes");
    }

    /**
     * Valida que el DE tenga todos los grupos requeridos antes de generar el XML.
     */
    private void validarDECompleto(com.roshka.sifen.core.beans.DocumentoElectronico de, FacturaLegal factura) {
        if (de == null) {
            throw new BusinessException("El objeto DE es null");
        }
        
        if (de.getgOpeDE() == null) {
            throw new BusinessException("Grupo B (gOpeDE) no está configurado");
        }
        
        if (de.getgTimb() == null) {
            throw new BusinessException("Grupo C (gTimb) no está configurado");
        }
        
        if (de.getgDatGralOpe() == null) {
            throw new BusinessException("Grupo D (gDatGralOpe) no está configurado");
        }
        
        if (de.getgDatGralOpe().getgEmis() == null) {
            throw new BusinessException("Datos del emisor (gEmis) no están configurados");
        }
        
        if (de.getgDatGralOpe().getgDatRec() == null) {
            throw new BusinessException("Datos del receptor (gDatRec) no están configurados");
        }
        
        if (de.getgDtipDE() == null) {
            throw new BusinessException("Grupo E (gDtipDE) no está configurado");
        }
        
        if (de.getgDtipDE().getgCamItemList() == null || de.getgDtipDE().getgCamItemList().isEmpty()) {
            throw new BusinessException("No hay items en el documento electrónico");
        }
        
        if (de.getgTotSub() == null) {
            throw new BusinessException("Grupo F (gTotSub) no está configurado");
        }
        
        log.debug("   ✅ Validación de DE completa - todos los grupos están presentes");
    }

    /**
     * Verifica si la factura tiene moneda extranjera configurada.
     * 
     * @param factura La factura a verificar
     * @return true si tiene moneda extranjera (no es PYG ni null), false en caso contrario
     */
    private boolean esMonedaExtranjera(FacturaLegal factura) {
        String moneda = factura.getMonedaExtranjera();
        return moneda != null && !moneda.trim().isEmpty() && !moneda.equals("PYG");
    }

    /**
     * Normaliza un monto de pago. 
     * IMPORTANTE: El campo dMonTiPag en SIFEN permite MÁXIMO 4 decimales (restricción específica del esquema).
     * Solo se usa para campos de tipo "monto de pago" en moneda local (PYG).
     * 
     * @param valor El valor a normalizar
     * @return BigDecimal normalizado a 4 decimales
     */
    private BigDecimal normalizarMontoPago(BigDecimal valor) {
        if (valor == null) {
            throw new IllegalArgumentException("El monto de pago no puede ser null");
        }
        return valor.setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * Aplica fix para bug de totales IVA en la librería SIFEN.
     */
    private void aplicarFixTotalesIVA(TgTotSub totales) {
        try {
            // Fix para IVA 10%
            if (totales.getdIVA10() != null && totales.getdIVA10().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal valorIVA10 = totales.getdIVA10();
                BigDecimal valorActualLiq10 = totales.getdLiqTotIVA10();
                
                if (valorActualLiq10 == null || valorActualLiq10.compareTo(BigDecimal.ZERO) == 0) {
                    Field field = TgTotSub.class.getDeclaredField("dLiqTotIVA10");
                    field.setAccessible(true);
                    field.set(totales, valorIVA10);
                }
            }
            
            // Fix para IVA 5%
            if (totales.getdIVA5() != null && totales.getdIVA5().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal valorIVA5 = totales.getdIVA5();
                BigDecimal valorActualLiq5 = totales.getdLiqTotIVA5();
                
                if (valorActualLiq5 == null || valorActualLiq5.compareTo(BigDecimal.ZERO) == 0) {
                    Field field = TgTotSub.class.getDeclaredField("dLiqTotIVA5");
                    field.setAccessible(true);
                    field.set(totales, valorIVA5);
                }
            }
        } catch (Exception e) {
            log.error("Error al aplicar workaround de totales IVA: {}", e.getMessage());
        }
    }

    /**
     * Mapea el nombre del departamento (desde la tabla geográfica) al enum TDepartamento de jsifenlib.
     * 
     * @param nombreDepartamento Nombre del departamento desde la tabla geografia.departamento
     * @return Enum TDepartamento correspondiente de jsifenlib
     */
    private TDepartamento mapearDepartamento(String nombreDepartamento) {
        if (nombreDepartamento == null || nombreDepartamento.isBlank()) {
            log.warn("⚠️ Nombre de departamento vacío - usando CAPITAL por defecto");
            return TDepartamento.CAPITAL;
        }
        
        // Normalizar nombre (mayúsculas, sin acentos opcionales)
        String nombreNormalizado = nombreDepartamento.trim().toUpperCase()
                .replace("Á", "A")
                .replace("É", "E")
                .replace("Í", "I")
                .replace("Ó", "O")
                .replace("Ú", "U")
                .replace("Ñ", "N");
        
        switch (nombreNormalizado) {
            case "CAPITAL":
            case "ASUNCION":
            case "ASUNCIÓN":
                return TDepartamento.CAPITAL;
            case "CONCEPCION":
            case "CONCEPCIÓN":
                return TDepartamento.CONCEPCION;
            case "SAN PEDRO":
                return TDepartamento.SAN_PEDRO;
            case "CORDILLERA":
                return TDepartamento.CORDILLERA;
            case "GUAIRA":
            case "GUAIRÁ":
                return TDepartamento.GUAIRA;
            case "CAAGUAZU":
            case "CAAGUAZÚ":
                return TDepartamento.CAAGUAZU;
            case "CAAZAPA":
            case "CAAZAPÁ":
                return TDepartamento.CAAZAPA;
            case "ITAPUA":
            case "ITAPÚA":
                return TDepartamento.ITAPUA;
            case "MISIONES":
                return TDepartamento.MISIONES;
            case "PARAGUARI":
            case "PARAGUARÍ":
                return TDepartamento.PARAGUARI;
            case "ALTO PARANA":
            case "ALTO PARANÁ":
                return TDepartamento.ALTO_PARANA;
            case "CENTRAL":
                return TDepartamento.CENTRAL;
            case "ÑEEMBUCU":
            case "ÑEEMBUCÚ":
            case "NEEMBUCU":
            case "NEEMBUCÚ":
                return TDepartamento.NEEMBUCU;
            case "AMAMBAY":
                return TDepartamento.AMAMBAY;
            case "CANINDEYU":
            case "CANINDEYÚ":
                return TDepartamento.CANINDEYU;
            case "PRESIDENTE HAYES":
            case "HAYES":
            case "PTE. HAYES":
                return TDepartamento.PTE_HAYES;
            case "BOQUERON":
            case "BOQUERÓN":
                return TDepartamento.BOQUERON;
            case "ALTO PARAGUAY":
                return TDepartamento.ALTO_PARAGUAY;
            default: 
                log.warn("⚠️ Departamento desconocido '{}' (normalizado: '{}') - usando CAPITAL por defecto", 
                        nombreDepartamento, nombreNormalizado);
                return TDepartamento.CAPITAL;
        }
    }

    /**
     * Imprime la estructura completa del DocumentoElectronico para debugging.
     * Útil para identificar campos faltantes o incorrectos antes de generar XML.
     */
    private void imprimirEstructuraDE(com.roshka.sifen.core.beans.DocumentoElectronico de) {
        log.info("═══════════════════════════════════════════════════════════════");
        log.info("📋 ESTRUCTURA COMPLETA DEL DOCUMENTO ELECTRÓNICO");
        log.info("═══════════════════════════════════════════════════════════════");
        
        try {
            // Grupo A - Identificación
            log.info("GRUPO A - Identificación:");
            log.info("  dFecFirma: {}", de.getdFecFirma());
            log.info("  dSisFact: {}", de.getdSisFact());
            
            // Grupo B - Operación
            if (de.getgOpeDE() != null) {
                log.info("GRUPO B - Operación:");
                log.info("  iTipEmi: {}", de.getgOpeDE().getiTipEmi());
            } else {
                log.error("  ❌ gOpeDE es NULL");
            }
            
            // Grupo C - Timbrado
            if (de.getgTimb() != null) {
                log.info("GRUPO C - Timbrado:");
                log.info("  iTiDE: {}", de.getgTimb().getiTiDE());
                log.info("  dNumTim: {}", de.getgTimb().getdNumTim());
                log.info("  dEst: '{}'", de.getgTimb().getdEst());
                log.info("  dPunExp: {}", de.getgTimb().getdPunExp());
                log.info("  dNumDoc: '{}'", de.getgTimb().getdNumDoc());
                log.info("  dFeIniT: {}", de.getgTimb().getdFeIniT());
            } else {
                log.error("  ❌ gTimb es NULL");
            }
            
            // Grupo D - Datos Generales
            if (de.getgDatGralOpe() != null) {
                log.info("GRUPO D - Datos Generales:");
                log.info("  dFeEmiDE: {}", de.getgDatGralOpe().getdFeEmiDE());
                
                // Operación Comercial
                if (de.getgDatGralOpe().getgOpeCom() != null) {
                    log.info("  gOpeCom:");
                    log.info("    iTipTra: {}", de.getgDatGralOpe().getgOpeCom().getiTipTra());
                    log.info("    iTImp: {}", de.getgDatGralOpe().getgOpeCom().getiTImp());
                    log.info("    cMoneOpe: {}", de.getgDatGralOpe().getgOpeCom().getcMoneOpe());
                } else {
                    log.error("    ❌ gOpeCom es NULL");
                }
                
                // Emisor
                if (de.getgDatGralOpe().getgEmis() != null) {
                    log.info("  gEmis (Emisor):");
                    log.info("    dRucEm: '{}'", de.getgDatGralOpe().getgEmis().getdRucEm());
                    log.info("    dDVEmi: '{}'", de.getgDatGralOpe().getgEmis().getdDVEmi());
                    log.info("    iTipCont: {}", de.getgDatGralOpe().getgEmis().getiTipCont());
                    log.info("    dNomEmi: '{}'", de.getgDatGralOpe().getgEmis().getdNomEmi());
                    log.info("    dDirEmi: '{}'", de.getgDatGralOpe().getgEmis().getdDirEmi());
                    log.info("    dNumCas: '{}'", de.getgDatGralOpe().getgEmis().getdNumCas());
                    log.info("    dTelEmi: '{}'", de.getgDatGralOpe().getgEmis().getdTelEmi());
                    log.info("    dEmailE: '{}'", de.getgDatGralOpe().getgEmis().getdEmailE());
                    log.info("    cDepEmi: {}", de.getgDatGralOpe().getgEmis().getcDepEmi());
                    log.info("    cCiuEmi: {}", de.getgDatGralOpe().getgEmis().getcCiuEmi());
                    log.info("    dDesCiuEmi: '{}'", de.getgDatGralOpe().getgEmis().getdDesCiuEmi());
                    if (de.getgDatGralOpe().getgEmis().getgActEcoList() != null) {
                        log.info("    gActEcoList: {} actividades", de.getgDatGralOpe().getgEmis().getgActEcoList().size());
                    } else {
                        log.warn("    ⚠️ gActEcoList es NULL");
                    }
                } else {
                    log.error("    ❌ gEmis es NULL");
                }
                
                // Receptor
                if (de.getgDatGralOpe().getgDatRec() != null) {
                    log.info("  gDatRec (Receptor):");
                    log.info("    iNatRec: {}", de.getgDatGralOpe().getgDatRec().getiNatRec());
                    log.info("    iTiOpe: {}", de.getgDatGralOpe().getgDatRec().getiTiOpe());
                    log.info("    iTiContRec: {}", de.getgDatGralOpe().getgDatRec().getiTiContRec());
                    log.info("    dRucRec: '{}'", de.getgDatGralOpe().getgDatRec().getdRucRec());
                    log.info("    dDVRec: '{}'", de.getgDatGralOpe().getgDatRec().getdDVRec());
                    log.info("    iTipIDRec: {}", de.getgDatGralOpe().getgDatRec().getiTipIDRec());
                    log.info("    dNumIDRec: '{}'", de.getgDatGralOpe().getgDatRec().getdNumIDRec());
                    log.info("    dNomRec: '{}'", de.getgDatGralOpe().getgDatRec().getdNomRec());
                    log.info("    cPaisRec: {}", de.getgDatGralOpe().getgDatRec().getcPaisRec());
                } else {
                    log.error("    ❌ gDatRec es NULL");
                }
            } else {
                log.error("  ❌ gDatGralOpe es NULL");
            }
            
            // Grupo E - Items
            if (de.getgDtipDE() != null) {
                log.info("GRUPO E - Items y Condiciones:");
                if (de.getgDtipDE().getgCamFE() != null) {
                    log.info("  gCamFE:");
                    log.info("    iIndPres: {}", de.getgDtipDE().getgCamFE().getiIndPres());
                }
                if (de.getgDtipDE().getgCamCond() != null) {
                    log.info("  gCamCond:");
                    log.info("    iCondOpe: {}", de.getgDtipDE().getgCamCond().getiCondOpe());
                }
                if (de.getgDtipDE().getgCamItemList() != null && !de.getgDtipDE().getgCamItemList().isEmpty()) {
                    log.info("  gCamItemList: {} items", de.getgDtipDE().getgCamItemList().size());
                    for (int i = 0; i < de.getgDtipDE().getgCamItemList().size(); i++) {
                        var item = de.getgDtipDE().getgCamItemList().get(i);
                        log.info("    Item {}:", i + 1);
                        log.info("      dCodInt: '{}'", item.getdCodInt());
                        log.info("      dDesProSer: '{}'", item.getdDesProSer());
                        log.info("      cUniMed: {}", item.getcUniMed());
                        log.info("      dCantProSer: {}", item.getdCantProSer());
                        if (item.getgValorItem() != null) {
                            log.info("      dPUniProSer: {}", item.getgValorItem().getdPUniProSer());
                        }
                        if (item.getgCamIVA() != null) {
                            log.info("      gCamIVA:");
                            log.info("        iAfecIVA: {}", item.getgCamIVA().getiAfecIVA());
                            log.info("        dPropIVA: {}", item.getgCamIVA().getdPropIVA());
                            log.info("        dTasaIVA: {}", item.getgCamIVA().getdTasaIVA());
                        }
                    }
                } else {
                    log.error("  ❌ gCamItemList es NULL o vacío");
                }
            } else {
                log.error("  ❌ gDtipDE es NULL");
            }
            
            // Grupo F - Totales
            if (de.getgTotSub() != null) {
                log.info("GRUPO F - Totales:");
                log.info("  dTotOpe: {}", de.getgTotSub().getdTotOpe());
                log.info("  dIVA10: {}", de.getgTotSub().getdIVA10());
                log.info("  dIVA5: {}", de.getgTotSub().getdIVA5());
                log.info("  dLiqTotIVA10: {}", de.getgTotSub().getdLiqTotIVA10());
                log.info("  dLiqTotIVA5: {}", de.getgTotSub().getdLiqTotIVA5());
                // Nota: Los totales gravados y exentos se calculan automáticamente por jsifenlib
            } else {
                log.error("  ❌ gTotSub es NULL");
            }
            
            log.info("═══════════════════════════════════════════════════════════════");
            
        } catch (Exception e) {
            log.error("❌ Error al imprimir estructura del DE: {}", e.getMessage(), e);
        }
    }

    private <T> T execute(SifenConfig config, SifenConfigFactory.SifenOperation<T> operation) {
        return sifenConfigFactory.executeWithConfig(config, operation);
    }
}