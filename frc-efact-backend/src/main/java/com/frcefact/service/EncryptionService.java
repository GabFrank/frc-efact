package com.frcefact.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Servicio de encriptación para datos sensibles usando AES-256-GCM.
 * Utilizado para encriptar certificados .pfx, CSC de timbrados, y contraseñas de certificados.
 */
@Service
public class EncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;
    
    private final SecretKey secretKey;
    private final SecureRandom secureRandom;

    public EncryptionService(@Value("${encryption.secret-key}") String secretKeyString) {
        // La clave debe ser de 32 bytes (256 bits) para AES-256
        byte[] keyBytes = secretKeyString.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length != 32) {
            throw new IllegalArgumentException(
                "La clave de encriptación debe tener exactamente 32 caracteres (256 bits)"
            );
        }
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
        this.secureRandom = new SecureRandom();
    }

    /**
     * Encripta un texto plano usando AES-256-GCM.
     * 
     * @param plainText Texto a encriptar
     * @return Texto encriptado en Base64 (incluye IV + texto cifrado + tag de autenticación)
     * @throws RuntimeException si ocurre un error durante la encriptación
     */
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }

        try {
            // Generar IV aleatorio
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            // Configurar cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            // Encriptar
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // Combinar IV + texto cifrado
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);

            // Retornar en Base64
            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception e) {
            throw new RuntimeException("Error al encriptar datos sensibles", e);
        }
    }

    /**
     * Desencripta un texto encriptado con AES-256-GCM.
     * 
     * @param encryptedText Texto encriptado en Base64
     * @return Texto plano desencriptado
     * @throws RuntimeException si ocurre un error durante la desencriptación
     */
    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }

        try {
            // Decodificar de Base64
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);

            // Extraer IV y texto cifrado
            ByteBuffer byteBuffer = ByteBuffer.wrap(decodedBytes);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);
            byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText);

            // Configurar cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            // Desencriptar
            byte[] plainText = cipher.doFinal(cipherText);

            return new String(plainText, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error al desencriptar datos sensibles", e);
        }
    }

    /**
     * Encripta datos binarios (útil para archivos como certificados .pfx).
     * 
     * @param plainBytes Bytes a encriptar
     * @return Bytes encriptados en Base64
     * @throws RuntimeException si ocurre un error durante la encriptación
     */
    public String encryptBytes(byte[] plainBytes) {
        if (plainBytes == null || plainBytes.length == 0) {
            return null;
        }

        try {
            // Generar IV aleatorio
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            // Configurar cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            // Encriptar
            byte[] cipherText = cipher.doFinal(plainBytes);

            // Combinar IV + texto cifrado
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);

            // Retornar en Base64
            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception e) {
            throw new RuntimeException("Error al encriptar datos binarios", e);
        }
    }

    /**
     * Desencripta datos binarios.
     * 
     * @param encryptedText Texto encriptado en Base64
     * @return Bytes desencriptados
     * @throws RuntimeException si ocurre un error durante la desencriptación
     */
    public byte[] decryptBytes(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return null;
        }

        try {
            // Decodificar de Base64
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);

            // Extraer IV y texto cifrado
            ByteBuffer byteBuffer = ByteBuffer.wrap(decodedBytes);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);
            byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText);

            // Configurar cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            // Desencriptar
            return cipher.doFinal(cipherText);
        } catch (Exception e) {
            throw new RuntimeException("Error al desencriptar datos binarios", e);
        }
    }
}
