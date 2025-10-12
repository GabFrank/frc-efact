package com.frcefact.controller;

import com.frcefact.dto.ClienteRankingDto;
import com.frcefact.dto.FacturaFiltroDto;
import com.frcefact.dto.FacturaReporteDto;
import com.frcefact.dto.ProductoReporteDto;
import com.frcefact.service.ReporteExportService;
import com.frcefact.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para reportes de facturación.
 * 
 * Requirements: 16.1, 16.2, 16.3, 16.4, 16.5, 16.6
 */
@RestController
@RequestMapping("/api/reportes")
@Tag(name = "Reportes", description = "Endpoints para generación y exportación de reportes")
public class ReporteController {

    private final ReporteService reporteService;
    private final ReporteExportService reporteExportService;

    public ReporteController(ReporteService reporteService, ReporteExportService reporteExportService) {
        this.reporteService = reporteService;
        this.reporteExportService = reporteExportService;
    }

    /**
     * Obtiene reporte de facturas con filtros.
     * 
     * Requirement 16.1: Reporte de facturas con filtros
     * Requirement 16.2: Filtros por fecha, cliente, estado y monto
     * 
     * @param filtro Filtros de búsqueda
     * @return Page de FacturaReporteDto
     */
    @GetMapping("/facturas")
    @Operation(summary = "Obtener reporte de facturas",
               description = "Retorna facturas con filtros dinámicos y paginación")
    public ResponseEntity<Page<FacturaReporteDto>> getReporteFacturas(@Valid FacturaFiltroDto filtro) {
        Page<FacturaReporteDto> reporte = reporteService.reporteFacturas(filtro);
        return ResponseEntity.ok(reporte);
    }

