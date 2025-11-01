package com.frcefact.service;

import com.frcefact.annotation.Auditable;
import com.frcefact.dto.TimbradoDetalleDto;
import com.frcefact.dto.mapper.TimbradoDetalleMapper;
import com.frcefact.model.AccionEnum;
import com.frcefact.model.Barrio;
import com.frcefact.model.Ciudad;
import com.frcefact.model.Timbrado;
import com.frcefact.model.TimbradoDetalle;
import com.frcefact.repository.TimbradoDetalleRepository;
import com.frcefact.repository.TimbradoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio para gestión de detalles de timbrados (puntos de expedición).
 * Implementa CRUD completo con validaciones de rangos y permisos.
 */
@Service
@Transactional
public class TimbradoDetalleService {

    private final TimbradoDetalleRepository timbradoDetalleRepository;
    private final TimbradoRepository timbradoRepository;
    private final TimbradoDetalleMapper timbradoDetalleMapper;
    private final EmpresaSecurityService empresaSecurityService;

    public TimbradoDetalleService(
            TimbradoDetalleRepository timbradoDetalleRepository,
            TimbradoRepository timbradoRepository,
            TimbradoDetalleMapper timbradoDetalleMapper,
            EmpresaSecurityService empresaSecurityService) {
        this.timbradoDetalleRepository = timbradoDetalleRepository;
        this.timbradoRepository = timbradoRepository;
        this.timbradoDetalleMapper = timbradoDetalleMapper;
        this.empresaSecurityService = empresaSecurityService;
    }

