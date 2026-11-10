package com.frcefact.service;

import com.frcefact.dto.integracion.DeConsultadoDto;
import com.frcefact.sifen.util.DeSifenXmlParser;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/** El KuDE se arma solo con lo que trae el XML, sin entidades de eFact. */
class KudePdfServiceDesdeSifenTest {

    private static DeConsultadoDto deDelEjemplo(String reemplazo, String por) throws Exception {
        String xml = Files.readString(Path.of("../docs/sifen/ejemplo_de_aprobado.xml"), StandardCharsets.UTF_8);
        if (reemplazo != null) {
            xml = xml.replace(reemplazo, por);
        }
        return DeSifenXmlParser.aDto(DeSifenXmlParser.leerDesdeXml(xml), null);
    }

    @Test
    void generaElPdfDeUnaFactura() throws Exception {
        byte[] pdf = new KudePdfService().generarPdfKudeDesdeSifen(deDelEjemplo(null, null));
        assertTrue(pdf.length > 1000);
        assertEquals("%PDF", new String(pdf, 0, 4, StandardCharsets.US_ASCII));
    }

    @Test
    void unItemGravadoSinTasaCortaEnVezDeAsumirUna() throws Exception {
        DeConsultadoDto de = deDelEjemplo("<dTasaIVA>10</dTasaIVA>", "");
        assertThrows(IllegalStateException.class, () -> new KudePdfService().generarPdfKudeDesdeSifen(de));
    }

    @Test
    void tiposSinPlantillaNoSeGeneran() {
        assertTrue(KudePdfService.soportaKudeDesdeSifen(1));
        assertTrue(KudePdfService.soportaKudeDesdeSifen(5));
        assertFalse(KudePdfService.soportaKudeDesdeSifen(7));
        assertFalse(KudePdfService.soportaKudeDesdeSifen(null));
    }
}
