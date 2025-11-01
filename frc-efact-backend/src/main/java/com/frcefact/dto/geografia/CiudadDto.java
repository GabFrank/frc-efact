package com.frcefact.dto.geografia;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO para representar una ciudad.
 */
@Schema(description = "Información de una ciudad")
public class CiudadDto {

    @Schema(description = "ID de la ciudad", example = "1")
    private Long id;

    @Schema(description = "Código de la ciudad", example = "001")
    private String codigo;

    @Schema(description = "Nombre de la ciudad", example = "ASUNCIÓN")
    private String nombre;

    @Schema(description = "Código del distrito", example = "001")
    private String distritoCodigo;

    @Schema(description = "Nombre del distrito", example = "ASUNCIÓN")
    private String distritoNombre;

    @Schema(description = "Código del departamento", example = "11")
    private String departamentoCodigo;

    @Schema(description = "Nombre del departamento", example = "CENTRAL")
    private String departamentoNombre;

    @Schema(description = "Estado activo", example = "true")
    private Boolean activo;

    // Constructors
    public CiudadDto() {}

    public CiudadDto(Long id, String codigo, String nombre, String distritoCodigo, String distritoNombre, 
                     String departamentoCodigo, String departamentoNombre, Boolean activo) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.distritoCodigo = distritoCodigo;
        this.distritoNombre = distritoNombre;
        this.departamentoCodigo = departamentoCodigo;
        this.departamentoNombre = departamentoNombre;
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

    public String getDistritoCodigo() {
        return distritoCodigo;
    }

    public void setDistritoCodigo(String distritoCodigo) {
        this.distritoCodigo = distritoCodigo;
    }

    public String getDistritoNombre() {
        return distritoNombre;
    }

    public void setDistritoNombre(String distritoNombre) {
        this.distritoNombre = distritoNombre;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public String getDepartamentoCodigo() {
        return departamentoCodigo;
    }

    public void setDepartamentoCodigo(String departamentoCodigo) {
        this.departamentoCodigo = departamentoCodigo;
    }

    public String getDepartamentoNombre() {
        return departamentoNombre;
    }

    public void setDepartamentoNombre(String departamentoNombre) {
        this.departamentoNombre = departamentoNombre;
    }
}