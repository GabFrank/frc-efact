package com.frcefact.service;

import com.frcefact.model.Usuario;
import com.frcefact.model.UsuarioEmpresa;
import com.frcefact.repository.UsuarioEmpresaRepository;
import com.frcefact.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Servicio de seguridad para verificar acceso de usuarios a empresas.
 * Implementa la lógica de autorización a nivel de empresa.
 */
@Service
@Transactional(readOnly = true)
public class EmpresaSecurityService {

    private final UsuarioEmpresaRepository usuarioEmpresaRepository;
    private final UsuarioRepository usuarioRepository;

    public EmpresaSecurityService(
            UsuarioEmpresaRepository usuarioEmpresaRepository,
            UsuarioRepository usuarioRepository) {
        this.usuarioEmpresaRepository = usuarioEmpresaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Verifica si el usuario actual tiene acceso a una empresa con el nivel de permiso especificado.
     *
     * @param empresaId ID de la empresa
     * @param tipoAcceso Tipo de acceso requerido: "READ" o "WRITE"
     * @return true si el usuario tiene acceso, false en caso contrario
     */
    public boolean hasAccess(Long empresaId, String tipoAcceso) {
        // Obtener el usuario autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String username = authentication.getName();
        
        // Los usuarios ADMIN tienen acceso completo a todas las empresas
        if (hasRole(authentication, "ADMIN")) {
            return true;
        }

        // Los usuarios EMPRESA_ADMIN y FACTURADOR del sistema también tienen acceso si están asignados a la empresa
        boolean isEmpresaAdmin = hasRole(authentication, "EMPRESA_ADMIN");
        boolean isFacturador = hasRole(authentication, "FACTURADOR");
        
        // Buscar el usuario
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
        if (usuarioOpt.isEmpty()) {
            return false;
        }

        Usuario usuario = usuarioOpt.get();

        // Buscar la relación usuario-empresa
        Optional<UsuarioEmpresa> usuarioEmpresaOpt = 
            usuarioEmpresaRepository.findByUsuarioAndEmpresa(usuario.getId(), empresaId);

        if (usuarioEmpresaOpt.isEmpty()) {
            return false;
        }

        UsuarioEmpresa usuarioEmpresa = usuarioEmpresaOpt.get();

        // Verificar si está activo
        if (!usuarioEmpresa.getActivo()) {
            return false;
        }
        
        // Si es EMPRESA_ADMIN o FACTURADOR del sistema y está asignado a la empresa, tiene acceso de lectura
        if ((isEmpresaAdmin || isFacturador) && "READ".equalsIgnoreCase(tipoAcceso)) {
            return true;
        }
        
        // Verificar el tipo de acceso
        if ("WRITE".equalsIgnoreCase(tipoAcceso)) {
            // Usuarios con rol del sistema FACTURADOR o EMPRESA_ADMIN pueden escribir
            // si están asignados a la empresa (independientemente del rol de empresa)
            if (isFacturador || isEmpresaAdmin) {
                return true;
            }
            // Si no tiene rol del sistema especial, solo ADMINISTRADOR de la empresa puede escribir
            return "ADMINISTRADOR".equals(usuarioEmpresa.getRolEmpresa());
        } else if ("READ".equalsIgnoreCase(tipoAcceso)) {
            // ADMINISTRADOR, FACTURADOR y LECTOR pueden leer
            String rolEmpresa = usuarioEmpresa.getRolEmpresa();
            return "ADMINISTRADOR".equals(rolEmpresa) ||
                   "FACTURADOR".equals(rolEmpresa) ||
                   "LECTOR".equals(rolEmpresa);
        }

        return false;
    }

    /**
     * Verifica si el usuario actual tiene acceso de lectura a una empresa.
     *
     * @param empresaId ID de la empresa
     * @return true si el usuario tiene acceso de lectura
     */
    public boolean hasReadAccess(Long empresaId) {
        return hasAccess(empresaId, "READ");
    }

    /**
     * Verifica si el usuario actual tiene acceso de escritura a una empresa.
     *
     * @param empresaId ID de la empresa
     * @return true si el usuario tiene acceso de escritura
     */
    public boolean hasWriteAccess(Long empresaId) {
        return hasAccess(empresaId, "WRITE");
    }

    /**
     * Verifica si el usuario actual es administrador de una empresa.
     *
     * @param empresaId ID de la empresa
     * @return true si el usuario es administrador de la empresa
     */
    public boolean isEmpresaAdmin(Long empresaId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String username = authentication.getName();
        
        // Los usuarios ADMIN del sistema son considerados administradores de todas las empresas
        if (hasRole(authentication, "ADMIN")) {
            return true;
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
        if (usuarioOpt.isEmpty()) {
            return false;
        }

        Usuario usuario = usuarioOpt.get();
        Optional<UsuarioEmpresa> usuarioEmpresaOpt = 
            usuarioEmpresaRepository.findByUsuarioAndEmpresa(usuario.getId(), empresaId);

        return usuarioEmpresaOpt.isPresent() &&
               usuarioEmpresaOpt.get().getActivo() &&
               "ADMINISTRADOR".equals(usuarioEmpresaOpt.get().getRolEmpresa());
    }

    /**
     * Obtiene el usuario actual autenticado.
     *
     * @return Optional con el usuario si está autenticado
     */
    public Optional<Usuario> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        String username = authentication.getName();
        return usuarioRepository.findByUsername(username);
    }

    /**
     * Verifica si la autenticación actual tiene un rol específico.
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

    /**
     * Verifica acceso de lectura y lanza excepción si no tiene permiso.
     *
     * @param empresaId ID de la empresa
     * @throws org.springframework.security.access.AccessDeniedException si no tiene acceso
     */
    public void verificarAccesoLectura(Long empresaId) {
        if (!hasReadAccess(empresaId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                "No tiene permisos de lectura para la empresa con ID: " + empresaId);
        }
    }

    /**
     * Verifica acceso de escritura y lanza excepción si no tiene permiso.
     *
     * @param empresaId ID de la empresa
     * @throws org.springframework.security.access.AccessDeniedException si no tiene acceso
     */
    public void verificarAccesoEscritura(Long empresaId) {
        if (!hasWriteAccess(empresaId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                "No tiene permisos de escritura para la empresa con ID: " + empresaId);
        }
    }
}
