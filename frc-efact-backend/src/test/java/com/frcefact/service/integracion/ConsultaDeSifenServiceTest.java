package com.frcefact.service.integracion;

import com.frcefact.security.apikey.ApiKeyPrincipal;
import com.frcefact.sifen.config.SifenConfigFactory;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    private static final String CDC = "01800994825001002000003822025120417284341967";

    private static ApiKeyPrincipal principal(Long empresaId) {
        return new ApiKeyPrincipal(1L, "abc123def456", 1L, "Loom", empresaId, Set.of("de:consultar"));
    }

    @Test
    void certificadoDePlataformaVencidoDa503ConMensajeClaroSinLlamarASifen() {
        SifenConfigFactory factory = mock(SifenConfigFactory.class);
        when(factory.buildParaConsulta(2L))
                .thenThrow(new SifenConfigFactory.CertificadoVencidoException(2L, LocalDate.of(2026, 8, 20)));
        ConsultaDeSifenService service = new ConsultaDeSifenService(factory, 2L);

        IntegracionException e = assertThrows(IntegracionException.class, () -> service.consultar(CDC, principal(null)));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, e.getStatus());
        assertEquals("SERVICIO_NO_CONFIGURADO", e.getCodigo());
        assertTrue(e.getMessage().contains("vencido"), e.getMessage());
        verify(factory, never()).executeWithConfig(any(), any());
    }

    @Test
    void cuentaConCertificadoPropioVencidoUsaElDePlataforma() {
        SifenConfigFactory factory = mock(SifenConfigFactory.class);
        when(factory.tieneCertificadoVigente(5L)).thenReturn(false);
        when(factory.buildParaConsulta(2L)).thenThrow(new RuntimeException("corte para el test"));
        ConsultaDeSifenService service = new ConsultaDeSifenService(factory, 2L);

        assertThrows(IntegracionException.class, () -> service.consultar(CDC, principal(5L)));

        verify(factory).buildParaConsulta(2L);
        verify(factory, never()).buildParaConsulta(5L);
    }

    @Test
    void inicioDeLaRespuestaQuedaEnUnaLineaYRecortado() {
        assertEquals("<html> <body>Error</body> </html>", ConsultaDeSifenService.inicio("<html>\n  <body>Error</body>\n</html>"));
        assertEquals(301, ConsultaDeSifenService.inicio("x".repeat(1000)).length());
    }
}
