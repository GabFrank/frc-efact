package com.frcefact.service;

import com.frcefact.annotation.Auditable;
import com.frcefact.model.AccionEnum;
import com.frcefact.model.Empresa;
import com.frcefact.model.Producto;
import com.frcefact.model.TipoTransaccionProducto;
import com.frcefact.repository.EmpresaRepository;
import com.frcefact.repository.ProductoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para gestión de productos.
 * Implementa CRUD completo, validaciones de IVA, búsqueda con filtros e importación masiva desde Excel.
 */
@Service
@Transactional
public class ProductoService {

    private static final Logger logger = LoggerFactory.getLogger(ProductoService.class);
    private static final List<Integer> TASAS_IVA_VALIDAS = List.of(0, 5, 10);

    private final ProductoRepository productoRepository;
    private final EmpresaRepository empresaRepository;
    private final EmpresaSecurityService empresaSecurityService;

    public ProductoService(ProductoRepository productoRepository,
                          EmpresaRepository empresaRepository,
                          EmpresaSecurityService empresaSecurityService) {
        this.productoRepository = productoRepository;
        this.empresaRepository = empresaRepository;
        this.empresaSecurityService = empresaSecurityService;
    }

    /**
     * Crea un nuevo producto para una empresa.
     * Valida que el usuario tenga acceso de escritura a la empresa.
     */
    @Auditable(entidad = "Producto", accion = AccionEnum.CREATE)
    public Producto crearProducto(Long empresaId, Producto producto) {
        logger.info("Creando producto para empresa ID: {}", empresaId);
        
        // Verificar acceso
        empresaSecurityService.verificarAccesoEscritura(empresaId);
        
        // Validar IVA
        validarTasaIva(producto.getIva());
        
        // Obtener empresa
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada con ID: " + empresaId));
        
        // Verificar código único si se proporciona
        if (producto.getCodigo() != null && !producto.getCodigo().trim().isEmpty()) {
            if (productoRepository.existsByEmpresaIdAndCodigoAndActivoTrue(empresaId, producto.getCodigo())) {
                throw new IllegalArgumentException("Ya existe un producto con el código: " + producto.getCodigo());
            }
        }
        
        producto.setEmpresa(empresa);
        producto.setActivo(true);
        
        Producto productoGuardado = productoRepository.save(producto);
        logger.info("Producto creado con ID: {}", productoGuardado.getId());
        
        return productoGuardado;
    }

    /**
     * Actualiza un producto existente.
     */
    @Auditable(entidad = "Producto", accion = AccionEnum.UPDATE)
    public Producto actualizarProducto(Long empresaId, Long productoId, Producto productoActualizado) {
        logger.info("Actualizando producto ID: {} de empresa ID: {}", productoId, empresaId);
        
        // Verificar acceso
        empresaSecurityService.verificarAccesoEscritura(empresaId);
        
        // Validar IVA
        validarTasaIva(productoActualizado.getIva());
        
        // Buscar producto existente
        Producto productoExistente = productoRepository.findById(productoId)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con ID: " + productoId));
        
        // Verificar que pertenece a la empresa
        if (!productoExistente.getEmpresa().getId().equals(empresaId)) {
            throw new IllegalArgumentException("El producto no pertenece a la empresa especificada");
        }
        
        // Verificar código único si cambió
        if (productoActualizado.getCodigo() != null && 
            !productoActualizado.getCodigo().equals(productoExistente.getCodigo())) {
            if (productoRepository.existsByEmpresaIdAndCodigoAndActivoTrue(empresaId, productoActualizado.getCodigo())) {
                throw new IllegalArgumentException("Ya existe un producto con el código: " + productoActualizado.getCodigo());
            }
        }
        
        // Actualizar campos
        productoExistente.setCodigo(productoActualizado.getCodigo());
        productoExistente.setDescripcion(productoActualizado.getDescripcion());
        productoExistente.setPrecio(productoActualizado.getPrecio());
        productoExistente.setIva(productoActualizado.getIva());
        productoExistente.setBalanza(productoActualizado.getBalanza());
        if (productoActualizado.getTipoTransaccion() != null) {
            productoExistente.setTipoTransaccion(productoActualizado.getTipoTransaccion());
        }
        if (productoActualizado.getUnidadMedida() != null) {
            productoExistente.setUnidadMedida(productoActualizado.getUnidadMedida());
        }
        
        Producto productoGuardado = productoRepository.save(productoExistente);
        logger.info("Producto actualizado con ID: {}", productoGuardado.getId());
        
        return productoGuardado;
    }

