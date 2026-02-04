package com.frcefact.security;

import com.frcefact.model.Usuario;
import com.frcefact.repository.UsuarioRepository;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UsuarioRepository usuarioRepository;
    private final CustomUserDetailsService customUserDetailsService;

    public CustomJwtAuthenticationConverter(UsuarioRepository usuarioRepository, CustomUserDetailsService customUserDetailsService) {
        this.usuarioRepository = usuarioRepository;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    @Transactional
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
        String auth0Id = jwt.getSubject();
        String email = jwt.getClaimAsString("email");
        Boolean emailVerified = jwt.getClaim("email_verified");
        String picture = jwt.getClaimAsString("picture"); // Imagen de perfil de Google/Auth0

        Optional<Usuario> userOptional = usuarioRepository.findByAuth0Id(auth0Id);
        
        // If not found by Auth0 ID, try by email (linking strategy)
        if (userOptional.isEmpty() && email != null) {
            userOptional = usuarioRepository.findByEmail(email);

            if (userOptional.isPresent() && Boolean.TRUE.equals(emailVerified)) {
                 // Auto-link logic continues below
            } else {
                // Don't allow login by email matching if not verified or not found
                userOptional = Optional.empty();
            }
        }

        if (userOptional.isPresent()) {
            Usuario usuario = userOptional.get();
            boolean needsUpdate = false;
            
            // Auto-vincular Auth0 ID si no está vinculado pero el email coincide
            if (usuario.getAuth0Id() == null && email != null && email.equals(usuario.getEmail())) {
                if (Boolean.TRUE.equals(emailVerified)) {
                    usuario.setAuth0Id(auth0Id);
                    needsUpdate = true;
                }
            }
            
            // Actualizar imagen de perfil si está disponible en el token y es diferente
            if (picture != null && !picture.equals(usuario.getImagenPerfil())) {
                usuario.setImagenPerfil(picture);
                needsUpdate = true;
            }
            
            if (needsUpdate) {
                usuarioRepository.save(usuario);
            }
            
            UserDetails userDetails = customUserDetailsService.loadUserByEntity(usuario);
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, jwt, userDetails.getAuthorities());
            // Guardar el JWT en los detalles para acceso posterior
            authToken.setDetails(jwt);
            return authToken;
        }

        // User not found in local DB - Auto-registro para usuarios de Auth0
        // Solo si el email está verificado
        if (email != null && Boolean.TRUE.equals(emailVerified)) {
            // Generar username único
            String baseUsername = email.split("@")[0];
            String username = baseUsername;
            int suffix = 1;
            while (usuarioRepository.existsByUsername(username)) {
                username = baseUsername + "_" + suffix;
                suffix++;
            }
            
            // Crear nuevo usuario automáticamente
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setUsername(username);
            nuevoUsuario.setEmail(email);
            nuevoUsuario.setAuth0Id(auth0Id);
            nuevoUsuario.setPasswordHash(null); // Sin password, solo Auth0
            nuevoUsuario.setIsActive(true);
            nuevoUsuario.setIntentosFallidosLogin(0);
            nuevoUsuario.setImagenPerfil(picture); // Guardar imagen de perfil de Google/Auth0
            
            // Guardar usuario
            nuevoUsuario = usuarioRepository.save(nuevoUsuario);
            
            // Cargar UserDetails (sin roles por defecto, el admin puede asignarlos después)
            UserDetails userDetails = customUserDetailsService.loadUserByEntity(nuevoUsuario);
            
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, jwt, userDetails.getAuthorities());
            // Guardar el JWT en los detalles para acceso posterior
            authToken.setDetails(jwt);
            return authToken;
        }

        // Email no verificado o no disponible - rechazar
        throw new BadCredentialsException("Usuario no encontrado y no se puede crear automáticamente. Por favor, contacte al administrador."); 
    }
}

