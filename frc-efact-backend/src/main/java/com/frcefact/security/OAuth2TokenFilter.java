package com.frcefact.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro que previene que el BearerTokenAuthenticationFilter de OAuth2
 * intente procesar tokens locales cuando ya hay una autenticación establecida.
 */
@Component
public class OAuth2TokenFilter extends OncePerRequestFilter {

    private final JwtDecoder jwtDecoder;
    
    // ThreadLocal para guardar el JWT antes de que se elimine el header
    private static final ThreadLocal<Jwt> JWT_THREAD_LOCAL = new ThreadLocal<>();

    public OAuth2TokenFilter(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }
    
    /**
     * Obtener el JWT del ThreadLocal (para uso en controladores)
     */
    public static Jwt getJwtFromThreadLocal() {
        return JWT_THREAD_LOCAL.get();
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        try {
            String authHeader = request.getHeader("Authorization");
            boolean hasAuthHeader = authHeader != null && authHeader.startsWith("Bearer ");
            
            // SIEMPRE intentar guardar el JWT en ThreadLocal si hay un header Authorization con Bearer
            // Esto permite que los controladores accedan al JWT incluso después de que se elimine el header
            // Nota: Si el token viene en el body de la request, no es necesario guardarlo aquí
            if (hasAuthHeader && authHeader != null) {
                try {
                    String jwtToken = authHeader.substring(7);
                    Jwt jwt = jwtDecoder.decode(jwtToken);
                    JWT_THREAD_LOCAL.set(jwt);
                    System.out.println("DEBUG OAuth2TokenFilter: JWT guardado en ThreadLocal");
                } catch (Exception e) {
                    // Si no se puede decodificar, continuar sin guardar el JWT
                    // Esto puede pasar si el token no es de Auth0, está mal formado, o si el JwtDecoder
                    // no está configurado correctamente en este punto del filtro
                    // No es crítico porque el token puede venir en el body de la request
                    // System.out.println("DEBUG OAuth2TokenFilter: Error decodificando JWT: " + e.getMessage());
                }
            }
            
            // Verificar si ya hay una autenticación establecida (por ejemplo, por el JwtAuthenticationFilter)
            Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
            boolean hasAuthentication = currentAuth != null && currentAuth.isAuthenticated();
            
            // Si ya hay una autenticación establecida, eliminar el header Authorization
            // para evitar que el BearerTokenAuthenticationFilter de OAuth2 intente procesar el token
            if (hasAuthentication && hasAuthHeader) {
                // Crear un wrapper que elimine el header Authorization
                HttpServletRequest wrappedRequest = new HttpServletRequestWrapper(request);
                filterChain.doFilter(wrappedRequest, response);
                return;
            }
            
            filterChain.doFilter(request, response);
        } finally {
            // Limpiar el ThreadLocal después de procesar la request
            JWT_THREAD_LOCAL.remove();
        }
    }
    
    /**
     * Wrapper de HttpServletRequest que elimina el header Authorization.
     */
    private static class HttpServletRequestWrapper extends jakarta.servlet.http.HttpServletRequestWrapper {
        public HttpServletRequestWrapper(HttpServletRequest request) {
            super(request);
        }
        
        @Override
        public String getHeader(String name) {
            if ("Authorization".equalsIgnoreCase(name)) {
                return null;
            }
            return super.getHeader(name);
        }
        
        @Override
        public java.util.Enumeration<String> getHeaders(String name) {
            if ("Authorization".equalsIgnoreCase(name)) {
                return java.util.Collections.emptyEnumeration();
            }
            return super.getHeaders(name);
        }
        
        @Override
        public java.util.Enumeration<String> getHeaderNames() {
            java.util.List<String> headerNames = new java.util.ArrayList<>();
            java.util.Enumeration<String> originalHeaders = super.getHeaderNames();
            while (originalHeaders.hasMoreElements()) {
                String headerName = originalHeaders.nextElement();
                if (!"Authorization".equalsIgnoreCase(headerName)) {
                    headerNames.add(headerName);
                }
            }
            return java.util.Collections.enumeration(headerNames);
        }
    }
}

