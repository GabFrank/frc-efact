package com.frcefact.controller;

import com.frcefact.dto.TimbradoDto;
import com.frcefact.dto.mapper.TimbradoMapper;
import com.frcefact.model.Timbrado;
import com.frcefact.service.TimbradoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador REST para gestión de timbrados.
 */
@RestController
@RequestMapping("/timbrados")
@Tag(name = "Timbrados", description = "API para gestión de timbrados fiscales")
public class TimbradoController {

    private final TimbradoService timbradoService;
    private final TimbradoMapper timbradoMapper;

    public TimbradoController(TimbradoService timbradoService, TimbradoMapper timbradoMapper) {
        this.timbradoService = timbradoService;
        this.timbradoMapper = timbradoMapper;
    }

    /**
     * Crea un nuevo timbrado.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR')")
    @Operation(summary = "Crear timbrado", description = "Crea un nuevo timbrado fiscal para una empresa")
    public ResponseEntity<TimbradoDto> crear(@Valid @RequestBody TimbradoDto timbradoDto) {
        Timbrado timbrado = timbradoMapper.toEntity(timbradoDto);
        Timbrado timbradoCreado = timbradoService.crear(timbrado);
        TimbradoDto responseDto = timbradoMapper.toDto(timbradoCreado);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    /**
     * Actualiza un timbrado existente.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR')")
    @Operation(summary = "Actualizar timbrado", description = "Actualiza un timbrado existente")
    public ResponseEntity<TimbradoDto> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody TimbradoDto timbradoDto) {
        Timbrado timbrado = timbradoMapper.toEntity(timbradoDto);
        Timbrado timbradoActualizado = timbradoService.actualizar(id, timbrado);
        TimbradoDto responseDto = timbradoMapper.toDto(timbradoActualizado);
        return ResponseEntity.ok(responseDto);
    }

    /**
     * Obtiene un timbrado por ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    @Operation(summary = "Obtener timbrado", description = "Obtiene un timbrado por su ID")
    public ResponseEntity<TimbradoDto> obtenerPorId(@PathVariable Long id) {
        Timbrado timbrado = timbradoService.obtenerPorId(id);
        TimbradoDto responseDto = timbradoMapper.toDto(timbrado);
        return ResponseEntity.ok(responseDto);
    }

    /**
     * Lista todos los timbrados de una empresa.
     */
    @GetMapping("/empresa/{empresaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    @Operation(summary = "Listar timbrados por empresa", description = "Lista todos los timbrados de una empresa")
    public ResponseEntity<List<TimbradoDto>> listarPorEmpresa(@PathVariable Long empresaId) {
        List<Timbrado> timbrados = timbradoService.listarPorEmpresa(empresaId);
        List<TimbradoDto> responseDtos = timbrados.stream()
                .map(timbradoMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDtos);
    }

    /**
     * Lista timbrados activos de una empresa.
     */
    @GetMapping("/empresa/{empresaId}/activos")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    @Operation(summary = "Listar timbrados activos", description = "Lista timbrados activos de una empresa")
    public ResponseEntity<List<TimbradoDto>> listarActivosPorEmpresa(@PathVariable Long empresaId) {
        List<Timbrado> timbrados = timbradoService.listarActivosPorEmpresa(empresaId);
        List<TimbradoDto> responseDtos = timbrados.stream()
                .map(timbradoMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDtos);
    }

    /**
     * Verifica si un timbrado está vigente.
     */
    @GetMapping("/{id}/vigente")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    @Operation(summary = "Verificar vigencia", description = "Verifica si un timbrado está vigente en la fecha actual")
    public ResponseEntity<Boolean> verificarVigencia(@PathVariable Long id) {
        boolean vigente = timbradoService.verificarVigencia(id);
        return ResponseEntity.ok(vigente);
    }

    /**
     * Obtiene timbrados vigentes de una empresa.
     */
    @GetMapping("/empresa/{empresaId}/vigentes")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    @Operation(summary = "Listar timbrados vigentes", description = "Lista timbrados vigentes de una empresa")
    public ResponseEntity<List<TimbradoDto>> obtenerTimbradosVigentes(@PathVariable Long empresaId) {
        List<Timbrado> timbrados = timbradoService.obtenerTimbradosVigentes(empresaId);
        List<TimbradoDto> responseDtos = timbrados.stream()
                .map(timbradoMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDtos);
    }

    /**
     * Obtiene timbrados electrónicos vigentes de una empresa.
     */
    @GetMapping("/empresa/{empresaId}/electronicos-vigentes")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    @Operation(summary = "Listar timbrados electrónicos vigentes", 
               description = "Lista timbrados electrónicos vigentes de una empresa")
    public ResponseEntity<List<TimbradoDto>> obtenerTimbradosElectronicosVigentes(@PathVariable Long empresaId) {
        List<Timbrado> timbrados = timbradoService.obtenerTimbradosElectronicosVigentes(empresaId);
        List<TimbradoDto> responseDtos = timbrados.stream()
                .map(timbradoMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDtos);
    }

    /**
     * Obtiene timbrados que están por vencer.
     */
    @GetMapping("/empresa/{empresaId}/por-vencer")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    @Operation(summary = "Listar timbrados por vencer", 
               description = "Lista timbrados que están por vencer en los próximos días")
    public ResponseEntity<List<TimbradoDto>> obtenerTimbradosPorVencer(
            @PathVariable Long empresaId,
            @RequestParam(defaultValue = "15") int diasAnticipacion) {
        List<Timbrado> timbrados = timbradoService.obtenerTimbradosPorVencer(empresaId, diasAnticipacion);
        List<TimbradoDto> responseDtos = timbrados.stream()
                .map(timbradoMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDtos);
    }

    /**
     * Desactiva un timbrado.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN')")
    @Operation(summary = "Desactivar timbrado", description = "Desactiva un timbrado (soft delete)")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        timbradoService.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
