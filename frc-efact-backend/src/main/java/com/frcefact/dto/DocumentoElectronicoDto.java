package com.frcefact.dto;

import com.frcefact.model.EstadoDE;
import com.frcefact.validation.ValidCdc;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * DTO para Documento Electrónico.
 */
public class DocumentoElectronicoDto {

    private Long id;

    @NotNull(message = "ID de factura legal es requerido")
    private Long facturaLegalId;

    private Long loteDeId;

    @ValidCdc
    private String cdc;

    private String urlQr;

    private String numeroDocumento;

    private String tipoDocumento;

    private EstadoDE estado;

    private String codigoRespuestaSifen;

    private String mensajeRespuestaSifen;

    private LocalDateTime fechaEmision;

    private LocalDateTime fechaRecepcionSifen;

    // Datos de la factura (para visualización)
    private String numeroFacturaFormateado;
    private String nombreCliente;
    private String rucCliente;
    private String razonSocialEmpresa;

    // Constructores

    public DocumentoElectronicoDto() {
    }

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFacturaLegalId() {
        return facturaLegalId;
    }

    public void setFacturaLegalId(Long facturaLegalId) {
        this.facturaLegalId = facturaLegalId;
    }

    public Long getLoteDeId() {
        return loteDeId;
    }

    public void setLoteDeId(Long loteDeId) {
        this.loteDeId = loteDeId;
    }

    public String getCdc() {
        return cdc;
    }

    public void setCdc(String cdc) {
        this.cdc = cdc;
    }

    public String getUrlQr() {
        return urlQr;
    }

    public void setUrlQr(String urlQr) {
        this.urlQr = urlQr;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public EstadoDE getEstado() {
        return estado;
    }

    public void setEstado(EstadoDE estado) {
        this.estado = estado;
    }

    public String getCodigoRespuestaSifen() {
        return codigoRespuestaSifen;
    }

    public void setCodigoRespuestaSifen(String codigoRespuestaSifen) {
        this.codigoRespuestaSifen = codigoRespuestaSifen;
    }

    public String getMensajeRespuestaSifen() {
        return mensajeRespuestaSifen;
    }

    public void setMensajeRespuestaSifen(String mensajeRespuestaSifen) {
        this.mensajeRespuestaSifen = mensajeRespuestaSifen;
    }

    public LocalDateTime getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(LocalDateTime fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public LocalDateTime getFechaRecepcionSifen() {
        return fechaRecepcionSifen;
    }

    public void setFechaRecepcionSifen(LocalDateTime fechaRecepcionSifen) {
        this.fechaRecepcionSifen = fechaRecepcionSifen;
    }

    public String getNumeroFacturaFormateado() {
        return numeroFacturaFormateado;
    }

    public void setNumeroFacturaFormateado(String numeroFacturaFormateado) {
        this.numeroFacturaFormateado = numeroFacturaFormateado;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getRucCliente() {
        return rucCliente;
    }

    public void setRucCliente(String rucCliente) {
        this.rucCliente = rucCliente;
    }

    public String getRazonSocialEmpresa() {
        return razonSocialEmpresa;
    }

    public void setRazonSocialEmpresa(String razonSocialEmpresa) {
        this.razonSocialEmpresa = razonSocialEmpresa;
    }
}
