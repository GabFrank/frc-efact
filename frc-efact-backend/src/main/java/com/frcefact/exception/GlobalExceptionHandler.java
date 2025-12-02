package com.frcefact.exception;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para la aplicación.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Manejar errores de validación.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            Object rejectedValue = ((FieldError) error).getRejectedValue();
            errors.put(fieldName, errorMessage);
            
            // Log detallado de cada error de validación
            logger.error("Error de validación en campo '{}': {} | Valor rechazado: {}", 
                    fieldName, errorMessage, rejectedValue);
        });

        // Log del objeto completo que causó el error
        if (ex.getBindingResult().getTarget() != null) {
            logger.error("Objeto que falló la validación: {}", ex.getBindingResult().getTarget());
        }

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Error de validación",
                errors,
                LocalDateTime.now()
        );

        logger.error("Errores de validación totales: {}", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Manejar errores de credenciales inválidas.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                ex.getMessage(),
                null,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Manejar errores de cuenta desactivada.
     */
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabledException(DisabledException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                ex.getMessage(),
                null,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * Manejar errores de cuenta bloqueada.
     */
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ErrorResponse> handleLockedException(LockedException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.LOCKED.value(),
                ex.getMessage(),
                null,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.LOCKED).body(errorResponse);
    }

    /**
     * Manejar otros errores de autenticación.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Error de autenticación: " + ex.getMessage(),
                null,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Manejar errores de argumento ilegal.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.warn("Argumento ilegal: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                null,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Manejar errores de recurso no encontrado.
     */
    @ExceptionHandler({ResourceNotFoundException.class, EntityNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(RuntimeException ex) {
        logger.warn("Recurso no encontrado: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                null,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Manejar errores de recurso duplicado.
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResourceException(DuplicateResourceException ex) {
        logger.warn("Recurso duplicado: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                null,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Manejar errores de acceso denegado.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        logger.warn("Acceso denegado: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "No tiene permisos para realizar esta operación",
                null,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * Manejar errores genéricos.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        logger.error("Error inesperado", ex);
        
        // En producción, no exponer detalles internos
        String message = "Error interno del servidor";
        
        // En desarrollo, incluir más detalles
        if (logger.isDebugEnabled()) {
            message = "Error interno: " + ex.getMessage();
            if (ex.getCause() != null) {
                message += " | Causa: " + ex.getCause().getMessage();
            }
        }
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                message,
                null,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Clase interna para representar respuestas de error.
     */
    public static class ErrorResponse {
        private int status;
        private String message;
        private Map<String, String> errors;
        private LocalDateTime timestamp;

        public ErrorResponse(int status, String message, Map<String, String> errors, LocalDateTime timestamp) {
            this.status = status;
            this.message = message;
            this.errors = errors;
            this.timestamp = timestamp;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Map<String, String> getErrors() {
            return errors;
        }

        public void setErrors(Map<String, String> errors) {
            this.errors = errors;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }
    }
}
