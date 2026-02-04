package com.frcefact.controller;

import com.frcefact.dto.ProductoDto;
import com.frcefact.dto.mapper.ProductoMapper;
import com.frcefact.model.Producto;
import com.frcefact.service.ProductoService;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controlador REST para gestión de productos.
 * Proporciona endpoints para CRUD, búsqueda e importación masiva.
 */
@RestController
@RequestMapping("/productos")
@Tag(name = "Productos", description = "API para gestión de productos")
@SecurityRequirement(name = "bearer-jwt")
public class ProductoController {

    private static final Logger logger = LoggerFactory.getLogger(ProductoController.class);

    private final ProductoService productoService;
    private final ProductoMapper productoMapper;

    public ProductoController(ProductoService productoService, ProductoMapper productoMapper) {
        this.productoService = productoService;
        this.productoMapper = productoMapper;
    }

    @Operation(summary = "Crear un nuevo producto", 
               description = "Crea un nuevo producto para una empresa específica")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Producto creado exitosamente",
                    content = @Content(schema = @Schema(implementation = ProductoDto.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta empresa"),
        @ApiResponse(responseCode = "404", description = "Empresa no encontrada")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR')")
    public ResponseEntity<ProductoDto> crearProducto(
            @Valid @RequestBody ProductoDto productoDto) {
        
        logger.info("POST /productos - Crear producto para empresa ID: {}", productoDto.getEmpresaId());
        
        Producto producto = productoMapper.toEntity(productoDto);
        Producto productoCreado = productoService.crearProducto(productoDto.getEmpresaId(), producto);
        ProductoDto responseDto = productoMapper.toDto(productoCreado);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @Operation(summary = "Actualizar un producto existente",
               description = "Actualiza los datos de un producto")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Producto actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = ProductoDto.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta empresa"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR')")
    public ResponseEntity<ProductoDto> actualizarProducto(
            @PathVariable Long id,
            @Valid @RequestBody ProductoDto productoDto) {
        
        logger.info("PUT /productos/{} - Actualizar producto", id);
        
        Producto producto = productoMapper.toEntity(productoDto);
        Producto productoActualizado = productoService.actualizarProducto(
            productoDto.getEmpresaId(), id, producto);
        ProductoDto responseDto = productoMapper.toDto(productoActualizado);
        
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "Obtener un producto por ID",
               description = "Obtiene los detalles de un producto específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Producto encontrado",
                    content = @Content(schema = @Schema(implementation = ProductoDto.class))),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta empresa"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    public ResponseEntity<ProductoDto> obtenerProducto(
            @PathVariable Long id,
            @RequestParam Long empresaId) {
        
        logger.debug("GET /productos/{} - Obtener producto", id);
        
        Producto producto = productoService.obtenerProductoPorId(empresaId, id);
        ProductoDto responseDto = productoMapper.toDto(producto);
        
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "Listar productos de una empresa",
               description = "Lista productos de una empresa con paginación, ordenamiento y filtros opcionales")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de productos obtenida exitosamente"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta empresa")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    public ResponseEntity<Page<ProductoDto>> listarProductos(
            @Parameter(description = "ID de la empresa") 
            @RequestParam Long empresaId,
            @Parameter(description = "Filtro por estado activo/inactivo (null = todos)")
            @RequestParam(required = false) Boolean activo,
            @Parameter(description = "Búsqueda por código o descripción")
            @RequestParam(required = false) String busqueda,
            @Parameter(description = "Filtro por tipo de transacción")
            @RequestParam(required = false) String tipoTransaccion,
            @Parameter(description = "Filtro por tasa de IVA (0, 5, 10)")
            @RequestParam(required = false) Integer iva,
            @Parameter(description = "Número de página (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campo para ordenar")
            @RequestParam(defaultValue = "descripcion") String sortBy,
            @Parameter(description = "Dirección de ordenamiento (asc/desc)")
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        logger.debug("GET /productos - Listar productos de empresa ID: {} con filtros - activo: {}, busqueda: {}, tipoTransaccion: {}, iva: {}", 
                     empresaId, activo, busqueda, tipoTransaccion, iva);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Producto> productosPage = productoService.listarProductosConFiltros(
            empresaId, activo, busqueda, tipoTransaccion, iva, pageable);
        Page<ProductoDto> productosDto = productosPage.map(productoMapper::toDto);
        
        return ResponseEntity.ok(productosDto);
    }

