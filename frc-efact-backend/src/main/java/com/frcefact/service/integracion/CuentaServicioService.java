package com.frcefact.service.integracion;

import com.frcefact.dto.integracion.CuentaServicioDto;
import com.frcefact.dto.integracion.CuentaServicioRequest;
import com.frcefact.exception.ResourceNotFoundException;
import com.frcefact.model.CuentaServicio;
import com.frcefact.model.Empresa;
import com.frcefact.repository.CuentaServicioRepository;
import com.frcefact.repository.EmpresaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Alta, modificación y consulta de cuentas de servicio. */
@Service
public class CuentaServicioService {

    private final CuentaServicioRepository repository;
    private final EmpresaRepository empresaRepository;

    public CuentaServicioService(CuentaServicioRepository repository, EmpresaRepository empresaRepository) {
        this.repository = repository;
        this.empresaRepository = empresaRepository;
    }

    @Transactional(readOnly = true)
    public List<CuentaServicioDto> listar() {
        return repository.findAll(Sort.by("nombre")).stream().map(CuentaServicioDto::desde).toList();
    }

    @Transactional(readOnly = true)
    public CuentaServicioDto obtener(Long id) {
        return CuentaServicioDto.desde(buscar(id));
    }

    @Transactional
    public CuentaServicioDto crear(CuentaServicioRequest req) {
        CuentaServicio c = new CuentaServicio();
        aplicar(c, req);
        return CuentaServicioDto.desde(repository.save(c));
    }

    @Transactional
    public CuentaServicioDto actualizar(Long id, CuentaServicioRequest req) {
        CuentaServicio c = buscar(id);
        aplicar(c, req);
        return CuentaServicioDto.desde(repository.save(c));
    }

    private void aplicar(CuentaServicio c, CuentaServicioRequest req) {
        c.setNombre(req.nombre().trim());
        c.setRuc(req.ruc() != null && !req.ruc().isBlank() ? req.ruc().trim() : null);
        c.setEmailContacto(req.emailContacto());
        c.setObservacion(req.observacion());
        if (req.activa() != null) {
            c.setActiva(req.activa());
        }
        if (req.empresaId() != null) {
            Empresa empresa = empresaRepository.findById(req.empresaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada: " + req.empresaId()));
            c.setEmpresa(empresa);
        } else {
            c.setEmpresa(null);
        }
    }

    private CuentaServicio buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta de servicio no encontrada: " + id));
    }
}
