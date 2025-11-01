package com.frcefact.controller;

import com.frcefact.dto.geografia.*;
import com.frcefact.service.GeografiaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para datos geográficos de SIFEN.
 */
@RestController
@RequestMapping("/api/geografia")
@CrossOrigin(origins = "*")
@Tag(name = "Geografía", description = "API para datos geográficos de SIFEN (departamentos, distritos, ciudades, barrios)")
public class GeografiaController {

    private final GeografiaService geografiaService;

    public GeografiaController(GeografiaService geografiaService) {
        this.geografiaService = geografiaService;
    }

    // ========== DEPARTAMENTOS ==========

    @GetMapping("/departamentos")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    @Operation(summary = "Obtener departamentos", description = "Obtiene la lista de todos los departamentos activos")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
        @ApiResponse(responseCode = "401", description = "No autenticado"),
        @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    public ResponseEntity<List<DepartamentoDto>> obtenerDepartamentos() {
        List<DepartamentoDto> departamentos = geografiaService.obtenerDepartamentos();
        return ResponseEntity.ok(departamentos);
    }

    @GetMapping("/departamentos/buscar")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    @Operation(summary = "Buscar departamentos", description = "Busca departamentos por código o nombre")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Búsqueda realizada exitosamente"),
        @ApiResponse(responseCode = "401", description = "No autenticado"),
        @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    public ResponseEntity<List<DepartamentoDto>> buscarDepartamentos(
            @Parameter(description = "Texto a buscar en código o nombre") @RequestParam String q) {
        List<DepartamentoDto> departamentos = geografiaService.buscarDepartamentos(q);
        return ResponseEntity.ok(departamentos);
    }

