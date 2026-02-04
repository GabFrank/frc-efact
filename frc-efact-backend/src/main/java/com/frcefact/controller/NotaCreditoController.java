package com.frcefact.controller;

import com.frcefact.dto.NotaCreditoDto;
import com.frcefact.dto.GenerarDeResponse;
import com.frcefact.dto.mapper.NotaCreditoMapper;
import com.frcefact.dto.mapper.DocumentoElectronicoMapper;
import com.frcefact.model.NotaCredito;
import com.frcefact.service.NotaCreditoService;
import com.frcefact.service.DocumentoElectronicoService;
import com.frcefact.dto.LoteDeDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/notas-credito")
@Tag(name = "Notas de Crédito", description = "API para gestión de notas de crédito")
@SecurityRequirement(name = "bearer-jwt")
public class NotaCreditoController {

    private static final Logger logger = LoggerFactory.getLogger(NotaCreditoController.class);

    private final NotaCreditoService notaCreditoService;
    private final NotaCreditoMapper notaCreditoMapper;
    private final DocumentoElectronicoService documentoElectronicoService;
    private final DocumentoElectronicoMapper documentoElectronicoMapper;

    public NotaCreditoController(
            NotaCreditoService notaCreditoService, 
            NotaCreditoMapper notaCreditoMapper,
            DocumentoElectronicoService documentoElectronicoService,
            DocumentoElectronicoMapper documentoElectronicoMapper) {
        this.notaCreditoService = notaCreditoService;
        this.notaCreditoMapper = notaCreditoMapper;
        this.documentoElectronicoService = documentoElectronicoService;
        this.documentoElectronicoMapper = documentoElectronicoMapper;
    }

    @Operation(summary = "Crear una nueva nota de crédito",
               description = "Crea una nueva nota de crédito con items.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Nota de crédito creada exitosamente",
                    content = @Content(schema = @Schema(implementation = NotaCreditoDto.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta empresa")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR')")
    public ResponseEntity<NotaCreditoDto> crearNotaCredito(
            @Valid @RequestBody NotaCreditoDto dto) {
        
        logger.info("POST /notas-credito - Crear nota de crédito para empresa ID: {}", dto.getEmpresaId());
        
        NotaCredito entity = notaCreditoMapper.toEntity(dto);
        NotaCredito created = notaCreditoService.crearNotaCredito(entity);
        NotaCreditoDto responseDto = notaCreditoMapper.toDto(created);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @Operation(summary = "Obtener una nota de crédito por ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    public ResponseEntity<NotaCreditoDto> obtenerNotaCredito(
            @Parameter(description = "ID de la nota de crédito") @PathVariable Long id) {
        
        NotaCredito entity = notaCreditoService.obtenerPorId(id);
        NotaCreditoDto responseDto = notaCreditoMapper.toDto(entity);
        
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "Listar notas de crédito de una empresa")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    public ResponseEntity<Page<NotaCreditoDto>> listarNotasCredito(
            @RequestParam Long empresaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "fecha") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ? 
                Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<NotaCredito> pageResult = notaCreditoService.listarPorEmpresa(empresaId, pageable);
        Page<NotaCreditoDto> pageDto = pageResult.map(notaCreditoMapper::toDto);
        
        return ResponseEntity.ok(pageDto);
    }
    
    @Operation(summary = "Desactivar una nota de crédito")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN')")
    public ResponseEntity<Void> desactivar(
            @PathVariable Long id) {
        
        notaCreditoService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Genera un documento electrónico a partir de una nota de crédito.
     * 
     * POST /notas-credito/{id}/generar-de
     */
    @Operation(summary = "Generar documento electrónico",
               description = "Genera un documento electrónico (DE) a partir de una nota de crédito")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Documento electrónico generado exitosamente"),
        @ApiResponse(responseCode = "400", description = "La nota de crédito ya tiene un DE o certificado no válido"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta empresa"),
        @ApiResponse(responseCode = "404", description = "Nota de crédito no encontrada")
    })
    @PostMapping("/{id}/generar-de")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR')")
    public ResponseEntity<GenerarDeResponse> generarDocumentoElectronico(
            @Parameter(description = "ID de la nota de crédito") @PathVariable Long id) {
        
        logger.info("POST /notas-credito/{}/generar-de - Generar documento electrónico", id);
        
        var resultado = documentoElectronicoService.generarYEnviarDesdeNotaCredito(id);
        var documentoDto = documentoElectronicoMapper.toDto(resultado.documento());
        var loteDto = LoteDeDto.fromEntity(resultado.lote());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(GenerarDeResponse.of(documentoDto, loteDto));
    }
}

