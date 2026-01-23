package com.frcefact.service;

import com.frcefact.exception.BusinessException;
import com.frcefact.model.*;
import com.frcefact.repository.DocumentoElectronicoRepository;
import com.frcefact.repository.FacturaLegalRepository;
import com.frcefact.repository.NotaCreditoRepository;
import com.frcefact.repository.NotaRemisionRepository;
import com.frcefact.repository.LoteDERepository;
import com.frcefact.service.sifen.SifenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Servicio para gestión de documentos electrónicos.
 * Implementa generación de DEs, firma digital y consulta de estado en SIFEN.
 */
@Service
@Transactional
public class DocumentoElectronicoService {

    private static final Logger log = LoggerFactory.getLogger(DocumentoElectronicoService.class);

    private final DocumentoElectronicoRepository documentoElectronicoRepository;
    private final FacturaLegalRepository facturaLegalRepository;
    private final NotaCreditoRepository notaCreditoRepository;
    private final NotaRemisionRepository notaRemisionRepository;
    private final LoteDERepository loteDERepository;
    private final CertificadoService certificadoService;
    private final SifenService sifenService;
    private final EmailFacturaElectronicaService emailFacturaElectronicaService;

    public DocumentoElectronicoService(
            DocumentoElectronicoRepository documentoElectronicoRepository,
            FacturaLegalRepository facturaLegalRepository,
            NotaCreditoRepository notaCreditoRepository,
            NotaRemisionRepository notaRemisionRepository,
            LoteDERepository loteDERepository,
            CertificadoService certificadoService,
            SifenService sifenService,
            EmailFacturaElectronicaService emailFacturaElectronicaService) {
        this.documentoElectronicoRepository = documentoElectronicoRepository;
        this.facturaLegalRepository = facturaLegalRepository;
        this.notaCreditoRepository = notaCreditoRepository;
        this.notaRemisionRepository = notaRemisionRepository;
        this.loteDERepository = loteDERepository;
        this.certificadoService = certificadoService;
        this.sifenService = sifenService;
        this.emailFacturaElectronicaService = emailFacturaElectronicaService;
    }

    /**
     * Genera un documento electrónico a partir de una factura legal.
     * 
     * Flujo (siguiendo el patrón del proyecto de referencia):
     * 1. Validar que la empresa tiene certificado vigente
     * 2. Construir objeto DE de SIFEN completo desde datos de la factura
     * 3. Obtener CDC del objeto DE de SIFEN
     * 4. Generar XML original desde el objeto DE usando GenerationCtx
     * 5. Extraer URL QR del XML generado
     * 6. Guardar DE con estado PENDIENTE
     * 
     * NOTA: La firma digital se realiza cuando se envía el lote a SIFEN.
     * 
     * @param facturaLegalId ID de la factura legal
     * @return Documento electrónico generado
     */
    public DocumentoElectronico generarDE(Long facturaLegalId) {
        log.info("🔧 Generando documento electrónico para factura ID: {}", facturaLegalId);
        
        // Buscar factura legal
        FacturaLegal factura = facturaLegalRepository.findById(facturaLegalId)
                .orElseThrow(() -> new BusinessException("Factura legal no encontrada: " + facturaLegalId));
        
        // Validar que la factura no tenga ya un DE activo
        // Si tiene un DE con error permanente (ERROR, RECHAZADO), permitir reemplazarlo
        DocumentoElectronico deExistente = documentoElectronicoRepository.findByFacturaLegalId(facturaLegalId).orElse(null);
        if (deExistente != null) {
            // Si el DE existente está en un estado que permite reemplazo (errores permanentes)
            if (deExistente.getEstado() == EstadoDE.ERROR || deExistente.getEstado() == EstadoDE.RECHAZADO) {
                log.warn("⚠️ La factura ID: {} tiene un DE con error permanente (estado: {}). Eliminando para crear uno nuevo.", 
                    facturaLegalId, deExistente.getEstado());
                // Eliminar el DE anterior para permitir crear uno nuevo
                documentoElectronicoRepository.delete(deExistente);
                log.info("✅ DE anterior eliminado. Procediendo a crear uno nuevo.");
            } else {
                // Si el DE está en otro estado (PENDIENTE, ACEPTADO, etc.), no permitir reemplazo
                throw new BusinessException(
                    String.format("La factura ya tiene un documento electrónico asociado en estado: %s. " +
                        "Solo se puede reemplazar si el DE anterior tiene error permanente (ERROR o RECHAZADO).", 
                        deExistente.getEstado()));
            }
        }
        
        // Validar que la empresa tiene certificado vigente
        try {
            certificadoService.validarCertificadoVigente(factura.getEmpresa());
        } catch (BusinessException e) {
            log.error("❌ Certificado no válido para empresa ID: {}", factura.getEmpresa().getId());
            throw new BusinessException("No se puede generar DE: " + e.getMessage());
        }
        
        // Construir objeto DE de SIFEN y generar XML (siguiendo patrón del proyecto de referencia)
        SifenService.CrearDEResult resultado = sifenService.crearDocumentoElectronicoSifen(factura);
        
        // Crear documento electrónico en BD
        DocumentoElectronico de = new DocumentoElectronico();
        de.setFacturaLegal(factura);
        de.setCdc(resultado.cdc());
        de.setUrlQr(resultado.urlQr());
        de.setNumeroDocumento(factura.getNumeroFacturaFormateado());
        de.setTipoDocumento("1"); // 1 = Factura electrónica
        de.setXmlOriginal(resultado.xmlOriginal());
        de.setEstado(EstadoDE.PENDIENTE);
        de.setFechaEmision(LocalDateTime.now());
        de.setActivo(true);
        
        // Guardar
        de = documentoElectronicoRepository.save(de);
        
        log.info("✅ Documento electrónico generado con CDC: {} para factura ID: {}", resultado.cdc(), facturaLegalId);
        return de;
    }

