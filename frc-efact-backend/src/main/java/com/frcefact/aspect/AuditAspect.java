package com.frcefact.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.frcefact.annotation.Auditable;
import com.frcefact.model.AccionEnum;
import com.frcefact.model.AuditLog;
import com.frcefact.model.Empresa;
import com.frcefact.model.Usuario;
import com.frcefact.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Aspecto que intercepta métodos anotados con @Auditable para registrar
 * automáticamente las operaciones en la tabla de auditoría.
 * 
 * Captura:
 * - Usuario que realiza la acción
 * - Empresa asociada (si aplica)
 * - Valores anteriores (para UPDATE)
 * - Valores nuevos (para CREATE y UPDATE)
 * - IP address y User Agent
 */
@Aspect
@Component
public class AuditAspect {

    private static final Logger logger = LoggerFactory.getLogger(AuditAspect.class);

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AuditAspect(AuditLogRepository auditLogRepository,
                      ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Intercepta métodos anotados con @Auditable y registra la operación en auditoría.
     */
    @Around("@annotation(com.frcefact.annotation.Auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Auditable auditable = method.getAnnotation(Auditable.class);

        // Obtener usuario actual
        Usuario usuario = getCurrentUser();
        if (usuario == null) {
            logger.warn("No se pudo obtener el usuario actual para auditoría");
            return joinPoint.proceed();
        }

        // Capturar valores anteriores para UPDATE
        Map<String, Object> valoresAnteriores = null;
        if (auditable.accion() == AccionEnum.UPDATE) {
            valoresAnteriores = captureCurrentValues(joinPoint);
        }

        // Ejecutar el método
        Object result = joinPoint.proceed();

        // Registrar auditoría de forma asíncrona para no afectar performance
        try {
            registrarAuditoria(auditable, usuario, valoresAnteriores, result, joinPoint);
        } catch (Exception e) {
            logger.error("Error al registrar auditoría: {}", e.getMessage(), e);
            // No lanzar excepción para no afectar la operación principal
        }

        return result;
    }

    /**
     * Registra la operación en la tabla de auditoría.
     */
    private void registrarAuditoria(Auditable auditable, Usuario usuario,
                                   Map<String, Object> valoresAnteriores,
                                   Object result, ProceedingJoinPoint joinPoint) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUsuario(usuario);
        auditLog.setEntidadTipo(auditable.entidad());
        auditLog.setAccion(auditable.accion());

        // Extraer ID de la entidad del resultado
        Long entidadId = extractEntityId(result);
        auditLog.setEntidadId(entidadId);

        // Extraer empresa si está disponible
        Empresa empresa = extractEmpresa(result, joinPoint);
        auditLog.setEmpresa(empresa);

        // Capturar valores nuevos
        if (auditable.accion() == AccionEnum.CREATE || auditable.accion() == AccionEnum.UPDATE) {
            Map<String, Object> valoresNuevos = convertToMap(result);
            auditLog.setValoresNuevos(valoresNuevos);
        }

        // Establecer valores anteriores para UPDATE
        if (auditable.accion() == AccionEnum.UPDATE && valoresAnteriores != null) {
            auditLog.setValoresAnteriores(valoresAnteriores);
        }

        // Capturar IP y User Agent
        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            auditLog.setIpAddress(getClientIpAddress(request));
            auditLog.setUserAgent(request.getHeader("User-Agent"));
        }

        // Descripción
        String descripcion = auditable.descripcion().isEmpty() 
            ? generateDefaultDescription(auditable) 
            : auditable.descripcion();
        auditLog.setDescripcion(descripcion);

