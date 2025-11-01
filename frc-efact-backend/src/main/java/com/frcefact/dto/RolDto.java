package com.frcefact.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * DTO para transferencia de datos de Rol.
 * Utilizado en operaciones CRUD de roles del sistema.
 */
public class RolDto {

    private Long id;

    @NotBlank(message = "Nombre del rol es requerido")
    @Size(max = 50, message = "Nombre del rol no debe exceder 50 caracteres")
    private String nombre;

    @Size(max = 500, message = "Descripción no debe exceder 500 caracteres")
    private String descripcion;

    private LocalDateTime creadoEn;

    // Constructores
    public RolDto() {
    }

    public RolDto(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public RolDto(Long id, String nombre, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }

    @Override
    public String toString() {
        return "RolDto{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", creadoEn=" + creadoEn +
                '}';
    }
}