    /**
     * Obtiene reporte agrupado por cliente.
     * 
     * Requirement 16.3: Reporte por cliente con agrupación
     * 
     * @param empresaId ID de la empresa
     * @param fechaDesde Fecha inicial
     * @param fechaHasta Fecha final
     * @return Lista de ClienteRankingDto
     */
    @GetMapping("/clientes")
    @Operation(summary = "Obtener reporte por cliente",
               description = "Retorna facturas agrupadas por cliente con totales")
    public ResponseEntity<List<ClienteRankingDto>> getReporteClientes(
            @Parameter(description = "ID de la empresa")
            @RequestParam Long empresaId,
            
            @Parameter(description = "Fecha inicial (formato: yyyy-MM-dd'T'HH:mm:ss)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            
            @Parameter(description = "Fecha final (formato: yyyy-MM-dd'T'HH:mm:ss)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta) {
        
        List<ClienteRankingDto> reporte = reporteService.reportePorCliente(empresaId, fechaDesde, fechaHasta);
        return ResponseEntity.ok(reporte);
    }

    /**
     * Obtiene reporte agrupado por producto.
     * 
     * Requirement 16.4: Reporte por producto con cantidad y monto
     * 
     * @param empresaId ID de la empresa
     * @param fechaDesde Fecha inicial
     * @param fechaHasta Fecha final
     * @return Lista de ProductoReporteDto
     */
    @GetMapping("/productos")
    @Operation(summary = "Obtener reporte por producto",
               description = "Retorna productos con cantidad vendida y monto total")
    public ResponseEntity<List<ProductoReporteDto>> getReporteProductos(
            @Parameter(description = "ID de la empresa")
            @RequestParam Long empresaId,
            
            @Parameter(description = "Fecha inicial (formato: yyyy-MM-dd'T'HH:mm:ss)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            
            @Parameter(description = "Fecha final (formato: yyyy-MM-dd'T'HH:mm:ss)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta) {
        
        List<ProductoReporteDto> reporte = reporteService.reportePorProducto(empresaId, fechaDesde, fechaHasta);
        return ResponseEntity.ok(reporte);
    }

    /**
     * Obtiene reporte agrupado por usuario.
     * 
     * Requirement 16.5: Reporte por usuario listando facturas por creador
     * 
     * @param empresaId ID de la empresa
     * @param fechaDesde Fecha inicial
     * @param fechaHasta Fecha final
     * @return Mapa de usuario -> lista de facturas
     */
    @GetMapping("/usuarios")
    @Operation(summary = "Obtener reporte por usuario",
               description = "Retorna facturas agrupadas por usuario creador")
    public ResponseEntity<Map<String, List<FacturaReporteDto>>> getReporteUsuarios(
            @Parameter(description = "ID de la empresa")
            @RequestParam Long empresaId,
            
            @Parameter(description = "Fecha inicial (formato: yyyy-MM-dd'T'HH:mm:ss)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            
            @Parameter(description = "Fecha final (formato: yyyy-MM-dd'T'HH:mm:ss)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta) {
        
        Map<String, List<FacturaReporteDto>> reporte = reporteService.reportePorUsuario(empresaId, fechaDesde, fechaHasta);
        return ResponseEntity.ok(reporte);
    }

    // ==================== EXPORTACIÓN A EXCEL ====================

    /**
     * Exporta reporte de facturas a Excel.
     * 
     * Requirement 16.6: Exportación de reportes
     * 
     * @param filtro Filtros de búsqueda
     * @return Archivo Excel
     */
    @GetMapping("/facturas/excel")
    @Operation(summary = "Exportar reporte de facturas a Excel",
               description = "Descarga reporte de facturas en formato Excel")
    public ResponseEntity<byte[]> exportarFacturasExcel(@Valid FacturaFiltroDto filtro) {
        try {
            Page<FacturaReporteDto> facturas = reporteService.reporteFacturas(filtro);
            ByteArrayOutputStream outputStream = reporteExportService.exportarFacturasExcel(
                    facturas.getContent(), 
                    "Facturas"
            );
            
            return crearRespuestaExcel(outputStream, "reporte_facturas");
        } catch (UnsupportedOperationException e) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                    .body(e.getMessage().getBytes());
        }
    }

    /**
     * Exporta reporte de clientes a Excel.
     * 
     * @param empresaId ID de la empresa
     * @param fechaDesde Fecha inicial
     * @param fechaHasta Fecha final
     * @return Archivo Excel
     */
    @GetMapping("/clientes/excel")
    @Operation(summary = "Exportar reporte de clientes a Excel",
               description = "Descarga reporte de clientes en formato Excel")
    public ResponseEntity<byte[]> exportarClientesExcel(
            @RequestParam Long empresaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta) {
        
        try {
            List<ClienteRankingDto> clientes = reporteService.reportePorCliente(empresaId, fechaDesde, fechaHasta);
            ByteArrayOutputStream outputStream = reporteExportService.exportarClientesExcel(clientes, "Clientes");
            
            return crearRespuestaExcel(outputStream, "reporte_clientes");
        } catch (UnsupportedOperationException e) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                    .body(e.getMessage().getBytes());
        }
    }

    /**
     * Exporta reporte de productos a Excel.
     * 
     * @param empresaId ID de la empresa
     * @param fechaDesde Fecha inicial
     * @param fechaHasta Fecha final
     * @return Archivo Excel
     */
    @GetMapping("/productos/excel")
    @Operation(summary = "Exportar reporte de productos a Excel",
               description = "Descarga reporte de productos en formato Excel")
    public ResponseEntity<byte[]> exportarProductosExcel(
            @RequestParam Long empresaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta) {
        
        try {
            List<ProductoReporteDto> productos = reporteService.reportePorProducto(empresaId, fechaDesde, fechaHasta);
            ByteArrayOutputStream outputStream = reporteExportService.exportarProductosExcel(productos, "Productos");
            
            return crearRespuestaExcel(outputStream, "reporte_productos");
        } catch (UnsupportedOperationException e) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                    .body(e.getMessage().getBytes());
        }
    }

    /**
     * Exporta reporte de usuarios a Excel.
     * 
     * @param empresaId ID de la empresa
     * @param fechaDesde Fecha inicial
     * @param fechaHasta Fecha final
     * @return Archivo Excel
     */
    @GetMapping("/usuarios/excel")
    @Operation(summary = "Exportar reporte de usuarios a Excel",
               description = "Descarga reporte de usuarios en formato Excel")
    public ResponseEntity<byte[]> exportarUsuariosExcel(
            @RequestParam Long empresaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta) {
        
        try {
            Map<String, List<FacturaReporteDto>> reportePorUsuario = reporteService.reportePorUsuario(
                    empresaId, fechaDesde, fechaHasta
            );
            ByteArrayOutputStream outputStream = reporteExportService.exportarUsuariosExcel(
                    reportePorUsuario, 
                    "Usuarios"
            );
            
            return crearRespuestaExcel(outputStream, "reporte_usuarios");
        } catch (UnsupportedOperationException e) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                    .body(e.getMessage().getBytes());
        }
    }

    // ==================== EXPORTACIÓN A PDF ====================

    /**
     * Exporta reporte de facturas a PDF.
     * 
     * @param filtro Filtros de búsqueda
     * @return Archivo PDF
     */
    @GetMapping("/facturas/pdf")
    @Operation(summary = "Exportar reporte de facturas a PDF",
               description = "Descarga reporte de facturas en formato PDF")
    public ResponseEntity<byte[]> exportarFacturasPdf(@Valid FacturaFiltroDto filtro) {
        try {
            Page<FacturaReporteDto> facturas = reporteService.reporteFacturas(filtro);
            ByteArrayOutputStream outputStream = reporteExportService.exportarFacturasPdf(
                    facturas.getContent(), 
                    "Reporte de Facturas"
            );
            
            return crearRespuestaPdf(outputStream, "reporte_facturas");
        } catch (UnsupportedOperationException e) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                    .body(e.getMessage().getBytes());
        }
    }

    /**
     * Exporta reporte de clientes a PDF.
     * 
     * @param empresaId ID de la empresa
     * @param fechaDesde Fecha inicial
     * @param fechaHasta Fecha final
     * @return Archivo PDF
     */
    @GetMapping("/clientes/pdf")
    @Operation(summary = "Exportar reporte de clientes a PDF",
               description = "Descarga reporte de clientes en formato PDF")
    public ResponseEntity<byte[]> exportarClientesPdf(
            @RequestParam Long empresaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta) {
        
        try {
            List<ClienteRankingDto> clientes = reporteService.reportePorCliente(empresaId, fechaDesde, fechaHasta);
            ByteArrayOutputStream outputStream = reporteExportService.exportarClientesPdf(
                    clientes, 
                    "Reporte de Clientes"
            );
            
            return crearRespuestaPdf(outputStream, "reporte_clientes");
        } catch (UnsupportedOperationException e) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                    .body(e.getMessage().getBytes());
        }
    }

