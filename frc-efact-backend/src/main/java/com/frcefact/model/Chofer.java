package com.frcefact.model;

import com.frcefact.model.base.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Entidad Chofer que representa un chofer/conductor de una empresa.
 * Mapea a la tabla transporte.chofer.
 */
@Entity
@Table(name = "chofer", schema = "transporte", indexes = {
    @Index(name = "idx_chofer_empresa", columnList = "empresa_id"),
    @Index(name = "idx_chofer_activo", columnList = "activo"),
    @Index(name = "idx_chofer_nombre", columnList = "nombre")
})
public class Chofer extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Empresa es requerida")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @NotBlank(message = "Nombre es requerido")
    @Size(max = 200, message = "Nombre no debe exceder 200 caracteres")
    @Column(nullable = false, length = 200)
    private String nombre;

    @Size(max = 20, message = "Documento no debe exceder 20 caracteres")
    @Column(length = 20)
    private String documento;

    @Column(name = "direccion", columnDefinition = "TEXT")
    private String direccion;

    @Column(nullable = false)
    private Boolean activo = true;

    public Chofer() {
    }

    public Chofer(Empresa empresa, String nombre, String documento, String direccion) {
        this.empresa = empresa;
        this.nombre = nombre;
        this.documento = documento;
        this.direccion = direccion;
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

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return "Chofer{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", documento='" + documento + '\'' +
                ", activo=" + activo +
                '}';
    }
}
