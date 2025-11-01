package com.frcefact.dto;

import com.frcefact.model.AccionEnum;

import java.time.LocalDateTime;

/**
 * DTO para representar una actividad reciente del usuario.
 */
public class ActividadRecienteDto {

    private Long id;
    private AccionEnum accion;
    private String entidadTipo;
    private Long entidadId;
    private String descripcion;
    private LocalDateTime fechaHora;
    private String empresaNombre;

    public ActividadRecienteDto() {
    }

    public ActividadRecienteDto(Long id, AccionEnum accion, String entidadTipo, Long entidadId,
                                String descripcion, LocalDateTime fechaHora, String empresaNombre) {
        this.id = id;
        this.accion = accion;
        this.entidadTipo = entidadTipo;
        this.entidadId = entidadId;
        this.descripcion = descripcion;
        this.fechaHora = fechaHora;
        this.empresaNombre = empresaNombre;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AccionEnum getAccion() {
        return accion;
    }

    public void setAccion(AccionEnum accion) {
        this.accion = accion;
    }

    public String getEntidadTipo() {
        return entidadTipo;
    }

    public void setEntidadTipo(String entidadTipo) {
        this.entidadTipo = entidadTipo;
    }

    public Long getEntidadId() {
        return entidadId;
    }

    public void setEntidadId(Long entidadId) {
        this.entidadId = entidadId;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getEmpresaNombre() {
        return empresaNombre;
    }

    public void setEmpresaNombre(String empresaNombre) {
        this.empresaNombre = empresaNombre;
    }
}
