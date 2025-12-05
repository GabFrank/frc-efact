package com.frcefact.model;

import com.frcefact.model.base.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "nota_remision_item", schema = "financiero",
    indexes = {
        @Index(name = "idx_nri_nota_remision", columnList = "nota_remision_id")
    }
)
public class NotaRemisionItem extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nota_remision_id", nullable = false)
    private NotaRemision notaRemision;

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

    @Size(max = 10)
    @Column(name = "unidad_medida", length = 10)
    private String unidadMedida;

    public NotaRemisionItem() {
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public NotaRemision getNotaRemision() { return notaRemision; }
    public void setNotaRemision(NotaRemision notaRemision) { this.notaRemision = notaRemision; }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }

    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotaRemisionItem that = (NotaRemisionItem) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

