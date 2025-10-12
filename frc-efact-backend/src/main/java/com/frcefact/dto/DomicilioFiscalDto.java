package com.frcefact.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para datos de domicilio fiscal.
 */
public class DomicilioFiscalDto {

    @NotBlank(message = "Departamento es requerido")
    @Size(max = 100, message = "Departamento no debe exceder 100 caracteres")
    private String departamento;

    @NotBlank(message = "Ciudad es requerida")
    @Size(max = 100, message = "Ciudad no debe exceder 100 caracteres")
    private String ciudad;

    @Size(max = 10, message = "Código de ciudad no debe exceder 10 caracteres")
    private String codigoCiudad;

    @Size(max = 100, message = "Localidad no debe exceder 100 caracteres")
    private String localidad;

    @Size(max = 100, message = "Barrio no debe exceder 100 caracteres")
    private String barrio;

    @NotBlank(message = "Dirección es requerida")
    private String direccion;

    // Constructores
    public DomicilioFiscalDto() {
    }

    public DomicilioFiscalDto(String departamento, String ciudad, String codigoCiudad, 
                             String localidad, String barrio, String direccion) {
        this.departamento = departamento;
        this.ciudad = ciudad;
        this.codigoCiudad = codigoCiudad;
        this.localidad = localidad;
        this.barrio = barrio;
        this.direccion = direccion;
    }

    // Getters y Setters
    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getCodigoCiudad() {
        return codigoCiudad;
    }

    public void setCodigoCiudad(String codigoCiudad) {
        this.codigoCiudad = codigoCiudad;
    }

    public String getLocalidad() {
        return localidad;
    }

    public void setLocalidad(String localidad) {
        this.localidad = localidad;
    }

    public String getBarrio() {
        return barrio;
    }

    public void setBarrio(String barrio) {
        this.barrio = barrio;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
}
