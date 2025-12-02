package com.frcefact.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.frcefact.model.LoteDE;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoteDeDto {
    private Long id;
    private Long empresaId;
    private String estado;
    private String protocolo;
    private String codigoRespuesta;
    private String mensajeRespuesta;
    private String respuestaSifen;
    private Integer intentos;
    private LocalDateTime fechaUltimoIntento;
    private LocalDateTime fechaProcesado;

    public static LoteDeDto fromEntity(LoteDE lote) {
        LoteDeDto dto = new LoteDeDto();
        dto.setId(lote.getId());
        dto.setEmpresaId(lote.getEmpresa() != null ? lote.getEmpresa().getId() : null);
        dto.setEstado(lote.getEstado() != null ? lote.getEstado().name() : null);
        dto.setProtocolo(lote.getProtocolo());
        dto.setCodigoRespuesta(lote.getCodigoRespuesta());
        dto.setMensajeRespuesta(lote.getMensajeRespuesta());
        dto.setRespuestaSifen(lote.getRespuestaSifen());
        dto.setIntentos(lote.getIntentos());
        dto.setFechaUltimoIntento(lote.getFechaUltimoIntento());
        dto.setFechaProcesado(lote.getFechaProcesado());
        return dto;
    }

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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getProtocolo() {
        return protocolo;
    }

    public void setProtocolo(String protocolo) {
        this.protocolo = protocolo;
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

    public String getRespuestaSifen() {
        return respuestaSifen;
    }

    public void setRespuestaSifen(String respuestaSifen) {
        this.respuestaSifen = respuestaSifen;
    }

    public Integer getIntentos() {
        return intentos;
    }

    public void setIntentos(Integer intentos) {
        this.intentos = intentos;
    }

    public LocalDateTime getFechaUltimoIntento() {
        return fechaUltimoIntento;
    }

    public void setFechaUltimoIntento(LocalDateTime fechaUltimoIntento) {
        this.fechaUltimoIntento = fechaUltimoIntento;
    }

    public LocalDateTime getFechaProcesado() {
        return fechaProcesado;
    }

    public void setFechaProcesado(LocalDateTime fechaProcesado) {
        this.fechaProcesado = fechaProcesado;
    }
}








