package com.frcefact.security.apikey;

import com.frcefact.service.integracion.ApiKeyService;
import com.frcefact.service.integracion.SolicitudApiService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Autenticación por API key para la API de integración ({@code /v1/integracion/**}).
 *
 * <p>Solo actúa sobre esas rutas: en cualquier otra ignora el header, así una key nunca sirve
 * para entrar a la API que usa el frontend. Y en esas rutas no acepta otra cosa: sin key válida
 * corta con 401 antes de llegar al controller.
 *
 * <p>Además aplica un límite de solicitudes por key (ventana fija de un minuto, en memoria) y
 * registra cada llamada autenticada en {@code integracion.solicitud_api}.
 */
@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ApiKeyAuthenticationFilter.class);

    public static final String HEADER = "X-API-Key";
    public static final String RUTA_INTEGRACION = "/v1/integracion/";

    /** Atributos que el controller completa para que quede registrado en la solicitud. */
    public static final String ATTR_OPERACION = "integracion.operacion";
    public static final String ATTR_RECURSO = "integracion.recurso";
    public static final String ATTR_CODIGO_SIFEN = "integracion.codigoSifen";

    private final ApiKeyService apiKeyService;
    private final SolicitudApiService solicitudApiService;
    private final int limitePorMinuto;
    private final Map<Long, Ventana> ventanas = new ConcurrentHashMap<>();

    public ApiKeyAuthenticationFilter(ApiKeyService apiKeyService,
                                      SolicitudApiService solicitudApiService,
                                      @Value("${integracion.rate-limit.por-minuto:60}") int limitePorMinuto) {
        this.apiKeyService = apiKeyService;
        this.solicitudApiService = solicitudApiService;
        this.limitePorMinuto = limitePorMinuto;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !esRutaIntegracion(request);
    }

    static boolean esRutaIntegracion(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contexto = request.getContextPath();
        String ruta = (contexto != null && uri.startsWith(contexto)) ? uri.substring(contexto.length()) : uri;
        return ruta.startsWith(RUTA_INTEGRACION);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = obtenerIp(request);
        String clave = request.getHeader(HEADER);
        if (!StringUtils.hasText(clave)) {
            responderError(response, HttpStatus.UNAUTHORIZED, "API_KEY_FALTANTE",
                    "Falta el header " + HEADER);
            return;
        }

        Optional<ApiKeyPrincipal> principal;
        try {
            principal = apiKeyService.autenticar(clave, ip);
        } catch (Exception e) {
            log.error("Error validando API key", e);
            responderError(response, HttpStatus.SERVICE_UNAVAILABLE, "ERROR_INTERNO",
                    "No se pudo validar la API key. Intente nuevamente.");
            return;
        }
        if (principal.isEmpty()) {
            responderError(response, HttpStatus.UNAUTHORIZED, "API_KEY_INVALIDA",
                    "API key inválida, vencida o revocada");
            return;
        }
        ApiKeyPrincipal p = principal.get();

        if (!consumirCupo(p.apiKeyId())) {
            response.setHeader("Retry-After", "60");
            responderError(response, HttpStatus.TOO_MANY_REQUESTS, "LIMITE_EXCEDIDO",
                    "Límite de " + limitePorMinuto + " solicitudes por minuto excedido");
            solicitudApiService.registrar(p, request.getMethod() + " " + request.getRequestURI(), null,
                    HttpStatus.TOO_MANY_REQUESTS.value(), null, 0L, ip);
            return;
        }

        SecurityContextHolder.getContext().setAuthentication(new ApiKeyAuthenticationToken(p));
        long inicio = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            Object operacion = request.getAttribute(ATTR_OPERACION);
            solicitudApiService.registrar(
                    p,
                    operacion != null ? operacion.toString() : request.getMethod() + " " + request.getRequestURI(),
                    (String) request.getAttribute(ATTR_RECURSO),
                    response.getStatus(),
                    (String) request.getAttribute(ATTR_CODIGO_SIFEN),
                    System.currentTimeMillis() - inicio,
                    ip);
        }
    }

    private boolean consumirCupo(Long apiKeyId) {
        long minutoActual = System.currentTimeMillis() / 60_000L;
        Ventana v = ventanas.compute(apiKeyId, (id, actual) ->
                (actual == null || actual.minuto != minutoActual) ? new Ventana(minutoActual) : actual);
        synchronized (v) {
            v.contador++;
            return v.contador <= limitePorMinuto;
        }
    }

    private static final class Ventana {
        private final long minuto;
        private int contador;

        private Ventana(long minuto) {
            this.minuto = minuto;
        }
    }

    private static String obtenerIp(HttpServletRequest request) {
        // nginx del host pone X-Real-IP; X-Forwarded-For puede traer una cadena, se toma el primero.
        String realIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(realIp)) {
            return recortar(realIp.trim());
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            return recortar(forwarded.split(",")[0].trim());
        }
        return recortar(request.getRemoteAddr());
    }

    private static String recortar(String ip) {
        return ip != null && ip.length() > 64 ? ip.substring(0, 64) : ip;
    }

    private static void responderError(HttpServletResponse response, HttpStatus status,
                                       String codigo, String mensaje) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"error\":{\"codigo\":\"" + codigo + "\",\"mensaje\":\""
                + mensaje.replace("\"", "'") + "\"}}");
    }
}
