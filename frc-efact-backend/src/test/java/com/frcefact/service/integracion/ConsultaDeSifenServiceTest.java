package com.frcefact.service.integracion;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

class ConsultaDeSifenServiceTest {

    @Test
    void aceptaUnCdcReal() {
        assertDoesNotThrow(() -> ConsultaDeSifenService.validarCdc("01800994825001002000003822025120417284341967"));
    }

    @Test
    void rechazaCdcConDigitoVerificadorIncorrecto() {
        IntegracionException e = assertThrows(IntegracionException.class,
                () -> ConsultaDeSifenService.validarCdc("01800994825001002000003822025120417284341968"));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals("CDC_INVALIDO", e.getCodigo());
    }

    @Test
    void rechazaCdcConLargoOCaracteresInvalidos() {
        for (String cdc : new String[]{null, "", "123", "0180099482500100200000382202512041728434196X",
                "018009948250010020000038220251204172843419670"}) {
            assertThrows(IntegracionException.class, () -> ConsultaDeSifenService.validarCdc(cdc), String.valueOf(cdc));
        }
    }
}
