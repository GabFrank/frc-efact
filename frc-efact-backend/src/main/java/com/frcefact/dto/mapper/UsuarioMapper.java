package com.frcefact.dto.mapper;

import com.frcefact.dto.UsuarioDto;
import com.frcefact.model.Usuario;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre Usuario y UsuarioDto.
 */
@Component
public class UsuarioMapper {

    private final RolMapper rolMapper;
    private final UsuarioEmpresaMapper usuarioEmpresaMapper;

    public UsuarioMapper(RolMapper rolMapper, UsuarioEmpresaMapper usuarioEmpresaMapper) {
        this.rolMapper = rolMapper;
        this.usuarioEmpresaMapper = usuarioEmpresaMapper;
    }

    /**
     * Convierte una entidad Usuario a UsuarioDto.
     *
     * @param usuario Entidad Usuario
     * @return UsuarioDto
     */
    public UsuarioDto toDto(Usuario usuario) {
        if (usuario == null) {
            return null;
        }

        UsuarioDto dto = new UsuarioDto();
        dto.setId(usuario.getId());
        dto.setUsername(usuario.getUsername());
        dto.setEmail(usuario.getEmail());
        dto.setIsActive(usuario.getIsActive());
        dto.setUltimoLogin(usuario.getUltimoLogin());
        dto.setCreadoEn(usuario.getCreadoEn());
        dto.setActualizadoEn(usuario.getActualizadoEn());

        // Mapear roles
        if (usuario.getUsuarioRoles() != null) {
            dto.setRoles(usuario.getUsuarioRoles().stream()
                    .map(usuarioRol -> rolMapper.toDto(usuarioRol.getRol()))
                    .collect(Collectors.toSet()));
        }

        // Mapear empresas
        if (usuario.getUsuarioEmpresas() != null) {
            dto.setEmpresas(usuario.getUsuarioEmpresas().stream()
                    .map(usuarioEmpresaMapper::toDto)
                    .collect(Collectors.toSet()));
        }

        // No incluir password en el DTO de salida
        return dto;
    }

    /**
     * Convierte una entidad Usuario a UsuarioDto sin incluir relaciones.
     * Útil para evitar carga circular de datos.
     *
     * @param usuario Entidad Usuario
     * @return UsuarioDto sin roles ni empresas
     */
    public UsuarioDto toDtoSimple(Usuario usuario) {
        if (usuario == null) {
            return null;
        }

        UsuarioDto dto = new UsuarioDto();
        dto.setId(usuario.getId());
        dto.setUsername(usuario.getUsername());
        dto.setEmail(usuario.getEmail());
        dto.setIsActive(usuario.getIsActive());
        dto.setUltimoLogin(usuario.getUltimoLogin());
        dto.setCreadoEn(usuario.getCreadoEn());
        dto.setActualizadoEn(usuario.getActualizadoEn());

        return dto;
    }

    /**
     * Convierte un UsuarioDto a entidad Usuario.
     * No incluye password hash, debe ser manejado por separado.
     *
     * @param dto UsuarioDto
     * @return Entidad Usuario
     */
    public Usuario toEntity(UsuarioDto dto) {
        if (dto == null) {
            return null;
        }

        Usuario usuario = new Usuario();
        usuario.setId(dto.getId());
        usuario.setUsername(dto.getUsername());
        usuario.setEmail(dto.getEmail());
        usuario.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);

        // El password debe ser hasheado antes de asignarlo
        // No se mapea directamente desde el DTO

        return usuario;
    }

    /**
     * Actualiza una entidad Usuario existente con datos del DTO.
     * No actualiza password ni relaciones.
     *
     * @param usuario Entidad Usuario existente
     * @param dto UsuarioDto con datos actualizados
     */
    public void updateEntityFromDto(Usuario usuario, UsuarioDto dto) {
        if (usuario == null || dto == null) {
            return;
        }

        usuario.setUsername(dto.getUsername());
        usuario.setEmail(dto.getEmail());
        if (dto.getIsActive() != null) {
            usuario.setIsActive(dto.getIsActive());
        }

        // No actualizar password aquí, debe ser un endpoint separado
        // No actualizar roles ni empresas aquí, deben ser endpoints separados
    }

    /**
     * Convierte una lista de entidades Usuario a lista de UsuarioDto.
     *
     * @param usuarios Lista de entidades Usuario
     * @return Lista de UsuarioDto
     */
    public List<UsuarioDto> toDtoList(List<Usuario> usuarios) {
        if (usuarios == null) {
            return null;
        }

        return usuarios.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Convierte una lista de entidades Usuario a lista de UsuarioDto simple.
     *
     * @param usuarios Lista de entidades Usuario
     * @return Lista de UsuarioDto sin relaciones
     */
    public List<UsuarioDto> toDtoSimpleList(List<Usuario> usuarios) {
        if (usuarios == null) {
            return null;
        }

        return usuarios.stream()
                .map(this::toDtoSimple)
                .collect(Collectors.toList());
    }
}