        auditLogRepository.save(auditLog);
        logger.debug("Auditoría registrada: {} {} en {}", auditable.accion(), auditable.entidad(), entidadId);
    }

    /**
     * Obtiene el usuario actual del contexto de seguridad.
     */
    private Usuario getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Usuario) {
            return (Usuario) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * Captura los valores actuales de la entidad antes de la actualización.
     */
    private Map<String, Object> captureCurrentValues(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        
        // Buscar el primer argumento que tenga un ID (entidad existente)
        for (Object arg : args) {
            if (arg != null) {
                try {
                    Long id = extractEntityId(arg);
                    if (id != null) {
                        // Convertir la entidad actual a Map
                        return convertToMap(arg);
                    }
                } catch (Exception e) {
                    logger.debug("No se pudo extraer ID de argumento: {}", arg.getClass().getSimpleName());
                }
            }
        }
        return null;
    }

    /**
     * Extrae el ID de una entidad usando reflexión.
     */
    private Long extractEntityId(Object entity) {
        if (entity == null) {
            return null;
        }

        try {
            BeanWrapper wrapper = new BeanWrapperImpl(entity);
            if (wrapper.isReadableProperty("id")) {
                Object id = wrapper.getPropertyValue("id");
                if (id instanceof Long) {
                    return (Long) id;
                } else if (id instanceof Number) {
                    return ((Number) id).longValue();
                }
            }
        } catch (Exception e) {
            logger.debug("No se pudo extraer ID de {}: {}", entity.getClass().getSimpleName(), e.getMessage());
        }
        return null;
    }

    /**
     * Extrae la empresa asociada a la entidad o de los argumentos del método.
     */
    private Empresa extractEmpresa(Object result, ProceedingJoinPoint joinPoint) {
        // Intentar extraer de la entidad resultado
        if (result != null) {
            try {
                BeanWrapper wrapper = new BeanWrapperImpl(result);
                if (wrapper.isReadableProperty("empresa")) {
                    Object empresa = wrapper.getPropertyValue("empresa");
                    if (empresa instanceof Empresa) {
                        return (Empresa) empresa;
                    }
                }
                if (wrapper.isReadableProperty("empresaId")) {
                    Object empresaId = wrapper.getPropertyValue("empresaId");
                    if (empresaId instanceof Long) {
                        Empresa empresa = new Empresa();
                        empresa.setId((Long) empresaId);
                        return empresa;
                    }
                }
            } catch (Exception e) {
                logger.debug("No se pudo extraer empresa del resultado");
            }
        }

        // Intentar extraer de los argumentos
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof Empresa) {
                return (Empresa) arg;
            }
            if (arg instanceof Long && joinPoint.getArgs().length > 0) {
                // Podría ser empresaId como primer argumento
                try {
                    Empresa empresa = new Empresa();
                    empresa.setId((Long) arg);
                    return empresa;
                } catch (Exception e) {
                    logger.debug("No se pudo crear empresa desde ID");
                }
            }
        }

        return null;
    }

    /**
     * Convierte una entidad a Map para almacenar en JSON.
     */
    private Map<String, Object> convertToMap(Object entity) {
        if (entity == null) {
            return null;
        }

        try {
            // Usar ObjectMapper para convertir a Map
            @SuppressWarnings("unchecked")
            Map<String, Object> map = objectMapper.convertValue(entity, Map.class);
            
            // Filtrar campos sensibles que no deben ser auditados
            map.remove("password");
            map.remove("passwordHash");
            map.remove("certificadoPasswordEncrypted");
            map.remove("cscEncrypted");
            
            return map;
        } catch (Exception e) {
            logger.warn("Error al convertir entidad a Map: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * Obtiene el request HTTP actual.
     */
    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            logger.debug("No se pudo obtener el request actual");
            return null;
        }
    }

    /**
     * Obtiene la dirección IP real del cliente, considerando proxies.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };

        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For puede contener múltiples IPs, tomar la primera
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        return request.getRemoteAddr();
    }

    /**
     * Genera una descripción por defecto basada en la anotación.
     */
    private String generateDefaultDescription(Auditable auditable) {
        return switch (auditable.accion()) {
            case CREATE -> "Creación de " + auditable.entidad();
            case UPDATE -> "Actualización de " + auditable.entidad();
            case DELETE -> "Eliminación de " + auditable.entidad();
            case READ -> "Consulta de " + auditable.entidad();
        };
    }
}
