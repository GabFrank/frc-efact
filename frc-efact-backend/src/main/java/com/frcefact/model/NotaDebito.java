package com.frcefact.model;

import com.frcefact.model.base.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "nota_debito", schema = "financiero",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_nd_numero", 
            columnNames = {"timbrado_detalle_id", "numero_nota_debito"})
    },
    indexes = {
        @Index(name = "idx_nd_empresa", columnList = "empresa_id"),
        @Index(name = "idx_nd_cliente", columnList = "cliente_id")
    }
)
public class NotaDebito extends AuditableEntity {

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_legal_id")
    private FacturaLegal facturaLegal;

    @NotNull(message = "Número de nota de débito es requerido")
    @Column(name = "numero_nota_debito", nullable = false)
    private Integer numeroNotaDebito;

    @NotNull(message = "Fecha es requerida")
    @Column(nullable = false)
    private LocalDateTime fecha;

    // Datos del cliente
    @Size(max = 200)
    @Column(length = 200)
    private String nombre;

    @Size(max = 20)
    @Column(length = 20)
    private String ruc;

    @Column(columnDefinition = "TEXT")
    private String direccion;

    // Motivo
    @Size(max = 50)
    @Column(name = "motivo_emision", length = 50)
    private String motivoEmision;

    @Size(max = 255)
    @Column(name = "descripcion_motivo", length = 255)
    private String descripcionMotivo;

    // Totales
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

    // Moneda
    @Size(max = 3)
    @Column(name = "moneda_extranjera", length = 3)
    private String monedaExtranjera;

    @Column(name = "cambio", precision = 10, scale = 4)
    private BigDecimal cambio;

    @OneToMany(mappedBy = "notaDebito", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NotaDebitoItem> items = new ArrayList<>();

    @OneToOne(mappedBy = "notaDebito", fetch = FetchType.LAZY)
    private DocumentoElectronico documentoElectronico;

    public NotaDebito() {
    }

    public void agregarItem(NotaDebitoItem item) {
        items.add(item);
        item.setNotaDebito(this);
        recalcularTotales();
    }

    public void eliminarItem(NotaDebitoItem item) {
        items.remove(item);
        item.setNotaDebito(null);
        recalcularTotales();
    }

    public void recalcularTotales() {
        totalParcial0 = BigDecimal.ZERO;
        totalParcial5 = BigDecimal.ZERO;
        totalParcial10 = BigDecimal.ZERO;
        ivaParcial0 = BigDecimal.ZERO;
        ivaParcial5 = BigDecimal.ZERO;
        ivaParcial10 = BigDecimal.ZERO;
        totalParcial = BigDecimal.ZERO;
        totalFinal = BigDecimal.ZERO;

        for (NotaDebitoItem item : items) {
            BigDecimal subtotal = item.getTotal();
            Integer ivaProducto = item.getIva();

            if (ivaProducto == 0) {
                totalParcial0 = totalParcial0.add(subtotal);
            } else if (ivaProducto == 5) {
                BigDecimal iva = subtotal.divide(BigDecimal.valueOf(21), 2, java.math.RoundingMode.HALF_UP);
                ivaParcial5 = ivaParcial5.add(iva);
                totalParcial5 = totalParcial5.add(subtotal);
            } else if (ivaProducto == 10) {
                BigDecimal iva = subtotal.divide(BigDecimal.valueOf(11), 2, java.math.RoundingMode.HALF_UP);
                ivaParcial10 = ivaParcial10.add(iva);
                totalParcial10 = totalParcial10.add(subtotal);
            }
        }

        // Calcular total parcial (suma de todos los parciales)
        totalParcial = totalParcial0.add(totalParcial5).add(totalParcial10);

        // Calcular total final (total parcial - descuento)
        totalFinal = totalParcial.subtract(descuentoFinal != null ? descuentoFinal : BigDecimal.ZERO);
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }

    public TimbradoDetalle getTimbradoDetalle() { return timbradoDetalle; }
    public void setTimbradoDetalle(TimbradoDetalle timbradoDetalle) { this.timbradoDetalle = timbradoDetalle; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public FacturaLegal getFacturaLegal() { return facturaLegal; }
    public void setFacturaLegal(FacturaLegal facturaLegal) { this.facturaLegal = facturaLegal; }

    public Integer getNumeroNotaDebito() { return numeroNotaDebito; }
    public void setNumeroNotaDebito(Integer numeroNotaDebito) { this.numeroNotaDebito = numeroNotaDebito; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getRuc() { return ruc; }
    public void setRuc(String ruc) { this.ruc = ruc; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getMotivoEmision() { return motivoEmision; }
    public void setMotivoEmision(String motivoEmision) { this.motivoEmision = motivoEmision; }

    public String getDescripcionMotivo() { return descripcionMotivo; }
    public void setDescripcionMotivo(String descripcionMotivo) { this.descripcionMotivo = descripcionMotivo; }

    public BigDecimal getIvaParcial0() { return ivaParcial0; }
    public void setIvaParcial0(BigDecimal ivaParcial0) { this.ivaParcial0 = ivaParcial0; }

    public BigDecimal getIvaParcial5() { return ivaParcial5; }
    public void setIvaParcial5(BigDecimal ivaParcial5) { this.ivaParcial5 = ivaParcial5; }

    public BigDecimal getIvaParcial10() { return ivaParcial10; }
    public void setIvaParcial10(BigDecimal ivaParcial10) { this.ivaParcial10 = ivaParcial10; }

    public BigDecimal getTotalParcial0() { return totalParcial0; }
    public void setTotalParcial0(BigDecimal totalParcial0) { this.totalParcial0 = totalParcial0; }

    public BigDecimal getTotalParcial5() { return totalParcial5; }
    public void setTotalParcial5(BigDecimal totalParcial5) { this.totalParcial5 = totalParcial5; }

    public BigDecimal getTotalParcial10() { return totalParcial10; }
    public void setTotalParcial10(BigDecimal totalParcial10) { this.totalParcial10 = totalParcial10; }

    public BigDecimal getDescuentoFinal() { return descuentoFinal; }
    public void setDescuentoFinal(BigDecimal descuentoFinal) { this.descuentoFinal = descuentoFinal; }

    public BigDecimal getTotalParcial() { return totalParcial; }
    public void setTotalParcial(BigDecimal totalParcial) { this.totalParcial = totalParcial; }

    public BigDecimal getTotalFinal() { return totalFinal; }
    public void setTotalFinal(BigDecimal totalFinal) { this.totalFinal = totalFinal; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public String getMonedaExtranjera() { return monedaExtranjera; }
    public void setMonedaExtranjera(String monedaExtranjera) { this.monedaExtranjera = monedaExtranjera; }

    public BigDecimal getCambio() { return cambio; }
    public void setCambio(BigDecimal cambio) { this.cambio = cambio; }

    public List<NotaDebitoItem> getItems() { return items; }
    public void setItems(List<NotaDebitoItem> items) { this.items = items; }

    public DocumentoElectronico getDocumentoElectronico() { return documentoElectronico; }
    public void setDocumentoElectronico(DocumentoElectronico documentoElectronico) { this.documentoElectronico = documentoElectronico; }

    public String getNumeroFormateado() {
        if (timbradoDetalle == null || numeroNotaDebito == null) {
            return "";
        }
        return String.format("%s-%s-%07d",
            timbradoDetalle.getCodigoEstablecimientoFactura(),
            timbradoDetalle.getPuntoExpedicion(),
            numeroNotaDebito);
    }
}

