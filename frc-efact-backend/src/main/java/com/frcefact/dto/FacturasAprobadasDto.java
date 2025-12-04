package com.frcefact.dto;

import java.math.BigDecimal;

/**
 * DTO para estadísticas de facturas aprobadas.
 */
public class FacturasAprobadasDto {
    private Long cantidad;
    private BigDecimal totalGs;
    private BigDecimal totalIva10;
    private BigDecimal totalIva5;
    private BigDecimal totalExentas;

    public FacturasAprobadasDto() {
    }

    public FacturasAprobadasDto(Long cantidad, BigDecimal totalGs, BigDecimal totalIva10, 
                                BigDecimal totalIva5, BigDecimal totalExentas) {
        this.cantidad = cantidad;
        this.totalGs = totalGs;
        this.totalIva10 = totalIva10;
        this.totalIva5 = totalIva5;
        this.totalExentas = totalExentas;
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

    public BigDecimal getTotalIva10() {
        return totalIva10;
    }

    public void setTotalIva10(BigDecimal totalIva10) {
        this.totalIva10 = totalIva10;
    }

    public BigDecimal getTotalIva5() {
        return totalIva5;
    }

    public void setTotalIva5(BigDecimal totalIva5) {
        this.totalIva5 = totalIva5;
    }

    public BigDecimal getTotalExentas() {
        return totalExentas;
    }

    public void setTotalExentas(BigDecimal totalExentas) {
        this.totalExentas = totalExentas;
    }
}

