package com.frcefact.service;

import com.frcefact.exception.BusinessException;
import com.frcefact.model.*;
import com.frcefact.repository.DocumentoElectronicoRepository;
import com.frcefact.repository.FacturaLegalRepository;
import com.roshka.sifen.Sifen;
import com.roshka.sifen.core.beans.response.RespuestaConsultaDE;
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
    private final XmlGeneratorService xmlGeneratorService;
    private final CertificadoService certificadoService;

    public DocumentoElectronicoService(
            DocumentoElectronicoRepository documentoElectronicoRepository,
            FacturaLegalRepository facturaLegalRepository,
            XmlGeneratorService xmlGeneratorService,
            CertificadoService certificadoService) {
        this.documentoElectronicoRepository = documentoElectronicoRepository;
        this.facturaLegalRepository = facturaLegalRepository;
        this.xmlGeneratorService = xmlGeneratorService;
        this.certificadoService = certificadoService;
    }

    /**
     * Genera un documento electrónico a partir de una factura legal.
     * 
     * Flujo:
     * 1. Validar que la empresa tiene certificado vigente
     * 2. Generar XML original
     * 3. Generar CDC
     * 4. Generar URL QR
     * 5. Guardar DE con estado PENDIENTE
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
        
        // Validar que la factura no tenga ya un DE
        if (documentoElectronicoRepository.existsByFacturaLegalId(facturaLegalId)) {
            throw new BusinessException("La factura ya tiene un documento electrónico asociado");
        }
        
        // Validar que la empresa tiene certificado vigente
        try {
            certificadoService.validarCertificadoVigente(factura.getEmpresa());
        } catch (BusinessException e) {
            log.error("❌ Certificado no válido para empresa ID: {}", factura.getEmpresa().getId());
            throw new BusinessException("No se puede generar DE: " + e.getMessage());
        }
        
        // Generar código de seguridad
        String codigoSeguridad = xmlGeneratorService.generarCodigoSeguridad();
        
        // Generar CDC
        String cdc = xmlGeneratorService.generarCDC(factura, codigoSeguridad);
        
        // Generar URL QR
        String urlQr = xmlGeneratorService.generarUrlQr(cdc);
        
        // Generar XML original
        String xmlOriginal = xmlGeneratorService.generarXmlOriginal(factura);
        
        // Crear documento electrónico
        DocumentoElectronico de = new DocumentoElectronico();
        de.setFacturaLegal(factura);
        de.setCdc(cdc);
        de.setUrlQr(urlQr);
        de.setNumeroDocumento(factura.getNumeroFacturaFormateado());
        de.setTipoDocumento("1"); // 1 = Factura electrónica
        de.setXmlOriginal(xmlOriginal);
        de.setEstado(EstadoDE.PENDIENTE);
        de.setFechaEmision(LocalDateTime.now());
        de.setActivo(true);
        
        // Guardar
        de = documentoElectronicoRepository.save(de);
        
        log.info("✅ Documento electrónico generado con CDC: {} para factura ID: {}", cdc, facturaLegalId);
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
        
        // Configurar SIFEN para la empresa del DE
        Long empresaId = de.getFacturaLegal().getEmpresa().getId();
        
        try {
            // Ejecutar consulta con el certificado de la empresa
            RespuestaConsultaDE respuesta = certificadoService.ejecutarConCertificado(
                empresaId,
                () -> Sifen.consultaDE(de.getCdc())
            );
            
            // Procesar respuesta
            if (respuesta != null) {
                // TODO: Implementar parsing completo de respuesta en tarea 11
                // Por ahora, solo registramos que se consultó
                log.info("✅ Consulta realizada para DE {}", deId);
                
                // La actualización del estado se hará cuando se implemente
                // el servicio de lotes en la tarea 10
            }
            
        } catch (Exception e) {
            log.error("❌ Error al consultar estado de DE {} en SIFEN", deId, e);
            de.setEstado(EstadoDE.ERROR);
            de.setMensajeRespuestaSifen("Error al consultar SIFEN: " + e.getMessage());
            documentoElectronicoRepository.save(de);
            throw new BusinessException("Error al consultar estado en SIFEN: " + e.getMessage());
        }
        
        return de;
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
            return documentoElectronicoRepository.findAll(pageable);
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
}
