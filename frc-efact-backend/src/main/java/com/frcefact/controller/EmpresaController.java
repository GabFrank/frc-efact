package com.frcefact.controller;

import com.frcefact.dto.AsignarUsuarioEmpresaRequest;
import com.frcefact.dto.EmpresaDto;
import com.frcefact.dto.UsuarioEmpresaDto;
import com.frcefact.dto.mapper.EmpresaMapper;
import com.frcefact.dto.mapper.UsuarioEmpresaMapper;
import com.frcefact.model.Empresa;
import com.frcefact.model.UsuarioEmpresa;
import com.frcefact.service.EmpresaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de empresas.
 * Proporciona endpoints CRUD y asignación de usuarios.
 */
@RestController
@RequestMapping("/empresas")
@Tag(name = "Empresas", description = "API para gestión de empresas")
@SecurityRequirement(name = "bearer-jwt")
public class EmpresaController {

    private static final Logger logger = LoggerFactory.getLogger(EmpresaController.class);

    private final EmpresaService empresaService;
    private final EmpresaMapper empresaMapper;
    private final UsuarioEmpresaMapper usuarioEmpresaMapper;

    public EmpresaController(EmpresaService empresaService,
            EmpresaMapper empresaMapper,
            UsuarioEmpresaMapper usuarioEmpresaMapper) {
        this.empresaService = empresaService;
        this.empresaMapper = empresaMapper;
        this.usuarioEmpresaMapper = usuarioEmpresaMapper;
    }

    /**
     * Crea una nueva empresa.
     *
     * @param empresaDto datos de la empresa
     * @return la empresa creada
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN')")
    @Operation(summary = "Crear empresa", description = "Crea una nueva empresa con validación de RUC")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Empresa creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o RUC duplicado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    public ResponseEntity<EmpresaDto> crearEmpresa(@Valid @RequestBody EmpresaDto empresaDto) {
        logger.info("POST /api/empresas - Creando nueva empresa con RUC: {}", empresaDto.getRuc());
        logger.info("Datos recibidos: ciudadId={}, barrioId={}, domicilioFiscalDireccion={}",
                empresaDto.getCiudadId(), empresaDto.getBarrioId(), empresaDto.getDomicilioFiscalDireccion());

        try {
            Empresa empresa = empresaMapper.toEntity(empresaDto);
            Empresa empresaCreada = empresaService.crearEmpresa(empresa);
            EmpresaDto resultado = empresaMapper.toDto(empresaCreada);

            return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
        } catch (IllegalArgumentException e) {
            logger.error("Error de validación al crear empresa: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado al crear empresa", e);
            throw e;
        }
    }

    /**
     * Actualiza una empresa existente.
     *
     * @param id         ID de la empresa
     * @param empresaDto datos actualizados
     * @return la empresa actualizada
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar empresa", description = "Actualiza los datos de una empresa existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Empresa actualizada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos para esta empresa"),
            @ApiResponse(responseCode = "404", description = "Empresa no encontrada")
    })
    public ResponseEntity<EmpresaDto> actualizarEmpresa(
            @Parameter(description = "ID de la empresa") @PathVariable Long id,
            @Valid @RequestBody EmpresaDto empresaDto) {
        logger.info("PUT /api/empresas/{} - Actualizando empresa", id);

        Empresa empresaActualizada = empresaMapper.toEntity(empresaDto);
        Empresa resultado = empresaService.actualizarEmpresa(id, empresaActualizada);
        EmpresaDto resultadoDto = empresaMapper.toDto(resultado);

        return ResponseEntity.ok(resultadoDto);
    }

    /**
     * Obtiene una empresa por ID.
     *
     * @param id ID de la empresa
     * @return la empresa encontrada
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener empresa por ID", description = "Obtiene los detalles de una empresa específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Empresa encontrada"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos para esta empresa"),
            @ApiResponse(responseCode = "404", description = "Empresa no encontrada")
    })
    public ResponseEntity<EmpresaDto> obtenerEmpresaPorId(
            @Parameter(description = "ID de la empresa") @PathVariable Long id) {
        logger.debug("GET /api/empresas/{} - Obteniendo empresa", id);

        Empresa empresa = empresaService.obtenerEmpresaPorId(id);
        EmpresaDto empresaDto = empresaMapper.toDto(empresa);

        return ResponseEntity.ok(empresaDto);
    }

    /**
     * Obtiene todas las empresas activas.
     *
     * @return lista de empresas activas
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    @Operation(summary = "Listar todas las empresas", description = "Obtiene lista de todas las empresas activas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    public ResponseEntity<List<EmpresaDto>> obtenerTodasLasEmpresas() {
        logger.debug("GET /api/empresas - Obteniendo empresas según permisos del usuario");

        try {
            List<Empresa> empresas = empresaService.obtenerEmpresasSegunPermisos();
            logger.debug("Se encontraron {} empresas para el usuario", empresas.size());

            List<EmpresaDto> empresasDto = empresaMapper.toDtoList(empresas);
            logger.debug("Se mapearon {} empresas a DTO", empresasDto.size());

            return ResponseEntity.ok(empresasDto);
        } catch (Exception e) {
            logger.error("Error al obtener empresas", e);
            throw e;
        }
    }

    /**
     * Obtiene las empresas del usuario autenticado.
     *
     * @return lista de empresas del usuario
     */
    @GetMapping("/mis-empresas")
    @Operation(summary = "Obtener mis empresas", description = "Obtiene las empresas a las que el usuario tiene acceso")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    public ResponseEntity<List<EmpresaDto>> obtenerMisEmpresas() {
        logger.debug("GET /api/empresas/mis-empresas - Obteniendo empresas del usuario");

        List<Empresa> empresas = empresaService.obtenerEmpresasDelUsuario();
        List<EmpresaDto> empresasDto = empresaMapper.toDtoList(empresas);

        return ResponseEntity.ok(empresasDto);
    }