    /**
     * Asocia un documento electrónico a un lote.
     * 
     * @param deId ID del documento electrónico
     * @param lote Lote al que se asociará
     */
    public void asociarALote(Long deId, LoteDE lote) {
        log.debug("🔗 Asociando DE {} a lote {}", deId, lote.getId());
        
        DocumentoElectronico de = documentoElectronicoRepository.findById(deId)
                .orElseThrow(() -> new BusinessException("Documento electrónico no encontrado: " + deId));
        
        if (de.getEstado() != EstadoDE.PENDIENTE) {
            throw new BusinessException("Solo se pueden asociar DEs en estado PENDIENTE a un lote");
        }
        
        de.setLoteDE(lote);
        de.setEstado(EstadoDE.EN_PROCESO);
        documentoElectronicoRepository.save(de);
        
        log.info("✅ DE {} asociado a lote {}", deId, lote.getId());
    }

    /**
     * Consulta y actualiza el estado de un documento electrónico en SIFEN.
     * 
     * IMPORTANTE: Requiere que el certificado de la empresa esté configurado en SIFEN.
     * 
     * NOTA: La implementación completa de este método se realizará en la tarea 11
     * cuando se implemente la integración completa con SIFEN.
     * 
     * @param deId ID del documento electrónico
     * @return Documento electrónico actualizado
     */
    @Transactional
    public DocumentoElectronico consultarYActualizarEstado(Long deId) {
        log.info("🔍 Consultando estado de DE ID: {}", deId);
        
        DocumentoElectronico de = documentoElectronicoRepository.findById(deId)
                .orElseThrow(() -> new BusinessException("Documento electrónico no encontrado: " + deId));
        
        if (de.getCdc() == null || de.getCdc().isEmpty()) {
            throw new BusinessException("El documento electrónico no tiene CDC generado");
        }
        
        return sifenService.consultarDocumento(de.getCdc());
    }

    /**
     * Busca un documento electrónico por ID.
     * 
     * @param id ID del documento electrónico
     * @return Documento electrónico
     */
    @Transactional(readOnly = true)
    public DocumentoElectronico findById(Long id) {
        return documentoElectronicoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Documento electrónico no encontrado: " + id));
    }

    /**
     * Busca un documento electrónico por CDC.
     * 
     * @param cdc Código de Control del Documento
     * @return Documento electrónico
     */
    @Transactional(readOnly = true)
    public DocumentoElectronico findByCdc(String cdc) {
        return documentoElectronicoRepository.findByCdc(cdc)
                .orElseThrow(() -> new BusinessException("Documento electrónico no encontrado con CDC: " + cdc));
    }

