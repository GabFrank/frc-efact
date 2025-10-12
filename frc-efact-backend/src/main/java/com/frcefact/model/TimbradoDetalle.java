package com.frcefact.model;

import com.frcefact.model.base.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entidad TimbradoDetalle que representa un punto de expedición de un timbrado.
 * Mapea a la tabla financiero.timbrado_detalle.
 */
@Entity
@Table(name = "timbrado_detalle", schema = "financiero", indexes = {
    @Index(name = "idx_timbrado_detalle_timbrado", columnList = "timbrado_id"),
    @Index(name = "idx_timbrado_detalle_activo", columnList = "activo")
})
public class TimbradoDetalle extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Timbrado es requerido")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timbrado_id", nullable = false)
    private Timbrado timbrado;

    @NotBlank(message = "Punto de expedición es requerido")
    @Size(max = 10, message = "Punto de expedición no debe exceder 10 caracteres")
    @Column(name = "punto_expedicion", nullable = false, length = 10)
    private String puntoExpedicion;

    @NotBlank(message = "Código de establecimiento es requerido")
    @Size(max = 10, message = "Código de establecimiento no debe exceder 10 caracteres")
    @Column(name = "codigo_establecimiento_factura", nullable = false, length = 10)
    private String codigoEstablecimientoFactura;

    @NotNull(message = "Cantidad es requerida")
    @Column(nullable = false)
    private Long cantidad;

    @NotNull(message = "Rango desde es requerido")
    @Column(name = "rango_desde", nullable = false)
    private Long rangoDesde;

    @NotNull(message = "Rango hasta es requerido")
    @Column(name = "rango_hasta", nullable = false)
    private Long rangoHasta;

    @NotNull(message = "Número actual es requerido")
    @Column(name = "numero_actual", nullable = false)
    private Long numeroActual = 0L;

    // Ubicación del punto de expedición
    @Size(max = 100)
    @Column(length = 100)
    private String departamento;

    @Size(max = 100)
    @Column(length = 100)
    private String ciudad;

    @Size(max = 10)
    @Column(name = "codigo_ciudad", length = 10)
    private String codigoCiudad;

    @Size(max = 100)
    @Column(length = 100)
    private String localidad;

    @Size(max = 100)
    @Column(length = 100)
    private String barrio;

    @Column(columnDefinition = "TEXT")
    private String direccion;

    @Size(max = 50)
    @Column(length = 50)
    private String telefono;

    @Column(nullable = false)
    private Boolean activo = true;

    // Relaciones
    @OneToMany(mappedBy = "timbradoDetalle", cascade = CascadeType.ALL)
    private Set<FacturaLegal> facturas = new HashSet<>();

    @Version
    private Long version;

    // Constructores
    public TimbradoDetalle() {
    }

    public TimbradoDetalle(Timbrado timbrado, String puntoExpedicion, String codigoEstablecimientoFactura,
                          Long rangoDesde, Long rangoHasta) {
        this.timbrado = timbrado;
        this.puntoExpedicion = puntoExpedicion;
        this.codigoEstablecimientoFactura = codigoEstablecimientoFactura;
        this.rangoDesde = rangoDesde;
        this.rangoHasta = rangoHasta;
        this.cantidad = rangoHasta - rangoDesde + 1;
        this.numeroActual = rangoDesde;
        this.activo = true;
    }

    // Métodos de negocio
    public synchronized Long obtenerYIncrementarNumeroActual() {
        if (!tieneNumerosDisponibles()) {
            throw new IllegalStateException("No hay números disponibles en el rango");
        }
        Long numero = numeroActual;
        numeroActual++;
        return numero;
    }

    public boolean tieneNumerosDisponibles() {
        return numeroActual <= rangoHasta;
    }

    public long getNumerosDisponibles() {
        return Math.max(0, rangoHasta - numeroActual + 1);
    }

    public double getPorcentajeUtilizado() {
        if (cantidad == 0) return 0;
        long utilizados = numeroActual - rangoDesde;
        return (utilizados * 100.0) / cantidad;
    }

    public boolean isRangoPorAgotarse(int umbralPorcentaje) {
        return getPorcentajeUtilizado() >= umbralPorcentaje;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Timbrado getTimbrado() {
        return timbrado;
    }

    public void setTimbrado(Timbrado timbrado) {
        this.timbrado = timbrado;
    }

    public String getPuntoExpedicion() {
        return puntoExpedicion;
    }

    public void setPuntoExpedicion(String puntoExpedicion) {
        this.puntoExpedicion = puntoExpedicion;
    }

    public String getCodigoEstablecimientoFactura() {
        return codigoEstablecimientoFactura;
    }

    public void setCodigoEstablecimientoFactura(String codigoEstablecimientoFactura) {
        this.codigoEstablecimientoFactura = codigoEstablecimientoFactura;
    }

    public Long getCantidad() {
        return cantidad;
    }

    public void setCantidad(Long cantidad) {
        this.cantidad = cantidad;
    }

    public Long getRangoDesde() {
        return rangoDesde;
    }

    public void setRangoDesde(Long rangoDesde) {
        this.rangoDesde = rangoDesde;
    }

    public Long getRangoHasta() {
        return rangoHasta;
    }

    public void setRangoHasta(Long rangoHasta) {
        this.rangoHasta = rangoHasta;
    }

    public Long getNumeroActual() {
        return numeroActual;
    }

    public void setNumeroActual(Long numeroActual) {
        this.numeroActual = numeroActual;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getCodigoCiudad() {
        return codigoCiudad;
    }

    public void setCodigoCiudad(String codigoCiudad) {
        this.codigoCiudad = codigoCiudad;
    }

    public String getLocalidad() {
        return localidad;
    }

    public void setLocalidad(String localidad) {
        this.localidad = localidad;
    }

    public String getBarrio() {
        return barrio;
    }

    public void setBarrio(String barrio) {
        this.barrio = barrio;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Set<FacturaLegal> getFacturas() {
        return facturas;
    }

    public void setFacturas(Set<FacturaLegal> facturas) {
        this.facturas = facturas;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimbradoDetalle that = (TimbradoDetalle) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "TimbradoDetalle{" +
                "id=" + id +
                ", puntoExpedicion='" + puntoExpedicion + '\'' +
                ", codigoEstablecimientoFactura='" + codigoEstablecimientoFactura + '\'' +
                ", rangoDesde=" + rangoDesde +
                ", rangoHasta=" + rangoHasta +
                ", numeroActual=" + numeroActual +
                ", activo=" + activo +
                '}';
    }
}
