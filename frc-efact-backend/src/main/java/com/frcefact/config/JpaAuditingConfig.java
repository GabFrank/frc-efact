package com.frcefact.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Configuración de auditoría JPA.
 * Habilita el seguimiento automático de cambios en entidades auditables.
 * 
 * Proporciona el usuario actual para los campos @CreatedBy y @LastModifiedBy
 * mediante la integración con Spring Security.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {

    /**
     * Proveedor de auditor que obtiene el usuario actual del contexto de seguridad.
     * 
     * @return AuditorAware que proporciona el username del usuario autenticado
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return new AuditorAwareImpl();
    }

    /**
     * Implementación de AuditorAware que extrae el username del contexto de seguridad.
     */
    public static class AuditorAwareImpl implements AuditorAware<String> {

        @Override
        public Optional<String> getCurrentAuditor() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated() 
                    || "anonymousUser".equals(authentication.getPrincipal())) {
                return Optional.of("SYSTEM");
            }

            return Optional.of(authentication.getName());
        }
    }
}
