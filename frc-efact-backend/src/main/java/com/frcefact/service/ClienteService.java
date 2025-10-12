package com.frcefact.service;

import com.frcefact.model.Cliente;
import com.frcefact.model.Empresa;
import com.frcefact.repository.ClienteRepository;
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
 * Servicio para gestión de clientes.
 * Implementa CRUD completo con validaciones de negocio.
 */
@Service
@Transactional
public class ClienteService {

    private static final Logger logger = LoggerFactory.getLogger(ClienteService.class);

    private final ClienteRepository clienteRepository;
    private final EmpresaRepository empresaRepository;
    private final EmpresaSecurityService empresaSecurityService;

    public ClienteService(ClienteRepository clienteRepository,
                         EmpresaRepository empresaRepository,
                         EmpresaSecurityService empresaSecurityService) {
        this.clienteRepository = clienteRepository;
        this.empresaRepository = empresaRepository;
        this.empresaSecurityService = empresaSecurityService;
    }

    /**
     * Crea un nuevo cliente.
     * Valida que el RUC sea requerido cuando tributa=true.
     */
    public Cliente crearCliente(Long empresaId, Cliente cliente) {
        logger.info("Creando cliente para empresa ID: {}", empresaId);

        // Verificar acceso a la empresa
        empresaSecurityService.verificarAccesoEscritura(empresaId);

        // Buscar empresa
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada con ID: " + empresaId));

        // Validar RUC si tributa
        validarRucSiTributa(cliente);

        // Verificar RUC duplicado si se proporciona
        if (cliente.getRuc() != null && !cliente.getRuc().isEmpty()) {
            if (clienteRepository.existsByEmpresaIdAndRucAndActivoTrue(empresaId, cliente.getRuc())) {
                throw new IllegalArgumentException("Ya existe un cliente con el RUC: " + cliente.getRuc());
            }
        }

        // Asignar empresa
        cliente.setEmpresa(empresa);
        cliente.setActivo(true);

        Cliente clienteGuardado = clienteRepository.save(cliente);
        logger.info("Cliente creado exitosamente con ID: {}", clienteGuardado.getId());

        return clienteGuardado;
    }

