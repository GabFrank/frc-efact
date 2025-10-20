package com.frcefact.dto.mapper;

import com.frcefact.dto.ActividadEconomicaDto;
import com.frcefact.dto.EmpresaDto;
import com.frcefact.model.Barrio;
import com.frcefact.model.Ciudad;
import com.frcefact.model.Empresa;
import com.frcefact.repository.BarrioRepository;
import com.frcefact.repository.CiudadRepository;
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
    private final CiudadRepository ciudadRepository;
    private final BarrioRepository barrioRepository;

    public EmpresaMapper(EncryptionService encryptionService,
            CiudadRepository ciudadRepository,
            BarrioRepository barrioRepository) {
        this.encryptionService = encryptionService;
        this.ciudadRepository = ciudadRepository;
        this.barrioRepository = barrioRepository;
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
        dto.setTipoContribuyente(empresa.getTipoContribuyente());
        dto.setNombreFantasia(empresa.getNombreFantasia());
        dto.setEmail(empresa.getEmail());
        dto.setTelefono(empresa.getTelefono());
        dto.setDireccion(empresa.getDireccion());
        dto.setTipoSociedad(empresa.getTipoSociedad());
        dto.setActivo(empresa.getActivo());

        // Domicilio fiscal - usar IDs directamente
        if (empresa.getCiudad() != null) {
            dto.setCiudadId(empresa.getCiudad().getId());
        }
        if (empresa.getBarrio() != null) {
            dto.setBarrioId(empresa.getBarrio().getId());
        }
        dto.setDomicilioFiscalDireccion(empresa.getDomicilioFiscalDireccion());

        // Actividad económica
        if (empresa.getCodActividadEconomicaPrincipal() != null) {
            ActividadEconomicaDto actividad = new ActividadEconomicaDto();
            actividad.setCodigoPrincipal(empresa.getCodActividadEconomicaPrincipal());
            actividad.setDescripcionPrincipal(empresa.getDescActividadEconomicaPrincipal());

            // Convertir listas de texto separado por comas a List
            if (empresa.getListCodigoActividadEconomicaSecundaria() != null) {
                actividad.setCodigosSecundarios(
                        Arrays.asList(empresa.getListCodigoActividadEconomicaSecundaria().split(",")));
            }
            if (empresa.getListDescripcionActividadEconomicaSecundaria() != null) {
                actividad.setDescripcionesSecundarias(
                        Arrays.asList(empresa.getListDescripcionActividadEconomicaSecundaria().split(",")));
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
        empresa.setTipoContribuyente(dto.getTipoContribuyente());
        empresa.setNombreFantasia(dto.getNombreFantasia());
        empresa.setEmail(dto.getEmail());
        empresa.setTelefono(dto.getTelefono());
        empresa.setDireccion(dto.getDireccion());
        empresa.setTipoSociedad(dto.getTipoSociedad());
        empresa.setActivo(dto.getActivo() != null ? dto.getActivo() : true);

        // Domicilio fiscal - buscar Ciudad por ID
        if (dto.getCiudadId() != null) {
            Ciudad ciudad = ciudadRepository.findById(dto.getCiudadId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Ciudad no encontrada con ID: " + dto.getCiudadId()));
            empresa.setCiudad(ciudad);
        }

        // Barrio opcional
        if (dto.getBarrioId() != null) {
            Barrio barrio = barrioRepository.findById(dto.getBarrioId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Barrio no encontrado con ID: " + dto.getBarrioId()));
            empresa.setBarrio(barrio);
        }

        empresa.setDomicilioFiscalDireccion(dto.getDomicilioFiscalDireccion());

        // Actividad económica
        if (dto.getActividadEconomica() != null) {
            ActividadEconomicaDto actividad = dto.getActividadEconomica();
            empresa.setCodActividadEconomicaPrincipal(actividad.getCodigoPrincipal());
            empresa.setDescActividadEconomicaPrincipal(actividad.getDescripcionPrincipal());

            // Convertir List a texto separado por comas
            if (actividad.getCodigosSecundarios() != null && !actividad.getCodigosSecundarios().isEmpty()) {
                empresa.setListCodigoActividadEconomicaSecundaria(
                        String.join(",", actividad.getCodigosSecundarios()));
            }
            if (actividad.getDescripcionesSecundarias() != null && !actividad.getDescripcionesSecundarias().isEmpty()) {
                empresa.setListDescripcionActividadEconomicaSecundaria(
                        String.join(",", actividad.getDescripcionesSecundarias()));
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
