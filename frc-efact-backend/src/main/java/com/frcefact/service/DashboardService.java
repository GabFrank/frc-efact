package com.frcefact.service;

import com.frcefact.dto.ActividadRecienteDto;
import com.frcefact.dto.ClienteRankingDto;
import com.frcefact.dto.DashboardEmpresaDto;
import com.frcefact.dto.DashboardUsuarioDto;
import com.frcefact.dto.FacturasAprobadasDto;
import com.frcefact.dto.FacturasCanceladasDto;
import com.frcefact.dto.ProductoReporteDto;
import com.frcefact.dto.TotalesPorIvaDto;
import com.frcefact.model.EstadoDE;
import com.frcefact.model.AuditLog;
import com.frcefact.model.Usuario;
import com.frcefact.repository.AuditLogRepository;
import com.frcefact.repository.FacturaLegalRepository;
import com.frcefact.repository.UsuarioEmpresaRepository;
import com.frcefact.repository.UsuarioRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar dashboards y métricas del sistema.
 */
@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioEmpresaRepository usuarioEmpresaRepository;
    private final AuditLogRepository auditLogRepository;
    private final FacturaLegalRepository facturaLegalRepository;
    private final com.frcefact.repository.EmpresaRepository empresaRepository;
    private final com.frcefact.repository.DocumentoElectronicoRepository documentoElectronicoRepository;
    private final com.frcefact.repository.FacturaLegalItemRepository facturaLegalItemRepository;
    private final com.frcefact.repository.ProductoRepository productoRepository;

    public DashboardService(UsuarioRepository usuarioRepository,
                           UsuarioEmpresaRepository usuarioEmpresaRepository,
                           AuditLogRepository auditLogRepository,
                           FacturaLegalRepository facturaLegalRepository,
                           com.frcefact.repository.EmpresaRepository empresaRepository,
                           com.frcefact.repository.DocumentoElectronicoRepository documentoElectronicoRepository,
                           com.frcefact.repository.FacturaLegalItemRepository facturaLegalItemRepository,
                           com.frcefact.repository.ProductoRepository productoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioEmpresaRepository = usuarioEmpresaRepository;
        this.auditLogRepository = auditLogRepository;
        this.facturaLegalRepository = facturaLegalRepository;
        this.empresaRepository = empresaRepository;
        this.documentoElectronicoRepository = documentoElectronicoRepository;
        this.facturaLegalItemRepository = facturaLegalItemRepository;
        this.productoRepository = productoRepository;
    }

    /**
     * Obtiene el dashboard del usuario con métricas personales.
     * 
     * Requirements: 14.1, 14.2, 14.3, 14.4
     * 
     * @param usuarioId ID del usuario
     * @return DashboardUsuarioDto con métricas del usuario
     */
    public DashboardUsuarioDto getDashboardUsuario(Long usuarioId) {
        // Buscar usuario
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Calcular cantidad de empresas con acceso (Requirement 14.1)
        int cantidadEmpresas = usuarioEmpresaRepository.findByUsuarioIdAndActivoTrue(usuarioId).size();

        // Obtener último acceso (Requirement 14.2)
        LocalDateTime ultimoAcceso = usuario.getUltimoLogin();

        // Obtener últimas 10 actividades (Requirement 14.3)
        List<AuditLog> auditLogs = auditLogRepository.findUltimasActividadesUsuario(
                usuarioId, 
                PageRequest.of(0, 10)
        );
        
        List<ActividadRecienteDto> ultimasActividades = auditLogs.stream()
                .map(this::convertirAActividadReciente)
                .collect(Collectors.toList());

        // Contar facturas creadas en el mes actual (Requirement 14.4)
        LocalDateTime inicioMes = YearMonth.now().atDay(1).atStartOfDay();
        LocalDateTime finMes = YearMonth.now().atEndOfMonth().atTime(23, 59, 59);
        
        long facturasCreadasMesActual = contarFacturasCreadasPorUsuario(usuarioId, inicioMes, finMes);

        return new DashboardUsuarioDto(
                usuarioId,
                usuario.getUsername(),
                cantidadEmpresas,
                ultimoAcceso,
                facturasCreadasMesActual,
                ultimasActividades
        );
    }

    /**
     * Convierte un AuditLog a ActividadRecienteDto.
     */
    private ActividadRecienteDto convertirAActividadReciente(AuditLog auditLog) {
        String descripcion = generarDescripcionActividad(auditLog);
        String empresaNombre = auditLog.getEmpresa() != null ? 
                auditLog.getEmpresa().getRazonSocial() : null;

        return new ActividadRecienteDto(
                auditLog.getId(),
                auditLog.getAccion(),
                auditLog.getEntidadTipo(),
                auditLog.getEntidadId(),
                descripcion,
                auditLog.getFechaHora(),
                empresaNombre
        );
    }

    /**
     * Genera una descripción legible de la actividad.
     */
    private String generarDescripcionActividad(AuditLog auditLog) {
        String accion = switch (auditLog.getAccion()) {
            case CREATE -> "creó";
            case UPDATE -> "actualizó";
            case DELETE -> "eliminó";
            case READ -> "consultó";
        };

        String entidad = auditLog.getEntidadTipo();
        
        return String.format("%s %s", accion, entidad);
    }

    /**
     * Cuenta las facturas creadas por un usuario en un rango de fechas.
     */
    private long contarFacturasCreadasPorUsuario(Long usuarioId, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        if (usuario == null) {
            return 0;
        }

        // Por ahora retornamos 0 para evitar el problema del enum
        // TODO: Implementar una consulta directa a la tabla de facturas
        return 0;
    }

    /**
     * Obtiene métricas generales del sistema.
     * 
     * @return DashboardGeneralDto con métricas generales
     */
    public com.frcefact.dto.DashboardGeneralDto getDashboardGeneral() {
        // Contar total de empresas activas
        long totalEmpresas = empresaRepository.count();

        // Contar total de usuarios activos
        long totalUsuarios = usuarioRepository.count();

        // Contar total de documentos (facturas)
        long totalDocumentos = facturaLegalRepository.count();

        // Contar actividad de hoy
        LocalDateTime inicioHoy = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime finHoy = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        long actividadHoy = auditLogRepository.countByFechaHoraBetween(inicioHoy, finHoy);

        return new com.frcefact.dto.DashboardGeneralDto(
                totalEmpresas,
                totalUsuarios,
                totalDocumentos,
                actividadHoy
        );
    }

    /**
     * Obtiene el dashboard de empresa con métricas de facturación.
     * 
     * Requirements: 15.1, 15.2, 15.3, 15.4
     * 
     * @param empresaId ID de la empresa
     * @param fechaDesde Fecha inicial del período (opcional, por defecto inicio del mes)
     * @param fechaHasta Fecha final del período (opcional, por defecto fin del mes)
     * @return DashboardEmpresaDto con métricas de la empresa
     */
    public DashboardEmpresaDto getDashboardEmpresa(Long empresaId, LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        // Buscar empresa
        com.frcefact.model.Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        // Si no se especifican fechas, usar el mes actual
        if (fechaDesde == null) {
            fechaDesde = YearMonth.now().atDay(1).atStartOfDay();
        }
        if (fechaHasta == null) {
            fechaHasta = YearMonth.now().atEndOfMonth().atTime(23, 59, 59);
        }

        // Calcular total de facturas emitidas (Requirement 15.1)
        long totalFacturasEmitidas = facturaLegalRepository.countByEmpresaIdAndActivoTrue(empresaId);

        // Sumar total en guaraníes del mes actual (Requirement 15.2)
        BigDecimal totalGuaraniesMesActual = facturaLegalRepository.sumTotalByFechaRange(
                empresaId, fechaDesde, fechaHasta
        );

        // Calcular totales por tasa de IVA (Requirement 15.3)
        TotalesPorIvaDto totalesPorIva = calcularTotalesPorIva(empresaId, fechaDesde, fechaHasta);

        // Generar ranking de top 10 clientes (Requirement 15.4)
        List<ClienteRankingDto> top10Clientes = obtenerTop10Clientes(empresaId, fechaDesde, fechaHasta);

        // Generar ranking de top 10 productos
        List<ProductoReporteDto> top10Productos = obtenerTop10Productos(empresaId, fechaDesde, fechaHasta);

        // Calcular estadísticas de facturas aprobadas
        FacturasAprobadasDto facturasAprobadas = obtenerFacturasAprobadas(empresaId, fechaDesde, fechaHasta);

        // Calcular estadísticas de facturas canceladas
        FacturasCanceladasDto facturasCanceladas = obtenerFacturasCanceladas(empresaId, fechaDesde, fechaHasta);

        return new DashboardEmpresaDto(
                empresaId,
                empresa.getRazonSocial(),
                totalFacturasEmitidas,
                totalGuaraniesMesActual,
                totalesPorIva,
                top10Clientes,
                top10Productos,
                facturasAprobadas,
                facturasCanceladas
        );
    }

    /**
     * Calcula los totales por tasa de IVA para un período.
     * Excluye facturas con documentos electrónicos cancelados o rechazados.
     */
    private TotalesPorIvaDto calcularTotalesPorIva(Long empresaId, LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        List<com.frcefact.model.FacturaLegal> facturas = facturaLegalRepository.findByFechaRangeParaCalculos(
                empresaId, fechaDesde, fechaHasta
        );

        BigDecimal totalIva10 = facturas.stream()
                .map(com.frcefact.model.FacturaLegal::getTotalParcial10)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalIva5 = facturas.stream()
                .map(com.frcefact.model.FacturaLegal::getTotalParcial5)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalIva0 = facturas.stream()
                .map(com.frcefact.model.FacturaLegal::getTotalParcial0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new TotalesPorIvaDto(totalIva10, totalIva5, totalIva0);
    }

    /**
     * Obtiene el ranking de los 10 clientes con mayor monto facturado.
     * Excluye facturas con documentos electrónicos cancelados o rechazados.
     */
    private List<ClienteRankingDto> obtenerTop10Clientes(Long empresaId, LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        List<com.frcefact.model.FacturaLegal> facturas = facturaLegalRepository.findByFechaRangeParaCalculos(
                empresaId, fechaDesde, fechaHasta
        );

        // Agrupar por cliente y sumar montos
        java.util.Map<Long, ClienteRankingDto> clientesMap = new java.util.HashMap<>();
        
        for (com.frcefact.model.FacturaLegal factura : facturas) {
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

        // Ordenar por monto total descendente y tomar top 10
        return clientesMap.values().stream()
                .sorted((c1, c2) -> c2.getMontoTotal().compareTo(c1.getMontoTotal()))
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene estadísticas de facturas aprobadas para un período.
     */
    private FacturasAprobadasDto obtenerFacturasAprobadas(Long empresaId, LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        // Obtener documentos electrónicos aprobados de la empresa en el rango de fechas
        List<com.frcefact.model.DocumentoElectronico> documentosAprobados = 
                documentoElectronicoRepository.findByEmpresaAndEstado(empresaId, EstadoDE.APROBADO);

        // Filtrar por rango de fechas
        List<com.frcefact.model.FacturaLegal> facturasAprobadas = documentosAprobados.stream()
                .map(com.frcefact.model.DocumentoElectronico::getFacturaLegal)
                .filter(factura -> factura != null 
                        && factura.getActivo() 
                        && !factura.getFecha().isBefore(fechaDesde) 
                        && !factura.getFecha().isAfter(fechaHasta))
                .collect(Collectors.toList());

        long cantidad = facturasAprobadas.size();
        BigDecimal totalGs = facturasAprobadas.stream()
                .map(com.frcefact.model.FacturaLegal::getTotalFinal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalIva10 = facturasAprobadas.stream()
                .map(com.frcefact.model.FacturaLegal::getTotalParcial10)
                .filter(total -> total != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalIva5 = facturasAprobadas.stream()
                .map(com.frcefact.model.FacturaLegal::getTotalParcial5)
                .filter(total -> total != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalExentas = facturasAprobadas.stream()
                .map(com.frcefact.model.FacturaLegal::getTotalParcial0)
                .filter(total -> total != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new FacturasAprobadasDto(cantidad, totalGs, totalIva10, totalIva5, totalExentas);
    }

    /**
     * Obtiene estadísticas de facturas canceladas para un período.
     */
    private FacturasCanceladasDto obtenerFacturasCanceladas(Long empresaId, LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        // Obtener documentos electrónicos cancelados de la empresa en el rango de fechas
        List<com.frcefact.model.DocumentoElectronico> documentosCancelados = 
                documentoElectronicoRepository.findByEmpresaAndEstado(empresaId, EstadoDE.CANCELADO);

        // Filtrar por rango de fechas
        List<com.frcefact.model.FacturaLegal> facturasCanceladas = documentosCancelados.stream()
                .map(com.frcefact.model.DocumentoElectronico::getFacturaLegal)
                .filter(factura -> factura != null 
                        && factura.getActivo() 
                        && !factura.getFecha().isBefore(fechaDesde) 
                        && !factura.getFecha().isAfter(fechaHasta))
                .collect(Collectors.toList());

        long cantidad = facturasCanceladas.size();
        BigDecimal totalGs = facturasCanceladas.stream()
                .map(com.frcefact.model.FacturaLegal::getTotalFinal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new FacturasCanceladasDto(cantidad, totalGs);
    }

    /**
     * Obtiene el ranking de los 10 productos con mayor monto facturado.
     * Excluye facturas con documentos electrónicos cancelados o rechazados.
     */
    private List<ProductoReporteDto> obtenerTop10Productos(Long empresaId, LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        // Obtener productos más vendidos (excluyendo canceladas/rechazadas para cálculos)
        List<Object[]> resultados = facturaLegalItemRepository.findProductosMasVendidosParaCalculos(
                empresaId, fechaDesde, fechaHasta
        );
        
        List<ProductoReporteDto> productos = new java.util.ArrayList<>();
        
        for (Object[] resultado : resultados) {
            Long productoId = (Long) resultado[0];
            String descripcion = (String) resultado[1];
            BigDecimal cantidadTotal = (BigDecimal) resultado[2];
            BigDecimal montoTotal = (BigDecimal) resultado[3];
            
            // Obtener información adicional del producto
            com.frcefact.model.Producto producto = productoRepository.findById(productoId).orElse(null);
            
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
                productos.add(dto);
            }
            
            // Limitar a top 10
            if (productos.size() >= 10) {
                break;
            }
        }
        
        return productos;
    }
}