    /**
     * Actualiza un cliente existente.
     */
    public Cliente actualizarCliente(Long empresaId, Long clienteId, Cliente clienteActualizado) {
        logger.info("Actualizando cliente ID: {} de empresa ID: {}", clienteId, empresaId);

        // Verificar acceso a la empresa
        empresaSecurityService.verificarAccesoEscritura(empresaId);

        // Buscar cliente existente
        Cliente clienteExistente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + clienteId));

        // Verificar que el cliente pertenece a la empresa
        if (!clienteExistente.getEmpresa().getId().equals(empresaId)) {
            throw new IllegalArgumentException("El cliente no pertenece a la empresa especificada");
        }

        // Validar RUC si tributa
        validarRucSiTributa(clienteActualizado);

        // Verificar RUC duplicado si cambió
        if (clienteActualizado.getRuc() != null && !clienteActualizado.getRuc().isEmpty()) {
            if (!clienteActualizado.getRuc().equals(clienteExistente.getRuc())) {
                if (clienteRepository.existsByEmpresaIdAndRucAndActivoTrue(empresaId, clienteActualizado.getRuc())) {
                    throw new IllegalArgumentException("Ya existe un cliente con el RUC: " + clienteActualizado.getRuc());
                }
            }
        }

        // Actualizar campos
        clienteExistente.setNombre(clienteActualizado.getNombre());
        clienteExistente.setRazonSocial(clienteActualizado.getRazonSocial());
        clienteExistente.setRuc(clienteActualizado.getRuc());
        clienteExistente.setDireccion(clienteActualizado.getDireccion());
        clienteExistente.setTelefono(clienteActualizado.getTelefono());
        clienteExistente.setEmail(clienteActualizado.getEmail());
        clienteExistente.setTributa(clienteActualizado.getTributa());
        clienteExistente.setTipoContribuyente(clienteActualizado.getTipoContribuyente());

        Cliente clienteGuardado = clienteRepository.save(clienteExistente);
        logger.info("Cliente actualizado exitosamente con ID: {}", clienteGuardado.getId());

        return clienteGuardado;
    }

    /**
     * Obtiene un cliente por ID.
     */
    @Transactional(readOnly = true)
    public Optional<Cliente> obtenerClientePorId(Long empresaId, Long clienteId) {
        logger.debug("Obteniendo cliente ID: {} de empresa ID: {}", clienteId, empresaId);

        // Verificar acceso a la empresa
        empresaSecurityService.verificarAccesoLectura(empresaId);

        Optional<Cliente> cliente = clienteRepository.findById(clienteId);

        // Verificar que el cliente pertenece a la empresa
        if (cliente.isPresent() && !cliente.get().getEmpresa().getId().equals(empresaId)) {
            return Optional.empty();
        }

        return cliente;
    }

    /**
     * Lista todos los clientes activos de una empresa.
     */
    @Transactional(readOnly = true)
    public List<Cliente> listarClientesPorEmpresa(Long empresaId) {
        logger.debug("Listando clientes de empresa ID: {}", empresaId);

        // Verificar acceso a la empresa
        empresaSecurityService.verificarAccesoLectura(empresaId);

        return clienteRepository.findByEmpresaIdAndActivoTrue(empresaId);
    }

    /**
     * Lista clientes con paginación.
     */
    @Transactional(readOnly = true)
    public Page<Cliente> listarClientesPaginados(Long empresaId, Pageable pageable) {
        logger.debug("Listando clientes paginados de empresa ID: {}", empresaId);

        // Verificar acceso a la empresa
        empresaSecurityService.verificarAccesoLectura(empresaId);

        return clienteRepository.findByEmpresaIdAndActivoTrue(empresaId, pageable);
    }

    /**
     * Busca clientes por nombre, razón social o RUC.
     */
    @Transactional(readOnly = true)
    public List<Cliente> buscarClientes(Long empresaId, String busqueda) {
        logger.debug("Buscando clientes con término: {} en empresa ID: {}", busqueda, empresaId);

        // Verificar acceso a la empresa
        empresaSecurityService.verificarAccesoLectura(empresaId);

        if (busqueda == null || busqueda.trim().isEmpty()) {
            return clienteRepository.findByEmpresaIdAndActivoTrue(empresaId);
        }

        return clienteRepository.buscarClientes(empresaId, busqueda.trim());
    }

    /**
     * Busca clientes con paginación.
     */
    @Transactional(readOnly = true)
    public Page<Cliente> buscarClientesPaginados(Long empresaId, String busqueda, Pageable pageable) {
        logger.debug("Buscando clientes paginados con término: {} en empresa ID: {}", busqueda, empresaId);

        // Verificar acceso a la empresa
        empresaSecurityService.verificarAccesoLectura(empresaId);

        if (busqueda == null || busqueda.trim().isEmpty()) {
            return clienteRepository.findByEmpresaIdAndActivoTrue(empresaId, pageable);
        }

        return clienteRepository.buscarClientes(empresaId, busqueda.trim(), pageable);
    }

    /**
     * Busca un cliente por RUC.
     */
    @Transactional(readOnly = true)
    public Optional<Cliente> buscarPorRuc(Long empresaId, String ruc) {
        logger.debug("Buscando cliente por RUC: {} en empresa ID: {}", ruc, empresaId);

        // Verificar acceso a la empresa
        empresaSecurityService.verificarAccesoLectura(empresaId);

        if (ruc == null || ruc.trim().isEmpty()) {
            return Optional.empty();
        }

        return clienteRepository.findByEmpresaIdAndRuc(empresaId, ruc.trim());
    }

    /**
     * Desactiva un cliente (soft delete).
     */
    public void desactivarCliente(Long empresaId, Long clienteId) {
        logger.info("Desactivando cliente ID: {} de empresa ID: {}", clienteId, empresaId);

        // Verificar acceso a la empresa
        empresaSecurityService.verificarAccesoEscritura(empresaId);

        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + clienteId));

        // Verificar que el cliente pertenece a la empresa
        if (!cliente.getEmpresa().getId().equals(empresaId)) {
            throw new IllegalArgumentException("El cliente no pertenece a la empresa especificada");
        }

        cliente.setActivo(false);
        clienteRepository.save(cliente);

        logger.info("Cliente desactivado exitosamente con ID: {}", clienteId);
    }

    /**
     * Reactiva un cliente.
     */
    public void reactivarCliente(Long empresaId, Long clienteId) {
        logger.info("Reactivando cliente ID: {} de empresa ID: {}", clienteId, empresaId);

        // Verificar acceso a la empresa
        empresaSecurityService.verificarAccesoEscritura(empresaId);

        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + clienteId));

        // Verificar que el cliente pertenece a la empresa
        if (!cliente.getEmpresa().getId().equals(empresaId)) {
            throw new IllegalArgumentException("El cliente no pertenece a la empresa especificada");
        }

        cliente.setActivo(true);
        clienteRepository.save(cliente);

        logger.info("Cliente reactivado exitosamente con ID: {}", clienteId);
    }

    /**
     * Cuenta clientes activos de una empresa.
     */
    @Transactional(readOnly = true)
    public long contarClientesActivos(Long empresaId) {
        logger.debug("Contando clientes activos de empresa ID: {}", empresaId);

        // Verificar acceso a la empresa
        empresaSecurityService.verificarAccesoLectura(empresaId);

        return clienteRepository.countByEmpresaIdAndActivoTrue(empresaId);
    }

    /**
     * Valida que el RUC sea requerido cuando tributa=true.
     */
    private void validarRucSiTributa(Cliente cliente) {
        if (cliente.getTributa() != null && cliente.getTributa()) {
            if (cliente.getRuc() == null || cliente.getRuc().trim().isEmpty()) {
                throw new IllegalArgumentException("El RUC es requerido cuando el cliente tributa");
            }
        }
    }
}
