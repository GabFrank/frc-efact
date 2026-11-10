package com.frcefact.model;

import com.frcefact.model.base.AuditableEntity;
import jakarta.persistence.*;

/**
 * Cliente de FRC eFact que consume la API de integración (otro sistema).
 * Mapea a la tabla integracion.cuenta_servicio.
 *
 * <p>La empresa es opcional: un consultor que solo consulta DE en SIFEN no tiene empresa en
 * eFact. Cuando la tiene y la empresa tiene certificado, las consultas a SIFEN salen con ese
 * certificado; si no, con el de la empresa de plataforma (ver {@code integracion.empresa-plataforma-id}).
 */
@Entity
@Table(name = "cuenta_servicio", schema = "integracion")
public class CuentaServicio extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(length = 20)
    private String ruc;

    @Column(name = "email_contacto", length = 150)
    private String emailContacto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    @Column(nullable = false)
    private Boolean activa = true;

    @Column(columnDefinition = "TEXT")
    private String observacion;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getRuc() { return ruc; }
    public void setRuc(String ruc) { this.ruc = ruc; }

    public String getEmailContacto() { return emailContacto; }
    public void setEmailContacto(String emailContacto) { this.emailContacto = emailContacto; }

    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }

    public Boolean getActiva() { return activa; }
    public void setActiva(Boolean activa) { this.activa = activa; }

    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
}
