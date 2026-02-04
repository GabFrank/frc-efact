package com.frcefact.dto.mapper;

import com.frcefact.dto.UsuarioEmpresaDto;
import com.frcefact.model.UsuarioEmpresa;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre UsuarioEmpresa y UsuarioEmpresaDto.
 */
@Component
public class UsuarioEmpresaMapper {

    /**
     * Convierte una entidad UsuarioEmpresa a UsuarioEmpresaDto.
     *
     * @param usuarioEmpresa Entidad UsuarioEmpresa
     * @return UsuarioEmpresaDto
     */
    public UsuarioEmpresaDto toDto(UsuarioEmpresa usuarioEmpresa) {
        if (usuarioEmpresa == null) {
            return null;
        }

        UsuarioEmpresaDto dto = new UsuarioEmpresaDto();
        dto.setId(usuarioEmpresa.getId());
        dto.setUsuarioId(usuarioEmpresa.getUsuario() != null ? usuarioEmpresa.getUsuario().getId() : null);
        dto.setEmpresaId(usuarioEmpresa.getEmpresa() != null ? usuarioEmpresa.getEmpresa().getId() : null);
        dto.setRolEmpresa(usuarioEmpresa.getRolEmpresa());
        dto.setActivo(usuarioEmpresa.getActivo());
        dto.setCreadoEn(usuarioEmpresa.getCreadoEn());
        dto.setActualizadoEn(usuarioEmpresa.getActualizadoEn());

        // Información adicional para visualización
        if (usuarioEmpresa.getUsuario() != null) {
            dto.setUsuarioUsername(usuarioEmpresa.getUsuario().getUsername());
            dto.setUsuarioEmail(usuarioEmpresa.getUsuario().getEmail());
            
            // Mapear roles del usuario
            if (usuarioEmpresa.getUsuario().getUsuarioRoles() != null) {
                dto.setUsuarioRoles(usuarioEmpresa.getUsuario().getUsuarioRoles().stream()
                        .map(usuarioRol -> usuarioRol.getRol() != null ? usuarioRol.getRol().getNombre() : null)
                        .filter(rolNombre -> rolNombre != null)
                        .collect(java.util.stream.Collectors.toList()));
            }
        }
        if (usuarioEmpresa.getEmpresa() != null) {
            dto.setEmpresaRazonSocial(usuarioEmpresa.getEmpresa().getRazonSocial());
        }

        return dto;
    }

    /**
     * Convierte una lista de entidades UsuarioEmpresa a lista de UsuarioEmpresaDto.
     *
     * @param usuarioEmpresas Lista de entidades UsuarioEmpresa
     * @return Lista de UsuarioEmpresaDto
     */
    public List<UsuarioEmpresaDto> toDtoList(List<UsuarioEmpresa> usuarioEmpresas) {
        if (usuarioEmpresas == null) {
            return null;
        }

        return usuarioEmpresas.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Convierte un UsuarioEmpresaDto a entidad UsuarioEmpresa.
     * No establece las relaciones Usuario y Empresa, deben ser asignadas por separado.
     *
     * @param dto UsuarioEmpresaDto
     * @return Entidad UsuarioEmpresa
     */
    public UsuarioEmpresa toEntity(UsuarioEmpresaDto dto) {
        if (dto == null) {
            return null;
        }

        UsuarioEmpresa usuarioEmpresa = new UsuarioEmpresa();
        usuarioEmpresa.setId(dto.getId());
        usuarioEmpresa.setRolEmpresa(dto.getRolEmpresa());
        usuarioEmpresa.setActivo(dto.getActivo() != null ? dto.getActivo() : true);

        // Las relaciones Usuario y Empresa deben ser establecidas por el servicio
        return usuarioEmpresa;
    }

    /**
     * Actualiza una entidad UsuarioEmpresa existente con datos del DTO.
     *
     * @param usuarioEmpresa Entidad UsuarioEmpresa existente
     * @param dto UsuarioEmpresaDto con datos actualizados
     */
    public void updateEntityFromDto(UsuarioEmpresa usuarioEmpresa, UsuarioEmpresaDto dto) {
        if (usuarioEmpresa == null || dto == null) {
            return;
        }

        usuarioEmpresa.setRolEmpresa(dto.getRolEmpresa());
        if (dto.getActivo() != null) {
            usuarioEmpresa.setActivo(dto.getActivo());
        }

        // No actualizar las relaciones Usuario y Empresa
    }
}
