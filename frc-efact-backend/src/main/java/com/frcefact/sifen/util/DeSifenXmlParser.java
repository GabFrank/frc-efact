package com.frcefact.sifen.util;

import com.frcefact.dto.integracion.DeConsultadoDto;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lee la respuesta de la consulta de DE por CDC de SIFEN (siConsDE) y el XML del DE.
 *
 * <p>Por qué no se usa {@code RespuestaConsultaDE.getxContenDE()} de jsifenlib: el XSD de SIFEN
 * declara {@code xContenDE} como {@code xs:string}, o sea que el DE viaja como texto escapado
 * dentro de la respuesta, y jsifenlib solo lo lee si viene como nodos hijos. Este parser acepta
 * las dos formas.
 *
 * <p>Todo se lee por nombre local, sin depender de prefijos de namespace. Los valores se toman
 * tal cual vienen en el XML: un campo ausente queda en {@code null}, nunca con un default.
 */
public final class DeSifenXmlParser {

    private static final Map<String, String> TIPOS_EVENTO = Map.of(
            "rGeVeCan", "CANCELACION",
            "rGeVeInu", "INUTILIZACION",
            "rGeVeConf", "CONFORMIDAD",
            "rGeVeDisconf", "DISCONFORMIDAD",
            "rGeVeDescon", "DESCONOCIMIENTO",
            "rGeVeNotRec", "NOTIFICACION_RECEPCION",
            "rGeVeTr", "ACTUALIZACION_DATOS_TRANSPORTE",
            "rGeVeRem", "REMISION");

    private DeSifenXmlParser() {
    }

    /** Resultado de una consulta a SIFEN. {@code contenido} vacío = SIFEN no devolvió el DE. */
    public record RespuestaConsulta(String codigo, String mensaje, LocalDateTime fechaProceso,
                                    Optional<ContenidoDe> contenido) {
    }

    /**
     * Contenido devuelto por SIFEN: el {@code rDE}, el protocolo de autorización y los eventos.
     *
     * @param xmlRde        el {@code rDE} serializado (DE firmado + QR), sin declaración XML
     * @param rde           el mismo {@code rDE} ya parseado
     * @param contenedor    el contenedor completo (para buscar protocolo y eventos)
     */
    public record ContenidoDe(String xmlRde, Element rde, Element contenedor) {
    }

    // ------------------------------------------------------------------------------------------
    // Respuesta SOAP
    // ------------------------------------------------------------------------------------------

    public static RespuestaConsulta parsearRespuestaConsulta(String soapRaw) {
        if (soapRaw == null || soapRaw.isBlank()) {
            throw new IllegalArgumentException("Respuesta vacía de SIFEN");
        }
        Document doc = parsear(soapRaw);
        Element raiz = doc.getDocumentElement();
        String codigo = texto(raiz, "dCodRes");
        String mensaje = texto(raiz, "dMsgRes");
        LocalDateTime fechaProceso = fechaHora(texto(raiz, "dFecProc"));

        Element xContenDE = primero(raiz, "xContenDE");
        return new RespuestaConsulta(codigo, mensaje, fechaProceso, Optional.ofNullable(xContenDE)
                .flatMap(DeSifenXmlParser::leerContenedor));
    }

    private static Optional<ContenidoDe> leerContenedor(Element xContenDE) {
        Element contenedor;
        if (primero(xContenDE, "rDE") != null) {
            contenedor = xContenDE;
        } else {
            String textoContenido = xContenDE.getTextContent();
            if (textoContenido == null || textoContenido.isBlank()) {
                return Optional.empty();
            }
            // Puede traer una o más declaraciones <?xml ...?>, que no son válidas dentro de un envoltorio.
            String limpio = textoContenido.replaceAll("<\\?xml[^>]*\\?>", "").trim();
            contenedor = parsear("<contenedor>" + limpio + "</contenedor>").getDocumentElement();
        }
        Element rde = primero(contenedor, "rDE");
        if (rde == null) {
            return Optional.empty();
        }
        return Optional.of(new ContenidoDe(serializar(rde), rde, contenedor));
    }

