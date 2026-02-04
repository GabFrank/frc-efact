package com.frcefact.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.frcefact.model.EventoCancelacionDE;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventoCancelacionDeDto {
    private Long id;
    private Long documentoId;
    private String cdcDocumento;
    private String eventoId;
    private String estado;
    private String codigoRespuesta;
    private String mensajeRespuesta;
    private String protocoloAutorizacion;
    private LocalDateTime fechaFirma;
    private LocalDateTime fechaProcesamiento;

    public static EventoCancelacionDeDto fromEntity(EventoCancelacionDE evento) {
        EventoCancelacionDeDto dto = new EventoCancelacionDeDto();
        dto.setId(evento.getId());
        dto.setDocumentoId(evento.getDocumentoElectronico() != null ? evento.getDocumentoElectronico().getId() : null);
        dto.setCdcDocumento(evento.getCdcDocumento());
        dto.setEventoId(evento.getEventoId());
        dto.setEstado(evento.getEstado() != null ? evento.getEstado().name() : null);
        dto.setCodigoRespuesta(evento.getCodigoRespuesta());
        dto.setMensajeRespuesta(evento.getMensajeRespuesta());
        dto.setProtocoloAutorizacion(evento.getProtocoloAutorizacion());
        dto.setFechaFirma(evento.getFechaFirma());
        dto.setFechaProcesamiento(evento.getFechaProcesamiento());
        return dto;
    }

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
}








