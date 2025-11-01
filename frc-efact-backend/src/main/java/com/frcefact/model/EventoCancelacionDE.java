package com.frcefact.model;

import com.frcefact.model.base.AuditableEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entidad que representa un Evento de Cancelación de un Documento Electrónico.
 * Se utiliza para solicitar la anulación de un DE ya aprobado por SIFEN.
 */
@Entity
@Table(name = "evento_cancelacion_de", schema = "financiero")
public class EventoCancelacionDE extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Documento electrónico que se desea cancelar
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_electronico_id", nullable = false)
    private DocumentoElectronico documentoElectronico;

    /**
     * Identificador único del evento de cancelación
     */
    @Column(name = "evento_id", length = 50, unique = true, nullable = false)
    private String eventoId;

    /**
     * Fecha y hora de firma del evento
     */
    @Column(name = "fecha_firma", nullable = false)
    private LocalDateTime fechaFirma;

    /**
     * CDC del documento que se está cancelando
     */
    @Column(name = "cdc_documento", length = 44, nullable = false)
    private String cdcDocumento;

    /**
     * Motivo de la cancelación (requerido por SIFEN)
     */
    @Column(name = "motivo_cancelacion", columnDefinition = "TEXT", nullable = false)
    private String motivoCancelacion;

    /**
     * XML del evento de cancelación firmado
     */
    @Column(name = "xml_evento", columnDefinition = "TEXT")
    private String xmlEvento;

    /**
     * Estado actual del evento
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", columnDefinition = "financiero.estado_evento_enum", nullable = false)
    private EstadoEvento estado = EstadoEvento.PENDIENTE;

    /**
     * Fecha en que SIFEN procesó el evento
     */
    @Column(name = "fecha_procesamiento")
    private LocalDateTime fechaProcesamiento;

    /**
     * Protocolo de autorización de SIFEN
     */
    @Column(name = "protocolo_autorizacion", length = 50)
    private String protocoloAutorizacion;

    /**
     * Código de respuesta de SIFEN
     */
    @Column(name = "codigo_respuesta", length = 10)
    private String codigoRespuesta;

    /**
     * Mensaje de respuesta de SIFEN
     */
    @Column(name = "mensaje_respuesta", columnDefinition = "TEXT")
    private String mensajeRespuesta;

    /**
     * Respuesta completa de SIFEN en formato original
     */
    @Column(name = "respuesta_bruta", columnDefinition = "TEXT")
    private String respuestaBruta;

    /**
     * Indica si el evento está activo
     */
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    // Constructors

    public EventoCancelacionDE() {
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DocumentoElectronico getDocumentoElectronico() {
        return documentoElectronico;
    }

    public void setDocumentoElectronico(DocumentoElectronico documentoElectronico) {
        this.documentoElectronico = documentoElectronico;
    }

    public String getEventoId() {
        return eventoId;
    }

    public void setEventoId(String eventoId) {
        this.eventoId = eventoId;
    }

    public LocalDateTime getFechaFirma() {
        return fechaFirma;
    }

    public void setFechaFirma(LocalDateTime fechaFirma) {
        this.fechaFirma = fechaFirma;
    }

    public String getCdcDocumento() {
        return cdcDocumento;
    }

    public void setCdcDocumento(String cdcDocumento) {
        this.cdcDocumento = cdcDocumento;
    }

    public String getMotivoCancelacion() {
        return motivoCancelacion;
    }

    public void setMotivoCancelacion(String motivoCancelacion) {
        this.motivoCancelacion = motivoCancelacion;
    }

    public String getXmlEvento() {
        return xmlEvento;
    }

    public void setXmlEvento(String xmlEvento) {
        this.xmlEvento = xmlEvento;
    }

    public EstadoEvento getEstado() {
        return estado;
    }

    public void setEstado(EstadoEvento estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaProcesamiento() {
        return fechaProcesamiento;
    }

    public void setFechaProcesamiento(LocalDateTime fechaProcesamiento) {
        this.fechaProcesamiento = fechaProcesamiento;
    }

    public String getProtocoloAutorizacion() {
        return protocoloAutorizacion;
    }

    public void setProtocoloAutorizacion(String protocoloAutorizacion) {
        this.protocoloAutorizacion = protocoloAutorizacion;
    }

    public String getCodigoRespuesta() {
        return codigoRespuesta;
    }

    public void setCodigoRespuesta(String codigoRespuesta) {
        this.codigoRespuesta = codigoRespuesta;
    }

    public String getMensajeRespuesta() {
        return mensajeRespuesta;
    }

    public void setMensajeRespuesta(String mensajeRespuesta) {
        this.mensajeRespuesta = mensajeRespuesta;
    }

    public String getRespuestaBruta() {
        return respuestaBruta;
    }

    public void setRespuestaBruta(String respuestaBruta) {
        this.respuestaBruta = respuestaBruta;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
