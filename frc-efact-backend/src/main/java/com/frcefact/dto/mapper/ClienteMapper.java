package com.frcefact.dto.mapper;

import com.frcefact.dto.ClienteDto;
import com.frcefact.model.Cliente;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre Cliente y ClienteDto.
 */
@Component
public class ClienteMapper {

    /**
     * Convierte una entidad Cliente a ClienteDto.
     */
    public ClienteDto toDto(Cliente cliente) {
        if (cliente == null) {
            return null;
        }

        ClienteDto dto = new ClienteDto();
        dto.setId(cliente.getId());
        dto.setNombre(cliente.getNombre());
        dto.setRazonSocial(cliente.getRazonSocial());
        dto.setRuc(cliente.getRuc());
        dto.setDireccion(cliente.getDireccion());
        dto.setTelefono(cliente.getTelefono());
        dto.setEmail(cliente.getEmail());
        dto.setTributa(cliente.getTributa());
        dto.setTipoContribuyente(cliente.getTipoContribuyente());
        dto.setActivo(cliente.getActivo());

        if (cliente.getEmpresa() != null) {
            dto.setEmpresaId(cliente.getEmpresa().getId());
        }

        return dto;
    }

    /**
     * Convierte un ClienteDto a entidad Cliente.
     * No asigna la empresa, debe hacerse en el servicio.
     */
    public Cliente toEntity(ClienteDto dto) {
        if (dto == null) {
            return null;
        }

        Cliente cliente = new Cliente();
        cliente.setId(dto.getId());
        cliente.setNombre(dto.getNombre());
        cliente.setRazonSocial(dto.getRazonSocial());
        cliente.setRuc(dto.getRuc());
        cliente.setDireccion(dto.getDireccion());
        cliente.setTelefono(dto.getTelefono());
        cliente.setEmail(dto.getEmail());
        cliente.setTributa(dto.getTributa());
        cliente.setTipoContribuyente(dto.getTipoContribuyente());
        cliente.setActivo(dto.getActivo());

        return cliente;
    }

    /**
     * Actualiza una entidad Cliente existente con datos del DTO.
     */
    public void updateEntityFromDto(ClienteDto dto, Cliente cliente) {
        if (dto == null || cliente == null) {
            return;
        }

        cliente.setNombre(dto.getNombre());
        cliente.setRazonSocial(dto.getRazonSocial());
        cliente.setRuc(dto.getRuc());
        cliente.setDireccion(dto.getDireccion());
        cliente.setTelefono(dto.getTelefono());
        cliente.setEmail(dto.getEmail());
        cliente.setTributa(dto.getTributa());
        cliente.setTipoContribuyente(dto.getTipoContribuyente());
    }
}
