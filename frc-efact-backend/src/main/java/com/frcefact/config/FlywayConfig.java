package com.frcefact.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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

    private static final Logger logger = LoggerFactory.getLogger(FlywayConfig.class);

    /**
     * Development migration strategy.
     * Executes repair to fix checksum mismatches (if enabled), then migrates.
     * This is useful when migration files have been modified after being applied.
     * 
     * @param repairEnabled Flag from application-dev.yml to enable/disable repair
     */
    @Bean
    @Profile("dev")
    public FlywayMigrationStrategy developmentMigrationStrategy(
            @Value("${spring.flyway.repair:false}") boolean repairEnabled) {
        return flyway -> {
            // Execute repair only if enabled in configuration
            if (repairEnabled) {
                try {
                    logger.info("🔧 Executing Flyway repair to fix checksum mismatches...");
                    flyway.repair();
                    logger.info("✅ Flyway repair completed successfully");
                } catch (Exception e) {
                    logger.warn("⚠️ Flyway repair encountered an issue (this is normal if no checksum mismatches exist): {}", e.getMessage());
                }
            } else {
                logger.debug("⏭️ Flyway repair is disabled (spring.flyway.repair=false)");
            }
            
            // Then, migrate to apply any pending migrations
            logger.info("🔄 Executing Flyway migrate...");
            flyway.migrate();
            logger.info("✅ Flyway migrate completed successfully");
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