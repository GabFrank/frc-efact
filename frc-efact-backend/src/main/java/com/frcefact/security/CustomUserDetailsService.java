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
     * Construir UserDetails desde la entidad Usuario.
     *
     * @param usuario la entidad Usuario
     * @return UserDetails para Spring Security
     */
    private UserDetails buildUserDetails(Usuario usuario) {
        // Verificar si la cuenta está bloqueada
        boolean accountNonLocked = !usuario.isAccountLocked();

        return User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPasswordHash())
                .disabled(!usuario.getIsActive())
                .accountLocked(!accountNonLocked)
                .authorities(getAuthorities(usuario))
                .build();
    }

    /**
     * Obtener authorities/roles del usuario.
     * Por ahora retorna un rol básico USER, pero puede extenderse para soportar roles.
     *
     * @param usuario la entidad Usuario
     * @return colección de authorities
     */
    private Collection<? extends GrantedAuthority> getAuthorities(Usuario usuario) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        // Por defecto todos los usuarios tienen rol USER
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        
        // Aquí se pueden agregar más roles basados en la lógica de negocio
        // Por ejemplo, si el usuario es admin:
        // if (usuario.isAdmin()) {
        //     authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        // }
        
        return authorities;
    }
}
