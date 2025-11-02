package com.frcefact.dto.mapper;

import com.frcefact.dto.ProductoDto;
import com.frcefact.model.Producto;
import com.frcefact.model.TipoTransaccionProducto;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

/**
 * Mapper para convertir entre Producto y ProductoDto.
 */
@Component
public class ProductoMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Convierte una entidad Producto a ProductoDto.
     */
    public ProductoDto toDto(Producto producto) {
        if (producto == null) {
            return null;
        }

        ProductoDto dto = new ProductoDto();
        dto.setId(producto.getId());
        dto.setEmpresaId(producto.getEmpresa() != null ? producto.getEmpresa().getId() : null);
        dto.setCodigo(producto.getCodigo());
        dto.setDescripcion(producto.getDescripcion());
        dto.setPrecio(producto.getPrecio());
        dto.setIva(producto.getIva());
        dto.setBalanza(producto.getBalanza());
        dto.setActivo(producto.getActivo());
        dto.setTipoTransaccion(producto.getTipoTransaccion());
        dto.setUnidadMedida(producto.getUnidadMedida());

        // Campos de auditoría
        if (producto.getCreadoEn() != null) {
            dto.setCreadoEn(producto.getCreadoEn().format(FORMATTER));
        }
        dto.setCreadoPor(producto.getCreadoPor());
        if (producto.getActualizadoEn() != null) {
            dto.setActualizadoEn(producto.getActualizadoEn().format(FORMATTER));
        }
        dto.setActualizadoPor(producto.getActualizadoPor());

        return dto;
    }

    /**
     * Convierte un ProductoDto a entidad Producto.
     * No establece la empresa, debe ser asignada por el servicio.
     */
    public Producto toEntity(ProductoDto dto) {
        if (dto == null) {
            return null;
        }

        Producto producto = new Producto();
        producto.setId(dto.getId());
        producto.setCodigo(dto.getCodigo());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecio(dto.getPrecio());
        producto.setIva(dto.getIva());
        producto.setBalanza(dto.getBalanza() != null ? dto.getBalanza() : false);
        producto.setActivo(dto.getActivo() != null ? dto.getActivo() : true);
        producto.setTipoTransaccion(dto.getTipoTransaccion() != null ? dto.getTipoTransaccion() : TipoTransaccionProducto.VENTA_MERCADERIA);
        producto.setUnidadMedida(dto.getUnidadMedida() != null ? dto.getUnidadMedida() : "UNI");

        return producto;
    }

    /**
     * Actualiza una entidad Producto existente con datos del DTO.
     * No actualiza la empresa ni el ID.
     */
    public void updateEntityFromDto(ProductoDto dto, Producto producto) {
        if (dto == null || producto == null) {
            return;
        }

        producto.setCodigo(dto.getCodigo());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecio(dto.getPrecio());
        producto.setIva(dto.getIva());
        producto.setBalanza(dto.getBalanza() != null ? dto.getBalanza() : false);
        
        if (dto.getTipoTransaccion() != null) {
            producto.setTipoTransaccion(dto.getTipoTransaccion());
        }
        if (dto.getUnidadMedida() != null) {
            producto.setUnidadMedida(dto.getUnidadMedida());
        }
        
        // No actualizamos activo aquí, se maneja con endpoint específico
    }
}
