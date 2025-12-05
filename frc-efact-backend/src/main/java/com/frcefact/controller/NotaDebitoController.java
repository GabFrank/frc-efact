package com.frcefact.controller;

import com.frcefact.dto.NotaDebitoDto;
import com.frcefact.dto.mapper.NotaDebitoMapper;
import com.frcefact.model.NotaDebito;
import com.frcefact.service.NotaDebitoService;
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
@RequestMapping("/notas-debito")
@Tag(name = "Notas de Débito", description = "API para gestión de notas de débito")
@SecurityRequirement(name = "bearer-jwt")
public class NotaDebitoController {

    private static final Logger logger = LoggerFactory.getLogger(NotaDebitoController.class);

    private final NotaDebitoService notaDebitoService;
    private final NotaDebitoMapper notaDebitoMapper;

    public NotaDebitoController(NotaDebitoService notaDebitoService, NotaDebitoMapper notaDebitoMapper) {
        this.notaDebitoService = notaDebitoService;
        this.notaDebitoMapper = notaDebitoMapper;
    }

    @Operation(summary = "Crear una nueva nota de débito")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Nota de débito creada exitosamente",
                    content = @Content(schema = @Schema(implementation = NotaDebitoDto.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta empresa")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR')")
    public ResponseEntity<NotaDebitoDto> crearNotaDebito(
            @Valid @RequestBody NotaDebitoDto dto) {
        
        logger.info("POST /notas-debito - Crear nota de débito para empresa ID: {}", dto.getEmpresaId());
        
        NotaDebito entity = notaDebitoMapper.toEntity(dto);
        NotaDebito created = notaDebitoService.crearNotaDebito(entity);
        NotaDebitoDto responseDto = notaDebitoMapper.toDto(created);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @Operation(summary = "Obtener una nota de débito por ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    public ResponseEntity<NotaDebitoDto> obtenerNotaDebito(
            @PathVariable Long id) {
        
        NotaDebito entity = notaDebitoService.obtenerPorId(id);
        NotaDebitoDto responseDto = notaDebitoMapper.toDto(entity);
        
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "Listar notas de débito de una empresa")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    public ResponseEntity<Page<NotaDebitoDto>> listarNotasDebito(
            @RequestParam Long empresaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "fecha") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ? 
                Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<NotaDebito> pageResult = notaDebitoService.listarPorEmpresa(empresaId, pageable);
        Page<NotaDebitoDto> pageDto = pageResult.map(notaDebitoMapper::toDto);
        
        return ResponseEntity.ok(pageDto);
    }
    
    @Operation(summary = "Desactivar una nota de débito")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN')")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        notaDebitoService.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}

