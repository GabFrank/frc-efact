package com.frcefact.dto;

import com.frcefact.validation.ValidRuc;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * DTO para transferencia de datos de Empresa.
 * Incluye validaciones de campos requeridos y formato.
 */
public class EmpresaDto {

    private Long id;

    @NotBlank(message = "Razón social es requerida")
    @Size(max = 200, message = "Razón social no debe exceder 200 caracteres")
    private String razonSocial;

    @NotBlank(message = "RUC es requerido")
    @ValidRuc
    private String ruc;

    @NotBlank(message = "Tipo de contribuyente es requerido")
    @Size(max = 2, message = "Tipo contribuyente debe ser PF o PJ")
    private String tipoContribuyente = "PF"; // PF = Persona Física, PJ = Persona Jurídica

    @Size(max = 200, message = "Nombre fantasía no debe exceder 200 caracteres")
    private String nombreFantasia;

    @Email(message = "Email debe ser válido")
    @Size(max = 100, message = "Email no debe exceder 100 caracteres")
    private String email;

    @Size(max = 50, message = "Teléfono no debe exceder 50 caracteres")
    private String telefono;

    private String direccion; // Dirección general de la empresa

    @Size(max = 50, message = "Tipo sociedad no debe exceder 50 caracteres")
    private String tipoSociedad;

    // Domicilio fiscal simplificado
    private Long ciudadId;
    
    private Long barrioId;
    
    private String domicilioFiscalDireccion;

    @Valid
    private ActividadEconomicaDto actividadEconomica;

    // Certificado digital
    private String certificadoPath;
    private String certificadoPassword; // Se encriptará antes de guardar
    private LocalDate certificadoFechaExpiracion;

    private Boolean activo;

    // Constructores
    public EmpresaDto() {
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

    public String getTipoContribuyente() {
        return tipoContribuyente;
    }

    public void setTipoContribuyente(String tipoContribuyente) {
        this.tipoContribuyente = tipoContribuyente;
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

    public Long getCiudadId() {
        return ciudadId;
    }

    public void setCiudadId(Long ciudadId) {
        this.ciudadId = ciudadId;
    }

    public Long getBarrioId() {
        return barrioId;
    }

    public void setBarrioId(Long barrioId) {
        this.barrioId = barrioId;
    }

    public String getDomicilioFiscalDireccion() {
        return domicilioFiscalDireccion;
    }

    public void setDomicilioFiscalDireccion(String domicilioFiscalDireccion) {
        this.domicilioFiscalDireccion = domicilioFiscalDireccion;
    }

    public ActividadEconomicaDto getActividadEconomica() {
        return actividadEconomica;
    }

    public void setActividadEconomica(ActividadEconomicaDto actividadEconomica) {
        this.actividadEconomica = actividadEconomica;
    }

    public String getCertificadoPath() {
        return certificadoPath;
    }

    public void setCertificadoPath(String certificadoPath) {
        this.certificadoPath = certificadoPath;
    }

    public String getCertificadoPassword() {
        return certificadoPassword;
    }

    public void setCertificadoPassword(String certificadoPassword) {
        this.certificadoPassword = certificadoPassword;
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
}
