package com.frcefact.model;

import com.frcefact.model.base.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entidad Producto que representa un producto o servicio de una empresa.
 * Mapea a la tabla productos.producto.
 */
@Entity
@Table(name = "producto", schema = "productos", 
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_producto_empresa_codigo", columnNames = {"empresa_id", "codigo"})
    },
    indexes = {
        @Index(name = "idx_producto_empresa", columnList = "empresa_id"),
        @Index(name = "idx_producto_descripcion", columnList = "descripcion"),
        @Index(name = "idx_producto_activo", columnList = "activo")
    }
)
public class Producto extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Empresa es requerida")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Size(max = 50, message = "Código no debe exceder 50 caracteres")
    @Column(length = 50)
    private String codigo;

    @NotBlank(message = "Descripción es requerida")
    @Size(max = 500, message = "Descripción no debe exceder 500 caracteres")
    @Column(nullable = false, length = 500)
    private String descripcion;

    @NotNull(message = "Precio es requerido")
    @DecimalMin(value = "0.0", inclusive = false, message = "Precio debe ser mayor a 0")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal precio;

    @NotNull(message = "IVA es requerido")
    @Column(nullable = false)
    private Integer iva;

    @Column(nullable = false)
    private Boolean balanza = false;

    @Column(nullable = false)
    private Boolean activo = true;

    // Relaciones
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL)
    private Set<FacturaLegalItem> facturaItems = new HashSet<>();

    // Constructores
    public Producto() {
    }

    public Producto(Empresa empresa, String descripcion, BigDecimal precio, Integer iva) {
        this.empresa = empresa;
        this.descripcion = descripcion;
        this.precio = precio;
        this.iva = iva;
        this.balanza = false;
        this.activo = true;
    }

    // Métodos de negocio
    public boolean isIvaValido() {
        return iva != null && (iva == 0 || iva == 5 || iva == 10);
    }

    public BigDecimal calcularPrecioConIva() {
        if (precio == null || iva == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal tasaIva = BigDecimal.valueOf(iva).divide(BigDecimal.valueOf(100));
        return precio.add(precio.multiply(tasaIva));
    }

    public BigDecimal calcularMontoIva(BigDecimal cantidad) {
        if (precio == null || iva == null || cantidad == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal subtotal = precio.multiply(cantidad);
        BigDecimal tasaIva = BigDecimal.valueOf(iva).divide(BigDecimal.valueOf(100));
        return subtotal.multiply(tasaIva);
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
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

    public Set<FacturaLegalItem> getFacturaItems() {
        return facturaItems;
    }

    public void setFacturaItems(Set<FacturaLegalItem> facturaItems) {
        this.facturaItems = facturaItems;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Producto producto = (Producto) o;
        return Objects.equals(id, producto.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Producto{" +
                "id=" + id +
                ", codigo='" + codigo + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", precio=" + precio +
                ", iva=" + iva +
                ", activo=" + activo +
                '}';
    }
}
