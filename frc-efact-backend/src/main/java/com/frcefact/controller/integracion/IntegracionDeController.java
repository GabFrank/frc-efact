package com.frcefact.controller.integracion;

import com.frcefact.dto.integracion.DeConsultadoDto;
import com.frcefact.security.apikey.ApiKeyAuthenticationFilter;
import com.frcefact.security.apikey.ApiKeyPrincipal;
import com.frcefact.service.KudePdfService;
import com.frcefact.service.integracion.ConsultaDeSifenService;
import com.frcefact.service.integracion.IntegracionException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * API de integración — consulta de documentos electrónicos en SIFEN por CDC.
 *
 * <p>Autenticación: header {@code X-API-Key} con una key que tenga el scope {@code de:consultar}.
 * Contrato versionado: cualquier cambio incompatible va a {@code /v2}, nunca acá.
 */
@RestController
@RequestMapping("/v1/integracion/documentos")
@Tag(name = "Integración — Documentos", description = "Consulta de DE en SIFEN para sistemas externos (API key)")
@SecurityRequirement(name = "apiKey")
public class IntegracionDeController {

    private static final Set<String> INCLUIBLES = Set.of("items", "xml");

    private final ConsultaDeSifenService consultaDeSifenService;
    private final KudePdfService kudePdfService;

    public IntegracionDeController(ConsultaDeSifenService consultaDeSifenService, KudePdfService kudePdfService) {
        this.consultaDeSifenService = consultaDeSifenService;
        this.kudePdfService = kudePdfService;
    }

    @GetMapping(value = "/{cdc}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('SCOPE_de:consultar')")
    @Operation(summary = "Consultar un DE por CDC",
            description = "Consulta el DE en SIFEN y devuelve estado, emisor, receptor, operación y totales. "
                    + "Con ?incluir=items,xml agrega el detalle de ítems y/o el XML firmado.")
    public ResponseEntity<DeConsultadoDto> consultar(
            @PathVariable String cdc,
            @Parameter(description = "Partes opcionales separadas por coma: items, xml")
            @RequestParam(required = false) String incluir,
            @AuthenticationPrincipal ApiKeyPrincipal principal,
            HttpServletRequest request) {
        marcar(request, "DE_CONSULTAR", cdc);
        Set<String> partes = parsearIncluir(incluir);
        ConsultaDeSifenService.ResultadoConsulta r = consultaDeSifenService.consultar(cdc, principal);
        request.setAttribute(ApiKeyAuthenticationFilter.ATTR_CODIGO_SIFEN, r.codigoSifen());
        return ResponseEntity.ok(r.de().conIncluidos(partes.contains("items"), partes.contains("xml")));
    }

    @GetMapping(value = "/{cdc}/xml", produces = MediaType.APPLICATION_XML_VALUE)
    @PreAuthorize("hasAuthority('SCOPE_de:consultar')")
    @Operation(summary = "XML firmado del DE", description = "Devuelve el rDE (DE firmado + QR) tal como lo tiene SIFEN.")
    public ResponseEntity<byte[]> xml(@PathVariable String cdc,
                                      @AuthenticationPrincipal ApiKeyPrincipal principal,
                                      HttpServletRequest request) {
        marcar(request, "DE_XML", cdc);
        ConsultaDeSifenService.ResultadoConsulta r = consultaDeSifenService.consultar(cdc, principal);
        request.setAttribute(ApiKeyAuthenticationFilter.ATTR_CODIGO_SIFEN, r.codigoSifen());
        return ResponseEntity.ok()
                .contentType(new MediaType(MediaType.APPLICATION_XML, StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename("DE_" + cdc + ".xml").build().toString())
                .body(r.de().xml().getBytes(StandardCharsets.UTF_8));
    }

    @GetMapping(value = "/{cdc}/kude", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAuthority('SCOPE_de:consultar')")
    @Operation(summary = "KuDE PDF del DE",
            description = "Genera el KuDE a partir del XML de SIFEN. Disponible para factura electrónica (1) "
                    + "y nota de crédito (5); otros tipos responden 501.")
    public ResponseEntity<byte[]> kude(@PathVariable String cdc,
                                       @AuthenticationPrincipal ApiKeyPrincipal principal,
                                       HttpServletRequest request) throws Exception {
        marcar(request, "DE_KUDE", cdc);
        ConsultaDeSifenService.ResultadoConsulta r = consultaDeSifenService.consultar(cdc, principal);
        request.setAttribute(ApiKeyAuthenticationFilter.ATTR_CODIGO_SIFEN, r.codigoSifen());
        if (!KudePdfService.soportaKudeDesdeSifen(r.de().tipoDocumento())) {
            throw new IntegracionException(HttpStatus.NOT_IMPLEMENTED, "KUDE_NO_DISPONIBLE",
                    "KuDE no disponible todavía para el tipo de documento " + r.de().tipoDocumento()
                            + " (" + r.de().tipoDocumentoDescripcion() + ")");
        }
        byte[] pdf = kudePdfService.generarPdfKudeDesdeSifen(r.de());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename("KuDE_" + cdc + ".pdf").build().toString())
                .body(pdf);
    }

    private static void marcar(HttpServletRequest request, String operacion, String cdc) {
        request.setAttribute(ApiKeyAuthenticationFilter.ATTR_OPERACION, operacion);
        request.setAttribute(ApiKeyAuthenticationFilter.ATTR_RECURSO, cdc);
    }

    static Set<String> parsearIncluir(String incluir) {
        if (incluir == null || incluir.isBlank()) {
            return Set.of();
        }
        Set<String> partes = Arrays.stream(incluir.split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).map(String::toLowerCase)
                .collect(Collectors.toSet());
        for (String p : partes) {
            if (!INCLUIBLES.contains(p)) {
                throw new IntegracionException(HttpStatus.BAD_REQUEST, "PARAMETRO_INVALIDO",
                        "Valor de 'incluir' desconocido: '" + p + "'. Valores válidos: items, xml");
            }
        }
        return partes;
    }
}
