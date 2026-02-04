package com.frcefact.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class NotaCreditoItemDto {

    private Long id;

    private Long productoId;

    @NotNull(message = "Cantidad es requerida")
    @DecimalMin(value = "0.001", message = "Cantidad debe ser mayor a 0")
    private BigDecimal cantidad;

    @NotBlank(message = "Descripción es requerida")
    @Size(max = 255, message = "Descripción no debe exceder 255 caracteres")
    private String descripcion;

    @NotNull(message = "Precio unitario es requerido")
    @DecimalMin(value = "0.01", message = "Precio unitario debe ser mayor a 0")
    private BigDecimal precioUnitario;

    private BigDecimal descuento = BigDecimal.ZERO;

    private BigDecimal total;

    @NotNull(message = "IVA es requerido")
    private Integer iva; // 0, 5, 10

    private String codigo;

    // Constructores
    public NotaCreditoItemDto() {
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }

    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    public BigDecimal getDescuento() { return descuento; }
    public void setDescuento(BigDecimal descuento) { this.descuento = descuento; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public Integer getIva() { return iva; }
    public void setIva(Integer iva) { this.iva = iva; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
}

