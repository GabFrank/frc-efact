package com.frcefact.controller;

import com.frcefact.dto.FacturaLegalDto;
import com.frcefact.dto.FacturaLegalItemDto;
import com.frcefact.dto.GenerarDeResponse;
import com.frcefact.dto.LoteDeDto;
import com.frcefact.dto.mapper.FacturaLegalMapper;
import com.frcefact.dto.mapper.DocumentoElectronicoMapper;
import com.frcefact.model.FacturaLegal;
import com.frcefact.model.FacturaLegalItem;
import com.frcefact.service.FacturaLegalService;
import com.frcefact.service.DocumentoElectronicoService;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
@RequestMapping("/facturas")
@Tag(name = "Facturas Legales", description = "API para gestión de facturas legales")
@SecurityRequirement(name = "bearer-jwt")
public class FacturaLegalController {

    private static final Logger logger = LoggerFactory.getLogger(FacturaLegalController.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final FacturaLegalService facturaLegalService;
    private final DocumentoElectronicoService documentoElectronicoService;
    private final FacturaLegalMapper facturaLegalMapper;
    private final DocumentoElectronicoMapper documentoElectronicoMapper;
    private final com.frcefact.service.KudePdfService kudePdfService;
    private final com.frcefact.service.EmailFacturaElectronicaService emailFacturaElectronicaService;

    public FacturaLegalController(
            FacturaLegalService facturaLegalService,
            DocumentoElectronicoService documentoElectronicoService,
            FacturaLegalMapper facturaLegalMapper,
            DocumentoElectronicoMapper documentoElectronicoMapper,
            com.frcefact.service.KudePdfService kudePdfService,
            com.frcefact.service.EmailFacturaElectronicaService emailFacturaElectronicaService) {
        this.facturaLegalService = facturaLegalService;
        this.documentoElectronicoService = documentoElectronicoService;
        this.facturaLegalMapper = facturaLegalMapper;
        this.documentoElectronicoMapper = documentoElectronicoMapper;
        this.kudePdfService = kudePdfService;
        this.emailFacturaElectronicaService = emailFacturaElectronicaService;
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
        
        logger.info("POST /facturas - Crear factura para empresa ID: {}", facturaDto.getEmpresaId());
        logger.debug("Datos recibidos - Moneda: {}, Cambio: {}, Total Parcial: {}, IVA 5%: {}, IVA 10%: {}, Total Final: {}", 
                facturaDto.getMonedaExtranjera(), 
                facturaDto.getCambio(),
                facturaDto.getTotalParcial(),
                facturaDto.getIvaParcial5(),
                facturaDto.getIvaParcial10(),
                facturaDto.getTotalFinal());
        logger.debug("Cantidad de items: {}", facturaDto.getItems() != null ? facturaDto.getItems().size() : 0);
        if (facturaDto.getItems() != null && !facturaDto.getItems().isEmpty()) {
            int index = 0;
            for (var item : facturaDto.getItems()) {
                logger.debug("Item {} - ProductoId: {}, Cantidad: {}, PrecioUnitario: {}, Total: {}", 
                        index++, item.getProductoId(), item.getCantidad(), item.getPrecioUnitario(), item.getTotal());
            }
        }
        
        FacturaLegal factura = facturaLegalMapper.toEntity(facturaDto);
        logger.debug("Factura mapeada - Moneda: {}, Cambio: {}, Total Parcial: {}, Total Final: {}", 
                factura.getMonedaExtranjera(), 
                factura.getCambio(),
                factura.getTotalParcial(),
                factura.getTotalFinal());
        
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
        
        logger.info("GET /facturas/{} - Obtener factura", id);
        
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
        
        logger.info("PUT /facturas/{} - Actualizar factura", id);
        logger.debug("Datos recibidos - Moneda: {}, Cambio: {}, Total Parcial: {}, IVA 5%: {}, IVA 10%: {}, Total Final: {}", 
                facturaDto.getMonedaExtranjera(), 
                facturaDto.getCambio(),
                facturaDto.getTotalParcial(),
                facturaDto.getIvaParcial5(),
                facturaDto.getIvaParcial10(),
                facturaDto.getTotalFinal());
        logger.debug("Cantidad de items: {}", facturaDto.getItems() != null ? facturaDto.getItems().size() : 0);
        if (facturaDto.getItems() != null && !facturaDto.getItems().isEmpty()) {
            int index = 0;
            for (var item : facturaDto.getItems()) {
                logger.debug("Item {} - ProductoId: {}, Cantidad: {}, PrecioUnitario: {}, Total: {}", 
                        index++, item.getProductoId(), item.getCantidad(), item.getPrecioUnitario(), item.getTotal());
            }
        }
        
        FacturaLegal factura = facturaLegalMapper.toEntity(facturaDto);
        logger.debug("Factura mapeada - Moneda: {}, Cambio: {}, Total Parcial: {}, Total Final: {}", 
                factura.getMonedaExtranjera(), 
                factura.getCambio(),
                factura.getTotalParcial(),
                factura.getTotalFinal());
        
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
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<Page<FacturaLegalDto>> listarFacturas(
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId,
            @Parameter(description = "ID del cliente (opcional)") @RequestParam(required = false) Long clienteId,
            @Parameter(description = "Fecha desde (formato ISO)") @RequestParam(required = false) String fechaDesde,
            @Parameter(description = "Fecha hasta (formato ISO)") @RequestParam(required = false) String fechaHasta,
            @Parameter(description = "Estado del documento electrónico (opcional)") @RequestParam(required = false) String estado,
            @Parameter(description = "Número de página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campo de ordenamiento") @RequestParam(defaultValue = "fecha") String sortBy,
            @Parameter(description = "Dirección de ordenamiento") @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        logger.info("GET /facturas - Listar facturas de empresa ID: {}, estado: {}", empresaId, estado);
        
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
                empresaId, clienteId, fechaDesdeDate, fechaHastaDate, estado, pageable);
        
        // Convertir a DTOs
        Page<FacturaLegalDto> facturasDto = facturas.map(facturaLegalMapper::toDto);
        
        return ResponseEntity.ok(facturasDto);
    }

    @Operation(summary = "Obtener resumen de facturas",
               description = "Obtiene el resumen de facturas aprobadas y no aprobadas, con desglose por IVA. Solo filtra por fechas si se proporcionan.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Resumen obtenido exitosamente"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta empresa")
    })
    @GetMapping("/resumen")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    public ResponseEntity<com.frcefact.dto.ResumenFacturasDto> obtenerResumen(
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId,
            @Parameter(description = "Fecha desde (formato ISO, opcional)") @RequestParam(required = false) String fechaDesde,
            @Parameter(description = "Fecha hasta (formato ISO, opcional)") @RequestParam(required = false) String fechaHasta) {
        
        logger.info("GET /facturas/resumen - Obtener resumen para empresa ID: {}", empresaId);
        
        // Parsear fechas
        LocalDateTime fechaDesdeDate = null;
        LocalDateTime fechaHastaDate = null;
        
        if (fechaDesde != null && !fechaDesde.isEmpty()) {
            fechaDesdeDate = LocalDateTime.parse(fechaDesde, FORMATTER);
        }
        if (fechaHasta != null && !fechaHasta.isEmpty()) {
            fechaHastaDate = LocalDateTime.parse(fechaHasta, FORMATTER);
        }
        
        com.frcefact.dto.ResumenFacturasDto resumen = facturaLegalService.obtenerResumenFacturas(
                empresaId, fechaDesdeDate, fechaHastaDate);
        
        return ResponseEntity.ok(resumen);
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
        
        logger.info("POST /facturas/{}/items - Agregar item", id);
        
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
        
        logger.info("DELETE /facturas/{}/items/{} - Eliminar item", facturaId, itemId);
        
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
        
        logger.info("PUT /facturas/{}/descuento - Aplicar descuento: {}", id, descuento);
        
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
        
        logger.info("POST /facturas/{}/recalcular - Recalcular totales", id);
        
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
        
        logger.info("DELETE /facturas/{} - Desactivar factura", id);
        
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
        
        logger.info("GET /facturas/estadisticas - Empresa ID: {}", empresaId);
        
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
     * POST /facturas/{id}/generar-de
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
    public ResponseEntity<GenerarDeResponse> generarDocumentoElectronico(
            @Parameter(description = "ID de la factura") @PathVariable Long id) {
        
        logger.info("POST /facturas/{}/generar-de - Generar documento electrónico", id);
        
        var resultado = documentoElectronicoService.generarYEnviarDesdeFactura(id);
        var documentoDto = documentoElectronicoMapper.toDto(resultado.documento());
        var loteDto = LoteDeDto.fromEntity(resultado.lote());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(GenerarDeResponse.of(documentoDto, loteDto));
    }

    /**
     * POST /facturas/{id}/desvincular-de
     */
    @Operation(summary = "Desvincular documento electrónico",
               description = "Desvincula el documento electrónico (DE) de una factura. Solo permitido si el DE tiene error permanente (ERROR o RECHAZADO)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Documento electrónico desvinculado exitosamente"),
        @ApiResponse(responseCode = "400", description = "No se puede desvincular el DE (estado no permite desvinculación)"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta empresa"),
        @ApiResponse(responseCode = "404", description = "Factura o DE no encontrado")
    })
    @PostMapping("/{id}/desvincular-de")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR')")
    public ResponseEntity<Void> desvincularDocumentoElectronico(
            @Parameter(description = "ID de la factura") @PathVariable Long id) {
        
        logger.info("POST /facturas/{}/desvincular-de - Desvincular documento electrónico", id);
        
        documentoElectronicoService.desvincularDE(id);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Generar PDF del KUDE",
               description = "Genera el PDF de la representación gráfica del documento electrónico (KUDE) de una factura")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "PDF generado exitosamente",
                    content = @Content(mediaType = "application/pdf")),
        @ApiResponse(responseCode = "400", description = "Error al generar el PDF"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta empresa"),
        @ApiResponse(responseCode = "404", description = "Factura no encontrada")
    })
    @GetMapping("/{id}/kude-pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    public ResponseEntity<byte[]> generarPdfKude(
            @Parameter(description = "ID de la factura") @PathVariable Long id) {
        
        logger.info("GET /facturas/{}/kude-pdf - Generar PDF KUDE", id);
        
        try {
            // Obtener la factura con todas sus relaciones
            FacturaLegal factura = facturaLegalService.obtenerPorId(id);
            
            // Verificar que la factura tenga documento electrónico
            if (factura.getDocumentoElectronico() == null) {
                logger.warn("La factura {} no tiene documento electrónico asociado", id);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("La factura no tiene documento electrónico asociado".getBytes());
            }
            
            // Generar el PDF
            byte[] pdfBytes = kudePdfService.generarPdfKude(factura);
            
            // Preparar headers para descarga
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", 
                    "KUDE_" + factura.getNumeroFacturaFormateado() + ".pdf");
            headers.setContentLength(pdfBytes.length);
            
            logger.info("PDF KUDE generado exitosamente para factura ID: {}", id);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
                    
        } catch (Exception e) {
            logger.error("Error al generar PDF KUDE para factura ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error al generar PDF: " + e.getMessage()).getBytes());
        }
    }

    @Operation(summary = "Reenviar email de factura electrónica",
               description = "Reenvía el email con la factura electrónica (XML y PDF) al cliente. Solo disponible para facturas con DE en estado APROBADO.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Email reenviado exitosamente"),
        @ApiResponse(responseCode = "400", description = "La factura no tiene DE o el DE no está en estado APROBADO"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta empresa"),
        @ApiResponse(responseCode = "404", description = "Factura no encontrada")
    })
    @PostMapping("/{id}/reenviar-email")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR')")
    public ResponseEntity<Map<String, String>> reenviarEmail(
            @Parameter(description = "ID de la factura") @PathVariable Long id) {
        
        logger.info("POST /facturas/{}/reenviar-email - Reenviar email", id);
        
        // Obtener la factura
        FacturaLegal factura = facturaLegalService.obtenerPorId(id);
        
        // Validar que tenga documento electrónico
        if (factura.getDocumentoElectronico() == null) {
            logger.warn("La factura {} no tiene documento electrónico asociado", id);
            Map<String, String> error = new HashMap<>();
            error.put("error", "La factura no tiene documento electrónico asociado");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        
        com.frcefact.model.DocumentoElectronico documento = factura.getDocumentoElectronico();
        
        // Validar que el DE esté en estado APROBADO
        if (documento.getEstado() != com.frcefact.model.EstadoDE.APROBADO) {
            logger.warn("El documento electrónico de la factura {} no está en estado APROBADO. Estado actual: {}", 
                    id, documento.getEstado());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Solo se puede reenviar email para documentos electrónicos en estado APROBADO. Estado actual: " + documento.getEstado());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        
        // Validar que la factura tenga cliente asociado
        com.frcefact.model.Cliente cliente = factura.getCliente();
        if (cliente == null) {
            logger.warn("La factura {} no tiene cliente asociado (SIN NOMBRE)", id);
            Map<String, String> error = new HashMap<>();
            error.put("error", "CLIENTE_SIN_NOMBRE");
            error.put("message", "No se puede enviar email a un cliente SIN NOMBRE. La factura debe tener un cliente nominado.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        
        // Validar que el cliente no sea "SIN NOMBRE"
        String nombreCliente = cliente.getNombre();
        String razonSocialCliente = cliente.getRazonSocial();
        boolean esSinNombre = (nombreCliente != null && (nombreCliente.equalsIgnoreCase("SIN NOMBRE") || 
                                                          nombreCliente.equalsIgnoreCase("Sin Nombre"))) ||
                              (razonSocialCliente != null && (razonSocialCliente.equalsIgnoreCase("SIN NOMBRE") || 
                                                               razonSocialCliente.equalsIgnoreCase("Sin Nombre")));
        
        if (esSinNombre) {
            logger.warn("La factura {} tiene un cliente SIN NOMBRE (ID: {})", id, cliente.getId());
            Map<String, String> error = new HashMap<>();
            error.put("error", "CLIENTE_SIN_NOMBRE");
            error.put("message", "No se puede enviar email a un cliente SIN NOMBRE. La factura debe tener un cliente nominado.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        
        // Validar que el cliente tenga email configurado
        String emailCliente = cliente.getEmail();
        if (emailCliente == null || emailCliente.isBlank()) {
            logger.warn("El cliente {} de la factura {} no tiene email configurado", cliente.getId(), id);
            Map<String, String> error = new HashMap<>();
            error.put("error", "CLIENTE_SIN_EMAIL");
            error.put("message", "El cliente no tiene un email configurado. Por favor, configure el email del cliente antes de reenviar.");
            error.put("clienteId", cliente.getId().toString());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        
        // Reenviar email
        try {
            emailFacturaElectronicaService.enviarFacturaAlClienteAsync(documento);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Email reenviado exitosamente");
            logger.info("Email reenviado exitosamente para factura ID: {}", id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al reenviar email para factura ID: {}", id, e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al reenviar email: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
