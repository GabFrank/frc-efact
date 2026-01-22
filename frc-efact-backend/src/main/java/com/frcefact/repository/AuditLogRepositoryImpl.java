package com.frcefact.repository;

import com.frcefact.model.AccionEnum;
import com.frcefact.model.AuditLog;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación personalizada del repositorio AuditLog usando Criteria Builder.
 * Esto evita problemas con enums de PostgreSQL en queries JPQL.
 */
@Repository
public class AuditLogRepositoryImpl implements AuditLogRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<AuditLog> buscarConFiltros(
            Long usuarioId,
            Long empresaId,
            String entidadTipo,
            AccionEnum accion,
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta,
            Pageable pageable) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        
        // Query para obtener los resultados
        CriteriaQuery<AuditLog> query = cb.createQuery(AuditLog.class);
        Root<AuditLog> root = query.from(AuditLog.class);
        
        // Construir predicados dinámicamente
        List<Predicate> predicates = new ArrayList<>();
        
        if (usuarioId != null) {
            predicates.add(cb.equal(root.join("usuario", JoinType.INNER).get("id"), usuarioId));
        }
        
        if (empresaId != null) {
            // Usar LEFT JOIN porque empresa puede ser null
            predicates.add(cb.equal(root.join("empresa", JoinType.LEFT).get("id"), empresaId));
        }
        
        if (entidadTipo != null && !entidadTipo.trim().isEmpty()) {
            predicates.add(cb.equal(root.get("entidadTipo"), entidadTipo));
        }
        
        if (accion != null) {
            predicates.add(cb.equal(root.get("accion"), accion));
        }
        
        if (fechaDesde != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("fechaHora"), fechaDesde));
        }
        
        if (fechaHasta != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("fechaHora"), fechaHasta));
        }
        
        // Aplicar predicados
        query.where(predicates.toArray(new Predicate[0]));
        
        // Ordenar por fechaHora descendente
        query.orderBy(cb.desc(root.get("fechaHora")));
        
        // Query para contar el total
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<AuditLog> countRoot = countQuery.from(AuditLog.class);
        
        // Reconstruir predicados para la query de conteo
        List<Predicate> countPredicates = new ArrayList<>();
        
        if (usuarioId != null) {
            countPredicates.add(cb.equal(countRoot.join("usuario", JoinType.INNER).get("id"), usuarioId));
        }
        
        if (empresaId != null) {
            countPredicates.add(cb.equal(countRoot.join("empresa", JoinType.LEFT).get("id"), empresaId));
        }
        
        if (entidadTipo != null && !entidadTipo.trim().isEmpty()) {
            countPredicates.add(cb.equal(countRoot.get("entidadTipo"), entidadTipo));
        }
        
        if (accion != null) {
            countPredicates.add(cb.equal(countRoot.get("accion"), accion));
        }
        
        if (fechaDesde != null) {
            countPredicates.add(cb.greaterThanOrEqualTo(countRoot.get("fechaHora"), fechaDesde));
        }
        
        if (fechaHasta != null) {
            countPredicates.add(cb.lessThanOrEqualTo(countRoot.get("fechaHora"), fechaHasta));
        }
        
        countQuery.select(cb.count(countRoot));
        if (!countPredicates.isEmpty()) {
            countQuery.where(countPredicates.toArray(new Predicate[0]));
        }
        
        // Ejecutar query de conteo
        Long total = entityManager.createQuery(countQuery).getSingleResult();
        
        // Ejecutar query principal con paginación
        TypedQuery<AuditLog> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());
        
        List<AuditLog> results = typedQuery.getResultList();
        
        return new PageImpl<>(results, pageable, total);
    }
}

