package com.frcefact.service;

import com.frcefact.model.EstadoEvento;
import com.frcefact.model.EventoInutilizacionDE;
import com.frcefact.repository.EventoInutilizacionDERepository;
import com.frcefact.repository.specification.EventoInutilizacionDESpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestión de Eventos de Inutilización de Numeración de Documentos Electrónicos.
 */
@Service
@Transactional
public class EventoInutilizacionDEService {

    private final EventoInutilizacionDERepository repository;

    public EventoInutilizacionDEService(EventoInutilizacionDERepository repository) {
        this.repository = repository;
    }

    public EventoInutilizacionDE save(EventoInutilizacionDE evento) {
        return repository.save(evento);
    }

    public Optional<EventoInutilizacionDE> findById(Long id) {
        return repository.findById(id);
    }

    public List<EventoInutilizacionDE> findByTimbradoId(Long timbradoId) {
        return repository.findByTimbradoId(timbradoId);
    }

    public List<EventoInutilizacionDE> findByTimbradoIdAndActivoTrue(Long timbradoId) {
        return repository.findByTimbradoIdAndActivoTrue(timbradoId);
    }

    public List<EventoInutilizacionDE> findEventosPendientes() {
        return repository.findEventosPendientes();
    }

    public Optional<EventoInutilizacionDE> findByEventoId(String eventoId) {
        return repository.findByEventoId(eventoId);
    }

    public Page<EventoInutilizacionDE> findWithFilters(
            Long empresaId,
            Long timbradoId,
            EstadoEvento estado,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin,
            Pageable pageable) {
        Specification<EventoInutilizacionDE> spec = EventoInutilizacionDESpecification.withFilters(
                empresaId, timbradoId, estado, fechaInicio, fechaFin);
        return repository.findAll(spec, pageable);
    }
}


