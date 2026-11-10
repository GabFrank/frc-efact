package com.frcefact.controller.integracion;

import com.frcefact.dto.integracion.*;
import com.frcefact.service.integracion.ApiKeyService;
import com.frcefact.service.integracion.CuentaServicioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Administración de cuentas de servicio y API keys. Solo ADMIN.
 */
@RestController
@RequestMapping("/admin/integracion")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Integración — Administración", description = "Cuentas de servicio y API keys")
public class AdminIntegracionController {

    private final CuentaServicioService cuentaServicioService;
    private final ApiKeyService apiKeyService;

    public AdminIntegracionController(CuentaServicioService cuentaServicioService, ApiKeyService apiKeyService) {
        this.cuentaServicioService = cuentaServicioService;
        this.apiKeyService = apiKeyService;
    }

    @GetMapping("/cuentas")
    @Operation(summary = "Listar cuentas de servicio")
    public List<CuentaServicioDto> listarCuentas() {
        return cuentaServicioService.listar();
    }

    @GetMapping("/cuentas/{id}")
    @Operation(summary = "Obtener una cuenta de servicio")
    public CuentaServicioDto obtenerCuenta(@PathVariable Long id) {
        return cuentaServicioService.obtener(id);
    }

    @PostMapping("/cuentas")
    @Operation(summary = "Crear una cuenta de servicio")
    public ResponseEntity<CuentaServicioDto> crearCuenta(@Valid @RequestBody CuentaServicioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cuentaServicioService.crear(request));
    }

    @PutMapping("/cuentas/{id}")
    @Operation(summary = "Modificar una cuenta de servicio (activa=false bloquea todas sus keys)")
    public CuentaServicioDto actualizarCuenta(@PathVariable Long id, @Valid @RequestBody CuentaServicioRequest request) {
        return cuentaServicioService.actualizar(id, request);
    }

    @GetMapping("/cuentas/{id}/keys")
    @Operation(summary = "Listar las keys de una cuenta (sin secretos)")
    public List<ApiKeyDto> listarKeys(@PathVariable Long id) {
        return apiKeyService.listarPorCuenta(id).stream().map(ApiKeyDto::desde).toList();
    }

    @PostMapping("/cuentas/{id}/keys")
    @Operation(summary = "Crear una key", description = "La respuesta trae la clave completa. Es la única vez que se muestra.")
    public ResponseEntity<ApiKeyCreadaDto> crearKey(@PathVariable Long id, @Valid @RequestBody CrearApiKeyRequest request) {
        ApiKeyService.ApiKeyCreada creada = apiKeyService.crear(id, request.nombre(), request.scopes(), request.expiraEn());
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiKeyCreadaDto(
                ApiKeyDto.desde(creada.apiKey()),
                creada.claveCompleta(),
                "Guardá esta clave ahora: eFact no la almacena y no se puede volver a mostrar."));
    }

    @PostMapping("/keys/{keyId}/revocar")
    @Operation(summary = "Revocar una key", description = "Efecto inmediato. No se puede deshacer.")
    public ApiKeyDto revocarKey(@PathVariable Long keyId) {
        String usuario = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiKeyDto.desde(apiKeyService.revocar(keyId, usuario));
    }
}
