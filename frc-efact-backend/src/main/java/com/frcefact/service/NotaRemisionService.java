package com.frcefact.service;

import com.frcefact.annotation.Auditable;
import com.frcefact.dto.NotaRemisionDto;
import com.frcefact.dto.mapper.NotaRemisionMapper;
import com.frcefact.model.*;
import com.frcefact.repository.*;
import com.frcefact.repository.specification.NotaRemisionSpecification;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@Transactional
public class NotaRemisionService {

    private static final Logger logger = LoggerFactory.getLogger(NotaRemisionService.class);

    private final NotaRemisionRepository notaRemisionRepository;
    private final EmpresaRepository empresaRepository;
    private final TimbradoDetalleRepository timbradoDetalleRepository;
    private final ClienteRepository clienteRepository;
    private final FacturaLegalRepository facturaLegalRepository;
    private final ProductoRepository productoRepository;
    private final VehiculoRepository vehiculoRepository;
    private final ChoferRepository choferRepository;
    private final TimbradoDetalleService timbradoDetalleService;
    private final EmpresaSecurityService empresaSecurityService;
    private final NotaRemisionMapper notaRemisionMapper;

    public NotaRemisionService(
            NotaRemisionRepository notaRemisionRepository,
            EmpresaRepository empresaRepository,
            TimbradoDetalleRepository timbradoDetalleRepository,
            ClienteRepository clienteRepository,
            FacturaLegalRepository facturaLegalRepository,
            ProductoRepository productoRepository,
            VehiculoRepository vehiculoRepository,
            ChoferRepository choferRepository,
            TimbradoDetalleService timbradoDetalleService,
            EmpresaSecurityService empresaSecurityService,
            NotaRemisionMapper notaRemisionMapper) {
        this.notaRemisionRepository = notaRemisionRepository;
        this.empresaRepository = empresaRepository;
        this.timbradoDetalleRepository = timbradoDetalleRepository;
        this.clienteRepository = clienteRepository;
        this.facturaLegalRepository = facturaLegalRepository;
        this.productoRepository = productoRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.choferRepository = choferRepository;
        this.timbradoDetalleService = timbradoDetalleService;
        this.empresaSecurityService = empresaSecurityService;
        this.notaRemisionMapper = notaRemisionMapper;
    }

