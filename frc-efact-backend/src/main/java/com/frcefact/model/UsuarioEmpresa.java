package com.frcefact.model;

import com.frcefact.model.base.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

/**
 * Entidad UsuarioEmpresa que representa la relación entre Usuario y Empresa
 * con rol específico por empresa (ADMINISTRADOR o LECTOR).
 * Mapea a la tabla empresa.usuario_empresa
 */
@Entity
@Table(name = "usuario_empresa", schema = "empresa",
       uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "empresa_id"}),
       indexes = {
           @Index(name = "idx_usuario_empresa_usuario", columnList = "usuario_id"),
           @Index(name = "idx_usuario_empresa_empresa", columnList = "empresa_id"),
           @Index(name = "idx_usuario_empresa_activo", columnList = "activo")
       })
public class UsuarioEmpresa extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @NotNull(message = "Usuario es requerido")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @NotNull(message = "Empresa es requerida")
    private Empresa empresa;

    @Column(name = "rol_empresa", nullable = false, length = 20)
    private String rolEmpresa; // ADMINISTRADOR o LECTOR

    @Column(nullable = false)
    private Boolean activo = true;

    // Constructores
    public UsuarioEmpresa() {
    }

    public UsuarioEmpresa(Usuario usuario, Empresa empresa, String rolEmpresa) {
        this.usuario = usuario;
        this.empresa = empresa;
        this.rolEmpresa = rolEmpresa;
        this.activo = true;
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

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }

    public String getRolEmpresa() {
        return rolEmpresa;
    }

    public void setRolEmpresa(String rolEmpresa) {
        this.rolEmpresa = rolEmpresa;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UsuarioEmpresa that = (UsuarioEmpresa) o;
        return Objects.equals(usuario, that.usuario) && Objects.equals(empresa, that.empresa);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usuario, empresa);
    }

    @Override
    public String toString() {
        return "UsuarioEmpresa{" +
                "id=" + id +
                ", usuario=" + (usuario != null ? usuario.getUsername() : null) +
                ", empresa=" + (empresa != null ? empresa.getId() : null) +
                ", rolEmpresa='" + rolEmpresa + '\'' +
                ", activo=" + activo +
                '}';
    }
}