    /**
     * Desactiva una empresa.
     *
     * @param id ID de la empresa
     * @return respuesta sin contenido
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Desactivar empresa", description = "Desactiva una empresa (soft delete)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Empresa desactivada exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos para esta empresa"),
            @ApiResponse(responseCode = "404", description = "Empresa no encontrada")
    })
    public ResponseEntity<Void> desactivarEmpresa(
            @Parameter(description = "ID de la empresa") @PathVariable Long id) {
        logger.info("DELETE /api/empresas/{} - Desactivando empresa", id);

        empresaService.desactivarEmpresa(id);

        return ResponseEntity.noContent().build();
    }

    /**
     * Obtiene los usuarios asignados a una empresa.
     *
     * @param id ID de la empresa
     * @return lista de usuarios asignados a la empresa
     */
    @GetMapping("/{id}/usuarios")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN')")
    @Operation(summary = "Listar usuarios de empresa", description = "Obtiene la lista de usuarios asignados a una empresa")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuarios obtenidos exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos para esta empresa"),
            @ApiResponse(responseCode = "404", description = "Empresa no encontrada")
    })
    public ResponseEntity<List<UsuarioEmpresaDto>> obtenerUsuariosDeEmpresa(
            @Parameter(description = "ID de la empresa") @PathVariable Long id) {
        logger.debug("GET /api/empresas/{}/usuarios - Obteniendo usuarios de empresa", id);

        List<UsuarioEmpresa> usuariosEmpresa = empresaService.obtenerUsuariosDeEmpresa(id);
        List<UsuarioEmpresaDto> usuariosDto = usuarioEmpresaMapper.toDtoList(usuariosEmpresa);

        return ResponseEntity.ok(usuariosDto);
    }

    /**
     * Asigna un usuario a una empresa con un rol específico.
     *
     * @param id      ID de la empresa
     * @param request datos de asignación
     * @return la relación usuario-empresa creada
     */
    @PostMapping("/{id}/usuarios")
    @Operation(summary = "Asignar usuario a empresa", description = "Asigna un usuario a una empresa con rol ADMINISTRADOR o LECTOR")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuario asignado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos para esta empresa"),
            @ApiResponse(responseCode = "404", description = "Empresa o usuario no encontrado")
    })
    public ResponseEntity<UsuarioEmpresaDto> asignarUsuarioAEmpresa(
            @Parameter(description = "ID de la empresa") @PathVariable Long id,
            @Valid @RequestBody AsignarUsuarioEmpresaRequest request) {
        logger.info("POST /api/empresas/{}/usuarios - Asignando usuario {} con rol {}",
                id, request.getUsuarioId(), request.getRolEmpresa());

        UsuarioEmpresa usuarioEmpresa = empresaService.asignarUsuarioAEmpresa(
                id,
                request.getUsuarioId(),
                request.getRolEmpresa());

        UsuarioEmpresaDto resultado = usuarioEmpresaMapper.toDto(usuarioEmpresa);

        return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
    }

    /**
     * Remueve un usuario de una empresa.
     *
     * @param empresaId ID de la empresa
     * @param usuarioId ID del usuario
     * @return respuesta vacía
     */
    @DeleteMapping("/{empresaId}/usuarios/{usuarioId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN')")
    @Operation(summary = "Remover usuario de empresa", description = "Remueve un usuario de una empresa")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuario removido exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos para esta empresa"),
            @ApiResponse(responseCode = "404", description = "Empresa o usuario no encontrado")
    })
    public ResponseEntity<Void> removerUsuarioDeEmpresa(
            @Parameter(description = "ID de la empresa") @PathVariable Long empresaId,
            @Parameter(description = "ID del usuario") @PathVariable Long usuarioId) {
        logger.info("DELETE /api/empresas/{}/usuarios/{} - Removiendo usuario de empresa", empresaId, usuarioId);

        empresaService.removerUsuarioDeEmpresa(empresaId, usuarioId);

        return ResponseEntity.noContent().build();
    }

    /**
     * Busca empresas por razón social.
     *
     * @param razonSocial texto a buscar
     * @return lista de empresas que coinciden
     */
    @GetMapping("/buscar")
    @Operation(summary = "Buscar empresas", description = "Busca empresas por razón social (búsqueda parcial)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Búsqueda completada"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    public ResponseEntity<List<EmpresaDto>> buscarEmpresas(
            @Parameter(description = "Texto a buscar en razón social") @RequestParam String razonSocial) {
        logger.debug("GET /api/empresas/buscar?razonSocial={}", razonSocial);

        List<Empresa> empresas = empresaService.buscarPorRazonSocial(razonSocial);
        List<EmpresaDto> empresasDto = empresaMapper.toDtoList(empresas);

        return ResponseEntity.ok(empresasDto);
    }

    /**
     * Endpoint de diagnóstico para verificar empresas.
     */
    @GetMapping("/diagnostico")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> diagnostico() {
        try {
            List<Empresa> empresas = empresaService.obtenerTodasLasEmpresas();
            StringBuilder sb = new StringBuilder();
            sb.append("Total empresas: ").append(empresas.size()).append("\n\n");

            for (Empresa empresa : empresas) {
                sb.append("ID: ").append(empresa.getId()).append("\n");
                sb.append("RUC: ").append(empresa.getRuc()).append("\n");
                sb.append("Razón Social: ").append(empresa.getRazonSocial()).append("\n");
                sb.append("Ciudad: ").append(empresa.getCiudad() != null ? empresa.getCiudad().getNombre() : "NULL")
                        .append("\n");
                sb.append("Barrio: ").append(empresa.getBarrio() != null ? empresa.getBarrio().getNombre() : "NULL")
                        .append("\n");
                sb.append("---\n");
            }

            return ResponseEntity.ok(sb.toString());
        } catch (Exception e) {
            logger.error("Error en diagnóstico", e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage() + "\nCause: "
                    + (e.getCause() != null ? e.getCause().getMessage() : "N/A"));
        }
    }
}
