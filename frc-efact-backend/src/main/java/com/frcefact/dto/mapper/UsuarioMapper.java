package com.frcefact.dto.mapper;

import com.frcefact.dto.RolDto;
import com.frcefact.dto.UsuarioDto;
import com.frcefact.model.Usuario;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
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
        dto.setAuth0Id(usuario.getAuth0Id());
        dto.setImagenPerfil(usuario.getImagenPerfil());

        // Mapear roles globales (persona.usuario_rol)
        Set<RolDto> roles = new HashSet<>();
        if (usuario.getUsuarioRoles() != null) {
            usuario.getUsuarioRoles().stream()
                    .map(usuarioRol -> rolMapper.toDto(usuarioRol.getRol()))
                    .forEach(roles::add);
        }

        // Mapear roles de empresa activos a roles de sistema y agregarlos al mismo set.
        // Esto refleja en el DTO la misma lógica que CustomUserDetailsService.getAuthorities()
        // aplica a las authorities de Spring Security, para que el frontend
        // (PermissionsService) tome los permisos correctos sin requerir un rol global aparte.
        // Mapping: ADMINISTRADOR -> EMPRESA_ADMIN, FACTURADOR -> FACTURADOR, LECTOR -> LECTOR.
        if (usuario.getUsuarioEmpresas() != null) {
            Set<String> nombresExistentes = roles.stream()
                    .map(RolDto::getNombre)
                    .collect(Collectors.toSet());

            usuario.getUsuarioEmpresas().forEach(usuarioEmpresa -> {
                if (usuarioEmpresa == null || !Boolean.TRUE.equals(usuarioEmpresa.getActivo())) {
                    return;
                }
                String rolEmpresa = usuarioEmpresa.getRolEmpresa();
                if (rolEmpresa == null || rolEmpresa.isBlank()) {
                    return;
                }

                String rolSistema = switch (rolEmpresa.toUpperCase(Locale.ROOT)) {
                    case "ADMINISTRADOR" -> "EMPRESA_ADMIN";
                    case "FACTURADOR" -> "FACTURADOR";
                    case "LECTOR" -> "LECTOR";
                    default -> null;
                };

                if (rolSistema != null && nombresExistentes.add(rolSistema)) {
                    roles.add(new RolDto(null, rolSistema));
                }
            });
        }
        dto.setRoles(roles);

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
        dto.setAuth0Id(usuario.getAuth0Id());
        dto.setImagenPerfil(usuario.getImagenPerfil());

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
