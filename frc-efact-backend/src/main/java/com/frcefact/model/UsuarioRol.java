package com.frcefact.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad UsuarioRol que representa la relación many-to-many entre Usuario y Rol.
 * Mapea a la tabla persona.usuario_rol
 */
@Entity
@Table(name = "usuario_rol", schema = "persona", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "rol_id"}),
       indexes = {
           @Index(name = "idx_usuario_rol_usuario", columnList = "usuario_id"),
           @Index(name = "idx_usuario_rol_rol", columnList = "rol_id")
       })
public class UsuarioRol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @PrePersist
    protected void onCreate() {
        creadoEn = LocalDateTime.now();
    }

    // Constructores
    public UsuarioRol() {
    }

    public UsuarioRol(Usuario usuario, Rol rol) {
        this.usuario = usuario;
        this.rol = rol;
    }

    // Getters y Setters
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

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UsuarioRol that = (UsuarioRol) o;
        return Objects.equals(usuario, that.usuario) && Objects.equals(rol, that.rol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usuario, rol);
    }

    @Override
    public String toString() {
        return "UsuarioRol{" +
                "id=" + id +
                ", usuario=" + (usuario != null ? usuario.getUsername() : null) +
                ", rol=" + (rol != null ? rol.getNombre() : null) +
                ", creadoEn=" + creadoEn +
                '}';
    }
}
