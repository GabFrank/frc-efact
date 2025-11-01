package com.frcefact.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Inicializador de base de datos para desarrollo.
 * Verifica si la base de datos existe y la crea si es necesario.
 * También verifica y carga datos geográficos si es necesario.
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
                
                boolean dbExists = rs.next();
                rs.close();
                
                if (dbExists) {
                    logger.info("La base de datos '{}' ya existe.", databaseName);
                } else {
                    logger.info("La base de datos '{}' no existe. Creándola...", databaseName);
                    
                    // Crear la base de datos
                    String createDbQuery = String.format("CREATE DATABASE %s", databaseName);
                    stmt.executeUpdate(createDbQuery);
                    
                    logger.info("Base de datos '{}' creada exitosamente.", databaseName);
                }
                
            } catch (SQLException e) {
                logger.error("Error al verificar/crear la base de datos: {}", e.getMessage());
                throw new RuntimeException("No se pudo inicializar la base de datos", e);
            }
            
            // Verificar y cargar datos geográficos si es necesario
            checkAndLoadGeografiaData(databaseName, baseUrl);
            
        } catch (Exception e) {
            logger.error("Error en la inicialización de la base de datos: {}", e.getMessage());
            throw new RuntimeException("Error fatal en la inicialización de la base de datos", e);
        }
    }
    
    private void checkAndLoadGeografiaData(String databaseName, String baseUrl) {
        logger.info("Verificando datos geográficos en la base de datos...");
        
        try (Connection conn = DriverManager.getConnection(datasourceUrl, username, password);
             Statement stmt = conn.createStatement()) {
            
            // Verificar si hay datos en la tabla departamento
            String checkDepartamentoQuery = "SELECT COUNT(*) FROM geografia.departamento";
            ResultSet rs = stmt.executeQuery(checkDepartamentoQuery);
            int count = 0;
            if (rs.next()) {
                count = rs.getInt(1);
            }
            rs.close();
            
            if (count > 0) {
                logger.info("Los datos geográficos ya existen ({} departamentos encontrados).", count);
                return;
            }
            
            logger.info("No se encontraron datos geográficos. Cargando datos...");
            
            // Cargar archivos SQL en orden
            String[] sqlFiles = {
                "paises.sql",
                "departamentos.sql",
                "distritos.sql",
                "ciudades.sql",
                "barrios.sql"
            };
            
            for (String sqlFile : sqlFiles) {
                loadSqlFile(stmt, sqlFile);
            }
            
            logger.info("Datos geográficos cargados exitosamente.");
            
        } catch (SQLException e) {
            // Si la tabla no existe aún (migraciones no ejecutadas), no es un error
            if (e.getMessage() != null && e.getMessage().contains("does not exist")) {
                logger.debug("Las tablas de geografía aún no existen (migraciones pendientes).");
            } else {
                logger.error("Error al verificar/cargar datos geográficos: {}", e.getMessage());
            }
            // No lanzar excepción, solo loguear el error para no bloquear el inicio
        }
    }
    
    private void loadSqlFile(Statement stmt, String filename) throws SQLException {
        logger.info("Cargando archivo: {}", filename);
        
        try {
            Resource resource = new ClassPathResource(filename);
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), "UTF-8"))) {
                
                String line;
                int statementCount = 0;
                int lineCount = 0;
                
                while ((line = reader.readLine()) != null) {
                    lineCount++;
                    String trimmedLine = line.trim();
                    
                    // Saltar líneas vacías y comentarios
                    if (trimmedLine.isEmpty() || trimmedLine.startsWith("--")) {
                        continue;
                    }
                    
                    // Ejecutar cada INSERT individualmente
                    try {
                        stmt.execute(trimmedLine);
                        statementCount++;
                    } catch (SQLException e) {
                        // Log del error pero continuar con las siguientes líneas
                        logger.debug("Error al ejecutar línea {} de '{}': {}", lineCount, filename, e.getMessage());
                        // Continuar con el siguiente INSERT
                    }
                }
                
                logger.info("Archivo '{}' cargado exitosamente ({} statements ejecutados de {} líneas).", 
                    filename, statementCount, lineCount);
                
            } catch (Exception e) {
                logger.error("Error al leer el archivo '{}': {}", filename, e.getMessage());
                throw new SQLException("Error al cargar archivo SQL: " + filename, e);
            }
            
        } catch (Exception e) {
            logger.error("Error al cargar el archivo '{}': {}", filename, e.getMessage());
            throw new SQLException("Error al cargar archivo SQL: " + filename, e);
        }
    }
}
