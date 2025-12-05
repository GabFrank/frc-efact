package com.frcefact.model;

import com.frcefact.model.base.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "nota_remision", schema = "financiero",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_nr_numero", 
            columnNames = {"timbrado_detalle_id", "numero_nota_remision"})
    },
    indexes = {
        @Index(name = "idx_nr_empresa", columnList = "empresa_id")
    }
)
public class NotaRemision extends AuditableEntity {

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

    @NotNull(message = "Número de nota de remisión es requerido")
    @Column(name = "numero_nota_remision", nullable = false)
    private Integer numeroNotaRemision;

    @NotNull(message = "Fecha es requerida")
    @Column(nullable = false)
    private LocalDateTime fecha;

    // Salida
    @Column(name = "direccion_partida", columnDefinition = "TEXT")
    private String direccionPartida;

    @Size(max = 100)
    @Column(name = "ciudad_partida", length = 100)
    private String ciudadPartida;

    @Size(max = 100)
    @Column(name = "departamento_partida", length = 100)
    private String departamentoPartida;

    // Llegada
    @Size(max = 200)
    @Column(name = "nombre_destinatario", length = 200)
    private String nombreDestinatario;

    @Size(max = 20)
    @Column(name = "ruc_destinatario", length = 20)
    private String rucDestinatario;

    @Column(name = "direccion_destinatario", columnDefinition = "TEXT")
    private String direccionDestinatario;

    @Size(max = 100)
    @Column(name = "ciudad_destinatario", length = 100)
    private String ciudadDestinatario;

    @Size(max = 100)
    @Column(name = "departamento_destinatario", length = 100)
    private String departamentoDestinatario;

    // Datos remision
    @Size(max = 50)
    @Column(name = "motivo_emision", length = 50)
    private String motivoEmision;

    @Column(name = "fecha_inicio_traslado")
    private LocalDate fechaInicioTraslado;

    @Column(name = "fecha_fin_traslado")
    private LocalDate fechaFinTraslado;

    @Column(name = "km_estimado", precision = 10, scale = 2)
    private BigDecimal kmEstimado;

    // Transporte
    @Size(max = 50)
    @Column(name = "tipo_transporte", length = 50)
    private String tipoTransporte;

    @Size(max = 50)
    @Column(name = "modalidad_transporte", length = 50)
    private String modalidadTransporte;

    // Vehiculo
    @Size(max = 100)
    @Column(name = "vehiculo_marca", length = 100)
    private String vehiculoMarca;

    @Size(max = 20)
    @Column(name = "vehiculo_matricula", length = 20)
    private String vehiculoMatricula;

    // Conductor
    @Size(max = 200)
    @Column(name = "conductor_nombre", length = 200)
    private String conductorNombre;

    @Size(max = 20)
    @Column(name = "conductor_doc", length = 20)
    private String conductorDoc;

    @Column(name = "conductor_direccion", columnDefinition = "TEXT")
    private String conductorDireccion;

    @Column(nullable = false)
    private Boolean activo = true;

