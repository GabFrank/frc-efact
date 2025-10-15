package com.frcefact.dto;

/**
 * DTO para métricas generales del dashboard.
 */
public class DashboardGeneralDto {
    private Long totalEmpresas;
    private Long totalUsuarios;
    private Long totalDocumentos;
    private Long actividadHoy;

    public DashboardGeneralDto() {
    }

    public DashboardGeneralDto(Long totalEmpresas, Long totalUsuarios, Long totalDocumentos, Long actividadHoy) {
        this.totalEmpresas = totalEmpresas;
        this.totalUsuarios = totalUsuarios;
        this.totalDocumentos = totalDocumentos;
        this.actividadHoy = actividadHoy;
    }

    public Long getTotalEmpresas() {
        return totalEmpresas;
    }

    public void setTotalEmpresas(Long totalEmpresas) {
        this.totalEmpresas = totalEmpresas;
    }

    public Long getTotalUsuarios() {
        return totalUsuarios;
    }

    public void setTotalUsuarios(Long totalUsuarios) {
        this.totalUsuarios = totalUsuarios;
    }

    public Long getTotalDocumentos() {
        return totalDocumentos;
    }

    public void setTotalDocumentos(Long totalDocumentos) {
        this.totalDocumentos = totalDocumentos;
    }

    public Long getActividadHoy() {
        return actividadHoy;
    }

    public void setActividadHoy(Long actividadHoy) {
        this.actividadHoy = actividadHoy;
    }
}