    /** Lee un XML que tenga un {@code rDE} en cualquier nivel (respuesta de consulta, envío, o rDE suelto). */
    public static ContenidoDe leerDesdeXml(String xml) {
        Element raiz = parsear(xml).getDocumentElement();
        Element xContenDE = primero(raiz, "xContenDE");
        if (xContenDE != null) {
            return leerContenedor(xContenDE)
                    .orElseThrow(() -> new IllegalArgumentException("El xContenDE no contiene un rDE"));
        }
        Element rde = "rDE".equals(raiz.getLocalName()) ? raiz : primero(raiz, "rDE");
        if (rde == null) {
            throw new IllegalArgumentException("El XML no contiene un rDE");
        }
        return new ContenidoDe(serializar(rde), rde, raiz);
    }

    // ------------------------------------------------------------------------------------------
    // DE → DTO
    // ------------------------------------------------------------------------------------------

    public static DeConsultadoDto aDto(ContenidoDe contenido, RespuestaConsulta respuesta) {
        Element rde = contenido.rde();
        Element de = primero(rde, "DE");
        if (de == null) {
            throw new IllegalArgumentException("El rDE no contiene el nodo DE");
        }

        List<DeConsultadoDto.Evento> eventos = leerEventos(contenido.contenedor());
        boolean cancelado = eventos.stream().anyMatch(e -> "CANCELACION".equals(e.tipo()));

        Element gTimb = primero(de, "gTimb");
        String est = texto(gTimb, "dEst");
        String pun = texto(gTimb, "dPunExp");
        String num = texto(gTimb, "dNumDoc");
        String numeroCompleto = (est != null && pun != null && num != null) ? est + "-" + pun + "-" + num : null;

        DeConsultadoDto.RespuestaSifen respuestaSifen = respuesta == null ? null : new DeConsultadoDto.RespuestaSifen(
                respuesta.codigo(), respuesta.mensaje(), respuesta.fechaProceso(),
                texto(contenido.contenedor(), "dProtAut"));

        Element gEmis = primero(de, "gEmis");
        List<String> actividades = new ArrayList<>();
        for (Element act : todos(gEmis, "gActEco")) {
            String desc = texto(act, "dDesActEco");
            String cod = texto(act, "cActEco");
            if (desc != null || cod != null) {
                actividades.add(cod != null && desc != null ? cod + " - " + desc : (desc != null ? desc : cod));
            }
        }
        DeConsultadoDto.Emisor emisor = gEmis == null ? null : new DeConsultadoDto.Emisor(
                texto(gEmis, "dRucEm"), texto(gEmis, "dDVEmi"), texto(gEmis, "dNomEmi"), texto(gEmis, "dNomFanEmi"),
                texto(gEmis, "dDirEmi"), texto(gEmis, "dTelEmi"), texto(gEmis, "dEmailE"),
                texto(gEmis, "dDesCiuEmi"), texto(gEmis, "dDesDepEmi"),
                actividades.isEmpty() ? null : actividades);

        Element gDatRec = primero(de, "gDatRec");
        DeConsultadoDto.Receptor receptor = gDatRec == null ? null : new DeConsultadoDto.Receptor(
                entero(texto(gDatRec, "iNatRec")) == null ? null : entero(texto(gDatRec, "iNatRec")) == 1,
                texto(gDatRec, "dRucRec"), texto(gDatRec, "dDVRec"), texto(gDatRec, "dDTipIDRec"),
                texto(gDatRec, "dNumIDRec"), texto(gDatRec, "dNomRec"), texto(gDatRec, "dNomFanRec"),
                texto(gDatRec, "dDirRec"), primerNoNulo(texto(gDatRec, "dTelRec"), texto(gDatRec, "dCelRec")),
                texto(gDatRec, "dEmailRec"), texto(gDatRec, "dDesCiuRec"), texto(gDatRec, "dDesDepRec"));

        Element gOpeCom = primero(de, "gOpeCom");
        Element gCamCond = primero(de, "gCamCond");
        DeConsultadoDto.Operacion operacion = new DeConsultadoDto.Operacion(
                texto(gOpeCom, "cMoneOpe"), texto(gOpeCom, "dDesMoneOpe"), decimal(texto(gOpeCom, "dTiCam")),
                texto(gOpeCom, "dDesTipTra"),
                entero(texto(gCamCond, "iCondOpe")), texto(gCamCond, "dDCondOpe"),
                texto(gCamCond, "dPlazoCre"));

        Element gTotSub = primero(de, "gTotSub");
        DeConsultadoDto.Totales totales = gTotSub == null ? null : new DeConsultadoDto.Totales(
                decimal(texto(gTotSub, "dSubExe")), decimal(texto(gTotSub, "dSub5")), decimal(texto(gTotSub, "dSub10")),
                decimal(texto(gTotSub, "dTotOpe")), decimal(texto(gTotSub, "dTotDesc")),
                decimal(texto(gTotSub, "dTotGralOpe")), decimal(texto(gTotSub, "dIVA5")),
                decimal(texto(gTotSub, "dIVA10")), decimal(texto(gTotSub, "dTotIVA")),
                decimal(texto(gTotSub, "dBaseGrav5")), decimal(texto(gTotSub, "dBaseGrav10")),
                decimal(texto(gTotSub, "dTotalGs")));

        List<String> asociados = new ArrayList<>();
        for (Element asoc : todos(de, "gCamDEAsoc")) {
            String ref = texto(asoc, "dCdCDERef");
            if (ref != null) {
                asociados.add(ref);
            }
        }

        Element gCamNCDE = primero(de, "gCamNCDE");
        DeConsultadoDto.MotivoEmision motivo = gCamNCDE == null ? null
                : new DeConsultadoDto.MotivoEmision(entero(texto(gCamNCDE, "iMotEmi")), texto(gCamNCDE, "dDesMotEmi"));

        return new DeConsultadoDto(
                de.getAttribute("Id"),
                cancelado ? "CANCELADO" : "APROBADO",
                respuestaSifen,
                entero(texto(gTimb, "iTiDE")),
                texto(gTimb, "dDesTiDE"),
                gTimb == null ? null : new DeConsultadoDto.Timbrado(texto(gTimb, "dNumTim"),
                        fecha(texto(gTimb, "dFeIniT")), est, pun, num, numeroCompleto),
                fechaHora(texto(de, "dFeEmiDE")),
                fechaHora(texto(de, "dFecFirma")),
                emisor,
                receptor,
                operacion,
                totales,
                asociados.isEmpty() ? null : asociados,
                motivo,
                eventos.isEmpty() ? null : eventos,
                texto(rde, "dCarQR"),
                leerItems(de),
                contenido.xmlRde());
    }

