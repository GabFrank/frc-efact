package com.frcefact.model;

import com.frcefact.model.base.AuditableEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Credencial de una {@link CuentaServicio}. Mapea a integracion.api_key.
 *
 * <p>La key completa tiene la forma {@code frc_<prefijo>_<secreto>}. Solo se guarda el
 * prefijo (para buscarla) y el SHA-256 del secreto: el secreto en claro se entrega una única
 * vez, al crearla, y no se puede recuperar.
 */
@Entity
@Table(name = "api_key", schema = "integracion")
public class ApiKey extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cuenta_servicio_id", nullable = false)
    private CuentaServicio cuentaServicio;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 16, unique = true)
    private String prefijo;

    @Column(name = "hash_secreto", nullable = false, length = 64)
    private String hashSecreto;

    /** Scopes separados por espacio, como en OAuth2. */
    @Column(nullable = false, length = 500)
    private String scopes;

    @Column(name = "expira_en")
    private LocalDateTime expiraEn;

    @Column(name = "revocada_en")
    private LocalDateTime revocadaEn;

    @Column(name = "revocada_por", length = 50)
    private String revocadaPor;

    @Column(name = "ultimo_uso_en")
    private LocalDateTime ultimoUsoEn;

    @Column(name = "ultimo_uso_ip", length = 64)
    private String ultimoUsoIp;

    /** Vigente = no revocada y no vencida. No mira si la cuenta está activa. */
    public boolean estaVigente(LocalDateTime ahora) {
        return revocadaEn == null && (expiraEn == null || expiraEn.isAfter(ahora));
    }

    public Set<String> getScopesSet() {
        if (scopes == null || scopes.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(scopes.trim().split("\\s+"))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public void setScopesSet(Set<String> valores) {
        this.scopes = String.join(" ", valores);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public CuentaServicio getCuentaServicio() { return cuentaServicio; }
    public void setCuentaServicio(CuentaServicio cuentaServicio) { this.cuentaServicio = cuentaServicio; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getPrefijo() { return prefijo; }
    public void setPrefijo(String prefijo) { this.prefijo = prefijo; }

    public String getHashSecreto() { return hashSecreto; }
    public void setHashSecreto(String hashSecreto) { this.hashSecreto = hashSecreto; }

    public String getScopes() { return scopes; }
    public void setScopes(String scopes) { this.scopes = scopes; }

    public LocalDateTime getExpiraEn() { return expiraEn; }
    public void setExpiraEn(LocalDateTime expiraEn) { this.expiraEn = expiraEn; }

    public LocalDateTime getRevocadaEn() { return revocadaEn; }
    public void setRevocadaEn(LocalDateTime revocadaEn) { this.revocadaEn = revocadaEn; }

    public String getRevocadaPor() { return revocadaPor; }
    public void setRevocadaPor(String revocadaPor) { this.revocadaPor = revocadaPor; }

    public LocalDateTime getUltimoUsoEn() { return ultimoUsoEn; }
    public void setUltimoUsoEn(LocalDateTime ultimoUsoEn) { this.ultimoUsoEn = ultimoUsoEn; }

    public String getUltimoUsoIp() { return ultimoUsoIp; }
    public void setUltimoUsoIp(String ultimoUsoIp) { this.ultimoUsoIp = ultimoUsoIp; }
}
