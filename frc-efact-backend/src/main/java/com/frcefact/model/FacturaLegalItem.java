package com.frcefact.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad FacturaLegalItem que representa un item de una factura legal.
 * Mapea a la tabla financiero.factura_legal_item.
 */
@Entity
@Table(name = "factura_legal_item", schema = "financiero", indexes = {
    @Index(name = "idx_factura_item_factura", columnList = "factura_legal_id"),
    @Index(name = "idx_factura_item_producto", columnList = "producto_id")
})
public class FacturaLegalItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Factura legal es requerida")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_legal_id", nullable = false)
    private FacturaLegal facturaLegal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @NotNull(message = "Cantidad es requerida")
    @DecimalMin(value = "0.001", message = "Cantidad debe ser mayor a 0")
    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal cantidad;

    @NotBlank(message = "Descripción es requerida")
    @Size(max = 500, message = "Descripción no debe exceder 500 caracteres")
    @Column(nullable = false, length = 500)
    private String descripcion;

    @NotNull(message = "Precio unitario es requerido")
    @DecimalMin(value = "0.0", inclusive = false, message = "Precio unitario debe ser mayor a 0")
    @Column(name = "precio_unitario", nullable = false, precision = 15, scale = 2)
    private BigDecimal precioUnitario;

    @NotNull(message = "Total es requerido")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal total;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @Column(name = "creado_por", length = 50)
    private String creadoPor;

    // Constructores
    public FacturaLegalItem() {
    }

    public FacturaLegalItem(FacturaLegal facturaLegal, Producto producto, BigDecimal cantidad, BigDecimal precioUnitario) {
        this.facturaLegal = facturaLegal;
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.descripcion = producto != null ? producto.getDescripcion() : "";
        calcularTotal();
    }

    // Métodos de negocio
    public void calcularTotal() {
        if (cantidad != null && precioUnitario != null) {
            this.total = cantidad.multiply(precioUnitario);
        } else {
            this.total = BigDecimal.ZERO;
        }
    }

    public BigDecimal calcularSubtotal() {
        return cantidad.multiply(precioUnitario);
    }

    public BigDecimal calcularIva() {
        if (producto == null || producto.getIva() == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal subtotal = calcularSubtotal();
        BigDecimal tasaIva = BigDecimal.valueOf(producto.getIva()).divide(BigDecimal.valueOf(100));
        return subtotal.multiply(tasaIva);
    }

    public BigDecimal calcularTotalConIva() {
        return calcularSubtotal().add(calcularIva());
    }

    @PrePersist
    protected void onCreate() {
        creadoEn = LocalDateTime.now();
        calcularTotal();
    }

    @PreUpdate
    protected void onUpdate() {
        calcularTotal();
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public FacturaLegal getFacturaLegal() {
        return facturaLegal;
    }

    public void setFacturaLegal(FacturaLegal facturaLegal) {
        this.facturaLegal = facturaLegal;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
        if (producto != null && (descripcion == null || descripcion.isEmpty())) {
            this.descripcion = producto.getDescripcion();
        }
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
        calcularTotal();
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
        calcularTotal();
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }

    public String getCreadoPor() {
        return creadoPor;
    }

    public void setCreadoPor(String creadoPor) {
        this.creadoPor = creadoPor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FacturaLegalItem that = (FacturaLegalItem) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "FacturaLegalItem{" +
                "id=" + id +
                ", descripcion='" + descripcion + '\'' +
                ", cantidad=" + cantidad +
                ", precioUnitario=" + precioUnitario +
                ", total=" + total +
                '}';
    }
}
