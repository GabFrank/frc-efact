package com.frcefact.security;

import com.frcefact.model.Usuario;
import com.frcefact.repository.UsuarioRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * Implementación personalizada de UserDetailsService para Spring Security.
 * Carga los detalles del usuario desde la base de datos.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        return buildUserDetails(usuario);
    }

    /**
     * Cargar usuario por ID.
     *
     * @param id el ID del usuario
     * @return UserDetails del usuario
     * @throws UsernameNotFoundException si el usuario no existe
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con ID: " + id));

        return buildUserDetails(usuario);
    }

    /**
     * Cargar UserDetails directamente desde una entidad Usuario.
     * Útil para autenticación por token donde ya se recuperó el usuario.
     *
     * @param usuario la entidad Usuario
     * @return UserDetails del usuario
     */
    public UserDetails loadUserByEntity(Usuario usuario) {
        return buildUserDetails(usuario);
    }

    /**
     * Construir UserDetails desde la entidad Usuario.
     *
     * @param usuario la entidad Usuario
     * @return UserDetails para Spring Security
     */
    private UserDetails buildUserDetails(Usuario usuario) {
        // Verificar si la cuenta está bloqueada
        boolean accountNonLocked = !usuario.isAccountLocked();

        // Para usuarios de Auth0 sin password local, usar un placeholder
        // que nunca puede ser usado para autenticación local
        String password = usuario.getPasswordHash();
        if (password == null) {
            // Password placeholder para usuarios que solo se autentican mediante Auth0
            // Este password nunca puede ser usado para login tradicional
            password = "[AUTH0_ONLY]";
        }

        return User.builder()
                .username(usuario.getUsername())
                .password(password)
                .disabled(!usuario.getIsActive())
                .accountLocked(!accountNonLocked)
                .authorities(getAuthorities(usuario))
                .build();
    }

    /**
     * Obtener authorities/roles del usuario desde la base de datos.
     * Carga los roles asignados al usuario en la tabla persona.usuario_rol.
     *
     * @param usuario la entidad Usuario
     * @return colección de authorities
     */
    private Collection<? extends GrantedAuthority> getAuthorities(Usuario usuario) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        // Cargar roles desde la relación usuario-rol
        if (usuario.getUsuarioRoles() != null && !usuario.getUsuarioRoles().isEmpty()) {
            usuario.getUsuarioRoles().forEach(usuarioRol -> {
                if (usuarioRol.getRol() != null) {
                    // Agregar el rol con prefijo ROLE_ para Spring Security
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + usuarioRol.getRol().getNombre()));
                }
            });
        }

        // Mapear roles de empresa activos a roles de sistema para autorización en controladores.
        // Esto permite que un ADMINISTRADOR/FACTURADOR/LECTOR de empresa use endpoints protegidos
        // con hasAnyRole('EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR').
        if (usuario.getUsuarioEmpresas() != null && !usuario.getUsuarioEmpresas().isEmpty()) {
            usuario.getUsuarioEmpresas().forEach(usuarioEmpresa -> {
                if (usuarioEmpresa == null || !Boolean.TRUE.equals(usuarioEmpresa.getActivo())) {
                    return;
                }
                String rolEmpresa = usuarioEmpresa.getRolEmpresa();
                if (rolEmpresa == null || rolEmpresa.isBlank()) {
                    return;
                }

                String rolSistema = switch (rolEmpresa.toUpperCase(Locale.ROOT)) {
                    case "ADMINISTRADOR" -> "EMPRESA_ADMIN";
                    case "FACTURADOR" -> "FACTURADOR";
                    case "LECTOR" -> "LECTOR";
                    default -> null;
                };

                if (rolSistema != null) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + rolSistema));
                }
            });
        }
        
        // Si no tiene roles asignados, dar rol USER por defecto
        if (authorities.isEmpty()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }
        
        return authorities;
    }
}
