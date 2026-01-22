package com.frcefact.model;

import com.frcefact.model.base.AuditableEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa un Evento de Nominación de Receptor de un Documento Electrónico.
 * Se utiliza para nominar un cliente como receptor de un DE que fue emitido sin receptor (innominado).
 */
@Entity
@Table(name = "evento_nominacion_de", schema = "financiero")
public class EventoNominacionDE extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Documento electrónico que se desea nominar
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_electronico_id", nullable = false)
    private DocumentoElectronico documentoElectronico;

    /**
     * Cliente nominado como receptor
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    /**
     * Identificador único del evento de nominación
     */
    @Column(name = "evento_id", length = 50, unique = true, nullable = false)
    private String eventoId;

    /**
     * Fecha y hora de firma del evento
     */
    @Column(name = "fecha_firma", nullable = false)
    private LocalDateTime fechaFirma;

    /**
     * CDC del documento que se está nominando
     */
    @Column(name = "cdc_documento", length = 44, nullable = false)
    private String cdcDocumento;

    /**
     * Nombre del receptor nominado
     */
    @Column(name = "nombre_receptor", length = 200)
    private String nombreReceptor;

    /**
     * Documento del receptor nominado (RUC o documento de identidad)
     */
    @Column(name = "documento_receptor", length = 50)
    private String documentoReceptor;

    /**
     * Tipo de receptor: CONTRIBUYENTE o NO_CONTRIBUYENTE
     */
    @Column(name = "tipo_receptor", length = 50)
    private String tipoReceptor;

    /**
     * Total de la factura nominada
     */
    @Column(name = "total_factura", precision = 15, scale = 2)
    private BigDecimal totalFactura;

    /**
     * Fecha de emisión de la factura
     */
    @Column(name = "fecha_emision")
    private LocalDateTime fechaEmision;

    /**
     * Fecha de recepción por el receptor
     */
    @Column(name = "fecha_recepcion")
    private LocalDateTime fechaRecepcion;

    /**
     * XML del evento de nominación firmado
     */
    @Column(name = "xml_evento", columnDefinition = "TEXT")
    private String xmlEvento;

    /**
     * Estado actual del evento
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 50, nullable = false)
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

    public EventoNominacionDE() {
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

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
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

    public String getNombreReceptor() {
        return nombreReceptor;
    }

    public void setNombreReceptor(String nombreReceptor) {
        this.nombreReceptor = nombreReceptor;
    }

    public String getDocumentoReceptor() {
        return documentoReceptor;
    }

    public void setDocumentoReceptor(String documentoReceptor) {
        this.documentoReceptor = documentoReceptor;
    }

    public String getTipoReceptor() {
        return tipoReceptor;
    }

    public void setTipoReceptor(String tipoReceptor) {
        this.tipoReceptor = tipoReceptor;
    }

    public BigDecimal getTotalFactura() {
        return totalFactura;
    }

    public void setTotalFactura(BigDecimal totalFactura) {
        this.totalFactura = totalFactura;
    }

    public LocalDateTime getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(LocalDateTime fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public LocalDateTime getFechaRecepcion() {
        return fechaRecepcion;
    }

    public void setFechaRecepcion(LocalDateTime fechaRecepcion) {
        this.fechaRecepcion = fechaRecepcion;
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













