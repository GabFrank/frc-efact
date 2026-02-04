package com.frcefact.sifen.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utilidad para extraer valores de respuestas XML de SIFEN.
 */
public final class SifenResponseParser {

    private static final Logger log = LoggerFactory.getLogger(SifenResponseParser.class);
    private static final XPathFactory X_PATH_FACTORY = XPathFactory.newInstance();

    private SifenResponseParser() {
        // Utility
    }

    /**
     * Obtiene el valor de un tag (ignorando namespaces) dentro del XML.
     *
     * @param xml      contenido XML
     * @param tagName  nombre del tag (sin prefijo)
     * @return valor del tag o {@code null} si no se encuentra
     */
    public static String getTagValue(String xml, String tagName) {
        if (xml == null || xml.isBlank() || tagName == null || tagName.isBlank()) {
            return null;
        }

        try {
            Document document = parse(xml);
            XPath xPath = X_PATH_FACTORY.newXPath();
            String expression = String.format("//*[local-name()='%s']/text()", tagName);
            String value = (String) xPath.compile(expression).evaluate(document, XPathConstants.STRING);
            return value != null && !value.isBlank() ? value.trim() : null;
        } catch (Exception e) {
            log.debug("No se pudo obtener el valor del tag '{}' desde la respuesta SIFEN: {}", tagName, e.getMessage());
            return null;
        }
    }

    /**
     * Extrae el protocolo de autorización (dProtAut) de una respuesta de documento individual.
     */
    public static String extractProtocolo(String xml) {
        return getTagValue(xml, "dProtAut");
    }