    /**
     * Obtiene un producto por ID.
     */
    @Transactional(readOnly = true)
    public Producto obtenerProductoPorId(Long empresaId, Long productoId) {
        logger.debug("Obteniendo producto ID: {} de empresa ID: {}", productoId, empresaId);
        
        // Verificar acceso
        empresaSecurityService.verificarAccesoLectura(empresaId);
        
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con ID: " + productoId));
        
        // Verificar que pertenece a la empresa
        if (!producto.getEmpresa().getId().equals(empresaId)) {
            throw new IllegalArgumentException("El producto no pertenece a la empresa especificada");
        }
        
        return producto;
    }

    /**
     * Lista todos los productos activos de una empresa con paginación.
     */
    @Transactional(readOnly = true)
    public Page<Producto> listarProductos(Long empresaId, Pageable pageable) {
        logger.debug("Listando productos de empresa ID: {}", empresaId);
        
        // Verificar acceso
        empresaSecurityService.verificarAccesoLectura(empresaId);
        
        return productoRepository.findByEmpresaIdAndActivoTrue(empresaId, pageable);
    }

    /**
     * Lista productos con filtros opcionales.
     */
    @Transactional(readOnly = true)
    public Page<Producto> listarProductosConFiltros(Long empresaId, 
                                                     Boolean activo,
                                                     String busqueda,
                                                     String tipoTransaccion,
                                                     Integer iva,
                                                     Pageable pageable) {
        logger.debug("Listando productos con filtros - empresa: {}, activo: {}, busqueda: {}, tipoTransaccion: {}, iva: {}", 
                     empresaId, activo, busqueda, tipoTransaccion, iva);
        
        // Verificar acceso
        empresaSecurityService.verificarAccesoLectura(empresaId);
        
        // Normalizar búsqueda
        String busquedaNormalizada = (busqueda != null && !busqueda.trim().isEmpty()) 
            ? busqueda.trim().toUpperCase() 
            : null;
        
        // Validar y convertir tipoTransaccion si está presente
        TipoTransaccionProducto tipoTransaccionEnum = null;
        if (tipoTransaccion != null && !tipoTransaccion.trim().isEmpty()) {
            try {
                tipoTransaccionEnum = TipoTransaccionProducto.valueOf(tipoTransaccion.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                logger.warn("Tipo de transacción inválido: {}", tipoTransaccion);
                // Si el tipo es inválido, no aplicar el filtro (null)
            }
        }
        
        // Usar String para la query (la query manejará la comparación)
        String tipoTransaccionStr = (tipoTransaccionEnum != null) ? tipoTransaccionEnum.name() : null;
        
        return productoRepository.buscarConFiltros(empresaId, activo, busquedaNormalizada, 
                                                   tipoTransaccionStr, iva, pageable);
    }

    /**
     * Busca productos por descripción con filtros.
     */
    @Transactional(readOnly = true)
    public Page<Producto> buscarProductos(Long empresaId, String busqueda, Pageable pageable) {
        logger.debug("Buscando productos en empresa ID: {} con término: {}", empresaId, busqueda);
        
        // Verificar acceso
        empresaSecurityService.verificarAccesoLectura(empresaId);
        
        if (busqueda == null || busqueda.trim().isEmpty()) {
            return productoRepository.findByEmpresaIdAndActivoTrue(empresaId, pageable);
        }
        
        return productoRepository.buscarProductos(empresaId, busqueda.trim(), pageable);
    }

    /**
     * Busca productos por descripción sin paginación.
     */
    @Transactional(readOnly = true)
    public List<Producto> buscarProductosPorDescripcion(Long empresaId, String descripcion) {
        logger.debug("Buscando productos por descripción en empresa ID: {}", empresaId);
        
        // Verificar acceso
        empresaSecurityService.verificarAccesoLectura(empresaId);
        
        if (descripcion == null || descripcion.trim().isEmpty()) {
            return productoRepository.findByEmpresaIdAndActivoTrue(empresaId);
        }
        
        return productoRepository.buscarPorDescripcion(empresaId, descripcion.trim());
    }

    /**
     * Desactiva un producto (soft delete).
     */
    public void desactivarProducto(Long empresaId, Long productoId) {
        logger.info("Desactivando producto ID: {} de empresa ID: {}", productoId, empresaId);
        
        // Verificar acceso
        empresaSecurityService.verificarAccesoEscritura(empresaId);
        
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con ID: " + productoId));
        
        // Verificar que pertenece a la empresa
        if (!producto.getEmpresa().getId().equals(empresaId)) {
            throw new IllegalArgumentException("El producto no pertenece a la empresa especificada");
        }
        
        producto.setActivo(false);
        productoRepository.save(producto);
        
        logger.info("Producto desactivado con ID: {}", productoId);
    }

    /**
     * Importa productos masivamente desde un archivo Excel.
     * Formato esperado: Código | Descripción | Precio | IVA | Tipo Transacción | Unidad Medida | Balanza
     * Columnas opcionales: Código, Tipo Transacción (default: VENTA_MERCADERIA), Unidad Medida (default: UNI), Balanza (default: false)
     */
    public List<Producto> importarProductosDesdeExcel(Long empresaId, MultipartFile archivo) throws IOException {
        logger.info("Importando productos desde Excel para empresa ID: {}", empresaId);
        
        // Verificar acceso
        empresaSecurityService.verificarAccesoEscritura(empresaId);
        
        // Obtener empresa
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada con ID: " + empresaId));
        
        List<Producto> productosImportados = new ArrayList<>();
        List<String> errores = new ArrayList<>();
        
        try (Workbook workbook = new XSSFWorkbook(archivo.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // Saltar la primera fila (encabezados)
            int filaInicio = 1;
            int filasProcesadas = 0;
            
            for (int i = filaInicio; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                try {
                    Producto producto = procesarFilaExcel(row, empresa);
                    if (producto != null) {
                        // Validar IVA
                        validarTasaIva(producto.getIva());
                        
                        // Verificar código único si existe
                        if (producto.getCodigo() != null && !producto.getCodigo().trim().isEmpty()) {
                            if (productoRepository.existsByEmpresaIdAndCodigoAndActivoTrue(empresaId, producto.getCodigo())) {
                                errores.add("Fila " + (i + 1) + ": Código duplicado - " + producto.getCodigo());
                                continue;
                            }
                        }
                        
                        Producto productoGuardado = productoRepository.save(producto);
                        productosImportados.add(productoGuardado);
                        filasProcesadas++;
                    }
                } catch (Exception e) {
                    errores.add("Fila " + (i + 1) + ": " + e.getMessage());
                    logger.warn("Error procesando fila {}: {}", i + 1, e.getMessage());
                }
            }
            
            logger.info("Importación completada. Productos importados: {}, Errores: {}", 
                       filasProcesadas, errores.size());
            
            if (!errores.isEmpty()) {
                logger.warn("Errores durante la importación: {}", String.join("; ", errores));
            }
        }
        
        return productosImportados;
    }

    /**
     * Procesa una fila del Excel y crea un objeto Producto.
     * Formato esperado: Código | Descripción | Precio | IVA | Tipo Transacción | Unidad Medida | Balanza
     * Columnas opcionales: Código, Tipo Transacción (default: VENTA_MERCADERIA), Unidad Medida (default: UNI), Balanza (default: false)
     */
    private Producto procesarFilaExcel(Row row, Empresa empresa) {
        // Columnas: 0=Código, 1=Descripción, 2=Precio, 3=IVA, 4=Tipo Transacción, 5=Unidad Medida, 6=Balanza
        
        Cell codigoCell = row.getCell(0);
        Cell descripcionCell = row.getCell(1);
        Cell precioCell = row.getCell(2);
        Cell ivaCell = row.getCell(3);
        Cell tipoTransaccionCell = row.getCell(4);
        Cell unidadMedidaCell = row.getCell(5);
        Cell balanzaCell = row.getCell(6);
        
        // Descripción es obligatoria
        if (descripcionCell == null || descripcionCell.getStringCellValue().trim().isEmpty()) {
            throw new IllegalArgumentException("Descripción es requerida");
        }
        
        // Precio es obligatorio
        if (precioCell == null) {
            throw new IllegalArgumentException("Precio es requerido");
        }
        
        // IVA es obligatorio
        if (ivaCell == null) {
            throw new IllegalArgumentException("IVA es requerido");
        }
        
        Producto producto = new Producto();
        producto.setEmpresa(empresa);
        
        // Código (opcional)
        if (codigoCell != null) {
            String codigo = obtenerValorCeldaComoString(codigoCell);
            if (codigo != null && !codigo.trim().isEmpty()) {
                producto.setCodigo(codigo);
            }
        }
        
        // Descripción
        producto.setDescripcion(descripcionCell.getStringCellValue().trim());
        
        // Precio
        BigDecimal precio = obtenerValorCeldaComoBigDecimal(precioCell);
        if (precio.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Precio no puede ser negativo");
        }
        producto.setPrecio(precio);
        
        // IVA
        Integer iva = obtenerValorCeldaComoInteger(ivaCell);
        producto.setIva(iva);
        
        // Tipo de Transacción (opcional, default: VENTA_MERCADERIA)
        if (tipoTransaccionCell != null) {
            String tipoTransaccionStr = obtenerValorCeldaComoString(tipoTransaccionCell);
            if (tipoTransaccionStr != null && !tipoTransaccionStr.trim().isEmpty()) {
                try {
                    // Intentar parsear como enum
                    TipoTransaccionProducto tipo = 
                        TipoTransaccionProducto.valueOf(tipoTransaccionStr.trim().toUpperCase());
                    producto.setTipoTransaccion(tipo);
                } catch (IllegalArgumentException e) {
                    logger.warn("Tipo de transacción inválido '{}', usando VENTA_MERCADERIA por defecto", tipoTransaccionStr);
                    producto.setTipoTransaccion(TipoTransaccionProducto.VENTA_MERCADERIA);
                }
            } else {
                producto.setTipoTransaccion(TipoTransaccionProducto.VENTA_MERCADERIA);
            }
        } else {
            producto.setTipoTransaccion(TipoTransaccionProducto.VENTA_MERCADERIA);
        }
        
        // Unidad de Medida (opcional, default: UNI)
        if (unidadMedidaCell != null) {
            String unidadMedida = obtenerValorCeldaComoString(unidadMedidaCell);
            if (unidadMedida != null && !unidadMedida.trim().isEmpty()) {
                producto.setUnidadMedida(unidadMedida.trim().toUpperCase());
            } else {
                producto.setUnidadMedida("UNI");
            }
        } else {
            producto.setUnidadMedida("UNI");
        }
        
        // Balanza (opcional, por defecto false)
        Boolean balanza = false;
        if (balanzaCell != null) {
            balanza = obtenerValorCeldaComoBoolean(balanzaCell);
        }
        producto.setBalanza(balanza);
        
        producto.setActivo(true);
        
        return producto;
    }

    /**
     * Obtiene el valor de una celda como String.
     */
    private String obtenerValorCeldaComoString(Cell cell) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return null;
        }
    }

