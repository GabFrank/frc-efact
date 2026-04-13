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
                } else if (isTokenFromQueryParam(request)) {
                    // Token Auth0 (no local) llegó por query param ?token=...
                    // El BearerTokenAuthenticationFilter de OAuth2 solo lee el header Authorization,
                    // así que inyectamos el token como header para que lo procese.
                    filterChain.doFilter(new BearerTokenRequestWrapper(request, jwt), response);
                    return;
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
     * Verifica si el token se obtuvo del parámetro 'token' en la URL (no del header Authorization).
     */
    private boolean isTokenFromQueryParam(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        boolean hasAuthHeader = StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ");
        String tokenParam = request.getParameter("token");
        return !hasAuthHeader && StringUtils.hasText(tokenParam);
    }

    /**
     * Wrapper que inyecta un header Authorization: Bearer para que el
     * BearerTokenAuthenticationFilter de OAuth2 procese tokens Auth0
     * que llegaron por query param (?token=...) en lugar de por header.
     */
    private static class BearerTokenRequestWrapper extends jakarta.servlet.http.HttpServletRequestWrapper {
        private final String token;

        BearerTokenRequestWrapper(HttpServletRequest request, String token) {
            super(request);
            this.token = token;
        }

        @Override
        public String getHeader(String name) {
            if ("Authorization".equalsIgnoreCase(name)) {
                return "Bearer " + token;
            }
            return super.getHeader(name);
        }

        @Override
        public java.util.Enumeration<String> getHeaders(String name) {
            if ("Authorization".equalsIgnoreCase(name)) {
                return java.util.Collections.enumeration(java.util.List.of("Bearer " + token));
            }
            return super.getHeaders(name);
        }

        @Override
        public java.util.Enumeration<String> getHeaderNames() {
            java.util.List<String> names = new java.util.ArrayList<>();
            java.util.Enumeration<String> original = super.getHeaderNames();
            while (original.hasMoreElements()) {
                names.add(original.nextElement());
            }
            if (!names.stream().anyMatch(n -> "Authorization".equalsIgnoreCase(n))) {
                names.add("Authorization");
            }
            return java.util.Collections.enumeration(names);
        }
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
