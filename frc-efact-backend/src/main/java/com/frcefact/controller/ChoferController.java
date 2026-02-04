package com.frcefact.controller;

import com.frcefact.dto.ChoferDto;
import com.frcefact.dto.mapper.ChoferMapper;
import com.frcefact.model.Chofer;
import com.frcefact.service.ChoferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador REST para gestión de choferes.
 * Proporciona endpoints CRUD con búsqueda y paginación.
 */
@RestController
@RequestMapping("/choferes")
@Tag(name = "Choferes", description = "API para gestión de choferes")
public class ChoferController {

    private static final Logger logger = LoggerFactory.getLogger(ChoferController.class);

    private final ChoferService choferService;
    private final ChoferMapper choferMapper;

    public ChoferController(ChoferService choferService, ChoferMapper choferMapper) {
        this.choferService = choferService;
        this.choferMapper = choferMapper;
    }

    /**
     * Crea un nuevo chofer.
     */
    @PostMapping("/empresa/{empresaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR')")
    @Operation(summary = "Crear chofer", description = "Crea un nuevo chofer para una empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Chofer creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta operación"),
        @ApiResponse(responseCode = "404", description = "Empresa no encontrada")
    })
    public ResponseEntity<ChoferDto> crearChofer(
            @Parameter(description = "ID de la empresa") @PathVariable Long empresaId,
            @Valid @RequestBody ChoferDto choferDto) {
        
        Chofer chofer = choferMapper.toEntity(choferDto);
        Chofer choferCreado = choferService.crearChofer(empresaId, chofer);
        ChoferDto responseDto = choferMapper.toDto(choferCreado);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    /**
     * Actualiza un chofer existente.
     */
    @PutMapping("/empresa/{empresaId}/{choferId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR')")
    @Operation(summary = "Actualizar chofer", description = "Actualiza un chofer existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Chofer actualizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta operación"),
        @ApiResponse(responseCode = "404", description = "Chofer no encontrado")
    })
    public ResponseEntity<ChoferDto> actualizarChofer(
            @Parameter(description = "ID de la empresa") @PathVariable Long empresaId,
            @Parameter(description = "ID del chofer") @PathVariable Long choferId,
            @Valid @RequestBody ChoferDto choferDto) {
        
        Chofer chofer = choferMapper.toEntity(choferDto);
        Chofer choferActualizado = choferService.actualizarChofer(empresaId, choferId, chofer);
        ChoferDto responseDto = choferMapper.toDto(choferActualizado);

        return ResponseEntity.ok(responseDto);
    }

    /**
     * Obtiene un chofer por ID.
     */
    @GetMapping("/empresa/{empresaId}/{choferId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    @Operation(summary = "Obtener chofer", description = "Obtiene un chofer por su ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Chofer encontrado"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta operación"),
        @ApiResponse(responseCode = "404", description = "Chofer no encontrado")
    })
    public ResponseEntity<ChoferDto> obtenerChofer(
            @Parameter(description = "ID de la empresa") @PathVariable Long empresaId,
            @Parameter(description = "ID del chofer") @PathVariable Long choferId) {
        
        return choferService.obtenerChoferPorId(empresaId, choferId)
                .map(choferMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Lista todos los choferes activos de una empresa.
     */
    @GetMapping("/empresa/{empresaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    @Operation(summary = "Listar choferes", description = "Lista todos los choferes activos de una empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de choferes"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta operación")
    })
    public ResponseEntity<List<ChoferDto>> listarChoferes(
            @Parameter(description = "ID de la empresa") @PathVariable Long empresaId) {
        
        List<Chofer> choferes = choferService.listarChoferesPorEmpresa(empresaId);
        List<ChoferDto> choferesDto = choferes.stream()
                .map(choferMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(choferesDto);
    }

    /**
     * Lista choferes con paginación.
     */
    @GetMapping("/empresa/{empresaId}/paginado")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    @Operation(summary = "Listar choferes paginados", description = "Lista choferes con paginación y ordenamiento")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Página de choferes"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta operación")
    })
    public ResponseEntity<Page<ChoferDto>> listarChoferesPaginados(
            @Parameter(description = "ID de la empresa") @PathVariable Long empresaId,
            @Parameter(description = "Número de página (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campo de ordenamiento") @RequestParam(defaultValue = "nombre") String sortBy,
            @Parameter(description = "Dirección de ordenamiento") @RequestParam(defaultValue = "ASC") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("DESC") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Chofer> choferesPage = choferService.listarChoferesPaginados(empresaId, pageable);
        Page<ChoferDto> choferesDtoPage = choferesPage.map(choferMapper::toDto);

        return ResponseEntity.ok(choferesDtoPage);
    }

    /**
     * Busca choferes por nombre o documento.
     */
    @GetMapping("/empresa/{empresaId}/buscar")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    @Operation(summary = "Buscar choferes", description = "Busca choferes por nombre o documento con autocompletado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de choferes encontrados"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta operación")
    })
    public ResponseEntity<List<ChoferDto>> buscarChoferes(
            @Parameter(description = "ID de la empresa") @PathVariable Long empresaId,
            @Parameter(description = "Término de búsqueda") @RequestParam(required = false) String q) {
        
        List<Chofer> choferes = choferService.buscarChoferes(empresaId, q);
        List<ChoferDto> choferesDto = choferes.stream()
                .map(choferMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(choferesDto);
    }

    /**
     * Busca choferes con filtros múltiples y paginación.
     */
    @GetMapping("/empresa/{empresaId}/filtrar")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    @Operation(summary = "Buscar choferes con filtros", description = "Busca choferes con múltiples filtros y paginación")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Página de choferes encontrados"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta operación")
    })
    public ResponseEntity<Page<ChoferDto>> buscarChoferesConFiltros(
            @Parameter(description = "ID de la empresa") @PathVariable Long empresaId,
            @Parameter(description = "Término de búsqueda") @RequestParam(required = false) String q,
            @Parameter(description = "Estado activo") @RequestParam(required = false) Boolean activo,
            @Parameter(description = "Número de página (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campo de ordenamiento") @RequestParam(defaultValue = "nombre") String sortBy,
            @Parameter(description = "Dirección de ordenamiento") @RequestParam(defaultValue = "ASC") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("DESC") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Chofer> choferesPage = choferService.buscarChoferesPaginados(empresaId, q, activo, pageable);
        Page<ChoferDto> choferesDtoPage = choferesPage.map(choferMapper::toDto);

        return ResponseEntity.ok(choferesDtoPage);
    }

    /**
     * Desactiva un chofer.
     */
    @DeleteMapping("/empresa/{empresaId}/{choferId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN')")
    @Operation(summary = "Desactivar chofer", description = "Desactiva un chofer (soft delete)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Chofer desactivado exitosamente"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta operación"),
        @ApiResponse(responseCode = "404", description = "Chofer no encontrado")
    })
    public ResponseEntity<Void> desactivarChofer(
            @Parameter(description = "ID de la empresa") @PathVariable Long empresaId,
            @Parameter(description = "ID del chofer") @PathVariable Long choferId) {
        
        choferService.desactivarChofer(empresaId, choferId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reactiva un chofer.
     */
    @PatchMapping("/empresa/{empresaId}/{choferId}/reactivar")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN')")
    @Operation(summary = "Reactivar chofer", description = "Reactiva un chofer desactivado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Chofer reactivado exitosamente"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta operación"),
        @ApiResponse(responseCode = "404", description = "Chofer no encontrado")
    })
    public ResponseEntity<Void> reactivarChofer(
            @Parameter(description = "ID de la empresa") @PathVariable Long empresaId,
            @Parameter(description = "ID del chofer") @PathVariable Long choferId) {
        
        choferService.reactivarChofer(empresaId, choferId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Cuenta choferes activos de una empresa.
     */
    @GetMapping("/empresa/{empresaId}/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    @Operation(summary = "Contar choferes", description = "Cuenta los choferes activos de una empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cantidad de choferes"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta operación")
    })
    public ResponseEntity<Long> contarChoferes(
            @Parameter(description = "ID de la empresa") @PathVariable Long empresaId) {
        
        long count = choferService.contarChoferesActivos(empresaId);
        return ResponseEntity.ok(count);
    }
}