    /**
     * Crea un nuevo detalle de timbrado.
     * Valida rangos, unicidad y permisos.
     */
    @Auditable(entidad = "TimbradoDetalle", accion = AccionEnum.CREATE)
    public TimbradoDetalle crear(TimbradoDetalleDto dto) {
        // Validar que el timbrado existe
        Timbrado timbrado = timbradoRepository.findById(dto.getTimbradoId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Timbrado no encontrado con ID: " + dto.getTimbradoId()));

        // Verificar permisos de escritura
        empresaSecurityService.verificarAccesoEscritura(timbrado.getEmpresa().getId());

        // Validar datos del detalle
        validarDetalle(dto, null);

        // Verificar unicidad del punto de expedición
        if (timbradoDetalleRepository.findByTimbradoIdAndPuntoExpedicion(
                dto.getTimbradoId(), dto.getPuntoExpedicion()).isPresent()) {
            throw new IllegalArgumentException(
                    "Ya existe un punto de expedición con el código: " + dto.getPuntoExpedicion());
        }

        // Verificar que no hay rangos superpuestos (solo si se proporcionan rangos)
        if (dto.getRangoDesde() != null && dto.getRangoHasta() != null) {
            if (timbradoDetalleRepository.existsRangoSuperpuesto(
                    dto.getTimbradoId(), dto.getRangoDesde(), dto.getRangoHasta(), 0L)) {
                throw new IllegalArgumentException(
                        "El rango especificado se superpone con otro punto de expedición existente");
            }
        }

        // Convertir DTO a entidad
        TimbradoDetalle detalle = timbradoDetalleMapper.toEntity(dto);
        detalle.setTimbrado(timbrado);

        // Para timbrados electrónicos, establecer valores NULL si los rangos no están presentes
        if (timbrado.getIsElectronico() && (dto.getRangoDesde() == null || dto.getRangoHasta() == null)) {
            // Para timbrados electrónicos, usar NULL en lugar de 0
            detalle.setCantidad(null);
            detalle.setRangoDesde(null);
            detalle.setRangoHasta(null);
            detalle.setNumeroActual(null);
        } else if (dto.getRangoDesde() != null && dto.getRangoHasta() != null) {
            // Calcular cantidad y establecer número actual para timbrados no electrónicos
            long cantidad = dto.getRangoHasta() - dto.getRangoDesde() + 1;
            detalle.setCantidad(cantidad);
            detalle.setNumeroActual(dto.getRangoDesde());
        } else {
            // Si no hay rangos y no es electrónico, esto es un error
            throw new IllegalArgumentException("Los rangos son requeridos para timbrados no electrónicos");
        }

        return timbradoDetalleRepository.save(detalle);
    }

    /**
     * Actualiza un detalle de timbrado existente.
     */
    @Auditable(entidad = "TimbradoDetalle", accion = AccionEnum.UPDATE)
    public TimbradoDetalle actualizar(Long id, TimbradoDetalleDto dto) {
        TimbradoDetalle detalleExistente = timbradoDetalleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Detalle de timbrado no encontrado con ID: " + id));

        // Verificar permisos de escritura
        empresaSecurityService.verificarAccesoEscritura(detalleExistente.getTimbrado().getEmpresa().getId());

        // Validar datos del detalle
        validarDetalle(dto, id);

        // Verificar unicidad del punto de expedición (excluyendo el actual)
        if (timbradoDetalleRepository.findByTimbradoIdAndPuntoExpedicionAndIdNot(
                dto.getTimbradoId(), dto.getPuntoExpedicion(), id).isPresent()) {
            throw new IllegalArgumentException(
                    "Ya existe otro punto de expedición con el código: " + dto.getPuntoExpedicion());
        }

        // Verificar que no hay rangos superpuestos (excluyendo el actual, solo si se proporcionan rangos)
        if (dto.getRangoDesde() != null && dto.getRangoHasta() != null) {
            if (timbradoDetalleRepository.existsRangoSuperpuesto(
                    dto.getTimbradoId(), dto.getRangoDesde(), dto.getRangoHasta(), id)) {
                throw new IllegalArgumentException(
                        "El rango especificado se superpone con otro punto de expedición existente");
            }
        }

        // Obtener el timbrado para verificar si es electrónico
        Timbrado timbrado = detalleExistente.getTimbrado();
        
        // Actualizar campos
        detalleExistente.setPuntoExpedicion(dto.getPuntoExpedicion());
        detalleExistente.setCodigoEstablecimientoFactura(dto.getCodigoEstablecimientoFactura());
        
        // Actualizar rangos según el tipo de timbrado
        if (timbrado.getIsElectronico() && (dto.getRangoDesde() == null || dto.getRangoHasta() == null)) {
            // Para timbrados electrónicos sin rangos, establecer NULL
            if (detalleExistente.getRangoDesde() == null || (detalleExistente.getRangoDesde() != null && detalleExistente.getRangoDesde() == 0)) {
                detalleExistente.setCantidad(null);
                detalleExistente.setRangoDesde(null);
                detalleExistente.setRangoHasta(null);
                detalleExistente.setNumeroActual(null);
            }
        } else if (dto.getRangoDesde() != null && dto.getRangoHasta() != null) {
            // Actualizar rangos para timbrados no electrónicos o electrónicos con rangos
            detalleExistente.setRangoDesde(dto.getRangoDesde());
            detalleExistente.setRangoHasta(dto.getRangoHasta());
            detalleExistente.setCantidad(dto.getRangoHasta() - dto.getRangoDesde() + 1);

            // Ajustar número actual si está fuera del nuevo rango
            if (detalleExistente.getNumeroActual() < dto.getRangoDesde()) {
                detalleExistente.setNumeroActual(dto.getRangoDesde());
            } else if (detalleExistente.getNumeroActual() > dto.getRangoHasta()) {
                detalleExistente.setNumeroActual(dto.getRangoHasta());
            }
        }
        
        // Actualizar relaciones geográficas usando mapper
        if (dto.getCiudadId() != null) {
            Ciudad ciudad = new Ciudad();
            ciudad.setId(dto.getCiudadId());
            detalleExistente.setCiudad(ciudad);
        }
        if (dto.getBarrioId() != null) {
            Barrio barrio = new Barrio();
            barrio.setId(dto.getBarrioId());
            detalleExistente.setBarrio(barrio);
        }
        
        detalleExistente.setDireccion(dto.getDireccion());
        detalleExistente.setTelefono(dto.getTelefono());
        detalleExistente.setActivo(dto.getActivo());

        return timbradoDetalleRepository.save(detalleExistente);
    }

    /**
     * Obtiene un detalle por ID.
     */
    @Transactional(readOnly = true)
    public TimbradoDetalle obtenerPorId(Long id) {
        TimbradoDetalle detalle = timbradoDetalleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Detalle de timbrado no encontrado con ID: " + id));

        // Verificar permisos de lectura
        empresaSecurityService.verificarAccesoLectura(detalle.getTimbrado().getEmpresa().getId());

        return detalle;
    }

    /**
     * Lista todos los detalles de un timbrado.
     */
    @Transactional(readOnly = true)
    public List<TimbradoDetalle> listarPorTimbrado(Long timbradoId) {
        // Verificar que el timbrado existe y permisos
        Timbrado timbrado = timbradoRepository.findById(timbradoId)
                .orElseThrow(() -> new EntityNotFoundException("Timbrado no encontrado con ID: " + timbradoId));

        empresaSecurityService.verificarAccesoLectura(timbrado.getEmpresa().getId());

        return timbradoDetalleRepository.findByTimbradoIdOrderByPuntoExpedicionAsc(timbradoId);
    }

    /**
     * Lista detalles activos de un timbrado.
     */
    @Transactional(readOnly = true)
    public List<TimbradoDetalle> listarActivosPorTimbrado(Long timbradoId) {
        // Verificar que el timbrado existe y permisos
        Timbrado timbrado = timbradoRepository.findById(timbradoId)
                .orElseThrow(() -> new EntityNotFoundException("Timbrado no encontrado con ID: " + timbradoId));

        empresaSecurityService.verificarAccesoLectura(timbrado.getEmpresa().getId());

        return timbradoDetalleRepository.findByTimbradoIdAndActivoTrue(timbradoId);
    }

    /**
     * Desactiva un detalle de timbrado (soft delete).
     */
    @Auditable(entidad = "TimbradoDetalle", accion = AccionEnum.DELETE)
    public void desactivar(Long id) {
        TimbradoDetalle detalle = timbradoDetalleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Detalle de timbrado no encontrado con ID: " + id));

        // Verificar permisos de escritura
        empresaSecurityService.verificarAccesoEscritura(detalle.getTimbrado().getEmpresa().getId());

        // Verificar si tiene facturas asociadas
        if (!detalle.getFacturas().isEmpty()) {
            throw new IllegalStateException(
                    "No se puede eliminar el punto de expedición porque tiene facturas asociadas. " +
                    "Use la opción de desactivar en su lugar.");
        }

        detalle.setActivo(false);
        timbradoDetalleRepository.save(detalle);
    }

    /**
     * Obtiene detalles que están por agotarse.
     */
    @Transactional(readOnly = true)
    public List<TimbradoDetalle> obtenerDetallesPorAgotarse(Long timbradoId, double umbralPorcentaje) {
        // Verificar permisos
        Timbrado timbrado = timbradoRepository.findById(timbradoId)
                .orElseThrow(() -> new EntityNotFoundException("Timbrado no encontrado con ID: " + timbradoId));

        empresaSecurityService.verificarAccesoLectura(timbrado.getEmpresa().getId());

        return timbradoDetalleRepository.findDetallesPorAgotarse(timbradoId, umbralPorcentaje);
    }

    /**
     * Obtiene y incrementa el número actual de un detalle de timbrado.
     * Usado para asignar números de factura.
     */
    @Transactional
    public synchronized Long incrementarNumeroActual(Long detalleId) {
        TimbradoDetalle detalle = timbradoDetalleRepository.findById(detalleId)
                .orElseThrow(() -> new EntityNotFoundException("Detalle de timbrado no encontrado con ID: " + detalleId));

        // Verificar permisos de escritura
        empresaSecurityService.verificarAccesoEscritura(detalle.getTimbrado().getEmpresa().getId());

        // Verificar que tiene números disponibles
        if (!detalle.tieneNumerosDisponibles()) {
            throw new IllegalStateException("No hay números disponibles en el rango del punto de expedición");
        }

        // Obtener y incrementar el número
        Long numeroAsignado = detalle.obtenerYIncrementarNumeroActual();
        
        // Guardar los cambios
        timbradoDetalleRepository.save(detalle);
        
        return numeroAsignado;
    }

    /**
     * Valida los datos de un detalle de timbrado.
     * Los rangos son opcionales para timbrados electrónicos.
     */
    private void validarDetalle(TimbradoDetalleDto dto, Long excludeId) {
        // Validar rangos solo si están presentes (no son obligatorios para timbrados electrónicos)
        if (dto.getRangoDesde() != null && dto.getRangoHasta() != null) {
            if (dto.getRangoDesde() >= dto.getRangoHasta()) {
                throw new IllegalArgumentException("El rango desde debe ser menor que el rango hasta");
            }

            if (dto.getRangoDesde() < 1) {
                throw new IllegalArgumentException("El rango desde debe ser mayor a 0");
            }

            if (dto.getRangoHasta() < 1) {
                throw new IllegalArgumentException("El rango hasta debe ser mayor a 0");
            }

            // Validar que la cantidad calculada sea coherente
            long cantidadCalculada = dto.getRangoHasta() - dto.getRangoDesde() + 1;
            if (dto.getCantidad() != null && !dto.getCantidad().equals(cantidadCalculada)) {
                throw new IllegalArgumentException("La cantidad debe ser igual a (rango hasta - rango desde + 1)");
            }
        }

        // Validar formato de códigos
        if (dto.getPuntoExpedicion() != null && !dto.getPuntoExpedicion().matches("^[A-Z0-9]{1,10}$")) {
            throw new IllegalArgumentException("El punto de expedición debe contener solo letras mayúsculas y números");
        }

        if (dto.getCodigoEstablecimientoFactura() != null && 
            !dto.getCodigoEstablecimientoFactura().matches("^[A-Z0-9]{1,10}$")) {
            throw new IllegalArgumentException("El código de establecimiento debe contener solo letras mayúsculas y números");
        }
    }
}