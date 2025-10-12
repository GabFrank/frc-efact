package com.frcefact.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO para filtros de búsqueda de facturas.
 */
public class FacturaFiltroDto {

    @NotNull(message = "Empresa ID es requerido")
    private Long empresaId;

    private Long clienteId;

    private String fechaDesde;

    private String fechaHasta;

    private Boolean credito;

    private Integer page = 0;

    private Integer size = 20;

    private String sortBy = "fecha";

    private String sortDirection = "DESC";

    // Constructores
    public FacturaFiltroDto() {
    }

    public FacturaFiltroDto(Long empresaId, Long clienteId, String fechaDesde, String fechaHasta) {
        this.empresaId = empresaId;
        this.clienteId = clienteId;
        this.fechaDesde = fechaDesde;
        this.fechaHasta = fechaHasta;
    }

    // Getters y Setters
    public Long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public String getFechaDesde() {
        return fechaDesde;
    }

    public void setFechaDesde(String fechaDesde) {
        this.fechaDesde = fechaDesde;
    }

    public String getFechaHasta() {
        return fechaHasta;
    }

    public void setFechaHasta(String fechaHasta) {
        this.fechaHasta = fechaHasta;
    }

    public Boolean getCredito() {
        return credito;
    }

    public void setCredito(Boolean credito) {
        this.credito = credito;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }

    @Override
    public String toString() {
        return "FacturaFiltroDto{" +
                "empresaId=" + empresaId +
                ", clienteId=" + clienteId +
                ", fechaDesde='" + fechaDesde + '\'' +
                ", fechaHasta='" + fechaHasta + '\'' +
                ", credito=" + credito +
                ", page=" + page +
                ", size=" + size +
                ", sortBy='" + sortBy + '\'' +
                ", sortDirection='" + sortDirection + '\'' +
                '}';
    }
}
