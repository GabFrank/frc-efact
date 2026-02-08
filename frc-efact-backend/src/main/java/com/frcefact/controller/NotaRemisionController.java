package com.frcefact.controller;

import com.frcefact.dto.EmailSendDto;
import com.frcefact.dto.GenerarDeResponse;
import com.frcefact.dto.LoteDeDto;
import com.frcefact.dto.NotaRemisionDto;
import com.frcefact.dto.mapper.DocumentoElectronicoMapper;
import com.frcefact.dto.mapper.NotaRemisionMapper;
import com.frcefact.model.Cliente;
import com.frcefact.model.DocumentoElectronico;
import com.frcefact.model.NotaRemision;
import com.frcefact.repository.ClienteRepository;
import com.frcefact.service.DocumentoElectronicoService;
import com.frcefact.service.EmailNotaRemisionService;
import com.frcefact.service.NotaRemisionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import com.frcefact.service.KudePdfService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/notas-remision")
@Tag(name = "Notas de Remisión", description = "API para gestión de notas de remisión")
@SecurityRequirement(name = "bearer-jwt")
public class NotaRemisionController {

    private static final Logger logger = LoggerFactory.getLogger(NotaRemisionController.class);

    private final NotaRemisionService notaRemisionService;
    private final NotaRemisionMapper notaRemisionMapper;
    private final DocumentoElectronicoService documentoElectronicoService;
    private final DocumentoElectronicoMapper documentoElectronicoMapper;
    private final KudePdfService kudePdfService;
    private final EmailNotaRemisionService emailNotaRemisionService;
    private final ClienteRepository clienteRepository;

    public NotaRemisionController(NotaRemisionService notaRemisionService, 
                                 NotaRemisionMapper notaRemisionMapper,
                                 DocumentoElectronicoService documentoElectronicoService,
                                 DocumentoElectronicoMapper documentoElectronicoMapper,
                                 KudePdfService kudePdfService,
                                 EmailNotaRemisionService emailNotaRemisionService,
                                 ClienteRepository clienteRepository) {
        this.notaRemisionService = notaRemisionService;
        this.notaRemisionMapper = notaRemisionMapper;
        this.documentoElectronicoService = documentoElectronicoService;
        this.documentoElectronicoMapper = documentoElectronicoMapper;
        this.kudePdfService = kudePdfService;
        this.emailNotaRemisionService = emailNotaRemisionService;
        this.clienteRepository = clienteRepository;
    }

