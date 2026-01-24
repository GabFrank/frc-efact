package com.frcefact.service;

import com.frcefact.model.Chofer;
import com.frcefact.model.Empresa;
import com.frcefact.repository.ChoferRepository;
import com.frcefact.repository.EmpresaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestión de choferes.
 * Implementa CRUD completo con validaciones de negocio.
 */
@Service
@Transactional
public class ChoferService {

    private static final Logger logger = LoggerFactory.getLogger(ChoferService.class);

    private final ChoferRepository choferRepository;
    private final EmpresaRepository empresaRepository;
    private final EmpresaSecurityService empresaSecurityService;

    public ChoferService(ChoferRepository choferRepository,
                        EmpresaRepository empresaRepository,
                        EmpresaSecurityService empresaSecurityService) {
        this.choferRepository = choferRepository;
        this.empresaRepository = empresaRepository;
        this.empresaSecurityService = empresaSecurityService;
    }

    /**
     * Crea un nuevo chofer.
     */
    public Chofer crearChofer(Long empresaId, Chofer chofer) {
        // Verificar acceso a la empresa
        empresaSecurityService.verificarAccesoEscritura(empresaId);

        // Buscar empresa
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada con ID: " + empresaId));

        // Asignar empresa
        chofer.setEmpresa(empresa);
        chofer.setActivo(true);

        Chofer choferGuardado = choferRepository.save(chofer);
        return choferGuardado;
    }

    /**
     * Actualiza un chofer existente.
     */
    public Chofer actualizarChofer(Long empresaId, Long choferId, Chofer choferActualizado) {
        // Verificar acceso a la empresa
        empresaSecurityService.verificarAccesoEscritura(empresaId);

        // Buscar chofer existente
        Chofer choferExistente = choferRepository.findById(choferId)
                .orElseThrow(() -> new IllegalArgumentException("Chofer no encontrado con ID: " + choferId));

        // Verificar que el chofer pertenece a la empresa
        if (!choferExistente.getEmpresa().getId().equals(empresaId)) {
            throw new IllegalArgumentException("El chofer no pertenece a la empresa especificada");
        }

        // Actualizar campos
        choferExistente.setNombre(choferActualizado.getNombre());
        choferExistente.setDocumento(choferActualizado.getDocumento());
        choferExistente.setDireccion(choferActualizado.getDireccion());
        choferExistente.setActivo(choferActualizado.getActivo());

        Chofer choferGuardado = choferRepository.save(choferExistente);
        return choferGuardado;
    }

    /**
     * Obtiene un chofer por ID.
     */
    @Transactional(readOnly = true)
    public Optional<Chofer> obtenerChoferPorId(Long empresaId, Long choferId) {
        // Verificar acceso a la empresa
        empresaSecurityService.verificarAccesoLectura(empresaId);

        Optional<Chofer> chofer = choferRepository.findById(choferId);

        // Verificar que el chofer pertenece a la empresa
        if (chofer.isPresent() && !chofer.get().getEmpresa().getId().equals(empresaId)) {
            return Optional.empty();
        }

        return chofer;
    }

    /**
     * Lista todos los choferes activos de una empresa.
     */
    @Transactional(readOnly = true)
    public List<Chofer> listarChoferesPorEmpresa(Long empresaId) {
        // Verificar acceso a la empresa
        empresaSecurityService.verificarAccesoLectura(empresaId);

        return choferRepository.findByEmpresaIdAndActivoTrue(empresaId);
    }

    /**
     * Lista choferes con paginación.
     */
    @Transactional(readOnly = true)
    public Page<Chofer> listarChoferesPaginados(Long empresaId, Pageable pageable) {
        // Verificar acceso a la empresa
        empresaSecurityService.verificarAccesoLectura(empresaId);

        return choferRepository.findByEmpresaIdAndActivoTrue(empresaId, pageable);
    }

    /**
     * Busca choferes por nombre o documento.
     */
    @Transactional(readOnly = true)
    public List<Chofer> buscarChoferes(Long empresaId, String busqueda) {
        // Verificar acceso a la empresa
        empresaSecurityService.verificarAccesoLectura(empresaId);

        if (busqueda == null || busqueda.trim().isEmpty()) {
            return choferRepository.findByEmpresaIdAndActivoTrue(empresaId);
        }

        return choferRepository.buscarChoferes(empresaId, busqueda.trim());
    }

    /**
     * Busca choferes con paginación y filtros.
     */
    @Transactional(readOnly = true)
    public Page<Chofer> buscarChoferesPaginados(Long empresaId, String busqueda, Boolean activo, Pageable pageable) {
        // Verificar acceso a la empresa
        empresaSecurityService.verificarAccesoLectura(empresaId);

        String busquedaNormalizada = (busqueda != null && !busqueda.trim().isEmpty()) 
                ? busqueda.trim() 
                : null;

        return choferRepository.buscarChoferes(empresaId, busquedaNormalizada, activo, pageable);
    }

    /**
     * Desactiva un chofer (soft delete).
     */
    public void desactivarChofer(Long empresaId, Long choferId) {
        // Verificar acceso a la empresa
        empresaSecurityService.verificarAccesoEscritura(empresaId);

        Chofer chofer = choferRepository.findById(choferId)
                .orElseThrow(() -> new IllegalArgumentException("Chofer no encontrado con ID: " + choferId));

        // Verificar que el chofer pertenece a la empresa
        if (!chofer.getEmpresa().getId().equals(empresaId)) {
            throw new IllegalArgumentException("El chofer no pertenece a la empresa especificada");
        }

        chofer.setActivo(false);
        choferRepository.save(chofer);
    }

    /**
     * Reactiva un chofer.
     */
    public void reactivarChofer(Long empresaId, Long choferId) {
        // Verificar acceso a la empresa
        empresaSecurityService.verificarAccesoEscritura(empresaId);

        Chofer chofer = choferRepository.findById(choferId)
                .orElseThrow(() -> new IllegalArgumentException("Chofer no encontrado con ID: " + choferId));

        // Verificar que el chofer pertenece a la empresa
        if (!chofer.getEmpresa().getId().equals(empresaId)) {
            throw new IllegalArgumentException("El chofer no pertenece a la empresa especificada");
        }

        chofer.setActivo(true);
        choferRepository.save(chofer);
    }

    /**
     * Cuenta choferes activos de una empresa.
     */
    @Transactional(readOnly = true)
    public long contarChoferesActivos(Long empresaId) {
        // Verificar acceso a la empresa
        empresaSecurityService.verificarAccesoLectura(empresaId);

        return choferRepository.countByEmpresaIdAndActivoTrue(empresaId);
    }
}
