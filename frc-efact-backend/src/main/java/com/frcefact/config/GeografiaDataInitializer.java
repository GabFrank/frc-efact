package com.frcefact.config;

import com.frcefact.service.GeografiaDataLoaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Inicializador que carga los datos geográficos al arrancar la aplicación.
 */
@Component
@Order(2) // Ejecutar después de las migraciones de base de datos
public class GeografiaDataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(GeografiaDataInitializer.class);

    private final GeografiaDataLoaderService geografiaDataLoaderService;

    public GeografiaDataInitializer(GeografiaDataLoaderService geografiaDataLoaderService) {
        this.geografiaDataLoaderService = geografiaDataLoaderService;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            logger.info("Verificando datos geográficos...");
            
            if (!geografiaDataLoaderService.datosGeograficosCargados()) {
                logger.info("Cargando datos geográficos de SIFEN...");
                geografiaDataLoaderService.cargarDatosGeograficos();
                logger.info("Datos geográficos cargados exitosamente");
            } else {
                logger.info("Los datos geográficos ya están disponibles");
            }
            
            // Mostrar estadísticas
            var stats = geografiaDataLoaderService.obtenerEstadisticas();
            logger.info("Estadísticas geográficas: {}", stats);
            
        } catch (Exception e) {
            logger.error("Error inicializando datos geográficos", e);
            // No lanzar excepción para no impedir el arranque de la aplicación
        }
    }
}