    @Auditable(entidad = "NotaRemision", accion = AccionEnum.CREATE)
    public NotaRemision crearNotaRemision(NotaRemision notaRemision) {
        // Verificar permisos
        empresaSecurityService.verificarAccesoEscritura(notaRemision.getEmpresa().getId());

        // Validar empresa
        Empresa empresa = empresaRepository.findById(notaRemision.getEmpresa().getId())
                .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada"));
        notaRemision.setEmpresa(empresa);

        // Validar timbrado
        TimbradoDetalle timbradoDetalle = timbradoDetalleRepository.findById(
                notaRemision.getTimbradoDetalle().getId())
                .orElseThrow(() -> new EntityNotFoundException("Timbrado detalle no encontrado"));
        notaRemision.setTimbradoDetalle(timbradoDetalle);

        if (!timbradoDetalle.getTimbrado().isVigente()) {
            throw new IllegalStateException("El timbrado no está vigente");
        }

        // Validar cliente (destinatario)
        if (notaRemision.getCliente() != null && notaRemision.getCliente().getId() != null) {
            Cliente cliente = clienteRepository.findById(notaRemision.getCliente().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado"));
            notaRemision.setCliente(cliente);
            
            if (notaRemision.getNombreDestinatario() == null) notaRemision.setNombreDestinatario(cliente.getNombre());
            if (notaRemision.getRucDestinatario() == null) notaRemision.setRucDestinatario(cliente.getRuc());
            if (notaRemision.getDireccionDestinatario() == null) notaRemision.setDireccionDestinatario(cliente.getDireccion());
        }

        // Validar factura asociada (opcional)
        if (notaRemision.getFacturaLegal() != null && notaRemision.getFacturaLegal().getId() != null) {
            FacturaLegal factura = facturaLegalRepository.findById(notaRemision.getFacturaLegal().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Factura asociada no encontrada"));
            notaRemision.setFacturaLegal(factura);
        }

        // Validar vehículo (opcional)
        if (notaRemision.getVehiculo() != null && notaRemision.getVehiculo().getId() != null) {
            Vehiculo vehiculo = vehiculoRepository.findById(notaRemision.getVehiculo().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Vehículo no encontrado"));
            // Verificar que el vehículo pertenece a la misma empresa
            if (!vehiculo.getEmpresa().getId().equals(empresa.getId())) {
                throw new IllegalArgumentException("El vehículo no pertenece a la empresa de la nota");
            }
            notaRemision.setVehiculo(vehiculo);
            // Copiar datos a campos legacy para compatibilidad
            notaRemision.setVehiculoMarca(vehiculo.getMarca());
            notaRemision.setVehiculoMatricula(vehiculo.getMatricula());
        }

        // Validar chofer (opcional)
        if (notaRemision.getChofer() != null && notaRemision.getChofer().getId() != null) {
            Chofer chofer = choferRepository.findById(notaRemision.getChofer().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Chofer no encontrado"));
            // Verificar que el chofer pertenece a la misma empresa
            if (!chofer.getEmpresa().getId().equals(empresa.getId())) {
                throw new IllegalArgumentException("El chofer no pertenece a la empresa de la nota");
            }
            notaRemision.setChofer(chofer);
            // Copiar datos a campos legacy para compatibilidad
            notaRemision.setConductorNombre(chofer.getNombre());
            notaRemision.setConductorDoc(chofer.getDocumento());
            notaRemision.setConductorDireccion(chofer.getDireccion());
        }

        // Validar items
        if (notaRemision.getItems() == null || notaRemision.getItems().isEmpty()) {
            throw new IllegalArgumentException("La nota de remisión debe tener al menos un item");
        }

        // Asignar número (usar método específico para notas de remisión)
        Long numeroAsignado = timbradoDetalleService.incrementarNumeroNotaRemision(timbradoDetalle.getId());
        notaRemision.setNumeroNotaRemision(numeroAsignado.intValue());

        if (notaRemision.getFecha() == null) {
            notaRemision.setFecha(LocalDateTime.now());
        }

        // Procesar items
        for (NotaRemisionItem item : notaRemision.getItems()) {
            item.setNotaRemision(notaRemision);

            if (item.getProducto() != null && item.getProducto().getId() != null) {
                Producto producto = productoRepository.findById(item.getProducto().getId())
                        .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
                item.setProducto(producto);
                if (item.getDescripcion() == null || item.getDescripcion().isEmpty()) {
                    item.setDescripcion(producto.getDescripcion());
                }
            }
        }

        return notaRemisionRepository.save(notaRemision);
    }

    @Transactional(readOnly = true)
    public NotaRemision obtenerPorId(Long id) {
        NotaRemision nr = notaRemisionRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new EntityNotFoundException("Nota de remisión no encontrada"));
        
        empresaSecurityService.verificarAccesoLectura(nr.getEmpresa().getId());
        
        return nr;
    }

    @Transactional(readOnly = true)
    public Page<NotaRemisionDto> listarPorEmpresa(
            Long empresaId,
            String numero,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            String motivo,
            String destinatario,
            String vehiculo,
            String chofer,
            String estadoDE,
            Pageable pageable) {
        empresaSecurityService.verificarAccesoLectura(empresaId);

        Specification<NotaRemision> spec = NotaRemisionSpecification.filterBy(
                empresaId, numero, fechaDesde, fechaHasta, motivo, destinatario, vehiculo, chofer, estadoDE
        );

        return notaRemisionRepository.findAll(spec, pageable).map(notaRemisionMapper::toDto);
    }

    public void desactivar(Long id) {
        NotaRemision nr = obtenerPorId(id);
        empresaSecurityService.verificarAccesoEscritura(nr.getEmpresa().getId());
        nr.setActivo(false);
        notaRemisionRepository.save(nr);
    }
}

