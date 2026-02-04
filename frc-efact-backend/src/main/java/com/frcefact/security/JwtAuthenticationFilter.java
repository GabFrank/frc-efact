package com.frcefact.security;

import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de autenticación JWT que intercepta requests y valida tokens.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                // Verificar si es un token local antes de intentar validarlo
                // Si no es local, probablemente es de Auth0 (RS256) y lo dejamos pasar al OAuth2 Resource Server
                if (tokenProvider.isLocalToken(jwt)) {
                    // Es un token local, intentar validarlo
                    if (tokenProvider.validateToken(jwt)) {
                        String username = tokenProvider.getUsernameFromToken(jwt);
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            }
        } catch (UnsupportedJwtException ex) {
            // Token usa algoritmo diferente (probablemente RS256 de Auth0)
            // Lo dejamos pasar al OAuth2 Resource Server
        } catch (Exception ex) {
            // Solo loguear errores inesperados, pero continuar con el filtro chain
            // para permitir que el OAuth2 Resource Server intente validar el token
            logger.debug("JwtAuthenticationFilter - Error validando token local: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extraer JWT token del header Authorization o del parámetro 'token' en la URL.
     *
     * @param request el HTTP request
     * @return el token JWT o null si no existe
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        // 1. Intentar obtener del header Authorization
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        // 2. Intentar obtener del parámetro 'token' en la URL (útil para abrir archivos en nuevas pestañas)
        String tokenParam = request.getParameter("token");
        if (StringUtils.hasText(tokenParam)) {
            return tokenParam;
        }

        return null;
    }
}
