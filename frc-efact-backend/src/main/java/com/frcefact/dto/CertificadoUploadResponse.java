package com.frcefact.dto;

import java.time.LocalDate;

/**
 * DTO para respuesta del upload de certificado.
 */
public class CertificadoUploadResponse {
    
    private String certificadoPath;
    private LocalDate fechaExpiracion;
    private String mensaje;

    public CertificadoUploadResponse() {
    }

    public CertificadoUploadResponse(String certificadoPath, LocalDate fechaExpiracion, String mensaje) {
        this.certificadoPath = certificadoPath;
        this.fechaExpiracion = fechaExpiracion;
        this.mensaje = mensaje;
    }

    public String getCertificadoPath() {
        return certificadoPath;
    }

    public void setCertificadoPath(String certificadoPath) {
        this.certificadoPath = certificadoPath;
    }

    public LocalDate getFechaExpiracion() {
        return fechaExpiracion;
    }

    public void setFechaExpiracion(LocalDate fechaExpiracion) {
        this.fechaExpiracion = fechaExpiracion;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
