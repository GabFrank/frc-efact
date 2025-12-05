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

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Transactional
public class NotaDebitoService {

    private static final Logger logger = LoggerFactory.getLogger(NotaDebitoService.class);

    private final NotaDebitoRepository notaDebitoRepository;
    private final EmpresaRepository empresaRepository;
    private final TimbradoDetalleRepository timbradoDetalleRepository;
    private final ClienteRepository clienteRepository;
    private final FacturaLegalRepository facturaLegalRepository;
    private final ProductoRepository productoRepository;
    private final TimbradoDetalleService timbradoDetalleService;
    private final EmpresaSecurityService empresaSecurityService;

    public NotaDebitoService(
            NotaDebitoRepository notaDebitoRepository,
            EmpresaRepository empresaRepository,
            TimbradoDetalleRepository timbradoDetalleRepository,
            ClienteRepository clienteRepository,
            FacturaLegalRepository facturaLegalRepository,
            ProductoRepository productoRepository,
            TimbradoDetalleService timbradoDetalleService,
            EmpresaSecurityService empresaSecurityService) {
        this.notaDebitoRepository = notaDebitoRepository;
        this.empresaRepository = empresaRepository;
        this.timbradoDetalleRepository = timbradoDetalleRepository;
        this.clienteRepository = clienteRepository;
        this.facturaLegalRepository = facturaLegalRepository;
        this.productoRepository = productoRepository;
        this.timbradoDetalleService = timbradoDetalleService;
        this.empresaSecurityService = empresaSecurityService;
    }

    @Auditable(entidad = "NotaDebito", accion = AccionEnum.CREATE)
    public NotaDebito crearNotaDebito(NotaDebito notaDebito) {
        // Verificar permisos
        empresaSecurityService.verificarAccesoEscritura(notaDebito.getEmpresa().getId());

        // Validar empresa
        Empresa empresa = empresaRepository.findById(notaDebito.getEmpresa().getId())
                .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada"));
        notaDebito.setEmpresa(empresa);

        // Validar timbrado
        TimbradoDetalle timbradoDetalle = timbradoDetalleRepository.findById(
                notaDebito.getTimbradoDetalle().getId())
                .orElseThrow(() -> new EntityNotFoundException("Timbrado detalle no encontrado"));
        notaDebito.setTimbradoDetalle(timbradoDetalle);

        if (!timbradoDetalle.getTimbrado().isVigente()) {
            throw new IllegalStateException("El timbrado no está vigente");
        }

        // Validar factura asociada (Requerida según SIFEN para ND)
        if (notaDebito.getFacturaLegal() == null || notaDebito.getFacturaLegal().getId() == null) {
            throw new IllegalArgumentException("La nota de débito debe tener una factura asociada");
        }
        
        FacturaLegal factura = facturaLegalRepository.findById(notaDebito.getFacturaLegal().getId())
                .orElseThrow(() -> new EntityNotFoundException("Factura asociada no encontrada"));
        notaDebito.setFacturaLegal(factura);
        
        // Si no tiene cliente, usar el de la factura
        if (notaDebito.getCliente() == null && factura.getCliente() != null) {
             notaDebito.setCliente(factura.getCliente());
        }

        // Validar items
        if (notaDebito.getItems() == null || notaDebito.getItems().isEmpty()) {
            throw new IllegalArgumentException("La nota de débito debe tener al menos un item");
        }

        // Asignar número
        Long numeroAsignado = timbradoDetalleService.incrementarNumeroActual(timbradoDetalle.getId());
        notaDebito.setNumeroNotaDebito(numeroAsignado.intValue());

        if (notaDebito.getFecha() == null) {
            notaDebito.setFecha(LocalDateTime.now());
        }

        // Cargar cliente
        if (notaDebito.getCliente() != null && notaDebito.getCliente().getId() != null) {
            Cliente cliente = clienteRepository.findById(notaDebito.getCliente().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado"));
            notaDebito.setCliente(cliente);
            
            if (notaDebito.getNombre() == null) notaDebito.setNombre(cliente.getNombre());
            if (notaDebito.getRuc() == null) notaDebito.setRuc(cliente.getRuc());
            if (notaDebito.getDireccion() == null) notaDebito.setDireccion(cliente.getDireccion());
        }

        // Procesar items
        for (NotaDebitoItem item : notaDebito.getItems()) {
            item.setNotaDebito(notaDebito);

            if (item.getProducto() != null && item.getProducto().getId() != null) {
                Producto producto = productoRepository.findById(item.getProducto().getId())
                        .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
                item.setProducto(producto);
                if (item.getDescripcion() == null || item.getDescripcion().isEmpty()) {
                    item.setDescripcion(producto.getDescripcion());
                }
                // Si no tiene IVA, usar el del producto
                if (item.getIva() == null) {
                    item.setIva(producto.getIva() != null ? producto.getIva() : 10);
                }
            } else {
                // Si no tiene producto y no tiene IVA, usar 10% por defecto
                if (item.getIva() == null) {
                    item.setIva(10);
                }
            }
            
            if (item.getTotal() == null) {
                item.calcularTotal();
            }
        }

        // Calcular totales
        if (notaDebito.getTotalFinal() == null || notaDebito.getTotalFinal().compareTo(BigDecimal.ZERO) == 0) {
             notaDebito.recalcularTotales();
        }

        return notaDebitoRepository.save(notaDebito);
    }

    @Transactional(readOnly = true)
    public NotaDebito obtenerPorId(Long id) {
        NotaDebito nd = notaDebitoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Nota de débito no encontrada"));
        
        empresaSecurityService.verificarAccesoLectura(nd.getEmpresa().getId());
        
        // Inicializar lazy loading
        nd.getItems().size();
        if (nd.getDocumentoElectronico() != null) {
            nd.getDocumentoElectronico().getId();
        }
        
        return nd;
    }

    @Transactional(readOnly = true)
    public Page<NotaDebito> listarPorEmpresa(Long empresaId, Pageable pageable) {
        empresaSecurityService.verificarAccesoLectura(empresaId);
        return notaDebitoRepository.findByEmpresaIdAndActivoTrue(empresaId, pageable);
    }

    public void desactivar(Long id) {
        NotaDebito nd = obtenerPorId(id);
        empresaSecurityService.verificarAccesoEscritura(nd.getEmpresa().getId());
        nd.setActivo(false);
        notaDebitoRepository.save(nd);
    }
}

