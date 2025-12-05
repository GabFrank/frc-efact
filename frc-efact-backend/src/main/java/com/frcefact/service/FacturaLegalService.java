package com.frcefact.service;

import com.frcefact.annotation.Auditable;
import com.frcefact.model.*;
import com.frcefact.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

        // LOG: Verificar valores antes de procesar items
        logger.info("=== SERVICIO: Antes de procesar items ===");
        logger.info("Moneda: {}, Cambio: {}", factura.getMonedaExtranjera(), factura.getCambio());
        logger.info("Total Parcial (del DTO): {}, Total Final (del DTO): {}", 
                factura.getTotalParcial(), factura.getTotalFinal());
        logger.info("IVA Parcial 5% (del DTO): {}, IVA Parcial 10% (del DTO): {}", 
                factura.getIvaParcial5(), factura.getIvaParcial10());
        
        // Procesar items y asociarlos a la factura
        for (FacturaLegalItem item : factura.getItems()) {
            item.setFacturaLegal(factura);
            
            // LOG: Verificar valores del item antes de procesar
            logger.info("Item antes de procesar: precioUnitario={}, total={}", 
                    item.getPrecioUnitario(), item.getTotal());
            
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
            
            // IMPORTANTE: NO recalcular el total del item si ya viene del DTO
            // El total del item ya debe estar en guaraníes desde el frontend
            // Solo calcular si el total es null (para compatibilidad con código antiguo)
            // PERO: Verificar que el precioUnitario y total estén en guaraníes
            // Si la factura tiene moneda extranjera y los valores parecen estar en moneda extranjera,
            // NO recalcular porque eso sobrescribiría los valores correctos
            if (item.getTotal() == null) {
                logger.warn("⚠️ Item sin total, recalculando desde precioUnitario y cantidad");
                item.calcularTotal();
            } else {
                // CRÍTICO: Verificar si los valores parecen estar en moneda extranjera (valores muy pequeños)
                // Si la factura tiene moneda extranjera y cambio, y el total es muy pequeño,
                // probablemente está en moneda extranjera - CONVERTIR a guaraníes
                boolean pareceMonedaExtranjera = factura.getMonedaExtranjera() != null 
                    && !factura.getMonedaExtranjera().equals("PYG")
                    && factura.getCambio() != null
                    && factura.getCambio().compareTo(BigDecimal.ZERO) > 0
                    && (item.getTotal().compareTo(BigDecimal.valueOf(1000)) < 0 || 
                        item.getPrecioUnitario().compareTo(BigDecimal.valueOf(1000)) < 0);
                
                if (pareceMonedaExtranjera) {
                    logger.error("❌ ERROR CRÍTICO: Item parece tener valores en moneda extranjera (precioUnitario={}, total={}), " +
                            "pero debería estar en guaraníes. Convirtiendo a guaraníes como medida de seguridad.",
                            item.getPrecioUnitario(), item.getTotal());
                    
                    // CONVERTIR a guaraníes como medida de seguridad
                    BigDecimal cambio = factura.getCambio();
                    BigDecimal precioUnitarioGs = item.getPrecioUnitario().multiply(cambio)
                            .setScale(2, java.math.RoundingMode.HALF_UP);
                    BigDecimal totalGs = item.getTotal().multiply(cambio)
                            .setScale(2, java.math.RoundingMode.HALF_UP);
                    
                    logger.info("🔄 Conversión de seguridad: precioUnitario {} {} → {} Gs, total {} {} → {} Gs",
                            item.getPrecioUnitario(), factura.getMonedaExtranjera(), precioUnitarioGs,
                            item.getTotal(), factura.getMonedaExtranjera(), totalGs);
                    
                    item.setPrecioUnitario(precioUnitarioGs);
                    item.setTotal(totalGs);
                } else {
                    logger.info("✅ Item con total del DTO (en guaraníes): precioUnitario={}, total={}", 
                            item.getPrecioUnitario(), item.getTotal());
                }
            }
            
            // LOG: Verificar valores del item después de procesar
            logger.info("Item después de procesar: precioUnitario={}, total={}", 
                    item.getPrecioUnitario(), item.getTotal());
        }

        // IMPORTANTE: Los totales ya vienen calculados en guaraníes desde el frontend
        // NO recalcular si ya están establecidos - esto asegura que siempre se guarden en guaraníes
        if (factura.getTotalParcial() == null || factura.getTotalFinal() == null) {
            logger.warn("⚠️ Totales no establecidos desde DTO, recalculando desde items");
            factura.recalcularTotales();
            logger.info("Totales recalculados: Total Parcial={}, Total Final={}", 
                    factura.getTotalParcial(), factura.getTotalFinal());
        } else {
            logger.info("✅ Usando totales del DTO (en guaraníes): Total Parcial={}, Total Final={}", 
                    factura.getTotalParcial(), factura.getTotalFinal());
        }
        
        logger.info("=== SERVICIO: Antes de guardar ===");
        logger.info("Total Parcial: {}, Total Final: {}", factura.getTotalParcial(), factura.getTotalFinal());
        logger.info("IVA Parcial 5%: {}, IVA Parcial 10%: {}", 
                factura.getIvaParcial5(), factura.getIvaParcial10());
        logger.info("Total Parcial 5%: {}, Total Parcial 10%: {}", 
                factura.getTotalParcial5(), factura.getTotalParcial10());
        logger.info("===================================");

        // Aplicar descuento final si existe
        if (factura.getDescuentoFinal() != null && factura.getDescuentoFinal().compareTo(BigDecimal.ZERO) > 0) {
            // Si los totales ya están establecidos, solo ajustar el total final
            if (factura.getTotalParcial() != null && factura.getTotalFinal() != null) {
                factura.setTotalFinal(factura.getTotalParcial().subtract(factura.getDescuentoFinal()));
            } else {
                factura.aplicarDescuento(factura.getDescuentoFinal());
            }
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
        // Usar método con JOIN FETCH para cargar la relación empresa
        FacturaLegal factura = facturaLegalRepository.findByIdWithEmpresa(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Factura no encontrada con ID: " + id));

        // Verificar permisos (ahora empresa ya está cargada)
        empresaSecurityService.verificarAccesoLectura(factura.getEmpresa().getId());

        inicializarRelacionesFactura(factura);

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

        Page<FacturaLegal> page = facturaLegalRepository.findByEmpresaIdAndActivoTrue(empresaId, pageable);
        page.forEach(this::inicializarRelacionesFactura);
        return page;
    }

    /**
     * Lista facturas con filtros.
     * 
     * @param empresaId ID de la empresa
     * @param clienteId ID del cliente (opcional)
     * @param fechaDesde Fecha desde
     * @param fechaHasta Fecha hasta
     * @param estado Estado del documento electrónico (opcional, puede ser "SIN_DE" para facturas sin DE)
     * @param pageable Configuración de paginación
     * @return Página de facturas filtradas
     */
    @Transactional(readOnly = true)
    public Page<FacturaLegal> listarConFiltros(
            Long empresaId,
            Long clienteId,
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta,
            String estado,
            Pageable pageable) {
        
        // Verificar permisos
        empresaSecurityService.verificarAccesoLectura(empresaId);

        // Construir Specification dinámicamente
        Specification<FacturaLegal> spec = crearSpecificationFiltros(
                empresaId, clienteId, fechaDesde, fechaHasta, estado);
        
        Page<FacturaLegal> page = facturaLegalRepository.findAll(spec, pageable);
        page.forEach(this::inicializarRelacionesFactura);
        return page;
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

        Page<FacturaLegal> page = facturaLegalRepository.findByClienteIdAndActivoTrue(clienteId, pageable);
        page.forEach(this::inicializarRelacionesFactura);
        return page;
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

    private void inicializarRelacionesFactura(FacturaLegal factura) {
        if (factura == null) {
            return;
        }

        factura.getItems().size();
        DocumentoElectronico documento = factura.getDocumentoElectronico();
        if (documento != null) {
            documento.getId();
            LoteDE lote = documento.getLoteDE();
            if (lote != null) {
                lote.getId();
            }
        }
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

    /**
     * Obtiene el resumen de facturas aprobadas y no aprobadas.
     * Solo filtra por fechas si se proporcionan.
     * 
     * @param empresaId ID de la empresa
     * @param fechaDesde Fecha desde (opcional)
     * @param fechaHasta Fecha hasta (opcional)
     * @return ResumenFacturasDto con los totales
     */
    @Transactional(readOnly = true)
    public com.frcefact.dto.ResumenFacturasDto obtenerResumenFacturas(
            Long empresaId,
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta) {
        
        // Verificar permisos
        empresaSecurityService.verificarAccesoLectura(empresaId);

        // Obtener facturas según filtros
        List<FacturaLegal> facturas;
        if (fechaDesde != null && fechaHasta != null) {
            facturas = facturaLegalRepository.findByFechaRange(empresaId, fechaDesde, fechaHasta);
        } else {
            facturas = facturaLegalRepository.findByEmpresaIdAndActivoTrue(empresaId);
        }

        // Inicializar totales
        long cantidadAprobadas = 0;
        BigDecimal totalAprobadas = BigDecimal.ZERO;
        BigDecimal totalIva10Aprobadas = BigDecimal.ZERO;
        BigDecimal totalIva5Aprobadas = BigDecimal.ZERO;
        BigDecimal totalExentasAprobadas = BigDecimal.ZERO;

        long cantidadNoAprobadas = 0;
        BigDecimal totalNoAprobadas = BigDecimal.ZERO;

        // Procesar cada factura
        for (FacturaLegal factura : facturas) {
            // Verificar si está aprobada
            boolean aprobada = false;
            if (factura.getDocumentoElectronico() != null) {
                aprobada = EstadoDE.APROBADO.equals(factura.getDocumentoElectronico().getEstado());
            }

            BigDecimal totalFactura = factura.getTotalFinal() != null ? factura.getTotalFinal() : BigDecimal.ZERO;

            if (aprobada) {
                cantidadAprobadas++;
                totalAprobadas = totalAprobadas.add(totalFactura);
                
                // Sumar por tipo de IVA
                if (factura.getTotalParcial10() != null) {
                    totalIva10Aprobadas = totalIva10Aprobadas.add(factura.getTotalParcial10());
                }
                if (factura.getTotalParcial5() != null) {
                    totalIva5Aprobadas = totalIva5Aprobadas.add(factura.getTotalParcial5());
                }
                if (factura.getTotalParcial0() != null) {
                    totalExentasAprobadas = totalExentasAprobadas.add(factura.getTotalParcial0());
                }
            } else {
                cantidadNoAprobadas++;
                totalNoAprobadas = totalNoAprobadas.add(totalFactura);
            }
        }

        return new com.frcefact.dto.ResumenFacturasDto(
                cantidadAprobadas,
                totalAprobadas,
                totalIva10Aprobadas,
                totalIva5Aprobadas,
                totalExentasAprobadas,
                cantidadNoAprobadas,
                totalNoAprobadas
        );
    }

    /**
     * Crea una Specification dinámica para filtrar facturas.
     * 
     * @param empresaId ID de la empresa
     * @param clienteId ID del cliente (opcional)
     * @param fechaDesde Fecha desde
     * @param fechaHasta Fecha hasta
     * @param estado Estado del documento electrónico (opcional, puede ser "SIN_DE")
     * @return Specification para filtrar facturas
     */
    private Specification<FacturaLegal> crearSpecificationFiltros(
            Long empresaId,
            Long clienteId,
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta,
            String estado) {
        
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Filtro por empresa (requerido)
            predicates.add(criteriaBuilder.equal(root.get("empresa").get("id"), empresaId));
            
            // Filtro por activo
            predicates.add(criteriaBuilder.equal(root.get("activo"), true));
            
            // Filtro por cliente (opcional)
            if (clienteId != null) {
                predicates.add(criteriaBuilder.equal(root.get("cliente").get("id"), clienteId));
            }
            
            // Filtro por fecha desde
            if (fechaDesde != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("fecha"), fechaDesde));
            }
            
            // Filtro por fecha hasta
            if (fechaHasta != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("fecha"), fechaHasta));
            }
            
            // Filtro por estado del documento electrónico (opcional)
            if (estado != null && !estado.isEmpty()) {
                jakarta.persistence.criteria.Join<FacturaLegal, DocumentoElectronico> deJoin = 
                        root.join("documentoElectronico", JoinType.LEFT);
                
                if ("SIN_DE".equals(estado)) {
                    // Facturas sin documento electrónico
                    predicates.add(criteriaBuilder.isNull(deJoin));
                } else {
                    // Filtrar por estado específico del documento electrónico
                    try {
                        EstadoDE estadoEnum = EstadoDE.valueOf(estado);
                        predicates.add(criteriaBuilder.equal(deJoin.get("estado"), estadoEnum));
                    } catch (IllegalArgumentException e) {
                        // Si el estado no es válido, ignorar el filtro
                    }
                }
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
