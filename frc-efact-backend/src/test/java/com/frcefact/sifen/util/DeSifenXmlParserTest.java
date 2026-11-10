package com.frcefact.sifen.util;

import com.frcefact.dto.integracion.DeConsultadoDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * El parser se prueba contra el XML real de un DE aprobado (docs/sifen/ejemplo_de_aprobado.xml),
 * tanto suelto como envuelto en una respuesta siConsDE con el contenido escapado, que es como
 * lo manda SIFEN (xContenDE es xs:string en el XSD).
 */
class DeSifenXmlParserTest {

    private static final String CDC = "01800994825001002000003822025120417284341967";
    private static String xmlEjemplo;

    @BeforeAll
    static void cargar() throws Exception {
        xmlEjemplo = Files.readString(Path.of("../docs/sifen/ejemplo_de_aprobado.xml"), StandardCharsets.UTF_8);
    }

    /** El rDE del ejemplo, sin la declaración XML, tal como iría dentro de xContenDE. */
    private static String rdeDelEjemplo() {
        int ini = xmlEjemplo.indexOf("<rDE");
        int fin = xmlEjemplo.indexOf("</rDE>") + "</rDE>".length();
        // El rDE hereda el namespace por defecto de rEnviDe; al sacarlo hay que declararlo.
        return xmlEjemplo.substring(ini, fin)
                .replaceFirst("<rDE ", "<rDE xmlns=\"http://ekuatia.set.gov.py/sifen/xsd\" ");
    }

    private static String escapar(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String respuestaSoap(String contenidoEscapado) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<env:Envelope xmlns:env=\"http://www.w3.org/2003/05/soap-envelope\"><env:Header/><env:Body>"
                + "<ns2:rEnviConsDeResponse xmlns:ns2=\"http://ekuatia.set.gov.py/sifen/xsd\">"
                + "<ns2:dFecProc>2026-11-09T10:00:00-03:00</ns2:dFecProc>"
                + "<ns2:dCodRes>0422</ns2:dCodRes><ns2:dMsgRes>CDC encontrado</ns2:dMsgRes>"
                + (contenidoEscapado == null ? "" : "<ns2:xContenDE>" + contenidoEscapado + "</ns2:xContenDE>")
                + "</ns2:rEnviConsDeResponse></env:Body></env:Envelope>";
    }

    @Test
    void leeLosDatosDelDeAprobado() {
        DeSifenXmlParser.ContenidoDe contenido = DeSifenXmlParser.leerDesdeXml(xmlEjemplo);
        DeConsultadoDto de = DeSifenXmlParser.aDto(contenido, null);

        assertEquals(CDC, de.cdc());
        assertEquals("APROBADO", de.estado());
        assertEquals(1, de.tipoDocumento());
        assertEquals("18270044", de.timbrado().numero());
        assertEquals("001-002-0000038", de.timbrado().numeroCompleto());
        assertEquals(LocalDate.of(2025, 8, 29), de.timbrado().fechaInicioVigencia());
        assertEquals(LocalDateTime.of(2025, 12, 4, 18, 20, 33), de.fechaEmision());

        assertEquals("80099482", de.emisor().ruc());
        assertEquals("5", de.emisor().dv());
        assertEquals("FRANCO AREVALOS S.A.", de.emisor().razonSocial());
        assertTrue(de.emisor().actividadesEconomicas().get(0).startsWith("46304 - "));

        assertTrue(de.receptor().contribuyente());
        assertEquals("4043581", de.receptor().ruc());
        assertEquals("4", de.receptor().dv());

        assertEquals("PYG", de.operacion().moneda());
        assertEquals(1, de.operacion().condicion());

        assertEquals(0, new BigDecimal("96000").compareTo(de.totales().subtotal10()));
        assertEquals(0, new BigDecimal("96000").compareTo(de.totales().totalGeneral()));
        assertEquals(0, new BigDecimal("8727").compareTo(de.totales().iva10()));
        assertNull(de.totales().totalGuaranies(), "dTotalGs no viene en guaraníes: no se inventa");

        assertEquals(1, de.items().size());
        DeConsultadoDto.Item item = de.items().get(0);
        assertEquals("001", item.codigo());
        assertEquals("77", item.unidadMedida());
        assertEquals(0, new BigDecimal("12").compareTo(item.cantidad()));
        assertEquals(0, new BigDecimal("8000.00").compareTo(item.precioUnitario()));
        assertEquals(0, BigDecimal.TEN.compareTo(item.tasaIva()));
        assertEquals(1, item.afectacionIva());

        assertNotNull(de.urlQr());
        assertTrue(de.xml().startsWith("<rDE"));
        assertTrue(de.xml().contains(CDC));
    }

