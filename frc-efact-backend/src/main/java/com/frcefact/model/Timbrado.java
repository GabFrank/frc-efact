package com.frcefact.model;

import com.frcefact.model.base.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entidad Timbrado que representa un timbrado fiscal autorizado por la SET.
 * Mapea a la tabla financiero.timbrado.
 */
@Entity
@Table(name = "timbrado", schema = "financiero", indexes = {
    @Index(name = "idx_timbrado_empresa", columnList = "empresa_id"),
    @Index(name = "idx_timbrado_numero", columnList = "numero"),
    @Index(name = "idx_timbrado_activo", columnList = "activo")
})
public class Timbrado extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Empresa es requerida")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @NotBlank(message = "Razón social es requerida")
    @Size(max = 200, message = "Razón social no debe exceder 200 caracteres")
    @Column(name = "razon_social", nullable = false, length = 200)
    private String razonSocial;

    @NotBlank(message = "RUC es requerido")
    @Size(max = 20, message = "RUC no debe exceder 20 caracteres")
    @Column(nullable = false, length = 20)
    private String ruc;

    @NotBlank(message = "Número de timbrado es requerido")
    @Size(max = 20, message = "Número de timbrado no debe exceder 20 caracteres")
    @Column(nullable = false, length = 20)
    private String numero;

    @Column(name = "is_electronico", nullable = false)
    private Boolean isElectronico = false;

    @Column(name = "csc_encrypted", columnDefinition = "TEXT")
    private String cscEncrypted;

    @NotNull(message = "Fecha de inicio es requerida")
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @NotNull(message = "Fecha de fin es requerida")
    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    // Datos para documento electrónico
    @Size(max = 100)
    @Column(length = 100)
    private String email;

    @Size(max = 50)
    @Column(name = "tipo_sociedad", length = 50)
    private String tipoSociedad;

    @Size(max = 100)
    @Column(name = "domicilio_fiscal_departamento", length = 100)
    private String domicilioFiscalDepartamento;

    @Size(max = 100)
    @Column(name = "domicilio_fiscal_ciudad", length = 100)
    private String domicilioFiscalCiudad;

    @Size(max = 10)
    @Column(name = "domicilio_fiscal_codigo_ciudad", length = 10)
    private String domicilioFiscalCodigoCiudad;

    @Size(max = 100)
    @Column(name = "domicilio_fiscal_localidad", length = 100)
    private String domicilioFiscalLocalidad;

    @Size(max = 100)
    @Column(name = "domicilio_fiscal_barrio", length = 100)
    private String domicilioFiscalBarrio;

    @Column(name = "domicilio_fiscal_direccion", columnDefinition = "TEXT")
    private String domicilioFiscalDireccion;

    @Size(max = 50)
    @Column(length = 50)
    private String telefono;

    @Size(max = 20)
    @Column(name = "cod_actividad_economica_principal", length = 20)
    private String codActividadEconomicaPrincipal;

    @Size(max = 200)
    @Column(name = "desc_actividad_economica_principal", length = 200)
    private String descActividadEconomicaPrincipal;

    @Column(name = "list_codigo_actividad_economica_secundaria", columnDefinition = "TEXT")
    private String listCodigoActividadEconomicaSecundaria;

    @Column(name = "list_descripcion_actividad_economica_secundaria", columnDefinition = "TEXT")
    private String listDescripcionActividadEconomicaSecundaria;

    @Column(nullable = false)
    private Boolean activo = true;

    // Relaciones
    @OneToMany(mappedBy = "timbrado", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TimbradoDetalle> timbradoDetalles = new HashSet<>();

    // Constructores
    public Timbrado() {
    }

    public Timbrado(Empresa empresa, String numero, LocalDate fechaInicio, LocalDate fechaFin) {
        this.empresa = empresa;
        this.numero = numero;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.activo = true;
        this.isElectronico = false;
    }

    // Métodos de negocio
    public boolean isVigente() {
        LocalDate hoy = LocalDate.now();
        return hoy.isAfter(fechaInicio.minusDays(1)) && hoy.isBefore(fechaFin.plusDays(1));
    }

    public boolean isPorVencer(int diasAnticipacion) {
        LocalDate fechaLimite = LocalDate.now().plusDays(diasAnticipacion);
        return fechaFin.isBefore(fechaLimite) && fechaFin.isAfter(LocalDate.now());
    }

    public long getDiasRestantes() {
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), fechaFin);
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

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public Boolean getIsElectronico() {
        return isElectronico;
    }

    public void setIsElectronico(Boolean isElectronico) {
        this.isElectronico = isElectronico;
    }

    public String getCscEncrypted() {
        return cscEncrypted;
    }

    public void setCscEncrypted(String cscEncrypted) {
        this.cscEncrypted = cscEncrypted;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTipoSociedad() {
        return tipoSociedad;
    }

    public void setTipoSociedad(String tipoSociedad) {
        this.tipoSociedad = tipoSociedad;
    }

    public String getDomicilioFiscalDepartamento() {
        return domicilioFiscalDepartamento;
    }

    public void setDomicilioFiscalDepartamento(String domicilioFiscalDepartamento) {
        this.domicilioFiscalDepartamento = domicilioFiscalDepartamento;
    }

    public String getDomicilioFiscalCiudad() {
        return domicilioFiscalCiudad;
    }

    public void setDomicilioFiscalCiudad(String domicilioFiscalCiudad) {
        this.domicilioFiscalCiudad = domicilioFiscalCiudad;
    }

    public String getDomicilioFiscalCodigoCiudad() {
        return domicilioFiscalCodigoCiudad;
    }

    public void setDomicilioFiscalCodigoCiudad(String domicilioFiscalCodigoCiudad) {
        this.domicilioFiscalCodigoCiudad = domicilioFiscalCodigoCiudad;
    }

    public String getDomicilioFiscalLocalidad() {
        return domicilioFiscalLocalidad;
    }

    public void setDomicilioFiscalLocalidad(String domicilioFiscalLocalidad) {
        this.domicilioFiscalLocalidad = domicilioFiscalLocalidad;
    }

    public String getDomicilioFiscalBarrio() {
        return domicilioFiscalBarrio;
    }

    public void setDomicilioFiscalBarrio(String domicilioFiscalBarrio) {
        this.domicilioFiscalBarrio = domicilioFiscalBarrio;
    }

    public String getDomicilioFiscalDireccion() {
        return domicilioFiscalDireccion;
    }

    public void setDomicilioFiscalDireccion(String domicilioFiscalDireccion) {
        this.domicilioFiscalDireccion = domicilioFiscalDireccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCodActividadEconomicaPrincipal() {
        return codActividadEconomicaPrincipal;
    }

    public void setCodActividadEconomicaPrincipal(String codActividadEconomicaPrincipal) {
        this.codActividadEconomicaPrincipal = codActividadEconomicaPrincipal;
    }

    public String getDescActividadEconomicaPrincipal() {
        return descActividadEconomicaPrincipal;
    }

    public void setDescActividadEconomicaPrincipal(String descActividadEconomicaPrincipal) {
        this.descActividadEconomicaPrincipal = descActividadEconomicaPrincipal;
    }

    public String getListCodigoActividadEconomicaSecundaria() {
        return listCodigoActividadEconomicaSecundaria;
    }

    public void setListCodigoActividadEconomicaSecundaria(String listCodigoActividadEconomicaSecundaria) {
        this.listCodigoActividadEconomicaSecundaria = listCodigoActividadEconomicaSecundaria;
    }

    public String getListDescripcionActividadEconomicaSecundaria() {
        return listDescripcionActividadEconomicaSecundaria;
    }

    public void setListDescripcionActividadEconomicaSecundaria(String listDescripcionActividadEconomicaSecundaria) {
        this.listDescripcionActividadEconomicaSecundaria = listDescripcionActividadEconomicaSecundaria;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Set<TimbradoDetalle> getTimbradoDetalles() {
        return timbradoDetalles;
    }

    public void setTimbradoDetalles(Set<TimbradoDetalle> timbradoDetalles) {
        this.timbradoDetalles = timbradoDetalles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Timbrado timbrado = (Timbrado) o;
        return Objects.equals(id, timbrado.id) && Objects.equals(numero, timbrado.numero);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, numero);
    }

    @Override
    public String toString() {
        return "Timbrado{" +
                "id=" + id +
                ", numero='" + numero + '\'' +
                ", fechaInicio=" + fechaInicio +
                ", fechaFin=" + fechaFin +
                ", isElectronico=" + isElectronico +
                ", activo=" + activo +
                '}';
    }
}
