package com.frcefact.sifen.config;

import com.frcefact.model.Empresa;
import com.frcefact.repository.EmpresaRepository;
import com.frcefact.repository.TimbradoRepository;
import com.frcefact.service.CertificadoService;
import com.frcefact.service.EncryptionService;
import com.roshka.sifen.core.SifenConfig;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Regresión: con ambiente PROD, jsifenlib rechaza cualquier operación (también siConsDE) si la
 * config tiene el CSC de desarrollo que pone por defecto el constructor sin CSC.
 */
class SifenConfigFactoryConsultaTest {

    @Test
    void configDeConsultaEnProdNoUsaElCscDeDesarrollo() {
        EmpresaRepository empresaRepository = mock(EmpresaRepository.class);
        EncryptionService encryptionService = mock(EncryptionService.class);
        CertificadoService certificadoService = mock(CertificadoService.class);

        Empresa empresa = new Empresa();
        empresa.setSifenAmbiente("PROD");
        empresa.setCertificadoPath("empresa_1.pfx");
        empresa.setCertificadoPasswordEncrypted("cifrado");
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(encryptionService.decrypt(anyString())).thenReturn("pwd");
        when(certificadoService.obtenerPathAbsoluto(anyString())).thenReturn(Path.of("/tmp/empresa_1.pfx"));

        SifenConfigFactory factory = new SifenConfigFactory(empresaRepository, mock(TimbradoRepository.class),
                encryptionService, certificadoService, mock(SifenProperties.class));

        SifenConfig config = factory.buildParaConsulta(1L);

        assertThat(config.getAmbiente()).isEqualTo(SifenConfig.TipoAmbiente.PROD);
        assertThat(config.getCSC()).isNotIn("ABCD0000000000000000000000000000", "EFGH0000000000000000000000000000");
        assertThat(config.getIdCSC()).isEqualTo(SifenConfigFactory.CSC_ID_SOLO_CONSULTA);
    }
}
