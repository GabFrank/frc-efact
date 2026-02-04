package com.frcefact.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * Configuración de validación de email.
 * Verifica que las variables de entorno necesarias estén configuradas.
 */
@Configuration
public class MailConfig {
    
    private static final Logger log = LoggerFactory.getLogger(MailConfig.class);
    
    @PostConstruct
    public void validateMailConfiguration() {
        String mailPassword = System.getenv("MAIL_PASSWORD");
        if (mailPassword == null || mailPassword.isBlank()) {
            log.warn("⚠️ MAIL_PASSWORD no está configurada. El envío de emails no funcionará.");
            log.warn("   Configura la variable de entorno MAIL_PASSWORD con la contraseña de aplicación de Gmail.");
        } else {
            log.info("✅ MAIL_PASSWORD configurada (longitud: {} caracteres)", mailPassword.length());
        }
    }
}

