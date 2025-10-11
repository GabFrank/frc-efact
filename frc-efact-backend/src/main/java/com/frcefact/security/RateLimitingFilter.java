package com.frcefact.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
 * Configurable vía application.yml
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitingFilter.class);

    // Máximo de intentos permitidos (configurable vía application.yml)
    @Value("${rate.limit.max-attempts:100}")
    private int maxAttempts;
    
    // Ventana de tiempo en minutos (configurable vía application.yml)
    @Value("${rate.limit.time-window-minutes:15}")
    private int timeWindowMinutes;
    
    // Habilitar/deshabilitar rate limiting (configurable vía application.yml)
    @Value("${rate.limit.enabled:true}")
    private boolean rateLimitEnabled;

    // Almacenamiento en memoria de intentos por IP
    private final Map<String, LoginAttempt> loginAttempts = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // Solo aplicar rate limiting si está habilitado y es un endpoint de login
        if (rateLimitEnabled && request.getRequestURI().contains("/auth/login") && "POST".equals(request.getMethod())) {
            String clientIp = getClientIp(request);
            
            if (isRateLimited(clientIp)) {
                logger.warn("Rate limit exceeded for IP: {} (max: {} attempts in {} minutes)", 
                    clientIp, maxAttempts, timeWindowMinutes);
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write(
                    "{\"status\": 429, \"message\": \"Demasiados intentos de login. Intente más tarde.\", " +
                    "\"maxAttempts\": " + maxAttempts + ", " +
                    "\"timeWindowMinutes\": " + timeWindowMinutes + ", " +
                    "\"timestamp\": \"" + LocalDateTime.now() + "\"}"
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
        if (Duration.between(attempt.getFirstAttempt(), LocalDateTime.now()).toMinutes() > timeWindowMinutes) {
            loginAttempts.remove(clientIp);
            return false;
        }

        return attempt.getCount() >= maxAttempts;
    }

    /**
     * Registrar un intento de login.
     */
    private void recordAttempt(String clientIp) {
        loginAttempts.compute(clientIp, (key, attempt) -> {
            if (attempt == null) {
                return new LoginAttempt(LocalDateTime.now(), 1);
            }

            // Si han pasado más de timeWindowMinutes, resetear contador
            if (Duration.between(attempt.getFirstAttempt(), LocalDateTime.now()).toMinutes() > timeWindowMinutes) {
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
