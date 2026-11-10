package com.frcefact.service.integracion;

import com.frcefact.exception.ResourceNotFoundException;
import com.frcefact.model.ApiKey;
import com.frcefact.model.CuentaServicio;
import com.frcefact.repository.ApiKeyRepository;
import com.frcefact.repository.CuentaServicioRepository;
import com.frcefact.security.apikey.ApiKeyPrincipal;
import com.frcefact.security.apikey.ScopeIntegracion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Emisión, validación y revocación de API keys.
 *
 * <p>Formato: {@code frc_<prefijo>_<secreto>}
 * <ul>
 *   <li>prefijo: 12 caracteres [a-z0-9], público, identifica la key en la base y en los logs;</li>
 *   <li>secreto: 32 bytes de {@link SecureRandom} en Base64 URL-safe sin padding (43 caracteres).</li>
 * </ul>
 * En la base solo queda el SHA-256 del secreto. Un hash rápido alcanza porque el secreto tiene
 * 256 bits de entropía: no hay diccionario que probar, a diferencia de una contraseña humana.
 */
@Service
public class ApiKeyService {

    private static final Logger log = LoggerFactory.getLogger(ApiKeyService.class);

    static final String PREFIJO_FORMATO = "frc_";
    static final int LARGO_PREFIJO = 12;
    private static final int BYTES_SECRETO = 32;
    private static final char[] ALFABETO_PREFIJO = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

    /** No se escribe `ultimo_uso_en` más de una vez por minuto por key: evita un UPDATE por request. */
    private static final Duration INTERVALO_REGISTRO_USO = Duration.ofMinutes(1);

    private final SecureRandom random = new SecureRandom();
    private final ApiKeyRepository apiKeyRepository;
    private final CuentaServicioRepository cuentaServicioRepository;

    public ApiKeyService(ApiKeyRepository apiKeyRepository, CuentaServicioRepository cuentaServicioRepository) {
        this.apiKeyRepository = apiKeyRepository;
        this.cuentaServicioRepository = cuentaServicioRepository;
    }

    /** Key recién creada. {@code claveCompleta} solo existe en este objeto: no se guarda. */
    public record ApiKeyCreada(ApiKey apiKey, String claveCompleta) {
    }

    @Transactional
    public ApiKeyCreada crear(Long cuentaServicioId, String nombre, Set<String> scopes, LocalDateTime expiraEn) {
        CuentaServicio cuenta = cuentaServicioRepository.findById(cuentaServicioId)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta de servicio no encontrada: " + cuentaServicioId));
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la key es obligatorio");
        }
        Set<String> scopesValidados = validarScopes(scopes);
        if (expiraEn != null && !expiraEn.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("La fecha de vencimiento debe ser futura");
        }

        String prefijo = generarPrefijoUnico();
        String secreto = generarSecreto();

        ApiKey key = new ApiKey();
        key.setCuentaServicio(cuenta);
        key.setNombre(nombre.trim());
        key.setPrefijo(prefijo);
        key.setHashSecreto(sha256Hex(secreto));
        key.setScopesSet(scopesValidados);
        key.setExpiraEn(expiraEn);
        key = apiKeyRepository.save(key);