    /**
     * Extrae el protocolo de recepción de lote (dProtConsLote) de una respuesta de recepción de lote.
     * Este es el protocolo que se usa para consultar el estado del lote posteriormente.
     * 
     * Busca primero con namespace ns2, luego sin namespace como fallback.
     */
    public static String extractProtocoloLote(String xml) {
        if (xml == null || xml.isBlank()) {
            return null;
        }
        
        try {
            // Intentar con namespace ns2 (formato más común en respuestas SIFEN)
            int startProtocolo = xml.indexOf("<ns2:dProtConsLote>");
            if (startProtocolo == -1) {
                startProtocolo = xml.indexOf("<dProtConsLote>");
            }
            
            if (startProtocolo != -1) {
                // Buscar el tag de cierre correspondiente
                int endProtocolo = xml.indexOf("</ns2:dProtConsLote>", startProtocolo);
                if (endProtocolo == -1) {
                    endProtocolo = xml.indexOf("</dProtConsLote>", startProtocolo);
                }
                
                if (endProtocolo > startProtocolo) {
                    // Encontrar el inicio del contenido (después de >)
                    int contentStart = xml.indexOf(">", startProtocolo) + 1;
                    if (contentStart > startProtocolo && contentStart < endProtocolo) {
                        String protocolo = xml.substring(contentStart, endProtocolo).trim();
                        if (!protocolo.isEmpty()) {
                            return protocolo;
                        }
                    }
                }
            }
            
            // Fallback: usar getTagValue que maneja namespaces automáticamente
            return getTagValue(xml, "dProtConsLote");
        } catch (Exception e) {
            log.debug("No se pudo extraer el protocolo de lote desde la respuesta SIFEN: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extrae el código de respuesta principal (dCodRes / dCodResLot).
     */
    public static String extractCodigoRespuesta(String xml) {
        String value = getTagValue(xml, "dCodRes");
        if (value == null) {
            value = getTagValue(xml, "dCodResLot");
        }
        return value;
    }

    /**
     * Extrae el mensaje de respuesta (dMsgRes / dMsgResLot).
     */
    public static String extractMensajeRespuesta(String xml) {
        String value = getTagValue(xml, "dMsgRes");
        if (value == null) {
            value = getTagValue(xml, "dMsgResLot");
        }
        return value;
    }

    /**
     * Extrae el estado de resultado (dEstRes).
     */
    public static String extractEstadoResultado(String xml) {
        return getTagValue(xml, "dEstRes");
    }

    /**
     * Extrae la URL del QR desde el XML del documento electrónico.
     * Busca el tag dCarQR dentro de gCamFuFD.
     * 
     * @param xml El XML completo del DE
     * @return La URL del QR, o null si no se encuentra
     */
    public static String extractUrlQr(String xml) {
        if (xml == null || xml.isBlank()) {
            return null;
        }
        
        // Buscar dCarQR (tag usado en el XML generado)
        String urlQr = getTagValue(xml, "dCarQR");
        if (urlQr != null && !urlQr.isBlank()) {
            return urlQr;
        }
        
        // Fallback: buscar dLinkQR (tag alternativo)
        urlQr = getTagValue(xml, "dLinkQR");
        if (urlQr != null && !urlQr.isBlank()) {
            return urlQr;
        }
        
        // Fallback: búsqueda directa en el XML (por si hay problemas con namespaces)
        try {
            int qrStart = xml.indexOf("<dCarQR>");
            if (qrStart == -1) {
                qrStart = xml.indexOf("<dLinkQR>");
            }
            
            if (qrStart > 0) {
                int tagLength = xml.substring(qrStart).startsWith("<dCarQR>") ? 8 : 9;
                int qrEnd = xml.indexOf("</dCarQR>", qrStart);
                if (qrEnd == -1) {
                    qrEnd = xml.indexOf("</dLinkQR>", qrStart);
                }
                
                if (qrEnd > qrStart) {
                    String extracted = xml.substring(qrStart + tagLength + 1, qrEnd).trim();
                    if (!extracted.isEmpty()) {
                        return extracted;
                    }
                }
            }
        } catch (Exception e) {
            log.debug("No se pudo extraer URL QR mediante búsqueda directa: {}", e.getMessage());
        }
        
        return null;
    }

    /**
     * Extrae resultados individuales de documentos dentro de una respuesta de lote.
     */
    public static List<DocumentResult> extractDocumentResults(String xml) {
        if (xml == null || xml.isBlank()) {
            return Collections.emptyList();
        }
        try {
            Document document = parse(xml);
            XPath xPath = X_PATH_FACTORY.newXPath();
            var expression = xPath.compile("//*[local-name()='gResProcDE']");
            var nodes = (org.w3c.dom.NodeList) expression.evaluate(document, XPathConstants.NODESET);
            List<DocumentResult> results = new ArrayList<>();

            for (int i = 0; i < nodes.getLength(); i++) {
                var node = nodes.item(i);
                String cdc = getChildValue(node, "dCDC");
                String codigo = getChildValue(node, "dCodRes");
                String estado = getChildValue(node, "dEstRes");
                String mensaje = getChildValue(node, "dMsgRes");
                String protocolo = getChildValue(node, "dProtAut");

                if (cdc != null) {
                    results.add(new DocumentResult(cdc, codigo, estado, mensaje, protocolo));
                }
            }
            return results;
        } catch (Exception e) {
            log.debug("No se pudieron extraer resultados de documentos del XML SIFEN: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Parsea el XML en un documento DOM.
     */
    private static Document parse(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        try (StringReader reader = new StringReader(xml)) {
            return builder.parse(new InputSource(reader));
        }
    }

    private static String getChildValue(org.w3c.dom.Node node, String tagName) throws Exception {
        XPath xPath = X_PATH_FACTORY.newXPath();
        String expression = String.format(".//*[local-name()='%s']/text()", tagName);
        String value = (String) xPath.compile(expression).evaluate(node, XPathConstants.STRING);
        return value != null && !value.isBlank() ? value.trim() : null;
    }

    public static final class DocumentResult {
        private final String cdc;
        private final String codigo;
        private final String estado;
        private final String mensaje;
        private final String protocolo;

        public DocumentResult(String cdc, String codigo, String estado, String mensaje, String protocolo) {
            this.cdc = cdc;
            this.codigo = codigo;
            this.estado = estado;
            this.mensaje = mensaje;
            this.protocolo = protocolo;
        }

        public String getCdc() {
            return cdc;
        }

        public String getCodigo() {
            return codigo;
        }

        public String getEstado() {
            return estado;
        }

        public String getMensaje() {
            return mensaje;
        }

        public String getProtocolo() {
            return protocolo;
        }
    }

    /**
     * Extrae los resultados de eventos asociados al documento.
     * 
     * Busca eventos en la estructura:
     * xContEv/rContEv/xEvento/rGesEve/rEve (con Id como atributo)
     * y su respuesta en rResEnviEventoDe/rRetEnviEventoDe/gResProcEVe
     */
    public static List<EventResult> extractEventResults(String xml) {
        if (xml == null || xml.isBlank()) {
            return Collections.emptyList();
        }
        try {
            Document document = parse(xml);
            XPath xPath = X_PATH_FACTORY.newXPath();
            List<EventResult> results = new ArrayList<>();
            
            // Buscar el contenedor de eventos xContEv
            var contEvExpression = xPath.compile("//*[local-name()='xContEv']");
            var contEvNodes = (org.w3c.dom.NodeList) contEvExpression.evaluate(document, XPathConstants.NODESET);
            
            if (contEvNodes.getLength() == 0) {
                log.debug("No se encontró contenedor xContEv en la respuesta");
                return Collections.emptyList();
            }
            
            // Buscar todos los rEve (eventos) dentro de xContEv
            // El ID del evento está en el atributo Id de rEve
            var rEveExpression = xPath.compile("//*[local-name()='xContEv']//*[local-name()='rEve']");
            var rEveNodes = (org.w3c.dom.NodeList) rEveExpression.evaluate(document, XPathConstants.NODESET);
            
            log.debug("Se encontraron {} evento(s) rEve en xContEv", rEveNodes.getLength());
            
            for (int i = 0; i < rEveNodes.getLength(); i++) {
                var rEveNode = rEveNodes.item(i);
                
                // Obtener el ID del evento desde el atributo Id de rEve
                String eventoId = null;
                if (rEveNode.getAttributes() != null && rEveNode.getAttributes().getNamedItem("Id") != null) {
                    eventoId = rEveNode.getAttributes().getNamedItem("Id").getNodeValue();
                }
                
                // Determinar el tipo de evento
                String tipoEvento = null;
                var rGeVeCanNodes = xPath.compile(".//*[local-name()='rGeVeCan']").evaluate(rEveNode, XPathConstants.NODESET);
                if (rGeVeCanNodes instanceof org.w3c.dom.NodeList && ((org.w3c.dom.NodeList) rGeVeCanNodes).getLength() > 0) {
                    tipoEvento = "CANCELACION";
                    log.debug("Evento {} es de cancelación", eventoId);
                } else {
                    var rGeVeNotRecNodes = xPath.compile(".//*[local-name()='rGeVeNotRec']").evaluate(rEveNode, XPathConstants.NODESET);
                    if (rGeVeNotRecNodes instanceof org.w3c.dom.NodeList && ((org.w3c.dom.NodeList) rGeVeNotRecNodes).getLength() > 0) {
                        tipoEvento = "NOMINACION";
                        log.debug("Evento {} es de nominación", eventoId);
                    }
                }
                
                // Buscar la respuesta del evento (gResProcEVe) que está en rResEnviEventoDe/rRetEnviEventoDe
                // Necesitamos buscar desde el nodo padre (rContEv) hacia adelante
                var parentNode = rEveNode.getParentNode();
                while (parentNode != null && !parentNode.getNodeName().contains("rContEv")) {
                    parentNode = parentNode.getParentNode();
                }
                
                if (parentNode != null) {
                    // Buscar gResProcEVe dentro del mismo rContEv
                    var gResProcEVeExpression = xPath.compile(".//*[local-name()='gResProcEVe']");
                    var gResProcEVeNodes = (org.w3c.dom.NodeList) gResProcEVeExpression.evaluate(parentNode, XPathConstants.NODESET);
                    
                    if (gResProcEVeNodes.getLength() > 0) {
                        var gResProcEVeNode = gResProcEVeNodes.item(0);
                        
                        String estado = getChildValue(gResProcEVeNode, "dEstRes");
                        String protocolo = getChildValue(gResProcEVeNode, "dProtAut");
                        
                        // Buscar código y mensaje en gResProc dentro de gResProcEVe
                        var gResProcNode = xPath.compile(".//*[local-name()='gResProc']").evaluate(gResProcEVeNode, XPathConstants.NODESET);
                        String codigo = null;
                        String mensaje = null;
                        if (gResProcNode instanceof org.w3c.dom.NodeList && ((org.w3c.dom.NodeList) gResProcNode).getLength() > 0) {
                            var gResProc = ((org.w3c.dom.NodeList) gResProcNode).item(0);
                            codigo = getChildValue(gResProc, "dCodRes");
                            mensaje = getChildValue(gResProc, "dMsgRes");
                        }
                        
                        if (eventoId != null || estado != null) {
                            log.info("✅ Evento extraído - ID: {}, Tipo: {}, Estado: {}, Protocolo: {}", 
                                    eventoId, tipoEvento != null ? tipoEvento : "Desconocido", estado, protocolo);
                            results.add(new EventResult(eventoId, codigo, estado, mensaje, protocolo, tipoEvento));
                        }
                    }
                }
            }
            
            return results;
        } catch (Exception e) {
            log.error("Error al extraer resultados de eventos del XML SIFEN: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public static final class EventResult {
        private final String id;
        private final String codigo;
        private final String estado;
        private final String mensaje;
        private final String protocolo;
        private final String tipoEvento; // "CANCELACION", "NOMINACION", etc.

        public EventResult(String id, String codigo, String estado, String mensaje, String protocolo) {
            this(id, codigo, estado, mensaje, protocolo, null);
        }
        
        public EventResult(String id, String codigo, String estado, String mensaje, String protocolo, String tipoEvento) {
            this.id = id;
            this.codigo = codigo;
            this.estado = estado;
            this.mensaje = mensaje;
            this.protocolo = protocolo;
            this.tipoEvento = tipoEvento;
        }

        public String getId() { return id; }
        public String getCodigo() { return codigo; }
        public String getEstado() { return estado; }
        public String getMensaje() { return mensaje; }
        public String getProtocolo() { return protocolo; }
        public String getTipoEvento() { return tipoEvento; }
    }
}

