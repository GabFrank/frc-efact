package com.frcefact.model;

import com.frcefact.model.base.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Entidad Vehiculo que representa un vehículo de una empresa.
 * Mapea a la tabla transporte.vehiculo.
 */
@Entity
@Table(name = "vehiculo", schema = "transporte", indexes = {
    @Index(name = "idx_vehiculo_empresa", columnList = "empresa_id"),
    @Index(name = "idx_vehiculo_activo", columnList = "activo"),
    @Index(name = "idx_vehiculo_empresa_matricula", columnList = "empresa_id, matricula")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_vehiculo_empresa_matricula", 
        columnNames = {"empresa_id", "matricula"})
})
public class Vehiculo extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Empresa es requerida")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @NotBlank(message = "Marca es requerida")
    @Size(max = 100, message = "Marca no debe exceder 100 caracteres")
    @Column(nullable = false, length = 100)
    private String marca;

    @NotBlank(message = "Matrícula es requerida")
    @Size(max = 20, message = "Matrícula no debe exceder 20 caracteres")
    @Column(nullable = false, length = 20)
    private String matricula;

    @Column(nullable = false)
    private Boolean activo = true;

    public Vehiculo() {
    }

    public Vehiculo(Empresa empresa, String marca, String matricula) {
        this.empresa = empresa;
        this.marca = marca;
        this.matricula = matricula;
        this.activo = true;
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

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return "Vehiculo{" +
                "id=" + id +
                ", marca='" + marca + '\'' +
                ", matricula='" + matricula + '\'' +
                ", activo=" + activo +
                '}';
    }
}
