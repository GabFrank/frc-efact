package com.frcefact.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para Vehiculo.
 */
public class VehiculoDto {

    private Long id;

    @NotBlank(message = "La marca es requerida")
    @Size(max = 100, message = "La marca no debe exceder 100 caracteres")
    private String marca;

    @NotBlank(message = "La matrícula es requerida")
    @Size(max = 20, message = "La matrícula no debe exceder 20 caracteres")
    private String matricula;

    private Boolean activo = true;

    private Long empresaId;

    public VehiculoDto() {
    }

    public VehiculoDto(Long id, String marca, String matricula, Boolean activo, Long empresaId) {
        this.id = id;
        this.marca = marca;
        this.matricula = matricula;
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

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
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
        return "VehiculoDto{" +
                "id=" + id +
                ", marca='" + marca + '\'' +
                ", matricula='" + matricula + '\'' +
                ", activo=" + activo +
                '}';
    }
}
