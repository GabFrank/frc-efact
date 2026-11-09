package com.frcefact.service.integracion;

import org.springframework.http.HttpStatus;

/**
 * Error de la API de integración, con el status HTTP y un código estable que el sistema cliente
 * puede usar para decidir qué hacer (los mensajes pueden cambiar; los códigos no).
 */
public class IntegracionException extends RuntimeException {

    private final HttpStatus status;
    private final String codigo;
    private final String codigoSifen;

    public IntegracionException(HttpStatus status, String codigo, String mensaje) {
        this(status, codigo, mensaje, null);
    }

    public IntegracionException(HttpStatus status, String codigo, String mensaje, String codigoSifen) {
        super(mensaje);
        this.status = status;
        this.codigo = codigo;
        this.codigoSifen = codigoSifen;
    }

    public HttpStatus getStatus() { return status; }
    public String getCodigo() { return codigo; }
    public String getCodigoSifen() { return codigoSifen; }
}
