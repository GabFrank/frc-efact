package com.frcefact.service;

import com.frcefact.model.*;
import com.frcefact.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Servicio para cargar datos geográficos desde archivos CSV.
 */
@Service
public class GeografiaDataLoaderService {

    private static final Logger logger = LoggerFactory.getLogger(GeografiaDataLoaderService.class);

    private final PaisRepository paisRepository;
    private final DepartamentoRepository departamentoRepository;
    private final DistritoRepository distritoRepository;
    private final CiudadRepository ciudadRepository;
    private final BarrioRepository barrioRepository;

    public GeografiaDataLoaderService(PaisRepository paisRepository,
                                     DepartamentoRepository departamentoRepository,
                                     DistritoRepository distritoRepository,
                                     CiudadRepository ciudadRepository,
                                     BarrioRepository barrioRepository) {
        this.paisRepository = paisRepository;
        this.departamentoRepository = departamentoRepository;
        this.distritoRepository = distritoRepository;
        this.ciudadRepository = ciudadRepository;
        this.barrioRepository = barrioRepository;
    }

    /**
     * Carga los datos geográficos desde el archivo CSV.
     */
    @Transactional
    public void cargarDatosGeograficos() {
        try {
            logger.info("Iniciando carga de datos geográficos...");

            // Verificar si ya existen datos
            long departamentosExistentes = departamentoRepository.count();
            if (departamentosExistentes > 0) {
                logger.warn("ATENCIÓN: Ya existen {} departamentos. Limpiando datos anteriores...", departamentosExistentes);
                limpiarDatosGeograficos();
                logger.info("Datos anteriores limpiados. Procediendo con carga fresca...");
            }

            // Obtener o crear Paraguay
            Pais paraguay = paisRepository.findByCodigo("PY")
                    .orElseGet(() -> paisRepository.save(new Pais("PY", "PARAGUAY")));

            // Mapas para cachear entidades y evitar duplicados
            Map<String, Departamento> departamentosMap = new HashMap<>();
            Map<String, Distrito> distritosMap = new HashMap<>();
            Map<String, Ciudad> ciudadesMap = new HashMap<>();

            // Leer archivo CSV
            ClassPathResource resource = new ClassPathResource("data/geografia_sifen.csv");
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                
                String line;
                boolean isFirstLine = true;
                int lineNumber = 0;
                int processedRecords = 0;
                int errorCount = 0;

                while ((line = reader.readLine()) != null) {
                    lineNumber++;
                    
                    // Saltar la primera línea (headers)
                    if (isFirstLine) {
                        isFirstLine = false;
                        continue;
                    }

                    try {
                        String[] campos = parseCsvLine(line);
                        
                        if (campos.length >= 8) {
                            procesarRegistroGeografico(campos, paraguay, departamentosMap, distritosMap, ciudadesMap);
                            processedRecords++;
                            
                            if (processedRecords % 100 == 0) {
                                logger.info("Procesados {} registros...", processedRecords);
                            }
                        } else {
                            logger.warn("Línea {} tiene formato incorrecto: {}", lineNumber, line);
                            errorCount++;
                        }
                    } catch (Exception e) {
                        logger.error("Error procesando línea {}: {}", lineNumber, line, e);
                        errorCount++;
                        
                        // Si hay demasiados errores, abortar
                        if (errorCount > 10) {
                            throw new RuntimeException("Demasiados errores procesando CSV. Abortando carga.", e);
                        }
                    }
                }

                logger.info("Carga completada. Total de registros procesados: {}, errores: {}", processedRecords, errorCount);
                
                // Mostrar estadísticas finales sin hacer consultas adicionales que puedan fallar
                logger.info("Datos cargados exitosamente en la base de datos");

            }
        } catch (Exception e) {
            logger.error("Error cargando datos geográficos", e);
            throw new RuntimeException("Error cargando datos geográficos", e);
        }
    }

    /**
     * Procesa un registro geográfico del CSV.
     */
    private void procesarRegistroGeografico(String[] campos, Pais pais,
                                          Map<String, Departamento> departamentosMap,
                                          Map<String, Distrito> distritosMap,
                                          Map<String, Ciudad> ciudadesMap) {
        
        // Extraer datos del CSV
        String codigoDepartamento = campos[0].trim();
        String nombreDepartamento = campos[1].trim().toUpperCase();
        String codigoDistrito = campos[2].trim();
        String nombreDistrito = campos[3].trim().toUpperCase();
        String codigoCiudad = campos[4].trim();
        String nombreCiudad = campos[5].trim().toUpperCase();
        String codigoBarrio = campos[6].trim();
        String nombreBarrio = campos[7].trim().toUpperCase();

        // 1. DEPARTAMENTO - Único por código a nivel país
        String keyDepartamento = codigoDepartamento;
        Departamento departamento = departamentosMap.get(keyDepartamento);
        if (departamento == null) {
            // Buscar en BD primero
            departamento = departamentoRepository.findByCodigoAndPaisId(codigoDepartamento, pais.getId())
                    .orElse(null);
            
            if (departamento == null) {
                // Crear nuevo departamento
                departamento = new Departamento(codigoDepartamento, nombreDepartamento, pais);
                departamento = departamentoRepository.save(departamento);
                logger.debug("Creado departamento: {} - {}", codigoDepartamento, nombreDepartamento);
            }
            departamentosMap.put(keyDepartamento, departamento);
        }

        // 2. DISTRITO - Único por código dentro del departamento
        String keyDistrito = codigoDepartamento + "-" + codigoDistrito;
        Distrito distrito = distritosMap.get(keyDistrito);
        if (distrito == null) {
            // Buscar en BD primero
            distrito = distritoRepository.findByCodigoAndDepartamentoId(codigoDistrito, departamento.getId())
                    .orElse(null);
            
            if (distrito == null) {
                // Crear nuevo distrito
                distrito = new Distrito(codigoDistrito, nombreDistrito, departamento);
                distrito = distritoRepository.save(distrito);
                logger.debug("Creado distrito: {} - {} (Depto: {})", codigoDistrito, nombreDistrito, codigoDepartamento);
            }
            distritosMap.put(keyDistrito, distrito);
        }

        // 3. CIUDAD - Única por código dentro del distrito
        String keyCiudad = keyDistrito + "-" + codigoCiudad;
        Ciudad ciudad = ciudadesMap.get(keyCiudad);
        if (ciudad == null) {
            // Buscar en BD primero
            ciudad = ciudadRepository.findByCodigoAndDistritoId(codigoCiudad, distrito.getId())
                    .orElse(null);
            
            if (ciudad == null) {
                // Crear nueva ciudad
                ciudad = new Ciudad(codigoCiudad, nombreCiudad, distrito);
                ciudad = ciudadRepository.save(ciudad);
                logger.debug("Creada ciudad: {} - {} (Distrito: {})", codigoCiudad, nombreCiudad, codigoDistrito);
            }
            ciudadesMap.put(keyCiudad, ciudad);
        }

        // 4. BARRIO - Único por código dentro de la ciudad
        if (!codigoBarrio.isEmpty() && !nombreBarrio.isEmpty()) {
            // Verificar si ya existe este barrio en esta ciudad
            boolean barrioExiste = barrioRepository.findByCodigoAndCiudadId(codigoBarrio, ciudad.getId())
                    .isPresent();
            
            if (!barrioExiste) {
                Barrio barrio = new Barrio(codigoBarrio, nombreBarrio, ciudad);
                barrioRepository.save(barrio);
                logger.debug("Creado barrio: {} - {} (Ciudad: {})", codigoBarrio, nombreBarrio, codigoCiudad);
            }
        }
    }

    /**
     * Parsea una línea CSV manejando comillas y comas dentro de campos.
     */
    private String[] parseCsvLine(String line) {
        // Implementación simple para CSV - en producción usar una librería como OpenCSV
        return line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    }

    /**
     * Verifica si los datos geográficos están cargados.
     */
    public boolean datosGeograficosCargados() {
        return departamentoRepository.count() > 0;
    }

    /**
     * Obtiene estadísticas de los datos cargados.
     */
    public Map<String, Long> obtenerEstadisticas() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("paises", paisRepository.count());
        stats.put("departamentos", departamentoRepository.count());
        stats.put("distritos", distritoRepository.count());
        stats.put("ciudades", ciudadRepository.count());
        stats.put("barrios", barrioRepository.count());
        return stats;
    }

    /**
     * Limpia todos los datos geográficos de la base de datos.
     */
    @Transactional
    public void limpiarDatosGeograficos() {
        try {
            logger.info("Iniciando limpieza de datos geográficos...");
            
            // Eliminar en orden inverso de dependencias
            long barriosEliminados = barrioRepository.count();
            barrioRepository.deleteAll();
            logger.info("Eliminados {} barrios", barriosEliminados);
            
            long ciudadesEliminadas = ciudadRepository.count();
            ciudadRepository.deleteAll();
            logger.info("Eliminadas {} ciudades", ciudadesEliminadas);
            
            long distritosEliminados = distritoRepository.count();
            distritoRepository.deleteAll();
            logger.info("Eliminados {} distritos", distritosEliminados);
            
            long departamentosEliminados = departamentoRepository.count();
            departamentoRepository.deleteAll();
            logger.info("Eliminados {} departamentos", departamentosEliminados);
            
            long paisesEliminados = paisRepository.count();
            paisRepository.deleteAll();
            logger.info("Eliminados {} países", paisesEliminados);
            
            logger.info("Limpieza de datos geográficos completada exitosamente");
            
        } catch (Exception e) {
            logger.error("Error limpiando datos geográficos", e);
            throw new RuntimeException("Error limpiando datos geográficos", e);
        }
    }
}