    /**
     * Lista documentos electrónicos con filtros.
     * 
     * @param estado Estado del documento (opcional)
     * @param empresaId ID de la empresa (opcional)
     * @param pageable Paginación
     * @return Página de documentos electrónicos
     */
    @Transactional(readOnly = true)
    public Page<DocumentoElectronico> listar(EstadoDE estado, Long empresaId, Pageable pageable) {
        if (estado != null && empresaId != null) {
            return documentoElectronicoRepository.findByEstadoAndFacturaLegal_Empresa_Id(estado, empresaId, pageable);
        } else if (estado != null) {
            return documentoElectronicoRepository.findByEstado(estado, pageable);
        } else if (empresaId != null) {
            return documentoElectronicoRepository.findByFacturaLegal_Empresa_Id(empresaId, pageable);
        } else {
            // Usar método con EntityGraph para cargar relaciones lazy
            return documentoElectronicoRepository.findAllWithRelations(pageable);
        }
    }

    /**
     * Obtiene el XML firmado de un documento electrónico.
     * 
     * @param id ID del documento electrónico
     * @return XML firmado
     */
    @Transactional(readOnly = true)
    public String obtenerXmlFirmado(Long id) {
        DocumentoElectronico de = findById(id);
        
        if (de.getXmlFirmado() == null || de.getXmlFirmado().isEmpty()) {
            throw new BusinessException("El documento electrónico no tiene XML firmado");
        }
        
        return de.getXmlFirmado();
    }

    /**
     * Obtiene el XML original de un documento electrónico.
     * 
     * @param id ID del documento electrónico
     * @return XML original
     */
    @Transactional(readOnly = true)
    public String obtenerXmlOriginal(Long id) {
        DocumentoElectronico de = findById(id);
        
        if (de.getXmlOriginal() == null || de.getXmlOriginal().isEmpty()) {
            throw new BusinessException("El documento electrónico no tiene XML original");
        }
        
        return de.getXmlOriginal();
    }

    /**
     * Actualiza el estado de un documento electrónico.
     * 
     * @param id ID del documento electrónico
     * @param nuevoEstado Nuevo estado
     * @param codigoRespuesta Código de respuesta de SIFEN (opcional)
     * @param mensajeRespuesta Mensaje de respuesta de SIFEN (opcional)
     */
    public void actualizarEstado(Long id, EstadoDE nuevoEstado, String codigoRespuesta, String mensajeRespuesta) {
        log.debug("🔄 Actualizando estado de DE {} a {}", id, nuevoEstado);
        
        DocumentoElectronico de = findById(id);
        EstadoDE estadoAnterior = de.getEstado();
        de.setEstado(nuevoEstado);
        
        if (codigoRespuesta != null) {
            de.setCodigoRespuestaSifen(codigoRespuesta);
        }
        
        if (mensajeRespuesta != null) {
            de.setMensajeRespuestaSifen(mensajeRespuesta);
        }
        
        if (nuevoEstado == EstadoDE.APROBADO && de.getFechaRecepcionSifen() == null) {
            de.setFechaRecepcionSifen(LocalDateTime.now());
        }
        
        documentoElectronicoRepository.save(de);
        log.info("✅ Estado de DE {} actualizado a {}", id, nuevoEstado);
        
        // Enviar email si el estado cambió a APROBADO
        if (nuevoEstado == EstadoDE.APROBADO && estadoAnterior != EstadoDE.APROBADO) {
            log.info("📧 Estado cambió a APROBADO, enviando email al cliente...");
            try {
                emailFacturaElectronicaService.enviarFacturaAlClienteAsync(de);
            } catch (Exception e) {
                log.error("❌ Error al enviar email para DE {}: {}", id, e.getMessage(), e);
                // No lanzar excepción para no afectar el flujo principal
            }
        }
    }

    /**
     * Actualiza el XML firmado de un documento electrónico.
     * 
     * @param id ID del documento electrónico
     * @param xmlFirmado XML firmado
     */
    public void actualizarXmlFirmado(Long id, String xmlFirmado) {
        log.debug("📝 Actualizando XML firmado de DE {}", id);
        
        DocumentoElectronico de = findById(id);
        de.setXmlFirmado(xmlFirmado);
        documentoElectronicoRepository.save(de);
        
        log.info("✅ XML firmado actualizado para DE {}", id);
    }