    private static List<DeConsultadoDto.Item> leerItems(Element de) {
        List<DeConsultadoDto.Item> items = new ArrayList<>();
        for (Element it : todos(de, "gCamItem")) {
            items.add(new DeConsultadoDto.Item(
                    texto(it, "dCodInt"), texto(it, "dDesProSer"), texto(it, "cUniMed"), texto(it, "dDesUniMed"),
                    decimal(texto(it, "dCantProSer")), decimal(texto(it, "dPUniProSer")),
                    decimal(texto(it, "dTotBruOpeItem")), decimal(texto(it, "dDescItem")),
                    decimal(texto(it, "dTotOpeItem")), entero(texto(it, "iAfecIVA")), texto(it, "dDesAfecIVA"),
                    decimal(texto(it, "dTasaIVA")), decimal(texto(it, "dBasGravIVA")),
                    decimal(texto(it, "dLiqIVAItem")), decimal(texto(it, "dBasExe"))));
        }
        return items;
    }

    private static List<DeConsultadoDto.Evento> leerEventos(Element contenedor) {
        List<DeConsultadoDto.Evento> eventos = new ArrayList<>();
        for (Element rEve : todos(contenedor, "rEve")) {
            Element grupo = primero(rEve, "gGroupTiEvt");
            String tipoXml = null;
            if (grupo != null) {
                for (Node n = grupo.getFirstChild(); n != null; n = n.getNextSibling()) {
                    if (n instanceof Element e) {
                        tipoXml = e.getLocalName();
                        break;
                    }
                }
            }
            String tipo = tipoXml == null ? null : TIPOS_EVENTO.getOrDefault(tipoXml, tipoXml);
            String descripcion = grupo == null ? null : primerNoNulo(texto(grupo, "mOtEve"), texto(grupo, "mOtEveCan"));
            eventos.add(new DeConsultadoDto.Evento(rEve.getAttribute("Id"), tipo, descripcion,
                    fechaHora(texto(rEve, "dFecFirma"))));
        }
        return eventos;
    }

