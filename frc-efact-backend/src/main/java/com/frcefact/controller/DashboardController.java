package com.frcefact.controller;

import com.frcefact.dto.DashboardEmpresaDto;
import com.frcefact.dto.DashboardUsuarioDto;
import com.frcefact.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Controlador REST para dashboards y métricas del sistema.
 * 
 * Requirements: 14.1, 15.1, 15.5
 */
@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Endpoints para dashboards y métricas")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Obtiene el dashboard del usuario autenticado.
     * 
     * Requirement 14.1: Dashboard de usuario con métricas personales
     * 
     * @param authentication Autenticación del usuario
     * @return DashboardUsuarioDto con métricas del usuario
     */
    @GetMapping("/usuario")
    @Operation(summary = "Obtener dashboard de usuario", 
               description = "Retorna métricas personales del usuario autenticado")
    public ResponseEntity<DashboardUsuarioDto> getDashboardUsuario(Authentication authentication) {
        // Obtener ID del usuario autenticado
        Long usuarioId = obtenerUsuarioIdDeAuthentication(authentication);
        
        DashboardUsuarioDto dashboard = dashboardService.getDashboardUsuario(usuarioId);
        return ResponseEntity.ok(dashboard);
    }

    /**
     * Obtiene el dashboard de una empresa específica.
     * 
     * Requirement 15.1: Dashboard de empresa con métricas de facturación
     * Requirement 15.5: Filtros de fecha para dashboard de empresa
     * 
     * @param empresaId ID de la empresa
     * @param fechaDesde Fecha inicial del período (opcional)
     * @param fechaHasta Fecha final del período (opcional)
     * @return DashboardEmpresaDto con métricas de la empresa
     */
    @GetMapping("/empresa/{id}")
    @Operation(summary = "Obtener dashboard de empresa",
               description = "Retorna métricas de facturación de una empresa con filtros de fecha opcionales")
    public ResponseEntity<DashboardEmpresaDto> getDashboardEmpresa(
            @Parameter(description = "ID de la empresa")
            @PathVariable("id") Long empresaId,
            
            @Parameter(description = "Fecha inicial del período (formato: yyyy-MM-dd'T'HH:mm:ss)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fechaDesde,
            
            @Parameter(description = "Fecha final del período (formato: yyyy-MM-dd'T'HH:mm:ss)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fechaHasta) {
        
        DashboardEmpresaDto dashboard = dashboardService.getDashboardEmpresa(empresaId, fechaDesde, fechaHasta);
        return ResponseEntity.ok(dashboard);
    }

    /**
     * Extrae el ID del usuario desde el objeto Authentication.
     * 
     * @param authentication Objeto de autenticación de Spring Security
     * @return ID del usuario
     */
    private Long obtenerUsuarioIdDeAuthentication(Authentication authentication) {
        // Asumiendo que el username es el ID del usuario o que hay un UserDetails personalizado
        // Esta implementación puede variar según la configuración de seguridad
        try {
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException e) {
            // Si el username no es un número, buscar por username
            // Esto requeriría inyectar UsuarioRepository
            throw new RuntimeException("No se pudo obtener el ID del usuario autenticado");
        }
    }
}