    /**
     * Obtiene el valor de una celda como BigDecimal.
     */
    private BigDecimal obtenerValorCeldaComoBigDecimal(Cell cell) {
        if (cell == null) {
            throw new IllegalArgumentException("Valor numérico requerido");
        }
        
        switch (cell.getCellType()) {
            case NUMERIC:
                return BigDecimal.valueOf(cell.getNumericCellValue());
            case STRING:
                try {
                    return new BigDecimal(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Valor numérico inválido: " + cell.getStringCellValue());
                }
            default:
                throw new IllegalArgumentException("Tipo de celda no soportado para valor numérico");
        }
    }

    /**
     * Obtiene el valor de una celda como Integer.
     */
    private Integer obtenerValorCeldaComoInteger(Cell cell) {
        if (cell == null) {
            throw new IllegalArgumentException("Valor entero requerido");
        }
        
        switch (cell.getCellType()) {
            case NUMERIC:
                return (int) cell.getNumericCellValue();
            case STRING:
                try {
                    return Integer.parseInt(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Valor entero inválido: " + cell.getStringCellValue());
                }
            default:
                throw new IllegalArgumentException("Tipo de celda no soportado para valor entero");
        }
    }

    /**
     * Obtiene el valor de una celda como Boolean.
     */
    private Boolean obtenerValorCeldaComoBoolean(Cell cell) {
        if (cell == null) return false;
        
        switch (cell.getCellType()) {
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case STRING:
                String valor = cell.getStringCellValue().trim().toLowerCase();
                return valor.equals("true") || valor.equals("si") || valor.equals("sí") || 
                       valor.equals("yes") || valor.equals("1");
            case NUMERIC:
                return cell.getNumericCellValue() != 0;
            default:
                return false;
        }
    }

    /**
     * Verifica si existe un producto con el código dado en la empresa.
     * Excluye el producto actual si se proporciona productoId.
     */
    public boolean existeCodigo(Long empresaId, String codigo, Long productoId) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return false;
        }
        
        if (productoId != null) {
            return productoRepository.existsByEmpresaIdAndCodigoAndActivoTrueExcludingId(
                empresaId, codigo.trim().toUpperCase(), productoId);
        } else {
            return productoRepository.existsByEmpresaIdAndCodigoAndActivoTrue(
                empresaId, codigo.trim().toUpperCase());
        }
    }

    /**
     * Verifica si existe un producto con la descripción dada en la empresa.
     * Excluye el producto actual si se proporciona productoId.
     * Comparación case-insensitive.
     */
    public boolean existeDescripcion(Long empresaId, String descripcion, Long productoId) {
        if (descripcion == null || descripcion.trim().isEmpty()) {
            return false;
        }
        
        if (productoId != null) {
            return productoRepository.existsByEmpresaIdAndDescripcionIgnoreCaseAndActivoTrueExcludingId(
                empresaId, descripcion.trim(), productoId);
        } else {
            return productoRepository.existsByEmpresaIdAndDescripcionIgnoreCaseAndActivoTrue(
                empresaId, descripcion.trim());
        }
    }

    /**
     * Valida que la tasa de IVA sea válida (0, 5 o 10).
     */
    private void validarTasaIva(Integer iva) {
        if (iva == null) {
            throw new IllegalArgumentException("IVA es requerido");
        }
        
        if (!TASAS_IVA_VALIDAS.contains(iva)) {
            throw new IllegalArgumentException(
                "Tasa de IVA inválida. Valores permitidos: 0, 5, 10. Valor recibido: " + iva
            );
        }
    }
}
