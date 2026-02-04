package com.frcefact.dto.mapper;

import com.frcefact.dto.ChoferDto;
import com.frcefact.model.Chofer;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre Chofer y ChoferDto.
 */
@Component
public class ChoferMapper {

    /**
     * Convierte una entidad Chofer a ChoferDto.
     */
    public ChoferDto toDto(Chofer chofer) {
        if (chofer == null) {
            return null;
        }

        ChoferDto dto = new ChoferDto();
        dto.setId(chofer.getId());
        dto.setNombre(chofer.getNombre());
        dto.setDocumento(chofer.getDocumento());
        dto.setDireccion(chofer.getDireccion());
        dto.setActivo(chofer.getActivo());

        if (chofer.getEmpresa() != null) {
            dto.setEmpresaId(chofer.getEmpresa().getId());
        }

        return dto;
    }

    /**
     * Convierte un ChoferDto a entidad Chofer.
     * No asigna la empresa completa, solo crea referencia proxy con ID.
     * El servicio debe cargar la entidad completa.
     */
    public Chofer toEntity(ChoferDto dto) {
        if (dto == null) {
            return null;
        }

        Chofer chofer = new Chofer();
        chofer.setId(dto.getId());
        chofer.setNombre(dto.getNombre());
        chofer.setDocumento(dto.getDocumento());
        chofer.setDireccion(dto.getDireccion());
        chofer.setActivo(dto.getActivo());

        return chofer;
    }

    /**
     * Actualiza una entidad Chofer existente con datos del DTO.
     */
    public void updateEntityFromDto(ChoferDto dto, Chofer chofer) {
        if (dto == null || chofer == null) {
            return;
        }

        chofer.setNombre(dto.getNombre());
        chofer.setDocumento(dto.getDocumento());
        chofer.setDireccion(dto.getDireccion());
        chofer.setActivo(dto.getActivo());
    }
}
