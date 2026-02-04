package com.frcefact.controller;

import com.frcefact.dto.TimbradoDetalleDto;
import com.frcefact.dto.mapper.TimbradoDetalleMapper;
import com.frcefact.model.TimbradoDetalle;
import com.frcefact.service.TimbradoDetalleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para gestión de detalles de timbrados (puntos de expedición).
 */
@RestController
@RequestMapping
@CrossOrigin(origins = "*")
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
     * Crea un nuevo detalle de timbrado.
     */
    @PostMapping("/timbrados/{timbradoId}/detalles")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN')")
    public ResponseEntity<TimbradoDetalleDto> crearDetalle(
            @PathVariable Long timbradoId,
            @Valid @RequestBody TimbradoDetalleDto dto) {
        
        // Validar que el timbradoId del path coincide con el del body
        if (!timbradoId.equals(dto.getTimbradoId())) {
            throw new IllegalArgumentException("El ID de timbrado en la URL no coincide con el del cuerpo de la petición");
        }
        
        TimbradoDetalle detalle = timbradoDetalleService.crear(dto);
        TimbradoDetalleDto detalleDto = timbradoDetalleMapper.toDto(detalle);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(detalleDto);
    }

    /**
     * Lista todos los detalles de un timbrado.
     */
    @GetMapping("/timbrados/{timbradoId}/detalles")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    public ResponseEntity<List<TimbradoDetalleDto>> listarDetallesPorTimbrado(
            @PathVariable Long timbradoId) {
        
        List<TimbradoDetalle> detalles = timbradoDetalleService.listarPorTimbrado(timbradoId);
        List<TimbradoDetalleDto> detallesDto = detalles.stream()
                .map(timbradoDetalleMapper::toDto)
                .toList();
        
        return ResponseEntity.ok(detallesDto);
    }

    /**
     * Lista detalles activos de un timbrado.
     */
    @GetMapping("/timbrados/{timbradoId}/detalles/activos")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    public ResponseEntity<List<TimbradoDetalleDto>> listarDetallesActivosPorTimbrado(
            @PathVariable Long timbradoId) {
        
        List<TimbradoDetalle> detalles = timbradoDetalleService.listarActivosPorTimbrado(timbradoId);
        List<TimbradoDetalleDto> detallesDto = detalles.stream()
                .map(timbradoDetalleMapper::toDto)
                .toList();
        
        return ResponseEntity.ok(detallesDto);
    }

    /**
     * Obtiene un detalle específico por ID.
     */
    @GetMapping("/timbrado-detalles/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    public ResponseEntity<TimbradoDetalleDto> obtenerDetalle(@PathVariable Long id) {
        TimbradoDetalle detalle = timbradoDetalleService.obtenerPorId(id);
        TimbradoDetalleDto detalleDto = timbradoDetalleMapper.toDto(detalle);
        
        return ResponseEntity.ok(detalleDto);
    }

    /**
     * Actualiza un detalle de timbrado.
     */
    @PutMapping("/timbrado-detalles/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN')")
    public ResponseEntity<TimbradoDetalleDto> actualizarDetalle(
            @PathVariable Long id,
            @Valid @RequestBody TimbradoDetalleDto dto) {
        
        TimbradoDetalle detalle = timbradoDetalleService.actualizar(id, dto);
        TimbradoDetalleDto detalleDto = timbradoDetalleMapper.toDto(detalle);
        
        return ResponseEntity.ok(detalleDto);
    }

    /**
     * Desactiva un detalle de timbrado.
     */
    @DeleteMapping("/timbrado-detalles/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN')")
    public ResponseEntity<Void> desactivarDetalle(@PathVariable Long id) {
        timbradoDetalleService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtiene detalles que están por agotarse.
     */
    @GetMapping("/timbrados/{timbradoId}/detalles/por-agotarse")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    public ResponseEntity<List<TimbradoDetalleDto>> obtenerDetallesPorAgotarse(
            @PathVariable Long timbradoId,
            @RequestParam(defaultValue = "80.0") double umbralPorcentaje) {
        
        List<TimbradoDetalle> detalles = timbradoDetalleService.obtenerDetallesPorAgotarse(timbradoId, umbralPorcentaje);
        List<TimbradoDetalleDto> detallesDto = detalles.stream()
                .map(timbradoDetalleMapper::toDto)
                .toList();
        
        return ResponseEntity.ok(detallesDto);
    }

    /**
     * Lista todos los detalles activos de una empresa.
     */
    @GetMapping("/timbrado-detalles/empresa/{empresaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    public ResponseEntity<List<TimbradoDetalleDto>> listarDetallesActivosPorEmpresa(
            @PathVariable Long empresaId) {
        
        List<TimbradoDetalle> detalles = timbradoDetalleService.listarActivosPorEmpresa(empresaId);
        List<TimbradoDetalleDto> detallesDto = detalles.stream()
                .map(timbradoDetalleMapper::toDto)
                .toList();
        
        return ResponseEntity.ok(detallesDto);
    }
}