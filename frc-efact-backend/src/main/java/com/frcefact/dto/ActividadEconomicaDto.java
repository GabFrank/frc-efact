package com.frcefact.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * DTO para datos de actividad económica.
 */
public class ActividadEconomicaDto {

    @NotBlank(message = "Código de actividad económica principal es requerido")
    @Size(max = 20, message = "Código no debe exceder 20 caracteres")
    private String codigoPrincipal;

    @NotBlank(message = "Descripción de actividad económica principal es requerida")
    @Size(max = 200, message = "Descripción no debe exceder 200 caracteres")
    private String descripcionPrincipal;

    private List<String> codigosSecundarios;

    private List<String> descripcionesSecundarias;

    // Constructores
    public ActividadEconomicaDto() {
    }

    public ActividadEconomicaDto(String codigoPrincipal, String descripcionPrincipal,
                                List<String> codigosSecundarios, List<String> descripcionesSecundarias) {
        this.codigoPrincipal = codigoPrincipal;
        this.descripcionPrincipal = descripcionPrincipal;
        this.codigosSecundarios = codigosSecundarios;
        this.descripcionesSecundarias = descripcionesSecundarias;
    }

    // Getters y Setters
    public String getCodigoPrincipal() {
        return codigoPrincipal;
    }

    public void setCodigoPrincipal(String codigoPrincipal) {
        this.codigoPrincipal = codigoPrincipal;
    }

    public String getDescripcionPrincipal() {
        return descripcionPrincipal;
    }

    public void setDescripcionPrincipal(String descripcionPrincipal) {
        this.descripcionPrincipal = descripcionPrincipal;
    }

    public List<String> getCodigosSecundarios() {
        return codigosSecundarios;
    }

    public void setCodigosSecundarios(List<String> codigosSecundarios) {
        this.codigosSecundarios = codigosSecundarios;
    }

    public List<String> getDescripcionesSecundarias() {
        return descripcionesSecundarias;
    }

    public void setDescripcionesSecundarias(List<String> descripcionesSecundarias) {
        this.descripcionesSecundarias = descripcionesSecundarias;
    }
}