    /**
     * Genera un documento electrónico a partir de una factura legal, crea un nuevo lote,
     * asocia el documento al lote y envía el lote a SIFEN.
     *
     * @param facturaLegalId ID de la factura legal
     * @return Resultado con el documento y el lote procesado
     */
    public GenerarDeResult generarYEnviarDesdeFactura(Long facturaLegalId) {
        log.info("🧾 Generando y enviando DE para factura {}", facturaLegalId);

        FacturaLegal factura = facturaLegalRepository.findById(facturaLegalId)
                .orElseThrow(() -> new BusinessException("Factura legal no encontrada: " + facturaLegalId));

        DocumentoElectronico documento = documentoElectronicoRepository.findByFacturaLegalId(facturaLegalId)
                .orElse(null);

        if (documento == null || EstadoDE.ERROR.equals(documento.getEstado()) || EstadoDE.RECHAZADO.equals(documento.getEstado())) {
            documento = generarDE(facturaLegalId);
        } else if (!EstadoDE.PENDIENTE.equals(documento.getEstado())) {
            throw new BusinessException("La factura ya cuenta con un DE en estado " + documento.getEstado());
        }

        LoteDE lote = crearLoteParaFactura(factura);
        asociarALote(documento.getId(), lote);

        LoteDE loteProcesado = sifenService.enviarLote(lote.getId());
        DocumentoElectronico documentoActualizado = documentoElectronicoRepository.findById(documento.getId())
                .orElseThrow(() -> new BusinessException("No se pudo recuperar el documento electrónico generado"));

        return new GenerarDeResult(documentoActualizado, loteProcesado);
    }

    private LoteDE crearLoteParaFactura(FacturaLegal factura) {
        LoteDE lote = new LoteDE();
        lote.setEmpresa(factura.getEmpresa());
        lote.setEstado(EstadoLoteDE.PENDIENTE);
        return loteDERepository.save(lote);
    }

    private LoteDE crearLoteParaNotaCredito(NotaCredito notaCredito) {
        LoteDE lote = new LoteDE();
        lote.setEmpresa(notaCredito.getEmpresa());
        lote.setEstado(EstadoLoteDE.PENDIENTE);
        return loteDERepository.save(lote);
    }

    /**
     * Genera un documento electrónico a partir de una nota de crédito.
     * Similar a generarDE para factura, pero para nota de crédito.
     * 
     * @param notaCreditoId ID de la nota de crédito
     * @return Documento electrónico generado
     */
    public DocumentoElectronico generarDEDesdeNotaCredito(Long notaCreditoId) {
        log.info("🔧 Generando documento electrónico para nota de crédito ID: {}", notaCreditoId);
        
        // Buscar nota de crédito
        NotaCredito notaCredito = notaCreditoRepository.findById(notaCreditoId)
                .orElseThrow(() -> new BusinessException("Nota de crédito no encontrada: " + notaCreditoId));
        
        // Validar que la nota de crédito no tenga ya un DE activo
        DocumentoElectronico deExistente = documentoElectronicoRepository.findByNotaCreditoId(notaCreditoId).orElse(null);
        if (deExistente != null) {
            if (deExistente.getEstado() == EstadoDE.ERROR || deExistente.getEstado() == EstadoDE.RECHAZADO) {
                log.warn("⚠️ La nota de crédito ID: {} tiene un DE con error permanente (estado: {}). Eliminando para crear uno nuevo.", 
                    notaCreditoId, deExistente.getEstado());
                documentoElectronicoRepository.delete(deExistente);
                log.info("✅ DE anterior eliminado. Procediendo a crear uno nuevo.");
            } else {
                throw new BusinessException(
                    String.format("La nota de crédito ya tiene un documento electrónico asociado en estado: %s. " +
                        "Solo se puede reemplazar si el DE anterior tiene error permanente (ERROR o RECHAZADO).", 
                        deExistente.getEstado()));
            }
        }
        
        // Validar que la empresa tiene certificado vigente
        try {
            certificadoService.validarCertificadoVigente(notaCredito.getEmpresa());
        } catch (BusinessException e) {
            log.error("❌ Certificado no válido para empresa ID: {}", notaCredito.getEmpresa().getId());
            throw new BusinessException("No se puede generar DE: " + e.getMessage());
        }
        
        // Construir objeto DE de SIFEN y generar XML
        SifenService.CrearDEResult resultado = sifenService.crearDocumentoElectronicoSifenDesdeNotaCredito(notaCredito);
        
        // Crear documento electrónico en BD
        DocumentoElectronico de = new DocumentoElectronico();
        de.setNotaCredito(notaCredito);
        de.setCdc(resultado.cdc());
        de.setUrlQr(resultado.urlQr());
        de.setNumeroDocumento(notaCredito.getNumeroFormateado());
        de.setTipoDocumento("5"); // 5 = Nota de Crédito electrónica (según TTiDE enum)
        de.setXmlOriginal(resultado.xmlOriginal());
        de.setEstado(EstadoDE.PENDIENTE);
        de.setFechaEmision(LocalDateTime.now());
        de.setActivo(true);
        
        // Guardar
        de = documentoElectronicoRepository.save(de);
        
        log.info("✅ Documento electrónico generado con CDC: {} para nota de crédito ID: {}", resultado.cdc(), notaCreditoId);
        return de;
    }

