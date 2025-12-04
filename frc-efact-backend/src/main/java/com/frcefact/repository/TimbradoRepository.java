package com.frcefact.repository;

import com.frcefact.model.Timbrado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Timbrado.
 */
@Repository
public interface TimbradoRepository extends JpaRepository<Timbrado, Long> {

    /**
     * Busca todos los timbrados de una empresa.
     */
    @Query("SELECT t FROM Timbrado t LEFT JOIN FETCH t.empresa WHERE t.empresa.id = :empresaId AND t.activo = true")
    List<Timbrado> findByEmpresaIdAndActivoTrue(@Param("empresaId") Long empresaId);

    /**
     * Busca timbrados por empresa ordenados por fecha de fin descendente.
     */
    @Query("SELECT t FROM Timbrado t LEFT JOIN FETCH t.empresa WHERE t.empresa.id = :empresaId ORDER BY t.fechaFin DESC")
    List<Timbrado> findByEmpresaIdOrderByFechaFinDesc(@Param("empresaId") Long empresaId);

    /**
     * Busca un timbrado por número.
     */
    @Query("SELECT t FROM Timbrado t LEFT JOIN FETCH t.empresa WHERE t.numero = :numero")
    Optional<Timbrado> findByNumero(@Param("numero") String numero);
    
    /**
     * Busca un timbrado por ID con empresa cargada.
     */
    @Query("SELECT t FROM Timbrado t LEFT JOIN FETCH t.empresa WHERE t.id = :id")
    Optional<Timbrado> findByIdWithEmpresa(@Param("id") Long id);

    /**
     * Busca timbrados vigentes de una empresa.
     */
    @Query("SELECT t FROM Timbrado t LEFT JOIN FETCH t.empresa WHERE t.empresa.id = :empresaId " +
           "AND t.activo = true " +
           "AND :fecha BETWEEN t.fechaInicio AND t.fechaFin")
    List<Timbrado> findTimbradosVigentes(@Param("empresaId") Long empresaId, 
                                         @Param("fecha") LocalDate fecha);

    /**
     * Busca timbrados electrónicos vigentes de una empresa.
     */
    @Query("SELECT t FROM Timbrado t LEFT JOIN FETCH t.empresa WHERE t.empresa.id = :empresaId " +
           "AND t.activo = true " +
           "AND t.isElectronico = true " +
           "AND :fecha BETWEEN t.fechaInicio AND t.fechaFin")
    List<Timbrado> findTimbradosElectronicosVigentes(@Param("empresaId") Long empresaId,
                                                      @Param("fecha") LocalDate fecha);

    /**
     * Busca timbrados que están por vencer.
     */
    @Query("SELECT t FROM Timbrado t LEFT JOIN FETCH t.empresa WHERE t.empresa.id = :empresaId " +
           "AND t.activo = true " +
           "AND t.fechaFin BETWEEN :fechaActual AND :fechaLimite")
    List<Timbrado> findTimbradosPorVencer(@Param("empresaId") Long empresaId,
                                          @Param("fechaActual") LocalDate fechaActual,
                                          @Param("fechaLimite") LocalDate fechaLimite);

    /**
     * Cuenta timbrados vigentes de una empresa.
     */
    @Query("SELECT COUNT(t) FROM Timbrado t WHERE t.empresa.id = :empresaId " +
           "AND t.activo = true " +
           "AND :fecha BETWEEN t.fechaInicio AND t.fechaFin")
    long countTimbradosVigentes(@Param("empresaId") Long empresaId,
                                @Param("fecha") LocalDate fecha);

    /**
     * Verifica si existe un timbrado con el número dado.
     */
    boolean existsByNumero(String numero);

    /**
     * Busca timbrados por empresa y estado activo.
     */
    List<Timbrado> findByEmpresaIdAndActivo(Long empresaId, Boolean activo);
}
