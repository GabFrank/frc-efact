package com.frcefact.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * DTO para transferencia de datos de UsuarioEmpresa.
 * Representa la relación entre un usuario y una empresa con su rol específico.
 */
public class UsuarioEmpresaDto {

    private Long id;

    @NotNull(message = "ID de usuario es requerido")
    private Long usuarioId;

    @NotNull(message = "ID de empresa es requerido")
    private Long empresaId;

    @NotBlank(message = "Rol en empresa es requerido")
    private String rolEmpresa; // ADMINISTRADOR, FACTURADOR o LECTOR

    private Boolean activo;

    // Información adicional para visualización
    private String usuarioUsername;
    private String empresaRazonSocial;

    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;

    // Constructores
    public UsuarioEmpresaDto() {
    }

    public UsuarioEmpresaDto(Long id, Long usuarioId, Long empresaId, String rolEmpresa) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.empresaId = empresaId;
        this.rolEmpresa = rolEmpresa;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
    }

    public String getRolEmpresa() {
        return rolEmpresa;
    }

    public void setRolEmpresa(String rolEmpresa) {
        this.rolEmpresa = rolEmpresa;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public String getUsuarioUsername() {
        return usuarioUsername;
    }

    public void setUsuarioUsername(String usuarioUsername) {
        this.usuarioUsername = usuarioUsername;
    }

    public String getEmpresaRazonSocial() {
        return empresaRazonSocial;
    }

    public void setEmpresaRazonSocial(String empresaRazonSocial) {
        this.empresaRazonSocial = empresaRazonSocial;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }

    public LocalDateTime getActualizadoEn() {
        return actualizadoEn;
    }

    public void setActualizadoEn(LocalDateTime actualizadoEn) {
        this.actualizadoEn = actualizadoEn;
    }

    @Override
    public String toString() {
        return "UsuarioEmpresaDto{" +
                "id=" + id +
                ", usuarioId=" + usuarioId +
                ", empresaId=" + empresaId +
                ", rolEmpresa='" + rolEmpresa + '\'' +
                ", activo=" + activo +
                ", creadoEn=" + creadoEn +
                '}';
    }
}
