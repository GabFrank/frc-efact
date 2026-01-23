package com.frcefact.service;

import com.frcefact.annotation.Auditable;
import com.frcefact.model.*;
import com.frcefact.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final TimbradoDetalleService timbradoDetalleService;
    private final EmpresaSecurityService empresaSecurityService;

    public NotaRemisionService(
            NotaRemisionRepository notaRemisionRepository,
            EmpresaRepository empresaRepository,
            TimbradoDetalleRepository timbradoDetalleRepository,
            ClienteRepository clienteRepository,
            FacturaLegalRepository facturaLegalRepository,
            ProductoRepository productoRepository,
            TimbradoDetalleService timbradoDetalleService,
            EmpresaSecurityService empresaSecurityService) {
        this.notaRemisionRepository = notaRemisionRepository;
        this.empresaRepository = empresaRepository;
        this.timbradoDetalleRepository = timbradoDetalleRepository;
        this.clienteRepository = clienteRepository;
        this.facturaLegalRepository = facturaLegalRepository;
        this.productoRepository = productoRepository;
        this.timbradoDetalleService = timbradoDetalleService;
        this.empresaSecurityService = empresaSecurityService;
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
    public Page<NotaRemision> listarPorEmpresa(Long empresaId, Pageable pageable) {
        empresaSecurityService.verificarAccesoLectura(empresaId);
        return notaRemisionRepository.findByEmpresaIdAndActivoTrueWithItems(empresaId, pageable);
    }

    public void desactivar(Long id) {
        NotaRemision nr = obtenerPorId(id);
        empresaSecurityService.verificarAccesoEscritura(nr.getEmpresa().getId());
        nr.setActivo(false);
        notaRemisionRepository.save(nr);
    }
}