    /**
     * Genera un documento electrónico a partir de una nota de remisión.
     * Similar a generarDE para factura, pero para nota de remisión (NRE).
     * 
     * @param notaRemisionId ID de la nota de remisión
     * @return Documento electrónico generado
     */
    public DocumentoElectronico generarDEDesdeNotaRemision(Long notaRemisionId) {
        log.info("🔧 Generando documento electrónico para nota de remisión ID: {}", notaRemisionId);
        
        // Buscar nota de remisión
        NotaRemision notaRemision = notaRemisionRepository.findById(notaRemisionId)
                .orElseThrow(() -> new BusinessException("Nota de remisión no encontrada: " + notaRemisionId));
        
        // Validar que la nota de remisión no tenga ya un DE activo
        DocumentoElectronico deExistente = documentoElectronicoRepository.findByNotaRemisionId(notaRemisionId).orElse(null);
        if (deExistente != null) {
            if (deExistente.getEstado() == EstadoDE.ERROR || deExistente.getEstado() == EstadoDE.RECHAZADO) {
                log.warn("⚠️ La nota de remisión ID: {} tiene un DE con error permanente (estado: {}). Eliminando para crear uno nuevo.", 
                    notaRemisionId, deExistente.getEstado());
                documentoElectronicoRepository.delete(deExistente);
                log.info("✅ DE anterior eliminado. Procediendo a crear uno nuevo.");
            } else {
                throw new BusinessException(
                    String.format("La nota de remisión ya tiene un documento electrónico asociado en estado: %s. " +
                        "Solo se puede reemplazar si el DE anterior tiene error permanente (ERROR o RECHAZADO).", 
                        deExistente.getEstado()));
            }
        }
        
        // Validar que la empresa tiene certificado vigente
        try {
            certificadoService.validarCertificadoVigente(notaRemision.getEmpresa());
        } catch (BusinessException e) {
            log.error("❌ Certificado no válido para empresa ID: {}", notaRemision.getEmpresa().getId());
            throw new BusinessException("No se puede generar DE: " + e.getMessage());
        }
        
        // Construir objeto DE de SIFEN y generar XML
        SifenService.CrearDEResult resultado = sifenService.crearDocumentoElectronicoSifenDesdeNotaRemision(notaRemision);
        
        // Crear documento electrónico en BD
        DocumentoElectronico de = new DocumentoElectronico();
        de.setNotaRemision(notaRemision);
        de.setCdc(resultado.cdc());
        de.setUrlQr(resultado.urlQr());
        de.setNumeroDocumento(notaRemision.getNumeroFormateado());
        de.setTipoDocumento("7"); // 7 = Nota de Remisión electrónica (según TTiDE enum)
        de.setXmlOriginal(resultado.xmlOriginal());
        de.setEstado(EstadoDE.PENDIENTE);
        de.setFechaEmision(LocalDateTime.now());
        de.setActivo(true);
        
        // Guardar
        de = documentoElectronicoRepository.save(de);
        
        log.info("✅ Documento electrónico generado con CDC: {} para nota de remisión ID: {}", resultado.cdc(), notaRemisionId);
        
        // Recargar el documento con todas las relaciones necesarias para evitar LazyInitializationException
        // Especialmente importante cargar la cadena geográfica completa: empresa -> ciudad -> distrito -> departamento -> pais
        DocumentoElectronico deConRelaciones = documentoElectronicoRepository
                .findByNotaRemisionIdWithRelations(notaRemisionId)
                .orElseThrow(() -> new BusinessException("No se pudo recuperar el documento electrónico generado con relaciones"));
        
        return deConRelaciones;
    }

