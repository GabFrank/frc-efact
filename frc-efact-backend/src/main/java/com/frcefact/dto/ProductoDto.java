package com.frcefact.dto;

import com.frcefact.validation.ValidIva;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * DTO para transferencia de datos de Producto.
 */
public class ProductoDto {

    private Long id;

    @NotNull(message = "Empresa ID es requerido")
    private Long empresaId;

    @Size(max = 50, message = "Código no debe exceder 50 caracteres")
    private String codigo;

    @NotBlank(message = "Descripción es requerida")
    @Size(max = 500, message = "Descripción no debe exceder 500 caracteres")
    private String descripcion;

    @NotNull(message = "Precio es requerido")
    @DecimalMin(value = "0.01", message = "Precio debe ser mayor a 0")
    @Digits(integer = 13, fraction = 2, message = "Precio debe tener máximo 13 dígitos enteros y 2 decimales")
    private BigDecimal precio;

    @NotNull(message = "IVA es requerido")
    @ValidIva
    private Integer iva;

    private Boolean balanza = false;

    private Boolean activo = true;

    private String creadoEn;
    private String creadoPor;
    private String actualizadoEn;
    private String actualizadoPor;

    // Constructores
    public ProductoDto() {
    }

    public ProductoDto(Long id, Long empresaId, String codigo, String descripcion, 
                      BigDecimal precio, Integer iva, Boolean balanza, Boolean activo) {
        this.id = id;
        this.empresaId = empresaId;
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.precio = precio;
        this.iva = iva;
        this.balanza = balanza;
        this.activo = activo;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
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

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Integer getIva() {
        return iva;
    }

    public void setIva(Integer iva) {
        this.iva = iva;
    }

    public Boolean getBalanza() {
        return balanza;
    }

    public void setBalanza(Boolean balanza) {
        this.balanza = balanza;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public String getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(String creadoEn) {
        this.creadoEn = creadoEn;
    }

    public String getCreadoPor() {
        return creadoPor;
    }

    public void setCreadoPor(String creadoPor) {
        this.creadoPor = creadoPor;
    }

    public String getActualizadoEn() {
        return actualizadoEn;
    }

    public void setActualizadoEn(String actualizadoEn) {
        this.actualizadoEn = actualizadoEn;
    }

    public String getActualizadoPor() {
        return actualizadoPor;
    }

    public void setActualizadoPor(String actualizadoPor) {
        this.actualizadoPor = actualizadoPor;
    }

    @Override
    public String toString() {
        return "ProductoDto{" +
                "id=" + id +
                ", empresaId=" + empresaId +
                ", codigo='" + codigo + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", precio=" + precio +
                ", iva=" + iva +
                ", balanza=" + balanza +
                ", activo=" + activo +
                '}';
    }
}
