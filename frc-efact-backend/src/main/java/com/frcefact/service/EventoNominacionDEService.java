package com.frcefact.service;

import com.frcefact.model.EstadoEvento;
import com.frcefact.model.EventoNominacionDE;
import com.frcefact.repository.EventoNominacionDERepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestión de Eventos de Nominación de Documentos Electrónicos.
 */
@Service
@Transactional
public class EventoNominacionDEService {

    private final EventoNominacionDERepository repository;

    public EventoNominacionDEService(EventoNominacionDERepository repository) {
        this.repository = repository;
    }

    public EventoNominacionDE save(EventoNominacionDE evento) {
        return repository.save(evento);
    }

    public Optional<EventoNominacionDE> findById(Long id) {
        return repository.findById(id);
    }

    public List<EventoNominacionDE> findByDocumentoElectronicoId(Long documentoElectronicoId) {
        return repository.findByDocumentoElectronicoId(documentoElectronicoId);
    }

    public boolean tieneNominacionAprobada(Long documentoElectronicoId) {
        return repository.existsEventoAprobadoForDocumento(documentoElectronicoId);
    }

    public List<EventoNominacionDE> findActivosByCdcDocumento(String cdcDocumento) {
        return repository.findByCdcDocumentoAndActivoTrue(cdcDocumento);
    }

    public List<EventoNominacionDE> findEventosPendientes() {
        return repository.findEventosPendientes();
    }

    public Optional<EventoNominacionDE> findByEventoId(String eventoId) {
        return repository.findByEventoId(eventoId);
    }
}










