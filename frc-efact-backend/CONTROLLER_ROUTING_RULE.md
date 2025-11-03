# Regla de Routing para Controladores REST

## ⚠️ REGLA CRÍTICA: Context-Path y @RequestMapping

En el proyecto FRC-eFact, la aplicación Spring Boot tiene configurado **`context-path: /api`** en `application.yml` (línea 30 del archivo `src/main/resources/application.yml`).

### Problema Común

**❌ INCORRECTO:**
```java
@RestController
@RequestMapping("/api/clientes")  // ❌ Duplica el prefijo /api
```

**Resultado:** La ruta final sería `/api/api/clientes` (duplicado), causando error:
```
NoResourceFoundException: No static resource clientes/empresa/1
```

### Solución Correcta

**✅ CORRECTO:**
```java
@RestController
@RequestMapping("/clientes")  // ✅ Sin /api/, el context-path lo agrega
```

**Resultado:** La ruta final es `/api/clientes` (correcto)

### Regla General

**SIEMPRE usar `@RequestMapping` SIN el prefijo `/api/` porque:**
1. El `context-path: /api` ya agrega el prefijo automáticamente
2. Todos los endpoints quedan bajo `/api/{recurso}`
3. Mantiene consistencia con el resto de controladores

### Controladores que Necesitan Corrección

Los siguientes controladores tienen `/api/` en el `@RequestMapping` y deben corregirse:

| Controlador | Actual (Incorrecto) | Debe Ser (Correcto) |
|------------|-------------------|-------------------|
| `DashboardController` | `/api/dashboard` | `/dashboard` |
| `GeografiaController` | `/api/geografia` | `/geografia` |
| `ReporteController` | `/api/reportes` | `/reportes` |
| `AuditLogController` | `/api/auditoria` | `/auditoria` |
| `DocumentoElectronicoController` | `/api/documentos-electronicos` | `/documentos-electronicos` |
| `FacturaLegalController` | `/api/facturas` | `/facturas` |

### Controladores Correctos (Ejemplos)

Estos controladores ya siguen la regla correctamente:

- ✅ `AuthController`: `@RequestMapping("/auth")`
- ✅ `ClienteController`: `@RequestMapping("/clientes")`
- ✅ `TimbradoController`: `@RequestMapping("/timbrados")`
- ✅ `ProductoController`: `@RequestMapping("/productos")`
- ✅ `EmpresaController`: `@RequestMapping("/empresas")`
- ✅ `UsuarioController`: `@RequestMapping("/usuarios")`

### Verificación

Para verificar que un controlador está correcto:
1. Buscar `@RequestMapping` en el archivo del controlador
2. Verificar que NO tenga `/api/` como prefijo
3. La ruta final será: `{context-path}/{@RequestMapping}` = `/api/{recurso}`

### Ejemplo Completo

```java
package com.frcefact.controller;

import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST correcto - SIN /api/ en @RequestMapping
 */
@RestController
@RequestMapping("/clientes")  // ✅ Correcto: sin /api/
@Tag(name = "Clientes", description = "API para gestión de clientes")
public class ClienteController {
    
    // El endpoint será: /api/clientes/empresa/{empresaId}
    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<List<ClienteDto>> listarClientes(@PathVariable Long empresaId) {
        // ...
    }
}
```

### Notas Importantes

1. **Los mensajes de log** deben mostrar la ruta relativa al context-path:
   ```java
   logger.info("GET /clientes/empresa/{} - Listando clientes", empresaId);
   ```

2. **En el frontend**, las URLs deben incluir `/api/`:
   ```typescript
   private readonly baseUrl = `${environment.apiUrl}/clientes`; // ✅ Correcto
   ```

3. **La configuración en `application.yml`**:
   ```yaml
   server:
     servlet:
       context-path: /api  # Esto agrega /api a todas las rutas
   ```

---

**Última actualización:** Después de corregir ClienteController (2024)
**Estado:** Esta regla debe aplicarse a TODOS los controladores nuevos y existentes

