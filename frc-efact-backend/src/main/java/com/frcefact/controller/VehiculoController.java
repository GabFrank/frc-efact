package com.frcefact.controller;

import com.frcefact.dto.VehiculoDto;
import com.frcefact.dto.mapper.VehiculoMapper;
import com.frcefact.model.Vehiculo;
import com.frcefact.service.VehiculoService;
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
 * Controlador REST para gestión de vehículos.
 * Proporciona endpoints CRUD con búsqueda y paginación.
 */
@RestController
@RequestMapping("/vehiculos")
@Tag(name = "Vehículos", description = "API para gestión de vehículos")
public class VehiculoController {

    private static final Logger logger = LoggerFactory.getLogger(VehiculoController.class);

    private final VehiculoService vehiculoService;
    private final VehiculoMapper vehiculoMapper;

    public VehiculoController(VehiculoService vehiculoService, VehiculoMapper vehiculoMapper) {
        this.vehiculoService = vehiculoService;
        this.vehiculoMapper = vehiculoMapper;
    }

    /**
     * Crea un nuevo vehículo.
     */
    @PostMapping("/empresa/{empresaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR')")
    @Operation(summary = "Crear vehículo", description = "Crea un nuevo vehículo para una empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Vehículo creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta operación"),
        @ApiResponse(responseCode = "404", description = "Empresa no encontrada")
    })
    public ResponseEntity<VehiculoDto> crearVehiculo(
            @Parameter(description = "ID de la empresa") @PathVariable Long empresaId,
            @Valid @RequestBody VehiculoDto vehiculoDto) {
        
        Vehiculo vehiculo = vehiculoMapper.toEntity(vehiculoDto);
        Vehiculo vehiculoCreado = vehiculoService.crearVehiculo(empresaId, vehiculo);
        VehiculoDto responseDto = vehiculoMapper.toDto(vehiculoCreado);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    /**
     * Actualiza un vehículo existente.
     */
    @PutMapping("/empresa/{empresaId}/{vehiculoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR')")
    @Operation(summary = "Actualizar vehículo", description = "Actualiza un vehículo existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Vehículo actualizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta operación"),
        @ApiResponse(responseCode = "404", description = "Vehículo no encontrado")
    })
    public ResponseEntity<VehiculoDto> actualizarVehiculo(
            @Parameter(description = "ID de la empresa") @PathVariable Long empresaId,
            @Parameter(description = "ID del vehículo") @PathVariable Long vehiculoId,
            @Valid @RequestBody VehiculoDto vehiculoDto) {
        
        Vehiculo vehiculo = vehiculoMapper.toEntity(vehiculoDto);
        Vehiculo vehiculoActualizado = vehiculoService.actualizarVehiculo(empresaId, vehiculoId, vehiculo);
        VehiculoDto responseDto = vehiculoMapper.toDto(vehiculoActualizado);

        return ResponseEntity.ok(responseDto);
    }

    /**
     * Obtiene un vehículo por ID.
     */
    @GetMapping("/empresa/{empresaId}/{vehiculoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    @Operation(summary = "Obtener vehículo", description = "Obtiene un vehículo por su ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Vehículo encontrado"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta operación"),
        @ApiResponse(responseCode = "404", description = "Vehículo no encontrado")
    })
    public ResponseEntity<VehiculoDto> obtenerVehiculo(
            @Parameter(description = "ID de la empresa") @PathVariable Long empresaId,
            @Parameter(description = "ID del vehículo") @PathVariable Long vehiculoId) {
        
        return vehiculoService.obtenerVehiculoPorId(empresaId, vehiculoId)
                .map(vehiculoMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Lista todos los vehículos activos de una empresa.
     */
    @GetMapping("/empresa/{empresaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    @Operation(summary = "Listar vehículos", description = "Lista todos los vehículos activos de una empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de vehículos"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta operación")
    })
    public ResponseEntity<List<VehiculoDto>> listarVehiculos(
            @Parameter(description = "ID de la empresa") @PathVariable Long empresaId) {
        
        List<Vehiculo> vehiculos = vehiculoService.listarVehiculosPorEmpresa(empresaId);
        List<VehiculoDto> vehiculosDto = vehiculos.stream()
                .map(vehiculoMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(vehiculosDto);
    }

    /**
     * Lista vehículos con paginación.
     */
    @GetMapping("/empresa/{empresaId}/paginado")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    @Operation(summary = "Listar vehículos paginados", description = "Lista vehículos con paginación y ordenamiento")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Página de vehículos"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta operación")
    })
    public ResponseEntity<Page<VehiculoDto>> listarVehiculosPaginados(
            @Parameter(description = "ID de la empresa") @PathVariable Long empresaId,
            @Parameter(description = "Número de página (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campo de ordenamiento") @RequestParam(defaultValue = "matricula") String sortBy,
            @Parameter(description = "Dirección de ordenamiento") @RequestParam(defaultValue = "ASC") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("DESC") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Vehiculo> vehiculosPage = vehiculoService.listarVehiculosPaginados(empresaId, pageable);
        Page<VehiculoDto> vehiculosDtoPage = vehiculosPage.map(vehiculoMapper::toDto);

        return ResponseEntity.ok(vehiculosDtoPage);
    }

    /**
     * Busca vehículos por matrícula o marca.
     */
    @GetMapping("/empresa/{empresaId}/buscar")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    @Operation(summary = "Buscar vehículos", description = "Busca vehículos por matrícula o marca con autocompletado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de vehículos encontrados"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta operación")
    })
    public ResponseEntity<List<VehiculoDto>> buscarVehiculos(
            @Parameter(description = "ID de la empresa") @PathVariable Long empresaId,
            @Parameter(description = "Término de búsqueda") @RequestParam(required = false) String q) {
        
        List<Vehiculo> vehiculos = vehiculoService.buscarVehiculos(empresaId, q);
        List<VehiculoDto> vehiculosDto = vehiculos.stream()
                .map(vehiculoMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(vehiculosDto);
    }

    /**
     * Busca vehículos con filtros múltiples y paginación.
     */
    @GetMapping("/empresa/{empresaId}/filtrar")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    @Operation(summary = "Buscar vehículos con filtros", description = "Busca vehículos con múltiples filtros y paginación")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Página de vehículos encontrados"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta operación")
    })
    public ResponseEntity<Page<VehiculoDto>> buscarVehiculosConFiltros(
            @Parameter(description = "ID de la empresa") @PathVariable Long empresaId,
            @Parameter(description = "Término de búsqueda") @RequestParam(required = false) String q,
            @Parameter(description = "Estado activo") @RequestParam(required = false) Boolean activo,
            @Parameter(description = "Número de página (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campo de ordenamiento") @RequestParam(defaultValue = "matricula") String sortBy,
            @Parameter(description = "Dirección de ordenamiento") @RequestParam(defaultValue = "ASC") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("DESC") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Vehiculo> vehiculosPage = vehiculoService.buscarVehiculosPaginados(empresaId, q, activo, pageable);
        Page<VehiculoDto> vehiculosDtoPage = vehiculosPage.map(vehiculoMapper::toDto);

        return ResponseEntity.ok(vehiculosDtoPage);
    }

    /**
     * Desactiva un vehículo.
     */
    @DeleteMapping("/empresa/{empresaId}/{vehiculoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN')")
    @Operation(summary = "Desactivar vehículo", description = "Desactiva un vehículo (soft delete)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Vehículo desactivado exitosamente"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta operación"),
        @ApiResponse(responseCode = "404", description = "Vehículo no encontrado")
    })
    public ResponseEntity<Void> desactivarVehiculo(
            @Parameter(description = "ID de la empresa") @PathVariable Long empresaId,
            @Parameter(description = "ID del vehículo") @PathVariable Long vehiculoId) {
        
        vehiculoService.desactivarVehiculo(empresaId, vehiculoId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reactiva un vehículo.
     */
    @PatchMapping("/empresa/{empresaId}/{vehiculoId}/reactivar")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN')")
    @Operation(summary = "Reactivar vehículo", description = "Reactiva un vehículo desactivado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Vehículo reactivado exitosamente"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta operación"),
        @ApiResponse(responseCode = "404", description = "Vehículo no encontrado")
    })
    public ResponseEntity<Void> reactivarVehiculo(
            @Parameter(description = "ID de la empresa") @PathVariable Long empresaId,
            @Parameter(description = "ID del vehículo") @PathVariable Long vehiculoId) {
        
        vehiculoService.reactivarVehiculo(empresaId, vehiculoId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Cuenta vehículos activos de una empresa.
     */
    @GetMapping("/empresa/{empresaId}/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    @Operation(summary = "Contar vehículos", description = "Cuenta los vehículos activos de una empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cantidad de vehículos"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta operación")
    })
    public ResponseEntity<Long> contarVehiculos(
            @Parameter(description = "ID de la empresa") @PathVariable Long empresaId) {
        
        long count = vehiculoService.contarVehiculosActivos(empresaId);
        return ResponseEntity.ok(count);
    }
}
