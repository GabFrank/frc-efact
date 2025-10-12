package com.frcefact.service;

import com.frcefact.annotation.Auditable;
import com.frcefact.model.AccionEnum;
import com.frcefact.model.Empresa;
import com.frcefact.model.Usuario;
import com.frcefact.model.UsuarioEmpresa;
import com.frcefact.repository.EmpresaRepository;
import com.frcefact.repository.UsuarioRepository;
import com.frcefact.repository.UsuarioEmpresaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Servicio para gestión de empresas.
 * Implementa CRUD completo con validación de RUC paraguayo y control de permisos.
 */
@Service
@Transactional
public class EmpresaService {

    private static final Logger logger = LoggerFactory.getLogger(EmpresaService.class);
    
    // Patrón para validar RUC paraguayo: formato XXXXXXXX-X (8 dígitos, guión, 1 dígito verificador)
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
            throw new IllegalArgumentException("Ya existe una empresa con el RUC: " + empresa.getRuc());
        }
        
        empresa.setActivo(true);
        Empresa empresaGuardada = empresaRepository.save(empresa);
        
        logger.info("Empresa creada exitosamente con ID: {}", empresaGuardada.getId());
        return empresaGuardada;
    }

    /**
     * Actualiza una empresa existente.
     *
     * @param id ID de la empresa
     * @param empresaActualizada datos actualizados
     * @return la empresa actualizada
     * @throws EntityNotFoundException si la empresa no existe
     * @throws AccessDeniedException si el usuario no tiene permisos
     */
    @Auditable(entidad = "Empresa", accion = AccionEnum.UPDATE)
    public Empresa actualizarEmpresa(Long id, Empresa empresaActualizada) {
        logger.info("Actualizando empresa con ID: {}", id);
        
        // Verificar permisos
        if (!empresaSecurityService.hasAccess(id, "WRITE")) {
            throw new AccessDeniedException("No tiene permisos para modificar esta empresa");
        }
        
        Empresa empresaExistente = empresaRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada con ID: " + id));
        
        // Validar RUC si cambió
        if (!empresaExistente.getRuc().equals(empresaActualizada.getRuc())) {
            validarRuc(empresaActualizada.getRuc());
            if (empresaRepository.existsByRuc(empresaActualizada.getRuc())) {
                throw new IllegalArgumentException("Ya existe una empresa con el RUC: " + empresaActualizada.getRuc());
            }
        }
        
        // Actualizar campos
        empresaExistente.setRazonSocial(empresaActualizada.getRazonSocial());
        empresaExistente.setRuc(empresaActualizada.getRuc());
        empresaExistente.setNombreFantasia(empresaActualizada.getNombreFantasia());
        empresaExistente.setEmail(empresaActualizada.getEmail());
        empresaExistente.setTelefono(empresaActualizada.getTelefono());
        empresaExistente.setDireccion(empresaActualizada.getDireccion());
        empresaExistente.setTipoSociedad(empresaActualizada.getTipoSociedad());
        
        // Actualizar domicilio fiscal
        empresaExistente.setDomicilioFiscalDepartamento(empresaActualizada.getDomicilioFiscalDepartamento());
        empresaExistente.setDomicilioFiscalCiudad(empresaActualizada.getDomicilioFiscalCiudad());
        empresaExistente.setDomicilioFiscalCodigoCiudad(empresaActualizada.getDomicilioFiscalCodigoCiudad());
        empresaExistente.setDomicilioFiscalLocalidad(empresaActualizada.getDomicilioFiscalLocalidad());
        empresaExistente.setDomicilioFiscalBarrio(empresaActualizada.getDomicilioFiscalBarrio());
        empresaExistente.setDomicilioFiscalDireccion(empresaActualizada.getDomicilioFiscalDireccion());
        
        // Actualizar actividad económica
        empresaExistente.setCodActividadEconomicaPrincipal(empresaActualizada.getCodActividadEconomicaPrincipal());
        empresaExistente.setDescActividadEconomicaPrincipal(empresaActualizada.getDescActividadEconomicaPrincipal());
        empresaExistente.setListCodigoActividadEconomicaSecundaria(empresaActualizada.getListCodigoActividadEconomicaSecundaria());
        empresaExistente.setListDescripcionActividadEconomicaSecundaria(empresaActualizada.getListDescripcionActividadEconomicaSecundaria());
        
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
     * @throws AccessDeniedException si el usuario no tiene permisos
     */
    @Transactional(readOnly = true)
    public Empresa obtenerEmpresaPorId(Long id) {
        logger.debug("Obteniendo empresa con ID: {}", id);
        
        // Verificar permisos
        if (!empresaSecurityService.hasAccess(id, "READ")) {
            throw new AccessDeniedException("No tiene permisos para ver esta empresa");
        }
        
        return empresaRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada con ID: " + id));
    }

    /**
     * Obtiene todas las empresas activas.
     *
     * @return lista de empresas activas
     */
    @Transactional(readOnly = true)
    public List<Empresa> obtenerTodasLasEmpresas() {
        logger.debug("Obteniendo todas las empresas activas");
        return empresaRepository.findByActivo(true);
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
            .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + username));
        
        return empresaRepository.findByUsuarioId(usuario.getId());
    }

    /**
     * Desactiva una empresa (soft delete).
     *
     * @param id ID de la empresa
     * @throws EntityNotFoundException si la empresa no existe
     * @throws AccessDeniedException si el usuario no tiene permisos
     */
    public void desactivarEmpresa(Long id) {
        logger.info("Desactivando empresa con ID: {}", id);
        
        // Verificar permisos
        if (!empresaSecurityService.hasAccess(id, "WRITE")) {
            throw new AccessDeniedException("No tiene permisos para desactivar esta empresa");
        }
        
        Empresa empresa = empresaRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada con ID: " + id));
        
        empresa.setActivo(false);
        empresaRepository.save(empresa);
        
        logger.info("Empresa desactivada exitosamente con ID: {}", id);
    }

    /**
     * Asigna un usuario a una empresa con un rol específico.
     *
     * @param empresaId ID de la empresa
     * @param usuarioId ID del usuario
     * @param rolEmpresa rol del usuario en la empresa (ADMINISTRADOR o LECTOR)
     * @return la relación usuario-empresa creada
     * @throws EntityNotFoundException si la empresa o usuario no existen
     * @throws AccessDeniedException si el usuario no tiene permisos
     * @throws IllegalArgumentException si el rol es inválido
     */
    public UsuarioEmpresa asignarUsuarioAEmpresa(Long empresaId, Long usuarioId, String rolEmpresa) {
        logger.info("Asignando usuario {} a empresa {} con rol {}", usuarioId, empresaId, rolEmpresa);
        
        // Verificar permisos
        if (!empresaSecurityService.hasAccess(empresaId, "WRITE")) {
            throw new AccessDeniedException("No tiene permisos para asignar usuarios a esta empresa");
        }
        
        // Validar rol
        if (!rolEmpresa.equals("ADMINISTRADOR") && !rolEmpresa.equals("LECTOR")) {
            throw new IllegalArgumentException("Rol inválido. Debe ser ADMINISTRADOR o LECTOR");
        }
        
        Empresa empresa = empresaRepository.findById(empresaId)
            .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada con ID: " + empresaId));
        
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + usuarioId));
        
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
            throw new IllegalArgumentException("Formato de RUC inválido. Debe ser XXXXXXXX-X");
        }
        
        // Extraer partes del RUC
        String[] partes = ruc.split("-");
        String numeroBase = partes[0];
        int digitoVerificador = Integer.parseInt(partes[1]);
        
        // Calcular dígito verificador
        int digitoCalculado = calcularDigitoVerificadorRuc(numeroBase);
        
        if (digitoCalculado != digitoVerificador) {
            throw new IllegalArgumentException("Dígito verificador del RUC es inválido");
        }
        
        logger.debug("RUC validado correctamente: {}", ruc);
    }

    /**
     * Calcula el dígito verificador del RUC paraguayo usando el algoritmo módulo 11.
     *
     * @param numeroBase los primeros 8 dígitos del RUC
     * @return el dígito verificador calculado
     */
    private int calcularDigitoVerificadorRuc(String numeroBase) {
        int[] multiplicadores = {2, 3, 4, 5, 6, 7, 2, 3};
        int suma = 0;
        
        for (int i = 0; i < numeroBase.length(); i++) {
            int digito = Character.getNumericValue(numeroBase.charAt(i));
            suma += digito * multiplicadores[i];
        }
        
        int resto = suma % 11;
        int digitoVerificador = 11 - resto;
        
        // Si el resultado es 11, el dígito verificador es 0
        // Si el resultado es 10, el dígito verificador es 1
        if (digitoVerificador == 11) {
            return 0;
        } else if (digitoVerificador == 10) {
            return 1;
        }
        
        return digitoVerificador;
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
}
