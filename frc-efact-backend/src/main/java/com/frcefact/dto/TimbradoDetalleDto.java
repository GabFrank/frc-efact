package com.frcefact.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para TimbradoDetalle (puntos de expedición).
 * Representa un punto de expedición específico dentro de un timbrado.
 */
public class TimbradoDetalleDto {

    private Long id;

    @NotNull(message = "ID de timbrado es requerido")
    private Long timbradoId;

    @NotBlank(message = "Punto de expedición es requerido")
    @Size(max = 10, message = "Punto de expedición no debe exceder 10 caracteres")
    private String puntoExpedicion;

    @NotBlank(message = "Código de establecimiento es requerido")
    @Size(max = 10, message = "Código de establecimiento no debe exceder 10 caracteres")
    private String codigoEstablecimientoFactura;

    // Campos de rango (opcionales para timbrados electrónicos)
    private Long cantidad;
    private Long rangoDesde;
    private Long rangoHasta;
    private Long numeroActual;

    // Ubicación del punto de expedición
    @NotNull(message = "Ciudad es requerida")
    private Long ciudadId;

    private Long barrioId;

    private String direccion;

    @Size(max = 50)
    private String telefono;

    private Boolean activo;

    // Campos calculados (solo lectura)
    private Long numerosDisponibles;
    private Double porcentajeUtilizado;
    
    // Información del timbrado para mostrar (solo lectura)
    private String timbradoNumero;
    private Boolean timbradoIsElectronico;

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

    public Long getCantidad() {
        return cantidad;
    }

    public void setCantidad(Long cantidad) {
        this.cantidad = cantidad;
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

    public Long getNumeroActual() {
        return numeroActual;
    }

    public void setNumeroActual(Long numeroActual) {
        this.numeroActual = numeroActual;
    }

    public Long getCiudadId() {
        return ciudadId;
    }

    public void setCiudadId(Long ciudadId) {
        this.ciudadId = ciudadId;
    }

    public Long getBarrioId() {
        return barrioId;
    }

    public void setBarrioId(Long barrioId) {
        this.barrioId = barrioId;
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

    // Campos calculados (solo lectura)
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

    public String getTimbradoNumero() {
        return timbradoNumero;
    }

    public void setTimbradoNumero(String timbradoNumero) {
        this.timbradoNumero = timbradoNumero;
    }

    public Boolean getTimbradoIsElectronico() {
        return timbradoIsElectronico;
    }

    public void setTimbradoIsElectronico(Boolean timbradoIsElectronico) {
        this.timbradoIsElectronico = timbradoIsElectronico;
    }
}