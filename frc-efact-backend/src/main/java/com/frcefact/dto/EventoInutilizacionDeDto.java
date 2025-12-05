package com.frcefact.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.frcefact.model.EventoInutilizacionDE;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventoInutilizacionDeDto {
    private Long id;
    private Long timbradoId;
    private Long timbradoDetalleId;
    private String eventoId;
    private String estado;
    private String codigoRespuesta;
    private String mensajeRespuesta;
    private String protocoloAutorizacion;
    private LocalDateTime fechaFirma;
    private LocalDateTime fechaProcesamiento;
    private String establecimiento;
    private String puntoExpedicion;
    private Integer numeroInicio;
    private Integer numeroFin;
    private String tipoDE;
    private String motivoInutilizacion;

    public static EventoInutilizacionDeDto fromEntity(EventoInutilizacionDE evento) {
        EventoInutilizacionDeDto dto = new EventoInutilizacionDeDto();
        dto.setId(evento.getId());
        dto.setTimbradoId(evento.getTimbrado() != null ? evento.getTimbrado().getId() : null);
        dto.setTimbradoDetalleId(evento.getTimbradoDetalle() != null ? evento.getTimbradoDetalle().getId() : null);
        dto.setEventoId(evento.getEventoId());
        dto.setEstado(evento.getEstado() != null ? evento.getEstado().name() : null);
        dto.setCodigoRespuesta(evento.getCodigoRespuesta());
        dto.setMensajeRespuesta(evento.getMensajeRespuesta());
        dto.setProtocoloAutorizacion(evento.getProtocoloAutorizacion());
        dto.setFechaFirma(evento.getFechaFirma());
        dto.setFechaProcesamiento(evento.getFechaProcesamiento());
        dto.setEstablecimiento(evento.getEstablecimiento());
        dto.setPuntoExpedicion(evento.getPuntoExpedicion());
        dto.setNumeroInicio(evento.getNumeroInicio());
        dto.setNumeroFin(evento.getNumeroFin());
        dto.setTipoDE(evento.getTipoDE());
        dto.setMotivoInutilizacion(evento.getMotivoInutilizacion());
        return dto;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTimbradoId() {
        return timbradoId;
    }

    public void setTimbradoId(Long timbradoId) {
        this.timbradoId = timbradoId;
    }

    public Long getTimbradoDetalleId() {
        return timbradoDetalleId;
    }

    public void setTimbradoDetalleId(Long timbradoDetalleId) {
        this.timbradoDetalleId = timbradoDetalleId;
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

    public String getEstablecimiento() {
        return establecimiento;
    }

    public void setEstablecimiento(String establecimiento) {
        this.establecimiento = establecimiento;
    }

    public String getPuntoExpedicion() {
        return puntoExpedicion;
    }

    public void setPuntoExpedicion(String puntoExpedicion) {
        this.puntoExpedicion = puntoExpedicion;
    }

    public Integer getNumeroInicio() {
        return numeroInicio;
    }

    public void setNumeroInicio(Integer numeroInicio) {
        this.numeroInicio = numeroInicio;
    }

    public Integer getNumeroFin() {
        return numeroFin;
    }

    public void setNumeroFin(Integer numeroFin) {
        this.numeroFin = numeroFin;
    }

    public String getTipoDE() {
        return tipoDE;
    }

    public void setTipoDE(String tipoDE) {
        this.tipoDE = tipoDE;
    }

    public String getMotivoInutilizacion() {
        return motivoInutilizacion;
    }

    public void setMotivoInutilizacion(String motivoInutilizacion) {
        this.motivoInutilizacion = motivoInutilizacion;
    }
}









