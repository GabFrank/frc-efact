package com.frcefact.dto.mapper;

import com.frcefact.dto.TimbradoDto;
import com.frcefact.model.Empresa;
import com.frcefact.model.Timbrado;
import com.frcefact.service.EncryptionService;
import org.springframework.stereotype.Component;

/**
 * Mapper simplificado para convertir entre Timbrado y TimbradoDto.
 * Solo mapea campos específicos del timbrado, los datos de empresa se obtienen de la relación.
 */
@Component
public class TimbradoMapper {

    private final EncryptionService encryptionService;

    public TimbradoMapper(EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }

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
        dto.setNumero(timbrado.getNumero());
        dto.setIsElectronico(timbrado.getIsElectronico());
        
        // Desencriptar CSC si existe para enviarlo al frontend
        if (Boolean.TRUE.equals(timbrado.getIsElectronico()) && timbrado.getCscEncrypted() != null) {
            try {
                String cscDesencriptado = encryptionService.decrypt(timbrado.getCscEncrypted());
                dto.setCsc(cscDesencriptado);
            } catch (Exception e) {
                // Si hay error al desencriptar, no incluir CSC
                dto.setCsc(null);
            }
        }
        
        dto.setFechaInicio(timbrado.getFechaInicio());
        dto.setFechaFin(timbrado.getFechaFin());
        dto.setActivo(timbrado.getActivo());
        
        // Campos calculados
        dto.setVigente(timbrado.isVigente());
        dto.setDiasRestantes(timbrado.getDiasRestantes());
        
        // Información de empresa para mostrar (solo lectura)
        if (timbrado.getEmpresa() != null) {
            dto.setRazonSocial(timbrado.getEmpresa().getRazonSocial());
            dto.setRuc(timbrado.getEmpresa().getRuc());
        }

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

        timbrado.setNumero(dto.getNumero());
        timbrado.setIsElectronico(dto.getIsElectronico());
        // El CSC se maneja en el servicio para encriptación
        if (dto.getCsc() != null) {
            timbrado.setCscEncrypted(dto.getCsc());
        }
        timbrado.setFechaInicio(dto.getFechaInicio());
        timbrado.setFechaFin(dto.getFechaFin());
        
        if (dto.getActivo() != null) {
            timbrado.setActivo(dto.getActivo());
        }

        return timbrado;
    }
}