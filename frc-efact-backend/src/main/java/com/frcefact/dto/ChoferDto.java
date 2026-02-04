package com.frcefact.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para Chofer.
 */
public class ChoferDto {

    private Long id;

    @NotBlank(message = "El nombre es requerido")
    @Size(max = 200, message = "El nombre no debe exceder 200 caracteres")
    private String nombre;

    @Size(max = 20, message = "El documento no debe exceder 20 caracteres")
    private String documento;

    private String direccion;

    private Boolean activo = true;

    private Long empresaId;

    public ChoferDto() {
    }

    public ChoferDto(Long id, String nombre, String documento, String direccion, Boolean activo, Long empresaId) {
        this.id = id;
        this.nombre = nombre;
        this.documento = documento;
        this.direccion = direccion;
        this.activo = activo;
        this.empresaId = empresaId;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
    }

    @Override
    public String toString() {
        return "ChoferDto{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", documento='" + documento + '\'' +
                ", activo=" + activo +
                '}';
    }
}
