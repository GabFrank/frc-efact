package com.frcefact.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para reportes de facturas.
 * Contiene información resumida de facturas para reportes.
 */
public class FacturaReporteDto {

    private Long id;
    private Integer numeroFactura;
    private LocalDateTime fecha;
    private String clienteNombre;
    private String clienteRuc;
    private BigDecimal totalFinal;
    private BigDecimal ivaParcial10;
    private BigDecimal ivaParcial5;
    private BigDecimal ivaParcial0;
    private Boolean credito;
    private String creadoPor;

    public FacturaReporteDto() {
    }

    public FacturaReporteDto(Long id, Integer numeroFactura, LocalDateTime fecha, String clienteNombre,
                             String clienteRuc, BigDecimal totalFinal, BigDecimal ivaParcial10,
                             BigDecimal ivaParcial5, BigDecimal ivaParcial0, Boolean credito, String creadoPor) {
        this.id = id;
        this.numeroFactura = numeroFactura;
        this.fecha = fecha;
        this.clienteNombre = clienteNombre;
        this.clienteRuc = clienteRuc;
        this.totalFinal = totalFinal;
        this.ivaParcial10 = ivaParcial10;
        this.ivaParcial5 = ivaParcial5;
        this.ivaParcial0 = ivaParcial0;
        this.credito = credito;
        this.creadoPor = creadoPor;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNumeroFactura() {
        return numeroFactura;
    }

    public void setNumeroFactura(Integer numeroFactura) {
        this.numeroFactura = numeroFactura;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public String getClienteRuc() {
        return clienteRuc;
    }

    public void setClienteRuc(String clienteRuc) {
        this.clienteRuc = clienteRuc;
    }

    public BigDecimal getTotalFinal() {
        return totalFinal;
    }

    public void setTotalFinal(BigDecimal totalFinal) {
        this.totalFinal = totalFinal;
    }

    public BigDecimal getIvaParcial10() {
        return ivaParcial10;
    }

    public void setIvaParcial10(BigDecimal ivaParcial10) {
        this.ivaParcial10 = ivaParcial10;
    }

    public BigDecimal getIvaParcial5() {
        return ivaParcial5;
    }

    public void setIvaParcial5(BigDecimal ivaParcial5) {
        this.ivaParcial5 = ivaParcial5;
    }

    public BigDecimal getIvaParcial0() {
        return ivaParcial0;
    }

    public void setIvaParcial0(BigDecimal ivaParcial0) {
        this.ivaParcial0 = ivaParcial0;
    }

    public Boolean getCredito() {
        return credito;
    }

    public void setCredito(Boolean credito) {
        this.credito = credito;
    }

    public String getCreadoPor() {
        return creadoPor;
    }

    public void setCreadoPor(String creadoPor) {
        this.creadoPor = creadoPor;
    }
}
