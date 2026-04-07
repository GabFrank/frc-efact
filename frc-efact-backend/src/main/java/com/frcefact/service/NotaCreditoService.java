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
import java.util.Set;

@Service
@Transactional
public class NotaCreditoService {

    private static final Logger logger = LoggerFactory.getLogger(NotaCreditoService.class);
    private static final Set<String> MOTIVOS_SIFEN_VALIDOS = Set.of(
            "DEVOLUCION_Y_AJUSTES_DE_PRECIOS",
            "DEVOLUCION",
            "DESCUENTO",
            "BONIFICACION",
            "CREDITO_INCOBRABLE",
            "RECUPERO_DE_COSTO",
            "RECUPERO_DE_GASTO",
            "AJUSTE_DE_PRECIO"
    );

    private final NotaCreditoRepository notaCreditoRepository;
    private final NotaCreditoItemRepository notaCreditoItemRepository;
    private final EmpresaRepository empresaRepository;
    private final TimbradoDetalleRepository timbradoDetalleRepository;
    private final ClienteRepository clienteRepository;
    private final FacturaLegalRepository facturaLegalRepository;
    private final ProductoRepository productoRepository;
    private final TimbradoDetalleService timbradoDetalleService;
    private final EmpresaSecurityService empresaSecurityService;

    public NotaCreditoService(
            NotaCreditoRepository notaCreditoRepository,
            NotaCreditoItemRepository notaCreditoItemRepository,
            EmpresaRepository empresaRepository,
            TimbradoDetalleRepository timbradoDetalleRepository,
            ClienteRepository clienteRepository,
            FacturaLegalRepository facturaLegalRepository,
            ProductoRepository productoRepository,
            TimbradoDetalleService timbradoDetalleService,
            EmpresaSecurityService empresaSecurityService) {
        this.notaCreditoRepository = notaCreditoRepository;
        this.notaCreditoItemRepository = notaCreditoItemRepository;
        this.empresaRepository = empresaRepository;
        this.timbradoDetalleRepository = timbradoDetalleRepository;
        this.clienteRepository = clienteRepository;
        this.facturaLegalRepository = facturaLegalRepository;
        this.productoRepository = productoRepository;
        this.timbradoDetalleService = timbradoDetalleService;
        this.empresaSecurityService = empresaSecurityService;
    }

