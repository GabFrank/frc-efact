package com.frcefact.service;

import com.frcefact.annotation.Auditable;
import com.frcefact.model.AccionEnum;
import com.frcefact.model.Empresa;
import com.frcefact.model.Timbrado;
import com.frcefact.repository.EmpresaRepository;
import com.frcefact.repository.TimbradoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio para gestión de timbrados fiscales.
 * Implementa CRUD completo con validación de fechas y encriptación de CSC.
 */
@Service
@Transactional
public class TimbradoService {

    private final TimbradoRepository timbradoRepository;
    private final EmpresaRepository empresaRepository;
    private final EncryptionService encryptionService;
    private final EmpresaSecurityService empresaSecurityService;

    public TimbradoService(
            TimbradoRepository timbradoRepository,
            EmpresaRepository empresaRepository,
            EncryptionService encryptionService,
            EmpresaSecurityService empresaSecurityService) {
        this.timbradoRepository = timbradoRepository;
        this.empresaRepository = empresaRepository;
        this.encryptionService = encryptionService;
        this.empresaSecurityService = empresaSecurityService;
    }

    /**
     * Crea un nuevo timbrado.
     * Valida fechas y encripta CSC si es electrónico.
     */
    @Auditable(entidad = "Timbrado", accion = AccionEnum.CREATE)
    public Timbrado crear(Timbrado timbrado) {
        // Validar fechas
        validarFechas(timbrado.getFechaInicio(), timbrado.getFechaFin());

        // Verificar que la empresa existe
        Empresa empresa = empresaRepository.findById(timbrado.getEmpresa().getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Empresa no encontrada con ID: " + timbrado.getEmpresa().getId()));

        // Verificar permisos
        empresaSecurityService.verificarAccesoEscritura(empresa.getId());

        timbrado.setEmpresa(empresa);

        // Encriptar CSC si es electrónico y tiene CSC
        if (Boolean.TRUE.equals(timbrado.getIsElectronico()) && timbrado.getCscEncrypted() != null) {
            String cscPlain = timbrado.getCscEncrypted();
            String cscEncrypted = encryptionService.encrypt(cscPlain);
            timbrado.setCscEncrypted(cscEncrypted);
        }

        return timbradoRepository.save(timbrado);
    }

    /**
     * Actualiza un timbrado existente.
     */
    @Auditable(entidad = "Timbrado", accion = AccionEnum.UPDATE)
    public Timbrado actualizar(Long id, Timbrado timbradoActualizado) {
        Timbrado timbradoExistente = timbradoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Timbrado no encontrado con ID: " + id));

        // Verificar permisos
        empresaSecurityService.verificarAccesoEscritura(timbradoExistente.getEmpresa().getId());

        // Validar fechas
        validarFechas(timbradoActualizado.getFechaInicio(), timbradoActualizado.getFechaFin());

        // Actualizar campos
        timbradoExistente.setRazonSocial(timbradoActualizado.getRazonSocial());
        timbradoExistente.setRuc(timbradoActualizado.getRuc());
        timbradoExistente.setNumero(timbradoActualizado.getNumero());
        timbradoExistente.setIsElectronico(timbradoActualizado.getIsElectronico());
        timbradoExistente.setFechaInicio(timbradoActualizado.getFechaInicio());
        timbradoExistente.setFechaFin(timbradoActualizado.getFechaFin());
        timbradoExistente.setEmail(timbradoActualizado.getEmail());
        timbradoExistente.setTipoSociedad(timbradoActualizado.getTipoSociedad());
        timbradoExistente.setDomicilioFiscalDepartamento(timbradoActualizado.getDomicilioFiscalDepartamento());
        timbradoExistente.setDomicilioFiscalCiudad(timbradoActualizado.getDomicilioFiscalCiudad());
        timbradoExistente.setDomicilioFiscalCodigoCiudad(timbradoActualizado.getDomicilioFiscalCodigoCiudad());
        timbradoExistente.setDomicilioFiscalLocalidad(timbradoActualizado.getDomicilioFiscalLocalidad());
        timbradoExistente.setDomicilioFiscalBarrio(timbradoActualizado.getDomicilioFiscalBarrio());
        timbradoExistente.setDomicilioFiscalDireccion(timbradoActualizado.getDomicilioFiscalDireccion());
        timbradoExistente.setTelefono(timbradoActualizado.getTelefono());
        timbradoExistente.setCodActividadEconomicaPrincipal(timbradoActualizado.getCodActividadEconomicaPrincipal());
        timbradoExistente.setDescActividadEconomicaPrincipal(timbradoActualizado.getDescActividadEconomicaPrincipal());
        timbradoExistente.setListCodigoActividadEconomicaSecundaria(timbradoActualizado.getListCodigoActividadEconomicaSecundaria());
        timbradoExistente.setListDescripcionActividadEconomicaSecundaria(timbradoActualizado.getListDescripcionActividadEconomicaSecundaria());

        // Encriptar CSC si cambió y es electrónico
        if (Boolean.TRUE.equals(timbradoActualizado.getIsElectronico()) 
                && timbradoActualizado.getCscEncrypted() != null
                && !timbradoActualizado.getCscEncrypted().equals(timbradoExistente.getCscEncrypted())) {
            String cscPlain = timbradoActualizado.getCscEncrypted();
            String cscEncrypted = encryptionService.encrypt(cscPlain);
            timbradoExistente.setCscEncrypted(cscEncrypted);
        }

        return timbradoRepository.save(timbradoExistente);
    }

    /**
     * Obtiene un timbrado por ID.
     */
    @Transactional(readOnly = true)
    public Timbrado obtenerPorId(Long id) {
        Timbrado timbrado = timbradoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Timbrado no encontrado con ID: " + id));

        // Verificar permisos de lectura
        empresaSecurityService.verificarAccesoLectura(timbrado.getEmpresa().getId());

        return timbrado;
    }

    /**
     * Lista todos los timbrados de una empresa.
     */
    @Transactional(readOnly = true)
    public List<Timbrado> listarPorEmpresa(Long empresaId) {
        // Verificar permisos
        empresaSecurityService.verificarAccesoLectura(empresaId);

        return timbradoRepository.findByEmpresaIdOrderByFechaFinDesc(empresaId);
    }

    /**
     * Lista timbrados activos de una empresa.
     */
    @Transactional(readOnly = true)
    public List<Timbrado> listarActivosPorEmpresa(Long empresaId) {
        // Verificar permisos
        empresaSecurityService.verificarAccesoLectura(empresaId);

        return timbradoRepository.findByEmpresaIdAndActivoTrue(empresaId);
    }

    /**
     * Desactiva un timbrado (soft delete).
     */
    public void desactivar(Long id) {
        Timbrado timbrado = timbradoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Timbrado no encontrado con ID: " + id));

        // Verificar permisos
        empresaSecurityService.verificarAccesoEscritura(timbrado.getEmpresa().getId());

        timbrado.setActivo(false);
        timbradoRepository.save(timbrado);
    }

    /**
     * Verifica si un timbrado está vigente en la fecha actual.
     */
    @Transactional(readOnly = true)
    public boolean verificarVigencia(Long id) {
        Timbrado timbrado = timbradoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Timbrado no encontrado con ID: " + id));

        // Verificar permisos
        empresaSecurityService.verificarAccesoLectura(timbrado.getEmpresa().getId());

        return timbrado.isVigente();
    }

    /**
     * Obtiene timbrados vigentes de una empresa.
     */
    @Transactional(readOnly = true)
    public List<Timbrado> obtenerTimbradosVigentes(Long empresaId) {
        // Verificar permisos
        empresaSecurityService.verificarAccesoLectura(empresaId);

        return timbradoRepository.findTimbradosVigentes(empresaId, LocalDate.now());
    }

    /**
     * Obtiene timbrados electrónicos vigentes de una empresa.
     */
    @Transactional(readOnly = true)
    public List<Timbrado> obtenerTimbradosElectronicosVigentes(Long empresaId) {
        // Verificar permisos
        empresaSecurityService.verificarAccesoLectura(empresaId);

        return timbradoRepository.findTimbradosElectronicosVigentes(empresaId, LocalDate.now());
    }

    /**
     * Obtiene timbrados que están por vencer en los próximos días.
     */
    @Transactional(readOnly = true)
    public List<Timbrado> obtenerTimbradosPorVencer(Long empresaId, int diasAnticipacion) {
        // Verificar permisos
        empresaSecurityService.verificarAccesoLectura(empresaId);

        LocalDate fechaActual = LocalDate.now();
        LocalDate fechaLimite = fechaActual.plusDays(diasAnticipacion);

        return timbradoRepository.findTimbradosPorVencer(empresaId, fechaActual, fechaLimite);
    }

    /**
     * Desencripta el CSC de un timbrado electrónico.
     * Solo para uso interno del sistema.
     */
    public String obtenerCscDesencriptado(Long id) {
        Timbrado timbrado = timbradoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Timbrado no encontrado con ID: " + id));

        // Verificar permisos
        empresaSecurityService.verificarAccesoLectura(timbrado.getEmpresa().getId());

        if (!Boolean.TRUE.equals(timbrado.getIsElectronico()) || timbrado.getCscEncrypted() == null) {
            return null;
        }

        return encryptionService.decrypt(timbrado.getCscEncrypted());
    }

    /**
     * Valida que las fechas sean coherentes.
     */
    private void validarFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            throw new IllegalArgumentException("Las fechas de inicio y fin son requeridas");
        }

        if (fechaFin.isBefore(fechaInicio)) {
            throw new IllegalArgumentException(
                    "La fecha de fin no puede ser anterior a la fecha de inicio");
        }

        if (fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException(
                    "La fecha de inicio no puede ser posterior a la fecha de fin");
        }
    }
}
