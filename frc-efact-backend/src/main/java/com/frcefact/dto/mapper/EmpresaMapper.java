package com.frcefact.dto.mapper;

import com.frcefact.dto.ActividadEconomicaDto;
import com.frcefact.dto.DomicilioFiscalDto;
import com.frcefact.dto.EmpresaDto;
import com.frcefact.model.Empresa;
import com.frcefact.service.EncryptionService;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre Empresa y EmpresaDto.
 */
@Component
public class EmpresaMapper {

    private final EncryptionService encryptionService;

    public EmpresaMapper(EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }

    /**
     * Convierte una entidad Empresa a EmpresaDto.
     *
     * @param empresa la entidad
     * @return el DTO
     */
    public EmpresaDto toDto(Empresa empresa) {
        if (empresa == null) {
            return null;
        }

        EmpresaDto dto = new EmpresaDto();
        dto.setId(empresa.getId());
        dto.setRazonSocial(empresa.getRazonSocial());
        dto.setRuc(empresa.getRuc());
        dto.setNombreFantasia(empresa.getNombreFantasia());
        dto.setEmail(empresa.getEmail());
        dto.setTelefono(empresa.getTelefono());
        dto.setDireccion(empresa.getDireccion());
        dto.setTipoSociedad(empresa.getTipoSociedad());
        dto.setActivo(empresa.getActivo());

        // Domicilio fiscal
        if (empresa.getDomicilioFiscalDepartamento() != null) {
            DomicilioFiscalDto domicilio = new DomicilioFiscalDto();
            domicilio.setDepartamento(empresa.getDomicilioFiscalDepartamento());
            domicilio.setCiudad(empresa.getDomicilioFiscalCiudad());
            domicilio.setCodigoCiudad(empresa.getDomicilioFiscalCodigoCiudad());
            domicilio.setLocalidad(empresa.getDomicilioFiscalLocalidad());
            domicilio.setBarrio(empresa.getDomicilioFiscalBarrio());
            domicilio.setDireccion(empresa.getDomicilioFiscalDireccion());
            dto.setDomicilioFiscal(domicilio);
        }

        // Actividad económica
        if (empresa.getCodActividadEconomicaPrincipal() != null) {
            ActividadEconomicaDto actividad = new ActividadEconomicaDto();
            actividad.setCodigoPrincipal(empresa.getCodActividadEconomicaPrincipal());
            actividad.setDescripcionPrincipal(empresa.getDescActividadEconomicaPrincipal());
            
            // Convertir listas de texto separado por comas a List
            if (empresa.getListCodigoActividadEconomicaSecundaria() != null) {
                actividad.setCodigosSecundarios(
                    Arrays.asList(empresa.getListCodigoActividadEconomicaSecundaria().split(","))
                );
            }
            if (empresa.getListDescripcionActividadEconomicaSecundaria() != null) {
                actividad.setDescripcionesSecundarias(
                    Arrays.asList(empresa.getListDescripcionActividadEconomicaSecundaria().split(","))
                );
            }
            
            dto.setActividadEconomica(actividad);
        }

        // Certificado (no incluir password por seguridad)
        dto.setCertificadoPath(empresa.getCertificadoPath());
        dto.setCertificadoFechaExpiracion(empresa.getCertificadoFechaExpiracion());

        return dto;
    }

    /**
     * Convierte un EmpresaDto a entidad Empresa.
     *
     * @param dto el DTO
     * @return la entidad
     */
    public Empresa toEntity(EmpresaDto dto) {
        if (dto == null) {
            return null;
        }

        Empresa empresa = new Empresa();
        empresa.setId(dto.getId());
        empresa.setRazonSocial(dto.getRazonSocial());
        empresa.setRuc(dto.getRuc());
        empresa.setNombreFantasia(dto.getNombreFantasia());
        empresa.setEmail(dto.getEmail());
        empresa.setTelefono(dto.getTelefono());
        empresa.setDireccion(dto.getDireccion());
        empresa.setTipoSociedad(dto.getTipoSociedad());
        empresa.setActivo(dto.getActivo() != null ? dto.getActivo() : true);

        // Domicilio fiscal
        if (dto.getDomicilioFiscal() != null) {
            DomicilioFiscalDto domicilio = dto.getDomicilioFiscal();
            empresa.setDomicilioFiscalDepartamento(domicilio.getDepartamento());
            empresa.setDomicilioFiscalCiudad(domicilio.getCiudad());
            empresa.setDomicilioFiscalCodigoCiudad(domicilio.getCodigoCiudad());
            empresa.setDomicilioFiscalLocalidad(domicilio.getLocalidad());
            empresa.setDomicilioFiscalBarrio(domicilio.getBarrio());
            empresa.setDomicilioFiscalDireccion(domicilio.getDireccion());
        }

        // Actividad económica
        if (dto.getActividadEconomica() != null) {
            ActividadEconomicaDto actividad = dto.getActividadEconomica();
            empresa.setCodActividadEconomicaPrincipal(actividad.getCodigoPrincipal());
            empresa.setDescActividadEconomicaPrincipal(actividad.getDescripcionPrincipal());
            
            // Convertir List a texto separado por comas
            if (actividad.getCodigosSecundarios() != null && !actividad.getCodigosSecundarios().isEmpty()) {
                empresa.setListCodigoActividadEconomicaSecundaria(
                    String.join(",", actividad.getCodigosSecundarios())
                );
            }
            if (actividad.getDescripcionesSecundarias() != null && !actividad.getDescripcionesSecundarias().isEmpty()) {
                empresa.setListDescripcionActividadEconomicaSecundaria(
                    String.join(",", actividad.getDescripcionesSecundarias())
                );
            }
        }

        // Certificado
        empresa.setCertificadoPath(dto.getCertificadoPath());
        empresa.setCertificadoFechaExpiracion(dto.getCertificadoFechaExpiracion());
        
        // Encriptar password del certificado si se proporciona
        if (dto.getCertificadoPassword() != null && !dto.getCertificadoPassword().isEmpty()) {
            try {
                String passwordEncriptado = encryptionService.encrypt(dto.getCertificadoPassword());
                empresa.setCertificadoPasswordEncrypted(passwordEncriptado);
            } catch (Exception e) {
                throw new RuntimeException("Error al encriptar password del certificado", e);
            }
        }

        return empresa;
    }

    /**
     * Convierte una lista de entidades a lista de DTOs.
     *
     * @param empresas lista de entidades
     * @return lista de DTOs
     */
    public List<EmpresaDto> toDtoList(List<Empresa> empresas) {
        if (empresas == null) {
            return null;
        }
        return empresas.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
