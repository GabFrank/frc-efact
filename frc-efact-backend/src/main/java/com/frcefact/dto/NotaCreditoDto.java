package com.frcefact.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class NotaCreditoDto {

    private Long id;

    @NotNull(message = "Empresa ID es requerido")
    private Long empresaId;

    @NotNull(message = "Timbrado detalle ID es requerido")
    private Long timbradoDetalleId;

    private Long clienteId;

    private Long facturaLegalId;

    private Integer numeroNotaCredito;

    private String fecha;

    // Datos del cliente (snapshot)
    @Size(max = 200, message = "Nombre no debe exceder 200 caracteres")
    private String nombre;

    @Size(max = 20, message = "RUC no debe exceder 20 caracteres")
    private String ruc;

    private String direccion;

    // Motivo
    @Size(max = 50, message = "Motivo no debe exceder 50 caracteres")
    private String motivoEmision;

    @Size(max = 255, message = "Descripción motivo no debe exceder 255 caracteres")
    private String descripcionMotivo;

    // Items
    @Valid
    private List<NotaCreditoItemDto> items = new ArrayList<>();

    // Totales
    private BigDecimal ivaParcial0;
    private BigDecimal ivaParcial5;
    private BigDecimal ivaParcial10;
    private BigDecimal totalParcial0;
    private BigDecimal totalParcial5;
    private BigDecimal totalParcial10;
    private BigDecimal descuentoFinal;
    private BigDecimal totalParcial;
    private BigDecimal totalFinal;

    // Moneda
    private String monedaExtranjera;
    private BigDecimal cambio;

    private Boolean activo = true;

    // Campos adicionales
    private String numeroFormateado;
    private String nombreEmpresa;
    private String nombreCliente;
    private String numeroFacturaAsociada;

    // Documento electrónico
    private Long documentoElectronicoId;
    private String estadoDocumentoElectronico;
    private String cdcDocumentoElectronico;
    private String urlQrDocumentoElectronico;
    private Long loteDeId;

    public NotaCreditoDto() {
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEmpresaId() { return empresaId; }
    public void setEmpresaId(Long empresaId) { this.empresaId = empresaId; }

    public Long getTimbradoDetalleId() { return timbradoDetalleId; }
    public void setTimbradoDetalleId(Long timbradoDetalleId) { this.timbradoDetalleId = timbradoDetalleId; }

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }

    public Long getFacturaLegalId() { return facturaLegalId; }
    public void setFacturaLegalId(Long facturaLegalId) { this.facturaLegalId = facturaLegalId; }

    public Integer getNumeroNotaCredito() { return numeroNotaCredito; }
    public void setNumeroNotaCredito(Integer numeroNotaCredito) { this.numeroNotaCredito = numeroNotaCredito; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getRuc() { return ruc; }
    public void setRuc(String ruc) { this.ruc = ruc; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getMotivoEmision() { return motivoEmision; }
    public void setMotivoEmision(String motivoEmision) { this.motivoEmision = motivoEmision; }

    public String getDescripcionMotivo() { return descripcionMotivo; }
    public void setDescripcionMotivo(String descripcionMotivo) { this.descripcionMotivo = descripcionMotivo; }

    public List<NotaCreditoItemDto> getItems() { return items; }
    public void setItems(List<NotaCreditoItemDto> items) { this.items = items; }

    public BigDecimal getIvaParcial0() { return ivaParcial0; }
    public void setIvaParcial0(BigDecimal ivaParcial0) { this.ivaParcial0 = ivaParcial0; }

    public BigDecimal getIvaParcial5() { return ivaParcial5; }
    public void setIvaParcial5(BigDecimal ivaParcial5) { this.ivaParcial5 = ivaParcial5; }

    public BigDecimal getIvaParcial10() { return ivaParcial10; }
    public void setIvaParcial10(BigDecimal ivaParcial10) { this.ivaParcial10 = ivaParcial10; }

    public BigDecimal getTotalParcial0() { return totalParcial0; }
    public void setTotalParcial0(BigDecimal totalParcial0) { this.totalParcial0 = totalParcial0; }

    public BigDecimal getTotalParcial5() { return totalParcial5; }
    public void setTotalParcial5(BigDecimal totalParcial5) { this.totalParcial5 = totalParcial5; }

    public BigDecimal getTotalParcial10() { return totalParcial10; }
    public void setTotalParcial10(BigDecimal totalParcial10) { this.totalParcial10 = totalParcial10; }

    public BigDecimal getDescuentoFinal() { return descuentoFinal; }
    public void setDescuentoFinal(BigDecimal descuentoFinal) { this.descuentoFinal = descuentoFinal; }

    public BigDecimal getTotalParcial() { return totalParcial; }
    public void setTotalParcial(BigDecimal totalParcial) { this.totalParcial = totalParcial; }

    public BigDecimal getTotalFinal() { return totalFinal; }
    public void setTotalFinal(BigDecimal totalFinal) { this.totalFinal = totalFinal; }

    public String getMonedaExtranjera() { return monedaExtranjera; }
    public void setMonedaExtranjera(String monedaExtranjera) { this.monedaExtranjera = monedaExtranjera; }

    public BigDecimal getCambio() { return cambio; }
    public void setCambio(BigDecimal cambio) { this.cambio = cambio; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public String getNumeroFormateado() { return numeroFormateado; }
    public void setNumeroFormateado(String numeroFormateado) { this.numeroFormateado = numeroFormateado; }

    public String getNombreEmpresa() { return nombreEmpresa; }
    public void setNombreEmpresa(String nombreEmpresa) { this.nombreEmpresa = nombreEmpresa; }

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }

    public String getNumeroFacturaAsociada() { return numeroFacturaAsociada; }
    public void setNumeroFacturaAsociada(String numeroFacturaAsociada) { this.numeroFacturaAsociada = numeroFacturaAsociada; }

    public Long getDocumentoElectronicoId() { return documentoElectronicoId; }
    public void setDocumentoElectronicoId(Long documentoElectronicoId) { this.documentoElectronicoId = documentoElectronicoId; }

    public String getEstadoDocumentoElectronico() { return estadoDocumentoElectronico; }
    public void setEstadoDocumentoElectronico(String estadoDocumentoElectronico) { this.estadoDocumentoElectronico = estadoDocumentoElectronico; }

    public String getCdcDocumentoElectronico() { return cdcDocumentoElectronico; }
    public void setCdcDocumentoElectronico(String cdcDocumentoElectronico) { this.cdcDocumentoElectronico = cdcDocumentoElectronico; }

    public String getUrlQrDocumentoElectronico() { return urlQrDocumentoElectronico; }
    public void setUrlQrDocumentoElectronico(String urlQrDocumentoElectronico) { this.urlQrDocumentoElectronico = urlQrDocumentoElectronico; }

    public Long getLoteDeId() { return loteDeId; }
    public void setLoteDeId(Long loteDeId) { this.loteDeId = loteDeId; }
}

