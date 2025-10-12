package com.frcefact.dto.mapper;

import com.frcefact.dto.TimbradoDto;
import com.frcefact.model.Empresa;
import com.frcefact.model.Timbrado;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre Timbrado y TimbradoDto.
 */
@Component
public class TimbradoMapper {

    /**
     * Convierte una entidad Timbrado a TimbradoDto.
     */
    public TimbradoDto toDto(Timbrado timbrado) {
        if (timbrado == null) {
            return null;
        }

        TimbradoDto dto = new TimbradoDto();
        dto.setId(timbrado.getId());
        dto.setEmpresaId(timbrado.getEmpresa().getId());
        dto.setRazonSocial(timbrado.getRazonSocial());
        dto.setRuc(timbrado.getRuc());
        dto.setNumero(timbrado.getNumero());
        dto.setIsElectronico(timbrado.getIsElectronico());
        // No incluir CSC encriptado en el DTO de respuesta por seguridad
        dto.setFechaInicio(timbrado.getFechaInicio());
        dto.setFechaFin(timbrado.getFechaFin());
        dto.setEmail(timbrado.getEmail());
        dto.setTipoSociedad(timbrado.getTipoSociedad());
        dto.setDomicilioFiscalDepartamento(timbrado.getDomicilioFiscalDepartamento());
        dto.setDomicilioFiscalCiudad(timbrado.getDomicilioFiscalCiudad());
        dto.setDomicilioFiscalCodigoCiudad(timbrado.getDomicilioFiscalCodigoCiudad());
        dto.setDomicilioFiscalLocalidad(timbrado.getDomicilioFiscalLocalidad());
        dto.setDomicilioFiscalBarrio(timbrado.getDomicilioFiscalBarrio());
        dto.setDomicilioFiscalDireccion(timbrado.getDomicilioFiscalDireccion());
        dto.setTelefono(timbrado.getTelefono());
        dto.setCodActividadEconomicaPrincipal(timbrado.getCodActividadEconomicaPrincipal());
        dto.setDescActividadEconomicaPrincipal(timbrado.getDescActividadEconomicaPrincipal());
        dto.setListCodigoActividadEconomicaSecundaria(timbrado.getListCodigoActividadEconomicaSecundaria());
        dto.setListDescripcionActividadEconomicaSecundaria(timbrado.getListDescripcionActividadEconomicaSecundaria());
        dto.setActivo(timbrado.getActivo());
        dto.setVigente(timbrado.isVigente());
        dto.setDiasRestantes(timbrado.getDiasRestantes());

        return dto;
    }

    /**
     * Convierte un TimbradoDto a entidad Timbrado.
     */
    public Timbrado toEntity(TimbradoDto dto) {
        if (dto == null) {
            return null;
        }

        Timbrado timbrado = new Timbrado();
        timbrado.setId(dto.getId());
        
        // La empresa se establece en el servicio
        if (dto.getEmpresaId() != null) {
            Empresa empresa = new Empresa();
            empresa.setId(dto.getEmpresaId());
            timbrado.setEmpresa(empresa);
        }

        timbrado.setRazonSocial(dto.getRazonSocial());
        timbrado.setRuc(dto.getRuc());
        timbrado.setNumero(dto.getNumero());
        timbrado.setIsElectronico(dto.getIsElectronico());
        // El CSC se maneja en el servicio para encriptación
        timbrado.setCscEncrypted(dto.getCsc());
        timbrado.setFechaInicio(dto.getFechaInicio());
        timbrado.setFechaFin(dto.getFechaFin());
        timbrado.setEmail(dto.getEmail());
        timbrado.setTipoSociedad(dto.getTipoSociedad());
        timbrado.setDomicilioFiscalDepartamento(dto.getDomicilioFiscalDepartamento());
        timbrado.setDomicilioFiscalCiudad(dto.getDomicilioFiscalCiudad());
        timbrado.setDomicilioFiscalCodigoCiudad(dto.getDomicilioFiscalCodigoCiudad());
        timbrado.setDomicilioFiscalLocalidad(dto.getDomicilioFiscalLocalidad());
        timbrado.setDomicilioFiscalBarrio(dto.getDomicilioFiscalBarrio());
        timbrado.setDomicilioFiscalDireccion(dto.getDomicilioFiscalDireccion());
        timbrado.setTelefono(dto.getTelefono());
        timbrado.setCodActividadEconomicaPrincipal(dto.getCodActividadEconomicaPrincipal());
        timbrado.setDescActividadEconomicaPrincipal(dto.getDescActividadEconomicaPrincipal());
        timbrado.setListCodigoActividadEconomicaSecundaria(dto.getListCodigoActividadEconomicaSecundaria());
        timbrado.setListDescripcionActividadEconomicaSecundaria(dto.getListDescripcionActividadEconomicaSecundaria());
        
        if (dto.getActivo() != null) {
            timbrado.setActivo(dto.getActivo());
        }

        return timbrado;
    }
}