    @Operation(summary = "Buscar productos",
               description = "Busca productos por código o descripción con paginación")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Búsqueda completada exitosamente"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta empresa")
    })
    @GetMapping("/buscar")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    public ResponseEntity<Page<ProductoDto>> buscarProductos(
            @Parameter(description = "ID de la empresa")
            @RequestParam Long empresaId,
            @Parameter(description = "Término de búsqueda (código o descripción)")
            @RequestParam(required = false) String busqueda,
            @Parameter(description = "Número de página (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campo para ordenar")
            @RequestParam(defaultValue = "descripcion") String sortBy,
            @Parameter(description = "Dirección de ordenamiento (asc/desc)")
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        logger.debug("GET /productos/buscar - Buscar productos en empresa ID: {} con término: {}", 
                    empresaId, busqueda);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Producto> productosPage = productoService.buscarProductos(empresaId, busqueda, pageable);
        Page<ProductoDto> productosDto = productosPage.map(productoMapper::toDto);
        
        return ResponseEntity.ok(productosDto);
    }

    @Operation(summary = "Desactivar un producto",
               description = "Desactiva un producto (soft delete)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Producto desactivado exitosamente"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta empresa"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR')")
    public ResponseEntity<Void> desactivarProducto(
            @PathVariable Long id,
            @RequestParam Long empresaId) {
        
        logger.info("DELETE /productos/{} - Desactivar producto", id);
        
        productoService.desactivarProducto(empresaId, id);
        
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Importar productos desde Excel",
               description = "Importa productos masivamente desde un archivo Excel. " +
                           "Formato esperado: Código | Descripción | Precio | IVA | Tipo Transacción | Unidad Medida | Balanza. " +
                           "Columnas opcionales: Código, Tipo Transacción (default: VENTA_MERCADERIA), Unidad Medida (default: UNI), Balanza (default: false)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Importación completada",
                    content = @Content(schema = @Schema(implementation = Map.class))),
        @ApiResponse(responseCode = "400", description = "Archivo inválido o errores en los datos"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta empresa")
    })
    @PostMapping(value = "/importar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR')")
    public ResponseEntity<Map<String, Object>> importarProductos(
            @Parameter(description = "ID de la empresa")
            @RequestParam Long empresaId,
            @Parameter(description = "Archivo Excel (.xlsx)")
            @RequestParam("archivo") MultipartFile archivo) {
        
        logger.info("POST /productos/importar - Importar productos para empresa ID: {}", empresaId);
        
        // Validar archivo
        if (archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }
        
        String nombreArchivo = archivo.getOriginalFilename();
        if (nombreArchivo == null || !nombreArchivo.toLowerCase().endsWith(".xlsx")) {
            throw new IllegalArgumentException("El archivo debe ser formato Excel (.xlsx)");
        }
        
        try {
            List<Producto> productosImportados = productoService.importarProductosDesdeExcel(empresaId, archivo);
            
            List<ProductoDto> productosDto = productosImportados.stream()
                .map(productoMapper::toDto)
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Importación completada exitosamente");
            response.put("cantidadImportada", productosDto.size());
            response.put("productos", productosDto);
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            logger.error("Error al procesar archivo Excel", e);
            throw new RuntimeException("Error al procesar el archivo Excel: " + e.getMessage());
        }
    }

    @Operation(summary = "Verificar si existe un código duplicado",
               description = "Verifica si ya existe un producto con el código dado en la empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Verificación completada"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta empresa")
    })
    @GetMapping("/verificar-codigo")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR')")
    public ResponseEntity<Map<String, Boolean>> verificarCodigo(
            @Parameter(description = "ID de la empresa")
            @RequestParam Long empresaId,
            @Parameter(description = "Código a verificar")
            @RequestParam String codigo,
            @Parameter(description = "ID del producto a excluir (opcional, para edición)")
            @RequestParam(required = false) Long productoId) {
        
        logger.debug("GET /productos/verificar-codigo - Verificar código {} en empresa {}", codigo, empresaId);
        
        boolean existe = productoService.existeCodigo(empresaId, codigo, productoId);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("existe", existe);
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Verificar si existe una descripción duplicada",
               description = "Verifica si ya existe un producto con la descripción dada en la empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Verificación completada"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta empresa")
    })
    @GetMapping("/verificar-descripcion")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR')")
    public ResponseEntity<Map<String, Boolean>> verificarDescripcion(
            @Parameter(description = "ID de la empresa")
            @RequestParam Long empresaId,
            @Parameter(description = "Descripción a verificar")
            @RequestParam String descripcion,
            @Parameter(description = "ID del producto a excluir (opcional, para edición)")
            @RequestParam(required = false) Long productoId) {
        
        logger.debug("GET /productos/verificar-descripcion - Verificar descripción en empresa {}", empresaId);
        
        boolean existe = productoService.existeDescripcion(empresaId, descripcion, productoId);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("existe", existe);
        
        return ResponseEntity.ok(response);
    }
}
