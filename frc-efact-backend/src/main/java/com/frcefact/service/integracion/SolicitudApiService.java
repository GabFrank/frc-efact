package com.frcefact.service.integracion;

import com.frcefact.model.SolicitudApi;
import com.frcefact.repository.SolicitudApiRepository;
import com.frcefact.security.apikey.ApiKeyPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registra cada llamada a la API de integración.
 *
 * <p>Un fallo al registrar nunca rompe la respuesta al cliente: se loguea y se sigue.
 */
@Service
public class SolicitudApiService {

    private static final Logger log = LoggerFactory.getLogger(SolicitudApiService.class);

    private final SolicitudApiRepository repository;

    public SolicitudApiService(SolicitudApiRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(ApiKeyPrincipal principal, String operacion, String recurso,
                          int codigoHttp, String codigoSifen, Long duracionMs, String ip) {
        try {
            SolicitudApi s = new SolicitudApi();
            s.setApiKeyId(principal.apiKeyId());
            s.setOperacion(recortar(operacion, 50));
            s.setRecurso(recortar(recurso, 100));
            s.setCodigoHttp(codigoHttp);
            s.setCodigoSifen(recortar(codigoSifen, 10));
            s.setDuracionMs(duracionMs);
            s.setIp(recortar(ip, 64));
            repository.save(s);
        } catch (Exception e) {
            log.error("No se pudo registrar la solicitud de la key {}: {}", principal.prefijo(), e.getMessage());
        }
    }

    private static String recortar(String valor, int max) {
        return valor != null && valor.length() > max ? valor.substring(0, max) : valor;
    }
}
