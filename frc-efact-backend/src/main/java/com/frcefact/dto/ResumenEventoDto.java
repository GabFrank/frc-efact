package com.frcefact.dto;

import com.frcefact.model.EstadoEvento;
import com.frcefact.model.EventoCancelacionDE;
import com.frcefact.model.EventoNominacionDE;

import java.time.LocalDateTime;

/**
 * DTO resumido para listar eventos asociados a un documento.
 */
public class ResumenEventoDto {
    private Long id;
    private String tipo; // CANCELACION, NOMINACION
    private String eventoId;
    private LocalDateTime fechaFirma;
    private EstadoEvento estado;
    private LocalDateTime fechaProcesamiento;
    private String codigoRespuesta;
    private String mensajeRespuesta;
    private String protocoloAutorizacion;
    private String observacion; // Motivo o datos relevantes

    public ResumenEventoDto() {
    }

    public static ResumenEventoDto fromCancelacion(EventoCancelacionDE evento) {
        ResumenEventoDto dto = new ResumenEventoDto();
        dto.setId(evento.getId());
        dto.setTipo("CANCELACION");
        dto.setEventoId(evento.getEventoId());
        dto.setFechaFirma(evento.getFechaFirma());
        dto.setEstado(evento.getEstado());
        dto.setFechaProcesamiento(evento.getFechaProcesamiento());
        dto.setCodigoRespuesta(evento.getCodigoRespuesta());
        dto.setMensajeRespuesta(evento.getMensajeRespuesta());
        dto.setProtocoloAutorizacion(evento.getProtocoloAutorizacion());
        dto.setObservacion(evento.getMotivoCancelacion());
        return dto;
    }

    public static ResumenEventoDto fromNominacion(EventoNominacionDE evento) {
        ResumenEventoDto dto = new ResumenEventoDto();
        dto.setId(evento.getId());
        dto.setTipo("NOMINACION");
        dto.setEventoId(evento.getEventoId());
        dto.setFechaFirma(evento.getFechaFirma());
        dto.setEstado(evento.getEstado());
        dto.setFechaProcesamiento(evento.getFechaProcesamiento());
        dto.setCodigoRespuesta(evento.getCodigoRespuesta());
        dto.setMensajeRespuesta(evento.getMensajeRespuesta());
        dto.setProtocoloAutorizacion(evento.getProtocoloAutorizacion());
        dto.setObservacion("Cliente: " + evento.getNombreReceptor() + " (" + evento.getDocumentoReceptor() + ")");
        return dto;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getEventoId() { return eventoId; }
    public void setEventoId(String eventoId) { this.eventoId = eventoId; }
    public LocalDateTime getFechaFirma() { return fechaFirma; }
    public void setFechaFirma(LocalDateTime fechaFirma) { this.fechaFirma = fechaFirma; }
    public EstadoEvento getEstado() { return estado; }
    public void setEstado(EstadoEvento estado) { this.estado = estado; }
    public LocalDateTime getFechaProcesamiento() { return fechaProcesamiento; }
    public void setFechaProcesamiento(LocalDateTime fechaProcesamiento) { this.fechaProcesamiento = fechaProcesamiento; }
    public String getCodigoRespuesta() { return codigoRespuesta; }
    public void setCodigoRespuesta(String codigoRespuesta) { this.codigoRespuesta = codigoRespuesta; }
    public String getMensajeRespuesta() { return mensajeRespuesta; }
    public void setMensajeRespuesta(String mensajeRespuesta) { this.mensajeRespuesta = mensajeRespuesta; }
    public String getProtocoloAutorizacion() { return protocoloAutorizacion; }
    public void setProtocoloAutorizacion(String protocoloAutorizacion) { this.protocoloAutorizacion = protocoloAutorizacion; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
}












