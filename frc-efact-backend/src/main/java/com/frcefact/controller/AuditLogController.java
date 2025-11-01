package com.frcefact.controller;

import com.frcefact.model.AccionEnum;
import com.frcefact.model.AuditLog;
import com.frcefact.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestión de auditoría.
 * Proporciona endpoints para consultar el historial de modificaciones
 * con diferentes filtros y criterios de búsqueda.
 */
@RestController
@RequestMapping("/api/auditoria")
@Tag(name = "Auditoría", description = "Endpoints para consultar historial de modificaciones")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    /**
     * Busca registros de auditoría con filtros opcionales.
     * 
     * @param usuarioId ID del usuario (opcional)
     * @param empresaId ID de la empresa (opcional)
     * @param entidadTipo tipo de entidad (opcional)
     * @param accion acción realizada (opcional)
     * @param fechaDesde fecha inicial (opcional)
     * @param fechaHasta fecha final (opcional)
     * @param pageable configuración de paginación
     * @return página de registros de auditoría
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN')")
    @Operation(summary = "Buscar registros de auditoría", 
               description = "Busca registros con filtros opcionales. Requiere rol ADMIN o EMPRESA_ADMIN.")
    public ResponseEntity<Page<AuditLog>> buscarConFiltros(
            @Parameter(description = "ID del usuario") 
            @RequestParam(required = false) Long usuarioId,
            
            @Parameter(description = "ID de la empresa") 
            @RequestParam(required = false) Long empresaId,
            
            @Parameter(description = "Tipo de entidad (ej: Empresa, Factura)") 
            @RequestParam(required = false) String entidadTipo,
            
            @Parameter(description = "Acción realizada") 
            @RequestParam(required = false) AccionEnum accion,
            
            @Parameter(description = "Fecha inicial (formato: yyyy-MM-dd'T'HH:mm:ss)") 
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            
            @Parameter(description = "Fecha final (formato: yyyy-MM-dd'T'HH:mm:ss)") 
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta,
            
            @PageableDefault(size = 20, sort = "fechaHora", direction = Sort.Direction.DESC) 
            Pageable pageable) {
        
        Page<AuditLog> resultado = auditLogService.buscarConFiltros(
            usuarioId, empresaId, entidadTipo, accion, fechaDesde, fechaHasta, pageable
        );
        
        return ResponseEntity.ok(resultado);
    }

    /**
     * Obtiene el historial completo de una entidad específica.
     * 
     * @param tipo tipo de entidad (ej: "Empresa", "Factura")
     * @param id ID de la entidad
     * @return lista de cambios realizados a la entidad
     */
    @GetMapping("/entidad/{tipo}/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    @Operation(summary = "Obtener historial de una entidad", 
               description = "Retorna todos los cambios realizados a una entidad específica")
    public ResponseEntity<List<AuditLog>> getHistorialEntidad(
            @Parameter(description = "Tipo de entidad", example = "Empresa") 
            @PathVariable String tipo,
            
            @Parameter(description = "ID de la entidad") 
            @PathVariable Long id) {
        
        List<AuditLog> historial = auditLogService.getHistorialEntidad(tipo, id);
        return ResponseEntity.ok(historial);
    }

    /**
     * Obtiene las últimas actividades del sistema.
     * 
     * @param limit cantidad máxima de registros (por defecto 10)
     * @return lista de actividades recientes
     */
    @GetMapping("/ultimas-actividades")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN')")
    @Operation(summary = "Obtener últimas actividades", 
               description = "Retorna las actividades más recientes del sistema")
    public ResponseEntity<List<AuditLog>> getUltimasActividades(
            @Parameter(description = "Cantidad de registros a retornar") 
            @RequestParam(defaultValue = "10") int limit) {
        
        List<AuditLog> actividades = auditLogService.getUltimasActividades(limit);
        return ResponseEntity.ok(actividades);
    }

    /**
     * Obtiene las últimas actividades de un usuario específico.
     * 
     * @param usuarioId ID del usuario
     * @param limit cantidad máxima de registros
     * @return lista de actividades del usuario
     */
    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN')")
    @Operation(summary = "Obtener actividades de un usuario", 
               description = "Retorna las actividades recientes de un usuario específico")
    public ResponseEntity<List<AuditLog>> getActividadesUsuario(
            @Parameter(description = "ID del usuario") 
            @PathVariable Long usuarioId,
            
            @Parameter(description = "Cantidad de registros a retornar") 
            @RequestParam(defaultValue = "10") int limit) {
        
        List<AuditLog> actividades = auditLogService.getUltimasActividadesUsuario(usuarioId, limit);
        return ResponseEntity.ok(actividades);
    }

    /**
     * Obtiene estadísticas de auditoría por tipo de acción.
     * 
     * @param empresaId ID de la empresa (opcional)
     * @param fechaDesde fecha inicial
     * @param fechaHasta fecha final
     * @return mapa con conteo de acciones por tipo
     */
    @GetMapping("/estadisticas")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN')")
    @Operation(summary = "Obtener estadísticas de auditoría", 
               description = "Retorna conteo de operaciones por tipo de acción")
    public ResponseEntity<Map<AccionEnum, Long>> getEstadisticas(
            @Parameter(description = "ID de la empresa (opcional)") 
            @RequestParam(required = false) Long empresaId,
            
            @Parameter(description = "Fecha inicial") 
            @RequestParam 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            
            @Parameter(description = "Fecha final") 
            @RequestParam 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta) {
        
        Map<AccionEnum, Long> estadisticas = auditLogService.getEstadisticasPorAccion(
            empresaId, fechaDesde, fechaHasta
        );
        
        return ResponseEntity.ok(estadisticas);
    }

    /**
     * Obtiene el conteo total de registros de auditoría.
     * 
     * @return cantidad total de registros
     */
    @GetMapping("/count")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Contar registros de auditoría", 
               description = "Retorna el total de registros en el sistema")
    public ResponseEntity<Long> contarTotal() {
        long total = auditLogService.contarTotal();
        return ResponseEntity.ok(total);
    }

    /**
     * Obtiene el conteo de registros por empresa.
     * 
     * @param empresaId ID de la empresa
     * @return cantidad de registros de la empresa
     */
    @GetMapping("/count/empresa/{empresaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN')")
    @Operation(summary = "Contar registros por empresa", 
               description = "Retorna el total de registros de una empresa específica")
    public ResponseEntity<Long> contarPorEmpresa(
            @Parameter(description = "ID de la empresa") 
            @PathVariable Long empresaId) {
        
        long total = auditLogService.contarPorEmpresa(empresaId);
        return ResponseEntity.ok(total);
    }
}
