package com.frcefact.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * DTO para solicitud de asignación de usuario a empresa.
 */
public class AsignarUsuarioEmpresaRequest {

    @NotNull(message = "ID de usuario es requerido")
    private Long usuarioId;

    @NotBlank(message = "Rol de empresa es requerido")
    @Pattern(regexp = "ADMINISTRADOR|FACTURADOR|LECTOR", message = "Rol debe ser ADMINISTRADOR, FACTURADOR o LECTOR")
    private String rolEmpresa;

    // Constructores
    public AsignarUsuarioEmpresaRequest() {
    }

    public AsignarUsuarioEmpresaRequest(Long usuarioId, String rolEmpresa) {
        this.usuarioId = usuarioId;
        this.rolEmpresa = rolEmpresa;
    }

    // Getters y Setters
    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getRolEmpresa() {
        return rolEmpresa;
    }

    public void setRolEmpresa(String rolEmpresa) {
        this.rolEmpresa = rolEmpresa;
    }
}
