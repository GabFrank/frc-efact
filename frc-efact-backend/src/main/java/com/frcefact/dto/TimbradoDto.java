package com.frcefact.dto;

import com.frcefact.validation.ValidFechasTimbrado;
import com.frcefact.validation.ValidTimbrado;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

/**
 * DTO simplificado para Timbrado.
 * Solo contiene datos específicos del timbrado, los datos de empresa se obtienen de la relación.
 */
@ValidFechasTimbrado
public class TimbradoDto {

    private Long id;

    @NotNull(message = "ID de empresa es requerido")
    private Long empresaId;

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

    private Boolean activo;

    // Campos calculados para mostrar información de empresa
    private String razonSocial; // Solo para mostrar, no se valida
    private String ruc; // Solo para mostrar, no se valida
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

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    // Campos de solo lectura (información de empresa)
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