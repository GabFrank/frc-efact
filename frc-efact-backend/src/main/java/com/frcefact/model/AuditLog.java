package com.frcefact.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Entidad que representa un registro de auditoría en el sistema.
 * Almacena el historial completo de modificaciones para trazabilidad.
 * Esta tabla es inmutable - los registros no pueden ser modificados ni eliminados.
 */
@Entity
@Table(name = "audit_log", schema = "auditoria", indexes = {
    @Index(name = "idx_audit_usuario", columnList = "usuario_id"),
    @Index(name = "idx_audit_empresa", columnList = "empresa_id"),
    @Index(name = "idx_audit_entidad", columnList = "entidad_tipo, entidad_id"),
    @Index(name = "idx_audit_accion", columnList = "accion"),
    @Index(name = "idx_audit_fecha", columnList = "fecha_hora")
})
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Usuario que realizó la acción
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    /**
     * Empresa en la que se realizó la acción (puede ser null para acciones globales)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    /**
     * Tipo de entidad afectada (ej: "Factura", "Cliente", "Producto")
     */
    @Column(name = "entidad_tipo", nullable = false, length = 100)
    private String entidadTipo;

    /**
     * ID de la entidad afectada
     */
    @Column(name = "entidad_id", nullable = false)
    private Long entidadId;

    /**
     * Acción realizada (CREATE, UPDATE, DELETE, READ)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "accion", columnDefinition = "auditoria.accion_enum", nullable = false)
    private AccionEnum accion;

    /**
     * Fecha y hora en que se realizó la acción
     */
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora = LocalDateTime.now();

    /**
     * Valores anteriores del registro (solo para UPDATE)
     * Almacenado como JSON
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "valores_anteriores", columnDefinition = "jsonb")
    private Map<String, Object> valoresAnteriores;

    /**
     * Valores nuevos del registro (para CREATE y UPDATE)
     * Almacenado como JSON
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "valores_nuevos", columnDefinition = "jsonb")
    private Map<String, Object> valoresNuevos;

    /**
     * Dirección IP desde donde se realizó la acción
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * User Agent del navegador/cliente
     */
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    /**
     * Descripción adicional de la acción
     */
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    // Constructors

    public AuditLog() {
    }

    public AuditLog(Usuario usuario, Empresa empresa, String entidadTipo, Long entidadId, AccionEnum accion) {
        this.usuario = usuario;
        this.empresa = empresa;
        this.entidadTipo = entidadTipo;
        this.entidadId = entidadId;
        this.accion = accion;
        this.fechaHora = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
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

    public AccionEnum getAccion() {
        return accion;
    }

    public void setAccion(AccionEnum accion) {
        this.accion = accion;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public Map<String, Object> getValoresAnteriores() {
        return valoresAnteriores;
    }

    public void setValoresAnteriores(Map<String, Object> valoresAnteriores) {
        this.valoresAnteriores = valoresAnteriores;
    }

    public Map<String, Object> getValoresNuevos() {
        return valoresNuevos;
    }

    public void setValoresNuevos(Map<String, Object> valoresNuevos) {
        this.valoresNuevos = valoresNuevos;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        return "AuditLog{" +
                "id=" + id +
                ", entidadTipo='" + entidadTipo + '\'' +
                ", entidadId=" + entidadId +
                ", accion=" + accion +
                ", fechaHora=" + fechaHora +
                '}';
    }
}
