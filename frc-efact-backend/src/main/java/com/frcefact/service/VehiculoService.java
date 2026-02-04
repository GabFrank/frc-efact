package com.frcefact.service;

import com.frcefact.model.Empresa;
import com.frcefact.model.Vehiculo;
import com.frcefact.repository.EmpresaRepository;
import com.frcefact.repository.VehiculoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestión de vehículos.
 * Implementa CRUD completo con validaciones de negocio.
 */
@Service
@Transactional
public class VehiculoService {

    private static final Logger logger = LoggerFactory.getLogger(VehiculoService.class);

    private final VehiculoRepository vehiculoRepository;
    private final EmpresaRepository empresaRepository;
    private final EmpresaSecurityService empresaSecurityService;

    public VehiculoService(VehiculoRepository vehiculoRepository,
                          EmpresaRepository empresaRepository,
                          EmpresaSecurityService empresaSecurityService) {
        this.vehiculoRepository = vehiculoRepository;
        this.empresaRepository = empresaRepository;
        this.empresaSecurityService = empresaSecurityService;
    }

    /**
     * Crea un nuevo vehículo.
     */
    public Vehiculo crearVehiculo(Long empresaId, Vehiculo vehiculo) {
        // Verificar acceso a la empresa
        empresaSecurityService.verificarAccesoEscritura(empresaId);

        // Buscar empresa
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada con ID: " + empresaId));

        // Verificar matrícula duplicada
        if (vehiculoRepository.existsByEmpresaIdAndMatriculaAndActivoTrue(empresaId, vehiculo.getMatricula())) {
            throw new IllegalArgumentException("Ya existe un vehículo activo con la matrícula: " + vehiculo.getMatricula());
        }

        // Asignar empresa
        vehiculo.setEmpresa(empresa);
        vehiculo.setActivo(true);

        Vehiculo vehiculoGuardado = vehiculoRepository.save(vehiculo);
        return vehiculoGuardado;
    }

    /**
     * Actualiza un vehículo existente.
     */
    public Vehiculo actualizarVehiculo(Long empresaId, Long vehiculoId, Vehiculo vehiculoActualizado) {
        // Verificar acceso a la empresa
        empresaSecurityService.verificarAccesoEscritura(empresaId);

        // Buscar vehículo existente
        Vehiculo vehiculoExistente = vehiculoRepository.findById(vehiculoId)
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado con ID: " + vehiculoId));

        // Verificar que el vehículo pertenece a la empresa
        if (!vehiculoExistente.getEmpresa().getId().equals(empresaId)) {
            throw new IllegalArgumentException("El vehículo no pertenece a la empresa especificada");
        }

        // Verificar matrícula duplicada si cambió
        if (!vehiculoActualizado.getMatricula().equals(vehiculoExistente.getMatricula())) {
            if (vehiculoRepository.existsByEmpresaIdAndMatriculaAndActivoTrue(empresaId, vehiculoActualizado.getMatricula())) {
                throw new IllegalArgumentException("Ya existe un vehículo activo con la matrícula: " + vehiculoActualizado.getMatricula());
            }
        }

        // Actualizar campos
        vehiculoExistente.setMarca(vehiculoActualizado.getMarca());
        vehiculoExistente.setMatricula(vehiculoActualizado.getMatricula());
        vehiculoExistente.setActivo(vehiculoActualizado.getActivo());

        Vehiculo vehiculoGuardado = vehiculoRepository.save(vehiculoExistente);
        return vehiculoGuardado;
    }

    /**
     * Obtiene un vehículo por ID.
     */
    @Transactional(readOnly = true)
    public Optional<Vehiculo> obtenerVehiculoPorId(Long empresaId, Long vehiculoId) {
        // Verificar acceso a la empresa
        empresaSecurityService.verificarAccesoLectura(empresaId);

        Optional<Vehiculo> vehiculo = vehiculoRepository.findById(vehiculoId);

        // Verificar que el vehículo pertenece a la empresa
        if (vehiculo.isPresent() && !vehiculo.get().getEmpresa().getId().equals(empresaId)) {
            return Optional.empty();
        }

        return vehiculo;
    }