    /**
     * Exporta reporte de productos a PDF.
     * 
     * @param empresaId ID de la empresa
     * @param fechaDesde Fecha inicial
     * @param fechaHasta Fecha final
     * @return Archivo PDF
     */
    @GetMapping("/productos/pdf")
    @Operation(summary = "Exportar reporte de productos a PDF",
               description = "Descarga reporte de productos en formato PDF")
    public ResponseEntity<byte[]> exportarProductosPdf(
            @RequestParam Long empresaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta) {
        
        try {
            List<ProductoReporteDto> productos = reporteService.reportePorProducto(empresaId, fechaDesde, fechaHasta);
            ByteArrayOutputStream outputStream = reporteExportService.exportarProductosPdf(
                    productos, 
                    "Reporte de Productos"
            );
            
            return crearRespuestaPdf(outputStream, "reporte_productos");
        } catch (UnsupportedOperationException e) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                    .body(e.getMessage().getBytes());
        }
    }

    /**
     * Exporta reporte de usuarios a PDF.
     * 
     * @param empresaId ID de la empresa
     * @param fechaDesde Fecha inicial
     * @param fechaHasta Fecha final
     * @return Archivo PDF
     */
    @GetMapping("/usuarios/pdf")
    @Operation(summary = "Exportar reporte de usuarios a PDF",
               description = "Descarga reporte de usuarios en formato PDF")
    public ResponseEntity<byte[]> exportarUsuariosPdf(
            @RequestParam Long empresaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta) {
        
        try {
            Map<String, List<FacturaReporteDto>> reportePorUsuario = reporteService.reportePorUsuario(
                    empresaId, fechaDesde, fechaHasta
            );
            ByteArrayOutputStream outputStream = reporteExportService.exportarUsuariosPdf(
                    reportePorUsuario, 
                    "Reporte de Usuarios"
            );
            
            return crearRespuestaPdf(outputStream, "reporte_usuarios");
        } catch (UnsupportedOperationException e) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                    .body(e.getMessage().getBytes());
        }
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Crea respuesta HTTP para archivo Excel.
     */
    private ResponseEntity<byte[]> crearRespuestaExcel(ByteArrayOutputStream outputStream, String nombreBase) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = nombreBase + "_" + timestamp + ".xlsx";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        ));
        headers.setContentDispositionFormData("attachment", filename);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        
        return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);
    }

    /**
     * Crea respuesta HTTP para archivo PDF.
     */
    private ResponseEntity<byte[]> crearRespuestaPdf(ByteArrayOutputStream outputStream, String nombreBase) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = nombreBase + "_" + timestamp + ".pdf";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        
        return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);
    }
}
