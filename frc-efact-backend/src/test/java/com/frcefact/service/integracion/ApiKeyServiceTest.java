package com.frcefact.service.integracion;

import com.frcefact.model.ApiKey;
import com.frcefact.model.CuentaServicio;
import com.frcefact.repository.ApiKeyRepository;
import com.frcefact.repository.CuentaServicioRepository;
import com.frcefact.security.apikey.ApiKeyPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ApiKeyServiceTest {

    private ApiKeyRepository apiKeyRepository;
    private CuentaServicioRepository cuentaRepository;
    private ApiKeyService service;
    private CuentaServicio cuenta;

    @BeforeEach
    void setUp() {
        apiKeyRepository = mock(ApiKeyRepository.class);
        cuentaRepository = mock(CuentaServicioRepository.class);
        service = new ApiKeyService(apiKeyRepository, cuentaRepository);
        cuenta = new CuentaServicio();
        cuenta.setId(7L);
        cuenta.setNombre("ERP Cliente");
        cuenta.setActiva(true);
        when(cuentaRepository.findById(7L)).thenReturn(Optional.of(cuenta));
        when(apiKeyRepository.existsByPrefijo(anyString())).thenReturn(false);
        when(apiKeyRepository.save(any(ApiKey.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private ApiKeyService.ApiKeyCreada crearKey() {
        return service.crear(7L, "producción", Set.of("de:consultar"), null);
    }

    @Test
    void laKeyCreadaTieneElFormatoYSoloSeGuardaElHash() {
        ApiKeyService.ApiKeyCreada creada = crearKey();
        String clave = creada.claveCompleta();

        assertTrue(clave.matches("frc_[a-z0-9]{12}_[A-Za-z0-9_-]{43}"), clave);
        ApiKey key = creada.apiKey();
        String secreto = clave.substring(4 + 12 + 1);
        assertEquals(ApiKeyService.sha256Hex(secreto), key.getHashSecreto());
        assertFalse(key.getHashSecreto().contains(secreto));
        assertEquals(Set.of("de:consultar"), key.getScopesSet());
    }

    @Test
    void autenticaConLaKeyCorrecta() {
        ApiKeyService.ApiKeyCreada creada = crearKey();
        creada.apiKey().setId(1L);
        creada.apiKey().setCuentaServicio(cuenta);
        when(apiKeyRepository.findByPrefijoConCuenta(creada.apiKey().getPrefijo()))
                .thenReturn(Optional.of(creada.apiKey()));

        Optional<ApiKeyPrincipal> p = service.autenticar(creada.claveCompleta(), "1.2.3.4");

        assertTrue(p.isPresent());
        assertEquals(7L, p.get().cuentaServicioId());
        assertNull(p.get().empresaId());
        assertEquals(Set.of("de:consultar"), p.get().scopes());
    }

    @Test
    void rechazaSecretoIncorrectoRevocadaVencidaOCuentaInactiva() {
        ApiKeyService.ApiKeyCreada creada = crearKey();
        ApiKey key = creada.apiKey();
        key.setCuentaServicio(cuenta);
        when(apiKeyRepository.findByPrefijoConCuenta(key.getPrefijo())).thenReturn(Optional.of(key));
        String clave = creada.claveCompleta();

        String otraClave = clave.substring(0, clave.length() - 1) + (clave.endsWith("A") ? "B" : "A");
        assertTrue(service.autenticar(otraClave, null).isEmpty(), "secreto incorrecto");

        key.setExpiraEn(LocalDateTime.now().minusMinutes(1));
        assertTrue(service.autenticar(clave, null).isEmpty(), "vencida");
        key.setExpiraEn(null);

        cuenta.setActiva(false);
        assertTrue(service.autenticar(clave, null).isEmpty(), "cuenta inactiva");
        cuenta.setActiva(true);

        key.setRevocadaEn(LocalDateTime.now());
        assertTrue(service.autenticar(clave, null).isEmpty(), "revocada");
    }

    @Test
    void rechazaFormatosInvalidosSinConsultarLaBase() {
        for (String c : new String[]{null, "", "abc", "frc_", "frc_corto_x", "frc_ABCDEFGHIJKL_secreto", "xyz_abcdefghijkl_s"}) {
            assertTrue(service.autenticar(c, null).isEmpty(), String.valueOf(c));
        }
        verify(apiKeyRepository, never()).findByPrefijoConCuenta(anyString());
    }

    @Test
    void elSecretoPuedeContenerGuionBajo() {
        Optional<String[]> partes = ApiKeyService.separar("frc_abcdefghijkl_ab_cd");
        assertTrue(partes.isPresent());
        assertEquals("abcdefghijkl", partes.get()[0]);
        assertEquals("ab_cd", partes.get()[1]);
    }

    @Test
    void validaScopesYVencimiento() {
        assertThrows(IllegalArgumentException.class, () -> service.crear(7L, "x", Set.of("de:emitir"), null));
        assertThrows(IllegalArgumentException.class, () -> service.crear(7L, "x", Set.of(), null));
        assertThrows(IllegalArgumentException.class,
                () -> service.crear(7L, "x", Set.of("de:consultar"), LocalDateTime.now().minusDays(1)));
    }

    @Test
    void revocarRegistraQuienYCuando() {
        ApiKey key = new ApiKey();
        key.setId(3L);
        when(apiKeyRepository.findById(3L)).thenReturn(Optional.of(key));

        service.revocar(3L, "admin");

        ArgumentCaptor<ApiKey> captor = ArgumentCaptor.forClass(ApiKey.class);
        verify(apiKeyRepository).save(captor.capture());
        assertNotNull(captor.getValue().getRevocadaEn());
        assertEquals("admin", captor.getValue().getRevocadaPor());
    }
}
