package com.frcefact.service;

import com.frcefact.dto.ClienteRankingDto;
import com.frcefact.dto.FacturaFiltroDto;
import com.frcefact.dto.FacturaReporteDto;
import com.frcefact.dto.ProductoReporteDto;
import com.frcefact.model.FacturaLegal;
import com.frcefact.model.Producto;
import com.frcefact.repository.FacturaLegalItemRepository;
import com.frcefact.repository.FacturaLegalRepository;
import com.frcefact.repository.ProductoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio para generación de reportes de facturación.
 * 
 * Requirements: 16.1, 16.2, 16.3, 16.4, 16.5
 */
@Service
@Transactional(readOnly = true)
public class ReporteService {

    private final FacturaLegalRepository facturaLegalRepository;
    private final FacturaLegalItemRepository facturaLegalItemRepository;
    private final ProductoRepository productoRepository;

    public ReporteService(FacturaLegalRepository facturaLegalRepository,
                         FacturaLegalItemRepository facturaLegalItemRepository,
                         ProductoRepository productoRepository) {
        this.facturaLegalRepository = facturaLegalRepository;
        this.facturaLegalItemRepository = facturaLegalItemRepository;
        this.productoRepository = productoRepository;
    }

    /**
     * Genera reporte de facturas con filtros dinámicos.
     * 
     * Requirement 16.1: Reporte por empresa con total de ventas y total de IVA por tasa
     * Requirement 16.2: Filtros por fecha, cliente, estado y monto
     * 
     * @param filtro Filtros de búsqueda
     * @return Page de FacturaReporteDto
     */
    public Page<FacturaReporteDto> reporteFacturas(FacturaFiltroDto filtro) {
        // Crear Specification para filtros dinámicos
        Specification<FacturaLegal> spec = crearSpecification(filtro);
        
        // Crear Pageable con ordenamiento
        Sort sort = Sort.by(
                filtro.getSortDirection().equalsIgnoreCase("ASC") ? 
                        Sort.Direction.ASC : Sort.Direction.DESC,
                filtro.getSortBy()
        );
        Pageable pageable = PageRequest.of(filtro.getPage(), filtro.getSize(), sort);
        
        // Buscar facturas
        Page<FacturaLegal> facturas = facturaLegalRepository.findAll(spec, pageable);
        
        // Convertir a DTO
        return facturas.map(this::convertirAFacturaReporteDto);
    }

    /**
     * Genera reporte agrupado por cliente.
     * Excluye facturas con documentos electrónicos cancelados o rechazados para cálculos.
     * 
     * Requirement 16.3: Reporte por cliente con agrupación
     * 
     * @param empresaId ID de la empresa
     * @param fechaDesde Fecha inicial
     * @param fechaHasta Fecha final
     * @return Lista de ClienteRankingDto
     */
    public List<ClienteRankingDto> reportePorCliente(Long empresaId, LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        List<FacturaLegal> facturas = facturaLegalRepository.findByFechaRangeParaCalculos(empresaId, fechaDesde, fechaHasta);
        
        // Agrupar por cliente
        Map<Long, ClienteRankingDto> clientesMap = new HashMap<>();
        
        for (FacturaLegal factura : facturas) {
            if (factura.getCliente() != null) {
                Long clienteId = factura.getCliente().getId();
                ClienteRankingDto ranking = clientesMap.get(clienteId);
                
                if (ranking == null) {
                    ranking = new ClienteRankingDto(
                            clienteId,
                            factura.getCliente().getNombre(),
                            factura.getCliente().getRuc(),
                            0L,
                            BigDecimal.ZERO
                    );
                    clientesMap.put(clienteId, ranking);
                }
                
                ranking.setCantidadFacturas(ranking.getCantidadFacturas() + 1);
                ranking.setMontoTotal(ranking.getMontoTotal().add(factura.getTotalFinal()));
            }
        }
        
        // Ordenar por monto total descendente
        return clientesMap.values().stream()
                .sorted((c1, c2) -> c2.getMontoTotal().compareTo(c1.getMontoTotal()))
                .collect(Collectors.toList());
    }

