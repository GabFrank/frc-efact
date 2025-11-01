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

/**
 * Servicio para gestión de facturas legales.
 * Implementa creación de facturas con gestión automática de numeración,
 * cálculo de totales por tasa de IVA y validación de timbrados.
 */
@Service
@Transactional
public class FacturaLegalService {

    private static final Logger logger = LoggerFactory.getLogger(FacturaLegalService.class);

    private final FacturaLegalRepository facturaLegalRepository;
    private final FacturaLegalItemRepository facturaLegalItemRepository;
    private final EmpresaRepository empresaRepository;
    private final TimbradoDetalleRepository timbradoDetalleRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final TimbradoDetalleService timbradoDetalleService;
    private final EmpresaSecurityService empresaSecurityService;

    public FacturaLegalService(
            FacturaLegalRepository facturaLegalRepository,
            FacturaLegalItemRepository facturaLegalItemRepository,
            EmpresaRepository empresaRepository,
            TimbradoDetalleRepository timbradoDetalleRepository,
            ClienteRepository clienteRepository,
            ProductoRepository productoRepository,
            TimbradoDetalleService timbradoDetalleService,
            EmpresaSecurityService empresaSecurityService) {
        this.facturaLegalRepository = facturaLegalRepository;
        this.facturaLegalItemRepository = facturaLegalItemRepository;
        this.empresaRepository = empresaRepository;
        this.timbradoDetalleRepository = timbradoDetalleRepository;
        this.clienteRepository = clienteRepository;
        this.productoRepository = productoRepository;
        this.timbradoDetalleService = timbradoDetalleService;
        this.empresaSecurityService = empresaSecurityService;
    }

