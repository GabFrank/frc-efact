package com.frcefact.dto.mapper;

import com.frcefact.dto.FacturaLegalDto;
import com.frcefact.dto.FacturaLegalItemDto;
import com.frcefact.model.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre FacturaLegal y FacturaLegalDto.
 */
@Component
public class FacturaLegalMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Convierte una entidad FacturaLegal a FacturaLegalDto.
     */
    public FacturaLegalDto toDto(FacturaLegal factura) {
        if (factura == null) {
            return null;
        }

        FacturaLegalDto dto = new FacturaLegalDto();
        dto.setId(factura.getId());
        dto.setEmpresaId(factura.getEmpresa() != null ? factura.getEmpresa().getId() : null);
        dto.setTimbradoDetalleId(factura.getTimbradoDetalle() != null ? 
                factura.getTimbradoDetalle().getId() : null);
        dto.setClienteId(factura.getCliente() != null ? factura.getCliente().getId() : null);
        dto.setNumeroFactura(factura.getNumeroFactura());
        
        if (factura.getFecha() != null) {
            dto.setFecha(factura.getFecha().format(FORMATTER));
        }
        
        dto.setCredito(factura.getCredito());
        dto.setNombre(factura.getNombre());
        dto.setRuc(factura.getRuc());
        dto.setDireccion(factura.getDireccion());

        // Totales
        dto.setIvaParcial0(factura.getIvaParcial0());
        dto.setIvaParcial5(factura.getIvaParcial5());
        dto.setIvaParcial10(factura.getIvaParcial10());
        dto.setTotalParcial0(factura.getTotalParcial0());
        dto.setTotalParcial5(factura.getTotalParcial5());
        dto.setTotalParcial10(factura.getTotalParcial10());
        dto.setDescuentoFinal(factura.getDescuentoFinal());
        dto.setTotalParcial(factura.getTotalParcial());
        dto.setTotalFinal(factura.getTotalFinal());

        dto.setActivo(factura.getActivo());

        // Items
        if (factura.getItems() != null) {
            dto.setItems(factura.getItems().stream()
                    .map(this::itemToDto)
                    .collect(Collectors.toList()));
        }

        // Campos de auditoría
        if (factura.getCreadoEn() != null) {
            dto.setCreadoEn(factura.getCreadoEn().format(FORMATTER));
        }
        dto.setCreadoPor(factura.getCreadoPor());
        if (factura.getActualizadoEn() != null) {
            dto.setActualizadoEn(factura.getActualizadoEn().format(FORMATTER));
        }
        dto.setActualizadoPor(factura.getActualizadoPor());

        // Campos adicionales
        dto.setNumeroFacturaFormateado(factura.getNumeroFacturaFormateado());
        if (factura.getEmpresa() != null) {
            dto.setNombreEmpresa(factura.getEmpresa().getRazonSocial());
        }
        if (factura.getCliente() != null) {
            dto.setNombreCliente(factura.getCliente().getNombre());
        }

        return dto;
    }

    /**
     * Convierte un FacturaLegalDto a entidad FacturaLegal.
     * No establece empresa, timbradoDetalle ni cliente, deben ser asignados por el servicio.
     */
    public FacturaLegal toEntity(FacturaLegalDto dto) {
        if (dto == null) {
            return null;
        }

        FacturaLegal factura = new FacturaLegal();
        factura.setId(dto.getId());
        factura.setNumeroFactura(dto.getNumeroFactura());
        
        if (dto.getFecha() != null && !dto.getFecha().isEmpty()) {
            factura.setFecha(LocalDateTime.parse(dto.getFecha(), FORMATTER));
        }
        
        factura.setCredito(dto.getCredito() != null ? dto.getCredito() : false);
        factura.setNombre(dto.getNombre());
        factura.setRuc(dto.getRuc());
        factura.setDireccion(dto.getDireccion());
        factura.setDescuentoFinal(dto.getDescuentoFinal());
        factura.setActivo(dto.getActivo() != null ? dto.getActivo() : true);

        // Items
        if (dto.getItems() != null) {
            List<FacturaLegalItem> items = dto.getItems().stream()
                    .map(this::itemToEntity)
                    .collect(Collectors.toList());
            
            for (FacturaLegalItem item : items) {
                factura.agregarItem(item);
            }
        }

        return factura;
    }

    /**
     * Convierte un FacturaLegalItem a FacturaLegalItemDto.
     */
    public FacturaLegalItemDto itemToDto(FacturaLegalItem item) {
        if (item == null) {
            return null;
        }

        FacturaLegalItemDto dto = new FacturaLegalItemDto();
        dto.setId(item.getId());
        dto.setProductoId(item.getProducto() != null ? item.getProducto().getId() : null);
        dto.setCantidad(item.getCantidad());
        dto.setDescripcion(item.getDescripcion());
        dto.setPrecioUnitario(item.getPrecioUnitario());
        dto.setTotal(item.getTotal());

        return dto;
    }

    /**
     * Convierte un FacturaLegalItemDto a FacturaLegalItem.
     * No establece la factura ni el producto, deben ser asignados por el servicio.
     */
    public FacturaLegalItem itemToEntity(FacturaLegalItemDto dto) {
        if (dto == null) {
            return null;
        }

        FacturaLegalItem item = new FacturaLegalItem();
        item.setId(dto.getId());
        item.setCantidad(dto.getCantidad());
        item.setDescripcion(dto.getDescripcion());
        item.setPrecioUnitario(dto.getPrecioUnitario());
        item.setTotal(dto.getTotal());

        return item;
    }

    /**
     * Actualiza una entidad FacturaLegal existente con datos del DTO.
     * Solo actualiza campos permitidos.
     */
    public void updateEntityFromDto(FacturaLegalDto dto, FacturaLegal factura) {
        if (dto == null || factura == null) {
            return;
        }

        factura.setCredito(dto.getCredito() != null ? dto.getCredito() : false);
        factura.setNombre(dto.getNombre());
        factura.setRuc(dto.getRuc());
        factura.setDireccion(dto.getDireccion());
        
        if (dto.getDescuentoFinal() != null) {
            factura.aplicarDescuento(dto.getDescuentoFinal());
        }
    }
}
