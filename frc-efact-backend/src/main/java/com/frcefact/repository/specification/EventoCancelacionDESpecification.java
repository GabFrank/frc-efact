package com.frcefact.repository.specification;

import com.frcefact.model.EstadoEvento;
import com.frcefact.model.EventoCancelacionDE;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Specifications para filtros dinámicos de Eventos de Cancelación.
 */
public class EventoCancelacionDESpecification {

    public static Specification<EventoCancelacionDE> withFilters(
            Long empresaId,
            EstadoEvento estado,
            String cdcDocumento,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Siempre filtrar por activo
            predicates.add(criteriaBuilder.equal(root.get("activo"), true));

            // Filtro por empresa (a través de documentoElectronico -> facturaLegal)
            if (empresaId != null) {
                predicates.add(criteriaBuilder.equal(
                    root.join("documentoElectronico", JoinType.INNER)
                        .join("facturaLegal", JoinType.INNER)
                        .get("empresa").get("id"),
                    empresaId
                ));
            }

            // Filtro por estado
            if (estado != null) {
                predicates.add(criteriaBuilder.equal(root.get("estado"), estado));
            }

            // Filtro por CDC (búsqueda parcial)
            if (cdcDocumento != null && !cdcDocumento.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.upper(root.get("cdcDocumento")),
                    "%" + cdcDocumento.toUpperCase() + "%"
                ));
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

