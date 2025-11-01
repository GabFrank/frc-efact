package com.frcefact.model;

import com.frcefact.model.base.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entidad Cliente que representa un cliente de una empresa.
 * Mapea a la tabla clientes.cliente.
 */
@Entity
@Table(name = "cliente", schema = "clientes", indexes = {
    @Index(name = "idx_cliente_empresa", columnList = "empresa_id"),
    @Index(name = "idx_cliente_ruc", columnList = "ruc"),
    @Index(name = "idx_cliente_nombre", columnList = "nombre"),
    @Index(name = "idx_cliente_activo", columnList = "activo")
})
public class Cliente extends AuditableEntity {

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

    @Size(max = 200, message = "Razón social no debe exceder 200 caracteres")
    @Column(name = "razon_social", length = 200)
    private String razonSocial;

    @Size(max = 20, message = "RUC no debe exceder 20 caracteres")
    @Column(length = 20)
    private String ruc;

    @Column(columnDefinition = "TEXT")
    private String direccion;

    @Size(max = 50, message = "Teléfono no debe exceder 50 caracteres")
    @Column(length = 50)
    private String telefono;

    @Email(message = "Email debe ser válido")
    @Size(max = 100, message = "Email no debe exceder 100 caracteres")
    @Column(length = 100)
    private String email;

    @Column(nullable = false)
    private Boolean tributa = true;

    @Size(max = 2, message = "Tipo contribuyente debe ser PF, PJ o EG")
    @Column(name = "tipo_contribuyente", length = 2)
    private String tipoContribuyente;

    @Column(nullable = false)
    private Boolean activo = true;

    // Relaciones
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    private Set<FacturaLegal> facturas = new HashSet<>();

    // Constructores
    public Cliente() {
    }

    public Cliente(Empresa empresa, String nombre) {
        this.empresa = empresa;
        this.nombre = nombre;
        this.tributa = true;
        this.activo = true;
    }

    // Métodos de negocio
    public boolean requiereRuc() {
        return tributa != null && tributa;
    }

    public String getNombreCompleto() {
        if (razonSocial != null && !razonSocial.isEmpty()) {
            return razonSocial;
        }
        return nombre;
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

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getTributa() {
        return tributa;
    }

    public void setTributa(Boolean tributa) {
        this.tributa = tributa;
    }

    public String getTipoContribuyente() {
        return tipoContribuyente;
    }

    public void setTipoContribuyente(String tipoContribuyente) {
        this.tipoContribuyente = tipoContribuyente;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Set<FacturaLegal> getFacturas() {
        return facturas;
    }

    public void setFacturas(Set<FacturaLegal> facturas) {
        this.facturas = facturas;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cliente cliente = (Cliente) o;
        return Objects.equals(id, cliente.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Cliente{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", ruc='" + ruc + '\'' +
                ", tributa=" + tributa +
                ", activo=" + activo +
                '}';
    }
}
