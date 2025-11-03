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
 * 
 * Esta entidad implementa los campos del Receptor según Manual Técnico SIFEN v1.50:
 * - Bloque B400-B499: Datos del receptor
 * - B401 iNatRec: Naturaleza del receptor
 * - B402 iTiOpe: Tipo de operación
 * - B403 iTiContRec: Tipo de contribuyente
 * - B404 dRucRec: RUC del receptor (incluye dígito verificador en formato 99999999-9)
 * - B405 dDVRec: Dígito verificador del RUC (derivado del RUC, no se almacena por separado)
 * - B406 dNomRec: Nombre o razón social
 * - B407 dDirRec: Dirección
 * - B408-B410: Códigos geográficos (Departamento, Distrito, Ciudad)
 * - B411 dNumCasRec: Número de casa
 * - B412 dTelRec: Teléfono
 * - B413 dCelRec: Celular
 * - B414 dEmailRec: Correo electrónico
 * - B415 cPaisRec: País del receptor
 */
@Entity
@Table(name = "cliente", schema = "clientes", indexes = {
    @Index(name = "idx_cliente_empresa", columnList = "empresa_id"),
    @Index(name = "idx_cliente_ruc", columnList = "ruc"),
    @Index(name = "idx_cliente_nombre", columnList = "nombre"),
    @Index(name = "idx_cliente_activo", columnList = "activo"),
    @Index(name = "idx_cliente_ciudad", columnList = "ciudad_id"),
    @Index(name = "idx_cliente_pais", columnList = "pais_id")
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

    /**
     * RUC del receptor (B404 dRucRec).
     * Formato: 99999999-9 (el dígito verificador está incluido en el RUC).
     * Obligatorio si el cliente es contribuyente.
     */
    @Size(max = 20, message = "RUC no debe exceder 20 caracteres")
    @Column(length = 20)
    private String ruc;

    @Column(columnDefinition = "TEXT")
    private String direccion;

    /**
     * Número de casa (B411 dNumCasRec).
     */
    @Size(max = 50, message = "Número de casa no debe exceder 50 caracteres")
    @Column(name = "numero_casa", length = 50)
    private String numeroCasa;

    @Size(max = 50, message = "Teléfono no debe exceder 50 caracteres")
    @Column(length = 50)
    private String telefono;

    /**
     * Celular (B413 dCelRec).
     */
    @Size(max = 50, message = "Celular no debe exceder 50 caracteres")
    @Column(length = 50)
    private String celular;

    @Email(message = "Email debe ser válido")
    @Size(max = 100, message = "Email no debe exceder 100 caracteres")
    @Column(length = 100)
    private String email;

    /**
     * País del receptor (B415 cPaisRec).
     * Relación con la entidad Pais para obtener código ISO.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pais_id")
    private Pais pais;

    /**
     * Ciudad del receptor (B410 cCiuRec).
     * A través de esta relación se puede acceder a Distrito y Departamento.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ciudad_id")
    private Ciudad ciudad;

    /**
     * Tipo de cliente según SIFEN v1.50.
     * Combina naturaleza del receptor, tipo de contribuyente y tipo de operación.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cliente_sifen", length = 50)
    private TipoClienteSifen tipoClienteSifen;

    /**
     * Campo legacy para compatibilidad.
     * Se mantiene para migración gradual.
     * @deprecated Usar tipoClienteSifen en su lugar
     */
    @Deprecated
    @Column(nullable = false)
    private Boolean tributa = true;

    /**
     * Campo legacy para compatibilidad.
     * Se mantiene para migración gradual.
     * @deprecated Usar tipoClienteSifen en su lugar
     */
    @Deprecated
    @Size(max = 2, message = "Tipo contribuyente debe ser PF, PJ, EX, EG")
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

    /**
     * Indica si el cliente requiere RUC (incluye dígito verificador).
     * Según SIFEN v1.50, los contribuyentes deben tener RUC en formato 99999999-9.
     */
    public boolean requiereRuc() {
        if (tipoClienteSifen != null) {
            return tipoClienteSifen.requiereRuc();
        }
        // Fallback a campo legacy
        return tributa != null && tributa;
    }

    /**
     * Obtiene el nombre completo del cliente.
     * Prioriza razón social si existe, sino nombre.
     */
    public String getNombreCompleto() {
        if (razonSocial != null && !razonSocial.isEmpty()) {
            return razonSocial;
        }
        return nombre;
    }

    /**
     * Obtiene la naturaleza del receptor según SIFEN (B401 iNatRec).
     * 1 = Contribuyente, 2 = No contribuyente
     */
    public Integer getNaturalezaReceptor() {
        if (tipoClienteSifen != null) {
            return tipoClienteSifen.getNaturalezaReceptor();
        }
        // Fallback: si tributa es true, es contribuyente (1), sino no contribuyente (2)
        return (tributa != null && tributa) ? 1 : 2;
    }

    /**
     * Obtiene el tipo de operación según SIFEN (B402 iTiOpe).
     * 1 = B2B, 2 = B2C, 3 = B2G, 4 = B2F
     */
    public Integer getTipoOperacion() {
        if (tipoClienteSifen != null) {
            return tipoClienteSifen.getTipoOperacion();
        }
        // Fallback basado en tipoContribuyente legacy
        if (tipoContribuyente != null) {
            if ("EG".equals(tipoContribuyente)) {
                return 3; // B2G
            } else if ("PF".equals(tipoContribuyente) || "PJ".equals(tipoContribuyente)) {
                return 1; // B2B
            }
        }
        return 2; // B2C por defecto
    }

    /**
     * Obtiene el tipo de contribuyente según SIFEN (B403 iTiContRec).
     * 1 = Persona física, 2 = Persona jurídica, null = No aplica
     */
    public Integer getTipoContribuyenteCodigo() {
        if (tipoClienteSifen != null) {
            return tipoClienteSifen.getTipoContribuyente();
        }
        // Fallback desde tipoContribuyente legacy
        if (tipoContribuyente != null) {
            if ("PF".equals(tipoContribuyente)) {
                return 1;
            } else if ("PJ".equals(tipoContribuyente) || "EG".equals(tipoContribuyente)) {
                return 2;
            }
        }
        return null;
    }

    /**
     * Obtiene el código del país según SIFEN (B415 cPaisRec).
     */
    public String getCodigoPais() {
        if (pais != null) {
            return pais.getCodigo();
        }
        return "PY"; // Paraguay por defecto
    }

    /**
     * Obtiene el código de ciudad según SIFEN (B410 cCiuRec).
     */
    public String getCodigoCiudad() {
        if (ciudad != null) {
            return ciudad.getCodigo();
        }
        return null;
    }

    /**
     * Obtiene el código de distrito según SIFEN (B409 cDisRec).
     */
    public String getCodigoDistrito() {
        if (ciudad != null && ciudad.getDistrito() != null) {
            return ciudad.getDistrito().getCodigo();
        }
        return null;
    }

    /**
     * Obtiene el código de departamento según SIFEN (B408 cDepRec).
     */
    public String getCodigoDepartamento() {
        if (ciudad != null && ciudad.getDistrito() != null 
            && ciudad.getDistrito().getDepartamento() != null) {
            return ciudad.getDistrito().getDepartamento().getCodigo();
        }
        return null;
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

    /**
     * Obtiene el dígito verificador del RUC.
     * Extrae el dígito verificador del RUC en formato 99999999-9.
     * @return Dígito verificador o null si el RUC no tiene formato válido
     */
    public String getDigitoVerificador() {
        if (ruc == null || ruc.isEmpty()) {
            return null;
        }
        // El RUC puede venir en formato "12345678-9" o solo "12345678"
        if (ruc.contains("-")) {
            String[] partes = ruc.split("-");
            return partes.length > 1 ? partes[1] : null;
        }
        return null;
    }

    public String getNumeroCasa() {
        return numeroCasa;
    }

    public void setNumeroCasa(String numeroCasa) {
        this.numeroCasa = numeroCasa;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }

    public Pais getPais() {
        return pais;
    }

    public void setPais(Pais pais) {
        this.pais = pais;
    }

    public Ciudad getCiudad() {
        return ciudad;
    }

    public void setCiudad(Ciudad ciudad) {
        this.ciudad = ciudad;
    }

    public TipoClienteSifen getTipoClienteSifen() {
        return tipoClienteSifen;
    }

    public void setTipoClienteSifen(TipoClienteSifen tipoClienteSifen) {
        this.tipoClienteSifen = tipoClienteSifen;
        // Sincronizar campos legacy para compatibilidad
        if (tipoClienteSifen != null) {
            this.tributa = tipoClienteSifen.esContribuyente();
            if (tipoClienteSifen.getTipoContribuyente() != null) {
                if (tipoClienteSifen.getTipoContribuyente() == 1) {
                    this.tipoContribuyente = "PF";
                } else if (tipoClienteSifen.getTipoContribuyente() == 2) {
                    this.tipoContribuyente = tipoClienteSifen == TipoClienteSifen.GUBERNAMENTAL ? "EG" : "PJ";
                }
            } else {
                this.tipoContribuyente = null;
            }
        }
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
