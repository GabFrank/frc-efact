package com.frcefact.service;

import com.frcefact.exception.BusinessException;
import com.frcefact.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * Servicio para generación de XML de documentos electrónicos según especificación SIFEN.
 * Genera XML original, CDC y URL QR.
 * 
 * NOTA: Este servicio genera el XML usando jsifenlib. La firma digital se realiza
 * automáticamente por jsifenlib cuando se envía el documento a SIFEN.
 */
@Service
public class XmlGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(XmlGeneratorService.class);
    private static final DateTimeFormatter CDC_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String QR_BASE_URL = "https://ekuatia.set.gov.py/consultas/qr?nVersion=150&Id=";

    /**
     * Genera el XML original del documento electrónico a partir de una factura legal.
     * 
     * IMPORTANTE: Este método genera un XML simplificado. El XML completo con todos
     * los campos requeridos por SIFEN se genera cuando se envía el lote usando jsifenlib.
     * 
     * @param factura Factura legal
     * @return XML original sin firmar
     */
    public String generarXmlOriginal(FacturaLegal factura) {
        log.debug("🔧 Generando XML original para factura ID: {}", factura.getId());
        
        // Validar datos requeridos
        if (factura.getEmpresa() == null) {
            throw new BusinessException("La factura no tiene empresa asociada");
        }
        if (factura.getEmpresa().getRuc() == null || factura.getEmpresa().getRuc().isBlank()) {
            throw new BusinessException("La empresa no tiene RUC configurado");
        }
        if (factura.getTimbradoDetalle() == null) {
            throw new BusinessException("La factura no tiene timbrado detalle configurado");
        }
        if (factura.getTimbradoDetalle().getTimbrado() == null) {
            throw new BusinessException("El timbrado detalle no tiene timbrado asociado");
        }
        if (factura.getItems() == null || factura.getItems().isEmpty()) {
            throw new BusinessException("La factura no tiene items");
        }
        
        log.debug("   - Empresa RUC: {}", factura.getEmpresa().getRuc());
        log.debug("   - Timbrado: {}", factura.getTimbradoDetalle().getTimbrado().getNumero());
        log.debug("   - Items: {}", factura.getItems().size());
        
        try {
            // Generar XML simplificado con los datos básicos
            // El XML completo se generará cuando se envíe a SIFEN usando jsifenlib
            StringBuilder xml = new StringBuilder();
            xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            xml.append("<rDE xmlns=\"http://ekuatia.set.gov.py/sifen/xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n");
            
            // Datos básicos del documento
            xml.append("  <DE>\n");
            xml.append("    <dVerFor>150</dVerFor>\n");
            xml.append("    <dFecFirma>").append(factura.getFecha().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("</dFecFirma>\n");
            
            // Datos del emisor
            xml.append("    <gEmis>\n");
            xml.append("      <dRucEm>").append(factura.getEmpresa().getRuc()).append("</dRucEm>\n");
            xml.append("      <dNomEmi>").append(escapeXml(factura.getEmpresa().getRazonSocial())).append("</dNomEmi>\n");
            xml.append("    </gEmis>\n");
            
            // Datos del receptor
            xml.append("    <gDatRec>\n");
            if (factura.getRuc() != null && !factura.getRuc().isEmpty()) {
                xml.append("      <dRucRec>").append(factura.getRuc()).append("</dRucRec>\n");
            }
            xml.append("      <dNomRec>").append(escapeXml(factura.getNombre())).append("</dNomRec>\n");
            xml.append("    </gDatRec>\n");
            
            // Timbrado
            xml.append("    <gTimb>\n");
            xml.append("      <dNumTim>").append(factura.getTimbradoDetalle().getTimbrado().getNumero()).append("</dNumTim>\n");
            xml.append("      <dEst>").append(factura.getTimbradoDetalle().getCodigoEstablecimientoFactura()).append("</dEst>\n");
            xml.append("      <dPunExp>").append(factura.getTimbradoDetalle().getPuntoExpedicion()).append("</dPunExp>\n");
            xml.append("      <dNumDoc>").append(String.format("%07d", factura.getNumeroFactura())).append("</dNumDoc>\n");
            xml.append("    </gTimb>\n");
            
            // Items
            for (int i = 0; i < factura.getItems().size(); i++) {
                FacturaLegalItem item = factura.getItems().get(i);
                xml.append("    <gCamItem>\n");
                xml.append("      <dNumItem>").append(i + 1).append("</dNumItem>\n");
                xml.append("      <dDesProSer>").append(escapeXml(item.getDescripcion())).append("</dDesProSer>\n");
                xml.append("      <dCantProSer>").append(item.getCantidad()).append("</dCantProSer>\n");
                xml.append("      <dPUniProSer>").append(item.getPrecioUnitario()).append("</dPUniProSer>\n");
                xml.append("      <dTotOpeItem>").append(item.getTotal()).append("</dTotOpeItem>\n");
                xml.append("    </gCamItem>\n");
            }
            
            // Totales
            xml.append("    <gTotSub>\n");
            xml.append("      <dTotGralOpe>").append(factura.getTotalFinal()).append("</dTotGralOpe>\n");
            xml.append("      <dTotIVA>").append(factura.getIvaParcial5().add(factura.getIvaParcial10())).append("</dTotIVA>\n");
            xml.append("    </gTotSub>\n");
            
            xml.append("  </DE>\n");
            xml.append("</rDE>");
            
            String xmlString = xml.toString();
            log.info("✅ XML original generado para factura ID: {}", factura.getId());
            return xmlString;
            
        } catch (Exception e) {
            log.error("❌ Error al generar XML para factura ID: {}", factura.getId(), e);
            throw new BusinessException("Error al generar XML del documento electrónico: " + e.getMessage());
        }
    }

    /**
     * Genera el CDC (Código de Control del Documento) de 44 caracteres.
     * Formato: TTAAAAAAAACCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC
     * TT = Tipo de documento (01 = Factura)
     * AAAAAAAA = RUC del emisor (8 dígitos)
     * CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC = Código de control (34 caracteres)
     * 
     * @param factura Factura legal
     * @param codigoSeguridad Código de seguridad del DE
     * @return CDC de 44 caracteres
     */
    public String generarCDC(FacturaLegal factura, String codigoSeguridad) {
        log.debug("🔧 Generando CDC para factura ID: {}", factura.getId());
        
        try {
            StringBuilder cdcBuilder = new StringBuilder();
            
            // TT: Tipo de documento (01 = Factura electrónica)
            cdcBuilder.append("01");
            
            // RUC del emisor (8 dígitos, sin DV)
            String ruc = factura.getEmpresa().getRuc().replace("-", "");
            if (ruc.length() > 8) {
                ruc = ruc.substring(0, 8); // Tomar solo los primeros 8 dígitos
            }
            cdcBuilder.append(String.format("%8s", ruc).replace(' ', '0'));
            
            // DV del RUC (1 dígito)
            String dvRuc = factura.getEmpresa().getRuc().contains("-") 
                ? factura.getEmpresa().getRuc().substring(factura.getEmpresa().getRuc().lastIndexOf("-") + 1)
                : "0";
            cdcBuilder.append(dvRuc);
            
            // Punto de expedición (3 dígitos)
            String puntoExpedicion = factura.getTimbradoDetalle().getPuntoExpedicion();
            cdcBuilder.append(String.format("%03d", Integer.parseInt(puntoExpedicion)));
            
            // Código de establecimiento (3 dígitos)
            String codEstablecimiento = factura.getTimbradoDetalle().getCodigoEstablecimientoFactura();
            cdcBuilder.append(String.format("%03d", Integer.parseInt(codEstablecimiento)));
            
            // Número de documento (7 dígitos)
            cdcBuilder.append(String.format("%07d", factura.getNumeroFactura()));
            
            // Tipo de contribuyente del receptor (1 dígito)
            // 1=Contribuyente, 2=No contribuyente, 3=Extranjero
            String tipoContribuyente = determinarTipoContribuyente(factura);
            cdcBuilder.append(tipoContribuyente);
            
            // Fecha de emisión (YYYYMMDD - 8 dígitos)
            String fecha = factura.getFecha().format(CDC_DATE_FORMAT);
            cdcBuilder.append(fecha);
            
            // Tipo de emisión (1 dígito) - 1=Normal
            cdcBuilder.append("1");
            
            // Código de seguridad (9 dígitos)
            cdcBuilder.append(String.format("%09d", Long.parseLong(codigoSeguridad)));
            
            // Dígito verificador (1 dígito) - Módulo 11
            int dv = calcularDigitoVerificador(cdcBuilder.toString());
            cdcBuilder.append(dv);
            
            String cdc = cdcBuilder.toString();
            
            if (cdc.length() != 44) {
                throw new BusinessException("CDC generado tiene longitud incorrecta: " + cdc.length());
            }
            
            log.info("✅ CDC generado: {} para factura ID: {}", cdc, factura.getId());
            return cdc;
            
        } catch (Exception e) {
            log.error("❌ Error al generar CDC para factura ID: {}", factura.getId(), e);
            throw new BusinessException("Error al generar CDC: " + e.getMessage());
        }
    }

    /**
     * Genera la URL del código QR para consulta del documento.
     * 
     * @param cdc Código de Control del Documento
     * @return URL del QR
     */
    public String generarUrlQr(String cdc) {
        if (cdc == null || cdc.length() != 44) {
            throw new BusinessException("CDC inválido para generar URL QR");
        }
        return QR_BASE_URL + cdc;
    }

    // Métodos auxiliares privados

    private String escapeXml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }

    /**
     * Genera un código de seguridad aleatorio de 9 dígitos.
     * 
     * @return Código de seguridad
     */
    public String generarCodigoSeguridad() {
        // Generar código de seguridad aleatorio de 9 dígitos
        Random random = new Random();
        return String.format("%09d", random.nextInt(1000000000));
    }

    private String determinarTipoContribuyente(FacturaLegal factura) {
        if (factura.getRuc() != null && !factura.getRuc().isEmpty()) {
            return "1"; // Contribuyente
        }
        return "2"; // No contribuyente
    }

    private int calcularDigitoVerificador(String cadena) {
        // Algoritmo Módulo 11 para dígito verificador
        int suma = 0;
        int multiplicador = 2;
        
        for (int i = cadena.length() - 1; i >= 0; i--) {
            int digito = Character.getNumericValue(cadena.charAt(i));
            suma += digito * multiplicador;
            multiplicador++;
            if (multiplicador > 9) {
                multiplicador = 2;
            }
        }
        
        int resto = suma % 11;
        int dv = 11 - resto;
        
        if (dv == 11) {
            return 0;
        } else if (dv == 10) {
            return 1;
        }
        return dv;
    }
}
