package com.frcefact.service;

import com.frcefact.model.DocumentoElectronico;
import com.frcefact.model.NotaRemision;
import com.frcefact.repository.NotaRemisionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Servicio para envío de emails de notas de remisión electrónicas.
 */
@Service
public class EmailNotaRemisionService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotaRemisionService.class);

    private final EmailService emailService;
    private final DocumentoElectronicoService documentoElectronicoService;
    private final KudePdfService kudePdfService;
    private final NotaRemisionRepository notaRemisionRepository;

    public EmailNotaRemisionService(
            EmailService emailService,
            @Lazy DocumentoElectronicoService documentoElectronicoService,
            KudePdfService kudePdfService,
            NotaRemisionRepository notaRemisionRepository) {
        this.emailService = emailService;
        this.documentoElectronicoService = documentoElectronicoService;
        this.kudePdfService = kudePdfService;
        this.notaRemisionRepository = notaRemisionRepository;
    }

    /**
     * Envía la nota de remisión electrónica al cliente por email de forma asíncrona.
     */
    @Async("emailExecutor")
    @Transactional(readOnly = true)
    public void enviarNotaRemisionAlClienteAsync(DocumentoElectronico documentoElectronico, String emailDestino) {
        try {
            log.info("📧 Iniciando envío de email para DE ID: {} a {}", documentoElectronico.getId(), emailDestino);
            
            final Long notaRemisionId;
            if (documentoElectronico.getNotaRemision() != null) {
                notaRemisionId = documentoElectronico.getNotaRemision().getId();
            } else {
                log.warn("⚠️ El documento electrónico {} no tiene nota de remisión asociada", documentoElectronico.getId());
                return;
            }
            
            NotaRemision notaRemision = notaRemisionRepository.findById(notaRemisionId)
                    .orElseThrow(() -> new RuntimeException("Nota de remisión no encontrada: " + notaRemisionId));
            
            // Inicializar relaciones lazy
            if (notaRemision.getEmpresa() != null) {
                notaRemision.getEmpresa().getRazonSocial();
            }
            if (notaRemision.getCliente() != null) {
                notaRemision.getCliente().getNombre();
            }
            notaRemision.getItems().size();

            // Preparar adjuntos
            Map<String, byte[]> attachments = new HashMap<>();
            
            String xmlParaAdjuntar = null;
            try {
                xmlParaAdjuntar = documentoElectronicoService.obtenerXmlOriginal(documentoElectronico.getId());
                if (xmlParaAdjuntar == null || xmlParaAdjuntar.isEmpty()) {
                    xmlParaAdjuntar = documentoElectronico.getXmlOriginal();
                }
            } catch (Exception e) {
                log.error("❌ No se pudo obtener XML original para DE ID: {}", documentoElectronico.getId(), e);
                return;
            }
            
            if (xmlParaAdjuntar != null && !xmlParaAdjuntar.isEmpty()) {
                String nombreXml = String.format("nota-remision-%s.xml", notaRemision.getNumeroFormateado());
                attachments.put(nombreXml, xmlParaAdjuntar.getBytes("UTF-8"));
            }

            // PDF del KUDE
            try {
                byte[] pdfBytes = kudePdfService.generarPdfKude(notaRemision);
                if (pdfBytes != null && pdfBytes.length > 0) {
                    // Usar el mismo formato de nombre que en el controlador
                    String fechaFormateada = notaRemision.getFecha() != null ? 
                            notaRemision.getFecha().format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yy")) : 
                            "N/A";
                    String matricula = notaRemision.getVehiculoMatricula();
                    if (matricula == null || matricula.trim().isEmpty()) {
                        matricula = "SIN-MATRICULA";
                    }
                    matricula = matricula.replaceAll("[^a-zA-Z0-9\\-]", "-");
                    String cdc = documentoElectronico.getCdc();
                    String numero = notaRemision.getNumeroFormateado();
                    
                    String nombrePdf = String.format("NRE-%s-%s-%s-%s.pdf", numero, fechaFormateada, matricula, cdc);
                    attachments.put(nombrePdf, pdfBytes);
                }
            } catch (Exception e) {
                log.warn("⚠️ No se pudo generar el PDF para la nota {}. Se enviará solo el XML.", notaRemision.getId(), e);
            }
            
            if (attachments.isEmpty()) {
                log.error("❌ No hay adjuntos para enviar. DE ID: {}", documentoElectronico.getId());
                return;
            }

            // Datos para el email
            String nombreEmpresa = notaRemision.getEmpresa().getRazonSocial();
            String nombreDestinatario = notaRemision.getNombreDestinatario();
            String urlQr = documentoElectronico.getUrlQr() != null ? documentoElectronico.getUrlQr() : "";
            
            String subject = String.format("Nota de Remisión Electrónica de %s", nombreEmpresa);
            
            String bodyHtml = String.format(
                "<html><body style=\"font-family: Arial, sans-serif; line-height: 1.6; color: #333;\">" +
                "<p>Estimado/a <strong>%s</strong>,</p>" +
                "<p>Se ha generado una nota de remisión electrónica a su nombre desde la empresa <strong>%s</strong>. " +
                "Puede acceder a la información del documento electrónico haciendo clic en el siguiente enlace:</p>" +
                "<p><a href=\"%s\" style=\"color: #1976d2; text-decoration: none; font-weight: bold;\">%s</a></p>" +
                "<p>Adjuntamos el PDF y XML correspondientes.</p>" +
                "<br>" +
                "<p>Muchas gracias.<br>Atentamente, %s</p>" +
                "</body></html>",
                nombreDestinatario,
                nombreEmpresa,
                urlQr,
                urlQr.isEmpty() ? "Ver documento electrónico" : urlQr,
                nombreEmpresa
            );

            emailService.enviarEmailConAdjuntos(emailDestino, subject, bodyHtml, true, attachments);
            log.info("✅ Email enviado exitosamente para nota N° {}", notaRemision.getNumeroFormateado());
            
        } catch (Exception e) {
            log.error("❌ Error al enviar email para DE ID: {}", documentoElectronico.getId(), e);
        }
    }
}
