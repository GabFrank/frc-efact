package com.frcefact.model;

import com.frcefact.model.base.AuditableEntity;
import jakarta.persistence.*;

/**
 * Una llamada a la API de integración. Mapea a integracion.solicitud_api.
 * Sirve de rastro de uso por key y como base para medir/facturar el servicio.
 */
@Entity
@Table(name = "solicitud_api", schema = "integracion")
public class SolicitudApi extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "api_key_id", nullable = false)
    private Long apiKeyId;

    @Column(nullable = false, length = 50)
    private String operacion;

    @Column(length = 100)
    private String recurso;

    @Column(name = "codigo_http", nullable = false)
    private Integer codigoHttp;

    @Column(name = "codigo_sifen", length = 10)
    private String codigoSifen;

    @Column(name = "duracion_ms")
    private Long duracionMs;

    @Column(length = 64)
    private String ip;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getApiKeyId() { return apiKeyId; }
    public void setApiKeyId(Long apiKeyId) { this.apiKeyId = apiKeyId; }

    public String getOperacion() { return operacion; }
    public void setOperacion(String operacion) { this.operacion = operacion; }

    public String getRecurso() { return recurso; }
    public void setRecurso(String recurso) { this.recurso = recurso; }

    public Integer getCodigoHttp() { return codigoHttp; }
    public void setCodigoHttp(Integer codigoHttp) { this.codigoHttp = codigoHttp; }

    public String getCodigoSifen() { return codigoSifen; }
    public void setCodigoSifen(String codigoSifen) { this.codigoSifen = codigoSifen; }

    public Long getDuracionMs() { return duracionMs; }
    public void setDuracionMs(Long duracionMs) { this.duracionMs = duracionMs; }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
}