    @GetMapping("/departamentos/{codigo}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    @Operation(summary = "Obtener departamento por código", description = "Obtiene un departamento específico por su código")
    public ResponseEntity<DepartamentoDto> obtenerDepartamentoPorCodigo(
            @Parameter(description = "Código del departamento") @PathVariable String codigo) {
        DepartamentoDto departamento = geografiaService.obtenerDepartamentoPorCodigo(codigo);
        return departamento != null ? ResponseEntity.ok(departamento) : ResponseEntity.notFound().build();
    }

    // ========== DISTRITOS ==========

    @GetMapping("/departamentos/{departamentoCodigo}/distritos")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    @Operation(summary = "Obtener distritos por departamento", description = "Obtiene todos los distritos de un departamento")
    public ResponseEntity<List<DistritoDto>> obtenerDistritosPorDepartamento(
            @Parameter(description = "Código del departamento") @PathVariable String departamentoCodigo) {
        List<DistritoDto> distritos = geografiaService.obtenerDistritosPorDepartamento(departamentoCodigo);
        return ResponseEntity.ok(distritos);
    }

    @GetMapping("/distritos/buscar")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    @Operation(summary = "Buscar distritos", description = "Busca distritos por código o nombre")
    public ResponseEntity<List<DistritoDto>> buscarDistritos(
            @Parameter(description = "Texto a buscar") @RequestParam String q,
            @Parameter(description = "Código del departamento (opcional)") @RequestParam(required = false) String departamento) {
        List<DistritoDto> distritos;
        if (departamento != null && !departamento.isEmpty()) {
            distritos = geografiaService.buscarDistritosPorDepartamento(departamento, q);
        } else {
            distritos = geografiaService.buscarDistritos(q);
        }
        return ResponseEntity.ok(distritos);
    }

    @GetMapping("/distritos/{codigo}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    @Operation(summary = "Obtener distrito por código", description = "Obtiene un distrito específico por su código")
    public ResponseEntity<DistritoDto> obtenerDistritoPorCodigo(
            @Parameter(description = "Código del distrito") @PathVariable String codigo) {
        DistritoDto distrito = geografiaService.obtenerDistritoPorCodigo(codigo);
        return distrito != null ? ResponseEntity.ok(distrito) : ResponseEntity.notFound().build();
    }

    // ========== CIUDADES ==========

    @GetMapping("/distritos/{distritoCodigo}/ciudades")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    @Operation(summary = "Obtener ciudades por distrito", description = "Obtiene todas las ciudades de un distrito")
    public ResponseEntity<List<CiudadDto>> obtenerCiudadesPorDistrito(
            @Parameter(description = "Código del distrito") @PathVariable String distritoCodigo) {
        List<CiudadDto> ciudades = geografiaService.obtenerCiudadesPorDistrito(distritoCodigo);
        return ResponseEntity.ok(ciudades);
    }

    @GetMapping("/ciudades/buscar")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    @Operation(summary = "Buscar ciudades", description = "Busca ciudades por código o nombre")
    public ResponseEntity<List<CiudadDto>> buscarCiudades(
            @Parameter(description = "Texto a buscar") @RequestParam String q,
            @Parameter(description = "Código del distrito (opcional)") @RequestParam(required = false) String distrito) {
        List<CiudadDto> ciudades;
        if (distrito != null && !distrito.isEmpty()) {
            ciudades = geografiaService.buscarCiudadesPorDistrito(distrito, q);
        } else {
            ciudades = geografiaService.buscarCiudades(q);
        }
        return ResponseEntity.ok(ciudades);
    }

    @GetMapping("/ciudades/{codigo}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    @Operation(summary = "Obtener ciudad por código", description = "Obtiene una ciudad específica por su código")
    public ResponseEntity<CiudadDto> obtenerCiudadPorCodigo(
            @Parameter(description = "Código de la ciudad") @PathVariable String codigo) {
        CiudadDto ciudad = geografiaService.obtenerCiudadPorCodigo(codigo);
        return ciudad != null ? ResponseEntity.ok(ciudad) : ResponseEntity.notFound().build();
    }

    @GetMapping("/ciudades/id/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    @Operation(summary = "Obtener ciudad por ID", description = "Obtiene una ciudad específica por su ID")
    public ResponseEntity<CiudadDto> obtenerCiudadPorId(
            @Parameter(description = "ID de la ciudad") @PathVariable Long id) {
        CiudadDto ciudad = geografiaService.obtenerCiudadPorId(id);
        return ciudad != null ? ResponseEntity.ok(ciudad) : ResponseEntity.notFound().build();
    }

    // ========== BARRIOS ==========

    @GetMapping("/ciudades/{ciudadCodigo}/barrios")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    @Operation(summary = "Obtener barrios por ciudad", description = "Obtiene todos los barrios de una ciudad")
    public ResponseEntity<List<BarrioDto>> obtenerBarriosPorCiudad(
            @Parameter(description = "Código de la ciudad") @PathVariable String ciudadCodigo) {
        List<BarrioDto> barrios = geografiaService.obtenerBarriosPorCiudad(ciudadCodigo);
        return ResponseEntity.ok(barrios);
    }

    @GetMapping("/barrios/buscar")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    @Operation(summary = "Buscar barrios", description = "Busca barrios por código o nombre")
    public ResponseEntity<List<BarrioDto>> buscarBarrios(
            @Parameter(description = "Texto a buscar") @RequestParam String q,
            @Parameter(description = "Código de la ciudad (opcional)") @RequestParam(required = false) String ciudad) {
        List<BarrioDto> barrios;
        if (ciudad != null && !ciudad.isEmpty()) {
            barrios = geografiaService.buscarBarriosPorCiudad(ciudad, q);
        } else {
            barrios = geografiaService.buscarBarrios(q);
        }
        return ResponseEntity.ok(barrios);
    }

    @GetMapping("/barrios/{codigo}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    @Operation(summary = "Obtener barrio por código", description = "Obtiene un barrio específico por su código")
    public ResponseEntity<BarrioDto> obtenerBarrioPorCodigo(
            @Parameter(description = "Código del barrio") @PathVariable String codigo) {
        BarrioDto barrio = geografiaService.obtenerBarrioPorCodigo(codigo);
        return barrio != null ? ResponseEntity.ok(barrio) : ResponseEntity.notFound().build();
    }
}