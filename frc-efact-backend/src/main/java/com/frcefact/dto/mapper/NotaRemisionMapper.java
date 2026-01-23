package com.frcefact.dto.mapper;

import com.frcefact.dto.NotaRemisionDto;
import com.frcefact.dto.NotaRemisionItemDto;
import com.frcefact.model.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class NotaRemisionMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public NotaRemisionDto toDto(NotaRemision entity) {
        if (entity == null) {
            return null;
        }

        NotaRemisionDto dto = new NotaRemisionDto();
        dto.setId(entity.getId());
        dto.setEmpresaId(entity.getEmpresa() != null ? entity.getEmpresa().getId() : null);
        dto.setTimbradoDetalleId(entity.getTimbradoDetalle() != null ? 
                entity.getTimbradoDetalle().getId() : null);
        dto.setClienteId(entity.getCliente() != null ? entity.getCliente().getId() : null);
        dto.setFacturaLegalId(entity.getFacturaLegal() != null ? entity.getFacturaLegal().getId() : null);
        dto.setNumeroNotaRemision(entity.getNumeroNotaRemision());
        
        if (entity.getFecha() != null) {
            dto.setFecha(entity.getFecha().format(FORMATTER));
        }
        
        dto.setDireccionPartida(entity.getDireccionPartida());
        dto.setCiudadPartida(entity.getCiudadPartida());
        dto.setDepartamentoPartida(entity.getDepartamentoPartida());
        dto.setCiudadPartidaId(entity.getCiudadPartidaId());
        dto.setDepartamentoPartidaId(entity.getDepartamentoPartidaId());
        dto.setDistritoPartidaId(entity.getDistritoPartidaId());
        
        dto.setNombreDestinatario(entity.getNombreDestinatario());
        dto.setRucDestinatario(entity.getRucDestinatario());
        dto.setDireccionDestinatario(entity.getDireccionDestinatario());
        dto.setCiudadDestinatario(entity.getCiudadDestinatario());
        dto.setDepartamentoDestinatario(entity.getDepartamentoDestinatario());
        dto.setCiudadDestinatarioId(entity.getCiudadDestinatarioId());
        dto.setDepartamentoDestinatarioId(entity.getDepartamentoDestinatarioId());
        dto.setDistritoDestinatarioId(entity.getDistritoDestinatarioId());
        
        dto.setMotivoEmision(entity.getMotivoEmision());
        
        if (entity.getFechaInicioTraslado() != null) {
            dto.setFechaInicioTraslado(entity.getFechaInicioTraslado().format(DATE_FORMATTER));
        }
        if (entity.getFechaFinTraslado() != null) {
            dto.setFechaFinTraslado(entity.getFechaFinTraslado().format(DATE_FORMATTER));
        }
        
        dto.setKmEstimado(entity.getKmEstimado());
        dto.setTipoTransporte(entity.getTipoTransporte());
        dto.setModalidadTransporte(entity.getModalidadTransporte());
        
        dto.setVehiculoMarca(entity.getVehiculoMarca());
        dto.setVehiculoMatricula(entity.getVehiculoMatricula());
        
        dto.setTransportistaNombre(entity.getTransportistaNombre());
        dto.setTransportistaRuc(entity.getTransportistaRuc());
        dto.setTransportistaDireccion(entity.getTransportistaDireccion());
        
        dto.setConductorNombre(entity.getConductorNombre());
        dto.setConductorDoc(entity.getConductorDoc());
        dto.setConductorDireccion(entity.getConductorDireccion());

        if (entity.getFechaEstimadaFactura() != null) {
            dto.setFechaEstimadaFactura(entity.getFechaEstimadaFactura().format(DATE_FORMATTER));
        }

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
            dto.setEmailCliente(entity.getCliente().getEmail());
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

    public NotaRemision toEntity(NotaRemisionDto dto) {
        if (dto == null) {
            return null;
        }

        NotaRemision entity = new NotaRemision();
        entity.setId(dto.getId());
        entity.setNumeroNotaRemision(dto.getNumeroNotaRemision());

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
                // Intentar parsear como ISO con zona horaria (formato del frontend: 2026-01-22T19:18:28.055Z)
                if (dto.getFecha().endsWith("Z") || dto.getFecha().contains("+") || (dto.getFecha().contains("-") && dto.getFecha().length() > 19)) {
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
        
        entity.setDireccionPartida(dto.getDireccionPartida());
        entity.setCiudadPartida(dto.getCiudadPartida());
        entity.setDepartamentoPartida(dto.getDepartamentoPartida());
        entity.setCiudadPartidaId(dto.getCiudadPartidaId());
        entity.setDepartamentoPartidaId(dto.getDepartamentoPartidaId());
        entity.setDistritoPartidaId(dto.getDistritoPartidaId());
        
        entity.setNombreDestinatario(dto.getNombreDestinatario());
        entity.setRucDestinatario(dto.getRucDestinatario());
        entity.setDireccionDestinatario(dto.getDireccionDestinatario());
        entity.setCiudadDestinatario(dto.getCiudadDestinatario());
        entity.setDepartamentoDestinatario(dto.getDepartamentoDestinatario());
        entity.setCiudadDestinatarioId(dto.getCiudadDestinatarioId());
        entity.setDepartamentoDestinatarioId(dto.getDepartamentoDestinatarioId());
        entity.setDistritoDestinatarioId(dto.getDistritoDestinatarioId());
        
        entity.setMotivoEmision(dto.getMotivoEmision());
        
        if (dto.getFechaInicioTraslado() != null && !dto.getFechaInicioTraslado().isEmpty()) {
            entity.setFechaInicioTraslado(LocalDate.parse(dto.getFechaInicioTraslado(), DATE_FORMATTER));
        }
        if (dto.getFechaFinTraslado() != null && !dto.getFechaFinTraslado().isEmpty()) {
            entity.setFechaFinTraslado(LocalDate.parse(dto.getFechaFinTraslado(), DATE_FORMATTER));
        }
        
        entity.setKmEstimado(dto.getKmEstimado());
        entity.setTipoTransporte(dto.getTipoTransporte());
        entity.setModalidadTransporte(dto.getModalidadTransporte());
        
        entity.setVehiculoMarca(dto.getVehiculoMarca());
        entity.setVehiculoMatricula(dto.getVehiculoMatricula());
        
        entity.setTransportistaNombre(dto.getTransportistaNombre());
        entity.setTransportistaRuc(dto.getTransportistaRuc());
        entity.setTransportistaDireccion(dto.getTransportistaDireccion());
        
        entity.setConductorNombre(dto.getConductorNombre());
        entity.setConductorDoc(dto.getConductorDoc());
        entity.setConductorDireccion(dto.getConductorDireccion());
        
        if (dto.getFechaEstimadaFactura() != null && !dto.getFechaEstimadaFactura().isEmpty()) {
            entity.setFechaEstimadaFactura(LocalDate.parse(dto.getFechaEstimadaFactura(), DATE_FORMATTER));
        }
        
        entity.setActivo(dto.getActivo() != null ? dto.getActivo() : true);

        // Items
        if (dto.getItems() != null) {
            List<NotaRemisionItem> items = dto.getItems().stream()
                    .map(this::itemToEntity)
                    .collect(Collectors.toList());

            for (NotaRemisionItem item : items) {
                item.setNotaRemision(entity);
            }

            entity.setItems(items);
        }

        return entity;
    }

    public NotaRemisionItemDto itemToDto(NotaRemisionItem item) {
        if (item == null) {
            return null;
        }

        NotaRemisionItemDto dto = new NotaRemisionItemDto();
        dto.setId(item.getId());
        dto.setProductoId(item.getProducto() != null ? item.getProducto().getId() : null);
        dto.setCantidad(item.getCantidad());
        dto.setDescripcion(item.getDescripcion());
        dto.setUnidadMedida(item.getUnidadMedida());
        dto.setCodigo(item.getCodigo());

        return dto;
    }

    public NotaRemisionItem itemToEntity(NotaRemisionItemDto dto) {
        if (dto == null) {
            return null;
        }

        NotaRemisionItem item = new NotaRemisionItem();
        item.setId(dto.getId());
        item.setCantidad(dto.getCantidad());
        item.setDescripcion(dto.getDescripcion());
        item.setUnidadMedida(dto.getUnidadMedida());
        item.setCodigo(dto.getCodigo());

        if (dto.getProductoId() != null) {
            Producto producto = new Producto();
            producto.setId(dto.getProductoId());
            item.setProducto(producto);
        }

        return item;
    }
}

