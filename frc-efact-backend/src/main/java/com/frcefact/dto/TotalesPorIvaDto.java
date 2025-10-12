package com.frcefact.dto;

import java.math.BigDecimal;

/**
 * DTO para representar totales desglosados por tasa de IVA.
 */
public class TotalesPorIvaDto {

    private BigDecimal totalIva10;
    private BigDecimal totalIva5;
    private BigDecimal totalIva0;
    private BigDecimal totalGeneral;

    public TotalesPorIvaDto() {
    }

    public TotalesPorIvaDto(BigDecimal totalIva10, BigDecimal totalIva5, BigDecimal totalIva0) {
        this.totalIva10 = totalIva10 != null ? totalIva10 : BigDecimal.ZERO;
        this.totalIva5 = totalIva5 != null ? totalIva5 : BigDecimal.ZERO;
        this.totalIva0 = totalIva0 != null ? totalIva0 : BigDecimal.ZERO;
        this.totalGeneral = this.totalIva10.add(this.totalIva5).add(this.totalIva0);
    }

    // Getters and Setters

    public BigDecimal getTotalIva10() {
        return totalIva10;
    }

    public void setTotalIva10(BigDecimal totalIva10) {
        this.totalIva10 = totalIva10;
        recalcularTotalGeneral();
    }

    public BigDecimal getTotalIva5() {
        return totalIva5;
    }

    public void setTotalIva5(BigDecimal totalIva5) {
        this.totalIva5 = totalIva5;
        recalcularTotalGeneral();
    }

    public BigDecimal getTotalIva0() {
        return totalIva0;
    }

    public void setTotalIva0(BigDecimal totalIva0) {
        this.totalIva0 = totalIva0;
        recalcularTotalGeneral();
    }

    public BigDecimal getTotalGeneral() {
        return totalGeneral;
    }

    private void recalcularTotalGeneral() {
        BigDecimal iva10 = this.totalIva10 != null ? this.totalIva10 : BigDecimal.ZERO;
        BigDecimal iva5 = this.totalIva5 != null ? this.totalIva5 : BigDecimal.ZERO;
        BigDecimal iva0 = this.totalIva0 != null ? this.totalIva0 : BigDecimal.ZERO;
        this.totalGeneral = iva10.add(iva5).add(iva0);
    }
}
