package com.frcefact.dto;

import com.frcefact.validation.ValidFechasTimbrado;
import com.frcefact.validation.ValidTimbrado;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

/**
 * DTO para Timbrado con validaciones.
 */
@ValidFechasTimbrado
public class TimbradoDto {

    private Long id;

    @NotNull(message = "ID de empresa es requerido")
    private Long empresaId;

    @NotBlank(message = "Razón social es requerida")
    @Size(max = 200, message = "Razón social no debe exceder 200 caracteres")
    private String razonSocial;

    @NotBlank(message = "RUC es requerido")
    @Size(max = 20, message = "RUC no debe exceder 20 caracteres")
    private String ruc;

    @NotBlank(message = "Número de timbrado es requerido")
    @ValidTimbrado
    @Size(max = 20, message = "Número de timbrado no debe exceder 20 caracteres")
    private String numero;

    @NotNull(message = "Debe indicar si es electrónico")
    private Boolean isElectronico;

    private String csc; // Se envía sin encriptar, el servicio lo encripta

    @NotNull(message = "Fecha de inicio es requerida")
    private LocalDate fechaInicio;

    @NotNull(message = "Fecha de fin es requerida")
    private LocalDate fechaFin;

    @Email(message = "Email debe ser válido")
    @Size(max = 100, message = "Email no debe exceder 100 caracteres")
    private String email;

    @Size(max = 50, message = "Tipo de sociedad no debe exceder 50 caracteres")
    private String tipoSociedad;

    @Size(max = 100, message = "Departamento no debe exceder 100 caracteres")
    private String domicilioFiscalDepartamento;

    @Size(max = 100, message = "Ciudad no debe exceder 100 caracteres")
    private String domicilioFiscalCiudad;

    @Size(max = 10, message = "Código de ciudad no debe exceder 10 caracteres")
    private String domicilioFiscalCodigoCiudad;

    @Size(max = 100, message = "Localidad no debe exceder 100 caracteres")
    private String domicilioFiscalLocalidad;

    @Size(max = 100, message = "Barrio no debe exceder 100 caracteres")
    private String domicilioFiscalBarrio;

    private String domicilioFiscalDireccion;

    @Size(max = 50, message = "Teléfono no debe exceder 50 caracteres")
    private String telefono;

    @Size(max = 20, message = "Código de actividad económica no debe exceder 20 caracteres")
    private String codActividadEconomicaPrincipal;

    @Size(max = 200, message = "Descripción de actividad económica no debe exceder 200 caracteres")
    private String descActividadEconomicaPrincipal;

    private String listCodigoActividadEconomicaSecundaria;

    private String listDescripcionActividadEconomicaSecundaria;

    private Boolean activo;

    private Boolean vigente;

    private Long diasRestantes;

    // Constructores
    public TimbradoDto() {
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
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

    public String getCsc() {
        return csc;
    }

    public void setCsc(String csc) {
        this.csc = csc;
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

    public Boolean getVigente() {
        return vigente;
    }

    public void setVigente(Boolean vigente) {
        this.vigente = vigente;
    }

    public Long getDiasRestantes() {
        return diasRestantes;
    }

    public void setDiasRestantes(Long diasRestantes) {
        this.diasRestantes = diasRestantes;
    }
}
