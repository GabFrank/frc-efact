package com.frcefact.dto.mapper;

import com.frcefact.dto.TimbradoDetalleDto;
import com.frcefact.model.Timbrado;
import com.frcefact.model.TimbradoDetalle;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre TimbradoDetalle y TimbradoDetalleDto.
 */
@Component
public class TimbradoDetalleMapper {

    /**
     * Convierte una entidad TimbradoDetalle a TimbradoDetalleDto.
     */
    public TimbradoDetalleDto toDto(TimbradoDetalle detalle) {
        if (detalle == null) {
            return null;
        }

        TimbradoDetalleDto dto = new TimbradoDetalleDto();
        dto.setId(detalle.getId());
        dto.setTimbradoId(detalle.getTimbrado().getId());
        dto.setPuntoExpedicion(detalle.getPuntoExpedicion());
        dto.setCodigoEstablecimientoFactura(detalle.getCodigoEstablecimientoFactura());
        dto.setRangoDesde(detalle.getRangoDesde());
        dto.setRangoHasta(detalle.getRangoHasta());
        dto.setCantidad(detalle.getCantidad());
        dto.setNumeroActual(detalle.getNumeroActual());
        dto.setDepartamento(detalle.getDepartamento());
        dto.setCiudad(detalle.getCiudad());
        dto.setCodigoCiudad(detalle.getCodigoCiudad());
        dto.setLocalidad(detalle.getLocalidad());
        dto.setBarrio(detalle.getBarrio());
        dto.setDireccion(detalle.getDireccion());
        dto.setTelefono(detalle.getTelefono());
        dto.setActivo(detalle.getActivo());
        dto.setNumerosDisponibles(detalle.getNumerosDisponibles());
        dto.setPorcentajeUtilizado(detalle.getPorcentajeUtilizado());

        return dto;
    }

    /**
     * Convierte un TimbradoDetalleDto a entidad TimbradoDetalle.
     */
    public TimbradoDetalle toEntity(TimbradoDetalleDto dto) {
        if (dto == null) {
            return null;
        }

        TimbradoDetalle detalle = new TimbradoDetalle();
        detalle.setId(dto.getId());
        
        // El timbrado se establece en el servicio
        if (dto.getTimbradoId() != null) {
            Timbrado timbrado = new Timbrado();
            timbrado.setId(dto.getTimbradoId());
            detalle.setTimbrado(timbrado);
        }

        detalle.setPuntoExpedicion(dto.getPuntoExpedicion());
        detalle.setCodigoEstablecimientoFactura(dto.getCodigoEstablecimientoFactura());
        detalle.setRangoDesde(dto.getRangoDesde());
        detalle.setRangoHasta(dto.getRangoHasta());
        detalle.setCantidad(dto.getCantidad());
        detalle.setNumeroActual(dto.getNumeroActual());
        detalle.setDepartamento(dto.getDepartamento());
        detalle.setCiudad(dto.getCiudad());
        detalle.setCodigoCiudad(dto.getCodigoCiudad());
        detalle.setLocalidad(dto.getLocalidad());
        detalle.setBarrio(dto.getBarrio());
        detalle.setDireccion(dto.getDireccion());
        detalle.setTelefono(dto.getTelefono());
        
        if (dto.getActivo() != null) {
            detalle.setActivo(dto.getActivo());
        }

        return detalle;
    }
}
