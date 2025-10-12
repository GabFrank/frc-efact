package com.frcefact.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para el dashboard de usuario.
 * Contiene métricas personales y actividad reciente del usuario.
 */
public class DashboardUsuarioDto {

    private Long usuarioId;
    private String nombreCompleto;
    private Integer cantidadEmpresas;
    private LocalDateTime ultimoAcceso;
    private Long facturasCreadasMesActual;
    private List<ActividadRecienteDto> ultimasActividades;

    public DashboardUsuarioDto() {
    }

    public DashboardUsuarioDto(Long usuarioId, String nombreCompleto, Integer cantidadEmpresas,
                               LocalDateTime ultimoAcceso, Long facturasCreadasMesActual,
                               List<ActividadRecienteDto> ultimasActividades) {
        this.usuarioId = usuarioId;
        this.nombreCompleto = nombreCompleto;
        this.cantidadEmpresas = cantidadEmpresas;
        this.ultimoAcceso = ultimoAcceso;
        this.facturasCreadasMesActual = facturasCreadasMesActual;
        this.ultimasActividades = ultimasActividades;
    }

    // Getters and Setters

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public Integer getCantidadEmpresas() {
        return cantidadEmpresas;
    }

    public void setCantidadEmpresas(Integer cantidadEmpresas) {
        this.cantidadEmpresas = cantidadEmpresas;
    }

    public LocalDateTime getUltimoAcceso() {
        return ultimoAcceso;
    }

    public void setUltimoAcceso(LocalDateTime ultimoAcceso) {
        this.ultimoAcceso = ultimoAcceso;
    }

    public Long getFacturasCreadasMesActual() {
        return facturasCreadasMesActual;
    }

    public void setFacturasCreadasMesActual(Long facturasCreadasMesActual) {
        this.facturasCreadasMesActual = facturasCreadasMesActual;
    }

    public List<ActividadRecienteDto> getUltimasActividades() {
        return ultimasActividades;
    }

    public void setUltimasActividades(List<ActividadRecienteDto> ultimasActividades) {
        this.ultimasActividades = ultimasActividades;
    }
}
