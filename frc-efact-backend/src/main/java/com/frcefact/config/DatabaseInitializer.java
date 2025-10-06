package com.frcefact.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Inicializador de base de datos para desarrollo.
 * Verifica si la base de datos existe y la crea si es necesario.
 * Solo se ejecuta en el perfil 'dev'.
 */
@Component
@Profile("dev")
public class DatabaseInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @PostConstruct
    public void initializeDatabase() {
        logger.info("Verificando existencia de la base de datos...");
        
        try {
            // Extraer información de la URL
            String[] urlParts = datasourceUrl.split("/");
            String databaseName = urlParts[urlParts.length - 1].split("\\?")[0];
            String baseUrl = datasourceUrl.substring(0, datasourceUrl.lastIndexOf("/"));
            
            // Conectar a la base de datos 'postgres' para verificar/crear la BD
            String postgresUrl = baseUrl + "/postgres";
            
            logger.info("Conectando a PostgreSQL en: {}", postgresUrl);
            
            try (Connection conn = DriverManager.getConnection(postgresUrl, username, password);
                 Statement stmt = conn.createStatement()) {
                
                // Verificar si la base de datos existe
                String checkDbQuery = String.format(
                    "SELECT 1 FROM pg_database WHERE datname = '%s'", 
                    databaseName
                );
                
                ResultSet rs = stmt.executeQuery(checkDbQuery);
                
                if (rs.next()) {
                    logger.info("La base de datos '{}' ya existe.", databaseName);
                } else {
                    logger.info("La base de datos '{}' no existe. Creándola...", databaseName);
                    
                    // Crear la base de datos
                    String createDbQuery = String.format("CREATE DATABASE %s", databaseName);
                    stmt.executeUpdate(createDbQuery);
                    
                    logger.info("Base de datos '{}' creada exitosamente.", databaseName);
                }
                
                rs.close();
                
            } catch (SQLException e) {
                logger.error("Error al verificar/crear la base de datos: {}", e.getMessage());
                throw new RuntimeException("No se pudo inicializar la base de datos", e);
            }
            
        } catch (Exception e) {
            logger.error("Error en la inicialización de la base de datos: {}", e.getMessage());
            throw new RuntimeException("Error fatal en la inicialización de la base de datos", e);
        }
    }
}