    /**
     * Lista todos los vehículos activos de una empresa.
     */
    @Transactional(readOnly = true)
    public List<Vehiculo> listarVehiculosPorEmpresa(Long empresaId) {
        // Verificar acceso a la empresa
        empresaSecurityService.verificarAccesoLectura(empresaId);

        return vehiculoRepository.findByEmpresaIdAndActivoTrue(empresaId);
    }

    /**
     * Lista vehículos con paginación.
     */
    @Transactional(readOnly = true)
    public Page<Vehiculo> listarVehiculosPaginados(Long empresaId, Pageable pageable) {
        // Verificar acceso a la empresa
        empresaSecurityService.verificarAccesoLectura(empresaId);

        return vehiculoRepository.findByEmpresaIdAndActivoTrue(empresaId, pageable);
    }

    /**
     * Busca vehículos por matrícula o marca.
     */
    @Transactional(readOnly = true)
    public List<Vehiculo> buscarVehiculos(Long empresaId, String busqueda) {
        // Verificar acceso a la empresa
        empresaSecurityService.verificarAccesoLectura(empresaId);

        if (busqueda == null || busqueda.trim().isEmpty()) {
            return vehiculoRepository.findByEmpresaIdAndActivoTrue(empresaId);
        }

        return vehiculoRepository.buscarVehiculos(empresaId, busqueda.trim());
    }

    /**
     * Busca vehículos con paginación y filtros.
     */
    @Transactional(readOnly = true)
    public Page<Vehiculo> buscarVehiculosPaginados(Long empresaId, String busqueda, Boolean activo, Pageable pageable) {
        // Verificar acceso a la empresa
        empresaSecurityService.verificarAccesoLectura(empresaId);

        String busquedaNormalizada = (busqueda != null && !busqueda.trim().isEmpty()) 
                ? busqueda.trim() 
                : null;

        return vehiculoRepository.buscarVehiculos(empresaId, busquedaNormalizada, activo, pageable);
    }

    /**
     * Desactiva un vehículo (soft delete).
     */
    public void desactivarVehiculo(Long empresaId, Long vehiculoId) {
        // Verificar acceso a la empresa
        empresaSecurityService.verificarAccesoEscritura(empresaId);

        Vehiculo vehiculo = vehiculoRepository.findById(vehiculoId)
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado con ID: " + vehiculoId));

        // Verificar que el vehículo pertenece a la empresa
        if (!vehiculo.getEmpresa().getId().equals(empresaId)) {
            throw new IllegalArgumentException("El vehículo no pertenece a la empresa especificada");
        }

        vehiculo.setActivo(false);
        vehiculoRepository.save(vehiculo);
    }

    /**
     * Reactiva un vehículo.
     */
    public void reactivarVehiculo(Long empresaId, Long vehiculoId) {
        // Verificar acceso a la empresa
        empresaSecurityService.verificarAccesoEscritura(empresaId);

        Vehiculo vehiculo = vehiculoRepository.findById(vehiculoId)
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado con ID: " + vehiculoId));

        // Verificar que el vehículo pertenece a la empresa
        if (!vehiculo.getEmpresa().getId().equals(empresaId)) {
            throw new IllegalArgumentException("El vehículo no pertenece a la empresa especificada");
        }

        // Verificar que no existe otro vehículo activo con la misma matrícula
        if (vehiculoRepository.existsByEmpresaIdAndMatriculaAndActivoTrue(empresaId, vehiculo.getMatricula())) {
            Optional<Vehiculo> otroVehiculo = vehiculoRepository.findByEmpresaIdAndMatricula(empresaId, vehiculo.getMatricula());
            if (otroVehiculo.isPresent() && !otroVehiculo.get().getId().equals(vehiculoId)) {
                throw new IllegalArgumentException("Ya existe un vehículo activo con la matrícula: " + vehiculo.getMatricula());
            }
        }

        vehiculo.setActivo(true);
        vehiculoRepository.save(vehiculo);
    }

    /**
     * Cuenta vehículos activos de una empresa.
     */
    @Transactional(readOnly = true)
    public long contarVehiculosActivos(Long empresaId) {
        // Verificar acceso a la empresa
        empresaSecurityService.verificarAccesoLectura(empresaId);

        return vehiculoRepository.countByEmpresaIdAndActivoTrue(empresaId);
    }
}
