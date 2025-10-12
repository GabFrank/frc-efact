package com.frcefact.dto;

import com.frcefact.validation.ValidRangoTimbrado;
import jakarta.validation.constraints.*;

/**
 * DTO para TimbradoDetalle con validaciones.
 */
@ValidRangoTimbrado
public class TimbradoDetalleDto {

    private Long id;

    @NotNull(message = "ID de timbrado es requerido")
    private Long timbradoId;

    @NotBlank(message = "Punto de expedición es requerido")
    @Size(max = 10, message = "Punto de expedición no debe exceder 10 caracteres")
    @Pattern(regexp = "^[0-9]{3}$", message = "Punto de expedición debe ser un número de 3 dígitos")
    private String puntoExpedicion;

    @NotBlank(message = "Código de establecimiento es requerido")
    @Size(max = 10, message = "Código de establecimiento no debe exceder 10 caracteres")
    @Pattern(regexp = "^[0-9]{3}$", message = "Código de establecimiento debe ser un número de 3 dígitos")
    private String codigoEstablecimientoFactura;

    @NotNull(message = "Rango desde es requerido")
    @Min(value = 1, message = "Rango desde debe ser mayor a 0")
    private Long rangoDesde;

    @NotNull(message = "Rango hasta es requerido")
    @Min(value = 1, message = "Rango hasta debe ser mayor a 0")
    private Long rangoHasta;

    private Long cantidad;

    private Long numeroActual;

    @Size(max = 100, message = "Departamento no debe exceder 100 caracteres")
    private String departamento;

    @Size(max = 100, message = "Ciudad no debe exceder 100 caracteres")
    private String ciudad;

    @Size(max = 10, message = "Código de ciudad no debe exceder 10 caracteres")
    private String codigoCiudad;

    @Size(max = 100, message = "Localidad no debe exceder 100 caracteres")
    private String localidad;

    @Size(max = 100, message = "Barrio no debe exceder 100 caracteres")
    private String barrio;

    private String direccion;

    @Size(max = 50, message = "Teléfono no debe exceder 50 caracteres")
    private String telefono;

    private Boolean activo;

    private Long numerosDisponibles;

    private Double porcentajeUtilizado;

    // Constructores
    public TimbradoDetalleDto() {
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTimbradoId() {
        return timbradoId;
    }

    public void setTimbradoId(Long timbradoId) {
        this.timbradoId = timbradoId;
    }

    public String getPuntoExpedicion() {
        return puntoExpedicion;
    }

    public void setPuntoExpedicion(String puntoExpedicion) {
        this.puntoExpedicion = puntoExpedicion;
    }

    public String getCodigoEstablecimientoFactura() {
        return codigoEstablecimientoFactura;
    }

    public void setCodigoEstablecimientoFactura(String codigoEstablecimientoFactura) {
        this.codigoEstablecimientoFactura = codigoEstablecimientoFactura;
    }

    public Long getRangoDesde() {
        return rangoDesde;
    }

    public void setRangoDesde(Long rangoDesde) {
        this.rangoDesde = rangoDesde;
    }

    public Long getRangoHasta() {
        return rangoHasta;
    }

    public void setRangoHasta(Long rangoHasta) {
        this.rangoHasta = rangoHasta;
    }

    public Long getCantidad() {
        return cantidad;
    }

    public void setCantidad(Long cantidad) {
        this.cantidad = cantidad;
    }

    public Long getNumeroActual() {
        return numeroActual;
    }

    public void setNumeroActual(Long numeroActual) {
        this.numeroActual = numeroActual;
    }

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

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Long getNumerosDisponibles() {
        return numerosDisponibles;
    }

    public void setNumerosDisponibles(Long numerosDisponibles) {
        this.numerosDisponibles = numerosDisponibles;
    }

    public Double getPorcentajeUtilizado() {
        return porcentajeUtilizado;
    }

    public void setPorcentajeUtilizado(Double porcentajeUtilizado) {
        this.porcentajeUtilizado = porcentajeUtilizado;
    }
}
