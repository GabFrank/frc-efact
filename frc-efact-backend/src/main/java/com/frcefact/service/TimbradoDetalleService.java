package com.frcefact.service;

import com.frcefact.model.Timbrado;
import com.frcefact.model.TimbradoDetalle;
import com.frcefact.repository.TimbradoDetalleRepository;
import com.frcefact.repository.TimbradoRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio para gestión de timbrados detalle (puntos de expedición).
 * Implementa gestión de rangos con lock optimista para concurrencia.
 */
@Service
@Transactional
public class TimbradoDetalleService {

    private static final Logger logger = LoggerFactory.getLogger(TimbradoDetalleService.class);
    private static final int UMBRAL_ALERTA_PORCENTAJE = 90; // Alertar cuando se use el 90%
    private static final long UMBRAL_ALERTA_CANTIDAD = 100; // Alertar cuando queden menos de 100

    private final TimbradoDetalleRepository timbradoDetalleRepository;
    private final TimbradoRepository timbradoRepository;
    private final EmpresaSecurityService empresaSecurityService;

    public TimbradoDetalleService(
            TimbradoDetalleRepository timbradoDetalleRepository,
            TimbradoRepository timbradoRepository,
            EmpresaSecurityService empresaSecurityService) {
        this.timbradoDetalleRepository = timbradoDetalleRepository;
        this.timbradoRepository = timbradoRepository;
        this.empresaSecurityService = empresaSecurityService;
    }

