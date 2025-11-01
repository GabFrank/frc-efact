package com.frcefact.model.base;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Clase base abstracta para entidades auditables.
 * Proporciona campos estándar de auditoría que se aplican automáticamente
 * mediante Spring Data JPA Auditing.
 * 
 * Campos incluidos:
 * - creadoEn: Timestamp de creación
 * - creadoPor: Usuario que creó el registro
 * - actualizadoEn: Timestamp de última modificación
 * - actualizadoPor: Usuario que realizó la última modificación
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity {

    @CreatedDate
    @Column(name = "creado_en", nullable = false, updatable = false)
    protected LocalDateTime creadoEn;

    @CreatedBy
    @Column(name = "creado_por", length = 50, updatable = false)
    protected String creadoPor;

    @LastModifiedDate
    @Column(name = "actualizado_en", nullable = false)
    protected LocalDateTime actualizadoEn;

    @LastModifiedBy
    @Column(name = "actualizado_por", length = 50)
    protected String actualizadoPor;

    // Getters and Setters

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }

    public String getCreadoPor() {
        return creadoPor;
    }

    public void setCreadoPor(String creadoPor) {
        this.creadoPor = creadoPor;
    }

    public LocalDateTime getActualizadoEn() {
        return actualizadoEn;
    }

    public void setActualizadoEn(LocalDateTime actualizadoEn) {
        this.actualizadoEn = actualizadoEn;
    }

    public String getActualizadoPor() {
        return actualizadoPor;
    }

    public void setActualizadoPor(String actualizadoPor) {
        this.actualizadoPor = actualizadoPor;
    }

    @PrePersist
    protected void onCreate() {
        if (creadoEn == null) {
            creadoEn = LocalDateTime.now();
        }
        if (actualizadoEn == null) {
            actualizadoEn = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        actualizadoEn = LocalDateTime.now();
    }
}