    /**
     * Genera reporte agrupado por producto.
     * Excluye facturas con documentos electrónicos cancelados o rechazados para cálculos.
     * 
     * Requirement 16.4: Reporte por producto con cantidad y monto
     * 
     * @param empresaId ID de la empresa
     * @param fechaDesde Fecha inicial
     * @param fechaHasta Fecha final
     * @return Lista de ProductoReporteDto
     */
    public List<ProductoReporteDto> reportePorProducto(Long empresaId, LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        // Obtener productos más vendidos (excluyendo canceladas/rechazadas para cálculos)
        List<Object[]> resultados = facturaLegalItemRepository.findProductosMasVendidosParaCalculos(
                empresaId, fechaDesde, fechaHasta
        );
        
        List<ProductoReporteDto> reporte = new ArrayList<>();
        
        for (Object[] resultado : resultados) {
            Long productoId = (Long) resultado[0];
            String descripcion = (String) resultado[1];
            BigDecimal cantidadTotal = (BigDecimal) resultado[2];
            BigDecimal montoTotal = (BigDecimal) resultado[3];
            
            // Obtener información adicional del producto
            Producto producto = productoRepository.findById(productoId).orElse(null);
            
            if (producto != null) {
                ProductoReporteDto dto = new ProductoReporteDto(
                        productoId,
                        producto.getCodigo(),
                        descripcion,
                        producto.getPrecio(),
                        producto.getIva(),
                        cantidadTotal,
                        montoTotal
                );
                reporte.add(dto);
            }
        }
        
        return reporte;
    }

    /**
     * Genera reporte de facturas por usuario creador.
     * 
     * Requirement 16.5: Reporte por usuario listando facturas por creador
     * 
     * @param empresaId ID de la empresa
     * @param fechaDesde Fecha inicial
     * @param fechaHasta Fecha final
     * @return Mapa de usuario -> lista de facturas
     */
    public Map<String, List<FacturaReporteDto>> reportePorUsuario(Long empresaId, LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        List<FacturaLegal> facturas = facturaLegalRepository.findByFechaRange(empresaId, fechaDesde, fechaHasta);
        
        // Agrupar por usuario creador
        Map<String, List<FacturaReporteDto>> reportePorUsuario = new HashMap<>();
        
        for (FacturaLegal factura : facturas) {
            String creadoPor = factura.getCreadoPor() != null ? factura.getCreadoPor() : "Sistema";
            
            reportePorUsuario.computeIfAbsent(creadoPor, k -> new ArrayList<>())
                    .add(convertirAFacturaReporteDto(factura));
        }
        
        return reportePorUsuario;
    }

    /**
     * Crea Specification para filtros dinámicos de facturas.
     */
    private Specification<FacturaLegal> crearSpecification(FacturaFiltroDto filtro) {
        return (root, query, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            
            // Filtro por empresa (requerido)
            predicates.add(criteriaBuilder.equal(root.get("empresa").get("id"), filtro.getEmpresaId()));
            
            // Filtro por activo
            predicates.add(criteriaBuilder.equal(root.get("activo"), true));
            
            // Filtro por cliente (opcional)
            if (filtro.getClienteId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("cliente").get("id"), filtro.getClienteId()));
            }
            
            // Filtro por fecha desde (opcional)
            if (filtro.getFechaDesde() != null && !filtro.getFechaDesde().isEmpty()) {
                LocalDateTime fechaDesde = LocalDateTime.parse(filtro.getFechaDesde(), DateTimeFormatter.ISO_DATE_TIME);
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("fecha"), fechaDesde));
            }
            
            // Filtro por fecha hasta (opcional)
            if (filtro.getFechaHasta() != null && !filtro.getFechaHasta().isEmpty()) {
                LocalDateTime fechaHasta = LocalDateTime.parse(filtro.getFechaHasta(), DateTimeFormatter.ISO_DATE_TIME);
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("fecha"), fechaHasta));
            }
            
            // Filtro por crédito (opcional)
            if (filtro.getCredito() != null) {
                predicates.add(criteriaBuilder.equal(root.get("credito"), filtro.getCredito()));
            }
            
            // NOTA: No excluimos facturas canceladas/rechazadas aquí porque este método
            // se usa para LISTAR facturas, no para cálculos. El usuario debe poder ver todas las facturas.
            // Los cálculos de totales usan métodos específicos que sí excluyen canceladas/rechazadas.
            
            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    /**
     * Convierte una FacturaLegal a FacturaReporteDto.
     */
    private FacturaReporteDto convertirAFacturaReporteDto(FacturaLegal factura) {
        return new FacturaReporteDto(
                factura.getId(),
                factura.getNumeroFactura(),
                factura.getFecha(),
                factura.getNombre(),
                factura.getRuc(),
                factura.getTotalFinal(),
                factura.getIvaParcial10(),
                factura.getIvaParcial5(),
                factura.getIvaParcial0(),
                factura.getCredito(),
                factura.getCreadoPor()
        );
    }
}