        log.info("🔑 API key creada: prefijo={}, cuenta={}, scopes={}", prefijo, cuentaServicioId, scopesValidados);
        return new ApiKeyCreada(key, PREFIJO_FORMATO + prefijo + "_" + secreto);
    }

    @Transactional
    public ApiKey revocar(Long apiKeyId, String usuario) {
        ApiKey key = apiKeyRepository.findById(apiKeyId)
                .orElseThrow(() -> new ResourceNotFoundException("API key no encontrada: " + apiKeyId));
        if (key.getRevocadaEn() == null) {
            key.setRevocadaEn(LocalDateTime.now());
            key.setRevocadaPor(usuario);
            key = apiKeyRepository.save(key);
            log.info("🔒 API key revocada: prefijo={}, por={}", key.getPrefijo(), usuario);
        }
        return key;
    }

    @Transactional(readOnly = true)
    public List<ApiKey> listarPorCuenta(Long cuentaServicioId) {
        return apiKeyRepository.findByCuentaServicioIdOrderByCreadoEnDesc(cuentaServicioId);
    }

    /**
     * Valida una key presentada por un cliente.
     *
     * @return el principal si la key existe, el secreto coincide, está vigente y la cuenta está
     *         activa; vacío en cualquier otro caso. No distingue los motivos hacia afuera, para no
     *         darle a quien prueba keys información sobre cuáles existen.
     */
    @Transactional
    public Optional<ApiKeyPrincipal> autenticar(String claveCompleta, String ip) {
        Optional<String[]> partes = separar(claveCompleta);
        if (partes.isEmpty()) {
            return Optional.empty();
        }
        String prefijo = partes.get()[0];
        String secreto = partes.get()[1];

        Optional<ApiKey> encontrada = apiKeyRepository.findByPrefijoConCuenta(prefijo);
        if (encontrada.isEmpty()) {
            return Optional.empty();
        }
        ApiKey key = encontrada.get();

        byte[] esperado = key.getHashSecreto().getBytes(StandardCharsets.US_ASCII);
        byte[] recibido = sha256Hex(secreto).getBytes(StandardCharsets.US_ASCII);
        if (!MessageDigest.isEqual(esperado, recibido)) {
            log.warn("🚫 API key con secreto incorrecto: prefijo={}, ip={}", prefijo, ip);
            return Optional.empty();
        }

        LocalDateTime ahora = LocalDateTime.now();
        CuentaServicio cuenta = key.getCuentaServicio();
        if (!key.estaVigente(ahora) || !Boolean.TRUE.equals(cuenta.getActiva())) {
            log.warn("🚫 API key no vigente o cuenta inactiva: prefijo={}, ip={}", prefijo, ip);
            return Optional.empty();
        }

        if (key.getUltimoUsoEn() == null || key.getUltimoUsoEn().plus(INTERVALO_REGISTRO_USO).isBefore(ahora)) {
            key.setUltimoUsoEn(ahora);
            key.setUltimoUsoIp(ip);
            apiKeyRepository.save(key);
        }

        return Optional.of(new ApiKeyPrincipal(
                key.getId(),
                key.getPrefijo(),
                cuenta.getId(),
                cuenta.getNombre(),
                cuenta.getEmpresa() != null ? cuenta.getEmpresa().getId() : null,
                key.getScopesSet()));
    }

    /** Separa {@code frc_<prefijo>_<secreto>}. El secreto puede contener '_' (Base64 URL-safe). */
    static Optional<String[]> separar(String clave) {
        if (clave == null) {
            return Optional.empty();
        }
        String c = clave.trim();
        int inicioSecreto = PREFIJO_FORMATO.length() + LARGO_PREFIJO + 1;
        if (!c.startsWith(PREFIJO_FORMATO) || c.length() <= inicioSecreto || c.charAt(inicioSecreto - 1) != '_') {
            return Optional.empty();
        }
        String prefijo = c.substring(PREFIJO_FORMATO.length(), PREFIJO_FORMATO.length() + LARGO_PREFIJO);
        if (!prefijo.matches("[a-z0-9]+")) {
            return Optional.empty();
        }
        return Optional.of(new String[]{prefijo, c.substring(inicioSecreto)});
    }

    private Set<String> validarScopes(Set<String> scopes) {
        if (scopes == null || scopes.isEmpty()) {
            throw new IllegalArgumentException("La key necesita al menos un scope");
        }
        Set<String> resultado = new LinkedHashSet<>();
        for (String s : scopes) {
            String valor = s == null ? "" : s.trim();
            if (ScopeIntegracion.desdeValor(valor).isEmpty()) {
                throw new IllegalArgumentException("Scope desconocido: '" + valor + "'");
            }
            resultado.add(valor);
        }
        return resultado;
    }

    private String generarPrefijoUnico() {
        for (int intento = 0; intento < 5; intento++) {
            StringBuilder sb = new StringBuilder(LARGO_PREFIJO);
            for (int i = 0; i < LARGO_PREFIJO; i++) {
                sb.append(ALFABETO_PREFIJO[random.nextInt(ALFABETO_PREFIJO.length)]);
            }
            String prefijo = sb.toString();
            if (!apiKeyRepository.existsByPrefijo(prefijo)) {
                return prefijo;
            }
        }
        throw new IllegalStateException("No se pudo generar un prefijo único para la API key");
    }

    private String generarSecreto() {
        byte[] bytes = new byte[BYTES_SECRETO];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    static String sha256Hex(String valor) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(valor.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
