package com.frcefact.repository.specification;

import com.frcefact.model.NotaRemision;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class NotaRemisionSpecification {

    public static Specification<NotaRemision> filterBy(
            Long empresaId,
            String numero,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            String motivo,
            String destinatario,
            String vehiculo,
            String chofer,
            String estadoDE) {

        return (root, query, cb) -> {
            // Optimización: Fetch de relaciones para evitar LazyInitializationException
            // Solo hacer fetch si la consulta no es de conteo (count)
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("vehiculo", jakarta.persistence.criteria.JoinType.LEFT);
                root.fetch("chofer", jakarta.persistence.criteria.JoinType.LEFT);
                root.fetch("cliente", jakarta.persistence.criteria.JoinType.LEFT);
                root.fetch("documentoElectronico", jakarta.persistence.criteria.JoinType.LEFT);
                root.fetch("timbradoDetalle", jakarta.persistence.criteria.JoinType.LEFT)
                    .fetch("timbrado", jakarta.persistence.criteria.JoinType.LEFT);
            }

            List<Predicate> predicates = new ArrayList<>();

            // Siempre filtrar por empresa y que esté activo
            predicates.add(cb.equal(root.get("empresa").get("id"), empresaId));
            predicates.add(cb.equal(root.get("activo"), true));

            if (numero != null && !numero.isEmpty()) {
                // Como no existe numeroFormateado en la base de datos, filtramos por el número correlativo
                // Si el usuario ingresa algo como 001-001-0000001, intentamos extraer la última parte
                String cleanNumero = numero;
                if (numero.contains("-")) {
                    String[] parts = numero.split("-");
                    cleanNumero = parts[parts.length - 1];
                }
                
                try {
                    Integer numInt = Integer.parseInt(cleanNumero);
                    predicates.add(cb.equal(root.get("numeroNotaRemision"), numInt));
                } catch (NumberFormatException e) {
                    // Si no es un número válido, intentamos búsqueda parcial como string
                    predicates.add(cb.like(cb.toString(root.get("numeroNotaRemision")), "%" + cleanNumero + "%"));
                }
            }

            if (fechaDesde != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fecha").as(LocalDate.class), fechaDesde));
            }

            if (fechaHasta != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fecha").as(LocalDate.class), fechaHasta));
            }

            if (motivo != null && !motivo.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("motivoEmision")), "%" + motivo.toLowerCase() + "%"));
            }

            if (destinatario != null && !destinatario.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("nombreDestinatario")), "%" + destinatario.toLowerCase() + "%"));
            }

            if (vehiculo != null && !vehiculo.isEmpty()) {
                String pattern = "%" + vehiculo.toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("vehiculoMatricula")), pattern),
                    cb.like(cb.lower(root.get("vehiculoMarca")), pattern)
                ));
            }

            if (chofer != null && !chofer.isEmpty()) {
                String pattern = "%" + chofer.toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("conductorNombre")), pattern),
                    cb.like(cb.lower(root.get("conductorDoc")), pattern)
                ));
            }

            if (estadoDE != null && !estadoDE.isEmpty()) {
                if (estadoDE.equals("SIN_DE")) {
                    predicates.add(cb.isNull(root.get("documentoElectronico")));
                } else {
                    predicates.add(cb.equal(root.get("documentoElectronico").get("estado").as(String.class), estadoDE));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
