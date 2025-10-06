package com.frcefact.util;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Test para verificar hashes de contraseñas BCrypt.
 */
public class PasswordHashTest {

    @Test
    public void testPasswordHashes() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Hash de la migración V2
        String hashV2 = "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.";
        
        // Hash de la migración V3
        String hashV3 = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
        
        // Contraseñas comunes para probar
        String[] passwords = {
            "password",
            "admin123",
            "admin",
            "test123",
            "123456",
            "password123"
        };
        
        System.out.println("=== Verificando Hash V2 (admin) ===");
        System.out.println("Hash: " + hashV2);
        for (String pwd : passwords) {
            boolean matches = encoder.matches(pwd, hashV2);
            System.out.println("Password '" + pwd + "': " + (matches ? "✓ MATCH" : "✗ NO MATCH"));
        }
        
        System.out.println("\n=== Verificando Hash V3 (testuser) ===");
        System.out.println("Hash: " + hashV3);
        for (String pwd : passwords) {
            boolean matches = encoder.matches(pwd, hashV3);
            System.out.println("Password '" + pwd + "': " + (matches ? "✓ MATCH" : "✗ NO MATCH"));
        }
        
        // Generar nuevos hashes
        System.out.println("\n=== Generando nuevos hashes ===");
        System.out.println("Hash para 'admin123': " + encoder.encode("admin123"));
        System.out.println("Hash para 'password': " + encoder.encode("password"));
    }
}
