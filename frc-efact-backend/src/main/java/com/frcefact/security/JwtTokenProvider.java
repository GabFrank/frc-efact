package com.frcefact.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Proveedor de tokens JWT para generación, validación y refresh de tokens.
 */
@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms:86400000}") // 24 horas por defecto
    private long jwtExpirationMs;

    @Value("${jwt.refresh-expiration-ms:604800000}") // 7 días por defecto
    private long jwtRefreshExpirationMs;

    /**
     * Generar token JWT para un usuario.
     *
     * @param username el username del usuario
     * @param userId el ID del usuario
     * @return el token JWT generado
     */
    public String generateToken(String username, Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }
    
    /**
     * Generar refresh token para un usuario.
     *
     * @param username el username del usuario
     * @param userId el ID del usuario
     * @return el refresh token generado
     */
    public String generateRefreshToken(String username, Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtRefreshExpirationMs);

        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Obtener username del token JWT.
     *
     * @param token el token JWT
     * @return el username extraído del token
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    /**
     * Obtener user ID del token JWT.
     *
     * @param token el token JWT
     * @return el user ID extraído del token
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("userId", Long.class);
    }

    /**
     * Verificar si el token es un token local (no es un token de Auth0).
     * Los tokens locales usan algoritmos simétricos (HS256, HS512, etc.),
     * mientras que Auth0 usa RS256 (algoritmo asimétrico).
     * 
     * @param token el token JWT
     * @return true si el token NO es RS256 (es decir, es un token local)
     */
    public boolean isLocalToken(String token) {
        try {
            // Parsear el header sin validar la firma para obtener el algoritmo
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return false;
            }
            
            // Decodificar el header (base64url)
            String headerJson = new String(java.util.Base64.getUrlDecoder().decode(parts[0]));
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode header = mapper.readTree(headerJson);
            String alg = header.get("alg").asText();
            
            // Si NO es RS256 (algoritmo de Auth0), entonces es un token local
            return !"RS256".equals(alg);
        } catch (Exception ex) {
            logger.debug("Error detectando algoritmo del token: {}", ex.getMessage());
            return false;
        }
    }

    /**
     * Validar token JWT.
     *
     * @param token el token JWT a validar
     * @return true si el token es válido
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SignatureException ex) {
            logger.error("Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            // No loguear como ERROR si es un token con algoritmo diferente (probablemente Auth0)
            logger.debug("Unsupported JWT token algorithm (likely Auth0 token): {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }

    /**
     * Verificar si el token es un refresh token.
     *
     * @param token el token JWT
     * @return true si es un refresh token
     */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return "refresh".equals(claims.get("type", String.class));
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Obtener fecha de expiración del token.
     *
     * @param token el token JWT
     * @return la fecha de expiración
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getExpiration();
    }

    /**
     * Verificar si el token ha expirado.
     *
     * @param token el token JWT
     * @return true si el token ha expirado
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (Exception ex) {
            return true;
        }
    }

    /**
     * Obtener la clave de firma para JWT.
     *
     * @return la clave secreta
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