    /**
     * Crea un nuevo timbrado detalle.
     * Valida que el rango sea coherente.
     */
    public TimbradoDetalle crear(TimbradoDetalle detalle) {
        // Validar rango
        validarRango(detalle.getRangoDesde(), detalle.getRangoHasta());

        // Verificar que el timbrado existe
        Timbrado timbrado = timbradoRepository.findById(detalle.getTimbrado().getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Timbrado no encontrado con ID: " + detalle.getTimbrado().getId()));

        // Verificar permisos
        empresaSecurityService.verificarAccesoEscritura(timbrado.getEmpresa().getId());

        detalle.setTimbrado(timbrado);

        // Calcular cantidad si no está establecida
        if (detalle.getCantidad() == null) {
            detalle.setCantidad(detalle.getRangoHasta() - detalle.getRangoDesde() + 1);
        }

        // Establecer número actual al inicio del rango si no está establecido
        if (detalle.getNumeroActual() == null || detalle.getNumeroActual() == 0) {
            detalle.setNumeroActual(detalle.getRangoDesde());
        }

        return timbradoDetalleRepository.save(detalle);
    }

    /**
     * Actualiza un timbrado detalle existente.
     */
    public TimbradoDetalle actualizar(Long id, TimbradoDetalle detalleActualizado) {
        TimbradoDetalle detalleExistente = timbradoDetalleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Timbrado detalle no encontrado con ID: " + id));

        // Verificar permisos
        empresaSecurityService.verificarAccesoEscritura(
                detalleExistente.getTimbrado().getEmpresa().getId());

        // Validar rango
        validarRango(detalleActualizado.getRangoDesde(), detalleActualizado.getRangoHasta());

        // Actualizar campos
        detalleExistente.setPuntoExpedicion(detalleActualizado.getPuntoExpedicion());
        detalleExistente.setCodigoEstablecimientoFactura(detalleActualizado.getCodigoEstablecimientoFactura());
        detalleExistente.setRangoDesde(detalleActualizado.getRangoDesde());
        detalleExistente.setRangoHasta(detalleActualizado.getRangoHasta());
        detalleExistente.setCantidad(detalleActualizado.getRangoHasta() - detalleActualizado.getRangoDesde() + 1);
        detalleExistente.setDepartamento(detalleActualizado.getDepartamento());
        detalleExistente.setCiudad(detalleActualizado.getCiudad());
        detalleExistente.setCodigoCiudad(detalleActualizado.getCodigoCiudad());
        detalleExistente.setLocalidad(detalleActualizado.getLocalidad());
        detalleExistente.setBarrio(detalleActualizado.getBarrio());
        detalleExistente.setDireccion(detalleActualizado.getDireccion());
        detalleExistente.setTelefono(detalleActualizado.getTelefono());

        return timbradoDetalleRepository.save(detalleExistente);
    }

    /**
     * Obtiene un timbrado detalle por ID.
     */
    @Transactional(readOnly = true)
    public TimbradoDetalle obtenerPorId(Long id) {
        TimbradoDetalle detalle = timbradoDetalleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Timbrado detalle no encontrado con ID: " + id));

        // Verificar permisos
        empresaSecurityService.verificarAccesoLectura(detalle.getTimbrado().getEmpresa().getId());

        return detalle;
    }

    /**
     * Lista todos los detalles de un timbrado.
     */
    @Transactional(readOnly = true)
    public List<TimbradoDetalle> listarPorTimbrado(Long timbradoId) {
        Timbrado timbrado = timbradoRepository.findById(timbradoId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Timbrado no encontrado con ID: " + timbradoId));

        // Verificar permisos
        empresaSecurityService.verificarAccesoLectura(timbrado.getEmpresa().getId());

        return timbradoDetalleRepository.findByTimbradoIdAndActivoTrue(timbradoId);
    }

    /**
     * Desactiva un timbrado detalle (soft delete).
     */
    public void desactivar(Long id) {
        TimbradoDetalle detalle = timbradoDetalleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Timbrado detalle no encontrado con ID: " + id));

        // Verificar permisos
        empresaSecurityService.verificarAccesoEscritura(detalle.getTimbrado().getEmpresa().getId());

        detalle.setActivo(false);
        timbradoDetalleRepository.save(detalle);
    }

    /**
     * Incrementa el número actual del timbrado detalle con lock optimista.
     * Este método es thread-safe y maneja concurrencia.
     * 
     * @param id ID del timbrado detalle
     * @return El número asignado
     * @throws IllegalStateException si no hay números disponibles
     * @throws OptimisticLockException si hay conflicto de concurrencia (debe reintentarse)
     */
    public Long incrementarNumeroActual(Long id) {
        // Obtener con lock pesimista para evitar conflictos
        TimbradoDetalle detalle = timbradoDetalleRepository.findByIdWithLock(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Timbrado detalle no encontrado con ID: " + id));

        // Verificar permisos
        empresaSecurityService.verificarAccesoEscritura(detalle.getTimbrado().getEmpresa().getId());

        // Verificar que el timbrado esté vigente
        if (!detalle.getTimbrado().isVigente()) {
            throw new IllegalStateException(
                    "El timbrado no está vigente. Fecha fin: " + detalle.getTimbrado().getFechaFin());
        }

        // Verificar que hay números disponibles
        if (!detalle.tieneNumerosDisponibles()) {
            throw new IllegalStateException(
                    "No hay números disponibles en el rango. Rango: " + 
                    detalle.getRangoDesde() + " - " + detalle.getRangoHasta() +
                    ", Número actual: " + detalle.getNumeroActual());
        }

        // Obtener el número actual y incrementar
        Long numeroAsignado = detalle.obtenerYIncrementarNumeroActual();

        // Guardar cambios
        timbradoDetalleRepository.save(detalle);

        // Verificar si debe alertar
        verificarYAlertarRangoAgotandose(detalle);

        logger.info("Número asignado: {} del timbrado detalle ID: {}", numeroAsignado, id);

        return numeroAsignado;
    }

    /**
     * Verifica si un timbrado detalle tiene números disponibles.
     */
    @Transactional(readOnly = true)
    public boolean verificarDisponibilidad(Long id) {
        TimbradoDetalle detalle = timbradoDetalleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Timbrado detalle no encontrado con ID: " + id));

        // Verificar permisos
        empresaSecurityService.verificarAccesoLectura(detalle.getTimbrado().getEmpresa().getId());

        return detalle.tieneNumerosDisponibles() && detalle.getTimbrado().isVigente();
    }

    /**
     * Obtiene la cantidad de números disponibles.
     */
    @Transactional(readOnly = true)
    public long obtenerNumerosDisponibles(Long id) {
        TimbradoDetalle detalle = timbradoDetalleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Timbrado detalle no encontrado con ID: " + id));

        // Verificar permisos
        empresaSecurityService.verificarAccesoLectura(detalle.getTimbrado().getEmpresa().getId());

        return detalle.getNumerosDisponibles();
    }

    /**
     * Obtiene el porcentaje de utilización del rango.
     */
    @Transactional(readOnly = true)
    public double obtenerPorcentajeUtilizado(Long id) {
        TimbradoDetalle detalle = timbradoDetalleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Timbrado detalle no encontrado con ID: " + id));

        // Verificar permisos
        empresaSecurityService.verificarAccesoLectura(detalle.getTimbrado().getEmpresa().getId());

        return detalle.getPorcentajeUtilizado();
    }

    /**
     * Obtiene detalles que están por agotar su rango.
     */
    @Transactional(readOnly = true)
    public List<TimbradoDetalle> obtenerDetallesPorAgotarse(Long empresaId) {
        // Verificar permisos
        empresaSecurityService.verificarAccesoLectura(empresaId);

        return timbradoDetalleRepository.findDetallesPorAgotarse(empresaId, UMBRAL_ALERTA_CANTIDAD);
    }

    /**
     * Obtiene detalles con números disponibles de un timbrado.
     */
    @Transactional(readOnly = true)
    public List<TimbradoDetalle> obtenerDetallesConNumerosDisponibles(Long timbradoId) {
        Timbrado timbrado = timbradoRepository.findById(timbradoId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Timbrado no encontrado con ID: " + timbradoId));

        // Verificar permisos
        empresaSecurityService.verificarAccesoLectura(timbrado.getEmpresa().getId());

        return timbradoDetalleRepository.findDetallesConNumerosDisponibles(timbradoId);
    }

    /**
     * Valida que el rango sea coherente.
     */
    private void validarRango(Long rangoDesde, Long rangoHasta) {
        if (rangoDesde == null || rangoHasta == null) {
            throw new IllegalArgumentException("Los rangos desde y hasta son requeridos");
        }

        if (rangoDesde <= 0 || rangoHasta <= 0) {
            throw new IllegalArgumentException("Los rangos deben ser números positivos");
        }

        if (rangoHasta < rangoDesde) {
            throw new IllegalArgumentException(
                    "El rango hasta (" + rangoHasta + ") no puede ser menor que el rango desde (" + rangoDesde + ")");
        }

        if (rangoDesde.equals(rangoHasta)) {
            throw new IllegalArgumentException(
                    "El rango debe tener al menos un número. Desde y hasta no pueden ser iguales");
        }
    }

    /**
     * Verifica si el rango se está agotando y registra alerta.
     */
    private void verificarYAlertarRangoAgotandose(TimbradoDetalle detalle) {
        long numerosDisponibles = detalle.getNumerosDisponibles();
        double porcentajeUtilizado = detalle.getPorcentajeUtilizado();

        if (numerosDisponibles <= UMBRAL_ALERTA_CANTIDAD || 
            porcentajeUtilizado >= UMBRAL_ALERTA_PORCENTAJE) {
            
            logger.warn("ALERTA: El timbrado detalle ID {} está por agotarse. " +
                       "Números disponibles: {}, Porcentaje utilizado: {:.2f}%",
                       detalle.getId(), numerosDisponibles, porcentajeUtilizado);
            
            // TODO: Integrar con sistema de notificaciones cuando esté implementado
            // notificacionService.alertarRangoAgotandose(detalle);
        }
    }
}
