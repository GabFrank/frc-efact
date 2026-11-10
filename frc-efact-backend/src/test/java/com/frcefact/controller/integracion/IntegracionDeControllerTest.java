package com.frcefact.controller.integracion;

import com.frcefact.service.integracion.IntegracionException;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class IntegracionDeControllerTest {

    @Test
    void parseaLasPartesAIncluir() {
        assertEquals(Set.of(), IntegracionDeController.parsearIncluir(null));
        assertEquals(Set.of(), IntegracionDeController.parsearIncluir(" "));
        assertEquals(Set.of("items", "xml"), IntegracionDeController.parsearIncluir("items, XML"));
    }

    @Test
    void rechazaUnaParteDesconocida() {
        IntegracionException e = assertThrows(IntegracionException.class,
                () -> IntegracionDeController.parsearIncluir("items,kude"));
        assertEquals("PARAMETRO_INVALIDO", e.getCodigo());
    }
}
