package com.frcefact.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro que previene que el BearerTokenAuthenticationFilter de OAuth2
 * intente procesar tokens locales cuando ya hay una autenticación establecida.
 */
@Component
public class OAuth2TokenFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        boolean hasAuthHeader = authHeader != null && authHeader.startsWith("Bearer ");
        
        // Verificar si ya hay una autenticación establecida (por ejemplo, por el JwtAuthenticationFilter)
        boolean hasAuthentication = SecurityContextHolder.getContext().getAuthentication() != null 
            && SecurityContextHolder.getContext().getAuthentication().isAuthenticated();
        
        // Si ya hay una autenticación establecida, eliminar el header Authorization
        // para evitar que el BearerTokenAuthenticationFilter de OAuth2 intente procesar el token
        if (hasAuthentication && hasAuthHeader) {
            // Crear un wrapper que elimine el header Authorization
            HttpServletRequest wrappedRequest = new HttpServletRequestWrapper(request);
            filterChain.doFilter(wrappedRequest, response);
            return;
        }
        
        filterChain.doFilter(request, response);
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

