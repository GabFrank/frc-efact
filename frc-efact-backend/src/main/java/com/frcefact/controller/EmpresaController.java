package com.frcefact.controller;

import com.frcefact.dto.AsignarUsuarioEmpresaRequest;
import com.frcefact.dto.CertificadoUploadResponse;
import com.frcefact.dto.EmpresaDto;
import com.frcefact.dto.PasswordUpdateRequest;
import com.frcefact.dto.UsuarioEmpresaDto;
import com.frcefact.dto.mapper.EmpresaMapper;
import com.frcefact.dto.mapper.UsuarioEmpresaMapper;
import com.frcefact.model.Empresa;
import com.frcefact.model.UsuarioEmpresa;
import com.frcefact.service.CertificadoService;
import com.frcefact.service.EmpresaService;
import com.frcefact.service.EncryptionService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    private final CertificadoService certificadoService;
    private final EncryptionService encryptionService;

    public EmpresaController(EmpresaService empresaService,
            EmpresaMapper empresaMapper,
            UsuarioEmpresaMapper usuarioEmpresaMapper,
            CertificadoService certificadoService,
            EncryptionService encryptionService) {
        this.empresaService = empresaService;
        this.empresaMapper = empresaMapper;
        this.usuarioEmpresaMapper = usuarioEmpresaMapper;
        this.certificadoService = certificadoService;
        this.encryptionService = encryptionService;
    }

    /**
     * Crea una nueva empresa con certificado opcional (multipart/form-data).
     *
     * @param empresaDto datos de la empresa
     * @param certificadoFile archivo certificado opcional
     * @param certificadoPassword contraseña del certificado (solo si hay archivo)
     * @return la empresa creada
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN')")
    @Operation(summary = "Crear empresa con certificado", description = "Crea una nueva empresa con validación de RUC. Puede incluir certificado opcional.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Empresa creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o RUC duplicado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    public ResponseEntity<EmpresaDto> crearEmpresaConCertificado(
            @Parameter(description = "Datos de la empresa") @Valid @RequestPart("empresa") EmpresaDto empresaDto,
            @Parameter(description = "Archivo certificado PFX (opcional)") @RequestPart(value = "certificadoFile", required = false) MultipartFile certificadoFile,
            @Parameter(description = "Contraseña del certificado (solo si hay archivo)") @RequestPart(value = "certificadoPassword", required = false) String certificadoPassword) {
        
        logger.info("POST /api/empresas (multipart) - Creando nueva empresa con RUC: {}", empresaDto.getRuc());
        logger.info("Datos recibidos: ciudadId={}, barrioId={}, domicilioFiscalDireccion={}",
                empresaDto.getCiudadId(), empresaDto.getBarrioId(), empresaDto.getDomicilioFiscalDireccion());

        try {
            // Crear empresa primero (necesitamos el ID para nombrar el certificado)
            Empresa empresa = empresaMapper.toEntity(empresaDto);
            Empresa empresaCreada = empresaService.crearEmpresa(empresa);
            
            // Si hay certificado, procesarlo ahora que tenemos el ID
            if (certificadoFile != null && !certificadoFile.isEmpty() && certificadoPassword != null) {
                logger.info("📎 Certificado detectado en request de creación para empresa ID: {}", empresaCreada.getId());
                try {
                    CertificadoService.CertificadoGuardado resultado = certificadoService.guardarCertificado(
                            empresaCreada.getId(), certificadoFile, certificadoPassword);
                    
                    // Actualizar la empresa con los datos del certificado
                    String passwordEncriptado = encryptionService.encrypt(certificadoPassword);
                    empresaService.actualizarCertificado(
                            empresaCreada.getId(),
                            resultado.getPath(),
                            passwordEncriptado,
                            resultado.getFechaExpiracion());
                    
                    // Recargar empresa con datos actualizados
                    empresaCreada = empresaService.obtenerEmpresaPorId(empresaCreada.getId());
                    logger.info("✅ Certificado guardado y empresa actualizada");
                } catch (Exception e) {
                    logger.error("❌ Error al procesar certificado después de crear empresa", e);
                    // No lanzar excepción - la empresa ya fue creada, solo loggear el error
                }
            }
            
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
     * Crea una nueva empresa sin certificado (application/json).
     *
     * @param empresaDto datos de la empresa
     * @return la empresa creada
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN')")
    @Operation(summary = "Crear empresa", description = "Crea una nueva empresa con validación de RUC")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Empresa creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o RUC duplicado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    public ResponseEntity<EmpresaDto> crearEmpresa(
            @Parameter(description = "Datos de la empresa") @Valid @RequestBody EmpresaDto empresaDto) {
        
        logger.info("POST /api/empresas (JSON) - Creando nueva empresa con RUC: {}", empresaDto.getRuc());
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
     * Actualiza una empresa existente con certificado opcional (multipart/form-data).
     *
     * @param id ID de la empresa
     * @param empresaDto datos actualizados
     * @param certificadoFile archivo certificado opcional
     * @param certificadoPassword contraseña del certificado (solo si hay archivo)
     * @return la empresa actualizada
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Actualizar empresa con certificado", description = "Actualiza los datos de una empresa existente. Puede incluir certificado opcional.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Empresa actualizada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos para esta empresa"),
            @ApiResponse(responseCode = "404", description = "Empresa no encontrada")
    })
    public ResponseEntity<EmpresaDto> actualizarEmpresaConCertificado(
            @Parameter(description = "ID de la empresa") @PathVariable Long id,
            @Parameter(description = "Datos de la empresa") @Valid @RequestPart("empresa") EmpresaDto empresaDto,
            @Parameter(description = "Archivo certificado PFX (opcional)") @RequestPart(value = "certificadoFile", required = false) MultipartFile certificadoFile,
            @Parameter(description = "Contraseña del certificado (solo si hay archivo)") @RequestPart(value = "certificadoPassword", required = false) String certificadoPassword) {
        logger.info("PUT /api/empresas/{} (multipart) - Actualizando empresa", id);

        try {
            // Actualizar empresa primero
            Empresa empresaActualizada = empresaMapper.toEntity(empresaDto);
            Empresa resultado = empresaService.actualizarEmpresa(id, empresaActualizada);
            
            // Si hay certificado, procesarlo ahora
            if (certificadoFile != null && !certificadoFile.isEmpty() && certificadoPassword != null) {
                logger.info("📎 Certificado detectado en request de actualización para empresa ID: {}", id);
                try {
                    CertificadoService.CertificadoGuardado certResultado = certificadoService.guardarCertificado(
                            id, certificadoFile, certificadoPassword);
                    
                    // Actualizar la empresa con los datos del certificado
                    String passwordEncriptado = encryptionService.encrypt(certificadoPassword);
                    empresaService.actualizarCertificado(
                            id,
                            certResultado.getPath(),
                            passwordEncriptado,
                            certResultado.getFechaExpiracion());
                    
                    // Recargar empresa con datos actualizados
                    resultado = empresaService.obtenerEmpresaPorId(id);
                    logger.info("✅ Certificado guardado y empresa actualizada");
                } catch (Exception e) {
                    logger.error("❌ Error al procesar certificado después de actualizar empresa", e);
                    // No lanzar excepción - la empresa ya fue actualizada, solo loggear el error
                }
            }
            
            EmpresaDto resultadoDto = empresaMapper.toDto(resultado);

            return ResponseEntity.ok(resultadoDto);
        } catch (IllegalArgumentException e) {
            logger.error("Error de validación al actualizar empresa: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado al actualizar empresa", e);
            throw e;
        }
    }

    /**
     * Actualiza una empresa existente sin certificado (application/json).
     *
     * @param id ID de la empresa
     * @param empresaDto datos actualizados
     * @return la empresa actualizada
     */
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
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
            @Parameter(description = "Datos de la empresa") @Valid @RequestBody EmpresaDto empresaDto) {
        logger.info("PUT /api/empresas/{} (JSON) - Actualizando empresa", id);

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
     * Actualiza solo la contraseña del certificado existente de una empresa.
     *
     * @param empresaId ID de la empresa
     * @param request   contiene la nueva contraseña
     * @return respuesta sin contenido
     */
    @PutMapping("/{id}/certificado/password")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN')")
    @Operation(summary = "Actualizar contraseña del certificado", description = "Actualiza solo la contraseña del certificado existente sin cambiar el archivo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contraseña actualizada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Contraseña inválida o empresa sin certificado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos para esta empresa"),
            @ApiResponse(responseCode = "404", description = "Empresa no encontrada")
    })
    public ResponseEntity<Void> actualizarPasswordCertificado(
            @Parameter(description = "ID de la empresa") @PathVariable("id") Long empresaId,
            @Parameter(description = "Nueva contraseña del certificado") @RequestBody PasswordUpdateRequest request) {
        
        logger.info("PUT /api/empresas/{}/certificado/password - Actualizando contraseña del certificado", empresaId);

        // Verificar que la empresa existe
        Empresa empresa = empresaService.obtenerEmpresaPorId(empresaId);

        // Validar que la empresa tiene certificado configurado
        if (empresa.getCertificadoPath() == null || empresa.getCertificadoPath().isBlank()) {
            throw new IllegalArgumentException("La empresa no tiene certificado digital configurado");
        }

        // Validar password
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña del certificado es requerida");
        }

        try {
            // Validar que la nueva contraseña funciona con el certificado existente
            certificadoService.validarCertificado(
                    certificadoService.obtenerPathAbsoluto(empresa.getCertificadoPath()).toString(),
                    request.getPassword());

            // Actualizar solo la contraseña encriptada
            String passwordEncriptado = encryptionService.encrypt(request.getPassword());
            empresaService.actualizarCertificado(
                    empresaId,
                    empresa.getCertificadoPath(), // Mantener el mismo path
                    passwordEncriptado,
                    empresa.getCertificadoFechaExpiracion() // Mantener la misma fecha
            );

            logger.info("✅ Contraseña del certificado actualizada exitosamente para empresa {}", empresaId);
            return ResponseEntity.ok().build();

        } catch (IllegalArgumentException e) {
            logger.error("Error de validación al actualizar contraseña: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado al actualizar contraseña del certificado", e);
            throw e;
        }
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
     * Sube un certificado PFX para una empresa.
     *
     * @param empresaId ID de la empresa
     * @param file Archivo PFX a subir
     * @param password Contraseña del certificado
     * @return Respuesta con el path y fecha de expiración del certificado
     */
    @PostMapping(value = "/{id}/certificado", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN')")
    @Operation(summary = "Subir certificado PFX", description = "Sube un certificado digital PFX para una empresa")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Certificado subido exitosamente",
                    content = @Content(schema = @Schema(implementation = CertificadoUploadResponse.class))),
            @ApiResponse(responseCode = "400", description = "Archivo inválido o certificado no válido"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos para esta empresa"),
            @ApiResponse(responseCode = "404", description = "Empresa no encontrada")
    })
    public ResponseEntity<CertificadoUploadResponse> subirCertificado(
            @Parameter(description = "ID de la empresa") @PathVariable("id") Long empresaId,
            @Parameter(description = "Archivo PFX del certificado") @RequestParam("file") MultipartFile file,
            @Parameter(description = "Contraseña del certificado") @RequestParam("password") String password) {
        
        logger.info("POST /api/empresas/{}/certificado - Subiendo certificado", empresaId);

        // Verificar que la empresa existe
        empresaService.obtenerEmpresaPorId(empresaId);

        // Validar password
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña del certificado es requerida");
        }

        try {
            CertificadoService.CertificadoGuardado resultado = certificadoService.guardarCertificado(
                    empresaId, file, password);

            logger.info("📝 Certificado guardado - Path retornado: {}", resultado.getPath());
            logger.info("📝 Certificado guardado - Fecha expiración: {}", resultado.getFechaExpiracion());

            // Actualizar la empresa con el certificadoPath, password encriptado y fecha de expiración
            String passwordEncriptado = encryptionService.encrypt(password);
            logger.info("💾 Actualizando empresa {} con certificadoPath: {}", empresaId, resultado.getPath());
            empresaService.actualizarCertificado(empresaId, resultado.getPath(), passwordEncriptado, resultado.getFechaExpiracion());

            CertificadoUploadResponse response = new CertificadoUploadResponse(
                    resultado.getPath(),
                    resultado.getFechaExpiracion(),
                    "Certificado guardado exitosamente"
            );

            logger.info("✅ Certificado subido exitosamente para empresa {}", empresaId);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.error("Error de validación al subir certificado: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error inesperado al subir certificado", e);
            throw e;
        }
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
