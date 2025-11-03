package com.frcefact.controller;

import com.frcefact.dto.ClienteDto;
import com.frcefact.dto.mapper.ClienteMapper;
import com.frcefact.model.Cliente;
import com.frcefact.service.ClienteService;
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
 * Controlador REST para gestión de clientes.
 * Proporciona endpoints CRUD con búsqueda y paginación.
 */
@RestController
@RequestMapping("/clientes")
@Tag(name = "Clientes", description = "API para gestión de clientes")
public class ClienteController {

    private static final Logger logger = LoggerFactory.getLogger(ClienteController.class);

    private final ClienteService clienteService;
    private final ClienteMapper clienteMapper;

    public ClienteController(ClienteService clienteService, ClienteMapper clienteMapper) {
        this.clienteService = clienteService;
        this.clienteMapper = clienteMapper;
    }

    /**
     * Crea un nuevo cliente.
     */
    @PostMapping("/empresa/{empresaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR')")
    @Operation(summary = "Crear cliente", description = "Crea un nuevo cliente para una empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Cliente creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta operación"),
        @ApiResponse(responseCode = "404", description = "Empresa no encontrada")
    })
    public ResponseEntity<ClienteDto> crearCliente(
            @Parameter(description = "ID de la empresa") @PathVariable Long empresaId,
            @Valid @RequestBody ClienteDto clienteDto) {
        
        Cliente cliente = clienteMapper.toEntity(clienteDto);
        Cliente clienteCreado = clienteService.crearCliente(empresaId, cliente);
        ClienteDto responseDto = clienteMapper.toDto(clienteCreado);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    /**
     * Actualiza un cliente existente.
     */
    @PutMapping("/empresa/{empresaId}/{clienteId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR')")
    @Operation(summary = "Actualizar cliente", description = "Actualiza un cliente existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cliente actualizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta operación"),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ResponseEntity<ClienteDto> actualizarCliente(
            @Parameter(description = "ID de la empresa") @PathVariable Long empresaId,
            @Parameter(description = "ID del cliente") @PathVariable Long clienteId,
            @Valid @RequestBody ClienteDto clienteDto) {
        
        Cliente cliente = clienteMapper.toEntity(clienteDto);
        Cliente clienteActualizado = clienteService.actualizarCliente(empresaId, clienteId, cliente);
        ClienteDto responseDto = clienteMapper.toDto(clienteActualizado);

        return ResponseEntity.ok(responseDto);
    }

    /**
     * Obtiene un cliente por ID.
     */
    @GetMapping("/empresa/{empresaId}/{clienteId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    @Operation(summary = "Obtener cliente", description = "Obtiene un cliente por su ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta operación"),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ResponseEntity<ClienteDto> obtenerCliente(
            @Parameter(description = "ID de la empresa") @PathVariable Long empresaId,
            @Parameter(description = "ID del cliente") @PathVariable Long clienteId) {
        
        return clienteService.obtenerClientePorId(empresaId, clienteId)
                .map(clienteMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Lista todos los clientes activos de una empresa.
     */
    @GetMapping("/empresa/{empresaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    @Operation(summary = "Listar clientes", description = "Lista todos los clientes activos de una empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de clientes"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta operación")
    })
    public ResponseEntity<List<ClienteDto>> listarClientes(
            @Parameter(description = "ID de la empresa") @PathVariable Long empresaId) {
        
        List<Cliente> clientes = clienteService.listarClientesPorEmpresa(empresaId);
        List<ClienteDto> clientesDto = clientes.stream()
                .map(clienteMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(clientesDto);
    }

    /**
     * Lista clientes con paginación.
     */
    @GetMapping("/empresa/{empresaId}/paginado")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    @Operation(summary = "Listar clientes paginados", description = "Lista clientes con paginación y ordenamiento")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Página de clientes"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta operación")
    })
    public ResponseEntity<Page<ClienteDto>> listarClientesPaginados(
            @Parameter(description = "ID de la empresa") @PathVariable Long empresaId,
            @Parameter(description = "Número de página (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campo de ordenamiento") @RequestParam(defaultValue = "nombre") String sortBy,
            @Parameter(description = "Dirección de ordenamiento") @RequestParam(defaultValue = "ASC") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("DESC") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Cliente> clientesPage = clienteService.listarClientesPaginados(empresaId, pageable);
        Page<ClienteDto> clientesDtoPage = clientesPage.map(clienteMapper::toDto);

        return ResponseEntity.ok(clientesDtoPage);
    }

    /**
     * Busca clientes por nombre, razón social o RUC.
     */
    @GetMapping("/empresa/{empresaId}/buscar")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    @Operation(summary = "Buscar clientes", description = "Busca clientes por nombre, razón social o RUC con autocompletado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de clientes encontrados"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta operación")
    })
    public ResponseEntity<List<ClienteDto>> buscarClientes(
            @Parameter(description = "ID de la empresa") @PathVariable Long empresaId,
            @Parameter(description = "Término de búsqueda") @RequestParam(required = false) String q) {
        
        List<Cliente> clientes = clienteService.buscarClientes(empresaId, q);
        List<ClienteDto> clientesDto = clientes.stream()
                .map(clienteMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(clientesDto);
    }

    /**
     * Busca clientes con paginación.
     */
    @GetMapping("/empresa/{empresaId}/buscar/paginado")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    @Operation(summary = "Buscar clientes paginados", description = "Busca clientes con paginación")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Página de clientes encontrados"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta operación")
    })
    public ResponseEntity<Page<ClienteDto>> buscarClientesPaginados(
            @Parameter(description = "ID de la empresa") @PathVariable Long empresaId,
            @Parameter(description = "Término de búsqueda") @RequestParam(required = false) String q,
            @Parameter(description = "Número de página (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campo de ordenamiento") @RequestParam(defaultValue = "nombre") String sortBy,
            @Parameter(description = "Dirección de ordenamiento") @RequestParam(defaultValue = "ASC") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("DESC") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Cliente> clientesPage = clienteService.buscarClientesPaginados(empresaId, q, pageable);
        Page<ClienteDto> clientesDtoPage = clientesPage.map(clienteMapper::toDto);

        return ResponseEntity.ok(clientesDtoPage);
    }

    /**
     * Busca clientes con filtros múltiples y paginación.
     */
    @GetMapping("/empresa/{empresaId}/filtrar")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    @Operation(summary = "Buscar clientes con filtros", description = "Busca clientes con múltiples filtros y paginación")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Página de clientes encontrados"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta operación")
    })
    public ResponseEntity<Page<ClienteDto>> buscarClientesConFiltros(
            @Parameter(description = "ID de la empresa") @PathVariable Long empresaId,
            @Parameter(description = "Término de búsqueda") @RequestParam(required = false) String q,
            @Parameter(description = "Tipo de cliente SIFEN") @RequestParam(required = false) String tipoClienteSifen,
            @Parameter(description = "Estado activo") @RequestParam(required = false) Boolean activo,
            @Parameter(description = "Número de página (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campo de ordenamiento") @RequestParam(defaultValue = "razonSocial") String sortBy,
            @Parameter(description = "Dirección de ordenamiento") @RequestParam(defaultValue = "ASC") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("DESC") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // Convertir String a Enum si se proporciona
        com.frcefact.model.TipoClienteSifen tipoClienteEnum = null;
        if (tipoClienteSifen != null && !tipoClienteSifen.trim().isEmpty()) {
            try {
                tipoClienteEnum = com.frcefact.model.TipoClienteSifen.valueOf(tipoClienteSifen);
            } catch (IllegalArgumentException e) {
                // Tipo de cliente SIFEN inválido, se ignorará el filtro
            }
        }

        Page<Cliente> clientesPage = clienteService.buscarClientesConFiltros(
                empresaId, q, tipoClienteEnum, activo, pageable);
        Page<ClienteDto> clientesDtoPage = clientesPage.map(clienteMapper::toDto);

        return ResponseEntity.ok(clientesDtoPage);
    }

    /**
     * Busca un cliente por RUC.
     */
    @GetMapping("/empresa/{empresaId}/ruc/{ruc}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    @Operation(summary = "Buscar por RUC", description = "Busca un cliente por su RUC")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta operación"),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ResponseEntity<ClienteDto> buscarPorRuc(
            @Parameter(description = "ID de la empresa") @PathVariable Long empresaId,
            @Parameter(description = "RUC del cliente") @PathVariable String ruc) {
        
        return clienteService.buscarPorRuc(empresaId, ruc)
                .map(clienteMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Verifica si existe un cliente con el RUC especificado para la empresa.
     * Útil para validación en tiempo real desde el frontend.
     */
    @GetMapping("/empresa/{empresaId}/ruc/{ruc}/existe")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    @Operation(summary = "Verificar existencia de RUC", description = "Verifica si existe un cliente activo con el RUC especificado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Respuesta con información de existencia"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta operación")
    })
    public ResponseEntity<Boolean> verificarRucExiste(
            @Parameter(description = "ID de la empresa") @PathVariable Long empresaId,
            @Parameter(description = "RUC a verificar") @PathVariable String ruc,
            @Parameter(description = "ID del cliente a excluir (para edición)") @RequestParam(required = false) Long excluirClienteId) {
        
        boolean existe = clienteService.existeRucEnEmpresa(empresaId, ruc, excluirClienteId);
        return ResponseEntity.ok(existe);
    }

    /**
     * Desactiva un cliente.
     */
    @DeleteMapping("/empresa/{empresaId}/{clienteId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN')")
    @Operation(summary = "Desactivar cliente", description = "Desactiva un cliente (soft delete)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Cliente desactivado exitosamente"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta operación"),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ResponseEntity<Void> desactivarCliente(
            @Parameter(description = "ID de la empresa") @PathVariable Long empresaId,
            @Parameter(description = "ID del cliente") @PathVariable Long clienteId) {
        
        clienteService.desactivarCliente(empresaId, clienteId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reactiva un cliente.
     */
    @PatchMapping("/empresa/{empresaId}/{clienteId}/reactivar")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN')")
    @Operation(summary = "Reactivar cliente", description = "Reactiva un cliente desactivado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Cliente reactivado exitosamente"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta operación"),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ResponseEntity<Void> reactivarCliente(
            @Parameter(description = "ID de la empresa") @PathVariable Long empresaId,
            @Parameter(description = "ID del cliente") @PathVariable Long clienteId) {
        
        clienteService.reactivarCliente(empresaId, clienteId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Cuenta clientes activos de una empresa.
     */
    @GetMapping("/empresa/{empresaId}/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    @Operation(summary = "Contar clientes", description = "Cuenta los clientes activos de una empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cantidad de clientes"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta operación")
    })
    public ResponseEntity<Long> contarClientes(
            @Parameter(description = "ID de la empresa") @PathVariable Long empresaId) {
        
        long count = clienteService.contarClientesActivos(empresaId);
        return ResponseEntity.ok(count);
    }
}
