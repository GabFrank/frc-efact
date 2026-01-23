package com.frcefact.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class NotaRemisionDto {

    private Long id;

    @NotNull(message = "Empresa ID es requerido")
    private Long empresaId;

    @NotNull(message = "Timbrado detalle ID es requerido")
    private Long timbradoDetalleId;

    private Long clienteId; // Destinatario principal

    private Long facturaLegalId; // Factura asociada (opcional)

    private Integer numeroNotaRemision;

    private String fecha;

    // Salida
    @Size(max = 255)
    private String direccionPartida;

    @Size(max = 100)
    private String ciudadPartida;

    @Size(max = 100)
    private String departamentoPartida;

    private Long ciudadPartidaId;
    private Long departamentoPartidaId;
    private Long distritoPartidaId;

    // Llegada
    @Size(max = 200)
    private String nombreDestinatario;

    @Size(max = 20)
    private String rucDestinatario;

    @Size(max = 255)
    private String direccionDestinatario;

    @Size(max = 100)
    private String ciudadDestinatario;

    @Size(max = 100)
    private String departamentoDestinatario;

    private Long ciudadDestinatarioId;
    private Long departamentoDestinatarioId;
    private Long distritoDestinatarioId;

    // Datos remision
    @Size(max = 50)
    private String motivoEmision;

    private String fechaInicioTraslado;
    private String fechaFinTraslado;
    private BigDecimal kmEstimado;

    // Transporte
    @Size(max = 50)
    private String tipoTransporte;

    @Size(max = 50)
    private String modalidadTransporte;

    // Vehiculo
    @Size(max = 100)
    private String vehiculoMarca;

    @Size(max = 20)
    private String vehiculoMatricula;

    // Transportista
    @Size(max = 200)
    private String transportistaNombre;

    @Size(max = 20)
    private String transportistaRuc;

    @Size(max = 255)
    private String transportistaDireccion;

    // Conductor
    @Size(max = 200)
    private String conductorNombre;

    @Size(max = 20)
    private String conductorDoc;

    @Size(max = 255)
    private String conductorDireccion;

    private String fechaEstimadaFactura;

    // Items
    @NotNull(message = "Items son requeridos")
    @NotEmpty(message = "La nota de remisión debe tener al menos un item")
    @Valid
    private List<NotaRemisionItemDto> items = new ArrayList<>();

    private Boolean activo = true;

    // Campos adicionales
    private String numeroFormateado;
    private String nombreEmpresa;
    private String nombreCliente;
    private String emailCliente;
    private String numeroFacturaAsociada;

    // Documento electrónico
    private Long documentoElectronicoId;
    private String estadoDocumentoElectronico;
    private String cdcDocumentoElectronico;
    private String urlQrDocumentoElectronico;
    private Long loteDeId;

    public NotaRemisionDto() {
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

    public Integer getNumeroNotaRemision() { return numeroNotaRemision; }
    public void setNumeroNotaRemision(Integer numeroNotaRemision) { this.numeroNotaRemision = numeroNotaRemision; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getDireccionPartida() { return direccionPartida; }
    public void setDireccionPartida(String direccionPartida) { this.direccionPartida = direccionPartida; }

    public String getCiudadPartida() { return ciudadPartida; }
    public void setCiudadPartida(String ciudadPartida) { this.ciudadPartida = ciudadPartida; }

    public String getDepartamentoPartida() { return departamentoPartida; }
    public void setDepartamentoPartida(String departamentoPartida) { this.departamentoPartida = departamentoPartida; }

    public Long getCiudadPartidaId() { return ciudadPartidaId; }
    public void setCiudadPartidaId(Long ciudadPartidaId) { this.ciudadPartidaId = ciudadPartidaId; }

    public Long getDepartamentoPartidaId() { return departamentoPartidaId; }
    public void setDepartamentoPartidaId(Long departamentoPartidaId) { this.departamentoPartidaId = departamentoPartidaId; }

    public Long getDistritoPartidaId() { return distritoPartidaId; }
    public void setDistritoPartidaId(Long distritoPartidaId) { this.distritoPartidaId = distritoPartidaId; }

    public String getNombreDestinatario() { return nombreDestinatario; }
    public void setNombreDestinatario(String nombreDestinatario) { this.nombreDestinatario = nombreDestinatario; }

    public String getRucDestinatario() { return rucDestinatario; }
    public void setRucDestinatario(String rucDestinatario) { this.rucDestinatario = rucDestinatario; }

    public String getDireccionDestinatario() { return direccionDestinatario; }
    public void setDireccionDestinatario(String direccionDestinatario) { this.direccionDestinatario = direccionDestinatario; }

    public String getCiudadDestinatario() { return ciudadDestinatario; }
    public void setCiudadDestinatario(String ciudadDestinatario) { this.ciudadDestinatario = ciudadDestinatario; }

    public String getDepartamentoDestinatario() { return departamentoDestinatario; }
    public void setDepartamentoDestinatario(String departamentoDestinatario) { this.departamentoDestinatario = departamentoDestinatario; }

    public Long getCiudadDestinatarioId() { return ciudadDestinatarioId; }
    public void setCiudadDestinatarioId(Long ciudadDestinatarioId) { this.ciudadDestinatarioId = ciudadDestinatarioId; }

    public Long getDepartamentoDestinatarioId() { return departamentoDestinatarioId; }
    public void setDepartamentoDestinatarioId(Long departamentoDestinatarioId) { this.departamentoDestinatarioId = departamentoDestinatarioId; }

    public Long getDistritoDestinatarioId() { return distritoDestinatarioId; }
    public void setDistritoDestinatarioId(Long distritoDestinatarioId) { this.distritoDestinatarioId = distritoDestinatarioId; }

    public String getMotivoEmision() { return motivoEmision; }
    public void setMotivoEmision(String motivoEmision) { this.motivoEmision = motivoEmision; }

    public String getFechaInicioTraslado() { return fechaInicioTraslado; }
    public void setFechaInicioTraslado(String fechaInicioTraslado) { this.fechaInicioTraslado = fechaInicioTraslado; }

    public String getFechaFinTraslado() { return fechaFinTraslado; }
    public void setFechaFinTraslado(String fechaFinTraslado) { this.fechaFinTraslado = fechaFinTraslado; }

    public BigDecimal getKmEstimado() { return kmEstimado; }
    public void setKmEstimado(BigDecimal kmEstimado) { this.kmEstimado = kmEstimado; }

    public String getTipoTransporte() { return tipoTransporte; }
    public void setTipoTransporte(String tipoTransporte) { this.tipoTransporte = tipoTransporte; }

    public String getModalidadTransporte() { return modalidadTransporte; }
    public void setModalidadTransporte(String modalidadTransporte) { this.modalidadTransporte = modalidadTransporte; }

    public String getVehiculoMarca() { return vehiculoMarca; }
    public void setVehiculoMarca(String vehiculoMarca) { this.vehiculoMarca = vehiculoMarca; }

    public String getVehiculoMatricula() { return vehiculoMatricula; }
    public void setVehiculoMatricula(String vehiculoMatricula) { this.vehiculoMatricula = vehiculoMatricula; }

    public String getTransportistaNombre() { return transportistaNombre; }
    public void setTransportistaNombre(String transportistaNombre) { this.transportistaNombre = transportistaNombre; }

    public String getTransportistaRuc() { return transportistaRuc; }
    public void setTransportistaRuc(String transportistaRuc) { this.transportistaRuc = transportistaRuc; }

    public String getTransportistaDireccion() { return transportistaDireccion; }
    public void setTransportistaDireccion(String transportistaDireccion) { this.transportistaDireccion = transportistaDireccion; }

    public String getConductorNombre() { return conductorNombre; }
    public void setConductorNombre(String conductorNombre) { this.conductorNombre = conductorNombre; }

    public String getConductorDoc() { return conductorDoc; }
    public void setConductorDoc(String conductorDoc) { this.conductorDoc = conductorDoc; }

    public String getConductorDireccion() { return conductorDireccion; }
    public void setConductorDireccion(String conductorDireccion) { this.conductorDireccion = conductorDireccion; }

    public String getFechaEstimadaFactura() { return fechaEstimadaFactura; }
    public void setFechaEstimadaFactura(String fechaEstimadaFactura) { this.fechaEstimadaFactura = fechaEstimadaFactura; }

    public List<NotaRemisionItemDto> getItems() { return items; }
    public void setItems(List<NotaRemisionItemDto> items) { this.items = items; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public String getNumeroFormateado() { return numeroFormateado; }
    public void setNumeroFormateado(String numeroFormateado) { this.numeroFormateado = numeroFormateado; }

    public String getNombreEmpresa() { return nombreEmpresa; }
    public void setNombreEmpresa(String nombreEmpresa) { this.nombreEmpresa = nombreEmpresa; }

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }

    public String getEmailCliente() { return emailCliente; }
    public void setEmailCliente(String emailCliente) { this.emailCliente = emailCliente; }

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

