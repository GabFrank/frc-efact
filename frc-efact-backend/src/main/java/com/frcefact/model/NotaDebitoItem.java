package com.frcefact.model;

import com.frcefact.model.base.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Entity
@Table(name = "nota_debito_item", schema = "financiero",
    indexes = {
        @Index(name = "idx_ndi_nota_debito", columnList = "nota_debito_id")
    }
)
public class NotaDebitoItem extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nota_debito_id", nullable = false)
    private NotaDebito notaDebito;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @Size(max = 50)
    @Column(length = 50)
    private String codigo;

    @NotNull(message = "La descripción es requerida")
    @Size(max = 255)
    @Column(length = 255, nullable = false)
    private String descripcion;

    @NotNull(message = "La cantidad debe ser mayor a 0")
    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal cantidad;

    @NotNull(message = "El precio unitario es requerido")
    @Column(name = "precio_unitario", nullable = false, precision = 15, scale = 2)
    private BigDecimal precioUnitario;

    @NotNull(message = "El total es requerido")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal total;

    @Column(nullable = false)
    private Integer iva = 10; // 0, 5, 10

    public NotaDebitoItem() {
    }

    public void calcularTotal() {
        if (cantidad != null && precioUnitario != null) {
            this.total = cantidad.multiply(precioUnitario).setScale(2, java.math.RoundingMode.HALF_UP);
        }
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public NotaDebito getNotaDebito() { return notaDebito; }
    public void setNotaDebito(NotaDebito notaDebito) { this.notaDebito = notaDebito; }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public Integer getIva() { return iva; }
    public void setIva(Integer iva) { this.iva = iva; }
}

