package com.frcefact.service;

import com.frcefact.model.EventoCancelacionDE;
import com.frcefact.repository.EventoCancelacionDERepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestión de Eventos de Cancelación de Documentos Electrónicos.
 */
@Service
@Transactional
public class EventoCancelacionDEService {

    private final EventoCancelacionDERepository repository;

    public EventoCancelacionDEService(EventoCancelacionDERepository repository) {
        this.repository = repository;
    }

    public EventoCancelacionDE save(EventoCancelacionDE evento) {
        return repository.save(evento);
    }

    public Optional<EventoCancelacionDE> findById(Long id) {
        return repository.findById(id);
    }

    public List<EventoCancelacionDE> findByDocumentoElectronicoId(Long documentoElectronicoId) {
        return repository.findByDocumentoElectronicoId(documentoElectronicoId);
    }

    public boolean tieneCancelacionAprobada(Long documentoElectronicoId) {
        return repository.existsEventoAprobadoForDocumento(documentoElectronicoId);
    }

    public List<EventoCancelacionDE> findActivosByCdcDocumento(String cdcDocumento) {
        return repository.findByCdcDocumentoAndActivoTrue(cdcDocumento);
    }

    public List<EventoCancelacionDE> findEventosPendientes() {
        return repository.findEventosPendientes();
    }

    public Optional<EventoCancelacionDE> findByEventoId(String eventoId) {
        return repository.findByEventoId(eventoId);
    }
}


