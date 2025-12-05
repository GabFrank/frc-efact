package com.frcefact.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.frcefact.model.EventoNominacionDE;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventoNominacionDeDto {
    private Long id;
    private Long documentoId;
    private Long clienteId;
    private String cdcDocumento;
    private String eventoId;
    private String estado;
    private String codigoRespuesta;
    private String mensajeRespuesta;
    private String protocoloAutorizacion;
    private LocalDateTime fechaFirma;
    private LocalDateTime fechaProcesamiento;
    private String nombreReceptor;
    private String documentoReceptor;
    private String tipoReceptor;
    private BigDecimal totalFactura;

    public static EventoNominacionDeDto fromEntity(EventoNominacionDE evento) {
        EventoNominacionDeDto dto = new EventoNominacionDeDto();
        dto.setId(evento.getId());
        dto.setDocumentoId(evento.getDocumentoElectronico() != null ? evento.getDocumentoElectronico().getId() : null);
        dto.setClienteId(evento.getCliente() != null ? evento.getCliente().getId() : null);
        dto.setCdcDocumento(evento.getCdcDocumento());
        dto.setEventoId(evento.getEventoId());
        dto.setEstado(evento.getEstado() != null ? evento.getEstado().name() : null);
        dto.setCodigoRespuesta(evento.getCodigoRespuesta());
        dto.setMensajeRespuesta(evento.getMensajeRespuesta());
        dto.setProtocoloAutorizacion(evento.getProtocoloAutorizacion());
        dto.setFechaFirma(evento.getFechaFirma());
        dto.setFechaProcesamiento(evento.getFechaProcesamiento());
        dto.setNombreReceptor(evento.getNombreReceptor());
        dto.setDocumentoReceptor(evento.getDocumentoReceptor());
        dto.setTipoReceptor(evento.getTipoReceptor());
        dto.setTotalFactura(evento.getTotalFactura());
        return dto;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDocumentoId() {
        return documentoId;
    }

    public void setDocumentoId(Long documentoId) {
        this.documentoId = documentoId;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public String getCdcDocumento() {
        return cdcDocumento;
    }

    public void setCdcDocumento(String cdcDocumento) {
        this.cdcDocumento = cdcDocumento;
    }

    public String getEventoId() {
        return eventoId;
    }

    public void setEventoId(String eventoId) {
        this.eventoId = eventoId;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCodigoRespuesta() {
        return codigoRespuesta;
    }

    public void setCodigoRespuesta(String codigoRespuesta) {
        this.codigoRespuesta = codigoRespuesta;
    }

    public String getMensajeRespuesta() {
        return mensajeRespuesta;
    }

    public void setMensajeRespuesta(String mensajeRespuesta) {
        this.mensajeRespuesta = mensajeRespuesta;
    }

    public String getProtocoloAutorizacion() {
        return protocoloAutorizacion;
    }

    public void setProtocoloAutorizacion(String protocoloAutorizacion) {
        this.protocoloAutorizacion = protocoloAutorizacion;
    }

    public LocalDateTime getFechaFirma() {
        return fechaFirma;
    }

    public void setFechaFirma(LocalDateTime fechaFirma) {
        this.fechaFirma = fechaFirma;
    }

    public LocalDateTime getFechaProcesamiento() {
        return fechaProcesamiento;
    }

    public void setFechaProcesamiento(LocalDateTime fechaProcesamiento) {
        this.fechaProcesamiento = fechaProcesamiento;
    }

    public String getNombreReceptor() {
        return nombreReceptor;
    }

    public void setNombreReceptor(String nombreReceptor) {
        this.nombreReceptor = nombreReceptor;
    }

    public String getDocumentoReceptor() {
        return documentoReceptor;
    }

    public void setDocumentoReceptor(String documentoReceptor) {
        this.documentoReceptor = documentoReceptor;
    }

    public String getTipoReceptor() {
        return tipoReceptor;
    }

    public void setTipoReceptor(String tipoReceptor) {
        this.tipoReceptor = tipoReceptor;
    }

    public BigDecimal getTotalFactura() {
        return totalFactura;
    }

    public void setTotalFactura(BigDecimal totalFactura) {
        this.totalFactura = totalFactura;
    }
}