    @Auditable(entidad = "NotaCredito", accion = AccionEnum.CREATE)
    public NotaCredito crearNotaCredito(NotaCredito notaCredito) {
        // Verificar permisos
        empresaSecurityService.verificarAccesoEscritura(notaCredito.getEmpresa().getId());

        // Validar empresa
        Empresa empresa = empresaRepository.findById(notaCredito.getEmpresa().getId())
                .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada"));
        notaCredito.setEmpresa(empresa);

        // Validar timbrado
        TimbradoDetalle timbradoDetalle = timbradoDetalleRepository.findById(
                notaCredito.getTimbradoDetalle().getId())
                .orElseThrow(() -> new EntityNotFoundException("Timbrado detalle no encontrado"));
        notaCredito.setTimbradoDetalle(timbradoDetalle);

        if (!timbradoDetalle.getTimbrado().isVigente()) {
            throw new IllegalStateException("El timbrado no está vigente");
        }

        // Validar factura asociada (Requerida según SIFEN para NC)
        if (notaCredito.getFacturaLegal() == null || notaCredito.getFacturaLegal().getId() == null) {
            throw new IllegalArgumentException("La nota de crédito debe tener una factura asociada");
        }
        
        FacturaLegal factura = facturaLegalRepository.findById(notaCredito.getFacturaLegal().getId())
                .orElseThrow(() -> new EntityNotFoundException("Factura asociada no encontrada"));
        notaCredito.setFacturaLegal(factura);

        String motivoEmision = notaCredito.getMotivoEmision() != null
                ? notaCredito.getMotivoEmision().trim().toUpperCase()
                : null;
        if (motivoEmision == null || motivoEmision.isBlank()) {
            throw new IllegalArgumentException("Debe seleccionar un motivo de emisión válido de SIFEN");
        }
        if (!MOTIVOS_SIFEN_VALIDOS.contains(motivoEmision)) {
            throw new IllegalArgumentException("Motivo de emisión no válido para SIFEN: " + motivoEmision);
        }
        notaCredito.setMotivoEmision(motivoEmision);

        // Regla SIFEN: la NC debe mantener la misma moneda y tipo de cambio del documento referenciado
        String monedaFactura = factura.getMonedaExtranjera();
        BigDecimal cambioFactura = factura.getCambio();
        boolean esMonedaExtranjera = monedaFactura != null
                && !monedaFactura.isBlank()
                && !"PYG".equalsIgnoreCase(monedaFactura);
        notaCredito.setMonedaExtranjera(monedaFactura != null ? monedaFactura.toUpperCase() : "PYG");
        notaCredito.setCambio(esMonedaExtranjera ? cambioFactura : null);

        if (esMonedaExtranjera && (cambioFactura == null || cambioFactura.compareTo(BigDecimal.ZERO) <= 0)) {
            throw new IllegalArgumentException("La factura asociada tiene moneda extranjera pero no tiene tipo de cambio válido");
        }
        
        // Si no tiene cliente, usar el de la factura
        if (notaCredito.getCliente() == null && factura.getCliente() != null) {
             notaCredito.setCliente(factura.getCliente());
        }

        // Si hay factura asociada, los ítems/totales se copian 1:1 desde la factura para evitar inconsistencias.
        if (notaCredito.getItems() == null) {
            notaCredito.setItems(new java.util.ArrayList<>());
        } else {
            notaCredito.getItems().clear();
        }

        if (factura.getItems() == null || factura.getItems().isEmpty()) {
            throw new IllegalArgumentException("La factura asociada no tiene items");
        }

        for (FacturaLegalItem itemFactura : factura.getItems()) {
            NotaCreditoItem itemNC = new NotaCreditoItem();
            itemNC.setNotaCredito(notaCredito);
            itemNC.setProducto(itemFactura.getProducto());
            itemNC.setCantidad(itemFactura.getCantidad());
            itemNC.setDescripcion(itemFactura.getDescripcion());
            itemNC.setPrecioUnitario(itemFactura.getPrecioUnitario());
            itemNC.setDescuento(BigDecimal.ZERO);
            itemNC.setTotal(itemFactura.getTotal());
            Integer ivaProducto = itemFactura.getProducto() != null && itemFactura.getProducto().getIva() != null
                    ? itemFactura.getProducto().getIva()
                    : 10;
            itemNC.setIva(ivaProducto);
            notaCredito.getItems().add(itemNC);
        }

        notaCredito.setIvaParcial0(factura.getIvaParcial0() != null ? factura.getIvaParcial0() : BigDecimal.ZERO);
        notaCredito.setIvaParcial5(factura.getIvaParcial5() != null ? factura.getIvaParcial5() : BigDecimal.ZERO);
        notaCredito.setIvaParcial10(factura.getIvaParcial10() != null ? factura.getIvaParcial10() : BigDecimal.ZERO);
        notaCredito.setTotalParcial0(factura.getTotalParcial0() != null ? factura.getTotalParcial0() : BigDecimal.ZERO);
        notaCredito.setTotalParcial5(factura.getTotalParcial5() != null ? factura.getTotalParcial5() : BigDecimal.ZERO);
        notaCredito.setTotalParcial10(factura.getTotalParcial10() != null ? factura.getTotalParcial10() : BigDecimal.ZERO);
        notaCredito.setTotalParcial(factura.getTotalParcial() != null ? factura.getTotalParcial() : BigDecimal.ZERO);
        notaCredito.setDescuentoFinal(factura.getDescuentoFinal() != null ? factura.getDescuentoFinal() : BigDecimal.ZERO);
        notaCredito.setTotalFinal(factura.getTotalFinal() != null ? factura.getTotalFinal() : BigDecimal.ZERO);

        // Asignar número
        Long numeroAsignado = timbradoDetalleService.incrementarNumeroNotaCredito(timbradoDetalle.getId());
        notaCredito.setNumeroNotaCredito(numeroAsignado.intValue());

        if (notaCredito.getFecha() == null) {
            notaCredito.setFecha(LocalDateTime.now());
        }

        // Cargar cliente
        if (notaCredito.getCliente() != null && notaCredito.getCliente().getId() != null) {
            Cliente cliente = clienteRepository.findById(notaCredito.getCliente().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado"));
            notaCredito.setCliente(cliente);
            
            if (notaCredito.getNombre() == null) notaCredito.setNombre(cliente.getNombre());
            if (notaCredito.getRuc() == null) notaCredito.setRuc(cliente.getRuc());
            if (notaCredito.getDireccion() == null) notaCredito.setDireccion(cliente.getDireccion());
        }

        // Procesar items ya copiados de la factura asociada
        for (NotaCreditoItem item : notaCredito.getItems()) {
            item.setNotaCredito(notaCredito);

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
            
            // Si total es nulo, calcularlo (pero idealmente viene del front)
            if (item.getTotal() == null) {
                item.calcularTotal();
            }

            // Ítems/totales ya vienen de factura en PYG internos: no aplicar heurística de conversión.
        }

        // Totales se copian desde factura; no recalcular para evitar diferencias por redondeo.

        return notaCreditoRepository.save(notaCredito);
    }

    @Transactional(readOnly = true)
    public NotaCredito obtenerPorId(Long id) {
        NotaCredito nc = notaCreditoRepository.findByIdWithEmpresa(id)
                .orElseThrow(() -> new EntityNotFoundException("Nota de crédito no encontrada"));
        
        empresaSecurityService.verificarAccesoLectura(nc.getEmpresa().getId());
        
        // Inicializar lazy loading
        nc.getItems().size();
        if (nc.getDocumentoElectronico() != null) {
            nc.getDocumentoElectronico().getId();
        }
        
        return nc;
    }

    @Transactional(readOnly = true)
    public Page<NotaCredito> listarPorEmpresa(Long empresaId, Pageable pageable) {
        empresaSecurityService.verificarAccesoLectura(empresaId);
        return notaCreditoRepository.findByEmpresaIdAndActivoTrueWithItems(empresaId, pageable);
    }

    public void desactivar(Long id) {
        NotaCredito nc = obtenerPorId(id);
        empresaSecurityService.verificarAccesoEscritura(nc.getEmpresa().getId());
        nc.setActivo(false);
        notaCreditoRepository.save(nc);
    }
}

