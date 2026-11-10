package com.frcefact.sifen.util;

import com.roshka.sifen.core.types.TcUniMed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de la resolución de {@code cUniMed} contra el catálogo real de SIFEN.
 */
class UnidadMedidaSifenTest {

    @Test
    @DisplayName("TN se resuelve a Tonelada (99), que es lo que el caso real necesitaba")
    void toneladaSeResuelve() {
        TcUniMed resultado = UnidadMedidaSifen.resolver("TN", false);

        assertEquals(TcUniMed.TN, resultado);
        assertEquals((short) 99, resultado.getVal());
    }

    @Test
    @DisplayName("El case importa: ML es Mililitros (88) y ml es Metro lineal (660)")
    void mlYMLSonUnidadesDistintas() {
        // Esta es la razón por la que no puede haber matcheo case-insensitive en ningún lado.
        // El .toUpperCase() del código viejo convertía metros lineales en mililitros en silencio.
        assertEquals(TcUniMed.ML, UnidadMedidaSifen.resolver("ML", false));
        assertEquals((short) 88, UnidadMedidaSifen.resolver("ML", false).getVal());

        assertEquals(TcUniMed.ml, UnidadMedidaSifen.resolver("ml", false));
        assertEquals((short) 660, UnidadMedidaSifen.resolver("ml", false).getVal());

        assertNotEquals(UnidadMedidaSifen.resolver("ML", false),
                UnidadMedidaSifen.resolver("ml", false));
    }

    @Test
    @DisplayName("Los códigos en minúscula del catálogo se resuelven")
    void codigosEnMinuscula() {
        // Con el .toUpperCase() del código viejo, ninguno de estos era alcanzable.
        assertEquals(TcUniMed.kg, UnidadMedidaSifen.resolver("kg", false));
        assertEquals(TcUniMed.g, UnidadMedidaSifen.resolver("g", false));
        assertEquals(TcUniMed.m, UnidadMedidaSifen.resolver("m", false));
        assertEquals(TcUniMed.ha, UnidadMedidaSifen.resolver("ha", false));
        assertEquals(TcUniMed.Hs, UnidadMedidaSifen.resolver("Hs", false));
        assertEquals(TcUniMed.Km, UnidadMedidaSifen.resolver("Km", false));
    }

    @Test
    @DisplayName("Se acepta tanto el nombre de la constante como el código SIFEN")
    void nombreDeConstanteYCodigo() {
        // Para estas dos, el nombre de la constante y la abreviatura NO coinciden.
        assertEquals(TcUniMed.kg_m2, UnidadMedidaSifen.resolver("kg_m2", false));
        assertEquals(TcUniMed.kg_m2, UnidadMedidaSifen.resolver("kg/m2", false));

        assertEquals(TcUniMed.racion, UnidadMedidaSifen.resolver("racion", false));
        assertEquals(TcUniMed.racion, UnidadMedidaSifen.resolver("ración", false));
    }

    @Test
    @DisplayName("Los alias del formulario viejo se mapean a su código real")
    void aliasLegacy() {
        assertEquals(TcUniMed.kg, UnidadMedidaSifen.resolver("KG", false));
        assertEquals(TcUniMed.g, UnidadMedidaSifen.resolver("G", false));
        assertEquals(TcUniMed.LT, UnidadMedidaSifen.resolver("L", false));
        assertEquals(TcUniMed.m, UnidadMedidaSifen.resolver("M", false));
        assertEquals(TcUniMed.Hs, UnidadMedidaSifen.resolver("H", false));
    }

    @Test
    @DisplayName("Los valores sin equivalente en SIFEN van a UNI")
    void sinEquivalenteVanAUni() {
        // Es lo que ya se emitía para ellos al caer en el catch; acá queda explícito.
        for (String sinEquivalente : new String[]{"SERV", "PAR", "CAJ", "BOL", "TUB"}) {
            assertEquals(TcUniMed.UNI, UnidadMedidaSifen.resolver(sinEquivalente, false),
                    "'" + sinEquivalente + "' no existe en SIFEN y debe emitirse como UNI");
        }
    }

    @Test
    @DisplayName("Un valor desconocido cae a UNI en vez de romper la emisión")
    void desconocidoCaeAUni() {
        assertEquals(TcUniMed.UNI, UnidadMedidaSifen.resolver("XYZ_INVENTADO", false));
    }

    @Test
    @DisplayName("Sin unidad cargada, `balanza` sigue decidiendo kg — no se le cambia la unidad a nadie")
    void balanzaComoFallbackLegacy() {
        assertEquals(TcUniMed.kg, UnidadMedidaSifen.resolver(null, true));
        assertEquals(TcUniMed.kg, UnidadMedidaSifen.resolver("", true));
        assertEquals(TcUniMed.kg, UnidadMedidaSifen.resolver("   ", true));
    }

    @Test
    @DisplayName("La unidad explícita gana sobre `balanza`")
    void unidadExplicitaGanaSobreBalanza() {
        // Un producto de balanza vendido por tonelada tiene que emitir TN, no kg.
        assertEquals(TcUniMed.TN, UnidadMedidaSifen.resolver("TN", true));
    }

    @Test
    @DisplayName("Sin unidad y sin balanza, UNI")
    void defaultUni() {
        assertEquals(TcUniMed.UNI, UnidadMedidaSifen.resolver(null, null));
        assertEquals(TcUniMed.UNI, UnidadMedidaSifen.resolver(null, false));
    }

    @Test
    @DisplayName("Se tolera el espacio en blanco alrededor")
    void toleraEspacios() {
        assertEquals(TcUniMed.TN, UnidadMedidaSifen.resolver("  TN  ", false));
    }
}
