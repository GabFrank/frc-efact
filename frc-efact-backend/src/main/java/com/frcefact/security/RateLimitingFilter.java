package com.frcefact.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Filtro de rate limiting para endpoints de autenticación.
 * Limita el número de intentos de login por IP.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitingFilter.class);

    // Máximo de intentos permitidos (configurable)
    private static final int MAX_ATTEMPTS = 100; // Aumentado para desarrollo
    
    // Ventana de tiempo en minutos (configurable)
    private static final int TIME_WINDOW_MINUTES = 15;

    // Almacenamiento en memoria de intentos por IP
    private final Map<String, LoginAttempt> loginAttempts = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // Solo aplicar rate limiting a endpoints de login
        if (request.getRequestURI().contains("/auth/login") && "POST".equals(request.getMethod())) {
            String clientIp = getClientIp(request);
            
            if (isRateLimited(clientIp)) {
                logger.warn("Rate limit exceeded for IP: {}", clientIp);
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write(
                    "{\"status\": 429, \"message\": \"Demasiados intentos de login. Intente más tarde.\", \"timestamp\": \"" 
                    + LocalDateTime.now() + "\"}"
                );
                return;
            }
            
            // Registrar intento
            recordAttempt(clientIp);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Verificar si una IP ha excedido el límite de intentos.
     */
    private boolean isRateLimited(String clientIp) {
        LoginAttempt attempt = loginAttempts.get(clientIp);
        
        if (attempt == null) {
            return false;
        }

        // Limpiar intentos antiguos
        if (Duration.between(attempt.getFirstAttempt(), LocalDateTime.now()).toMinutes() > TIME_WINDOW_MINUTES) {
            loginAttempts.remove(clientIp);
            return false;
        }

        return attempt.getCount() >= MAX_ATTEMPTS;
    }

    /**
     * Registrar un intento de login.
     */
    private void recordAttempt(String clientIp) {
        loginAttempts.compute(clientIp, (key, attempt) -> {
            if (attempt == null) {
                return new LoginAttempt(LocalDateTime.now(), 1);
            }

            // Si han pasado más de TIME_WINDOW_MINUTES, resetear contador
            if (Duration.between(attempt.getFirstAttempt(), LocalDateTime.now()).toMinutes() > TIME_WINDOW_MINUTES) {
                return new LoginAttempt(LocalDateTime.now(), 1);
            }

            // Incrementar contador
            attempt.incrementCount();
            return attempt;
        });
    }

    /**
     * Obtener la IP del cliente.
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Clase interna para almacenar intentos de login.
     */
    private static class LoginAttempt {
        private final LocalDateTime firstAttempt;
        private int count;

        public LoginAttempt(LocalDateTime firstAttempt, int count) {
            this.firstAttempt = firstAttempt;
            this.count = count;
        }

        public LocalDateTime getFirstAttempt() {
            return firstAttempt;
        }

        public int getCount() {
            return count;
        }

        public void incrementCount() {
            this.count++;
        }
    }
}
