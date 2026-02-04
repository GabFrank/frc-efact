package com.frcefact.model;

import com.frcefact.model.base.AuditableEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entidad que representa un Evento de Inutilización de Numeración de Documentos Electrónicos.
 * Se utiliza para inutilizar un rango de números de documentos que no se utilizarán.
 */
@Entity
@Table(name = "evento_inutilizacion_de", schema = "financiero")
public class EventoInutilizacionDE extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Timbrado al que pertenece la numeración a inutilizar
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timbrado_id", nullable = false)
    private Timbrado timbrado;

    /**
     * Timbrado detalle (punto de expedición) al que pertenece la numeración
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timbrado_detalle_id")
    private TimbradoDetalle timbradoDetalle;

    /**
     * Identificador único del evento de inutilización
     */
    @Column(name = "evento_id", length = 50, unique = true, nullable = false)
    private String eventoId;

    /**
     * Fecha y hora de firma del evento
     */
    @Column(name = "fecha_firma", nullable = false)
    private LocalDateTime fechaFirma;

    /**
     * Establecimiento (ej: "001")
     */
    @Column(name = "establecimiento", length = 10, nullable = false)
    private String establecimiento;

    /**
     * Punto de expedición
     */
    @Column(name = "punto_expedicion", length = 10, nullable = false)
    private String puntoExpedicion;

    /**
     * Número inicial del rango a inutilizar
     */
    @Column(name = "numero_inicio", nullable = false)
    private Integer numeroInicio;

    /**
     * Número final del rango a inutilizar
     */
    @Column(name = "numero_fin", nullable = false)
    private Integer numeroFin;

    /**
     * Tipo de documento electrónico (ej: "FACTURA_ELECTRONICA")
     */
    @Column(name = "tipo_de", length = 50, nullable = false)
    private String tipoDE;

    /**
     * Motivo de la inutilización
     */
    @Column(name = "motivo_inutilizacion", columnDefinition = "TEXT", nullable = false)
    private String motivoInutilizacion;

    /**
     * XML del evento de inutilización firmado
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

    public EventoInutilizacionDE() {
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Timbrado getTimbrado() {
        return timbrado;
    }

    public void setTimbrado(Timbrado timbrado) {
        this.timbrado = timbrado;
    }

    public TimbradoDetalle getTimbradoDetalle() {
        return timbradoDetalle;
    }

    public void setTimbradoDetalle(TimbradoDetalle timbradoDetalle) {
        this.timbradoDetalle = timbradoDetalle;
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

    public String getEstablecimiento() {
        return establecimiento;
    }

    public void setEstablecimiento(String establecimiento) {
        this.establecimiento = establecimiento;
    }

    public String getPuntoExpedicion() {
        return puntoExpedicion;
    }

    public void setPuntoExpedicion(String puntoExpedicion) {
        this.puntoExpedicion = puntoExpedicion;
    }

    public Integer getNumeroInicio() {
        return numeroInicio;
    }

    public void setNumeroInicio(Integer numeroInicio) {
        this.numeroInicio = numeroInicio;
    }

    public Integer getNumeroFin() {
        return numeroFin;
    }

    public void setNumeroFin(Integer numeroFin) {
        this.numeroFin = numeroFin;
    }

    public String getTipoDE() {
        return tipoDE;
    }

    public void setTipoDE(String tipoDE) {
        this.tipoDE = tipoDE;
    }

    public String getMotivoInutilizacion() {
        return motivoInutilizacion;
    }

    public void setMotivoInutilizacion(String motivoInutilizacion) {
        this.motivoInutilizacion = motivoInutilizacion;
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













