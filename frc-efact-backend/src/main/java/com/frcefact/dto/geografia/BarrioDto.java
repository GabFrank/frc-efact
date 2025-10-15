package com.frcefact.dto.geografia;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO para representar un barrio.
 */
@Schema(description = "Información de un barrio")
public class BarrioDto {

    @Schema(description = "ID del barrio", example = "1")
    private Long id;

    @Schema(description = "Código del barrio", example = "001")
    private String codigo;

    @Schema(description = "Nombre del barrio", example = "CENTRO")
    private String nombre;

    @Schema(description = "Código de la ciudad", example = "001")
    private String ciudadCodigo;

    @Schema(description = "Nombre de la ciudad", example = "ASUNCIÓN")
    private String ciudadNombre;

    @Schema(description = "Estado activo", example = "true")
    private Boolean activo;

    // Constructors
    public BarrioDto() {}

    public BarrioDto(Long id, String codigo, String nombre, String ciudadCodigo, String ciudadNombre, Boolean activo) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.ciudadCodigo = ciudadCodigo;
        this.ciudadNombre = ciudadNombre;
        this.activo = activo;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCiudadCodigo() {
        return ciudadCodigo;
    }

    public void setCiudadCodigo(String ciudadCodigo) {
        this.ciudadCodigo = ciudadCodigo;
    }

    public String getCiudadNombre() {
        return ciudadNombre;
    }

    public void setCiudadNombre(String ciudadNombre) {
        this.ciudadNombre = ciudadNombre;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}