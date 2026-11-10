package com.frcefact.dto.integracion;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Documento electrónico tal como lo devuelve SIFEN, para la API de integración.
 *
 * <p>Todos los valores se leen del XML aprobado por la SET; ninguno se recalcula. Un campo que
 * no viene en el XML queda en {@code null} (se omite en el JSON): no se completa con defaults.
 *
 * <p>{@code items} y {@code xml} solo vienen si el solicitante los pide ({@code ?incluir=items,xml}).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DeConsultadoDto(
        String cdc,
        /** APROBADO, o CANCELADO si SIFEN informa un evento de cancelación. */
        String estado,
        RespuestaSifen respuestaSifen,
        Integer tipoDocumento,
        String tipoDocumentoDescripcion,
        Timbrado timbrado,
        LocalDateTime fechaEmision,
        LocalDateTime fechaFirma,
        Emisor emisor,
        Receptor receptor,
        Operacion operacion,
        Totales totales,
        List<String> documentosAsociados,
        MotivoEmision motivoEmision,
        List<Evento> eventos,
        String urlQr,
        List<Item> items,
        String xml) {

    public DeConsultadoDto conIncluidos(boolean incluirItems, boolean incluirXml) {
        return new DeConsultadoDto(cdc, estado, respuestaSifen, tipoDocumento, tipoDocumentoDescripcion,
                timbrado, fechaEmision, fechaFirma, emisor, receptor, operacion, totales,
                documentosAsociados, motivoEmision, eventos, urlQr,
                incluirItems ? items : null,
                incluirXml ? xml : null);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record RespuestaSifen(String codigo, String mensaje, LocalDateTime fechaProceso, String protocoloAutorizacion) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Timbrado(String numero, LocalDate fechaInicioVigencia, String establecimiento,
                           String puntoExpedicion, String numeroDocumento, String numeroCompleto) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Emisor(String ruc, String dv, String razonSocial, String nombreFantasia, String direccion,
                         String telefono, String email, String ciudad, String departamento,
                         List<String> actividadesEconomicas) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Receptor(Boolean contribuyente, String ruc, String dv, String tipoDocumentoIdentidad,
                           String numeroDocumentoIdentidad, String nombre, String nombreFantasia, String direccion,
                           String telefono, String email, String ciudad, String departamento) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Operacion(String moneda, String monedaDescripcion, BigDecimal tipoCambio,
                            String tipoTransaccion, Integer condicion, String condicionDescripcion,
                            String plazoCredito) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Totales(BigDecimal subtotalExento, BigDecimal subtotal5, BigDecimal subtotal10,
                          BigDecimal totalOperacion, BigDecimal totalDescuento, BigDecimal totalGeneral,
                          BigDecimal iva5, BigDecimal iva10, BigDecimal totalIva,
                          BigDecimal baseGravada5, BigDecimal baseGravada10, BigDecimal totalGuaranies) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record MotivoEmision(Integer codigo, String descripcion) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Evento(String id, String tipo, String descripcion, LocalDateTime fechaFirma) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Item(String codigo, String descripcion, String unidadMedida, String unidadMedidaDescripcion,
                       BigDecimal cantidad, BigDecimal precioUnitario, BigDecimal totalBruto, BigDecimal descuento,
                       BigDecimal totalOperacion, Integer afectacionIva, String afectacionIvaDescripcion,
                       BigDecimal tasaIva, BigDecimal baseGravadaIva, BigDecimal liquidacionIva, BigDecimal baseExenta) {
    }
}
