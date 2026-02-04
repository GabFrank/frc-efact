package com.frcefact.dto.mapper;

import com.frcefact.dto.TimbradoDetalleDto;
import com.frcefact.model.Barrio;
import com.frcefact.model.Ciudad;
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
        
        // Verificar que el timbrado existe antes de acceder a su ID
        if (detalle.getTimbrado() != null) {
            dto.setTimbradoId(detalle.getTimbrado().getId());
            dto.setTimbradoNumero(detalle.getTimbrado().getNumero());
            dto.setTimbradoIsElectronico(detalle.getTimbrado().getIsElectronico());
        }
        
        dto.setPuntoExpedicion(detalle.getPuntoExpedicion());
        dto.setCodigoEstablecimientoFactura(detalle.getCodigoEstablecimientoFactura());
        dto.setCantidad(detalle.getCantidad());
        dto.setRangoDesde(detalle.getRangoDesde());
        dto.setRangoHasta(detalle.getRangoHasta());
        dto.setNumeroActual(detalle.getNumeroActual());
        
        // Mapear relaciones geográficas
        if (detalle.getCiudad() != null) {
            dto.setCiudadId(detalle.getCiudad().getId());
        }
        if (detalle.getBarrio() != null) {
            dto.setBarrioId(detalle.getBarrio().getId());
        }
        
        dto.setDireccion(detalle.getDireccion());
        dto.setTelefono(detalle.getTelefono());
        dto.setActivo(detalle.getActivo());

        // Campos calculados - manejar posibles errores
        try {
            dto.setNumerosDisponibles(detalle.getNumerosDisponibles());
        } catch (Exception e) {
            dto.setNumerosDisponibles(0L);
        }
        
        try {
            dto.setPorcentajeUtilizado(detalle.getPorcentajeUtilizado());
        } catch (Exception e) {
            dto.setPorcentajeUtilizado(0.0);
        }

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
        
        // La relación con Timbrado se establece en el servicio
        if (dto.getTimbradoId() != null) {
            Timbrado timbrado = new Timbrado();
            timbrado.setId(dto.getTimbradoId());
            detalle.setTimbrado(timbrado);
        }

        detalle.setPuntoExpedicion(dto.getPuntoExpedicion());
        detalle.setCodigoEstablecimientoFactura(dto.getCodigoEstablecimientoFactura());
        
        // Solo establecer campos de rango si están presentes
        // El servicio se encargará de establecer valores por defecto para timbrados electrónicos
        if (dto.getCantidad() != null) {
            detalle.setCantidad(dto.getCantidad());
        }
        if (dto.getRangoDesde() != null) {
            detalle.setRangoDesde(dto.getRangoDesde());
        }
        if (dto.getRangoHasta() != null) {
            detalle.setRangoHasta(dto.getRangoHasta());
        }
        if (dto.getNumeroActual() != null) {
            detalle.setNumeroActual(dto.getNumeroActual());
        }
        
        // Establecer relaciones geográficas usando IDs
        if (dto.getCiudadId() != null) {
            Ciudad ciudad = new Ciudad();
            ciudad.setId(dto.getCiudadId());
            detalle.setCiudad(ciudad);
        }
        if (dto.getBarrioId() != null) {
            Barrio barrio = new Barrio();
            barrio.setId(dto.getBarrioId());
            detalle.setBarrio(barrio);
        }
        
        detalle.setDireccion(dto.getDireccion());
        detalle.setTelefono(dto.getTelefono());
        
        if (dto.getActivo() != null) {
            detalle.setActivo(dto.getActivo());
        }

        return detalle;
    }
}