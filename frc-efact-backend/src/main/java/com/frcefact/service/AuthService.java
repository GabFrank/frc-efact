package com.frcefact.service;

import com.frcefact.dto.AuthResponse;
import com.frcefact.dto.LoginRequest;
import com.frcefact.dto.UsuarioDto;
import com.frcefact.model.Usuario;
import com.frcefact.repository.UsuarioRepository;
import com.frcefact.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de autenticación para login, logout y refresh de tokens.
 */
@Service
@Transactional
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;

    public AuthService(AuthenticationManager authenticationManager,
                      JwtTokenProvider tokenProvider,
                      UsuarioRepository usuarioRepository,
                      UsuarioService usuarioService) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
    }

    /**
     * Autenticar usuario y generar tokens JWT.
     *
     * @param loginRequest request con credenciales
     * @return AuthResponse con tokens y datos del usuario
     * @throws AuthenticationException si las credenciales son inválidas
     */
    public AuthResponse login(LoginRequest loginRequest) {
        Usuario usuario = usuarioRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        // Verificar si la cuenta está bloqueada
        if (usuario.isAccountLocked()) {
            throw new LockedException("Cuenta bloqueada temporalmente. Intente más tarde.");
        }

        // Verificar si la cuenta está activa
        if (!usuario.getIsActive()) {
            throw new DisabledException("Cuenta desactivada");
        }

        try {
            // Autenticar con Spring Security
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            // Resetear intentos fallidos y actualizar último login
            usuarioService.resetearIntentosFallidos(usuario.getUsername());
            usuarioService.actualizarUltimoLogin(usuario.getUsername());

            // Generar tokens
            String token = tokenProvider.generateToken(usuario.getUsername(), usuario.getId());
            String refreshToken = tokenProvider.generateRefreshToken(usuario.getUsername(), usuario.getId());

            // Recargar usuario con datos actualizados
            usuario = usuarioRepository.findByUsername(usuario.getUsername()).orElseThrow();

            return new AuthResponse(token, refreshToken, UsuarioDto.fromEntity(usuario));

        } catch (AuthenticationException ex) {
            // Registrar intento fallido
            usuarioService.registrarIntentoFallidoLogin(usuario.getUsername());
            throw new BadCredentialsException("Credenciales inválidas");
        }
    }

    /**
     * Refrescar token JWT usando refresh token.
     *
     * @param refreshToken el refresh token
     * @return AuthResponse con nuevos tokens
     * @throws BadCredentialsException si el refresh token es inválido
     */
    public AuthResponse refreshToken(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new BadCredentialsException("Refresh token inválido o expirado");
        }

        if (!tokenProvider.isRefreshToken(refreshToken)) {
            throw new BadCredentialsException("Token proporcionado no es un refresh token");
        }

        String username = tokenProvider.getUsernameFromToken(refreshToken);
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado"));

        // Verificar si la cuenta está activa
        if (!usuario.getIsActive()) {
            throw new DisabledException("Cuenta desactivada");
        }

        // Generar nuevos tokens
        String newToken = tokenProvider.generateToken(usuario.getUsername(), usuario.getId());
        String newRefreshToken = tokenProvider.generateRefreshToken(usuario.getUsername(), usuario.getId());

        return new AuthResponse(newToken, newRefreshToken, UsuarioDto.fromEntity(usuario));
    }

    /**
     * Logout de usuario (invalidar tokens).
     * Nota: En una implementación completa, se debería mantener una lista negra de tokens.
     *
     * @param username el username del usuario
     */
    public void logout(String username) {
        // En una implementación completa, aquí se agregaría el token a una lista negra
        // o se invalidaría en Redis/base de datos
        // Por ahora, simplemente registramos el logout
        usuarioRepository.findByUsername(username).ifPresent(usuario -> {
            // Aquí se puede agregar lógica adicional si es necesario
        });
    }
}
