package com.frcefact.controller;

import com.frcefact.dto.FacturaLegalDto;
import com.frcefact.dto.FacturaLegalItemDto;
import com.frcefact.dto.mapper.FacturaLegalMapper;
import com.frcefact.model.FacturaLegal;
import com.frcefact.model.FacturaLegalItem;
import com.frcefact.service.FacturaLegalService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST para gestión de facturas legales.
 * Proporciona endpoints para CRUD, búsqueda y gestión de items.
 */
@RestController
@RequestMapping("/api/facturas")
@Tag(name = "Facturas Legales", description = "API para gestión de facturas legales")
@SecurityRequirement(name = "bearer-jwt")
public class FacturaLegalController {

    private static final Logger logger = LoggerFactory.getLogger(FacturaLegalController.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final FacturaLegalService facturaLegalService;
    private final FacturaLegalMapper facturaLegalMapper;

    public FacturaLegalController(
            FacturaLegalService facturaLegalService,
            FacturaLegalMapper facturaLegalMapper) {
        this.facturaLegalService = facturaLegalService;
        this.facturaLegalMapper = facturaLegalMapper;
    }

    @Operation(summary = "Crear una nueva factura legal",
               description = "Crea una nueva factura legal con items. Asigna automáticamente el número de factura del timbrado detalle.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Factura creada exitosamente",
                    content = @Content(schema = @Schema(implementation = FacturaLegalDto.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o factura sin items"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta empresa"),
        @ApiResponse(responseCode = "404", description = "Empresa, timbrado detalle o cliente no encontrado"),
        @ApiResponse(responseCode = "409", description = "Timbrado vencido o sin números disponibles")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR')")
    public ResponseEntity<FacturaLegalDto> crearFactura(
            @Valid @RequestBody FacturaLegalDto facturaDto) {
        
        logger.info("POST /api/facturas - Crear factura para empresa ID: {}", facturaDto.getEmpresaId());
        
        FacturaLegal factura = facturaLegalMapper.toEntity(facturaDto);
        FacturaLegal facturaCreada = facturaLegalService.crearFactura(factura);
        FacturaLegalDto responseDto = facturaLegalMapper.toDto(facturaCreada);
        
        logger.info("Factura creada exitosamente: {} - Número: {}", 
                facturaCreada.getId(), facturaCreada.getNumeroFacturaFormateado());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @Operation(summary = "Obtener una factura por ID",
               description = "Obtiene los detalles completos de una factura incluyendo sus items")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Factura encontrada",
                    content = @Content(schema = @Schema(implementation = FacturaLegalDto.class))),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta empresa"),
        @ApiResponse(responseCode = "404", description = "Factura no encontrada")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    public ResponseEntity<FacturaLegalDto> obtenerFactura(
            @Parameter(description = "ID de la factura") @PathVariable Long id) {
        
        logger.info("GET /api/facturas/{} - Obtener factura", id);
        
        FacturaLegal factura = facturaLegalService.obtenerPorId(id);
        FacturaLegalDto responseDto = facturaLegalMapper.toDto(factura);
        
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "Actualizar una factura existente",
               description = "Actualiza los datos de una factura. No permite modificar items, usar endpoints específicos para eso.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Factura actualizada exitosamente",
                    content = @Content(schema = @Schema(implementation = FacturaLegalDto.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta empresa"),
        @ApiResponse(responseCode = "404", description = "Factura no encontrada")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR')")
    public ResponseEntity<FacturaLegalDto> actualizarFactura(
            @Parameter(description = "ID de la factura") @PathVariable Long id,
            @Valid @RequestBody FacturaLegalDto facturaDto) {
        
        logger.info("PUT /api/facturas/{} - Actualizar factura", id);
        
        FacturaLegal factura = facturaLegalMapper.toEntity(facturaDto);
        FacturaLegal facturaActualizada = facturaLegalService.actualizar(id, factura);
        FacturaLegalDto responseDto = facturaLegalMapper.toDto(facturaActualizada);
        
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "Listar facturas con filtros",
               description = "Lista facturas de una empresa con filtros opcionales de cliente y rango de fechas")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de facturas obtenida exitosamente"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta empresa")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    public ResponseEntity<Page<FacturaLegalDto>> listarFacturas(
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId,
            @Parameter(description = "ID del cliente (opcional)") @RequestParam(required = false) Long clienteId,
            @Parameter(description = "Fecha desde (formato ISO)") @RequestParam(required = false) String fechaDesde,
            @Parameter(description = "Fecha hasta (formato ISO)") @RequestParam(required = false) String fechaHasta,
            @Parameter(description = "Número de página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campo de ordenamiento") @RequestParam(defaultValue = "fecha") String sortBy,
            @Parameter(description = "Dirección de ordenamiento") @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        logger.info("GET /api/facturas - Listar facturas de empresa ID: {}", empresaId);
        
        // Parsear fechas
        LocalDateTime fechaDesdeDate = null;
        LocalDateTime fechaHastaDate = null;
        
        if (fechaDesde != null && !fechaDesde.isEmpty()) {
            fechaDesdeDate = LocalDateTime.parse(fechaDesde, FORMATTER);
        }
        if (fechaHasta != null && !fechaHasta.isEmpty()) {
            fechaHastaDate = LocalDateTime.parse(fechaHasta, FORMATTER);
        }
        
        // Crear pageable
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ? 
                Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        // Obtener facturas
        Page<FacturaLegal> facturas = facturaLegalService.listarConFiltros(
                empresaId, clienteId, fechaDesdeDate, fechaHastaDate, pageable);
        
        // Convertir a DTOs
        Page<FacturaLegalDto> facturasDto = facturas.map(facturaLegalMapper::toDto);
        
        return ResponseEntity.ok(facturasDto);
    }

    @Operation(summary = "Agregar item a una factura",
               description = "Agrega un nuevo item a una factura existente. Recalcula automáticamente los totales.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Item agregado exitosamente",
                    content = @Content(schema = @Schema(implementation = FacturaLegalDto.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta empresa"),
        @ApiResponse(responseCode = "404", description = "Factura o producto no encontrado")
    })
    @PostMapping("/{id}/items")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR')")
    public ResponseEntity<FacturaLegalDto> agregarItem(
            @Parameter(description = "ID de la factura") @PathVariable Long id,
            @Valid @RequestBody FacturaLegalItemDto itemDto) {
        
        logger.info("POST /api/facturas/{}/items - Agregar item", id);
        
        FacturaLegalItem item = facturaLegalMapper.itemToEntity(itemDto);
        FacturaLegal facturaActualizada = facturaLegalService.agregarItem(id, item);
        FacturaLegalDto responseDto = facturaLegalMapper.toDto(facturaActualizada);
        
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "Eliminar item de una factura",
               description = "Elimina un item de una factura. Recalcula automáticamente los totales.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Item eliminado exitosamente",
                    content = @Content(schema = @Schema(implementation = FacturaLegalDto.class))),
        @ApiResponse(responseCode = "400", description = "No se puede eliminar el último item"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta empresa"),
        @ApiResponse(responseCode = "404", description = "Factura o item no encontrado")
    })
    @DeleteMapping("/{facturaId}/items/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR')")
    public ResponseEntity<FacturaLegalDto> eliminarItem(
            @Parameter(description = "ID de la factura") @PathVariable Long facturaId,
            @Parameter(description = "ID del item") @PathVariable Long itemId) {
        
        logger.info("DELETE /api/facturas/{}/items/{} - Eliminar item", facturaId, itemId);
        
        FacturaLegal facturaActualizada = facturaLegalService.eliminarItem(facturaId, itemId);
        FacturaLegalDto responseDto = facturaLegalMapper.toDto(facturaActualizada);
        
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "Aplicar descuento a una factura",
               description = "Aplica un descuento final a la factura y recalcula el total final")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Descuento aplicado exitosamente",
                    content = @Content(schema = @Schema(implementation = FacturaLegalDto.class))),
        @ApiResponse(responseCode = "400", description = "Descuento inválido"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta empresa"),
        @ApiResponse(responseCode = "404", description = "Factura no encontrada")
    })
    @PutMapping("/{id}/descuento")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR')")
    public ResponseEntity<FacturaLegalDto> aplicarDescuento(
            @Parameter(description = "ID de la factura") @PathVariable Long id,
            @Parameter(description = "Monto del descuento") @RequestParam BigDecimal descuento) {
        
        logger.info("PUT /api/facturas/{}/descuento - Aplicar descuento: {}", id, descuento);
        
        FacturaLegal facturaActualizada = facturaLegalService.aplicarDescuento(id, descuento);
        FacturaLegalDto responseDto = facturaLegalMapper.toDto(facturaActualizada);
        
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "Recalcular totales de una factura",
               description = "Recalcula los totales de una factura basándose en sus items")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Totales recalculados exitosamente",
                    content = @Content(schema = @Schema(implementation = FacturaLegalDto.class))),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta empresa"),
        @ApiResponse(responseCode = "404", description = "Factura no encontrada")
    })
    @PostMapping("/{id}/recalcular")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR')")
    public ResponseEntity<FacturaLegalDto> recalcularTotales(
            @Parameter(description = "ID de la factura") @PathVariable Long id) {
        
        logger.info("POST /api/facturas/{}/recalcular - Recalcular totales", id);
        
        FacturaLegal facturaActualizada = facturaLegalService.recalcularTotalesFactura(id);
        FacturaLegalDto responseDto = facturaLegalMapper.toDto(facturaActualizada);
        
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "Desactivar una factura",
               description = "Desactiva una factura (soft delete)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Factura desactivada exitosamente"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta empresa"),
        @ApiResponse(responseCode = "404", description = "Factura no encontrada")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN')")
    public ResponseEntity<Void> desactivarFactura(
            @Parameter(description = "ID de la factura") @PathVariable Long id) {
        
        logger.info("DELETE /api/facturas/{} - Desactivar factura", id);
        
        facturaLegalService.desactivar(id);
        
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Obtener estadísticas de facturación",
               description = "Obtiene el total facturado y cantidad de facturas en un rango de fechas")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estadísticas obtenidas exitosamente"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta empresa")
    })
    @GetMapping("/estadisticas")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas(
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId,
            @Parameter(description = "Fecha desde (formato ISO)") @RequestParam(required = false) String fechaDesde,
            @Parameter(description = "Fecha hasta (formato ISO)") @RequestParam(required = false) String fechaHasta) {
        
        logger.info("GET /api/facturas/estadisticas - Empresa ID: {}", empresaId);
        
        // Parsear fechas o usar mes actual
        LocalDateTime fechaDesdeDate = fechaDesde != null && !fechaDesde.isEmpty() ?
                LocalDateTime.parse(fechaDesde, FORMATTER) :
                LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        
        LocalDateTime fechaHastaDate = fechaHasta != null && !fechaHasta.isEmpty() ?
                LocalDateTime.parse(fechaHasta, FORMATTER) :
                LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        
        // Obtener estadísticas
        BigDecimal totalFacturado = facturaLegalService.obtenerTotalFacturado(
                empresaId, fechaDesdeDate, fechaHastaDate);
        long cantidadFacturas = facturaLegalService.contarFacturas(
                empresaId, fechaDesdeDate, fechaHastaDate);
        
        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("totalFacturado", totalFacturado);
        estadisticas.put("cantidadFacturas", cantidadFacturas);
        estadisticas.put("fechaDesde", fechaDesdeDate.format(FORMATTER));
        estadisticas.put("fechaHasta", fechaHastaDate.format(FORMATTER));
        
        return ResponseEntity.ok(estadisticas);
    }

    /**
     * Genera un documento electrónico a partir de una factura legal.
     * 
     * POST /api/facturas/{id}/generar-de
     */
    @Operation(summary = "Generar documento electrónico",
               description = "Genera un documento electrónico (DE) a partir de una factura legal")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Documento electrónico generado exitosamente"),
        @ApiResponse(responseCode = "400", description = "La factura ya tiene un DE o certificado no válido"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta empresa"),
        @ApiResponse(responseCode = "404", description = "Factura no encontrada")
    })
    @PostMapping("/{id}/generar-de")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR')")
    public ResponseEntity<Map<String, Object>> generarDocumentoElectronico(
            @Parameter(description = "ID de la factura") @PathVariable Long id) {
        
        logger.info("POST /api/facturas/{}/generar-de - Generar documento electrónico", id);
        
        // Nota: La implementación completa se agregará cuando se inyecte DocumentoElectronicoService
        // Por ahora, retornamos un mensaje indicando que la funcionalidad está disponible
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Endpoint disponible. Implementación completa en progreso.");
        response.put("facturaId", id);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
