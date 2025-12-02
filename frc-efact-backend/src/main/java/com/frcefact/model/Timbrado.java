package com.frcefact.model;

import com.frcefact.model.base.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entidad Timbrado que representa un timbrado fiscal autorizado por la SET.
 * Mapea a la tabla financiero.timbrado.
 */
@Entity
@Table(name = "timbrado", schema = "financiero", indexes = {
    @Index(name = "idx_timbrado_empresa", columnList = "empresa_id"),
    @Index(name = "idx_timbrado_numero", columnList = "numero"),
    @Index(name = "idx_timbrado_activo", columnList = "activo")
})
public class Timbrado extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Empresa es requerida")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;


    @NotBlank(message = "Número de timbrado es requerido")
    @Size(max = 20, message = "Número de timbrado no debe exceder 20 caracteres")
    @Column(nullable = false, length = 20)
    private String numero;

    @Column(name = "is_electronico", nullable = false)
    private Boolean isElectronico = false;

    @Column(name = "csc_encrypted", columnDefinition = "TEXT")
    private String cscEncrypted;

    @Size(max = 50)
    @Column(name = "csc_id", length = 50)
    private String cscId;

    @NotNull(message = "Fecha de inicio es requerida")
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @NotNull(message = "Fecha de fin es requerida")
    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;


    @Column(nullable = false)
    private Boolean activo = true;

    // Relaciones
    @OneToMany(mappedBy = "timbrado", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TimbradoDetalle> timbradoDetalles = new HashSet<>();

    // Constructores
    public Timbrado() {
    }

    public Timbrado(Empresa empresa, String numero, LocalDate fechaInicio, LocalDate fechaFin) {
        this.empresa = empresa;
        this.numero = numero;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.activo = true;
        this.isElectronico = false;
    }

    // Métodos de negocio
    public boolean isVigente() {
        LocalDate hoy = LocalDate.now();
        return hoy.isAfter(fechaInicio.minusDays(1)) && hoy.isBefore(fechaFin.plusDays(1));
    }

    public boolean isPorVencer(int diasAnticipacion) {
        LocalDate fechaLimite = LocalDate.now().plusDays(diasAnticipacion);
        return fechaFin.isBefore(fechaLimite) && fechaFin.isAfter(LocalDate.now());
    }

    public long getDiasRestantes() {
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), fechaFin);
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }


    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public Boolean getIsElectronico() {
        return isElectronico;
    }

    public void setIsElectronico(Boolean isElectronico) {
        this.isElectronico = isElectronico;
    }

    public String getCscEncrypted() {
        return cscEncrypted;
    }

    public void setCscEncrypted(String cscEncrypted) {
        this.cscEncrypted = cscEncrypted;
    }

    public String getCscId() {
        return cscId;
    }

    public void setCscId(String cscId) {
        this.cscId = cscId;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }


    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Set<TimbradoDetalle> getTimbradoDetalles() {
        return timbradoDetalles;
    }

    public void setTimbradoDetalles(Set<TimbradoDetalle> timbradoDetalles) {
        this.timbradoDetalles = timbradoDetalles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Timbrado timbrado = (Timbrado) o;
        return Objects.equals(id, timbrado.id) && Objects.equals(numero, timbrado.numero);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, numero);
    }

    @Override
    public String toString() {
        return "Timbrado{" +
                "id=" + id +
                ", numero='" + numero + '\'' +
                ", fechaInicio=" + fechaInicio +
                ", fechaFin=" + fechaFin +
                ", isElectronico=" + isElectronico +
                ", activo=" + activo +
                '}';
    }
}
