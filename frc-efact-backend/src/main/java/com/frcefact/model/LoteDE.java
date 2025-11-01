package com.frcefact.model;

import com.frcefact.model.base.AuditableEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un Lote de Documentos Electrónicos para envío a SIFEN.
 * Agrupa múltiples DEs para procesamiento en batch.
 */
@Entity
@Table(name = "lote_de", schema = "financiero")
public class LoteDE extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Empresa a la que pertenece el lote
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    /**
     * Estado actual del lote
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", columnDefinition = "financiero.estado_lote_enum", nullable = false)
    private EstadoLoteDE estado = EstadoLoteDE.PENDIENTE;

    /**
     * Protocolo de respuesta de SIFEN
     */
    @Column(name = "protocolo", length = 50)
    private String protocolo;

    /**
     * Respuesta completa de SIFEN en formato JSON o XML
     */
    @Column(name = "respuesta_sifen", columnDefinition = "TEXT")
    private String respuestaSifen;

    /**
     * Fecha en que el lote fue procesado por SIFEN
     */
    @Column(name = "fecha_procesado")
    private LocalDateTime fechaProcesado;

    /**
     * Fecha del último intento de envío
     */
    @Column(name = "fecha_ultimo_intento")
    private LocalDateTime fechaUltimoIntento;

    /**
     * Número de intentos de envío realizados
     */
    @Column(name = "intentos", nullable = false)
    private Integer intentos = 0;

    /**
     * Documentos electrónicos incluidos en este lote
     */
    @OneToMany(mappedBy = "loteDE", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DocumentoElectronico> documentos = new ArrayList<>();

    // Constructors

    public LoteDE() {
    }

    // Getters and Setters

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

    public EstadoLoteDE getEstado() {
        return estado;
    }

    public void setEstado(EstadoLoteDE estado) {
        this.estado = estado;
    }

    public String getProtocolo() {
        return protocolo;
    }

    public void setProtocolo(String protocolo) {
        this.protocolo = protocolo;
    }

    public String getRespuestaSifen() {
        return respuestaSifen;
    }

    public void setRespuestaSifen(String respuestaSifen) {
        this.respuestaSifen = respuestaSifen;
    }

    public LocalDateTime getFechaProcesado() {
        return fechaProcesado;
    }

    public void setFechaProcesado(LocalDateTime fechaProcesado) {
        this.fechaProcesado = fechaProcesado;
    }

    public LocalDateTime getFechaUltimoIntento() {
        return fechaUltimoIntento;
    }

    public void setFechaUltimoIntento(LocalDateTime fechaUltimoIntento) {
        this.fechaUltimoIntento = fechaUltimoIntento;
    }

    public Integer getIntentos() {
        return intentos;
    }

    public void setIntentos(Integer intentos) {
        this.intentos = intentos;
    }

    public List<DocumentoElectronico> getDocumentos() {
        return documentos;
    }

    public void setDocumentos(List<DocumentoElectronico> documentos) {
        this.documentos = documentos;
    }

    // Helper methods

    /**
     * Agrega un documento electrónico al lote
     */
    public void agregarDocumento(DocumentoElectronico documento) {
        documentos.add(documento);
        documento.setLoteDE(this);
    }

    /**
     * Remueve un documento electrónico del lote
     */
    public void removerDocumento(DocumentoElectronico documento) {
        documentos.remove(documento);
        documento.setLoteDE(null);
    }

    /**
     * Incrementa el contador de intentos
     */
    public void incrementarIntentos() {
        this.intentos++;
        this.fechaUltimoIntento = LocalDateTime.now();
    }
}
