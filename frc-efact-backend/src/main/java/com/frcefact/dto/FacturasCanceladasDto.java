package com.frcefact.dto;

import java.math.BigDecimal;

/**
 * DTO para estadísticas de facturas canceladas.
 */
public class FacturasCanceladasDto {
    private Long cantidad;
    private BigDecimal totalGs;

    public FacturasCanceladasDto() {
    }

    public FacturasCanceladasDto(Long cantidad, BigDecimal totalGs) {
        this.cantidad = cantidad;
        this.totalGs = totalGs;
    }

    // Getters and Setters

    public Long getCantidad() {
        return cantidad;
    }

    public void setCantidad(Long cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getTotalGs() {
        return totalGs;
    }

    public void setTotalGs(BigDecimal totalGs) {
        this.totalGs = totalGs;
    }
}

