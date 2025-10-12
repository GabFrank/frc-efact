package com.frcefact.model;

import com.frcefact.model.base.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entidad Empresa que representa una empresa con datos fiscales completos.
 * Mapea a la tabla empresa.empresa siguiendo la estructura de facturación paraguaya.
 */
@Entity
@Table(name = "empresa", schema = "empresa", indexes = {
    @Index(name = "idx_empresa_ruc", columnList = "ruc"),
    @Index(name = "idx_empresa_activo", columnList = "activo"),
    @Index(name = "idx_empresa_razon_social", columnList = "razon_social")
})
public class Empresa extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Razón social es requerida")
    @Size(max = 200, message = "Razón social no debe exceder 200 caracteres")
    @Column(name = "razon_social", nullable = false, length = 200)
    private String razonSocial;

    @NotBlank(message = "RUC es requerido")
    @Size(max = 20, message = "RUC no debe exceder 20 caracteres")
    @Column(nullable = false, unique = true, length = 20)
    private String ruc;

    @Size(max = 200, message = "Nombre fantasía no debe exceder 200 caracteres")
    @Column(name = "nombre_fantasia", length = 200)
    private String nombreFantasia;

    @Email(message = "Email debe ser válido")
    @Size(max = 100, message = "Email no debe exceder 100 caracteres")
    @Column(length = 100)
    private String email;

    @Size(max = 50, message = "Teléfono no debe exceder 50 caracteres")
    @Column(length = 50)
    private String telefono;

    @Column(columnDefinition = "TEXT")
    private String direccion;

    // Datos fiscales
    @Size(max = 50, message = "Tipo sociedad no debe exceder 50 caracteres")
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

    // Actividad económica
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

    // Certificado digital
    @Size(max = 500)
    @Column(name = "certificado_path", length = 500)
    private String certificadoPath;

    @Column(name = "certificado_password_encrypted", columnDefinition = "TEXT")
    private String certificadoPasswordEncrypted;

    @Column(name = "certificado_fecha_expiracion")
    private LocalDate certificadoFechaExpiracion;

    // CSC (Código de Seguridad del Contribuyente)
    @Size(max = 50)
    @Column(name = "csc_id", length = 50)
    private String cscId;

    @Column(name = "csc_encrypted", columnDefinition = "TEXT")
    private String cscEncrypted;

    // Configuración SIFEN
    @Size(max = 20)
    @Column(name = "sifen_ambiente", length = 20)
    private String sifenAmbiente = "DEV"; // DEV, TEST, PRODUCTION

    @Column(nullable = false)
    private Boolean activo = true;

    // Relaciones
    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UsuarioEmpresa> usuarioEmpresas = new HashSet<>();

    // Constructores
    public Empresa() {
    }

    public Empresa(String razonSocial, String ruc) {
        this.razonSocial = razonSocial;
        this.ruc = ruc;
        this.activo = true;
    }

    // Métodos de negocio
    public boolean isCertificadoVigente() {
        if (certificadoFechaExpiracion == null) {
            return false;
        }
        return certificadoFechaExpiracion.isAfter(LocalDate.now());
    }

    public boolean isCertificadoPorVencer(int diasAnticipacion) {
        if (certificadoFechaExpiracion == null) {
            return false;
        }
        LocalDate fechaLimite = LocalDate.now().plusDays(diasAnticipacion);
        return certificadoFechaExpiracion.isBefore(fechaLimite) && 
               certificadoFechaExpiracion.isAfter(LocalDate.now());
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getNombreFantasia() {
        return nombreFantasia;
    }

    public void setNombreFantasia(String nombreFantasia) {
        this.nombreFantasia = nombreFantasia;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
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

    public String getCertificadoPath() {
        return certificadoPath;
    }

    public void setCertificadoPath(String certificadoPath) {
        this.certificadoPath = certificadoPath;
    }

    public String getCertificadoPasswordEncrypted() {
        return certificadoPasswordEncrypted;
    }

    public void setCertificadoPasswordEncrypted(String certificadoPasswordEncrypted) {
        this.certificadoPasswordEncrypted = certificadoPasswordEncrypted;
    }

    public LocalDate getCertificadoFechaExpiracion() {
        return certificadoFechaExpiracion;
    }

    public void setCertificadoFechaExpiracion(LocalDate certificadoFechaExpiracion) {
        this.certificadoFechaExpiracion = certificadoFechaExpiracion;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Set<UsuarioEmpresa> getUsuarioEmpresas() {
        return usuarioEmpresas;
    }

    public void setUsuarioEmpresas(Set<UsuarioEmpresa> usuarioEmpresas) {
        this.usuarioEmpresas = usuarioEmpresas;
    }

    public String getCscId() {
        return cscId;
    }

    public void setCscId(String cscId) {
        this.cscId = cscId;
    }

    public String getCscEncrypted() {
        return cscEncrypted;
    }

    public void setCscEncrypted(String cscEncrypted) {
        this.cscEncrypted = cscEncrypted;
    }

    public String getSifenAmbiente() {
        return sifenAmbiente;
    }

    public void setSifenAmbiente(String sifenAmbiente) {
        this.sifenAmbiente = sifenAmbiente;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Empresa empresa = (Empresa) o;
        return Objects.equals(id, empresa.id) && Objects.equals(ruc, empresa.ruc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, ruc);
    }

    @Override
    public String toString() {
        return "Empresa{" +
                "id=" + id +
                ", razonSocial='" + razonSocial + '\'' +
                ", ruc='" + ruc + '\'' +
                ", nombreFantasia='" + nombreFantasia + '\'' +
                ", activo=" + activo +
                ", creadoEn=" + creadoEn +
                '}';
    }
}
