package com.frcefact.dto;

import java.math.BigDecimal;

/**
 * DTO para reportes de productos.
 * Contiene información de productos con cantidad vendida y monto total.
 */
public class ProductoReporteDto {

    private Long productoId;
    private String codigo;
    private String descripcion;
    private BigDecimal precioUnitario;
    private Integer iva;
    private BigDecimal cantidadVendida;
    private BigDecimal montoTotal;

    public ProductoReporteDto() {
    }

    public ProductoReporteDto(Long productoId, String codigo, String descripcion, BigDecimal precioUnitario,
                              Integer iva, BigDecimal cantidadVendida, BigDecimal montoTotal) {
        this.productoId = productoId;
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.precioUnitario = precioUnitario;
        this.iva = iva;
        this.cantidadVendida = cantidadVendida;
        this.montoTotal = montoTotal;
    }

    // Getters and Setters

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public Integer getIva() {
        return iva;
    }

    public void setIva(Integer iva) {
        this.iva = iva;
    }

    public BigDecimal getCantidadVendida() {
        return cantidadVendida;
    }

    public void setCantidadVendida(BigDecimal cantidadVendida) {
        this.cantidadVendida = cantidadVendida;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }
}
