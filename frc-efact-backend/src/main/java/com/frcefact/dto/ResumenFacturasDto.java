package com.frcefact.dto;

import java.math.BigDecimal;

/**
 * DTO para el resumen de facturas aprobadas y no aprobadas.
 */
public class ResumenFacturasDto {
    
    private long cantidadFacturasAprobadas;
    private BigDecimal totalFacturasAprobadas;
    private BigDecimal totalIva10Aprobadas;
    private BigDecimal totalIva5Aprobadas;
    private BigDecimal totalExentasAprobadas;
    
    private long cantidadFacturasNoAprobadas;
    private BigDecimal totalFacturasNoAprobadas;
    
    public ResumenFacturasDto() {
    }
    
    public ResumenFacturasDto(long cantidadFacturasAprobadas, 
                             BigDecimal totalFacturasAprobadas,
                             BigDecimal totalIva10Aprobadas,
                             BigDecimal totalIva5Aprobadas,
                             BigDecimal totalExentasAprobadas,
                             long cantidadFacturasNoAprobadas,
                             BigDecimal totalFacturasNoAprobadas) {
        this.cantidadFacturasAprobadas = cantidadFacturasAprobadas;
        this.totalFacturasAprobadas = totalFacturasAprobadas;
        this.totalIva10Aprobadas = totalIva10Aprobadas;
        this.totalIva5Aprobadas = totalIva5Aprobadas;
        this.totalExentasAprobadas = totalExentasAprobadas;
        this.cantidadFacturasNoAprobadas = cantidadFacturasNoAprobadas;
        this.totalFacturasNoAprobadas = totalFacturasNoAprobadas;
    }
    
    // Getters y Setters
    public long getCantidadFacturasAprobadas() {
        return cantidadFacturasAprobadas;
    }
    
    public void setCantidadFacturasAprobadas(long cantidadFacturasAprobadas) {
        this.cantidadFacturasAprobadas = cantidadFacturasAprobadas;
    }
    
    public BigDecimal getTotalFacturasAprobadas() {
        return totalFacturasAprobadas;
    }
    
    public void setTotalFacturasAprobadas(BigDecimal totalFacturasAprobadas) {
        this.totalFacturasAprobadas = totalFacturasAprobadas;
    }
    
    public BigDecimal getTotalIva10Aprobadas() {
        return totalIva10Aprobadas;
    }
    
    public void setTotalIva10Aprobadas(BigDecimal totalIva10Aprobadas) {
        this.totalIva10Aprobadas = totalIva10Aprobadas;
    }
    
    public BigDecimal getTotalIva5Aprobadas() {
        return totalIva5Aprobadas;
    }
    
    public void setTotalIva5Aprobadas(BigDecimal totalIva5Aprobadas) {
        this.totalIva5Aprobadas = totalIva5Aprobadas;
    }
    
    public BigDecimal getTotalExentasAprobadas() {
        return totalExentasAprobadas;
    }
    
    public void setTotalExentasAprobadas(BigDecimal totalExentasAprobadas) {
        this.totalExentasAprobadas = totalExentasAprobadas;
    }
    
    public long getCantidadFacturasNoAprobadas() {
        return cantidadFacturasNoAprobadas;
    }
    
    public void setCantidadFacturasNoAprobadas(long cantidadFacturasNoAprobadas) {
        this.cantidadFacturasNoAprobadas = cantidadFacturasNoAprobadas;
    }
    
    public BigDecimal getTotalFacturasNoAprobadas() {
        return totalFacturasNoAprobadas;
    }
    
    public void setTotalFacturasNoAprobadas(BigDecimal totalFacturasNoAprobadas) {
        this.totalFacturasNoAprobadas = totalFacturasNoAprobadas;
    }
}

