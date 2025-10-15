package com.frcefact.service;

import com.frcefact.dto.geografia.*;
import com.frcefact.model.*;
import com.frcefact.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar datos geográficos de SIFEN.
 */
@Service
@Transactional(readOnly = true)
public class GeografiaService {

    private final PaisRepository paisRepository;
    private final DepartamentoRepository departamentoRepository;
    private final DistritoRepository distritoRepository;
    private final CiudadRepository ciudadRepository;
    private final BarrioRepository barrioRepository;

    public GeografiaService(PaisRepository paisRepository,
                           DepartamentoRepository departamentoRepository,
                           DistritoRepository distritoRepository,
                           CiudadRepository ciudadRepository,
                           BarrioRepository barrioRepository) {
        this.paisRepository = paisRepository;
        this.departamentoRepository = departamentoRepository;
        this.distritoRepository = distritoRepository;
        this.ciudadRepository = ciudadRepository;
        this.barrioRepository = barrioRepository;
    }

    // ========== DEPARTAMENTOS ==========

    /**
     * Obtiene todos los departamentos activos.
     */
    public List<DepartamentoDto> obtenerDepartamentos() {
        return departamentoRepository.findByActivoTrue().stream()
                .map(this::convertirADepartamentoDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca departamentos por texto (código o nombre).
     */
    public List<DepartamentoDto> buscarDepartamentos(String busqueda) {
        return departamentoRepository.findByCodigoOrNombreContainingIgnoreCase(busqueda).stream()
                .map(this::convertirADepartamentoDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un departamento por código.
     */
    public DepartamentoDto obtenerDepartamentoPorCodigo(String codigo) {
        return departamentoRepository.findByCodigo(codigo)
                .map(this::convertirADepartamentoDto)
                .orElse(null);
    }

    // ========== DISTRITOS ==========

    /**
     * Obtiene todos los distritos de un departamento.
     */
    public List<DistritoDto> obtenerDistritosPorDepartamento(String departamentoCodigo) {
        return distritoRepository.findByDepartamentoCodigoAndActivoTrue(departamentoCodigo).stream()
                .map(this::convertirADistritoDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca distritos por texto (código o nombre).
     */
    public List<DistritoDto> buscarDistritos(String busqueda) {
        return distritoRepository.findByCodigoOrNombreContainingIgnoreCase(busqueda).stream()
                .map(this::convertirADistritoDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca distritos por texto dentro de un departamento.
     */
    public List<DistritoDto> buscarDistritosPorDepartamento(String departamentoCodigo, String busqueda) {
        List<Distrito> distritos = distritoRepository.findByDepartamentoCodigoAndActivoTrue(departamentoCodigo);
        return distritos.stream()
                .filter(d -> d.getCodigo().toUpperCase().contains(busqueda.toUpperCase()) ||
                           d.getNombre().toUpperCase().contains(busqueda.toUpperCase()))
                .map(this::convertirADistritoDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un distrito por código.
     */
    public DistritoDto obtenerDistritoPorCodigo(String codigo) {
        return distritoRepository.findByCodigo(codigo)
                .map(this::convertirADistritoDto)
                .orElse(null);
    }

    // ========== CIUDADES ==========

    /**
     * Obtiene todas las ciudades de un distrito.
     */
    public List<CiudadDto> obtenerCiudadesPorDistrito(String distritoCodigo) {
        return ciudadRepository.findByDistritoCodigoAndActivoTrue(distritoCodigo).stream()
                .map(this::convertirACiudadDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca ciudades por texto (código o nombre).
     */
    public List<CiudadDto> buscarCiudades(String busqueda) {
        return ciudadRepository.findByCodigoOrNombreContainingIgnoreCase(busqueda).stream()
                .map(this::convertirACiudadDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca ciudades por texto dentro de un distrito.
     */
    public List<CiudadDto> buscarCiudadesPorDistrito(String distritoCodigo, String busqueda) {
        List<Ciudad> ciudades = ciudadRepository.findByDistritoCodigoAndActivoTrue(distritoCodigo);
        return ciudades.stream()
                .filter(c -> c.getCodigo().toUpperCase().contains(busqueda.toUpperCase()) ||
                           c.getNombre().toUpperCase().contains(busqueda.toUpperCase()))
                .map(this::convertirACiudadDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene una ciudad por código.
     */
    public CiudadDto obtenerCiudadPorCodigo(String codigo) {
        return ciudadRepository.findByCodigo(codigo)
                .map(this::convertirACiudadDto)
                .orElse(null);
    }

    // ========== BARRIOS ==========

    /**
     * Obtiene todos los barrios de una ciudad.
     */
    public List<BarrioDto> obtenerBarriosPorCiudad(String ciudadCodigo) {
        return barrioRepository.findByCiudadCodigoAndActivoTrue(ciudadCodigo).stream()
                .map(this::convertirABarrioDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca barrios por texto (código o nombre).
     */
    public List<BarrioDto> buscarBarrios(String busqueda) {
        return barrioRepository.findByCodigoOrNombreContainingIgnoreCase(busqueda).stream()
                .map(this::convertirABarrioDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca barrios por texto dentro de una ciudad.
     */
    public List<BarrioDto> buscarBarriosPorCiudad(String ciudadCodigo, String busqueda) {
        List<Barrio> barrios = barrioRepository.findByCiudadCodigoAndActivoTrue(ciudadCodigo);
        return barrios.stream()
                .filter(b -> b.getCodigo().toUpperCase().contains(busqueda.toUpperCase()) ||
                           b.getNombre().toUpperCase().contains(busqueda.toUpperCase()))
                .map(this::convertirABarrioDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un barrio por código.
     */
    public BarrioDto obtenerBarrioPorCodigo(String codigo) {
        return barrioRepository.findByCodigo(codigo)
                .map(this::convertirABarrioDto)
                .orElse(null);
    }

    // ========== MÉTODOS DE CONVERSIÓN ==========

    private DepartamentoDto convertirADepartamentoDto(Departamento departamento) {
        return new DepartamentoDto(
                departamento.getId(),
                departamento.getCodigo(),
                departamento.getNombre(),
                departamento.getPais().getCodigo(),
                departamento.getPais().getNombre(),
                departamento.getActivo()
        );
    }

    private DistritoDto convertirADistritoDto(Distrito distrito) {
        return new DistritoDto(
                distrito.getId(),
                distrito.getCodigo(),
                distrito.getNombre(),
                distrito.getDepartamento().getCodigo(),
                distrito.getDepartamento().getNombre(),
                distrito.getActivo()
        );
    }

    private CiudadDto convertirACiudadDto(Ciudad ciudad) {
        return new CiudadDto(
                ciudad.getId(),
                ciudad.getCodigo(),
                ciudad.getNombre(),
                ciudad.getDistrito().getCodigo(),
                ciudad.getDistrito().getNombre(),
                ciudad.getActivo()
        );
    }

    private BarrioDto convertirABarrioDto(Barrio barrio) {
        return new BarrioDto(
                barrio.getId(),
                barrio.getCodigo(),
                barrio.getNombre(),
                barrio.getCiudad().getCodigo(),
                barrio.getCiudad().getNombre(),
                barrio.getActivo()
        );
    }
}