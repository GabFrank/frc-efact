package com.frcefact.dto.integracion;

import com.frcefact.model.CuentaServicio;

import java.time.LocalDateTime;

public record CuentaServicioDto(
        Long id,
        String nombre,
        String ruc,
        String emailContacto,
        Long empresaId,
        String empresaRazonSocial,
        Boolean activa,
        String observacion,
        LocalDateTime creadoEn,
        String creadoPor) {

    public static CuentaServicioDto desde(CuentaServicio c) {
        return new CuentaServicioDto(c.getId(), c.getNombre(), c.getRuc(), c.getEmailContacto(),
                c.getEmpresa() != null ? c.getEmpresa().getId() : null,
                c.getEmpresa() != null ? c.getEmpresa().getRazonSocial() : null,
                c.getActiva(), c.getObservacion(), c.getCreadoEn(), c.getCreadoPor());
    }
}
