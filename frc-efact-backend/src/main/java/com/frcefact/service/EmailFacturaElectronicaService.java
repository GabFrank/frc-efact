package com.frcefact.service;

import com.frcefact.model.DocumentoElectronico;
import com.frcefact.model.FacturaLegal;
import com.frcefact.repository.FacturaLegalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Servicio para envío de emails de facturas electrónicas.
 * Envía el XML firmado y opcionalmente el PDF del KUDE al cliente.
 */
@Service
public class EmailFacturaElectronicaService {

    private static final Logger log = LoggerFactory.getLogger(EmailFacturaElectronicaService.class);

    private final EmailService emailService;
    private final DocumentoElectronicoService documentoElectronicoService;
    private final KudePdfService kudePdfService;
    private final FacturaLegalRepository facturaLegalRepository;

    public EmailFacturaElectronicaService(
            EmailService emailService,
            @Lazy DocumentoElectronicoService documentoElectronicoService,
            KudePdfService kudePdfService,
            FacturaLegalRepository facturaLegalRepository) {
        this.emailService = emailService;
        this.documentoElectronicoService = documentoElectronicoService;
        this.kudePdfService = kudePdfService;
        this.facturaLegalRepository = facturaLegalRepository;
    }

    /**
     * Envía la factura electrónica al cliente por email de forma asíncrona.
     * Incluye el XML firmado y opcionalmente el PDF del KUDE.
     * 
     * @param documentoElectronico Documento electrónico a enviar
     */
    @Async("emailExecutor")
    @Transactional(readOnly = true)
    public void enviarFacturaAlClienteAsync(DocumentoElectronico documentoElectronico) {
        try {
            log.info("📧 Iniciando envío de email para DE ID: {}", documentoElectronico.getId());
            
            // Obtener el ID de la factura desde el documento electrónico
            final Long facturaId;
            if (documentoElectronico.getFacturaLegal() != null) {
                facturaId = documentoElectronico.getFacturaLegal().getId();
            } else {
                log.warn("⚠️ El documento electrónico {} no tiene factura asociada", documentoElectronico.getId());
                return;
            }
            
            // Cargar la factura con todas sus relaciones dentro de una transacción
            // Esto es necesario porque el método es asíncrono y la sesión de Hibernate se cierra
            FacturaLegal factura = facturaLegalRepository.findById(facturaId)
                    .orElseThrow(() -> new RuntimeException("Factura no encontrada: " + facturaId));
            
            // Inicializar relaciones lazy necesarias para evitar LazyInitializationException
            // Esto es crítico porque el método es asíncrono y se ejecuta fuera de la sesión original
            if (factura.getEmpresa() != null) {
                // Inicializar empresa y sus campos necesarios
                factura.getEmpresa().getRazonSocial();
                factura.getEmpresa().getNombreFantasia();
                factura.getEmpresa().getRuc();
                factura.getEmpresa().getDireccion();
                factura.getEmpresa().getTelefono();
                factura.getEmpresa().getEmail();
                factura.getEmpresa().getDescActividadEconomicaPrincipal();
            }
            if (factura.getCliente() != null) {
                // Inicializar cliente y sus campos necesarios
                factura.getCliente().getNombre();
                factura.getCliente().getRazonSocial();
                factura.getCliente().getEmail();
                factura.getCliente().getRuc();
                factura.getCliente().getDireccion();
                factura.getCliente().getNumeroCasa();
                factura.getCliente().getTelefono();
                // Inicializar relaciones anidadas del cliente para el PDF
                if (factura.getCliente().getCiudad() != null) {
                    factura.getCliente().getCiudad().getNombre();
                    if (factura.getCliente().getCiudad().getDistrito() != null) {
                        factura.getCliente().getCiudad().getDistrito().getNombre();
                        if (factura.getCliente().getCiudad().getDistrito().getDepartamento() != null) {
                            factura.getCliente().getCiudad().getDistrito().getDepartamento().getNombre();
                        }
                    }
                }
            }
            if (factura.getItems() != null) {
                factura.getItems().size(); // Inicializar items
                // Inicializar productos de los items para el PDF
                factura.getItems().forEach(item -> {
                    if (item.getProducto() != null) {
                        item.getProducto().getCodigo();
                        item.getProducto().getIva();
                        item.getProducto().getUnidadMedida();
                    }
                });
            }
            if (factura.getTimbradoDetalle() != null) {
                // TimbradoDetalle es EAGER, pero inicializamos el timbrado anidado
                if (factura.getTimbradoDetalle().getTimbrado() != null) {
                    factura.getTimbradoDetalle().getTimbrado().getNumero();
                    factura.getTimbradoDetalle().getTimbrado().getFechaInicio();
                }
            }

            // Obtener el cliente
            com.frcefact.model.Cliente cliente = factura.getCliente();
            if (cliente == null) {
                log.warn("⚠️ La factura {} no tiene cliente asociado", factura.getId());
                return;
            }

            // Validar que el cliente tenga email
            String emailCliente = cliente.getEmail();
            if (emailCliente == null || emailCliente.isBlank()) {
                log.warn("⚠️ El cliente {} no tiene email configurado. No se puede enviar la factura.", cliente.getId());
                return;
            }

            log.info("   📬 Enviando email a: {}", emailCliente);

            // Preparar adjuntos
            Map<String, byte[]> attachments = new HashMap<>();
            
            // Obtener XML original (que contiene el XML firmado)
            // TODO: En el futuro, cuando se implemente el guardado de xml_firmado por separado,
            // se debería modificar esta lógica para intentar obtener xml_firmado primero
            // y usar xml_original solo como fallback. Actualmente xml_original contiene el XML firmado.
            String xmlParaAdjuntar = null;
            try {
                xmlParaAdjuntar = documentoElectronicoService.obtenerXmlOriginal(documentoElectronico.getId());
                if (xmlParaAdjuntar == null || xmlParaAdjuntar.isEmpty()) {
                    // Intentar obtener directamente del objeto
                    xmlParaAdjuntar = documentoElectronico.getXmlOriginal();
                }
            } catch (Exception e) {
                log.error("❌ No se pudo obtener XML original para DE ID: {}", 
                        documentoElectronico.getId(), e);
                return;
            }
            
            // Validar que tenemos XML para adjuntar
            if (xmlParaAdjuntar == null || xmlParaAdjuntar.isEmpty()) {
                log.error("❌ No se pudo obtener XML para adjuntar al email. DE ID: {}", documentoElectronico.getId());
                return;
            }
            
            // Agregar XML como adjunto
            String nombreXml = String.format("factura-%s.xml", factura.getNumeroFacturaFormateado());
            attachments.put(nombreXml, xmlParaAdjuntar.getBytes("UTF-8"));
            log.debug("   📎 XML agregado: {}", nombreXml);

            // Intentar generar y agregar PDF del KUDE (opcional)
            try {
                byte[] pdfBytes = kudePdfService.generarPdfKude(factura);
                if (pdfBytes != null && pdfBytes.length > 0) {
                    String nombrePdf = "KuDE-" + factura.getNumeroFacturaFormateado() + "-cdc.pdf";
                    attachments.put(nombrePdf, pdfBytes);
                    log.debug("   📎 PDF agregado: {}", nombrePdf);
                } else {
                    log.warn("⚠️ El PDF generado está vacío para la factura {}", factura.getId());
                }
            } catch (Exception e) {
                log.warn("⚠️ No se pudo generar el PDF del KUDE para la factura {}. Se enviará solo el XML.", 
                        factura.getId(), e);
                // Continuar sin el PDF
            }
            
            // Validar que al menos tengamos el XML como adjunto
            if (attachments.isEmpty()) {
                log.error("❌ No hay adjuntos disponibles para enviar. DE ID: {}", documentoElectronico.getId());
                return;
            }

            // Obtener datos para el email
            com.frcefact.model.Empresa empresa = factura.getEmpresa();
            String nombreEmpresa = empresa.getRazonSocial() != null ? empresa.getRazonSocial() : 
                (empresa.getNombreFantasia() != null ? empresa.getNombreFantasia() : "Empresa");
            
            String razonSocialCliente = cliente.getRazonSocial() != null ? cliente.getRazonSocial() : 
                (cliente.getNombre() != null ? cliente.getNombre() : "Cliente");
            
            String urlQr = documentoElectronico.getUrlQr() != null ? documentoElectronico.getUrlQr() : "";
            
            // Preparar asunto y cuerpo del email
            String subject = String.format("Factura Electronica de %s", nombreEmpresa);
            
            String bodyHtml = String.format(
                "<html><body style=\"font-family: Arial, sans-serif; line-height: 1.6; color: #333;\">" +
                "<p>Estimado cliente <strong>%s</strong>,</p>" +
                "<p>Se ha generado una factura electronica a su nombre desde la empresa <strong>%s</strong>. " +
                "Puedes acceder a la informacion del documento electronico haciendo click en el siguiente link:</p>" +
                "<p><a href=\"%s\" style=\"color: #1976d2; text-decoration: none; font-weight: bold;\">%s</a></p>" +
                "<p>A continuacion adjuntamos el pdf y xml correspondiente a la factura electronica.</p>" +
                "<br>" +
                "<p>Desde ya muchas gracias.<br>Att. Frc Efact</p>" +
                "</body></html>",
                razonSocialCliente,
                nombreEmpresa,
                urlQr,
                urlQr.isEmpty() ? "Ver documento electrónico" : urlQr
            );

            // Enviar email
            emailService.enviarEmailConAdjuntos(emailCliente, subject, bodyHtml, true, attachments);
            
            log.info("✅ Email enviado exitosamente para factura N° {}", factura.getNumeroFacturaFormateado());
            
        } catch (Exception e) {
            log.error("❌ Error al enviar email para DE ID: {}", documentoElectronico.getId(), e);
            // No lanzar excepción para no afectar el flujo principal
        }
    }
}