    @OneToMany(mappedBy = "notaRemision", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NotaRemisionItem> items = new ArrayList<>();

    @OneToOne(mappedBy = "notaRemision", fetch = FetchType.LAZY)
    private DocumentoElectronico documentoElectronico;

    public NotaRemision() {
    }

    public void agregarItem(NotaRemisionItem item) {
        items.add(item);
        item.setNotaRemision(this);
    }

    public void eliminarItem(NotaRemisionItem item) {
        items.remove(item);
        item.setNotaRemision(null);
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

    public Integer getNumeroNotaRemision() { return numeroNotaRemision; }
    public void setNumeroNotaRemision(Integer numeroNotaRemision) { this.numeroNotaRemision = numeroNotaRemision; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getDireccionPartida() { return direccionPartida; }
    public void setDireccionPartida(String direccionPartida) { this.direccionPartida = direccionPartida; }

    public String getCiudadPartida() { return ciudadPartida; }
    public void setCiudadPartida(String ciudadPartida) { this.ciudadPartida = ciudadPartida; }

    public String getDepartamentoPartida() { return departamentoPartida; }
    public void setDepartamentoPartida(String departamentoPartida) { this.departamentoPartida = departamentoPartida; }

    public String getNombreDestinatario() { return nombreDestinatario; }
    public void setNombreDestinatario(String nombreDestinatario) { this.nombreDestinatario = nombreDestinatario; }

    public String getRucDestinatario() { return rucDestinatario; }
    public void setRucDestinatario(String rucDestinatario) { this.rucDestinatario = rucDestinatario; }

    public String getDireccionDestinatario() { return direccionDestinatario; }
    public void setDireccionDestinatario(String direccionDestinatario) { this.direccionDestinatario = direccionDestinatario; }

    public String getCiudadDestinatario() { return ciudadDestinatario; }
    public void setCiudadDestinatario(String ciudadDestinatario) { this.ciudadDestinatario = ciudadDestinatario; }

    public String getDepartamentoDestinatario() { return departamentoDestinatario; }
    public void setDepartamentoDestinatario(String departamentoDestinatario) { this.departamentoDestinatario = departamentoDestinatario; }

    public String getMotivoEmision() { return motivoEmision; }
    public void setMotivoEmision(String motivoEmision) { this.motivoEmision = motivoEmision; }

    public LocalDate getFechaInicioTraslado() { return fechaInicioTraslado; }
    public void setFechaInicioTraslado(LocalDate fechaInicioTraslado) { this.fechaInicioTraslado = fechaInicioTraslado; }

    public LocalDate getFechaFinTraslado() { return fechaFinTraslado; }
    public void setFechaFinTraslado(LocalDate fechaFinTraslado) { this.fechaFinTraslado = fechaFinTraslado; }

    public BigDecimal getKmEstimado() { return kmEstimado; }
    public void setKmEstimado(BigDecimal kmEstimado) { this.kmEstimado = kmEstimado; }

    public String getTipoTransporte() { return tipoTransporte; }
    public void setTipoTransporte(String tipoTransporte) { this.tipoTransporte = tipoTransporte; }

    public String getModalidadTransporte() { return modalidadTransporte; }
    public void setModalidadTransporte(String modalidadTransporte) { this.modalidadTransporte = modalidadTransporte; }

    public String getVehiculoMarca() { return vehiculoMarca; }
    public void setVehiculoMarca(String vehiculoMarca) { this.vehiculoMarca = vehiculoMarca; }

    public String getVehiculoMatricula() { return vehiculoMatricula; }
    public void setVehiculoMatricula(String vehiculoMatricula) { this.vehiculoMatricula = vehiculoMatricula; }

    public String getConductorNombre() { return conductorNombre; }
    public void setConductorNombre(String conductorNombre) { this.conductorNombre = conductorNombre; }

    public String getConductorDoc() { return conductorDoc; }
    public void setConductorDoc(String conductorDoc) { this.conductorDoc = conductorDoc; }

    public String getConductorDireccion() { return conductorDireccion; }
    public void setConductorDireccion(String conductorDireccion) { this.conductorDireccion = conductorDireccion; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public List<NotaRemisionItem> getItems() { return items; }
    public void setItems(List<NotaRemisionItem> items) { this.items = items; }

    public DocumentoElectronico getDocumentoElectronico() { return documentoElectronico; }
    public void setDocumentoElectronico(DocumentoElectronico documentoElectronico) { this.documentoElectronico = documentoElectronico; }

    public String getNumeroFormateado() {
        if (timbradoDetalle == null || numeroNotaRemision == null) {
            return "";
        }
        return String.format("%s-%s-%07d",
            timbradoDetalle.getCodigoEstablecimientoFactura(),
            timbradoDetalle.getPuntoExpedicion(),
            numeroNotaRemision);
    }
}

