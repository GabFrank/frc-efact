package com.frcefact.service;

import com.frcefact.model.*;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

/**
 * Servicio para generar el PDF del KUDE (Representación Gráfica del Documento Electrónico)
 * usando JasperReports.
 */
@Service
public class KudePdfService {

    private static final Logger log = LoggerFactory.getLogger(KudePdfService.class);
    private static final String REPORT_PATH = "reports/factura-electronica-kude.jrxml";
    private static final String NR_REPORT_PATH = "reports/nota-remision-kude.jrxml";
    private static final String NC_REPORT_PATH = "reports/nota-credito-kude.jrxml";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Genera el PDF del KUDE para una factura legal.
     * 
     * @param factura Factura legal con todos sus datos cargados
     * @return Array de bytes del PDF generado
     * @throws Exception Si hay error al generar el PDF
     */
    public byte[] generarPdfKude(FacturaLegal factura) throws Exception {
        log.info("Generando PDF KUDE para factura ID: {}", factura.getId());

        // Cargar el template del reporte
        ClassPathResource resource = new ClassPathResource(REPORT_PATH);
        InputStream reportStream = resource.getInputStream();
        JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

        // Preparar parámetros del reporte
        Map<String, Object> parameters = prepararParametros(factura);

        // Preparar datos de los items (con conversión de moneda si aplica)
        boolean tieneMonedaExtranjera = factura.getMonedaExtranjera() != null 
                && !factura.getMonedaExtranjera().equals("PYG")
                && factura.getCambio() != null
                && factura.getCambio().compareTo(BigDecimal.ZERO) > 0;
        
        BigDecimal tipoCambio = tieneMonedaExtranjera ? factura.getCambio() : BigDecimal.ONE;
        List<Map<String, Object>> itemsData = prepararItemsData(
                factura.getItems(), 
                factura.getMonedaExtranjera(), 
                tipoCambio);
        JRBeanCollectionDataSource itemsDataSource = new JRBeanCollectionDataSource(itemsData);

        // Generar el PDF
        JasperPrint jasperPrint = JasperFillManager.fillReport(
                jasperReport, parameters, itemsDataSource);

        // Exportar a PDF
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);

