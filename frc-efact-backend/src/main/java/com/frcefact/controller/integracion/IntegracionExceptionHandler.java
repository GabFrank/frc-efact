package com.frcefact.controller.integracion;

import com.frcefact.security.apikey.ApiKeyAuthenticationFilter;
import com.frcefact.service.integracion.IntegracionException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Errores de la API de integración, con un formato propio y estable para los sistemas cliente:
 * <pre>{"error": {"codigo": "DE_NO_ENCONTRADO", "mensaje": "...", "codigoSifen": "0420"}}</pre>
 * Separado del {@code GlobalExceptionHandler} para que cambios en la API interna no rompan el contrato.
 */
@RestControllerAdvice(assignableTypes = IntegracionDeController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class IntegracionExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(IntegracionExceptionHandler.class);

    @ExceptionHandler(IntegracionException.class)
    public ResponseEntity<Map<String, Object>> integracion(IntegracionException e, HttpServletRequest request) {
        if (e.getCodigoSifen() != null) {
            request.setAttribute(ApiKeyAuthenticationFilter.ATTR_CODIGO_SIFEN, e.getCodigoSifen());
        }
        return error(e.getStatus(), e.getCodigo(), e.getMessage(), e.getCodigoSifen());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> accesoDenegado(AccessDeniedException e) {
        return error(HttpStatus.FORBIDDEN, "SCOPE_INSUFICIENTE", "La API key no tiene permiso para esta operación", null);
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<Map<String, Object>> noImplementado(UnsupportedOperationException e) {
        return error(HttpStatus.NOT_IMPLEMENTED, "NO_DISPONIBLE", e.getMessage(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> inesperado(Exception e) {
        log.error("Error inesperado en la API de integración", e);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO", "Error interno. Intente nuevamente.", null);
    }

    private static ResponseEntity<Map<String, Object>> error(HttpStatus status, String codigo, String mensaje,
                                                             String codigoSifen) {
        Map<String, Object> detalle = new LinkedHashMap<>();
        detalle.put("codigo", codigo);
        detalle.put("mensaje", mensaje);
        if (codigoSifen != null) {
            detalle.put("codigoSifen", codigoSifen);
        }
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(Map.of("error", detalle));
    }
}
