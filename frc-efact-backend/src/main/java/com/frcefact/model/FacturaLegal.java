package com.frcefact.model;

import com.frcefact.model.base.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entidad FacturaLegal que representa una factura legal emitida.
 * Mapea a la tabla financiero.factura_legal.
 */
@Entity
@Table(name = "factura_legal", schema = "financiero",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_factura_timbrado_numero", 
            columnNames = {"timbrado_detalle_id", "numero_factura"})
    },
    indexes = {
        @Index(name = "idx_factura_legal_empresa", columnList = "empresa_id"),
        @Index(name = "idx_factura_legal_cliente", columnList = "cliente_id"),
        @Index(name = "idx_factura_legal_fecha", columnList = "fecha"),
        @Index(name = "idx_factura_legal_activo", columnList = "activo")
    }
)
public class FacturaLegal extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Empresa es requerida")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @NotNull(message = "Timbrado detalle es requerido")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "timbrado_detalle_id", nullable = false)
    private TimbradoDetalle timbradoDetalle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @NotNull(message = "Número de factura es requerido")
    @Column(name = "numero_factura", nullable = false)
    private Integer numeroFactura;

    @NotNull(message = "Fecha es requerida")
    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false)
    private Boolean credito = false;

    // Datos del cliente (snapshot)
    @Size(max = 200)
    @Column(length = 200)
    private String nombre;

    @Size(max = 20)
    @Column(length = 20)
    private String ruc;

    @Column(columnDefinition = "TEXT")
    private String direccion;

    // Totales por tasa de IVA
    @Column(name = "iva_parcial_0", precision = 15, scale = 2)
    private BigDecimal ivaParcial0 = BigDecimal.ZERO;

    @Column(name = "iva_parcial_5", precision = 15, scale = 2)
    private BigDecimal ivaParcial5 = BigDecimal.ZERO;

    @Column(name = "iva_parcial_10", precision = 15, scale = 2)
    private BigDecimal ivaParcial10 = BigDecimal.ZERO;

    @Column(name = "total_parcial_0", precision = 15, scale = 2)
    private BigDecimal totalParcial0 = BigDecimal.ZERO;

    @Column(name = "total_parcial_5", precision = 15, scale = 2)
    private BigDecimal totalParcial5 = BigDecimal.ZERO;

    @Column(name = "total_parcial_10", precision = 15, scale = 2)
    private BigDecimal totalParcial10 = BigDecimal.ZERO;

    @Column(name = "descuento_final", precision = 15, scale = 2)
    private BigDecimal descuentoFinal = BigDecimal.ZERO;

    @NotNull(message = "Total parcial es requerido")
    @Column(name = "total_parcial", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalParcial = BigDecimal.ZERO;

    @NotNull(message = "Total final es requerido")
    @Column(name = "total_final", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalFinal = BigDecimal.ZERO;

    @Column(nullable = false)
    private Boolean activo = true;

    // Moneda extranjera
    @Size(max = 3)
    @Column(name = "moneda_extranjera", length = 3)
    private String monedaExtranjera;

    @Column(name = "cambio", precision = 10, scale = 4)
    private BigDecimal cambio;

    // Relaciones
    @OneToMany(mappedBy = "facturaLegal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FacturaLegalItem> items = new ArrayList<>();

    /**
     * Documento electrónico asociado a la factura.
     */
    @OneToOne(mappedBy = "facturaLegal", fetch = FetchType.LAZY)
    private DocumentoElectronico documentoElectronico;

    // Constructores
    public FacturaLegal() {
    }

    public FacturaLegal(Empresa empresa, TimbradoDetalle timbradoDetalle, Integer numeroFactura) {
        this.empresa = empresa;
        this.timbradoDetalle = timbradoDetalle;
        this.numeroFactura = numeroFactura;
        this.fecha = LocalDateTime.now();
        this.credito = false;
        this.activo = true;
    }

    // Métodos de negocio
    public void agregarItem(FacturaLegalItem item) {
        items.add(item);
        item.setFacturaLegal(this);
        recalcularTotales();
    }

    public void eliminarItem(FacturaLegalItem item) {
        items.remove(item);
        item.setFacturaLegal(null);
        recalcularTotales();
    }

    public void recalcularTotales() {
        // Resetear totales
        totalParcial0 = BigDecimal.ZERO;
        totalParcial5 = BigDecimal.ZERO;
        totalParcial10 = BigDecimal.ZERO;
        ivaParcial0 = BigDecimal.ZERO;
        ivaParcial5 = BigDecimal.ZERO;
        ivaParcial10 = BigDecimal.ZERO;

        // Calcular totales por tasa de IVA
        // Nota: El total del item ya incluye el IVA, por lo que:
        // - Para IVA 5%: el IVA es total / 21
        // - Para IVA 10%: el IVA es total / 11
        // - El total parcial es simplemente el total del item (no se suma el IVA)
        for (FacturaLegalItem item : items) {
            BigDecimal subtotal = item.getTotal();
            Integer ivaProducto = item.getProducto() != null ? item.getProducto().getIva() : 0;

            if (ivaProducto == 0) {
                totalParcial0 = totalParcial0.add(subtotal);
            } else if (ivaProducto == 5) {
                // IVA 5%: iva = total / 21
                BigDecimal iva = subtotal.divide(BigDecimal.valueOf(21), 2, java.math.RoundingMode.HALF_UP);
                ivaParcial5 = ivaParcial5.add(iva);
                // El total parcial 5 es el total del item (ya incluye IVA)
                totalParcial5 = totalParcial5.add(subtotal);
            } else if (ivaProducto == 10) {
                // IVA 10%: iva = total / 11
                BigDecimal iva = subtotal.divide(BigDecimal.valueOf(11), 2, java.math.RoundingMode.HALF_UP);
                ivaParcial10 = ivaParcial10.add(iva);
                // El total parcial 10 es el total del item (ya incluye IVA)
                totalParcial10 = totalParcial10.add(subtotal);
            }
        }

        // Calcular total parcial (suma de todos los parciales)
        totalParcial = totalParcial0.add(totalParcial5).add(totalParcial10);

        // Calcular total final (total parcial - descuento)
        totalFinal = totalParcial.subtract(descuentoFinal != null ? descuentoFinal : BigDecimal.ZERO);
    }

    public void aplicarDescuento(BigDecimal descuento) {
        this.descuentoFinal = descuento != null ? descuento : BigDecimal.ZERO;
        this.totalFinal = this.totalParcial.subtract(this.descuentoFinal);
    }

    public String getNumeroFacturaFormateado() {
        if (timbradoDetalle == null || numeroFactura == null) {
            return "";
        }
        return String.format("%s-%s-%07d",
            timbradoDetalle.getCodigoEstablecimientoFactura(),
            timbradoDetalle.getPuntoExpedicion(),
            numeroFactura);
    }

    public boolean tieneItems() {
        return items != null && !items.isEmpty();
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

    public TimbradoDetalle getTimbradoDetalle() {
        return timbradoDetalle;
    }

    public void setTimbradoDetalle(TimbradoDetalle timbradoDetalle) {
        this.timbradoDetalle = timbradoDetalle;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Integer getNumeroFactura() {
        return numeroFactura;
    }

    public void setNumeroFactura(Integer numeroFactura) {
        this.numeroFactura = numeroFactura;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
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

    public List<FacturaLegalItem> getItems() {
        return items;
    }

    public void setItems(List<FacturaLegalItem> items) {
        this.items = items;
    }

    public DocumentoElectronico getDocumentoElectronico() {
        return documentoElectronico;
    }

    public void setDocumentoElectronico(DocumentoElectronico documentoElectronico) {
        this.documentoElectronico = documentoElectronico;
    }

    public String getMonedaExtranjera() {
        return monedaExtranjera;
    }

    public void setMonedaExtranjera(String monedaExtranjera) {
        this.monedaExtranjera = monedaExtranjera;
    }

    public BigDecimal getCambio() {
        return cambio;
    }

    public void setCambio(BigDecimal cambio) {
        this.cambio = cambio;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FacturaLegal that = (FacturaLegal) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "FacturaLegal{" +
                "id=" + id +
                ", numeroFactura=" + numeroFactura +
                ", fecha=" + fecha +
                ", nombre='" + nombre + '\'' +
                ", totalFinal=" + totalFinal +
                ", activo=" + activo +
                '}';
    }
}
