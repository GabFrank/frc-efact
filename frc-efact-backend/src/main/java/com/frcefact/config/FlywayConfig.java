package com.frcefact.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Flyway configuration for database migrations.
 * Provides custom migration strategies for different environments.
 */
@Configuration
public class FlywayConfig {

    /**
     * Development migration strategy.
     * Allows clean and migrate for development environment.
     */
    @Bean
    @Profile("dev")
    public FlywayMigrationStrategy developmentMigrationStrategy() {
        return flyway -> {
            // In development, we can clean and migrate if needed
            // This is useful for development but should NEVER be used in production
            flyway.migrate();
        };
    }

    /**
     * Production migration strategy.
     * Only allows migrate operation for production safety.
     */
    @Bean
    @Profile("prod")
    public FlywayMigrationStrategy productionMigrationStrategy() {
        return flyway -> {
            // In production, only migrate - never clean
            flyway.migrate();
        };
    }

    /**
     * Test migration strategy.
     * Allows clean and migrate for testing environment.
     */
    @Bean
    @Profile("test")
    public FlywayMigrationStrategy testMigrationStrategy() {
        return flyway -> {
            // In test environment, clean and migrate for fresh state
            flyway.clean();
            flyway.migrate();
        };
    }
}