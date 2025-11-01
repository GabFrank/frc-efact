package com.frcefact.controller;

import com.frcefact.dto.RolDto;
import com.frcefact.dto.mapper.RolMapper;
import com.frcefact.model.Rol;
import com.frcefact.service.RolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para gestión de roles.
 * Proporciona endpoints para obtener información de roles del sistema.
 */
@RestController
@RequestMapping("/roles")
@Tag(name = "Roles", description = "Endpoints para gestión de roles")
@SecurityRequirement(name = "bearer-jwt")
public class RolController {

    private static final Logger logger = LoggerFactory.getLogger(RolController.class);

    private final RolService rolService;
    private final RolMapper rolMapper;

    public RolController(RolService rolService, RolMapper rolMapper) {
        this.rolService = rolService;
        this.rolMapper = rolMapper;
    }

    /**
     * Obtener todos los roles disponibles.
     *
     * @return lista de todos los roles
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener todos los roles", description = "Obtiene la lista de todos los roles disponibles en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Roles obtenidos exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos de administrador")
    })
    public ResponseEntity<List<RolDto>> obtenerTodosLosRoles() {
        logger.debug("GET /roles - Obteniendo todos los roles");

        List<Rol> roles = rolService.obtenerTodosLosRoles();
        List<RolDto> rolesDto = roles.stream()
                .map(rolMapper::toDto)
                .toList();

        return ResponseEntity.ok(rolesDto);
    }
}
