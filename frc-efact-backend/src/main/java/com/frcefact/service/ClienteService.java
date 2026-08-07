package com.frcefact.service;

import com.frcefact.model.Ciudad;
import com.frcefact.model.Cliente;
import com.frcefact.model.Empresa;
import com.frcefact.model.Pais;
import com.frcefact.model.TipoClienteSifen;
import com.frcefact.repository.CiudadRepository;
import com.frcefact.repository.ClienteRepository;
import com.frcefact.repository.EmpresaRepository;
import com.frcefact.repository.PaisRepository;
import com.frcefact.validation.ReglaRucCliente;
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
    private final PaisRepository paisRepository;
    private final CiudadRepository ciudadRepository;
    private final EmpresaSecurityService empresaSecurityService;

    public ClienteService(ClienteRepository clienteRepository,
                         EmpresaRepository empresaRepository,
                         PaisRepository paisRepository,
                         CiudadRepository ciudadRepository,
                         EmpresaSecurityService empresaSecurityService) {
        this.clienteRepository = clienteRepository;
        this.empresaRepository = empresaRepository;
        this.paisRepository = paisRepository;
        this.ciudadRepository = ciudadRepository;
        this.empresaSecurityService = empresaSecurityService;
    }

    /**
     * Crea un nuevo cliente.
     * Valida que el RUC sea requerido cuando tributa=true.
     */
    public Cliente crearCliente(Long empresaId, Cliente cliente) {
        // Verificar acceso a la empresa
        empresaSecurityService.verificarAccesoEscritura(empresaId);

        // Buscar empresa
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada con ID: " + empresaId));

        // Validar RUC según tipo de cliente SIFEN
        validarRucSegunTipoCliente(cliente);

        // Verificar RUC duplicado si se proporciona
        if (cliente.getRuc() != null && !cliente.getRuc().isEmpty()) {
            if (clienteRepository.existsByEmpresaIdAndRucAndActivoTrue(empresaId, cliente.getRuc())) {
                throw new IllegalArgumentException("Ya existe un cliente con el RUC: " + cliente.getRuc());
            }
        }

        // Establecer relaciones geográficas
        establecerRelacionesGeograficas(cliente);

        // Asignar empresa
        cliente.setEmpresa(empresa);
        cliente.setActivo(true);

        Cliente clienteGuardado = clienteRepository.save(cliente);
        return clienteGuardado;
    }

    /**
     * Actualiza un cliente existente.
     */
    public Cliente actualizarCliente(Long empresaId, Long clienteId, Cliente clienteActualizado) {
        // Verificar acceso a la empresa
        empresaSecurityService.verificarAccesoEscritura(empresaId);

        // Buscar cliente existente
        Cliente clienteExistente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + clienteId));

        // Verificar que el cliente pertenece a la empresa
        if (!clienteExistente.getEmpresa().getId().equals(empresaId)) {
            throw new IllegalArgumentException("El cliente no pertenece a la empresa especificada");
        }

        // Validar RUC según tipo de cliente SIFEN
        validarRucSegunTipoCliente(clienteActualizado);

        // Verificar RUC duplicado si cambió
        if (clienteActualizado.getRuc() != null && !clienteActualizado.getRuc().isEmpty()) {
            if (!clienteActualizado.getRuc().equals(clienteExistente.getRuc())) {
                if (clienteRepository.existsByEmpresaIdAndRucAndActivoTrue(empresaId, clienteActualizado.getRuc())) {
                    throw new IllegalArgumentException("Ya existe un cliente con el RUC: " + clienteActualizado.getRuc());
                }
            }
        }

        // Actualizar campos básicos
        clienteExistente.setNombre(clienteActualizado.getNombre());
        clienteExistente.setRazonSocial(clienteActualizado.getRazonSocial());
        clienteExistente.setRuc(clienteActualizado.getRuc());
        clienteExistente.setDireccion(clienteActualizado.getDireccion());
        clienteExistente.setNumeroCasa(clienteActualizado.getNumeroCasa());
        clienteExistente.setTelefono(clienteActualizado.getTelefono());
        clienteExistente.setCelular(clienteActualizado.getCelular());
        clienteExistente.setEmail(clienteActualizado.getEmail());
        
        // Actualizar tipo de cliente SIFEN
        if (clienteActualizado.getTipoClienteSifen() != null) {
            clienteExistente.setTipoClienteSifen(clienteActualizado.getTipoClienteSifen());
        } else {
            // Fallback a campos legacy
            clienteExistente.setTributa(clienteActualizado.getTributa());
            clienteExistente.setTipoContribuyente(clienteActualizado.getTipoContribuyente());
        }

        // Actualizar relaciones geográficas
        establecerRelacionesGeograficas(clienteExistente, clienteActualizado);

        Cliente clienteGuardado = clienteRepository.save(clienteExistente);
        return clienteGuardado;
    }

    /**
     * Obtiene un cliente por ID.
     */
    @Transactional(readOnly = true)
    public Optional<Cliente> obtenerClientePorId(Long empresaId, Long clienteId) {
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
        // Verificar acceso a la empresa
        empresaSecurityService.verificarAccesoLectura(empresaId);

        return clienteRepository.findByEmpresaIdAndActivoTrue(empresaId);
    }

    /**
     * Lista clientes con paginación.
     */
    @Transactional(readOnly = true)
    public Page<Cliente> listarClientesPaginados(Long empresaId, Pageable pageable) {
        // Verificar acceso a la empresa
        empresaSecurityService.verificarAccesoLectura(empresaId);

        return clienteRepository.findByEmpresaIdAndActivoTrue(empresaId, pageable);
    }

    /**
     * Busca clientes por nombre, razón social o RUC.
     */
    @Transactional(readOnly = true)
    public List<Cliente> buscarClientes(Long empresaId, String busqueda) {
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
        // Verificar acceso a la empresa
        empresaSecurityService.verificarAccesoLectura(empresaId);

        if (busqueda == null || busqueda.trim().isEmpty()) {
            return clienteRepository.findByEmpresaIdAndActivoTrue(empresaId, pageable);
        }

        return clienteRepository.buscarClientes(empresaId, busqueda.trim(), pageable);
    }

    /**
     * Busca clientes con filtros múltiples y paginación.
     * Soporta filtros por: búsqueda de texto, tipoClienteSifen, y activo.
     */
    @Transactional(readOnly = true)
    public Page<Cliente> buscarClientesConFiltros(
            Long empresaId,
            String busqueda,
            TipoClienteSifen tipoClienteSifen,
            Boolean activo,
            Pageable pageable) {
        // Verificar acceso a la empresa
        empresaSecurityService.verificarAccesoLectura(empresaId);

        // Normalizar búsqueda
        String busquedaNormalizada = (busqueda != null && !busqueda.trim().isEmpty()) 
                ? busqueda.trim() 
                : null;

        return clienteRepository.buscarClientesConFiltros(
                empresaId,
                busquedaNormalizada,
                tipoClienteSifen,
                activo,
                pageable);
    }

    /**
     * Busca un cliente por RUC.
     */
    @Transactional(readOnly = true)
    public Optional<Cliente> buscarPorRuc(Long empresaId, String ruc) {
        // Verificar acceso a la empresa
        empresaSecurityService.verificarAccesoLectura(empresaId);

        if (ruc == null || ruc.trim().isEmpty()) {
            return Optional.empty();
        }

        return clienteRepository.findByEmpresaIdAndRuc(empresaId, ruc.trim());
    }

    /**
     * Verifica si existe un cliente activo con el RUC especificado para la empresa.
     * Útil para validación en tiempo real desde el frontend.
     * 
     * @param empresaId ID de la empresa
     * @param ruc RUC a verificar
     * @param excluirClienteId ID del cliente a excluir (para edición, permite que el mismo cliente mantenga su RUC)
     * @return true si existe un cliente activo con ese RUC, false en caso contrario
     */
    @Transactional(readOnly = true)
    public boolean existeRucEnEmpresa(Long empresaId, String ruc, Long excluirClienteId) {
        logger.debug("Verificando existencia de RUC: {} en empresa ID: {}, excluyendo cliente ID: {}", ruc, empresaId, excluirClienteId);

        // Verificar acceso a la empresa
        empresaSecurityService.verificarAccesoLectura(empresaId);

        if (ruc == null || ruc.trim().isEmpty()) {
            return false;
        }

        // Buscar cliente por RUC
        Optional<Cliente> clienteOpt = clienteRepository.findByEmpresaIdAndRuc(empresaId, ruc.trim());

        if (clienteOpt.isEmpty()) {
            return false;
        }

        Cliente cliente = clienteOpt.get();

        // Si el cliente no está activo, no se considera duplicado
        if (!cliente.getActivo()) {
            return false;
        }

        // Si se especifica un cliente a excluir (edición), y es el mismo cliente, no es duplicado
        if (excluirClienteId != null && cliente.getId().equals(excluirClienteId)) {
            return false;
        }

        return true;
    }

    /**
     * Desactiva un cliente (soft delete).
     */
    public void desactivarCliente(Long empresaId, Long clienteId) {

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
    }

    /**
     * Reactiva un cliente.
     */
    public void reactivarCliente(Long empresaId, Long clienteId) {

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
    }

    /**
     * Cuenta clientes activos de una empresa.
     */
    @Transactional(readOnly = true)
    public long contarClientesActivos(Long empresaId) {

        // Verificar acceso a la empresa
        empresaSecurityService.verificarAccesoLectura(empresaId);

        return clienteRepository.countByEmpresaIdAndActivoTrue(empresaId);
    }

    /**
     * Valida el RUC según el tipo de cliente SIFEN, delegando en {@link ReglaRucCliente}.
     *
     * <p>Antes este método chequeaba <b>solo presencia</b> del RUC, mientras que
     * {@code ClienteRucValidator} —que corre sobre el DTO y solo en el borde HTTP— además exigía
     * el guion. Con eso, cualquier camino que creara un {@code Cliente} sin pasar por el controller
     * (un import, otro servicio, un seed) podía grabar un contribuyente sin dígito verificador, y
     * después {@code SifenService} emitía un {@code dDVRec} vacío que SIFEN rechaza sin explicar
     * por qué. Ahora los dos puntos de entrada comparten la misma regla, que además verifica que
     * el DV sea el correcto.
     */
    private void validarRucSegunTipoCliente(Cliente cliente) {
        boolean requiereRuc = ReglaRucCliente.requiereRuc(
                cliente.getTipoClienteSifen(), cliente.getTributa());

        ReglaRucCliente.Resultado resultado = ReglaRucCliente.validar(requiereRuc, cliente.getRuc());
        if (!resultado.valido()) {
            throw new IllegalArgumentException(resultado.mensaje());
        }
    }

    /**
     * Establece las relaciones geográficas (Pais y Ciudad) para un cliente nuevo.
     */
    private void establecerRelacionesGeograficas(Cliente cliente) {
        establecerRelacionesGeograficas(cliente, cliente);
    }

    /**
     * Establece las relaciones geográficas (Pais y Ciudad) desde el cliente origen al cliente destino.
     * Si el cliente origen tiene IDs de país o ciudad, carga las entidades correspondientes.
     * Si no se especifica país, establece Paraguay por defecto.
     */
    private void establecerRelacionesGeograficas(Cliente clienteDestino, Cliente clienteOrigen) {
        // Establecer País
        if (clienteOrigen.getPais() != null && clienteOrigen.getPais().getId() != null) {
            Long paisId = clienteOrigen.getPais().getId();
            Pais pais = paisRepository.findById(paisId)
                    .orElseThrow(() -> new IllegalArgumentException("País no encontrado con ID: " + paisId));
            clienteDestino.setPais(pais);
        } else if (clienteDestino.getPais() == null) {
            // Por defecto, si no se especifica, establecer Paraguay
            Optional<Pais> paraguay = paisRepository.findByCodigo("PY");
            paraguay.ifPresent(clienteDestino::setPais);
        }

        // Establecer Ciudad
        if (clienteOrigen.getCiudad() != null && clienteOrigen.getCiudad().getId() != null) {
            Long ciudadId = clienteOrigen.getCiudad().getId();
            Ciudad ciudad = ciudadRepository.findById(ciudadId)
                    .orElseThrow(() -> new IllegalArgumentException("Ciudad no encontrada con ID: " + ciudadId));
            clienteDestino.setCiudad(ciudad);
        }
    }
}
