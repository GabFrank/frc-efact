package com.frcefact.validation;

import com.frcefact.util.CalcularVerificadorRuc;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test para verificar el cálculo del dígito verificador del RUC paraguayo.
 */
class RucCalculatorTest {

    @Test
    void testCalculoDigitoVerificadorConNuevaUtilidad() {
        // Casos de prueba conocidos
        testRuc("80016875", 1); // Caso conocido
        testRuc("12345678", 9); // Otro caso de prueba
        testRuc("87654321", 4); // Otro caso de prueba
    }

    @Test
    void testGetDigitoVerificadorString() {
        String ruc = "80016875";
        String resultado = CalcularVerificadorRuc.getDigitoVerificadorString(ruc);
        assertNotNull(resultado);
        assertTrue(resultado.startsWith("-"));
        assertEquals("-1", resultado);
    }

    @Test
    void testRucMuyCorto() {
        String rucCorto = "123";
        Integer resultado = CalcularVerificadorRuc.getDigitoVerificador(rucCorto);
        assertNull(resultado, "RUC muy corto debe retornar null");
    }

    @Test
    void testEliminarNoDigitos() {
        // Test indirecto del método protegido a través del método público
        String rucConCaracteres = "8001-6875";
        Integer resultado = CalcularVerificadorRuc.getDigitoVerificador(rucConCaracteres);
        assertNotNull(resultado, "Debe manejar caracteres no numéricos");
    }

    private void testRuc(String numeroBase, int digitoEsperado) {
        Integer digitoCalculado = CalcularVerificadorRuc.getDigitoVerificador(numeroBase);
        
        System.out.println("Número base: " + numeroBase);
        System.out.println("Dígito esperado: " + digitoEsperado);
        System.out.println("Dígito calculado: " + digitoCalculado);
        
        assertNotNull(digitoCalculado, "El dígito calculado no debe ser null");
        assertEquals(digitoEsperado, digitoCalculado.intValue(), 
            "El dígito verificador calculado debe coincidir con el esperado para " + numeroBase);
    }
}