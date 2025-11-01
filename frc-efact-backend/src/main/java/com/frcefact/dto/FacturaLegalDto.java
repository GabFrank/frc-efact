package com.frcefact.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO para transferencia de datos de FacturaLegal.
 */
public class FacturaLegalDto {

    private Long id;

    @NotNull(message = "Empresa ID es requerido")
    private Long empresaId;

    @NotNull(message = "Timbrado detalle ID es requerido")
    private Long timbradoDetalleId;

    private Long clienteId;

    private Integer numeroFactura;

    private String fecha;

    private Boolean credito = false;

    // Datos del cliente (snapshot)
    @Size(max = 200, message = "Nombre no debe exceder 200 caracteres")
    private String nombre;

    @Size(max = 20, message = "RUC no debe exceder 20 caracteres")
    private String ruc;

    private String direccion;

    // Items de la factura
    @NotNull(message = "Items son requeridos")
    @NotEmpty(message = "La factura debe tener al menos un item")
    @Valid
    private List<FacturaLegalItemDto> items = new ArrayList<>();

    // Totales por tasa de IVA (calculados automáticamente)
    private BigDecimal ivaParcial0;
    private BigDecimal ivaParcial5;
    private BigDecimal ivaParcial10;
    private BigDecimal totalParcial0;
    private BigDecimal totalParcial5;
    private BigDecimal totalParcial10;

    @DecimalMin(value = "0.0", message = "Descuento final no puede ser negativo")
    @Digits(integer = 13, fraction = 2, message = "Descuento final debe tener máximo 13 dígitos enteros y 2 decimales")
    private BigDecimal descuentoFinal = BigDecimal.ZERO;

    private BigDecimal totalParcial;
    private BigDecimal totalFinal;

    private Boolean activo = true;

    private String creadoEn;
    private String creadoPor;
    private String actualizadoEn;
    private String actualizadoPor;

    // Campos adicionales para respuesta
    private String numeroFacturaFormateado;
    private String nombreEmpresa;
    private String nombreCliente;

    // Constructores
    public FacturaLegalDto() {
    }

    public FacturaLegalDto(Long id, Long empresaId, Long timbradoDetalleId, Long clienteId,
                          Integer numeroFactura, String fecha, Boolean credito,
                          String nombre, String ruc, String direccion,
                          BigDecimal descuentoFinal, BigDecimal totalParcial, BigDecimal totalFinal) {
        this.id = id;
        this.empresaId = empresaId;
        this.timbradoDetalleId = timbradoDetalleId;
        this.clienteId = clienteId;
        this.numeroFactura = numeroFactura;
        this.fecha = fecha;
        this.credito = credito;
        this.nombre = nombre;
        this.ruc = ruc;
        this.direccion = direccion;
        this.descuentoFinal = descuentoFinal;
        this.totalParcial = totalParcial;
        this.totalFinal = totalFinal;
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

    public Long getTimbradoDetalleId() {
        return timbradoDetalleId;
    }

    public void setTimbradoDetalleId(Long timbradoDetalleId) {
        this.timbradoDetalleId = timbradoDetalleId;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public Integer getNumeroFactura() {
        return numeroFactura;
    }

    public void setNumeroFactura(Integer numeroFactura) {
        this.numeroFactura = numeroFactura;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public Boolean getCredito() {
        return credito;
    }

    public void setCredito(Boolean credito) {
        this.credito = credito;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public List<FacturaLegalItemDto> getItems() {
        return items;
    }

    public void setItems(List<FacturaLegalItemDto> items) {
        this.items = items;
    }

    public BigDecimal getIvaParcial0() {
        return ivaParcial0;
    }

    public void setIvaParcial0(BigDecimal ivaParcial0) {
        this.ivaParcial0 = ivaParcial0;
    }

    public BigDecimal getIvaParcial5() {
        return ivaParcial5;
    }

    public void setIvaParcial5(BigDecimal ivaParcial5) {
        this.ivaParcial5 = ivaParcial5;
    }

    public BigDecimal getIvaParcial10() {
        return ivaParcial10;
    }

    public void setIvaParcial10(BigDecimal ivaParcial10) {
        this.ivaParcial10 = ivaParcial10;
    }

    public BigDecimal getTotalParcial0() {
        return totalParcial0;
    }

    public void setTotalParcial0(BigDecimal totalParcial0) {
        this.totalParcial0 = totalParcial0;
    }

    public BigDecimal getTotalParcial5() {
        return totalParcial5;
    }

    public void setTotalParcial5(BigDecimal totalParcial5) {
        this.totalParcial5 = totalParcial5;
    }

    public BigDecimal getTotalParcial10() {
        return totalParcial10;
    }

    public void setTotalParcial10(BigDecimal totalParcial10) {
        this.totalParcial10 = totalParcial10;
    }

    public BigDecimal getDescuentoFinal() {
        return descuentoFinal;
    }

    public void setDescuentoFinal(BigDecimal descuentoFinal) {
        this.descuentoFinal = descuentoFinal;
    }

    public BigDecimal getTotalParcial() {
        return totalParcial;
    }

    public void setTotalParcial(BigDecimal totalParcial) {
        this.totalParcial = totalParcial;
    }

    public BigDecimal getTotalFinal() {
        return totalFinal;
    }

    public void setTotalFinal(BigDecimal totalFinal) {
        this.totalFinal = totalFinal;
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

    public String getNumeroFacturaFormateado() {
        return numeroFacturaFormateado;
    }

    public void setNumeroFacturaFormateado(String numeroFacturaFormateado) {
        this.numeroFacturaFormateado = numeroFacturaFormateado;
    }

    public String getNombreEmpresa() {
        return nombreEmpresa;
    }

    public void setNombreEmpresa(String nombreEmpresa) {
        this.nombreEmpresa = nombreEmpresa;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    @Override
    public String toString() {
        return "FacturaLegalDto{" +
                "id=" + id +
                ", empresaId=" + empresaId +
                ", timbradoDetalleId=" + timbradoDetalleId +
                ", clienteId=" + clienteId +
                ", numeroFactura=" + numeroFactura +
                ", fecha='" + fecha + '\'' +
                ", credito=" + credito +
                ", nombre='" + nombre + '\'' +
                ", totalFinal=" + totalFinal +
                ", activo=" + activo +
                '}';
    }
}
