package com.frcefact.controller;

import com.frcefact.dto.NotaRemisionDto;
import com.frcefact.dto.mapper.NotaRemisionMapper;
import com.frcefact.model.NotaRemision;
import com.frcefact.service.NotaRemisionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notas-remision")
@Tag(name = "Notas de Remisión", description = "API para gestión de notas de remisión")
@SecurityRequirement(name = "bearer-jwt")
public class NotaRemisionController {

    private static final Logger logger = LoggerFactory.getLogger(NotaRemisionController.class);

    private final NotaRemisionService notaRemisionService;
    private final NotaRemisionMapper notaRemisionMapper;

    public NotaRemisionController(NotaRemisionService notaRemisionService, NotaRemisionMapper notaRemisionMapper) {
        this.notaRemisionService = notaRemisionService;
        this.notaRemisionMapper = notaRemisionMapper;
    }

    @Operation(summary = "Crear una nueva nota de remisión")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Nota de remisión creada exitosamente",
                    content = @Content(schema = @Schema(implementation = NotaRemisionDto.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta empresa")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR')")
    public ResponseEntity<NotaRemisionDto> crearNotaRemision(
            @Valid @RequestBody NotaRemisionDto dto) {
        
        logger.info("POST /notas-remision - Crear nota de remisión para empresa ID: {}", dto.getEmpresaId());
        
        NotaRemision entity = notaRemisionMapper.toEntity(dto);
        NotaRemision created = notaRemisionService.crearNotaRemision(entity);
        NotaRemisionDto responseDto = notaRemisionMapper.toDto(created);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @Operation(summary = "Obtener una nota de remisión por ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    public ResponseEntity<NotaRemisionDto> obtenerNotaRemision(
            @PathVariable Long id) {
        
        NotaRemision entity = notaRemisionService.obtenerPorId(id);
        NotaRemisionDto responseDto = notaRemisionMapper.toDto(entity);
        
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "Listar notas de remisión de una empresa")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    public ResponseEntity<Page<NotaRemisionDto>> listarNotasRemision(
            @RequestParam Long empresaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "fecha") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ? 
                Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<NotaRemision> pageResult = notaRemisionService.listarPorEmpresa(empresaId, pageable);
        Page<NotaRemisionDto> pageDto = pageResult.map(notaRemisionMapper::toDto);
        
        return ResponseEntity.ok(pageDto);
    }
    
    @Operation(summary = "Desactivar una nota de remisión")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN')")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        notaRemisionService.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}

