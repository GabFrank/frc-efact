package com.frcefact.controller;

import com.frcefact.dto.TimbradoDetalleDto;
import com.frcefact.dto.mapper.TimbradoDetalleMapper;
import com.frcefact.model.TimbradoDetalle;
import com.frcefact.service.TimbradoDetalleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controlador REST para gestión de timbrados detalle (puntos de expedición).
 */
@RestController
@RequestMapping("/timbrados-detalle")
@Tag(name = "Timbrados Detalle", description = "API para gestión de puntos de expedición de timbrados")
public class TimbradoDetalleController {

    private final TimbradoDetalleService timbradoDetalleService;
    private final TimbradoDetalleMapper timbradoDetalleMapper;

    public TimbradoDetalleController(
            TimbradoDetalleService timbradoDetalleService,
            TimbradoDetalleMapper timbradoDetalleMapper) {
        this.timbradoDetalleService = timbradoDetalleService;
        this.timbradoDetalleMapper = timbradoDetalleMapper;
    }

    /**
     * Crea un nuevo timbrado detalle.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR')")
    @Operation(summary = "Crear punto de expedición", 
               description = "Crea un nuevo punto de expedición para un timbrado")
    public ResponseEntity<TimbradoDetalleDto> crear(@Valid @RequestBody TimbradoDetalleDto detalleDto) {
        TimbradoDetalle detalle = timbradoDetalleMapper.toEntity(detalleDto);
        TimbradoDetalle detalleCreado = timbradoDetalleService.crear(detalle);
        TimbradoDetalleDto responseDto = timbradoDetalleMapper.toDto(detalleCreado);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    /**
     * Actualiza un timbrado detalle existente.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR')")
    @Operation(summary = "Actualizar punto de expedición", 
               description = "Actualiza un punto de expedición existente")
    public ResponseEntity<TimbradoDetalleDto> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody TimbradoDetalleDto detalleDto) {
        TimbradoDetalle detalle = timbradoDetalleMapper.toEntity(detalleDto);
        TimbradoDetalle detalleActualizado = timbradoDetalleService.actualizar(id, detalle);
        TimbradoDetalleDto responseDto = timbradoDetalleMapper.toDto(detalleActualizado);
        return ResponseEntity.ok(responseDto);
    }

    /**
     * Obtiene un timbrado detalle por ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    @Operation(summary = "Obtener punto de expedición", 
               description = "Obtiene un punto de expedición por su ID")
    public ResponseEntity<TimbradoDetalleDto> obtenerPorId(@PathVariable Long id) {
        TimbradoDetalle detalle = timbradoDetalleService.obtenerPorId(id);
        TimbradoDetalleDto responseDto = timbradoDetalleMapper.toDto(detalle);
        return ResponseEntity.ok(responseDto);
    }

    /**
     * Lista todos los detalles de un timbrado.
     */
    @GetMapping("/timbrado/{timbradoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    @Operation(summary = "Listar puntos de expedición", 
               description = "Lista todos los puntos de expedición de un timbrado")
    public ResponseEntity<List<TimbradoDetalleDto>> listarPorTimbrado(@PathVariable Long timbradoId) {
        List<TimbradoDetalle> detalles = timbradoDetalleService.listarPorTimbrado(timbradoId);
        List<TimbradoDetalleDto> responseDtos = detalles.stream()
                .map(timbradoDetalleMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDtos);
    }

    /**
     * Verifica si un timbrado detalle tiene números disponibles.
     */
    @GetMapping("/{id}/disponible")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    @Operation(summary = "Verificar disponibilidad", 
               description = "Verifica si un punto de expedición tiene números disponibles")
    public ResponseEntity<Map<String, Object>> verificarDisponibilidad(@PathVariable Long id) {
        boolean disponible = timbradoDetalleService.verificarDisponibilidad(id);
        long numerosDisponibles = timbradoDetalleService.obtenerNumerosDisponibles(id);
        double porcentajeUtilizado = timbradoDetalleService.obtenerPorcentajeUtilizado(id);

        Map<String, Object> response = new HashMap<>();
        response.put("disponible", disponible);
        response.put("numerosDisponibles", numerosDisponibles);
        response.put("porcentajeUtilizado", porcentajeUtilizado);

        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene detalles con números disponibles de un timbrado.
     */
    @GetMapping("/timbrado/{timbradoId}/con-numeros-disponibles")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    @Operation(summary = "Listar puntos con números disponibles", 
               description = "Lista puntos de expedición que tienen números disponibles")
    public ResponseEntity<List<TimbradoDetalleDto>> obtenerDetallesConNumerosDisponibles(
            @PathVariable Long timbradoId) {
        List<TimbradoDetalle> detalles = timbradoDetalleService.obtenerDetallesConNumerosDisponibles(timbradoId);
        List<TimbradoDetalleDto> responseDtos = detalles.stream()
                .map(timbradoDetalleMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDtos);
    }

    /**
     * Obtiene detalles que están por agotar su rango.
     */
    @GetMapping("/empresa/{empresaId}/por-agotarse")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    @Operation(summary = "Listar puntos por agotarse", 
               description = "Lista puntos de expedición que están por agotar su rango de numeración")
    public ResponseEntity<List<TimbradoDetalleDto>> obtenerDetallesPorAgotarse(@PathVariable Long empresaId) {
        List<TimbradoDetalle> detalles = timbradoDetalleService.obtenerDetallesPorAgotarse(empresaId);
        List<TimbradoDetalleDto> responseDtos = detalles.stream()
                .map(timbradoDetalleMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDtos);
    }

    /**
     * Desactiva un timbrado detalle.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN')")
    @Operation(summary = "Desactivar punto de expedición", 
               description = "Desactiva un punto de expedición (soft delete)")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        timbradoDetalleService.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
