package com.frcefact.model;

import jakarta.persistence.*;
import java.util.List;

/**
 * Entidad que representa un país en el sistema geográfico de SIFEN.
 */
@Entity
@Table(name = "pais", schema = "geografia")
public class Pais {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", unique = true, nullable = false, length = 3)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @OneToMany(mappedBy = "pais", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Departamento> departamentos;

    // Constructors
    public Pais() {}

    public Pais(String codigo, String nombre) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.activo = true;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public List<Departamento> getDepartamentos() {
        return departamentos;
    }

    public void setDepartamentos(List<Departamento> departamentos) {
        this.departamentos = departamentos;
    }
}