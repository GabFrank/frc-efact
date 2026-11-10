package com.frcefact.security.apikey;

import com.frcefact.service.integracion.ApiKeyService;
import com.frcefact.service.integracion.SolicitudApiService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ApiKeyAuthenticationFilterTest {

    private static final String CLAVE = "frc_abcdefghijkl_secreto";
    private ApiKeyService apiKeyService;
    private SolicitudApiService solicitudApiService;
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        apiKeyService = mock(ApiKeyService.class);
        solicitudApiService = mock(SolicitudApiService.class);
        chain = mock(FilterChain.class);
        ApiKeyPrincipal principal = new ApiKeyPrincipal(1L, "abcdefghijkl", 7L, "ERP", null, Set.of("de:consultar"));
        when(apiKeyService.autenticar(eq(CLAVE), any())).thenReturn(Optional.of(principal));
        when(apiKeyService.autenticar(argThat(c -> !CLAVE.equals(c)), any())).thenReturn(Optional.empty());
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    private ApiKeyAuthenticationFilter filtro(int limite) {
        return new ApiKeyAuthenticationFilter(apiKeyService, solicitudApiService, limite);
    }

    private static MockHttpServletRequest request(String ruta, String clave) {
        MockHttpServletRequest r = new MockHttpServletRequest("GET", "/api" + ruta);
        r.setContextPath("/api");
        if (clave != null) {
            r.addHeader(ApiKeyAuthenticationFilter.HEADER, clave);
        }
        return r;
    }

    @Test
    void ignoraLasRutasQueNoSonDeIntegracion() throws Exception {
        MockHttpServletRequest req = request("/facturas", CLAVE);
        MockHttpServletResponse res = new MockHttpServletResponse();
        filtro(60).doFilter(req, res, chain);

        verify(chain).doFilter(req, res);
        verifyNoInteractions(apiKeyService);
        assertNull(SecurityContextHolder.getContext().getAuthentication(), "una key no autentica fuera de /v1/integracion");
    }

    @Test
    void sinKeyResponde401() throws Exception {
        MockHttpServletResponse res = new MockHttpServletResponse();
        filtro(60).doFilter(request("/v1/integracion/documentos/1", null), res, chain);

        assertEquals(401, res.getStatus());
        assertTrue(res.getContentAsString().contains("API_KEY_FALTANTE"));
        verifyNoInteractions(chain);
    }

    @Test
    void keyInvalidaResponde401() throws Exception {
        MockHttpServletResponse res = new MockHttpServletResponse();
        filtro(60).doFilter(request("/v1/integracion/documentos/1", "frc_otra"), res, chain);

        assertEquals(401, res.getStatus());
        assertTrue(res.getContentAsString().contains("API_KEY_INVALIDA"));
        verifyNoInteractions(chain);
    }

    @Test
    void keyValidaAutenticaSoloConScopesYRegistraLaSolicitud() throws Exception {
        MockHttpServletRequest req = request("/v1/integracion/documentos/1", CLAVE);
        MockHttpServletResponse res = new MockHttpServletResponse();
        doAnswer(inv -> {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            assertInstanceOf(ApiKeyAuthenticationToken.class, auth);
            assertTrue(auth.getAuthorities().stream().allMatch(a -> a.getAuthority().startsWith("SCOPE_")));
            return null;
        }).when(chain).doFilter(any(), any());

        filtro(60).doFilter(req, res, chain);

        verify(chain).doFilter(req, res);
        verify(solicitudApiService).registrar(any(), anyString(), any(), anyInt(), any(), anyLong(), any());
    }

    @Test
    void superadoElLimiteResponde429() throws Exception {
        ApiKeyAuthenticationFilter f = filtro(2);
        for (int i = 0; i < 2; i++) {
            MockHttpServletResponse ok = new MockHttpServletResponse();
            f.doFilter(request("/v1/integracion/documentos/1", CLAVE), ok, chain);
            assertEquals(200, ok.getStatus());
        }
        MockHttpServletResponse res = new MockHttpServletResponse();
        f.doFilter(request("/v1/integracion/documentos/1", CLAVE), res, chain);
        assertEquals(429, res.getStatus());
        assertEquals("60", res.getHeader("Retry-After"));
    }
}
