package com.frcefact.config;

import com.frcefact.security.CustomJwtAuthenticationConverter;
import com.frcefact.security.JwtAuthenticationFilter;
import com.frcefact.security.OAuth2TokenFilter;
import com.frcefact.security.RateLimitingFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Configuración de seguridad de Spring Security con JWT.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitingFilter rateLimitingFilter;
    private final OAuth2TokenFilter oauth2TokenFilter;
    private final CustomJwtAuthenticationConverter customJwtAuthenticationConverter;

    public SecurityConfig(UserDetailsService userDetailsService,
                         JwtAuthenticationFilter jwtAuthenticationFilter,
                         RateLimitingFilter rateLimitingFilter,
                         OAuth2TokenFilter oauth2TokenFilter,
                         CustomJwtAuthenticationConverter customJwtAuthenticationConverter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.rateLimitingFilter = rateLimitingFilter;
        this.oauth2TokenFilter = oauth2TokenFilter;
        this.customJwtAuthenticationConverter = customJwtAuthenticationConverter;
    }

    /**
     * Configurar el filtro de seguridad.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Require HTTPS in production (Render handles SSL termination)
                .requiresChannel(channel -> channel
                        .requestMatchers(r -> r.getHeader("X-Forwarded-Proto") != null)
                        .requiresSecure())
                // Security headers
                .headers(headers -> headers
                        .frameOptions(frame -> frame.deny())
                        .contentTypeOptions(contentType -> {})
                        .xssProtection(xss -> {})
                        .httpStrictTransportSecurity(hsts -> hsts
                                .maxAgeInSeconds(31536000)
                                .includeSubDomains(true)
                                .preload(true))
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'; " +
                                        "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
                                        "style-src 'self' 'unsafe-inline'; " +
                                        "img-src 'self' data: https:; " +
                                        "font-src 'self' data:; " +
                                        "connect-src 'self' https://frc-efact-backend.onrender.com"))
                        .referrerPolicy(referrer -> referrer
                                .policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        .permissionsPolicy(permissions -> permissions
                                .policy("geolocation=(), microphone=(), camera=()")))
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers("/actuator/info").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        
                        // Endpoints de administración del sistema - solo ADMIN
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/usuarios/admin/**").hasRole("ADMIN")
                        .requestMatchers("/roles/**").hasRole("ADMIN")
                        
                        // Endpoints de gestión de empresas - requieren autenticación
                        // La autorización específica se maneja con @PreAuthorize en los servicios
                        .requestMatchers("/empresas/**").authenticated()
                        
                        // Endpoints de facturación - requieren autenticación
                        // La autorización específica se maneja con @PreAuthorize en los servicios
                        .requestMatchers("/facturas/**").authenticated()
                        .requestMatchers("/timbrados/**").authenticated()
                        .requestMatchers("/productos/**").authenticated()
                        .requestMatchers("/clientes/**").authenticated()
                        
                        // Endpoints de documentos electrónicos - requieren autenticación
                        .requestMatchers("/documentos-electronicos/**").authenticated()
                        .requestMatchers("/lotes/**").authenticated()
                        
                        // Endpoints de SIFEN - requieren autenticación
                        .requestMatchers("/sifen/**").authenticated()
                        
                        // Endpoints de reportes y dashboards - requieren autenticación
                        .requestMatchers("/reportes/**").authenticated()
                        .requestMatchers("/dashboard/**").authenticated()
                        
                        // Endpoints de auditoría - requieren autenticación
                        .requestMatchers("/auditoria/**").authenticated()
                        
                        // Endpoints de usuarios requieren autenticación
                        .requestMatchers("/usuarios/**").authenticated()
                        
                        // Todos los demás endpoints requieren autenticación
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(oauth2TokenFilter, org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter.class)
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(customJwtAuthenticationConverter)));

        return http.build();
    }

    /**
     * Configurar el proveedor de autenticación.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Bean para el AuthenticationManager.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Bean para el PasswordEncoder (BCrypt).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configuración de CORS.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Orígenes permitidos
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:4200",
                "https://localhost:4200",
                "https://*.onrender.com"
        ));
        
        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // Headers permitidos
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // Permitir credenciales
        configuration.setAllowCredentials(true);
        
        // Headers expuestos
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Disposition"));
        
        // Tiempo de cache para preflight requests
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
