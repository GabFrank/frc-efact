package com.frcefact.model;

import com.frcefact.model.base.AuditableEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entidad que representa un Documento Electrónico (DE) generado a partir de una factura legal.
 * Contiene el XML firmado, CDC, estado y respuesta de SIFEN.
 */
@Entity
@Table(name = "documento_electronico", schema = "financiero")
public class DocumentoElectronico extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Factura legal asociada al documento electrónico (relación 1:1)
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_legal_id", unique = true, nullable = true)
    private FacturaLegal facturaLegal;

    /**
     * Nota de Crédito asociada al documento electrónico (relación 1:1)
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nota_credito_id", unique = true, nullable = true)
    private NotaCredito notaCredito;

    /**
     * Nota de Débito asociada al documento electrónico (relación 1:1)
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nota_debito_id", unique = true, nullable = true)
    private NotaDebito notaDebito;

    /**
     * Nota de Remisión asociada al documento electrónico (relación 1:1)
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nota_remision_id", unique = true, nullable = true)
    private NotaRemision notaRemision;

    /**
     * Lote al que pertenece este documento electrónico
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_de_id")
    private LoteDE loteDE;

    /**
     * Código de Control del Documento Electrónico (44 caracteres)
     */
    @Column(name = "cdc", length = 44, unique = true)
    private String cdc;

    /**
     * URL del código QR para consulta del documento
     */
    @Column(name = "url_qr", columnDefinition = "TEXT")
    private String urlQr;

    /**
     * Número del documento electrónico
     */
    @Column(name = "numero_documento", length = 50)
    private String numeroDocumento;

    /**
     * Tipo de documento (1=Factura electrónica)
     */
    @Column(name = "tipo_documento", length = 10)
    private String tipoDocumento = "1";

    /**
     * XML original del documento antes de firmar
     */
    @Column(name = "xml_original", columnDefinition = "TEXT")
    private String xmlOriginal;

    /**
     * XML firmado digitalmente con el certificado de la empresa
     */
    @Column(name = "xml_firmado", columnDefinition = "TEXT")
    private String xmlFirmado;

    /**
     * Estado actual del documento electrónico
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 50, nullable = false)
    private EstadoDE estado = EstadoDE.PENDIENTE;

    /**
     * Código de respuesta recibido de SIFEN
     */
    @Column(name = "codigo_respuesta_sifen", length = 10)
    private String codigoRespuestaSifen;

    /**
     * Mensaje de respuesta recibido de SIFEN
     */
    @Column(name = "mensaje_respuesta_sifen", columnDefinition = "TEXT")
    private String mensajeRespuestaSifen;

    /**
     * Respuesta completa recibida de SIFEN en formato original
     */
    @Column(name = "respuesta_sifen", columnDefinition = "TEXT")
    private String respuestaSifen;

    /**
     * Protocolo/autorización retornado por SIFEN para el documento
     */
    @Column(name = "protocolo_autorizacion", length = 50)
    private String protocoloAutorizacion;

    /**
     * Fecha de emisión del documento
     */
    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision = LocalDateTime.now();

    /**
     * Fecha en que SIFEN recibió y procesó el documento
     */
    @Column(name = "fecha_recepcion_sifen")
    private LocalDateTime fechaRecepcionSifen;

    /**
     * Fecha de la última actualización de estado reportada por SIFEN
     */
    @Column(name = "fecha_estado_actualizado")
    private LocalDateTime fechaEstadoActualizado;

    /**
     * Contador de intentos de envío/consulta del documento
     */
    @Column(name = "intentos", nullable = false)
    private Integer intentos = 0;

    /**
     * Indica si el documento está activo
     */
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    // Constructors

    public DocumentoElectronico() {
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public FacturaLegal getFacturaLegal() {
        return facturaLegal;
    }

    public void setFacturaLegal(FacturaLegal facturaLegal) {
        this.facturaLegal = facturaLegal;
    }

    public NotaCredito getNotaCredito() {
        return notaCredito;
    }

    public void setNotaCredito(NotaCredito notaCredito) {
        this.notaCredito = notaCredito;
    }

    public NotaDebito getNotaDebito() {
        return notaDebito;
    }

    public void setNotaDebito(NotaDebito notaDebito) {
        this.notaDebito = notaDebito;
    }

    public NotaRemision getNotaRemision() {
        return notaRemision;
    }

    public void setNotaRemision(NotaRemision notaRemision) {
        this.notaRemision = notaRemision;
    }

    public LoteDE getLoteDE() {
        return loteDE;
    }

    public void setLoteDE(LoteDE loteDE) {
        this.loteDE = loteDE;
    }

    public String getCdc() {
        return cdc;
    }

    public void setCdc(String cdc) {
        this.cdc = cdc;
    }

    public String getUrlQr() {
        return urlQr;
    }

    public void setUrlQr(String urlQr) {
        this.urlQr = urlQr;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getXmlOriginal() {
        return xmlOriginal;
    }

    public void setXmlOriginal(String xmlOriginal) {
        this.xmlOriginal = xmlOriginal;
    }

    public String getXmlFirmado() {
        return xmlFirmado;
    }

    public void setXmlFirmado(String xmlFirmado) {
        this.xmlFirmado = xmlFirmado;
    }

    public EstadoDE getEstado() {
        return estado;
    }

    public void setEstado(EstadoDE estado) {
        this.estado = estado;
    }

    public String getCodigoRespuestaSifen() {
        return codigoRespuestaSifen;
    }

    public void setCodigoRespuestaSifen(String codigoRespuestaSifen) {
        this.codigoRespuestaSifen = codigoRespuestaSifen;
    }

    public String getMensajeRespuestaSifen() {
        return mensajeRespuestaSifen;
    }

    public void setMensajeRespuestaSifen(String mensajeRespuestaSifen) {
        this.mensajeRespuestaSifen = mensajeRespuestaSifen;
    }

    public String getRespuestaSifen() {
        return respuestaSifen;
    }

    public void setRespuestaSifen(String respuestaSifen) {
        this.respuestaSifen = respuestaSifen;
    }

    public String getProtocoloAutorizacion() {
        return protocoloAutorizacion;
    }

    public void setProtocoloAutorizacion(String protocoloAutorizacion) {
        this.protocoloAutorizacion = protocoloAutorizacion;
    }

    public LocalDateTime getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(LocalDateTime fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public LocalDateTime getFechaRecepcionSifen() {
        return fechaRecepcionSifen;
    }

    public void setFechaRecepcionSifen(LocalDateTime fechaRecepcionSifen) {
        this.fechaRecepcionSifen = fechaRecepcionSifen;
    }

    public LocalDateTime getFechaEstadoActualizado() {
        return fechaEstadoActualizado;
    }

    public void setFechaEstadoActualizado(LocalDateTime fechaEstadoActualizado) {
        this.fechaEstadoActualizado = fechaEstadoActualizado;
    }

    public Integer getIntentos() {
        return intentos;
    }

    public void setIntentos(Integer intentos) {
        this.intentos = intentos;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
