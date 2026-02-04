package com.frcefact.dto.mapper;

import com.frcefact.dto.DocumentoElectronicoDto;
import com.frcefact.model.DocumentoElectronico;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre DocumentoElectronico y DocumentoElectronicoDto.
 */
@Component
public class DocumentoElectronicoMapper {

    /**
     * Convierte una entidad DocumentoElectronico a DTO.
     * Si la URL del QR no está guardada, la extrae del XML original.
     * 
     * @param entity Entidad
     * @return DTO
     */
    public DocumentoElectronicoDto toDto(DocumentoElectronico entity) {
        if (entity == null) {
            return null;
        }

        DocumentoElectronicoDto dto = new DocumentoElectronicoDto();
        dto.setId(entity.getId());
        dto.setFacturaLegalId(entity.getFacturaLegal() != null ? entity.getFacturaLegal().getId() : null);
        dto.setLoteDeId(entity.getLoteDE() != null ? entity.getLoteDE().getId() : null);
        dto.setCdc(entity.getCdc());
        
        // Extraer URL del QR si no está guardada
        String urlQr = entity.getUrlQr();
        if ((urlQr == null || urlQr.isBlank()) && entity.getXmlOriginal() != null && !entity.getXmlOriginal().isBlank()) {
            urlQr = com.frcefact.sifen.util.SifenResponseParser.extractUrlQr(entity.getXmlOriginal());
        }
        dto.setUrlQr(urlQr);
        
        dto.setNumeroDocumento(entity.getNumeroDocumento());
        dto.setTipoDocumento(entity.getTipoDocumento());
        dto.setEstado(entity.getEstado());
        dto.setCodigoRespuestaSifen(entity.getCodigoRespuestaSifen());
        dto.setMensajeRespuestaSifen(entity.getMensajeRespuestaSifen());
        dto.setRespuestaSifen(entity.getRespuestaSifen());
        dto.setProtocoloAutorizacion(entity.getProtocoloAutorizacion());
        dto.setFechaEmision(entity.getFechaEmision());
        dto.setFechaRecepcionSifen(entity.getFechaRecepcionSifen());

        // Datos adicionales de la factura
        if (entity.getFacturaLegal() != null) {
            dto.setNumeroFacturaFormateado(entity.getFacturaLegal().getNumeroFacturaFormateado());
            dto.setNombreCliente(entity.getFacturaLegal().getNombre());
            dto.setRucCliente(entity.getFacturaLegal().getRuc());
            
            if (entity.getFacturaLegal().getEmpresa() != null) {
                dto.setRazonSocialEmpresa(entity.getFacturaLegal().getEmpresa().getRazonSocial());
            }
        }

        return dto;
    }

    /**
     * Convierte un DTO a entidad DocumentoElectronico.
     * 
     * NOTA: Este método solo establece los campos básicos.
     * Las relaciones (facturaLegal, loteDE) deben establecerse por separado.
     * 
     * @param dto DTO
     * @return Entidad
     */
    public DocumentoElectronico toEntity(DocumentoElectronicoDto dto) {
        if (dto == null) {
            return null;
        }

        DocumentoElectronico entity = new DocumentoElectronico();
        entity.setId(dto.getId());
        entity.setCdc(dto.getCdc());
        entity.setUrlQr(dto.getUrlQr());
        entity.setNumeroDocumento(dto.getNumeroDocumento());
        entity.setTipoDocumento(dto.getTipoDocumento());
        entity.setEstado(dto.getEstado());
        entity.setCodigoRespuestaSifen(dto.getCodigoRespuestaSifen());
        entity.setMensajeRespuestaSifen(dto.getMensajeRespuestaSifen());
        entity.setFechaEmision(dto.getFechaEmision());
        entity.setFechaRecepcionSifen(dto.getFechaRecepcionSifen());

        return entity;
    }

    /**
     * Actualiza una entidad existente con los datos del DTO.
     * 
     * @param dto DTO con los nuevos datos
     * @param entity Entidad a actualizar
     */
    public void updateEntity(DocumentoElectronicoDto dto, DocumentoElectronico entity) {
        if (dto == null || entity == null) {
            return;
        }

        entity.setCdc(dto.getCdc());
        entity.setUrlQr(dto.getUrlQr());
        entity.setNumeroDocumento(dto.getNumeroDocumento());
        entity.setTipoDocumento(dto.getTipoDocumento());
        entity.setEstado(dto.getEstado());
        entity.setCodigoRespuestaSifen(dto.getCodigoRespuestaSifen());
        entity.setMensajeRespuestaSifen(dto.getMensajeRespuestaSifen());
        entity.setFechaEmision(dto.getFechaEmision());
        entity.setFechaRecepcionSifen(dto.getFechaRecepcionSifen());
    }
}
