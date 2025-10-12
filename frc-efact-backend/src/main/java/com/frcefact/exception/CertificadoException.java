package com.frcefact.exception;

/**
 * Excepción específica para errores relacionados con certificados digitales.
 */
public class CertificadoException extends BusinessException {
    
    public CertificadoException(String message) {
        super(message);
    }
    
    public CertificadoException(String message, Throwable cause) {
        super(message, cause);
    }
}