    @Operation(summary = "Crear una nueva nota de remisión")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Nota de remisión creada exitosamente",
                    content = @Content(schema = @Schema(implementation = NotaRemisionDto.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta empresa")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR')")
    public ResponseEntity<NotaRemisionDto> crearNotaRemision(
            @Valid @RequestBody NotaRemisionDto dto) {
        
        logger.info("POST /notas-remision - Crear nota de remisión para empresa ID: {}", dto.getEmpresaId());
        
        NotaRemision entity = notaRemisionMapper.toEntity(dto);
        NotaRemision created = notaRemisionService.crearNotaRemision(entity);
        NotaRemisionDto responseDto = notaRemisionMapper.toDto(created);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @Operation(summary = "Obtener una nota de remisión por ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    public ResponseEntity<NotaRemisionDto> obtenerNotaRemision(
            @PathVariable Long id) {
        
        NotaRemision entity = notaRemisionService.obtenerPorId(id);
        NotaRemisionDto responseDto = notaRemisionMapper.toDto(entity);
        
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "Listar notas de remisión de una empresa")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    public ResponseEntity<Page<NotaRemisionDto>> listarNotasRemision(
            @RequestParam Long empresaId,
            @RequestParam(required = false) String numero,
            @RequestParam(required = false) LocalDate fechaDesde,
            @RequestParam(required = false) LocalDate fechaHasta,
            @RequestParam(required = false) String motivo,
            @RequestParam(required = false) String destinatario,
            @RequestParam(required = false) String vehiculo,
            @RequestParam(required = false) String chofer,
            @RequestParam(required = false) String estadoDE,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "fecha") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ? 
                Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<NotaRemisionDto> pageDto = notaRemisionService.listarPorEmpresa(
                empresaId, numero, fechaDesde, fechaHasta, motivo, destinatario, vehiculo, chofer, estadoDE, pageable);
        
        return ResponseEntity.ok(pageDto);
    }
    
    @Operation(summary = "Desactivar una nota de remisión")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN')")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        notaRemisionService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Generar y enviar documento electrónico para una nota de remisión")
    @PostMapping("/{id}/generar-de")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR')")
    public ResponseEntity<GenerarDeResponse> generarDE(@PathVariable Long id) {
        logger.info("POST /notas-remision/{}/generar-de - Generar y enviar DE", id);
        DocumentoElectronicoService.GenerarDeResult result = documentoElectronicoService.generarYEnviarDesdeNotaRemision(id);
        
        var documentoDto = documentoElectronicoMapper.toDto(result.documento());
        var loteDto = LoteDeDto.fromEntity(result.lote());
        
        return ResponseEntity.ok(GenerarDeResponse.of(documentoDto, loteDto));
    }

    @Operation(summary = "Generar y enviar documento electrónico para una nota de remisión")
    @PostMapping("/{id}/generar-y-enviar")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR')")
    public ResponseEntity<GenerarDeResponse> generarYEnviar(@PathVariable Long id) {
        logger.info("POST /notas-remision/{}/generar-y-enviar", id);
        DocumentoElectronicoService.GenerarDeResult result = documentoElectronicoService.generarYEnviarDesdeNotaRemision(id);
        
        var documentoDto = documentoElectronicoMapper.toDto(result.documento());
        var loteDto = LoteDeDto.fromEntity(result.lote());
        
        return ResponseEntity.ok(GenerarDeResponse.of(documentoDto, loteDto));
    }

    @Operation(summary = "Vincular documento electrónico existente a un nuevo lote y enviar")
    @PostMapping("/{id}/vincular-lote")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR')")
    public ResponseEntity<GenerarDeResponse> vincularLote(@PathVariable Long id) {
        logger.info("POST /notas-remision/{}/vincular-lote", id);
        DocumentoElectronicoService.GenerarDeResult result = documentoElectronicoService.vincularALoteYEnviarDesdeNotaRemision(id);
        
        var documentoDto = documentoElectronicoMapper.toDto(result.documento());
        var loteDto = LoteDeDto.fromEntity(result.lote());
        
        return ResponseEntity.ok(GenerarDeResponse.of(documentoDto, loteDto));
    }

    @Operation(summary = "Generar PDF del KUDE",
               description = "Genera el PDF de la representación gráfica del documento electrónico (KUDE) de una nota de remisión")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "PDF generado exitosamente",
                    content = @Content(mediaType = "application/pdf")),
        @ApiResponse(responseCode = "400", description = "Error al generar el PDF"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta empresa"),
        @ApiResponse(responseCode = "404", description = "Nota de remisión no encontrada")
    })
    @GetMapping("/{id}/kude-pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR', 'LECTOR')")
    public ResponseEntity<byte[]> generarPdfKude(@PathVariable Long id) {
        logger.info("GET /notas-remision/{}/kude-pdf - Generar PDF KUDE", id);
        try {
            NotaRemision notaRemision = notaRemisionService.obtenerPorId(id);
            if (notaRemision.getDocumentoElectronico() == null) {
                logger.warn("La nota de remisión {} no tiene documento electrónico asociado", id);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("La nota de remisión no tiene documento electrónico asociado".getBytes());
            }
            byte[] pdfBytes = kudePdfService.generarPdfKude(notaRemision);
            
            // Generar nombre del archivo: KuDE-{fecha dd-mm-yy}-{matricula}-{CDC}.pdf
            // Usar guiones en lugar de barras para evitar problemas con nombres de archivo
            String fechaFormateada = notaRemision.getFecha() != null ? 
                    notaRemision.getFecha().format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yy")) : 
                    "N/A";
            
            String matricula = notaRemision.getVehiculoMatricula();
            if (matricula == null || matricula.trim().isEmpty()) {
                matricula = "SIN-MATRICULA";
            }
            // Limpiar matrícula de caracteres inválidos para nombres de archivo
            matricula = matricula.replaceAll("[^a-zA-Z0-9\\-]", "-");
            
            String cdc = notaRemision.getDocumentoElectronico() != null ? 
                    notaRemision.getDocumentoElectronico().getCdc() : 
                    notaRemision.getNumeroFormateado();
            
            String numero = notaRemision.getNumeroFormateado();
            String fileName = String.format("NRE-%s-%s-%s-%s.pdf", numero, fechaFormateada, matricula, cdc);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            // Usar 'inline' para que se abra en el navegador, pero con nombre de archivo para cuando el usuario guarde
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"");
            headers.setContentLength(pdfBytes.length);
            return ResponseEntity.ok().headers(headers).body(pdfBytes);
        } catch (Exception e) {
            logger.error("Error al generar PDF KUDE para nota de remisión ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error al generar PDF: " + e.getMessage()).getBytes());
        }
    }

    @Operation(summary = "Enviar nota de remisión por email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Email enviado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o la nota no tiene DE aprobado"),
        @ApiResponse(responseCode = "403", description = "Sin permisos para esta empresa"),
        @ApiResponse(responseCode = "404", description = "Nota de remisión no encontrada")
    })
    @PostMapping("/{id}/enviar-email")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPRESA_ADMIN', 'FACTURADOR')")
    public ResponseEntity<Map<String, String>> enviarEmail(
            @PathVariable Long id,
            @Valid @RequestBody EmailSendDto emailDto) {
        
        logger.info("POST /notas-remision/{}/enviar-email - Enviar email a {}", id, emailDto.getEmail());
        
        NotaRemision notaRemision = notaRemisionService.obtenerPorId(id);
        
        if (notaRemision.getDocumentoElectronico() == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "La nota de remisión no tiene documento electrónico asociado");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        
        DocumentoElectronico documento = notaRemision.getDocumentoElectronico();
        
        // Opcional: Validar estado aprobado si se desea restringir
        // if (documento.getEstado() != EstadoDE.APROBADO) { ... }
        
        // Actualizar email del cliente si se solicita
        if (emailDto.isActualizarCliente() && notaRemision.getCliente() != null) {
            Cliente cliente = notaRemision.getCliente();
            cliente.setEmail(emailDto.getEmail());
            clienteRepository.save(cliente);
            logger.info("Cliente ID: {} actualizado con nuevo email: {}", cliente.getId(), emailDto.getEmail());
        }
        
        try {
            emailNotaRemisionService.enviarNotaRemisionAlClienteAsync(documento, emailDto.getEmail());
            Map<String, String> response = new HashMap<>();
            response.put("message", "Email enviado exitosamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al enviar email para nota ID: {}", id, e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al enviar email: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}

