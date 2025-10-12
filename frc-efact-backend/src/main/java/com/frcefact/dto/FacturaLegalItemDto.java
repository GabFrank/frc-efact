package com.frcefact.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * DTO para transferencia de datos de FacturaLegalItem.
 */
public class FacturaLegalItemDto {

    private Long id;

    private Long productoId;

    @NotNull(message = "Cantidad es requerida")
    @DecimalMin(value = "0.001", message = "Cantidad debe ser mayor a 0")
    @Digits(integer = 7, fraction = 3, message = "Cantidad debe tener máximo 7 dígitos enteros y 3 decimales")
    private BigDecimal cantidad;

    @NotBlank(message = "Descripción es requerida")
    @Size(max = 500, message = "Descripción no debe exceder 500 caracteres")
    private String descripcion;

    @NotNull(message = "Precio unitario es requerido")
    @DecimalMin(value = "0.01", message = "Precio unitario debe ser mayor a 0")
    @Digits(integer = 13, fraction = 2, message = "Precio unitario debe tener máximo 13 dígitos enteros y 2 decimales")
    private BigDecimal precioUnitario;

    private BigDecimal total;

    // Constructores
    public FacturaLegalItemDto() {
    }

    public FacturaLegalItemDto(Long id, Long productoId, BigDecimal cantidad, 
                              String descripcion, BigDecimal precioUnitario, BigDecimal total) {
        this.id = id;
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.descripcion = descripcion;
        this.precioUnitario = precioUnitario;
        this.total = total;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
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

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    @Override
    public String toString() {
        return "FacturaLegalItemDto{" +
                "id=" + id +
                ", productoId=" + productoId +
                ", cantidad=" + cantidad +
                ", descripcion='" + descripcion + '\'' +
                ", precioUnitario=" + precioUnitario +
                ", total=" + total +
                '}';
    }
}
