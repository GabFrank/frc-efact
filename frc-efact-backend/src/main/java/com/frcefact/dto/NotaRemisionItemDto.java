package com.frcefact.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class NotaRemisionItemDto {

    private Long id;

    private Long productoId;

    @NotNull(message = "Cantidad es requerida")
    @DecimalMin(value = "0.001", message = "Cantidad debe ser mayor a 0")
    private BigDecimal cantidad;

    @NotBlank(message = "Descripción es requerida")
    @Size(max = 255, message = "Descripción no debe exceder 255 caracteres")
    private String descripcion;

    @Size(max = 10, message = "Unidad de medida no debe exceder 10 caracteres")
    private String unidadMedida;

    private String codigo;

    public NotaRemisionItemDto() {
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

    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
}