    /**
     * Genera un documento electrónico a partir de una nota de remisión, crea un nuevo lote,
     * asocia el documento al lote y envía el lote a SIFEN.
     *
     * @param notaRemisionId ID de la nota de remisión
     * @return Resultado con el documento y el lote procesado
     */
    public GenerarDeResult generarYEnviarDesdeNotaRemision(Long notaRemisionId) {
        log.info("🧾 Generando y enviando DE para nota de remisión {}", notaRemisionId);

        NotaRemision notaRemision = notaRemisionRepository.findById(notaRemisionId)
                .orElseThrow(() -> new BusinessException("Nota de remisión no encontrada: " + notaRemisionId));

        DocumentoElectronico documento = documentoElectronicoRepository.findByNotaRemisionId(notaRemisionId)
                .orElse(null);

        if (documento == null || EstadoDE.ERROR.equals(documento.getEstado()) || EstadoDE.RECHAZADO.equals(documento.getEstado())) {
            documento = generarDEDesdeNotaRemision(notaRemisionId);
        } else if (!EstadoDE.PENDIENTE.equals(documento.getEstado())) {
            throw new BusinessException("La nota de remisión ya cuenta con un DE en estado " + documento.getEstado());
        }

        LoteDE lote = crearLoteParaNotaRemision(notaRemision);
        asociarALote(documento.getId(), lote);

        LoteDE loteProcesado = sifenService.enviarLote(lote.getId());
        // Cargar documento con todas las relaciones necesarias para evitar LazyInitializationException
        DocumentoElectronico documentoActualizado = documentoElectronicoRepository
                .findByNotaRemisionIdWithRelations(notaRemisionId)
                .orElseThrow(() -> new BusinessException("No se pudo recuperar el documento electrónico generado"));

        return new GenerarDeResult(documentoActualizado, loteProcesado);
    }

    private LoteDE crearLoteParaNotaRemision(NotaRemision notaRemision) {
        LoteDE lote = new LoteDE();
        lote.setEmpresa(notaRemision.getEmpresa());
        lote.setEstado(EstadoLoteDE.PENDIENTE);
        return loteDERepository.save(lote);
    }

    /**
     * Genera un documento electrónico a partir de una nota de crédito, crea un nuevo lote,
     * asocia el documento al lote y envía el lote a SIFEN.
     *
     * @param notaCreditoId ID de la nota de crédito
     * @return Resultado con el documento y el lote procesado
     */
    public GenerarDeResult generarYEnviarDesdeNotaCredito(Long notaCreditoId) {
        log.info("🧾 Generando y enviando DE para nota de crédito {}", notaCreditoId);

        NotaCredito notaCredito = notaCreditoRepository.findById(notaCreditoId)
                .orElseThrow(() -> new BusinessException("Nota de crédito no encontrada: " + notaCreditoId));

        DocumentoElectronico documento = documentoElectronicoRepository.findByNotaCreditoId(notaCreditoId)
                .orElse(null);

        if (documento == null || EstadoDE.ERROR.equals(documento.getEstado()) || EstadoDE.RECHAZADO.equals(documento.getEstado())) {
            documento = generarDEDesdeNotaCredito(notaCreditoId);
        } else if (!EstadoDE.PENDIENTE.equals(documento.getEstado())) {
            throw new BusinessException("La nota de crédito ya cuenta con un DE en estado " + documento.getEstado());
        }

        LoteDE lote = crearLoteParaNotaCredito(notaCredito);
        asociarALote(documento.getId(), lote);

        LoteDE loteProcesado = sifenService.enviarLote(lote.getId());
        DocumentoElectronico documentoActualizado = documentoElectronicoRepository.findById(documento.getId())
                .orElseThrow(() -> new BusinessException("No se pudo recuperar el documento electrónico generado"));

        return new GenerarDeResult(documentoActualizado, loteProcesado);
    }

