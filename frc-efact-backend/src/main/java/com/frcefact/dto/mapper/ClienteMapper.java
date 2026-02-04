package com.frcefact.dto.mapper;

import com.frcefact.dto.ClienteDto;
import com.frcefact.model.Cliente;
import com.frcefact.model.TipoClienteSifen;
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
        dto.setNumeroCasa(cliente.getNumeroCasa());
        dto.setTelefono(cliente.getTelefono());
        dto.setCelular(cliente.getCelular());
        dto.setEmail(cliente.getEmail());
        
        // Mapear tipoClienteSifen
        if (cliente.getTipoClienteSifen() != null) {
            dto.setTipoClienteSifen(cliente.getTipoClienteSifen().name());
        }
        
        // Campos legacy para compatibilidad
        dto.setTributa(cliente.getTributa());
        dto.setTipoContribuyente(cliente.getTipoContribuyente());
        
        // Relaciones geográficas
        if (cliente.getPais() != null) {
            dto.setPaisId(cliente.getPais().getId());
        }
        if (cliente.getCiudad() != null) {
            dto.setCiudadId(cliente.getCiudad().getId());
        }
        
        dto.setActivo(cliente.getActivo());

        if (cliente.getEmpresa() != null) {
            dto.setEmpresaId(cliente.getEmpresa().getId());
        }

        return dto;
    }

    /**
     * Convierte un ClienteDto a entidad Cliente.
     * No asigna la empresa, país ni ciudad completas, solo crea referencias proxy con IDs.
     * El servicio debe cargar las entidades completas.
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
        cliente.setNumeroCasa(dto.getNumeroCasa());
        cliente.setTelefono(dto.getTelefono());
        cliente.setCelular(dto.getCelular());
        cliente.setEmail(dto.getEmail());
        
        // Mapear tipoClienteSifen desde String a Enum
        if (dto.getTipoClienteSifen() != null && !dto.getTipoClienteSifen().isEmpty()) {
            try {
                cliente.setTipoClienteSifen(TipoClienteSifen.valueOf(dto.getTipoClienteSifen()));
            } catch (IllegalArgumentException e) {
                // Si el valor no es válido, se mantiene null
                // El servicio puede usar los campos legacy como fallback
            }
        }
        
        // Campos legacy para compatibilidad
        cliente.setTributa(dto.getTributa());
        cliente.setTipoContribuyente(dto.getTipoContribuyente());
        
        cliente.setActivo(dto.getActivo());

        // Crear referencias proxy para relaciones geográficas usando solo IDs
        // El servicio cargará las entidades completas
        if (dto.getPaisId() != null) {
            com.frcefact.model.Pais pais = new com.frcefact.model.Pais();
            pais.setId(dto.getPaisId());
            cliente.setPais(pais);
        }
        
        if (dto.getCiudadId() != null) {
            com.frcefact.model.Ciudad ciudad = new com.frcefact.model.Ciudad();
            ciudad.setId(dto.getCiudadId());
            cliente.setCiudad(ciudad);
        }

        return cliente;
    }

    /**
     * Actualiza una entidad Cliente existente con datos del DTO.
     * Establece referencias proxy para relaciones geográficas usando solo IDs.
     * El servicio debe cargar las entidades completas.
     */
    public void updateEntityFromDto(ClienteDto dto, Cliente cliente) {
        if (dto == null || cliente == null) {
            return;
        }

        cliente.setNombre(dto.getNombre());
        cliente.setRazonSocial(dto.getRazonSocial());
        cliente.setRuc(dto.getRuc());
        cliente.setDireccion(dto.getDireccion());
        cliente.setNumeroCasa(dto.getNumeroCasa());
        cliente.setTelefono(dto.getTelefono());
        cliente.setCelular(dto.getCelular());
        cliente.setEmail(dto.getEmail());
        
        // Mapear tipoClienteSifen desde String a Enum
        if (dto.getTipoClienteSifen() != null && !dto.getTipoClienteSifen().isEmpty()) {
            try {
                cliente.setTipoClienteSifen(TipoClienteSifen.valueOf(dto.getTipoClienteSifen()));
            } catch (IllegalArgumentException e) {
                // Si el valor no es válido, se mantiene el valor actual
            }
        } else if (dto.getTributa() != null || dto.getTipoContribuyente() != null) {
            // Fallback a campos legacy si no se proporciona tipoClienteSifen
            cliente.setTributa(dto.getTributa());
            cliente.setTipoContribuyente(dto.getTipoContribuyente());
        }
        
        // Actualizar referencias geográficas usando solo IDs
        // El servicio cargará las entidades completas
        // Solo actualizar si el DTO proporciona un valor
        if (dto.getPaisId() != null) {
            com.frcefact.model.Pais pais = new com.frcefact.model.Pais();
            pais.setId(dto.getPaisId());
            cliente.setPais(pais);
        }
        // Si el DTO no tiene paisId, mantener el existente (no cambiar)
        
        if (dto.getCiudadId() != null) {
            com.frcefact.model.Ciudad ciudad = new com.frcefact.model.Ciudad();
            ciudad.setId(dto.getCiudadId());
            cliente.setCiudad(ciudad);
        }
        // Si el DTO no tiene ciudadId, mantener el existente (no cambiar)
    }
}
