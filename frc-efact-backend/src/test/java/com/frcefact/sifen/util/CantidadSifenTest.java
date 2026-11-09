package com.frcefact.sifen.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de la normalización de {@code dCantProSer}.
 *
 * <p>El caso que motivó todo: una factura de 575,195 toneladas de maíz a USD 150 se emitió con
 * {@code dCantProSer = 575}, y como jsifenlib deriva los totales de
 * {@code dPUniProSer × dCantProSer}, el DE salió por USD 86.250 en vez de 86.279,25. El cliente
 * tuvo que cancelarlo.
 */
class CantidadSifenTest {

    @Test
    @DisplayName("El caso de producción: 575,195 no se trunca a 575")
    void noTruncaElCasoReal() {
        BigDecimal resultado = CantidadSifen.normalizar(new BigDecimal("575.195"));

        assertEquals(new BigDecimal("575.195"), resultado);

        // La consecuencia concreta: el total que jsifenlib deriva tiene que dar el correcto.
        BigDecimal total = resultado.multiply(new BigDecimal("150"));
        assertEquals(0, new BigDecimal("86279.250").compareTo(total),
                "El total derivado debe ser 86.279,25 y no 86.250");
    }

    @Test
    @DisplayName("Un entero se emite sin decimales sobrantes")
    void enteroQuedaLimpio() {
        assertEquals("3", CantidadSifen.normalizar(new BigDecimal("3")).toString());
    }

    @Test
    @DisplayName("Los ceros a la derecha se descartan: 575,000 → 575")
    void descartaCerosALaDerecha() {
        assertEquals("575", CantidadSifen.normalizar(new BigDecimal("575.000")).toString());
    }

    @Test
    @DisplayName("REGRESIÓN: nunca notación científica en el XML")
    void nuncaNotacionCientifica() {
        // stripTrailingZeros() sobre "575.000" devuelve 5.75E+2, y jsifenlib serializa con
        // String.valueOf(dCantProSer), así que eso iría tal cual al XML y SIFEN lo rechazaría.
        for (String entrada : new String[]{"575.000", "100", "1000.0000", "20.00", "3000"}) {
            String salida = CantidadSifen.normalizar(new BigDecimal(entrada)).toString();
            assertFalse(salida.contains("E"),
                    "La cantidad '" + entrada + "' se serializó como '" + salida + "'");
        }
    }

    @Test
    @DisplayName("Se respeta el techo de 4 decimales del XSD")
    void acotaACuatroDecimales() {
        // tdCantProSer: totalDigits=14, fractionDigits=4 (DE_Types_v150.xsd:1200-1204)
        BigDecimal resultado = CantidadSifen.normalizar(new BigDecimal("1.234567"));

        assertTrue(resultado.scale() <= CantidadSifen.MAX_DECIMALES);
        assertEquals("1.2346", resultado.toString(), "Debe redondear HALF_UP a 4 decimales");
    }

    @Test
    @DisplayName("Cuatro decimales exactos pasan intactos")
    void cuatroDecimalesIntactos() {
        assertEquals("0.0001", CantidadSifen.normalizar(new BigDecimal("0.0001")).toString());
    }

    @Test
    @DisplayName("Null pasa como null, no explota")
    void nullNoExplota() {
        assertNull(CantidadSifen.normalizar(null));
    }
}
