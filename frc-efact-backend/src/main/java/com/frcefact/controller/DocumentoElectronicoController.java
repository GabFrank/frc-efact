package com.frcefact.controller;

import com.frcefact.dto.DocumentoElectronicoDto;
import com.frcefact.dto.mapper.DocumentoElectronicoMapper;
import com.frcefact.model.DocumentoElectronico;
import com.frcefact.model.EstadoDE;
import com.frcefact.service.DocumentoElectronicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para gestión de documentos electrónicos.
 */
@RestController
@RequestMapping("/api/documentos-electronicos")
@Tag(name = "Documentos Electrónicos", description = "Gestión de documentos electrónicos (DEs)")
public class DocumentoElectronicoController {

    private static final Logger log = LoggerFactory.getLogger(DocumentoElectronicoController.class);

    private final DocumentoElectronicoService documentoElectronicoService;
    private final DocumentoElectronicoMapper documentoElectronicoMapper;

    public DocumentoElectronicoController(
            DocumentoElectronicoService documentoElectronicoService,
            DocumentoElectronicoMapper documentoElectronicoMapper) {
        this.documentoElectronicoService = documentoElectronicoService;
        this.documentoElectronicoMapper = documentoElectronicoMapper;
    }

    /**
     * Lista documentos electrónicos con filtros opcionales.
     * 
     * GET /api/documentos-electronicos?estado=PENDIENTE&empresaId=1&page=0&size=20
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    @Operation(summary = "Listar documentos electrónicos", 
               description = "Lista documentos electrónicos con filtros opcionales por estado y empresa")
    public ResponseEntity<Page<DocumentoElectronicoDto>> listar(
            @Parameter(description = "Estado del documento electrónico")
            @RequestParam(required = false) EstadoDE estado,
            @Parameter(description = "ID de la empresa")
            @RequestParam(required = false) Long empresaId,
            @PageableDefault(size = 20, sort = "fechaEmision", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.debug("📋 Listando documentos electrónicos - Estado: {}, Empresa: {}", estado, empresaId);
        
        Page<DocumentoElectronico> page = documentoElectronicoService.listar(estado, empresaId, pageable);
        Page<DocumentoElectronicoDto> dtoPage = page.map(documentoElectronicoMapper::toDto);
        
        return ResponseEntity.ok(dtoPage);
    }

    /**
     * Obtiene un documento electrónico por ID.
     * 
     * GET /api/documentos-electronicos/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    @Operation(summary = "Obtener documento electrónico por ID")
    public ResponseEntity<DocumentoElectronicoDto> obtenerPorId(
            @Parameter(description = "ID del documento electrónico")
            @PathVariable Long id) {
        
        log.debug("🔍 Obteniendo documento electrónico ID: {}", id);
        
        DocumentoElectronico de = documentoElectronicoService.findById(id);
        DocumentoElectronicoDto dto = documentoElectronicoMapper.toDto(de);
        
        return ResponseEntity.ok(dto);
    }

    /**
     * Obtiene un documento electrónico por CDC.
     * 
     * GET /api/documentos-electronicos/cdc/{cdc}
     */
    @GetMapping("/cdc/{cdc}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    @Operation(summary = "Obtener documento electrónico por CDC")
    public ResponseEntity<DocumentoElectronicoDto> obtenerPorCdc(
            @Parameter(description = "CDC del documento electrónico")
            @PathVariable String cdc) {
        
        log.debug("🔍 Obteniendo documento electrónico con CDC: {}", cdc);
        
        DocumentoElectronico de = documentoElectronicoService.findByCdc(cdc);
        DocumentoElectronicoDto dto = documentoElectronicoMapper.toDto(de);
        
        return ResponseEntity.ok(dto);
    }

    /**
     * Descarga el XML firmado de un documento electrónico.
     * 
     * GET /api/documentos-electronicos/{id}/xml
     */
    @GetMapping("/{id}/xml")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    @Operation(summary = "Descargar XML firmado", 
               description = "Descarga el XML firmado del documento electrónico")
    public ResponseEntity<String> descargarXml(
            @Parameter(description = "ID del documento electrónico")
            @PathVariable Long id,
            @Parameter(description = "Tipo de XML: 'firmado' u 'original'")
            @RequestParam(defaultValue = "firmado") String tipo) {
        
        log.debug("📥 Descargando XML {} de DE ID: {}", tipo, id);
        
        String xml;
        if ("original".equalsIgnoreCase(tipo)) {
            xml = documentoElectronicoService.obtenerXmlOriginal(id);
        } else {
            xml = documentoElectronicoService.obtenerXmlFirmado(id);
        }
        
        DocumentoElectronico de = documentoElectronicoService.findById(id);
        String filename = String.format("DE_%s_%s.xml", de.getCdc(), tipo);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        headers.setContentDispositionFormData("attachment", filename);
        
        return new ResponseEntity<>(xml, headers, HttpStatus.OK);
    }

    /**
     * Consulta el estado de un documento electrónico en SIFEN.
     * 
     * POST /api/documentos-electronicos/{id}/consultar
     */
    @PostMapping("/{id}/consultar")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR')")
    @Operation(summary = "Consultar estado en SIFEN", 
               description = "Consulta el estado del documento electrónico en SIFEN y actualiza el estado local")
    public ResponseEntity<DocumentoElectronicoDto> consultarEstado(
            @Parameter(description = "ID del documento electrónico")
            @PathVariable Long id) {
        
        log.info("🔍 Consultando estado en SIFEN de DE ID: {}", id);
        
        DocumentoElectronico de = documentoElectronicoService.consultarYActualizarEstado(id);
        DocumentoElectronicoDto dto = documentoElectronicoMapper.toDto(de);
        
        return ResponseEntity.ok(dto);
    }
}
