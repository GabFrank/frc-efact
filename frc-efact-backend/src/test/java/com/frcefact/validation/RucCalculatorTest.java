package com.frcefact.validation;

import com.frcefact.util.CalcularVerificadorRuc;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test del cálculo del dígito verificador del RUC paraguayo (módulo 11).
 *
 * <p><b>Historia de este archivo:</b> la versión original afirmaba que el DV de
 * {@code 80016875} era {@code 1} y el de {@code 87654321} era {@code 4}. Ambos valores estaban
 * inventados — el algoritmo calcula {@code 5} y {@code 2}. Esos tests en rojo se interpretaron
 * como "el algoritmo del backend está mal", y con esa premisa se deshabilitó la validación de RUC
 * del frontend, que quedó sirviendo mocks. El algoritmo nunca estuvo roto.
 *
 * <p>Para que no vuelva a pasar, los casos principales NO son números inventados: son RUCs reales
 * de producción, tres de ellos emisores de Documentos Electrónicos que SIFEN aprobó. Si un caso de
 * este archivo falla, sospechar del cambio antes que del caso.
 */
class RucCalculatorTest {

    @Test
    @DisplayName("RUCs reales de producción: el DV calculado coincide con el emitido")
    void testRucsRealesDeProduccion() {
        // Emisores con DE aprobados por SIFEN — la evidencia más fuerte de que el algoritmo sirve
        assertDigitoVerificador("3664097", 2);   // ANATOLE DEINZER DUARTE
        assertDigitoVerificador("80099482", 5);  // FRANCO AREVALOS S.A.
        assertDigitoVerificador("4173770", 9);   // LANGER MARIO
        // Receptor de un DE aprobado
        assertDigitoVerificador("6328132", 5);   // MARCIO TADEU BARBOSA
    }

    @Test
    @DisplayName("Los ejemplos documentados en RucValidator son consistentes con el algoritmo")
    void testEjemplosDocumentadosEnRucValidator() {
        // RucValidator los cita como RUCs de formato y DV válidos
        assertDigitoVerificador("123456", 0);
        assertDigitoVerificador("4043581", 4);
        assertDigitoVerificador("80016875", 5);
    }

    @Test
    @DisplayName("Secuencias sintéticas verificadas contra el módulo 11")
    void testSecuenciasSinteticas() {
        assertDigitoVerificador("12345678", 9);
        assertDigitoVerificador("87654321", 2);
    }

    @Test
    @DisplayName("getDigitoVerificadorString devuelve el DV con guion")
    void testGetDigitoVerificadorString() {
        String resultado = CalcularVerificadorRuc.getDigitoVerificadorString("80016875");
        assertNotNull(resultado);
        assertTrue(resultado.startsWith("-"));
        assertEquals("-5", resultado);
    }

    @Test
    @DisplayName("La variante con base explícita devuelve null si el RUC es muy corto")
    void testRucMuyCorto() {
        // Solo la variante de dos argumentos puede expresar "entrada inválida": devuelve null.
        Integer resultado = CalcularVerificadorRuc.getDigitoVerificador("123", 11);
        assertNull(resultado, "Un RUC de menos de 4 caracteres debe devolver null");
    }

    @Test
    @DisplayName("Deuda conocida: la variante de un argumento colapsa la entrada inválida en 0")
    void testVarianteDeUnArgumentoColapsaInvalidoEnCero() {
        // `int getDigitoVerificador(String)` no puede devolver null, así que convierte
        // "entrada inválida" en 0 — que también es un DV legítimo. El llamador no puede
        // distinguir los dos casos. No se cambió la firma porque SifenEventoService hace
        // `(short) getDigitoVerificador(...)` y pasar a Integer introduce riesgo de NPE.
        // Registrado en docs/TAREAS_PENDIENTES.md §4. Este test fija el comportamiento actual
        // para que un cambio de firma sea deliberado y no accidental.
        assertEquals(0, CalcularVerificadorRuc.getDigitoVerificador("123"),
                "Comportamiento actual documentado, no deseado");
    }

    @Test
    @DisplayName("Los caracteres no numéricos se procesan sin explotar")
    void testEliminarNoDigitos() {
        Integer resultado = CalcularVerificadorRuc.getDigitoVerificador("8001-6875", 11);
        assertNotNull(resultado, "Debe manejar caracteres no numéricos");
    }

    private void assertDigitoVerificador(String numeroBase, int digitoEsperado) {
        Integer digitoCalculado = CalcularVerificadorRuc.getDigitoVerificador(numeroBase, 11);
        assertNotNull(digitoCalculado, "El dígito calculado no debe ser null para " + numeroBase);
        assertEquals(digitoEsperado, digitoCalculado.intValue(), "DV esperado para " + numeroBase);
    }
}
