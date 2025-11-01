package com.frcefact.dto;

import java.math.BigDecimal;

/**
 * DTO para representar el ranking de clientes por monto facturado.
 */
public class ClienteRankingDto {

    private Long clienteId;
    private String nombre;
    private String ruc;
    private Long cantidadFacturas;
    private BigDecimal montoTotal;

    public ClienteRankingDto() {
    }

    public ClienteRankingDto(Long clienteId, String nombre, String ruc, Long cantidadFacturas, BigDecimal montoTotal) {
        this.clienteId = clienteId;
        this.nombre = nombre;
        this.ruc = ruc;
        this.cantidadFacturas = cantidadFacturas;
        this.montoTotal = montoTotal;
    }

    // Getters and Setters

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public Long getCantidadFacturas() {
        return cantidadFacturas;
    }

    public void setCantidadFacturas(Long cantidadFacturas) {
        this.cantidadFacturas = cantidadFacturas;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }
}