    @Test
    void leeLaRespuestaDeConsultaConElContenidoEscapado() {
        String contenido = rdeDelEjemplo() + "<dProtAut>123456789</dProtAut>";
        DeSifenXmlParser.RespuestaConsulta r = DeSifenXmlParser.parsearRespuestaConsulta(respuestaSoap(escapar(contenido)));

        assertEquals("0422", r.codigo());
        assertEquals("CDC encontrado", r.mensaje());
        assertEquals(LocalDateTime.of(2026, 11, 9, 10, 0, 0), r.fechaProceso());
        assertTrue(r.contenido().isPresent());

        DeConsultadoDto de = DeSifenXmlParser.aDto(r.contenido().get(), r);
        assertEquals(CDC, de.cdc());
        assertEquals("123456789", de.respuestaSifen().protocoloAutorizacion());
        assertEquals("0422", de.respuestaSifen().codigo());
    }

    @Test
    void aceptaLaDeclaracionXmlDentroDelContenido() {
        String contenido = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + rdeDelEjemplo();
        DeSifenXmlParser.RespuestaConsulta r = DeSifenXmlParser.parsearRespuestaConsulta(respuestaSoap(escapar(contenido)));
        assertTrue(r.contenido().isPresent());
    }

    @Test
    void unEventoDeCancelacionMarcaElDeComoCancelado() {
        String evento = "<xContEv><rContEv><xEvento><rGesEve><rEve Id=\"99\"><dFecFirma>2025-12-05T09:00:00</dFecFirma>"
                + "<dVerFor>150</dVerFor><gGroupTiEvt><rGeVeCan><Id>" + CDC + "</Id>"
                + "<mOtEve>Error en el precio</mOtEve></rGeVeCan></gGroupTiEvt></rEve></rGesEve></xEvento></rContEv></xContEv>";
        String contenido = rdeDelEjemplo() + evento;
        DeSifenXmlParser.RespuestaConsulta r = DeSifenXmlParser.parsearRespuestaConsulta(respuestaSoap(escapar(contenido)));
        DeConsultadoDto de = DeSifenXmlParser.aDto(r.contenido().get(), r);

        assertEquals("CANCELADO", de.estado());
        assertEquals(1, de.eventos().size());
        assertEquals("CANCELACION", de.eventos().get(0).tipo());
        assertEquals("Error en el precio", de.eventos().get(0).descripcion());
        assertEquals(LocalDateTime.of(2025, 12, 5, 9, 0, 0), de.eventos().get(0).fechaFirma());
    }

    @Test
    void sinXContenDeNoHayContenido() {
        String soap = respuestaSoap(null).replace("0422", "0420").replace("CDC encontrado", "CDC inexistente");
        DeSifenXmlParser.RespuestaConsulta r = DeSifenXmlParser.parsearRespuestaConsulta(soap);
        assertEquals("0420", r.codigo());
        assertTrue(r.contenido().isEmpty());
    }

    @Test
    void rechazaDoctypeParaEvitarXxe() {
        String malicioso = "<?xml version=\"1.0\"?><!DOCTYPE x [<!ENTITY e SYSTEM \"file:///etc/passwd\">]>"
                + "<rEnviConsDeResponse><dCodRes>&e;</dCodRes></rEnviConsDeResponse>";
        assertThrows(IllegalArgumentException.class, () -> DeSifenXmlParser.parsearRespuestaConsulta(malicioso));
    }

    @Test
    void unImporteIlegibleCortaConError() {
        String roto = xmlEjemplo.replace("<dSub10>96000</dSub10>", "<dSub10>noventa</dSub10>");
        DeSifenXmlParser.ContenidoDe contenido = DeSifenXmlParser.leerDesdeXml(roto);
        assertThrows(IllegalArgumentException.class, () -> DeSifenXmlParser.aDto(contenido, null));
    }
}
