package com.frcefact.dto.geografia;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO para representar un departamento.
 */
@Schema(description = "Información de un departamento")
public class DepartamentoDto {

    @Schema(description = "ID del departamento", example = "1")
    private Long id;

    @Schema(description = "Código del departamento", example = "11")
    private String codigo;

    @Schema(description = "Nombre del departamento", example = "CENTRAL")
    private String nombre;

    @Schema(description = "Código del país", example = "PY")
    private String paisCodigo;

    @Schema(description = "Nombre del país", example = "PARAGUAY")
    private String paisNombre;

    @Schema(description = "Estado activo", example = "true")
    private Boolean activo;

    // Constructors
    public DepartamentoDto() {}

    public DepartamentoDto(Long id, String codigo, String nombre, String paisCodigo, String paisNombre, Boolean activo) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.paisCodigo = paisCodigo;
        this.paisNombre = paisNombre;
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

    public String getPaisCodigo() {
        return paisCodigo;
    }

    public void setPaisCodigo(String paisCodigo) {
        this.paisCodigo = paisCodigo;
    }

    public String getPaisNombre() {
        return paisNombre;
    }

    public void setPaisNombre(String paisNombre) {
        this.paisNombre = paisNombre;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}