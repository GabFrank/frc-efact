package com.frcefact.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para el dashboard de empresa.
 * Contiene métricas de facturación y análisis comercial.
 */
public class DashboardEmpresaDto {

    private Long empresaId;
    private String razonSocial;
    private Long totalFacturasEmitidas;
    private BigDecimal totalGuaraniesMesActual;
    private TotalesPorIvaDto totalesPorIva;
    private List<ClienteRankingDto> top10Clientes;

    public DashboardEmpresaDto() {
    }

    public DashboardEmpresaDto(Long empresaId, String razonSocial, Long totalFacturasEmitidas,
                               BigDecimal totalGuaraniesMesActual, TotalesPorIvaDto totalesPorIva,
                               List<ClienteRankingDto> top10Clientes) {
        this.empresaId = empresaId;
        this.razonSocial = razonSocial;
        this.totalFacturasEmitidas = totalFacturasEmitidas;
        this.totalGuaraniesMesActual = totalGuaraniesMesActual;
        this.totalesPorIva = totalesPorIva;
        this.top10Clientes = top10Clientes;
    }

    // Getters and Setters

    public Long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public Long getTotalFacturasEmitidas() {
        return totalFacturasEmitidas;
    }

    public void setTotalFacturasEmitidas(Long totalFacturasEmitidas) {
        this.totalFacturasEmitidas = totalFacturasEmitidas;
    }

    public BigDecimal getTotalGuaraniesMesActual() {
        return totalGuaraniesMesActual;
    }

    public void setTotalGuaraniesMesActual(BigDecimal totalGuaraniesMesActual) {
        this.totalGuaraniesMesActual = totalGuaraniesMesActual;
    }

    public TotalesPorIvaDto getTotalesPorIva() {
        return totalesPorIva;
    }

    public void setTotalesPorIva(TotalesPorIvaDto totalesPorIva) {
        this.totalesPorIva = totalesPorIva;
    }

    public List<ClienteRankingDto> getTop10Clientes() {
        return top10Clientes;
    }

    public void setTop10Clientes(List<ClienteRankingDto> top10Clientes) {
        this.top10Clientes = top10Clientes;
    }
}
