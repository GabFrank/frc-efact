package com.frcefact.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Servicio base para envío de correos electrónicos.
 * Proporciona métodos para enviar emails simples y con adjuntos.
 */
@Service
public class EmailService {
    
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    
    private final JavaMailSender mailSender;
    private final String defaultFrom = "frcsistemasinformaticos@gmail.com";

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
        // Log de configuración (sin mostrar la contraseña completa)
        log.info("📧 EmailService inicializado - From: {}", defaultFrom);
        log.debug("📧 Configuración SMTP cargada correctamente");
    }

    /**
     * Envía un email simple sin adjuntos.
     * 
     * @param to Dirección de correo del destinatario
     * @param subject Asunto del correo
     * @param body Cuerpo del correo
     * @param isHtml Indica si el cuerpo es HTML
     */
    public void enviarEmailSimple(String to, String subject, String body, boolean isHtml) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, isHtml);
            helper.setFrom(defaultFrom);
            mailSender.send(message);
            log.info("✅ Email enviado exitosamente a: {}", to);
        } catch (MessagingException e) {
            log.error("❌ Error enviando email simple a: {}", to, e);
            throw new IllegalStateException("Error enviando email simple", e);
        }
    }

    /**
     * Envía un email con adjuntos.
     * 
     * @param to Dirección de correo del destinatario
     * @param subject Asunto del correo
     * @param body Cuerpo del correo
     * @param isHtml Indica si el cuerpo es HTML
     * @param attachments Mapa de adjuntos: nombre del archivo -> contenido en bytes
     */
    public void enviarEmailConAdjuntos(String to, String subject, String body, boolean isHtml, Map<String, byte[]> attachments) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, isHtml);
            helper.setFrom(defaultFrom);

            if (attachments != null && !attachments.isEmpty()) {
                for (Map.Entry<String, byte[]> entry : attachments.entrySet()) {
                    helper.addAttachment(entry.getKey(), new ByteArrayResource(entry.getValue()));
                    log.debug("   📎 Adjunto agregado: {}", entry.getKey());
                }
            }

            mailSender.send(message);
            log.info("✅ Email con adjuntos enviado exitosamente a: {}", to);
        } catch (MessagingException e) {
            log.error("❌ Error enviando email con adjuntos a: {}", to, e);
            throw new IllegalStateException("Error enviando email con adjuntos", e);
        }
    }
}

