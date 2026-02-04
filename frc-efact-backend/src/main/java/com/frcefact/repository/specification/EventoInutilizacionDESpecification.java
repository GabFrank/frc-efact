package com.frcefact.repository.specification;

import com.frcefact.model.EstadoEvento;
import com.frcefact.model.EventoInutilizacionDE;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Specifications para filtros dinámicos de Eventos de Inutilización.
 */
public class EventoInutilizacionDESpecification {

    public static Specification<EventoInutilizacionDE> withFilters(
            Long empresaId,
            Long timbradoId,
            EstadoEvento estado,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Siempre filtrar por activo
            predicates.add(criteriaBuilder.equal(root.get("activo"), true));

            // Filtro por empresa (a través de timbrado)
            if (empresaId != null) {
                predicates.add(criteriaBuilder.equal(
                    root.join("timbrado", JoinType.INNER)
                        .get("empresa").get("id"),
                    empresaId
                ));
            }

            // Filtro por timbrado
            if (timbradoId != null) {
                predicates.add(criteriaBuilder.equal(
                    root.get("timbrado").get("id"),
                    timbradoId
                ));
            }

            // Filtro por estado
            if (estado != null) {
                predicates.add(criteriaBuilder.equal(root.get("estado"), estado));
            }

            // Filtro por fecha inicio
            if (fechaInicio != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("creadoEn"),
                    fechaInicio
                ));
            }

            // Filtro por fecha fin
            if (fechaFin != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("creadoEn"),
                    fechaFin
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

