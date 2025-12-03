package com.frcefact.controller;

import com.frcefact.dto.DashboardEmpresaDto;
import com.frcefact.dto.DashboardGeneralDto;
import com.frcefact.dto.DashboardUsuarioDto;
import com.frcefact.service.DashboardService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Controlador REST para endpoints de dashboard y métricas.
 */
@RestController
@RequestMapping("/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Obtiene el dashboard del usuario actual.
     */
    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    public ResponseEntity<DashboardUsuarioDto> getDashboardUsuario(@PathVariable Long usuarioId) {
        DashboardUsuarioDto dashboard = dashboardService.getDashboardUsuario(usuarioId);
        return ResponseEntity.ok(dashboard);
    }

    /**
     * Obtiene el dashboard de una empresa.
     */
    @GetMapping("/empresa/{empresaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN')")
    public ResponseEntity<DashboardEmpresaDto> getDashboardEmpresa(
            @PathVariable Long empresaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta) {
        DashboardEmpresaDto dashboard = dashboardService.getDashboardEmpresa(empresaId, fechaDesde, fechaHasta);
        return ResponseEntity.ok(dashboard);
    }

    /**
     * Obtiene métricas generales del sistema.
     */
    @GetMapping("/general")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    public ResponseEntity<DashboardGeneralDto> getDashboardGeneral() {
        DashboardGeneralDto dashboard = dashboardService.getDashboardGeneral();
        return ResponseEntity.ok(dashboard);
    }
}