    /**
     * Vincula un documento electrónico existente de una nota de remisión a un nuevo lote y lo envía a SIFEN.
     * Útil cuando el DE fue generado pero no se pudo enviar o no se creó el lote.
     *
     * @param notaRemisionId ID de la nota de remisión
     * @return Resultado con el documento y el lote procesado
     */
    public GenerarDeResult vincularALoteYEnviarDesdeNotaRemision(Long notaRemisionId) {
        log.info("🔗 Vinculando a lote y enviando DE para nota de remisión {}", notaRemisionId);

        NotaRemision notaRemision = notaRemisionRepository.findById(notaRemisionId)
                .orElseThrow(() -> new BusinessException("Nota de remisión no encontrada: " + notaRemisionId));

        DocumentoElectronico documento = documentoElectronicoRepository.findByNotaRemisionId(notaRemisionId)
                .orElseThrow(() -> new BusinessException("La nota de remisión no tiene un documento electrónico generado"));

        if (documento.getLoteDE() != null) {
            throw new BusinessException("El documento electrónico ya está vinculado al lote ID: " + documento.getLoteDE().getId());
        }

        // Si está en ERROR o RECHAZADO, permitimos reintentar vinculando a un nuevo lote
        // Si está en PENDIENTE, es el estado ideal para vincular
        if (!EstadoDE.PENDIENTE.equals(documento.getEstado()) && !EstadoDE.ERROR.equals(documento.getEstado()) && !EstadoDE.RECHAZADO.equals(documento.getEstado())) {
            throw new BusinessException("El documento electrónico debe estar en estado PENDIENTE, ERROR o RECHAZADO para vincularse a un lote. Estado actual: " + documento.getEstado());
        }

        // Si estaba en ERROR o RECHAZADO, volvemos a PENDIENTE para que asociarALote funcione
        if (!EstadoDE.PENDIENTE.equals(documento.getEstado())) {
            documento.setEstado(EstadoDE.PENDIENTE);
            documento = documentoElectronicoRepository.save(documento);
        }

        LoteDE lote = crearLoteParaNotaRemision(notaRemision);
        asociarALote(documento.getId(), lote);

        LoteDE loteProcesado = sifenService.enviarLote(lote.getId());
        
        // Cargar documento con todas las relaciones necesarias para evitar LazyInitializationException
        DocumentoElectronico documentoActualizado = documentoElectronicoRepository
                .findByNotaRemisionIdWithRelations(notaRemisionId)
                .orElseThrow(() -> new BusinessException("No se pudo recuperar el documento electrónico actualizado"));

        return new GenerarDeResult(documentoActualizado, loteProcesado);
    }

    public record GenerarDeResult(DocumentoElectronico documento, LoteDE lote) {
    }

    /**
     * Desvincula el documento electrónico de una factura.
     * Solo permite desvincular si el DE tiene error permanente (ERROR o RECHAZADO).
     * No permite desvincular si el DE está en APROBADO, CANCELADO u otros estados.
     * 
     * @param facturaLegalId ID de la factura legal
     * @throws BusinessException si el DE no existe, no tiene error permanente, o no se puede desvincular
     */
    public void desvincularDE(Long facturaLegalId) {
        log.info("🔗 Desvinculando DE de factura ID: {}", facturaLegalId);
        
        // Verificar que la factura existe
        if (!facturaLegalRepository.existsById(facturaLegalId)) {
            throw new BusinessException("Factura legal no encontrada: " + facturaLegalId);
        }
        
        // Buscar DE asociado
        DocumentoElectronico de = documentoElectronicoRepository.findByFacturaLegalId(facturaLegalId)
                .orElseThrow(() -> new BusinessException("La factura no tiene un documento electrónico asociado"));
        
        // Validar que el DE tenga un estado que permita desvinculación (solo ERROR o RECHAZADO)
        EstadoDE estado = de.getEstado();
        if (estado != EstadoDE.ERROR && estado != EstadoDE.RECHAZADO) {
            throw new BusinessException(
                String.format("No se puede desvincular el documento electrónico. " +
                    "Solo se permite desvincular DEs con error permanente (ERROR o RECHAZADO). " +
                    "El DE actual está en estado: %s", estado));
        }
        
        log.info("   ✅ DE tiene estado que permite desvinculación: {}", estado);
        
        // Desvincular del lote si está asociado
        if (de.getLoteDE() != null) {
            log.info("   🔗 Removiendo DE del lote ID: {}", de.getLoteDE().getId());
            de.setLoteDE(null);
        }
        
        // Eliminar el DE
        documentoElectronicoRepository.delete(de);
        log.info("   ✅ DE desvinculado y eliminado exitosamente");
    }
}
