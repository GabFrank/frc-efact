package com.frcefact.service;

import com.frcefact.annotation.Auditable;
import com.frcefact.exception.DuplicateResourceException;
import com.frcefact.exception.ResourceNotFoundException;
import com.frcefact.model.AccionEnum;
import com.frcefact.model.Empresa;
import com.frcefact.model.Usuario;
import com.frcefact.model.UsuarioEmpresa;
import com.frcefact.repository.EmpresaRepository;
import com.frcefact.repository.UsuarioRepository;
import com.frcefact.repository.UsuarioEmpresaRepository;
import com.frcefact.util.CalcularVerificadorRuc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Servicio para gestión de empresas.
 * Implementa CRUD completo con validación de RUC paraguayo y control de
 * permisos.
 */
@Service
@Transactional
public class EmpresaService {

    private static final Logger logger = LoggerFactory.getLogger(EmpresaService.class);

    // Patrón para validar RUC paraguayo: formato XXXXXXXX-X (8 dígitos, guión, 1
    // dígito verificador)
    private static final Pattern RUC_PATTERN = Pattern.compile("^\\d{8}-\\d$");

    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioEmpresaRepository usuarioEmpresaRepository;
    private final EmpresaSecurityService empresaSecurityService;

    public EmpresaService(EmpresaRepository empresaRepository,
            UsuarioRepository usuarioRepository,
            UsuarioEmpresaRepository usuarioEmpresaRepository,
            EmpresaSecurityService empresaSecurityService) {
        this.empresaRepository = empresaRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioEmpresaRepository = usuarioEmpresaRepository;
        this.empresaSecurityService = empresaSecurityService;
    }

    /**
     * Crea una nueva empresa con validación de RUC.
     *
     * @param empresa la empresa a crear
     * @return la empresa creada
     * @throws IllegalArgumentException si el RUC es inválido o ya existe
     */
    @Auditable(entidad = "Empresa", accion = AccionEnum.CREATE)
    public Empresa crearEmpresa(Empresa empresa) {
        logger.info("Creando nueva empresa con RUC: {}", empresa.getRuc());

        // Validar RUC
        validarRuc(empresa.getRuc());

        // Verificar que no exista otra empresa con el mismo RUC
        if (empresaRepository.existsByRuc(empresa.getRuc())) {
            throw new DuplicateResourceException("Empresa", "RUC", empresa.getRuc());
        }

        empresa.setActivo(true);
        Empresa empresaGuardada = empresaRepository.save(empresa);

        // Auto-asignar al usuario creador como ADMINISTRADOR de la empresa
        autoAsignarCreadorAEmpresa(empresaGuardada);

        logger.info("Empresa creada exitosamente con ID: {}", empresaGuardada.getId());
        return empresaGuardada;
    }

    /**
     * Auto-asigna al usuario creador como administrador de la empresa.
     *
     * @param empresa la empresa creada
     */
    private void autoAsignarCreadorAEmpresa(Empresa empresa) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
                
