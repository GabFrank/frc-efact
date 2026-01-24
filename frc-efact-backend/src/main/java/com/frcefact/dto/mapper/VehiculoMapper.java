package com.frcefact.dto.mapper;

import com.frcefact.dto.VehiculoDto;
import com.frcefact.model.Vehiculo;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre Vehiculo y VehiculoDto.
 */
@Component
public class VehiculoMapper {

    /**
     * Convierte una entidad Vehiculo a VehiculoDto.
     */
    public VehiculoDto toDto(Vehiculo vehiculo) {
        if (vehiculo == null) {
            return null;
        }

        VehiculoDto dto = new VehiculoDto();
        dto.setId(vehiculo.getId());
        dto.setMarca(vehiculo.getMarca());
        dto.setMatricula(vehiculo.getMatricula());
        dto.setActivo(vehiculo.getActivo());

        if (vehiculo.getEmpresa() != null) {
            dto.setEmpresaId(vehiculo.getEmpresa().getId());
        }

        return dto;
    }

    /**
     * Convierte un VehiculoDto a entidad Vehiculo.
     * No asigna la empresa completa, solo crea referencia proxy con ID.
     * El servicio debe cargar la entidad completa.
     */
    public Vehiculo toEntity(VehiculoDto dto) {
        if (dto == null) {
            return null;
        }

        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(dto.getId());
        vehiculo.setMarca(dto.getMarca());
        vehiculo.setMatricula(dto.getMatricula());
        vehiculo.setActivo(dto.getActivo());

        return vehiculo;
    }

    /**
     * Actualiza una entidad Vehiculo existente con datos del DTO.
     */
    public void updateEntityFromDto(VehiculoDto dto, Vehiculo vehiculo) {
        if (dto == null || vehiculo == null) {
            return;
        }

        vehiculo.setMarca(dto.getMarca());
        vehiculo.setMatricula(dto.getMatricula());
        vehiculo.setActivo(dto.getActivo());
    }
}
