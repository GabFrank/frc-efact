package com.frcefact.dto.mapper;

import com.frcefact.dto.NotaDebitoDto;
import com.frcefact.dto.NotaDebitoItemDto;
import com.frcefact.model.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class NotaDebitoMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public NotaDebitoDto toDto(NotaDebito entity) {
        if (entity == null) {
            return null;
        }

        NotaDebitoDto dto = new NotaDebitoDto();
        dto.setId(entity.getId());
        dto.setEmpresaId(entity.getEmpresa() != null ? entity.getEmpresa().getId() : null);
        dto.setTimbradoDetalleId(entity.getTimbradoDetalle() != null ? 
                entity.getTimbradoDetalle().getId() : null);
        dto.setClienteId(entity.getCliente() != null ? entity.getCliente().getId() : null);
        dto.setFacturaLegalId(entity.getFacturaLegal() != null ? entity.getFacturaLegal().getId() : null);
        dto.setNumeroNotaDebito(entity.getNumeroNotaDebito());
        
        if (entity.getFecha() != null) {
            dto.setFecha(entity.getFecha().format(FORMATTER));
        }
        
        dto.setNombre(entity.getNombre());
        dto.setRuc(entity.getRuc());
        dto.setDireccion(entity.getDireccion());
        dto.setMotivoEmision(entity.getMotivoEmision());
        dto.setDescripcionMotivo(entity.getDescripcionMotivo());

        // Totales
        dto.setIvaParcial0(entity.getIvaParcial0());
        dto.setIvaParcial5(entity.getIvaParcial5());
        dto.setIvaParcial10(entity.getIvaParcial10());
        dto.setTotalParcial0(entity.getTotalParcial0());
        dto.setTotalParcial5(entity.getTotalParcial5());
        dto.setTotalParcial10(entity.getTotalParcial10());
        dto.setDescuentoFinal(entity.getDescuentoFinal());
        dto.setTotalParcial(entity.getTotalParcial());
        dto.setTotalFinal(entity.getTotalFinal());

        // Moneda extranjera
        dto.setMonedaExtranjera(entity.getMonedaExtranjera());
        dto.setCambio(entity.getCambio());

        dto.setActivo(entity.getActivo());

        // Items
        if (entity.getItems() != null) {
            dto.setItems(entity.getItems().stream()
                    .map(this::itemToDto)
                    .collect(Collectors.toList()));
        }

        // Campos adicionales
        dto.setNumeroFormateado(entity.getNumeroFormateado());
        if (entity.getEmpresa() != null) {
            dto.setNombreEmpresa(entity.getEmpresa().getRazonSocial());
        }
        if (entity.getCliente() != null) {
            dto.setNombreCliente(entity.getCliente().getNombre());
        }
        if (entity.getFacturaLegal() != null) {
            dto.setNumeroFacturaAsociada(entity.getFacturaLegal().getNumeroFacturaFormateado());
        }

        DocumentoElectronico documento = entity.getDocumentoElectronico();
        if (documento != null) {
            dto.setDocumentoElectronicoId(documento.getId());
            dto.setEstadoDocumentoElectronico(documento.getEstado() != null ? documento.getEstado().name() : null);
            dto.setCdcDocumentoElectronico(documento.getCdc());
            dto.setLoteDeId(documento.getLoteDE() != null ? documento.getLoteDE().getId() : null);
            
            String urlQr = documento.getUrlQr();
            dto.setUrlQrDocumentoElectronico(urlQr);
        }

        return dto;
    }

    public NotaDebito toEntity(NotaDebitoDto dto) {
        if (dto == null) {
            return null;
        }

        NotaDebito entity = new NotaDebito();
        entity.setId(dto.getId());
        entity.setNumeroNotaDebito(dto.getNumeroNotaDebito());

        if (dto.getEmpresaId() != null) {
            Empresa empresa = new Empresa();
            empresa.setId(dto.getEmpresaId());
            entity.setEmpresa(empresa);
        }

        if (dto.getTimbradoDetalleId() != null) {
            TimbradoDetalle timbradoDetalle = new TimbradoDetalle();
            timbradoDetalle.setId(dto.getTimbradoDetalleId());
            entity.setTimbradoDetalle(timbradoDetalle);
        }

        if (dto.getClienteId() != null) {
            Cliente cliente = new Cliente();
            cliente.setId(dto.getClienteId());
            entity.setCliente(cliente);
        }
        
        if (dto.getFacturaLegalId() != null) {
            FacturaLegal factura = new FacturaLegal();
            factura.setId(dto.getFacturaLegalId());
            entity.setFacturaLegal(factura);
        }

        if (dto.getFecha() != null && !dto.getFecha().isEmpty()) {
            try {
                // Intentar parsear como ISO con zona horaria (formato del frontend: 2025-12-04T18:37:36.423Z)
                if (dto.getFecha().endsWith("Z") || dto.getFecha().contains("+") || dto.getFecha().contains("-") && dto.getFecha().length() > 19) {
                    Instant instant = Instant.parse(dto.getFecha());
                    entity.setFecha(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()));
                } else {
                    // Parsear como ISO_LOCAL_DATE_TIME (sin zona horaria)
                    entity.setFecha(LocalDateTime.parse(dto.getFecha(), FORMATTER));
                }
            } catch (DateTimeParseException e) {
                // Si falla, intentar parsear como ISO con zona horaria de todas formas
                try {
                    Instant instant = Instant.parse(dto.getFecha());
                    entity.setFecha(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()));
                } catch (Exception ex) {
                    throw new IllegalArgumentException("Formato de fecha inválido: " + dto.getFecha(), ex);
                }
            }
        }
        
        entity.setNombre(dto.getNombre());
        entity.setRuc(dto.getRuc());
        entity.setDireccion(dto.getDireccion());
        entity.setMotivoEmision(dto.getMotivoEmision());
        entity.setDescripcionMotivo(dto.getDescripcionMotivo());
        
        // Moneda
        entity.setMonedaExtranjera(dto.getMonedaExtranjera());
        entity.setCambio(dto.getCambio());
        
        // Totales
        if (dto.getIvaParcial0() != null) entity.setIvaParcial0(dto.getIvaParcial0());
        if (dto.getIvaParcial5() != null) entity.setIvaParcial5(dto.getIvaParcial5());
        if (dto.getIvaParcial10() != null) entity.setIvaParcial10(dto.getIvaParcial10());
        if (dto.getTotalParcial0() != null) entity.setTotalParcial0(dto.getTotalParcial0());
        if (dto.getTotalParcial5() != null) entity.setTotalParcial5(dto.getTotalParcial5());
        if (dto.getTotalParcial10() != null) entity.setTotalParcial10(dto.getTotalParcial10());
        if (dto.getDescuentoFinal() != null) entity.setDescuentoFinal(dto.getDescuentoFinal());
        if (dto.getTotalParcial() != null) entity.setTotalParcial(dto.getTotalParcial());
        if (dto.getTotalFinal() != null) entity.setTotalFinal(dto.getTotalFinal());
        
        entity.setActivo(dto.getActivo() != null ? dto.getActivo() : true);

        // Items
        if (dto.getItems() != null) {
            List<NotaDebitoItem> items = dto.getItems().stream()
                    .map(this::itemToEntity)
                    .collect(Collectors.toList());

            for (NotaDebitoItem item : items) {
                item.setNotaDebito(entity);
            }

            entity.setItems(items);
        }

        return entity;
    }

    public NotaDebitoItemDto itemToDto(NotaDebitoItem item) {
        if (item == null) {
            return null;
        }

        NotaDebitoItemDto dto = new NotaDebitoItemDto();
        dto.setId(item.getId());
        dto.setProductoId(item.getProducto() != null ? item.getProducto().getId() : null);
        dto.setCantidad(item.getCantidad());
        dto.setDescripcion(item.getDescripcion());
        dto.setPrecioUnitario(item.getPrecioUnitario());
        dto.setTotal(item.getTotal());
        dto.setIva(item.getIva());
        dto.setCodigo(item.getCodigo());

        return dto;
    }

    public NotaDebitoItem itemToEntity(NotaDebitoItemDto dto) {
        if (dto == null) {
            return null;
        }

        NotaDebitoItem item = new NotaDebitoItem();
        item.setId(dto.getId());
        item.setCantidad(dto.getCantidad());
        item.setDescripcion(dto.getDescripcion());
        item.setPrecioUnitario(dto.getPrecioUnitario());
        item.setTotal(dto.getTotal());
        item.setIva(dto.getIva());
        item.setCodigo(dto.getCodigo());

        if (dto.getProductoId() != null) {
            Producto producto = new Producto();
            producto.setId(dto.getProductoId());
            item.setProducto(producto);
        }

        return item;
    }
}

