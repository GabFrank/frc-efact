package com.frcefact.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Configuración para habilitar AspectJ AOP en la aplicación.
 * Permite el uso de aspectos para funcionalidades transversales como auditoría.
 */
@Configuration
@EnableAspectJAutoProxy
public class AopConfig {
    // La anotación @EnableAspectJAutoProxy habilita el soporte para aspectos
}
