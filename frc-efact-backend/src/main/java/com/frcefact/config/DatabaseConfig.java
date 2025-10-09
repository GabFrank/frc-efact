package com.frcefact.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Database configuration for production environment.
 * Handles Render's DATABASE_URL format conversion to JDBC format.
 */
@Configuration
@Profile("prod")
public class DatabaseConfig {

    /**
     * Creates a DataSource from Render's DATABASE_URL environment variable.
     * Converts from: postgresql://user:password@host:port/database
     * To JDBC format: jdbc:postgresql://host:port/database
     * 
     * @return Configured DataSource
     */
    @Bean
    @Primary
    public DataSource dataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");
        
        if (databaseUrl == null || databaseUrl.isEmpty()) {
            throw new IllegalStateException("DATABASE_URL environment variable is not set");
        }

        try {
            URI dbUri = new URI(databaseUrl);
            
            String username = dbUri.getUserInfo().split(":")[0];
            String password = dbUri.getUserInfo().split(":")[1];
            String host = dbUri.getHost();
            int port = dbUri.getPort();
            String database = dbUri.getPath().substring(1); // Remove leading '/'
            
            // Build JDBC URL
            String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
            
            // Log connection info (without password)
            System.out.println("Connecting to database: " + jdbcUrl);
            System.out.println("Database user: " + username);
            
            return DataSourceBuilder
                    .create()
                    .url(jdbcUrl)
                    .username(username)
                    .password(password)
                    .driverClassName("org.postgresql.Driver")
                    .build();
                    
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Invalid DATABASE_URL format: " + databaseUrl, e);
        }
    }
}
