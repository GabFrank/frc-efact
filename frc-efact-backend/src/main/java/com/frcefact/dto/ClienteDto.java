package com.frcefact.dto;

import com.frcefact.validation.ValidClienteRuc;
import com.frcefact.validation.ValidRuc;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para Cliente.
 * Incluye validaciones condicionales para RUC cuando tributa=true.
 * Implementa los campos del Receptor según Manual Técnico SIFEN v1.50.
 */
@ValidClienteRuc
public class ClienteDto {

    private Long id;

    @NotBlank(message = "El nombre es requerido")
    @Size(max = 200, message = "El nombre no debe exceder 200 caracteres")
    private String nombre;

    @NotBlank(message = "La razón social es requerida")
    @Size(max = 200, message = "La razón social no debe exceder 200 caracteres")
    private String razonSocial;

    @ValidRuc(message = "El RUC no tiene un formato válido")
    @Size(max = 20, message = "El RUC no debe exceder 20 caracteres")
    private String ruc;

    private String direccion;

    /**
     * Número de casa (B411 dNumCasRec).
     */
    @Size(max = 50, message = "El número de casa no debe exceder 50 caracteres")
    private String numeroCasa;

    @Size(max = 50, message = "El teléfono no debe exceder 50 caracteres")
    private String telefono;

    /**
     * Celular (B413 dCelRec).
     */
    @Size(max = 50, message = "El celular no debe exceder 50 caracteres")
    private String celular;

    @Email(message = "El email debe ser válido")
    @Size(max = 100, message = "El email no debe exceder 100 caracteres")
    private String email;

    /**
     * Tipo de cliente según SIFEN v1.50.
     * Valores: PERSONA_FISICA, PERSONA_JURIDICA, NO_CONTRIBUYENTE, EXTRANJERO, GUBERNAMENTAL
     */
    private String tipoClienteSifen;

    /**
     * Campo legacy para compatibilidad.
     * @deprecated Usar tipoClienteSifen en su lugar
     */
    @Deprecated
    private Boolean tributa = true;

    /**
     * Campo legacy para compatibilidad.
     * @deprecated Usar tipoClienteSifen en su lugar. Este campo se ignora completamente.
     */
    @Deprecated
    @Pattern(regexp = "^(PF|PJ|EG)?$", message = "El tipo de contribuyente debe ser PF, PJ o EG")
    private String tipoContribuyente;

    /**
     * ID del país (B415 cPaisRec).
     */
    private Long paisId;

    /**
     * ID de la ciudad (B410 cCiuRec).
     */
    private Long ciudadId;

    private Boolean activo = true;

    private Long empresaId;

    // Constructores
    public ClienteDto() {
    }

    public ClienteDto(Long id, String nombre, String razonSocial, String ruc,
                     String direccion, String numeroCasa, String telefono, String celular, String email,
                     String tipoClienteSifen, Boolean tributa, String tipoContribuyente,
                     Long paisId, Long ciudadId, Boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.razonSocial = razonSocial;
        this.ruc = ruc;
        this.direccion = direccion;
        this.numeroCasa = numeroCasa;
        this.telefono = telefono;
        this.celular = celular;
        this.email = email;
        this.tipoClienteSifen = tipoClienteSifen;
        this.tributa = tributa;
        this.tipoContribuyente = tipoContribuyente;
        this.paisId = paisId;
        this.ciudadId = ciudadId;
        this.activo = activo;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
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

    public String getTipoClienteSifen() {
        return tipoClienteSifen;
    }

    public void setTipoClienteSifen(String tipoClienteSifen) {
        this.tipoClienteSifen = tipoClienteSifen;
    }

    public Long getPaisId() {
        return paisId;
    }

    public void setPaisId(Long paisId) {
        this.paisId = paisId;
    }

    public Long getCiudadId() {
        return ciudadId;
    }

    public void setCiudadId(Long ciudadId) {
        this.ciudadId = ciudadId;
    }

    @Override
    public String toString() {
        return "ClienteDto{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", ruc='" + ruc + '\'' +
                ", tipoClienteSifen='" + tipoClienteSifen + '\'' +
                ", tributa=" + tributa +
                ", activo=" + activo +
                '}';
    }
}