        log.info("PDF KUDE generado exitosamente para factura ID: {}", factura.getId());
        return outputStream.toByteArray();
    }

    /**
     * Genera el PDF del KUDE para una nota de remisión.
     * 
     * @param notaRemision Nota de remisión con todos sus datos cargados
     * @return Array de bytes del PDF generado
     * @throws Exception Si hay error al generar el PDF
     */
    public byte[] generarPdfKude(NotaRemision notaRemision) throws Exception {
        log.info("Generando PDF KUDE para nota de remisión ID: {}", notaRemision.getId());

        // Cargar el template del reporte
        ClassPathResource resource = new ClassPathResource(NR_REPORT_PATH);
        InputStream reportStream = resource.getInputStream();
        JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

        // Preparar parámetros del reporte
        Map<String, Object> parameters = prepararParametrosNotaRemision(notaRemision);

        // Preparar datos de los items
        List<Map<String, Object>> itemsData = prepararItemsNotaRemisionData(notaRemision.getItems());
        JRBeanCollectionDataSource itemsDataSource = new JRBeanCollectionDataSource(itemsData);

        // Generar el PDF
        JasperPrint jasperPrint = JasperFillManager.fillReport(
                jasperReport, parameters, itemsDataSource);

        // Exportar a PDF
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);

        log.info("PDF KUDE generado exitosamente para nota de remisión ID: {}", notaRemision.getId());
        return outputStream.toByteArray();
    }

    /**
     * Genera el PDF del KUDE para una nota de crédito.
     *
     * @param notaCredito Nota de crédito con todos sus datos cargados
     * @return Array de bytes del PDF generado
     * @throws Exception Si hay error al generar el PDF
     */
    public byte[] generarPdfKude(NotaCredito notaCredito) throws Exception {
        log.info("Generando PDF KUDE para nota de crédito ID: {}", notaCredito.getId());

        ClassPathResource resource = new ClassPathResource(NC_REPORT_PATH);
        InputStream reportStream = resource.getInputStream();
        JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

        Map<String, Object> parameters = prepararParametrosNotaCredito(notaCredito);

        boolean tieneMonedaExtranjera = notaCredito.getMonedaExtranjera() != null
                && !"PYG".equalsIgnoreCase(notaCredito.getMonedaExtranjera())
                && notaCredito.getCambio() != null
                && notaCredito.getCambio().compareTo(BigDecimal.ZERO) > 0;
        BigDecimal tipoCambio = tieneMonedaExtranjera ? notaCredito.getCambio() : BigDecimal.ONE;

        List<Map<String, Object>> itemsData = prepararItemsNotaCreditoData(
                notaCredito.getItems(),
                notaCredito.getMonedaExtranjera(),
                tipoCambio);
        JRBeanCollectionDataSource itemsDataSource = new JRBeanCollectionDataSource(itemsData);

        JasperPrint jasperPrint = JasperFillManager.fillReport(
                jasperReport, parameters, itemsDataSource);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);

        log.info("PDF KUDE generado exitosamente para nota de crédito ID: {}", notaCredito.getId());
        return outputStream.toByteArray();
    }

    /**
     * Prepara los parámetros del reporte a partir de la factura.
     */
    private Map<String, Object> prepararParametros(FacturaLegal factura) throws Exception {
        Map<String, Object> parameters = new HashMap<>();

        Empresa empresa = factura.getEmpresa();
        TimbradoDetalle timbradoDetalle = factura.getTimbradoDetalle();
        Timbrado timbrado = timbradoDetalle.getTimbrado();
        Cliente cliente = factura.getCliente();
        DocumentoElectronico documentoElectronico = factura.getDocumentoElectronico();

        // Logo (si existe)
        String logoPath = obtenerLogoPath(empresa);
        parameters.put("logo", logoPath);

        // Datos del emisor
        parameters.put("razonSocial", empresa.getRazonSocial() != null ? empresa.getRazonSocial() : "");
        parameters.put("rucEmisor", empresa.getRuc() != null ? empresa.getRuc() : "");
        parameters.put("numeroTimbrado", timbrado.getNumero() != null ? timbrado.getNumero() : "");
        parameters.put("fechaInicioVigencia", timbrado.getFechaInicio() != null ? 
                timbrado.getFechaInicio().format(DATE_FORMATTER) : "");
        parameters.put("direccionEmisor", empresa.getDireccion() != null ? empresa.getDireccion() : "");
        parameters.put("telefonoEmisor", empresa.getTelefono() != null ? empresa.getTelefono() : "");
        parameters.put("emailEmisor", empresa.getEmail() != null ? empresa.getEmail() : "");
        parameters.put("actividadEconomica", empresa.getDescActividadEconomicaPrincipal() != null ? 
                empresa.getDescActividadEconomicaPrincipal() : "");

        // Datos de la factura
        parameters.put("numeroFactura", factura.getNumeroFacturaFormateado());
        parameters.put("fechaEmision", factura.getFecha() != null ? 
                factura.getFecha().format(DATE_TIME_FORMATTER) : "");
        parameters.put("presupuesto", ""); // TODO: Si se implementa presupuesto
        parameters.put("ordenAsociada", ""); // TODO: Si se implementa orden asociada

        // Datos del cliente
        if (cliente != null) {
            parameters.put("rucCliente", cliente.getRuc() != null ? cliente.getRuc() : "");
            parameters.put("nombreCliente", cliente.getNombre() != null ? cliente.getNombre() : "");
            parameters.put("fantasiaCliente", cliente.getRazonSocial() != null ? cliente.getRazonSocial() : "");
            
            // Construir dirección completa del cliente
            String direccionCliente = construirDireccionCliente(cliente);
            parameters.put("direccionCliente", direccionCliente);
            
            parameters.put("ciudadCliente", cliente.getCiudad() != null && cliente.getCiudad().getNombre() != null ? 
                    cliente.getCiudad().getNombre() : "");
            parameters.put("departamentoCliente", cliente.getCiudad() != null && 
                    cliente.getCiudad().getDistrito() != null && 
                    cliente.getCiudad().getDistrito().getDepartamento() != null &&
                    cliente.getCiudad().getDistrito().getDepartamento().getNombre() != null ? 
                    cliente.getCiudad().getDistrito().getDepartamento().getNombre() : "");
            parameters.put("telefonoCliente", cliente.getTelefono() != null ? cliente.getTelefono() : "");
            parameters.put("emailCliente", cliente.getEmail() != null ? cliente.getEmail() : "");
        } else {
            // Usar datos snapshot de la factura
            parameters.put("rucCliente", factura.getRuc() != null ? factura.getRuc() : "");
            parameters.put("nombreCliente", factura.getNombre() != null ? factura.getNombre() : "");
            parameters.put("fantasiaCliente", "");
            parameters.put("direccionCliente", factura.getDireccion() != null ? factura.getDireccion() : "");
            parameters.put("ciudadCliente", "");
            parameters.put("departamentoCliente", "");
            parameters.put("telefonoCliente", "");
            parameters.put("emailCliente", "");
        }

        // Condición de pago (alineado con SIFEN: contado vs crédito por plazo 30 días)
        boolean esCredito = Boolean.TRUE.equals(factura.getCredito());
        parameters.put("contado", !esCredito);
        parameters.put("credito", esCredito);
        parameters.put("cuotas", ""); // Solo aplica si iCondCred=2 (Cuota); nosotros usamos Plazo
        parameters.put("moneda", factura.getMonedaExtranjera() != null ? 
                factura.getMonedaExtranjera() : "PYG");
        
        // Formatear tipo de cambio sin decimales
        String tipoCambioFormateado = "";
        if (factura.getCambio() != null) {
            tipoCambioFormateado = factura.getCambio().setScale(0, java.math.RoundingMode.HALF_UP).toString();
        }
        parameters.put("tipoCambio", tipoCambioFormateado);
        parameters.put("plazo", esCredito ? "30 días" : ""); // Crédito por plazo: mismo valor que en el DE

        // Determinar si hay moneda extranjera y necesitamos convertir
        boolean tieneMonedaExtranjera = factura.getMonedaExtranjera() != null 
                && !factura.getMonedaExtranjera().equals("PYG")
                && factura.getCambio() != null
                && factura.getCambio().compareTo(BigDecimal.ZERO) > 0;
        
        // Pasar parámetro para formateo en el reporte
        parameters.put("tieneMonedaExtranjera", tieneMonedaExtranjera);
        
        BigDecimal tipoCambio = tieneMonedaExtranjera ? factura.getCambio() : BigDecimal.ONE;

        // Totales - convertir a moneda extranjera si aplica
        BigDecimal subtotalExentas = factura.getTotalParcial0() != null ? 
                factura.getTotalParcial0() : BigDecimal.ZERO;
        BigDecimal subtotal5 = factura.getTotalParcial5() != null ? 
                factura.getTotalParcial5() : BigDecimal.ZERO;
        BigDecimal subtotal10 = factura.getTotalParcial10() != null ? 
                factura.getTotalParcial10() : BigDecimal.ZERO;
        BigDecimal totalOperacion = factura.getTotalParcial() != null ? 
                factura.getTotalParcial() : BigDecimal.ZERO;
        BigDecimal totalIva5 = factura.getIvaParcial5() != null ? 
                factura.getIvaParcial5() : BigDecimal.ZERO;
        BigDecimal totalIva10 = factura.getIvaParcial10() != null ? 
                factura.getIvaParcial10() : BigDecimal.ZERO;
        BigDecimal totalFinal = factura.getTotalFinal() != null ? 
                factura.getTotalFinal() : BigDecimal.ZERO;
        
        // Convertir a moneda extranjera si aplica
        // IMPORTANTE: Usar la misma lógica de redondeo que SIFEN
        // En SIFEN: los precios unitarios se convierten con 6 decimales, luego se calculan los totales
        // y se redondean según la Resolución 314/2014 de SEDECO (generalmente 2 decimales para totales)
        if (tieneMonedaExtranjera) {
            // Convertir con 6 decimales internamente (como en SIFEN) y luego redondear a 2 decimales
            subtotalExentas = subtotalExentas.divide(tipoCambio, 6, java.math.RoundingMode.HALF_UP)
                    .setScale(2, java.math.RoundingMode.HALF_UP);
            subtotal5 = subtotal5.divide(tipoCambio, 6, java.math.RoundingMode.HALF_UP)
                    .setScale(2, java.math.RoundingMode.HALF_UP);
            subtotal10 = subtotal10.divide(tipoCambio, 6, java.math.RoundingMode.HALF_UP)
                    .setScale(2, java.math.RoundingMode.HALF_UP);
            totalOperacion = totalOperacion.divide(tipoCambio, 6, java.math.RoundingMode.HALF_UP)
                    .setScale(2, java.math.RoundingMode.HALF_UP);
            totalIva5 = totalIva5.divide(tipoCambio, 6, java.math.RoundingMode.HALF_UP)
                    .setScale(2, java.math.RoundingMode.HALF_UP);
            totalIva10 = totalIva10.divide(tipoCambio, 6, java.math.RoundingMode.HALF_UP)
                    .setScale(2, java.math.RoundingMode.HALF_UP);
            totalFinal = totalFinal.divide(tipoCambio, 6, java.math.RoundingMode.HALF_UP)
                    .setScale(2, java.math.RoundingMode.HALF_UP);
        }
        
        BigDecimal totalIva = totalIva5.add(totalIva10);
        
        parameters.put("subtotalExentas", subtotalExentas.doubleValue());
        parameters.put("subtotal5", subtotal5.doubleValue());
        parameters.put("subtotal10", subtotal10.doubleValue());
        parameters.put("totalOperacion", totalOperacion.doubleValue());
        parameters.put("totalIva5", totalIva5.doubleValue());
        parameters.put("totalIva10", totalIva10.doubleValue());
        parameters.put("totalIva", totalIva.doubleValue());
        parameters.put("totalFinal", totalFinal.doubleValue());
        
        // Total en guaraníes siempre se muestra en guaraníes (valor original)
        BigDecimal totalEnGuarani = factura.getTotalFinal() != null ? 
                factura.getTotalFinal() : BigDecimal.ZERO;
        parameters.put("totalEnGuarani", totalEnGuarani.doubleValue());

        // Datos del documento electrónico
        if (documentoElectronico != null) {
            parameters.put("cdc", documentoElectronico.getCdc() != null ? documentoElectronico.getCdc() : "");
            parameters.put("urlValidacion", documentoElectronico.getUrlQr() != null ? 
                    documentoElectronico.getUrlQr() : "");
            
            // Generar o obtener QR code
            String qrImagePath = generarQRCode(documentoElectronico.getUrlQr());
            parameters.put("qrImagePath", qrImagePath);
        } else {
            parameters.put("cdc", "");
            parameters.put("urlValidacion", "");
            parameters.put("qrImagePath", "");
        }

        return parameters;
    }

    /**
     * Prepara los datos de los items para el reporte.
     * @param items Lista de items de la factura
     * @param monedaExtranjera Código de moneda extranjera (null o "PYG" si es guaraníes)
     * @param tipoCambio Tipo de cambio para convertir de guaraníes a moneda extranjera
     */
    private List<Map<String, Object>> prepararItemsData(
            List<FacturaLegalItem> items, 
            String monedaExtranjera, 
            BigDecimal tipoCambio) {
        
        List<Map<String, Object>> itemsData = new ArrayList<>();
        
        boolean tieneMonedaExtranjera = monedaExtranjera != null 
                && !monedaExtranjera.equals("PYG")
                && tipoCambio != null
                && tipoCambio.compareTo(BigDecimal.ZERO) > 0;

        for (FacturaLegalItem item : items) {
            Map<String, Object> itemData = new HashMap<>();
            itemData.put("id", item.getId());
            
            Producto producto = item.getProducto();
            if (producto != null) {
                itemData.put("codigo", producto.getCodigo() != null ? producto.getCodigo() : "");
                itemData.put("iva", producto.getIva() != null ? producto.getIva() : 0);
                itemData.put("descripcionPresentacion", producto.getUnidadMedida() != null ? 
                        producto.getUnidadMedida() : "");
            } else {
                itemData.put("codigo", "");
                itemData.put("iva", 0);
                itemData.put("descripcionPresentacion", "");
            }
            
            itemData.put("descripcion", item.getDescripcion() != null ? item.getDescripcion() : "");
            itemData.put("cantidad", item.getCantidad() != null ? item.getCantidad().floatValue() : 0.0f);
            
            // Convertir precios a moneda extranjera si aplica
            // IMPORTANTE: Usar la misma lógica de redondeo que SIFEN
            // En SIFEN: los precios unitarios se convierten con 6 decimales (precisión interna)
            // Los totales se calculan a partir de los precios unitarios y luego se redondean
            BigDecimal precioUnitario = item.getPrecioUnitario() != null ? 
                    item.getPrecioUnitario() : BigDecimal.ZERO;
            BigDecimal total = item.getTotal() != null ? 
                    item.getTotal() : BigDecimal.ZERO;
            
            if (tieneMonedaExtranjera) {
                // Convertir precio unitario con 6 decimales (como en SIFEN) y luego redondear a 2 decimales para mostrar
                precioUnitario = precioUnitario.divide(tipoCambio, 6, java.math.RoundingMode.HALF_UP)
                        .setScale(2, java.math.RoundingMode.HALF_UP);
                // Convertir total con 6 decimales internamente y luego redondear a 2 decimales
                total = total.divide(tipoCambio, 6, java.math.RoundingMode.HALF_UP)
                        .setScale(2, java.math.RoundingMode.HALF_UP);
            }
            
            itemData.put("precioUnitario", precioUnitario.doubleValue());
            itemData.put("total", total.doubleValue());

            itemsData.add(itemData);
        }

        return itemsData;
    }

    /**
     * Prepara los parámetros del reporte a partir de la nota de crédito.
     */
    private Map<String, Object> prepararParametrosNotaCredito(NotaCredito notaCredito) throws Exception {
        Map<String, Object> parameters = new HashMap<>();

        Empresa empresa = notaCredito.getEmpresa();
        TimbradoDetalle timbradoDetalle = notaCredito.getTimbradoDetalle();
        Timbrado timbrado = timbradoDetalle.getTimbrado();
        Cliente cliente = notaCredito.getCliente();
        DocumentoElectronico documentoElectronico = notaCredito.getDocumentoElectronico();
        FacturaLegal facturaAsociada = notaCredito.getFacturaLegal();

        String logoPath = obtenerLogoPath(empresa);
        parameters.put("logo", logoPath);
        parameters.put("razonSocial", empresa.getRazonSocial() != null ? empresa.getRazonSocial() : "");
        parameters.put("rucEmisor", empresa.getRuc() != null ? empresa.getRuc() : "");
        parameters.put("numeroTimbrado", timbrado.getNumero() != null ? timbrado.getNumero() : "");
        parameters.put("fechaInicioVigencia", timbrado.getFechaInicio() != null ?
                timbrado.getFechaInicio().format(DATE_FORMATTER) : "");
        parameters.put("direccionEmisor", empresa.getDireccion() != null ? empresa.getDireccion() : "");
        parameters.put("telefonoEmisor", empresa.getTelefono() != null ? empresa.getTelefono() : "");
        parameters.put("emailEmisor", empresa.getEmail() != null ? empresa.getEmail() : "");
        parameters.put("actividadEconomica", empresa.getDescActividadEconomicaPrincipal() != null ?
                empresa.getDescActividadEconomicaPrincipal() : "");

        parameters.put("numeroNotaCredito", notaCredito.getNumeroFormateado());
        parameters.put("fechaEmision", notaCredito.getFecha() != null ?
                notaCredito.getFecha().format(DATE_TIME_FORMATTER) : "");

        if (cliente != null) {
            parameters.put("rucCliente", cliente.getRuc() != null ? cliente.getRuc() : "");
            parameters.put("nombreCliente", cliente.getNombre() != null ? cliente.getNombre() : "");
            parameters.put("direccionCliente", construirDireccionCliente(cliente));
        } else {
            parameters.put("rucCliente", notaCredito.getRuc() != null ? notaCredito.getRuc() : "");
            parameters.put("nombreCliente", notaCredito.getNombre() != null ? notaCredito.getNombre() : "");
            parameters.put("direccionCliente", notaCredito.getDireccion() != null ? notaCredito.getDireccion() : "");
        }

        parameters.put("motivoEmision", mapearMotivoEmisionNotaCredito(notaCredito.getMotivoEmision()));
        parameters.put("descripcionMotivo", notaCredito.getDescripcionMotivo() != null ? notaCredito.getDescripcionMotivo() : "");
        parameters.put("cdcFacturaRelacionada", facturaAsociada != null
                && facturaAsociada.getDocumentoElectronico() != null
                && facturaAsociada.getDocumentoElectronico().getCdc() != null
                ? facturaAsociada.getDocumentoElectronico().getCdc()
                : "");

        String moneda = notaCredito.getMonedaExtranjera() != null ? notaCredito.getMonedaExtranjera() : "PYG";
        boolean tieneMonedaExtranjera = !"PYG".equalsIgnoreCase(moneda)
                && notaCredito.getCambio() != null
                && notaCredito.getCambio().compareTo(BigDecimal.ZERO) > 0;
        BigDecimal tipoCambio = tieneMonedaExtranjera ? notaCredito.getCambio() : BigDecimal.ONE;

        parameters.put("moneda", moneda);
        parameters.put("tipoCambio", tieneMonedaExtranjera
                ? notaCredito.getCambio().setScale(0, java.math.RoundingMode.HALF_UP).toString()
                : "");
        parameters.put("tieneMonedaExtranjera", tieneMonedaExtranjera);

        BigDecimal subtotalExentas = notaCredito.getTotalParcial0() != null ? notaCredito.getTotalParcial0() : BigDecimal.ZERO;
        BigDecimal subtotal5 = notaCredito.getTotalParcial5() != null ? notaCredito.getTotalParcial5() : BigDecimal.ZERO;
        BigDecimal subtotal10 = notaCredito.getTotalParcial10() != null ? notaCredito.getTotalParcial10() : BigDecimal.ZERO;
        BigDecimal totalOperacion = notaCredito.getTotalParcial() != null ? notaCredito.getTotalParcial() : BigDecimal.ZERO;
        BigDecimal totalIva5 = notaCredito.getIvaParcial5() != null ? notaCredito.getIvaParcial5() : BigDecimal.ZERO;
        BigDecimal totalIva10 = notaCredito.getIvaParcial10() != null ? notaCredito.getIvaParcial10() : BigDecimal.ZERO;
        BigDecimal totalFinal = notaCredito.getTotalFinal() != null ? notaCredito.getTotalFinal() : BigDecimal.ZERO;

        if (tieneMonedaExtranjera) {
            subtotalExentas = subtotalExentas.divide(tipoCambio, 6, java.math.RoundingMode.HALF_UP).setScale(2, java.math.RoundingMode.HALF_UP);
            subtotal5 = subtotal5.divide(tipoCambio, 6, java.math.RoundingMode.HALF_UP).setScale(2, java.math.RoundingMode.HALF_UP);
            subtotal10 = subtotal10.divide(tipoCambio, 6, java.math.RoundingMode.HALF_UP).setScale(2, java.math.RoundingMode.HALF_UP);
            totalOperacion = totalOperacion.divide(tipoCambio, 6, java.math.RoundingMode.HALF_UP).setScale(2, java.math.RoundingMode.HALF_UP);
            totalIva5 = totalIva5.divide(tipoCambio, 6, java.math.RoundingMode.HALF_UP).setScale(2, java.math.RoundingMode.HALF_UP);
            totalIva10 = totalIva10.divide(tipoCambio, 6, java.math.RoundingMode.HALF_UP).setScale(2, java.math.RoundingMode.HALF_UP);
            totalFinal = totalFinal.divide(tipoCambio, 6, java.math.RoundingMode.HALF_UP).setScale(2, java.math.RoundingMode.HALF_UP);
        }

        BigDecimal totalIva = totalIva5.add(totalIva10);
        parameters.put("subtotalExentas", subtotalExentas.doubleValue());
        parameters.put("subtotal5", subtotal5.doubleValue());
        parameters.put("subtotal10", subtotal10.doubleValue());
        parameters.put("totalOperacion", totalOperacion.doubleValue());
        parameters.put("totalIva5", totalIva5.doubleValue());
        parameters.put("totalIva10", totalIva10.doubleValue());
        parameters.put("totalIva", totalIva.doubleValue());
        parameters.put("totalFinal", totalFinal.doubleValue());
        parameters.put("totalEnGuarani", (notaCredito.getTotalFinal() != null ? notaCredito.getTotalFinal() : BigDecimal.ZERO).doubleValue());

        if (documentoElectronico != null) {
            parameters.put("cdc", documentoElectronico.getCdc() != null ? documentoElectronico.getCdc() : "");
            parameters.put("urlValidacion", documentoElectronico.getUrlQr() != null ? documentoElectronico.getUrlQr() : "");
            parameters.put("qrImagePath", generarQRCode(documentoElectronico.getUrlQr()));
        } else {
            parameters.put("cdc", "");
            parameters.put("urlValidacion", "");
            parameters.put("qrImagePath", "");
        }

        return parameters;
    }

    /**
     * Prepara los datos de los ítems para el reporte de nota de crédito.
     */
    private List<Map<String, Object>> prepararItemsNotaCreditoData(
            List<NotaCreditoItem> items,
            String monedaExtranjera,
            BigDecimal tipoCambio) {
        List<Map<String, Object>> itemsData = new ArrayList<>();
        if (items == null) {
            return itemsData;
        }

        boolean tieneMonedaExtranjera = monedaExtranjera != null
                && !"PYG".equalsIgnoreCase(monedaExtranjera)
                && tipoCambio != null
                && tipoCambio.compareTo(BigDecimal.ZERO) > 0;

        for (NotaCreditoItem item : items) {
            Map<String, Object> itemData = new HashMap<>();

            Producto producto = item.getProducto();
            String codigo = item.getCodigo();
            if ((codigo == null || codigo.isBlank()) && producto != null) {
                codigo = producto.getCodigo();
            }
            itemData.put("codigo", codigo != null ? codigo : "");
            itemData.put("descripcion", item.getDescripcion() != null ? item.getDescripcion() : "");

            BigDecimal cantidad = item.getCantidad() != null ? item.getCantidad() : BigDecimal.ZERO;
            BigDecimal precioUnitario = item.getPrecioUnitario() != null ? item.getPrecioUnitario() : BigDecimal.ZERO;
            BigDecimal total = item.getTotal() != null ? item.getTotal() : BigDecimal.ZERO;

            if (tieneMonedaExtranjera) {
                precioUnitario = precioUnitario.divide(tipoCambio, 6, java.math.RoundingMode.HALF_UP)
                        .setScale(2, java.math.RoundingMode.HALF_UP);
                total = total.divide(tipoCambio, 6, java.math.RoundingMode.HALF_UP)
                        .setScale(2, java.math.RoundingMode.HALF_UP);
            }

            BigDecimal montoExento = BigDecimal.ZERO;
            BigDecimal montoGravado5 = BigDecimal.ZERO;
            BigDecimal montoGravado10 = BigDecimal.ZERO;

            Integer iva = item.getIva() != null ? item.getIva() : 10;
            if (iva == 0) {
                montoExento = total;
            } else if (iva == 5) {
                montoGravado5 = total;
            } else {
                montoGravado10 = total;
            }

            itemData.put("cantidad", cantidad.doubleValue());
            itemData.put("precioUnitario", precioUnitario.doubleValue());
            itemData.put("montoExento", montoExento.doubleValue());
            itemData.put("montoGravado5", montoGravado5.doubleValue());
            itemData.put("montoGravado10", montoGravado10.doubleValue());

            itemsData.add(itemData);
        }

        return itemsData;
    }

    private String mapearMotivoEmisionNotaCredito(String motivo) {
        if (motivo == null) return "";
        switch (motivo.trim().toUpperCase()) {
            case "DEVOLUCION":
                return "Devolución de mercadería";
            case "DESCUENTO":
                return "Descuento concedido";
            case "BONIFICACION":
                return "Bonificación";
            case "AJUSTE_DE_PRECIO":
                return "Ajuste de precio";
            case "CREDITO_INCOBRABLE":
                return "Crédito incobrable";
            case "RECUPERO_DE_COSTO":
                return "Recupero de costo";
            case "RECUPERO_DE_GASTO":
                return "Recupero de gasto";
            case "DEVOLUCION_Y_AJUSTES_DE_PRECIOS":
                return "Devolución y ajustes de precios";
            default:
                return motivo;
        }
    }

    /**
     * Construye la dirección completa del cliente.
     */
    private String construirDireccionCliente(Cliente cliente) {
        StringBuilder direccion = new StringBuilder();
        
        if (cliente.getDireccion() != null && !cliente.getDireccion().isEmpty()) {
            direccion.append(cliente.getDireccion());
        }
        
        if (cliente.getNumeroCasa() != null && !cliente.getNumeroCasa().isEmpty()) {
            if (direccion.length() > 0) {
                direccion.append(", ");
            }
            direccion.append("N° ").append(cliente.getNumeroCasa());
        }
        
        if (cliente.getCiudad() != null) {
            if (cliente.getCiudad().getNombre() != null) {
                if (direccion.length() > 0) {
                    direccion.append(", ");
                }
                direccion.append(cliente.getCiudad().getNombre());
            }
            
            if (cliente.getCiudad().getDistrito() != null && 
                cliente.getCiudad().getDistrito().getNombre() != null) {
                if (direccion.length() > 0) {
                    direccion.append(", ");
                }
                direccion.append(cliente.getCiudad().getDistrito().getNombre());
            }
            
            if (cliente.getCiudad().getDistrito() != null && 
                cliente.getCiudad().getDistrito().getDepartamento() != null &&
                cliente.getCiudad().getDistrito().getDepartamento().getNombre() != null) {
                if (direccion.length() > 0) {
                    direccion.append(", ");
                }
                direccion.append(cliente.getCiudad().getDistrito().getDepartamento().getNombre());
            }
        }
        
        return direccion.toString();
    }

    /**
     * Obtiene la ruta del logo de la empresa.
     * Por ahora retorna null, pero se puede implementar para buscar el logo.
     */
    private String obtenerLogoPath(Empresa empresa) {
        // TODO: Implementar búsqueda del logo de la empresa
        // Por ejemplo, buscar en una carpeta de logos o en la base de datos
        return null;
    }

    /**
     * Prepara los parámetros del reporte a partir de la nota de remisión.
     */
    private Map<String, Object> prepararParametrosNotaRemision(NotaRemision notaRemision) throws Exception {
        Map<String, Object> parameters = new HashMap<>();

        Empresa empresa = notaRemision.getEmpresa();
        TimbradoDetalle timbradoDetalle = notaRemision.getTimbradoDetalle();
        Timbrado timbrado = timbradoDetalle.getTimbrado();
        DocumentoElectronico documentoElectronico = notaRemision.getDocumentoElectronico();

        // Logo
        String logoPath = obtenerLogoPath(empresa);
        parameters.put("logo", logoPath);

        // Datos del emisor
        parameters.put("razonSocial", empresa.getRazonSocial() != null ? empresa.getRazonSocial() : "");
        parameters.put("rucEmisor", empresa.getRuc() != null ? empresa.getRuc() : "");
        parameters.put("numeroTimbrado", timbrado.getNumero() != null ? timbrado.getNumero() : "");
        parameters.put("fechaInicioVigencia", timbrado.getFechaInicio() != null ? 
                timbrado.getFechaInicio().format(DATE_FORMATTER) : "");
        parameters.put("direccionEmisor", empresa.getDireccion() != null ? empresa.getDireccion() : "");
        parameters.put("telefonoEmisor", empresa.getTelefono() != null ? empresa.getTelefono() : "");
        parameters.put("emailEmisor", empresa.getEmail() != null ? empresa.getEmail() : "");
        parameters.put("actividadEconomica", empresa.getDescActividadEconomicaPrincipal() != null ? 
                empresa.getDescActividadEconomicaPrincipal() : "");

        // Datos de la nota de remisión
        parameters.put("numeroNotaRemision", notaRemision.getNumeroFormateado());
        parameters.put("fechaEmision", notaRemision.getFecha() != null ? 
                notaRemision.getFecha().format(DATE_TIME_FORMATTER) : "");
        
        // Destinatario
        parameters.put("nombreDestinatario", notaRemision.getNombreDestinatario() != null ? notaRemision.getNombreDestinatario() : "");
        parameters.put("rucDestinatario", notaRemision.getRucDestinatario() != null ? notaRemision.getRucDestinatario() : "");

        // Traslado
        parameters.put("motivoEmision", mapearMotivoEmisionDescripcion(notaRemision.getMotivoEmision()));
        parameters.put("fechaInicioTraslado", notaRemision.getFechaInicioTraslado() != null ? 
                notaRemision.getFechaInicioTraslado().format(DATE_FORMATTER) : "");
        parameters.put("fechaFinTraslado", notaRemision.getFechaFinTraslado() != null ? 
                notaRemision.getFechaFinTraslado().format(DATE_FORMATTER) : "");
        parameters.put("fechaEstimadaFactura", notaRemision.getFechaEstimadaFactura() != null ? 
                notaRemision.getFechaEstimadaFactura().format(DATE_FORMATTER) : "");
        parameters.put("kmEstimado", notaRemision.getKmEstimado() != null ? notaRemision.getKmEstimado().toString() : "0");

        // Origen
        parameters.put("direccionPartida", notaRemision.getDireccionPartida() != null ? notaRemision.getDireccionPartida() : "");
        parameters.put("ciudadPartida", notaRemision.getCiudadPartida() != null ? notaRemision.getCiudadPartida() : "");
        parameters.put("departamentoPartida", notaRemision.getDepartamentoPartida() != null ? notaRemision.getDepartamentoPartida() : "");
        parameters.put("distritoPartida", ""); // TODO: Si se agrega nombre de distrito a la entidad

        // Llegada
        parameters.put("direccionLlegada", notaRemision.getDireccionDestinatario() != null ? notaRemision.getDireccionDestinatario() : "");
        parameters.put("ciudadLlegada", notaRemision.getCiudadDestinatario() != null ? notaRemision.getCiudadDestinatario() : "");
        parameters.put("departamentoLlegada", notaRemision.getDepartamentoDestinatario() != null ? notaRemision.getDepartamentoDestinatario() : "");
        parameters.put("distritoLlegada", ""); // TODO: Si se agrega nombre de distrito a la entidad

        // Transporte
        parameters.put("tipoTransporte", notaRemision.getTipoTransporte() != null ? notaRemision.getTipoTransporte() : "PROPIO");
        parameters.put("modalidadTransporte", notaRemision.getModalidadTransporte() != null ? notaRemision.getModalidadTransporte() : "TERRESTRE");
        parameters.put("vehiculoMarca", notaRemision.getVehiculoMarca() != null ? notaRemision.getVehiculoMarca() : "");
        parameters.put("vehiculoMatricula", notaRemision.getVehiculoMatricula() != null ? notaRemision.getVehiculoMatricula() : "");

        // Transportista
        parameters.put("transportistaNombre", notaRemision.getTransportistaNombre() != null ? notaRemision.getTransportistaNombre() : "");
        parameters.put("transportistaRuc", notaRemision.getTransportistaRuc() != null ? notaRemision.getTransportistaRuc() : "");
        parameters.put("transportistaDireccion", notaRemision.getTransportistaDireccion() != null ? notaRemision.getTransportistaDireccion() : "");

        // Conductor
        parameters.put("conductorNombre", notaRemision.getConductorNombre() != null ? notaRemision.getConductorNombre() : "");
        parameters.put("conductorDoc", notaRemision.getConductorDoc() != null ? notaRemision.getConductorDoc() : "");
        parameters.put("conductorDireccion", notaRemision.getConductorDireccion() != null ? notaRemision.getConductorDireccion() : "");

        // Factura asociada
        parameters.put("numeroFacturaAsociada", notaRemision.getFacturaLegal() != null ? 
                notaRemision.getFacturaLegal().getNumeroFacturaFormateado() : "");

        // Documento electrónico
        if (documentoElectronico != null) {
            parameters.put("cdc", documentoElectronico.getCdc() != null ? documentoElectronico.getCdc() : "");
            parameters.put("urlValidacion", documentoElectronico.getUrlQr() != null ? 
                    documentoElectronico.getUrlQr() : "");
            
            // Generar o obtener QR code
            String qrImagePath = generarQRCode(documentoElectronico.getUrlQr());
            parameters.put("qrImagePath", qrImagePath);
        } else {
            parameters.put("cdc", "");
            parameters.put("urlValidacion", "");
            parameters.put("qrImagePath", "");
        }

        return parameters;
    }

    /**
     * Prepara los datos de los items para el reporte de nota de remisión.
     */
    private List<Map<String, Object>> prepararItemsNotaRemisionData(List<NotaRemisionItem> items) {
        List<Map<String, Object>> itemsData = new ArrayList<>();

        for (NotaRemisionItem item : items) {
            Map<String, Object> itemData = new HashMap<>();
            
            Producto producto = item.getProducto();
            if (producto != null) {
                itemData.put("codigo", producto.getCodigo() != null ? producto.getCodigo() : "");
            } else {
                itemData.put("codigo", "");
            }
            
            itemData.put("descripcion", item.getDescripcion() != null ? item.getDescripcion() : "");
            itemData.put("cantidad", item.getCantidad() != null ? item.getCantidad().doubleValue() : 0.0);
            itemData.put("unidadMedida", item.getUnidadMedida() != null ? item.getUnidadMedida() : "UNI");

            itemsData.add(itemData);
        }

        return itemsData;
    }

    /**
     * Mapea el ID del motivo de emisión a su descripción.
     */
    private String mapearMotivoEmisionDescripcion(String motivoId) {
        if (motivoId == null) return "";
        switch (motivoId) {
            case "1": return "Traslado por ventas";
            case "2": return "Traslado por compras";
            case "3": return "Traslado por devolución";
            case "4": return "Traslado por exportación";
            case "5": return "Traslado por importación";
            case "6": return "Traslado por consignación";
            case "7": return "Traslado entre locales de la misma empresa";
            case "8": return "Traslado por ferias";
            case "9": return "Traslado por reparación";
            case "10": return "Traslado por entrega de productos en carácter de préstamo";
            case "11": return "Traslado por exhibición";
            case "12": return "Traslado por publicidad";
            case "13": return "Traslado por transformación";
            case "14": return "Traslado por recolección de productos";
            case "99": return "Otros";
            default: return motivoId;
        }
    }

    /**
     * Genera el código QR a partir de la URL de validación.
     * Retorna la ruta del archivo temporal con la imagen del QR.
     */
    private String generarQRCode(String urlQr) throws Exception {
        if (urlQr == null || urlQr.isEmpty()) {
            return null;
        }

        try {
            // Configurar el generador de QR
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            // Generar el código QR
            BitMatrix bitMatrix = qrCodeWriter.encode(urlQr, BarcodeFormat.QR_CODE, 200, 200, hints);

            // Convertir a imagen
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }

            // Guardar en archivo temporal
            Path tempFile = Files.createTempFile("qr_", ".png");
            ImageIO.write(image, "PNG", tempFile.toFile());
            
            log.debug("QR code generado en: {}", tempFile.toString());
            return tempFile.toString();
        } catch (WriterException e) {
            log.error("Error al generar QR code: {}", e.getMessage(), e);
            return null;
        }
    }
}