    /**
     * Crea una nueva factura legal.
     * Obtiene y asigna automáticamente el número de factura del timbrado detalle.
     * Valida vigencia del timbrado y calcula totales automáticamente.
     * 
     * @param factura Factura a crear con items
     * @return Factura creada con número asignado
     * @throws IllegalStateException si el timbrado no está vigente o no hay números disponibles
     * @throws IllegalArgumentException si la factura no tiene items
     */
    @Auditable(entidad = "FacturaLegal", accion = AccionEnum.CREATE)
    public FacturaLegal crearFactura(FacturaLegal factura) {
        // Verificar permisos
        empresaSecurityService.verificarAccesoEscritura(factura.getEmpresa().getId());

        // Validar que la empresa existe
        Empresa empresa = empresaRepository.findById(factura.getEmpresa().getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Empresa no encontrada con ID: " + factura.getEmpresa().getId()));
        factura.setEmpresa(empresa);

        // Validar que el timbrado detalle existe
        TimbradoDetalle timbradoDetalle = timbradoDetalleRepository.findById(
                factura.getTimbradoDetalle().getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Timbrado detalle no encontrado con ID: " + factura.getTimbradoDetalle().getId()));
        factura.setTimbradoDetalle(timbradoDetalle);

        // Validar vigencia del timbrado
        if (!timbradoDetalle.getTimbrado().isVigente()) {
            throw new IllegalStateException(
                    "El timbrado no está vigente. Fecha fin: " + 
                    timbradoDetalle.getTimbrado().getFechaFin());
        }

        // Validar que tiene items
        if (factura.getItems() == null || factura.getItems().isEmpty()) {
            throw new IllegalArgumentException("La factura debe tener al menos un item");
        }

        // Obtener y asignar número de factura (incrementa automáticamente)
        Long numeroAsignado = timbradoDetalleService.incrementarNumeroActual(timbradoDetalle.getId());
        factura.setNumeroFactura(numeroAsignado.intValue());

        // Establecer fecha si no está definida
        if (factura.getFecha() == null) {
            factura.setFecha(LocalDateTime.now());
        }

        // Cargar cliente si está especificado y copiar datos
        if (factura.getCliente() != null && factura.getCliente().getId() != null) {
            Cliente cliente = clienteRepository.findById(factura.getCliente().getId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Cliente no encontrado con ID: " + factura.getCliente().getId()));
            factura.setCliente(cliente);
            
            // Copiar datos del cliente como snapshot
            factura.setNombre(cliente.getNombre());
            factura.setRuc(cliente.getRuc());
            factura.setDireccion(cliente.getDireccion());
        }

        // Procesar items y asociarlos a la factura
        for (FacturaLegalItem item : factura.getItems()) {
            item.setFacturaLegal(factura);
            
            // Cargar producto si está especificado
            if (item.getProducto() != null && item.getProducto().getId() != null) {
                Producto producto = productoRepository.findById(item.getProducto().getId())
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Producto no encontrado con ID: " + item.getProducto().getId()));
                item.setProducto(producto);
                
                // Usar descripción del producto si no se especificó
                if (item.getDescripcion() == null || item.getDescripcion().isEmpty()) {
                    item.setDescripcion(producto.getDescripcion());
                }
            }
            
            // Calcular total del item
            item.calcularTotal();
        }

        // Calcular totales de la factura
        factura.recalcularTotales();

        // Aplicar descuento final si existe
        if (factura.getDescuentoFinal() != null && factura.getDescuentoFinal().compareTo(BigDecimal.ZERO) > 0) {
            factura.aplicarDescuento(factura.getDescuentoFinal());
        }

        // Guardar factura (cascade guardará los items)
        FacturaLegal facturaGuardada = facturaLegalRepository.save(factura);

        logger.info("Factura creada: {} - Número: {} - Total: {}",
                facturaGuardada.getId(),
                facturaGuardada.getNumeroFacturaFormateado(),
                facturaGuardada.getTotalFinal());

        return facturaGuardada;
    }

    /**
     * Agrega un item a una factura existente.
     * Recalcula automáticamente los totales de la factura.
     * 
     * @param facturaId ID de la factura
     * @param item Item a agregar
     * @return Factura actualizada
     */
    public FacturaLegal agregarItem(Long facturaId, FacturaLegalItem item) {
        FacturaLegal factura = facturaLegalRepository.findById(facturaId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Factura no encontrada con ID: " + facturaId));

        // Verificar permisos
        empresaSecurityService.verificarAccesoEscritura(factura.getEmpresa().getId());

        // Cargar producto si está especificado
        if (item.getProducto() != null && item.getProducto().getId() != null) {
            Producto producto = productoRepository.findById(item.getProducto().getId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Producto no encontrado con ID: " + item.getProducto().getId()));
            item.setProducto(producto);
            
            // Usar descripción del producto si no se especificó
            if (item.getDescripcion() == null || item.getDescripcion().isEmpty()) {
                item.setDescripcion(producto.getDescripcion());
            }
        }

        // Calcular total del item
        item.calcularTotal();

        // Agregar item a la factura (esto recalcula totales automáticamente)
        factura.agregarItem(item);

        // Guardar cambios
        return facturaLegalRepository.save(factura);
    }

    /**
     * Elimina un item de una factura.
     * Recalcula automáticamente los totales de la factura.
     * 
     * @param facturaId ID de la factura
     * @param itemId ID del item a eliminar
     * @return Factura actualizada
     */
    public FacturaLegal eliminarItem(Long facturaId, Long itemId) {
        FacturaLegal factura = facturaLegalRepository.findById(facturaId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Factura no encontrada con ID: " + facturaId));

        // Verificar permisos
        empresaSecurityService.verificarAccesoEscritura(factura.getEmpresa().getId());

        FacturaLegalItem item = facturaLegalItemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Item no encontrado con ID: " + itemId));

        // Verificar que el item pertenece a la factura
        if (!item.getFacturaLegal().getId().equals(facturaId)) {
            throw new IllegalArgumentException(
                    "El item no pertenece a la factura especificada");
        }

        // Eliminar item (esto recalcula totales automáticamente)
        factura.eliminarItem(item);

        // Validar que la factura siga teniendo items
        if (factura.getItems().isEmpty()) {
            throw new IllegalStateException(
                    "No se puede eliminar el último item de la factura");
        }

        // Guardar cambios
        return facturaLegalRepository.save(factura);
    }

    /**
     * Recalcula los totales de una factura.
     * Útil cuando se modifican items externamente.
     * 
     * @param facturaId ID de la factura
     * @return Factura con totales recalculados
     */
    public FacturaLegal recalcularTotalesFactura(Long facturaId) {
        FacturaLegal factura = facturaLegalRepository.findById(facturaId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Factura no encontrada con ID: " + facturaId));

        // Verificar permisos
        empresaSecurityService.verificarAccesoEscritura(factura.getEmpresa().getId());

        // Recalcular totales
        factura.recalcularTotales();

        // Guardar cambios
        return facturaLegalRepository.save(factura);
    }

    /**
     * Aplica un descuento final a la factura y recalcula el total final.
     * 
     * @param facturaId ID de la factura
     * @param descuento Monto del descuento
     * @return Factura actualizada
     */
    public FacturaLegal aplicarDescuento(Long facturaId, BigDecimal descuento) {
        FacturaLegal factura = facturaLegalRepository.findById(facturaId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Factura no encontrada con ID: " + facturaId));

        // Verificar permisos
        empresaSecurityService.verificarAccesoEscritura(factura.getEmpresa().getId());

        // Validar descuento
        if (descuento != null && descuento.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El descuento no puede ser negativo");
        }

        if (descuento != null && descuento.compareTo(factura.getTotalParcial()) > 0) {
            throw new IllegalArgumentException(
                    "El descuento no puede ser mayor al total parcial de la factura");
        }

        // Aplicar descuento
        factura.aplicarDescuento(descuento);

        // Guardar cambios
        return facturaLegalRepository.save(factura);
    }

    /**
     * Actualiza una factura existente.
     * 
     * @param id ID de la factura
     * @param facturaActualizada Datos actualizados
     * @return Factura actualizada
     */
    public FacturaLegal actualizar(Long id, FacturaLegal facturaActualizada) {
        FacturaLegal facturaExistente = facturaLegalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Factura no encontrada con ID: " + id));

        // Verificar permisos
        empresaSecurityService.verificarAccesoEscritura(facturaExistente.getEmpresa().getId());

        // Actualizar campos permitidos
        facturaExistente.setCredito(facturaActualizada.getCredito());
        facturaExistente.setNombre(facturaActualizada.getNombre());
        facturaExistente.setRuc(facturaActualizada.getRuc());
        facturaExistente.setDireccion(facturaActualizada.getDireccion());

        // Si se actualizó el cliente, cargar sus datos
        if (facturaActualizada.getCliente() != null && 
            facturaActualizada.getCliente().getId() != null) {
            Cliente cliente = clienteRepository.findById(facturaActualizada.getCliente().getId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Cliente no encontrado con ID: " + facturaActualizada.getCliente().getId()));
            facturaExistente.setCliente(cliente);
        }

        // Aplicar descuento si cambió
        if (facturaActualizada.getDescuentoFinal() != null &&
            !facturaActualizada.getDescuentoFinal().equals(facturaExistente.getDescuentoFinal())) {
            facturaExistente.aplicarDescuento(facturaActualizada.getDescuentoFinal());
        }

        return facturaLegalRepository.save(facturaExistente);
    }

    /**
     * Obtiene una factura por ID con sus items.
     * 
     * @param id ID de la factura
     * @return Factura con items cargados
     */
    @Transactional(readOnly = true)
    public FacturaLegal obtenerPorId(Long id) {
        FacturaLegal factura = facturaLegalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Factura no encontrada con ID: " + id));

        // Verificar permisos
        empresaSecurityService.verificarAccesoLectura(factura.getEmpresa().getId());

        // Forzar carga de items
        factura.getItems().size();

        return factura;
    }

    /**
     * Lista facturas de una empresa con paginación.
     * 
     * @param empresaId ID de la empresa
     * @param pageable Configuración de paginación
     * @return Página de facturas
     */
    @Transactional(readOnly = true)
    public Page<FacturaLegal> listarPorEmpresa(Long empresaId, Pageable pageable) {
        // Verificar permisos
        empresaSecurityService.verificarAccesoLectura(empresaId);

        return facturaLegalRepository.findByEmpresaIdAndActivoTrue(empresaId, pageable);
    }

    /**
     * Lista facturas con filtros.
     * 
     * @param empresaId ID de la empresa
     * @param clienteId ID del cliente (opcional)
     * @param fechaDesde Fecha desde
     * @param fechaHasta Fecha hasta
     * @param pageable Configuración de paginación
     * @return Página de facturas filtradas
     */
    @Transactional(readOnly = true)
    public Page<FacturaLegal> listarConFiltros(
            Long empresaId,
            Long clienteId,
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta,
            Pageable pageable) {
        
        // Verificar permisos
        empresaSecurityService.verificarAccesoLectura(empresaId);

        // Si no se especifican fechas, usar rango del mes actual
        if (fechaDesde == null) {
            fechaDesde = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        }
        if (fechaHasta == null) {
            fechaHasta = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        }

        return facturaLegalRepository.findByFiltros(
                empresaId, clienteId, fechaDesde, fechaHasta, pageable);
    }

    /**
     * Lista facturas de un cliente.
     * 
     * @param clienteId ID del cliente
     * @param pageable Configuración de paginación
     * @return Página de facturas del cliente
     */
    @Transactional(readOnly = true)
    public Page<FacturaLegal> listarPorCliente(Long clienteId, Pageable pageable) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Cliente no encontrado con ID: " + clienteId));

        // Verificar permisos
        empresaSecurityService.verificarAccesoLectura(cliente.getEmpresa().getId());

        return facturaLegalRepository.findByClienteIdAndActivoTrue(clienteId, pageable);
    }

    /**
     * Desactiva una factura (soft delete).
     * 
     * @param id ID de la factura
     */
    public void desactivar(Long id) {
        FacturaLegal factura = facturaLegalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Factura no encontrada con ID: " + id));

        // Verificar permisos
        empresaSecurityService.verificarAccesoEscritura(factura.getEmpresa().getId());

        factura.setActivo(false);
        facturaLegalRepository.save(factura);

        logger.info("Factura desactivada: {} - Número: {}",
                factura.getId(), factura.getNumeroFacturaFormateado());
    }

    /**
     * Obtiene el total facturado por empresa en un rango de fechas.
     * 
     * @param empresaId ID de la empresa
     * @param fechaDesde Fecha desde
     * @param fechaHasta Fecha hasta
     * @return Total facturado
     */
    @Transactional(readOnly = true)
    public BigDecimal obtenerTotalFacturado(
            Long empresaId,
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta) {
        
        // Verificar permisos
        empresaSecurityService.verificarAccesoLectura(empresaId);

        return facturaLegalRepository.sumTotalByFechaRange(empresaId, fechaDesde, fechaHasta);
    }

    /**
     * Cuenta facturas de una empresa en un rango de fechas.
     * 
     * @param empresaId ID de la empresa
     * @param fechaDesde Fecha desde
     * @param fechaHasta Fecha hasta
     * @return Cantidad de facturas
     */
    @Transactional(readOnly = true)
    public long contarFacturas(
            Long empresaId,
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta) {
        
        // Verificar permisos
        empresaSecurityService.verificarAccesoLectura(empresaId);

        return facturaLegalRepository.countByFechaRange(empresaId, fechaDesde, fechaHasta);
    }
}