                if (usuarioOpt.isPresent()) {
                    Usuario usuario = usuarioOpt.get();
                    
                    // Crear relación usuario-empresa
                    UsuarioEmpresa usuarioEmpresa = new UsuarioEmpresa();
                    usuarioEmpresa.setUsuario(usuario);
                    usuarioEmpresa.setEmpresa(empresa);
                    usuarioEmpresa.setRolEmpresa("ADMINISTRADOR");
                    usuarioEmpresa.setActivo(true);
                    
                    usuarioEmpresaRepository.save(usuarioEmpresa);
                    logger.info("Usuario {} auto-asignado como ADMINISTRADOR de empresa {}", username, empresa.getId());
                }
            }
        } catch (Exception e) {
            logger.warn("No se pudo auto-asignar el creador a la empresa: {}", e.getMessage());
            // No lanzar excepción para no interrumpir la creación de la empresa
        }
    }

    /**
     * Actualiza una empresa existente.
     *
     * @param id                 ID de la empresa
     * @param empresaActualizada datos actualizados
     * @return la empresa actualizada
     * @throws EntityNotFoundException si la empresa no existe
     * @throws AccessDeniedException   si el usuario no tiene permisos
     */
    @Auditable(entidad = "Empresa", accion = AccionEnum.UPDATE)
    public Empresa actualizarEmpresa(Long id, Empresa empresaActualizada) {
        logger.info("Actualizando empresa con ID: {}", id);

        // Verificar permisos
        if (!empresaSecurityService.hasAccess(id, "WRITE")) {
            throw new AccessDeniedException("No tiene permisos para modificar esta empresa");
        }

        Empresa empresaExistente = empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", "ID", id));

        // Validar RUC si cambió
        if (!empresaExistente.getRuc().equals(empresaActualizada.getRuc())) {
            validarRuc(empresaActualizada.getRuc());
            if (empresaRepository.existsByRuc(empresaActualizada.getRuc())) {
                throw new DuplicateResourceException("Empresa", "RUC", empresaActualizada.getRuc());
            }
        }

        // Actualizar campos
        empresaExistente.setRazonSocial(empresaActualizada.getRazonSocial());
        empresaExistente.setRuc(empresaActualizada.getRuc());
        empresaExistente.setTipoContribuyente(empresaActualizada.getTipoContribuyente());
        empresaExistente.setNombreFantasia(empresaActualizada.getNombreFantasia());
        empresaExistente.setEmail(empresaActualizada.getEmail());
        empresaExistente.setTelefono(empresaActualizada.getTelefono());
        empresaExistente.setDireccion(empresaActualizada.getDireccion());
        empresaExistente.setTipoSociedad(empresaActualizada.getTipoSociedad());

        // Actualizar domicilio fiscal
        empresaExistente.setCiudad(empresaActualizada.getCiudad());
        empresaExistente.setBarrio(empresaActualizada.getBarrio());
        empresaExistente.setDomicilioFiscalDireccion(empresaActualizada.getDomicilioFiscalDireccion());

        // Actualizar actividad económica
        empresaExistente.setCodActividadEconomicaPrincipal(empresaActualizada.getCodActividadEconomicaPrincipal());
        empresaExistente.setDescActividadEconomicaPrincipal(empresaActualizada.getDescActividadEconomicaPrincipal());
        empresaExistente.setListCodigoActividadEconomicaSecundaria(
                empresaActualizada.getListCodigoActividadEconomicaSecundaria());
        empresaExistente.setListDescripcionActividadEconomicaSecundaria(
                empresaActualizada.getListDescripcionActividadEconomicaSecundaria());

        // Actualizar certificado
        if (empresaActualizada.getCertificadoPath() != null) {
            empresaExistente.setCertificadoPath(empresaActualizada.getCertificadoPath());
            empresaExistente.setCertificadoPasswordEncrypted(empresaActualizada.getCertificadoPasswordEncrypted());
            empresaExistente.setCertificadoFechaExpiracion(empresaActualizada.getCertificadoFechaExpiracion());
        }

        Empresa empresaGuardada = empresaRepository.save(empresaExistente);
        logger.info("Empresa actualizada exitosamente con ID: {}", empresaGuardada.getId());

        return empresaGuardada;
    }

    /**
     * Obtiene una empresa por ID.
     *
     * @param id ID de la empresa
     * @return la empresa encontrada
     * @throws EntityNotFoundException si la empresa no existe
     * @throws AccessDeniedException   si el usuario no tiene permisos
     */
    @Transactional(readOnly = true)
    public Empresa obtenerEmpresaPorId(Long id) {
        logger.debug("Obteniendo empresa con ID: {}", id);

        // Verificar permisos
        if (!empresaSecurityService.hasAccess(id, "READ")) {
            throw new AccessDeniedException("No tiene permisos para ver esta empresa");
        }

        return empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", "ID", id));
    }

    /**
     * Obtiene todas las empresas activas (solo para ADMIN del sistema).
     *
     * @return lista de empresas activas
     */
    @Transactional(readOnly = true)
    public List<Empresa> obtenerTodasLasEmpresas() {
        logger.debug("Obteniendo todas las empresas activas");
        return empresaRepository.findByActivo(true);
    }

    /**
     * Obtiene las empresas según los permisos del usuario autenticado.
     * - ADMIN: ve todas las empresas
     * - EMPRESA_ADMIN/FACTURADOR/LECTOR: solo ve las empresas asignadas
     *
     * @return lista de empresas según permisos del usuario
     */
    @Transactional(readOnly = true)
    public List<Empresa> obtenerEmpresasSegunPermisos() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        logger.debug("Obteniendo empresas según permisos para usuario: {}", username);

        // Si es ADMIN del sistema, puede ver todas las empresas
        if (hasRole(authentication, "ADMIN")) {
            logger.debug("Usuario ADMIN - obteniendo todas las empresas");
            return empresaRepository.findByActivo(true);
        }

        // Para otros roles, solo las empresas asignadas
        logger.debug("Usuario no ADMIN - obteniendo empresas asignadas");
        return obtenerEmpresasDelUsuario();
    }

    /**
     * Obtiene las empresas del usuario autenticado.
     *
     * @return lista de empresas del usuario
     */
    @Transactional(readOnly = true)
    public List<Empresa> obtenerEmpresasDelUsuario() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        logger.debug("Obteniendo empresas del usuario: {}", username);

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "username", username));

        return empresaRepository.findByUsuarioId(usuario.getId());
    }

    /**
     * Desactiva una empresa (soft delete).
     *
     * @param id ID de la empresa
     * @throws EntityNotFoundException si la empresa no existe
     * @throws AccessDeniedException   si el usuario no tiene permisos
     */
    public void desactivarEmpresa(Long id) {
        logger.info("Desactivando empresa con ID: {}", id);

        // Verificar permisos
        if (!empresaSecurityService.hasAccess(id, "WRITE")) {
            throw new AccessDeniedException("No tiene permisos para desactivar esta empresa");
        }

        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", "ID", id));

        empresa.setActivo(false);
        empresaRepository.save(empresa);

        logger.info("Empresa desactivada exitosamente con ID: {}", id);
    }

    /**
     * Asigna un usuario a una empresa con un rol específico.
     *
     * @param empresaId  ID de la empresa
     * @param usuarioId  ID del usuario
     * @param rolEmpresa rol del usuario en la empresa (ADMINISTRADOR, FACTURADOR o LECTOR)
     * @return la relación usuario-empresa creada
     * @throws EntityNotFoundException  si la empresa o usuario no existen
     * @throws AccessDeniedException    si el usuario no tiene permisos
     * @throws IllegalArgumentException si el rol es inválido
     */
    public UsuarioEmpresa asignarUsuarioAEmpresa(Long empresaId, Long usuarioId, String rolEmpresa) {
        logger.info("Asignando usuario {} a empresa {} con rol {}", usuarioId, empresaId, rolEmpresa);

        // Verificar permisos
        if (!empresaSecurityService.hasAccess(empresaId, "WRITE")) {
            throw new AccessDeniedException("No tiene permisos para asignar usuarios a esta empresa");
        }

        // Validar rol
        if (!rolEmpresa.equals("ADMINISTRADOR") && !rolEmpresa.equals("FACTURADOR") && !rolEmpresa.equals("LECTOR")) {
            throw new IllegalArgumentException("Rol inválido. Debe ser ADMINISTRADOR, FACTURADOR o LECTOR");
        }

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", "ID", empresaId));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "ID", usuarioId));

        // Verificar si ya existe la relación
        Optional<UsuarioEmpresa> relacionExistente = usuarioEmpresaRepository
                .findByUsuarioAndEmpresa(usuarioId, empresaId);

        if (relacionExistente.isPresent()) {
            // Actualizar rol si ya existe
            UsuarioEmpresa usuarioEmpresa = relacionExistente.get();
            usuarioEmpresa.setRolEmpresa(rolEmpresa);
            usuarioEmpresa.setActivo(true);

            logger.info("Relación usuario-empresa actualizada");
            return usuarioEmpresaRepository.save(usuarioEmpresa);
        }

        // Crear nueva relación
        UsuarioEmpresa usuarioEmpresa = new UsuarioEmpresa();
        usuarioEmpresa.setUsuario(usuario);
        usuarioEmpresa.setEmpresa(empresa);
        usuarioEmpresa.setRolEmpresa(rolEmpresa);
        usuarioEmpresa.setActivo(true);

        UsuarioEmpresa relacionGuardada = usuarioEmpresaRepository.save(usuarioEmpresa);
        logger.info("Usuario asignado exitosamente a empresa");

        return relacionGuardada;
    }

    /**
     * Obtiene los usuarios asignados a una empresa.
     *
     * @param empresaId ID de la empresa
     * @return lista de usuarios asignados a la empresa
     * @throws AccessDeniedException si el usuario no tiene permisos
     * @throws ResourceNotFoundException si la empresa no existe
     */
    @Transactional(readOnly = true)
    public List<UsuarioEmpresa> obtenerUsuariosDeEmpresa(Long empresaId) {
        logger.debug("Obteniendo usuarios de empresa con ID: {}", empresaId);

        // Verificar permisos
        if (!empresaSecurityService.hasAccess(empresaId, "READ")) {
            throw new AccessDeniedException("No tiene permisos para ver usuarios de esta empresa");
        }

        // Verificar que la empresa existe
        if (!empresaRepository.existsById(empresaId)) {
            throw new ResourceNotFoundException("Empresa", "ID", empresaId);
        }

        return usuarioEmpresaRepository.findByEmpresaIdAndActivoTrue(empresaId);
    }

    /**
     * Remueve un usuario de una empresa.
     *
     * @param empresaId ID de la empresa
     * @param usuarioId ID del usuario
     * @throws AccessDeniedException si el usuario no tiene permisos
     * @throws ResourceNotFoundException si la empresa o usuario no existen
     */
    public void removerUsuarioDeEmpresa(Long empresaId, Long usuarioId) {
        logger.info("Removiendo usuario {} de empresa {}", usuarioId, empresaId);

        // Verificar permisos
        if (!empresaSecurityService.hasAccess(empresaId, "WRITE")) {
            throw new AccessDeniedException("No tiene permisos para remover usuarios de esta empresa");
        }

        // Verificar que la empresa existe
        if (!empresaRepository.existsById(empresaId)) {
            throw new ResourceNotFoundException("Empresa", "ID", empresaId);
        }

        // Verificar que el usuario existe
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new ResourceNotFoundException("Usuario", "ID", usuarioId);
        }

        // Buscar la relación
        Optional<UsuarioEmpresa> relacionOpt = usuarioEmpresaRepository
                .findByUsuarioAndEmpresa(usuarioId, empresaId);

        if (relacionOpt.isEmpty()) {
            throw new ResourceNotFoundException("Relación Usuario-Empresa", "usuarioId", usuarioId);
        }

        UsuarioEmpresa relacion = relacionOpt.get();
        relacion.setActivo(false);
        usuarioEmpresaRepository.save(relacion);

        logger.info("Usuario removido exitosamente de empresa");
    }

    /**
     * Valida el formato y dígito verificador del RUC paraguayo.
     *
     * @param ruc el RUC a validar
     * @throws IllegalArgumentException si el RUC es inválido
     */
    private void validarRuc(String ruc) {
        if (ruc == null || ruc.trim().isEmpty()) {
            throw new IllegalArgumentException("RUC no puede estar vacío");
        }

        // Validar formato
        if (!RUC_PATTERN.matcher(ruc).matches()) {
            throw new IllegalArgumentException("Formato de RUC inválido");
        }

        // Extraer partes del RUC
        String[] partes = ruc.split("-");
        String numeroBase = partes[0];
        int digitoVerificador = Integer.parseInt(partes[1]);

        // Calcular dígito verificador usando la nueva utilidad
        Integer digitoCalculado = CalcularVerificadorRuc.getDigitoVerificador(numeroBase);

        if (digitoCalculado == null || digitoCalculado != digitoVerificador) {
            throw new IllegalArgumentException("Dígito verificador inválido");
        }

        logger.debug("RUC validado correctamente: {}", ruc);
    }

    /**
     * Busca empresas por razón social.
     *
     * @param razonSocial texto a buscar
     * @return lista de empresas que coinciden
     */
    @Transactional(readOnly = true)
    public List<Empresa> buscarPorRazonSocial(String razonSocial) {
        logger.debug("Buscando empresas por razón social: {}", razonSocial);
        return empresaRepository.findByRazonSocialContainingIgnoreCase(razonSocial);
    }

    /**
     * Obtiene empresas con certificado próximo a vencer.
     *
     * @return lista de empresas con certificado por vencer
     */
    @Transactional(readOnly = true)
    public List<Empresa> obtenerEmpresasConCertificadoPorVencer() {
        logger.debug("Obteniendo empresas con certificado por vencer");
        return empresaRepository.findEmpresasConCertificadoPorVencer();
    }

    /**
     * Actualiza solo los datos del certificado de una empresa.
     *
     * @param id ID de la empresa
     * @param certificadoPath Path del certificado
     * @param certificadoPasswordEncrypted Password del certificado ya encriptado
     * @param fechaExpiracion Fecha de expiración del certificado
     * @throws ResourceNotFoundException si la empresa no existe
     * @throws AccessDeniedException   si el usuario no tiene permisos
     */
    @Transactional
    public void actualizarCertificado(Long id, String certificadoPath, String certificadoPasswordEncrypted, LocalDate fechaExpiracion) {
        logger.info("Actualizando certificado para empresa ID: {}", id);

        // Verificar permisos
        if (!empresaSecurityService.hasAccess(id, "WRITE")) {
            throw new AccessDeniedException("No tiene permisos para modificar esta empresa");
        }

        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", "ID", id));

        logger.info("📝 Path recibido para guardar en BD: {}", certificadoPath);
        logger.info("📝 Path anterior en BD: {}", empresa.getCertificadoPath());

        empresa.setCertificadoPath(certificadoPath);
        empresa.setCertificadoPasswordEncrypted(certificadoPasswordEncrypted);
        empresa.setCertificadoFechaExpiracion(fechaExpiracion);

        Empresa empresaGuardada = empresaRepository.save(empresa);
        
        // Verificar que se guardó correctamente
        Empresa empresaVerificada = empresaRepository.findById(id).orElse(null);
        if (empresaVerificada != null) {
            logger.info("✅ Certificado actualizado exitosamente - Path guardado en BD: {}", empresaVerificada.getCertificadoPath());
            logger.info("✅ Password guardado: {}", empresaVerificada.getCertificadoPasswordEncrypted() != null ? "Sí" : "No");
            logger.info("✅ Fecha expiración guardada: {}", empresaVerificada.getCertificadoFechaExpiracion());
        }
        logger.info("✅ Certificado actualizado para empresa ID: {}", id);
    }

    /**
     * Verifica si el usuario autenticado tiene un rol específico.
     *
     * @param authentication Autenticación actual
     * @param roleName Nombre del rol a verificar
     * @return true si tiene el rol
     */
    private boolean hasRole(Authentication authentication, String roleName) {
        return authentication.getAuthorities().stream()
            .anyMatch(grantedAuthority -> 
                grantedAuthority.getAuthority().equals("ROLE_" + roleName) ||
                grantedAuthority.getAuthority().equals(roleName)
            );
    }
}
