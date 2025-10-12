package com.frcefact.dto.mapper;

import com.frcefact.dto.RolDto;
import com.frcefact.model.Rol;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre Rol y RolDto.
 */
@Component
public class RolMapper {

    /**
     * Convierte una entidad Rol a RolDto.
     *
     * @param rol Entidad Rol
     * @return RolDto
     */
    public RolDto toDto(Rol rol) {
        if (rol == null) {
            return null;
        }

        RolDto dto = new RolDto();
        dto.setId(rol.getId());
        dto.setNombre(rol.getNombre());
        dto.setDescripcion(rol.getDescripcion());
        dto.setCreadoEn(rol.getCreadoEn());

        return dto;
    }

    /**
     * Convierte un RolDto a entidad Rol.
     *
     * @param dto RolDto
     * @return Entidad Rol
     */
    public Rol toEntity(RolDto dto) {
        if (dto == null) {
            return null;
        }

        Rol rol = new Rol();
        rol.setId(dto.getId());
        rol.setNombre(dto.getNombre());
        rol.setDescripcion(dto.getDescripcion());

        return rol;
    }

    /**
     * Actualiza una entidad Rol existente con datos del DTO.
     *
     * @param rol Entidad Rol existente
     * @param dto RolDto con datos actualizados
     */
    public void updateEntityFromDto(Rol rol, RolDto dto) {
        if (rol == null || dto == null) {
            return;
        }

        rol.setNombre(dto.getNombre());
        rol.setDescripcion(dto.getDescripcion());
    }
}