    // ------------------------------------------------------------------------------------------
    // Utilidades XML
    // ------------------------------------------------------------------------------------------

    private static Document parsear(String xml) {
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setNamespaceAware(true);
            // XXE: el XML viene de afuera. Sin DTD ni entidades externas.
            f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            f.setFeature("http://xml.org/sax/features/external-general-entities", false);
            f.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            f.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            f.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            f.setXIncludeAware(false);
            f.setExpandEntityReferences(false);
            DocumentBuilder b = f.newDocumentBuilder();
            return b.parse(new InputSource(new StringReader(xml)));
        } catch (Exception e) {
            throw new IllegalArgumentException("XML inválido: " + e.getMessage(), e);
        }
    }

    private static String serializar(Element elemento) {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            Transformer t = tf.newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            StringWriter w = new StringWriter();
            t.transform(new DOMSource(elemento), new StreamResult(w));
            return w.toString();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo serializar el rDE: " + e.getMessage(), e);
        }
    }

    private static Element primero(Element base, String nombreLocal) {
        if (base == null) {
            return null;
        }
        NodeList nl = base.getElementsByTagNameNS("*", nombreLocal);
        return nl.getLength() > 0 ? (Element) nl.item(0) : null;
    }

    private static List<Element> todos(Element base, String nombreLocal) {
        List<Element> lista = new ArrayList<>();
        if (base == null) {
            return lista;
        }
        NodeList nl = base.getElementsByTagNameNS("*", nombreLocal);
        for (int i = 0; i < nl.getLength(); i++) {
            lista.add((Element) nl.item(i));
        }
        return lista;
    }

    private static String texto(Element base, String nombreLocal) {
        Element e = primero(base, nombreLocal);
        if (e == null) {
            return null;
        }
        String t = e.getTextContent();
        return t == null || t.isBlank() ? null : t.trim();
    }

    private static String primerNoNulo(String a, String b) {
        return a != null ? a : b;
    }

    private static BigDecimal decimal(String v) {
        if (v == null) {
            return null;
        }
        try {
            return new BigDecimal(v);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Valor numérico inválido en el DE: '" + v + "'");
        }
    }

    private static Integer entero(String v) {
        if (v == null) {
            return null;
        }
        try {
            return Integer.valueOf(v);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Valor entero inválido en el DE: '" + v + "'");
        }
    }

    private static LocalDate fecha(String v) {
        if (v == null) {
            return null;
        }
        try {
            return LocalDate.parse(v.length() > 10 ? v.substring(0, 10) : v);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static LocalDateTime fechaHora(String v) {
        if (v == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(v);
        } catch (DateTimeParseException e) {
            try {
                return OffsetDateTime.parse(v).toLocalDateTime();
            } catch (DateTimeParseException e2) {
                return null;
            }
        }
    }
}
