package com.frcefact.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Database configuration for the FRC eFact application.
 * Spring Boot auto-configures HikariCP DataSource from application.yml properties.
 * This class is kept for future custom database configurations if needed.
 */
@Configuration
public class DatabaseConfig {

    /**
     * Development profile specific configuration.
     * Additional development-specific database settings can be added here.
     */
    @Configuration
    @Profile("dev")
    static class DevelopmentDatabaseConfig {
        // Development-specific database configuration if needed
    }

    /**
     * Production profile specific configuration.
     * Additional production-specific database settings can be added here.
     */
    @Configuration
    @Profile("prod")
    static class ProductionDatabaseConfig {
        // Production-specific database configuration if needed
    }
}