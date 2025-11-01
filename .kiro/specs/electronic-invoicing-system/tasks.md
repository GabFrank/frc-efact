# Implementation Plan

- [ ] 1. Configurar estructura de base de datos y migraciones
  - [x] 1.1 Crear esquemas de base de datos
    - Crear migración para esquemas: persona, empresa, financiero, productos, clientes, auditoria
    - Agregar comentarios descriptivos a cada esquema
    - _Requirements: 2.1_
  
  - [x] 1.2 Crear tablas del esquema persona
    - Implementar migración para tabla persona.usuario con campos de auditoría
    - Crear tabla persona.rol con roles del sistema
    - Crear tabla persona.usuario_rol para relación many-to-many
    - Agregar índices y comentarios SQL
    - _Requirements: 18.2, 18.3_
  
  - [x] 1.3 Crear tablas del esquema empresa
    - Implementar migración para tabla empresa.empresa con datos fiscales completos
    - Crear tabla empresa.usuario_empresa para acceso multi-empresa
    - Agregar índices para búsquedas frecuentes
    - _Requirements: 1.1, 1.2, 1.3, 2.1_
  
  - [x] 1.4 Crear tablas del esquema financiero
    - Implementar migración para tabla financiero.timbrado
    - Crear tabla financiero.timbrado_detalle con constraints de rango
    - Crear tabla financiero.factura_legal con totales por IVA
    - Crear tabla financiero.factura_legal_item
    - Agregar índices compuestos para queries de reportes
    - _Requirements: 3.1, 3.2, 4.1, 7.1, 8.1_
  
  - [x] 1.5 Crear tablas para documentos electrónicos
    - Crear ENUM types para estados (estado_de_enum, estado_lote_enum, estado_evento_enum)
    - Implementar migración para tabla financiero.documento_electronico
    - Crear tabla financiero.lote_de
    - Crear tabla financiero.evento_cancelacion_de
    - Agregar índices parciales para estados pendientes
    - _Requirements: 9.1, 9.6, 10.1, 12.1_
  
  - [x] 1.6 Crear tablas de productos y clientes
    - Implementar migración para tabla productos.producto
    - Crear tabla clientes.cliente con tipo_contribuyente
    - Agregar índices para búsquedas por texto (GIN para trigram)
    - _Requirements: 5.1, 6.1, 6.3_
  
  - [x] 1.7 Crear tabla de auditoría
    - Crear ENUM auditoria.accion_enum
    - Implementar migración para tabla auditoria.audit_log con campos JSONB
    - Agregar índices para consultas de historial
    - _Requirements: 17.1, 17.2, 17.3_
  
  - [x] 1.8 Crear función trigger para actualización automática de timestamps
    - Implementar función PL/pgSQL actualizar_timestamp_modificacion()
    - Aplicar trigger a todas las tablas con campo actualizado_en
    - _Requirements: 2.1_


- [x] 2. Implementar entidades JPA y repositorios base
  - [x] 2.1 Crear clase base AuditableEntity
    - Implementar @MappedSuperclass con campos de auditoría (creadoEn, creadoPor, actualizadoEn, actualizadoPor)
    - Configurar @EntityListeners con AuditingEntityListener
    - Crear configuración JpaAuditingConfig con AuditorAware
    - _Requirements: 2.1_
  
  - [x] 2.2 Implementar entidades del módulo persona
    - Crear entidad Usuario con relaciones a Rol y UsuarioEmpresa
    - Crear entidad Rol
    - Crear entidad UsuarioRol
    - Implementar repositorios: UsuarioRepository, RolRepository
    - Agregar queries personalizadas (findByUsername, findByEmail)
    - _Requirements: 18.1, 18.2_
  
  - [x] 2.3 Implementar entidades del módulo empresa
    - Crear entidad Empresa con todos los campos fiscales
    - Crear entidad UsuarioEmpresa con rol_empresa
    - Implementar repositorios: EmpresaRepository, UsuarioEmpresaRepository
    - Agregar query para findByUsuarioAndEmpresa
    - _Requirements: 1.1, 1.2, 2.1, 2.2_
  
  - [x] 2.4 Implementar entidades del módulo financiero
    - Crear entidad Timbrado con relación a Empresa
    - Crear entidad TimbradoDetalle con constraints de rango
    - Crear entidad FacturaLegal con relaciones a Empresa, TimbradoDetalle, Cliente
    - Crear entidad FacturaLegalItem con relación a FacturaLegal y Producto
    - Implementar repositorios con queries personalizadas
    - _Requirements: 3.1, 3.2, 4.1, 7.1, 8.1_
  
  - [x] 2.5 Implementar entidades de documentos electrónicos
    - Crear ENUMs: EstadoDE, EstadoLoteDE, EstadoEvento
    - Crear entidad DocumentoElectronico con @Type para PostgreSQL ENUM
    - Crear entidad LoteDE con relación bidireccional a DocumentoElectronico
    - Crear entidad EventoCancelacionDE
    - Implementar repositorios con queries por estado
    - _Requirements: 9.1, 9.6, 10.1, 11.1, 12.1_
  
  - [x] 2.6 Implementar entidades de productos y clientes
    - Crear entidad Producto con relación a Empresa
    - Crear entidad Cliente con tipo_contribuyente
    - Implementar repositorios con búsqueda por texto
    - _Requirements: 5.1, 5.2, 6.1, 6.2_
  
  - [x] 2.7 Implementar entidad de auditoría
    - Crear ENUM AccionEnum
    - Crear entidad AuditLog con campos JSONB para valores anteriores/nuevos
    - Implementar AuditLogRepository con queries por usuario, empresa, entidad
    - _Requirements: 17.1, 17.4_

- [x] 3. Implementar servicios de seguridad y autenticación
  - [x] 3.1 Configurar encriptación de datos sensibles
    - Crear EncryptionService con AES-256 para encriptar/desencriptar
    - Implementar métodos encrypt() y decrypt()
    - Configurar clave secreta en application.yml
    - _Requirements: 25.1, 25.2_
  
  - [x] 3.2 Implementar servicio de roles y permisos
    - Crear RolService con métodos para gestión de roles
    - Implementar EmpresaSecurityService con método hasAccess()
    - Crear anotación @PreAuthorize personalizada para verificar acceso a empresa
    - _Requirements: 18.4, 18.5, 18.6, 18.7, 18.8, 2.4_
  
  - [x] 3.3 Configurar Spring Security con RBAC
    - Actualizar SecurityConfig para incluir roles del sistema
    - Configurar @EnableMethodSecurity para seguridad a nivel de método
    - Implementar filtros de autorización por rol
    - _Requirements: 18.3, 18.4_
  
  - [x] 3.4 Crear DTOs para autenticación y usuarios
    - Crear UsuarioDto, RolDto, UsuarioEmpresaDto
    - Implementar mappers entre entidades y DTOs
    - _Requirements: 18.1_


- [x] 4. Implementar gestión de empresas
  - [x] 4.1 Crear servicio de empresas
    - Implementar EmpresaService con CRUD completo
    - Agregar validación de RUC paraguayo
    - Implementar método para asignar usuarios a empresa
    - Agregar verificación de permisos antes de operaciones
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_
  
  - [x] 4.2 Crear controlador REST de empresas
    - Implementar EmpresaController con endpoints CRUD
    - Agregar endpoint POST /api/empresas/{id}/usuarios para asignar usuarios
    - Implementar endpoint GET /api/empresas/mis-empresas para listar empresas del usuario
    - Configurar validaciones con @Valid
    - _Requirements: 1.3, 2.1, 2.5_
  
  - [x] 4.3 Crear DTOs y validadores de empresa
    - Crear EmpresaDto con validaciones de campos requeridos
    - Implementar validador personalizado para RUC paraguayo
    - Crear DomicilioFiscalDto y ActividadEconomicaDto
    - _Requirements: 1.1, 1.4, 23.1_
  
  - [ ]* 4.4 Crear tests para gestión de empresas
    - Escribir tests unitarios para EmpresaService
    - Crear tests de integración para EmpresaController
    - Verificar validación de RUC y permisos
    - _Requirements: 1.1, 1.4_

- [x] 5. Implementar gestión de timbrados
  - [x] 5.1 Crear servicio de timbrados
    - Implementar TimbradoService con CRUD y validación de fechas
    - Agregar método para verificar vigencia de timbrado
    - Implementar encriptación de CSC al guardar
    - _Requirements: 3.1, 3.2, 3.3, 3.5, 3.6_
  
  - [x] 5.2 Crear servicio de timbrados detalle
    - Implementar TimbradoDetalleService con gestión de rangos
    - Crear método incrementarNumeroActual() con lock optimista
    - Implementar validación de rango disponible
    - Agregar método para alertar cuando rango se agota
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.7_
  
  - [x] 5.3 Crear controladores REST de timbrados
    - Implementar TimbradoController con endpoints CRUD
    - Crear TimbradoDetalleController con endpoints para puntos de expedición
    - Agregar endpoint GET /api/timbrados/{id}/vigente para verificar vigencia
    - Implementar endpoint GET /api/timbrados-detalle/{id}/disponible
    - _Requirements: 3.1, 4.1, 19.2_
  
  - [x] 5.4 Crear DTOs de timbrados
    - Crear TimbradoDto con validaciones de fechas
    - Implementar TimbradoDetalleDto con validación de rangos
    - Agregar validadores personalizados para fechaInicio < fechaFin y rangoDesde < rangoHasta
    - _Requirements: 3.2, 3.5, 4.2_
  
  - [ ]* 5.5 Crear tests para timbrados
    - Escribir tests unitarios para validación de rangos
    - Crear tests de concurrencia para incrementarNumeroActual()
    - Verificar encriptación de CSC
    - _Requirements: 3.3, 4.4_

- [x] 6. Implementar gestión de productos
  - [x] 6.1 Crear servicio de productos
    - Implementar ProductoService con CRUD por empresa
    - Agregar validación de IVA (0, 5, 10)
    - Implementar búsqueda por descripción con filtros
    - Crear método para importación masiva desde Excel
    - _Requirements: 5.1, 5.2, 5.3, 5.6, 19.3_
  
  - [x] 6.2 Crear controlador REST de productos
    - Implementar ProductoController con endpoints CRUD
    - Agregar endpoint GET /api/productos/buscar con parámetros de búsqueda
    - Implementar endpoint POST /api/productos/importar para carga masiva
    - Configurar paginación y ordenamiento
    - _Requirements: 5.1, 5.6, 19.3_
  
  - [x] 6.3 Crear DTOs de productos
    - Crear ProductoDto con validaciones
    - Implementar validador para tasa de IVA válida
    - _Requirements: 5.1, 23.5_
  
  - [ ]* 6.4 Crear tests para productos
    - Escribir tests unitarios para ProductoService
    - Verificar validación de IVA
    - Probar búsqueda y filtros
    - _Requirements: 5.1, 5.2_


- [x] 7. Implementar gestión de clientes
  - [x] 7.1 Crear servicio de clientes
    - Implementar ClienteService con CRUD por empresa
    - Agregar validación de RUC cuando tributa=true
    - Implementar búsqueda por nombre, RUC o razón social
    - _Requirements: 6.1, 6.2, 6.4, 6.5_
  
  - [x] 7.2 Crear controlador REST de clientes
    - Implementar ClienteController con endpoints CRUD
    - Agregar endpoint GET /api/clientes/buscar con autocompletado
    - Configurar paginación para listados
    - _Requirements: 6.1, 6.4, 19.4_
  
  - [x] 7.3 Crear DTOs de clientes
    - Crear ClienteDto con validaciones condicionales
    - Implementar validador que requiere RUC si tributa=true
    - _Requirements: 6.1, 6.2, 6.3_
  
  - [ ]* 7.4 Crear tests para clientes
    - Escribir tests unitarios para ClienteService
    - Verificar validación condicional de RUC
    - Probar búsqueda y filtros
    - _Requirements: 6.1, 6.2_

- [x] 8. Implementar creación de facturas legales
  - [x] 8.1 Crear servicio de facturas legales
    - Implementar FacturaLegalService con método crearFactura()
    - Agregar lógica para obtener y incrementar numeroActual del timbrado detalle
    - Implementar cálculo automático de totales por tasa de IVA
    - Crear método para aplicar descuento final y recalcular totalFinal
    - Agregar validación de vigencia de timbrado
    - _Requirements: 7.1, 7.2, 7.5, 7.6, 7.7, 8.5_
  
  - [x] 8.2 Implementar gestión de items de factura
    - Crear método agregarItem() en FacturaLegalService
    - Implementar cálculo de total por item (cantidad * precioUnitario)
    - Agregar método recalcularTotalesFactura() que suma items y calcula IVA
    - Implementar método eliminarItem() con recálculo automático
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 8.6_
  
  - [x] 8.3 Crear controlador REST de facturas
    - Implementar FacturaLegalController con endpoint POST /api/facturas
    - Agregar endpoint GET /api/facturas con filtros (empresa, fecha, cliente)
    - Implementar endpoint PUT /api/facturas/{id} para edición
    - Crear endpoint GET /api/facturas/{id} con items incluidos
    - _Requirements: 7.1, 7.7, 16.2, 19.5_
  
  - [x] 8.4 Crear DTOs de facturas
    - Crear FacturaLegalDto con validaciones
    - Implementar FacturaLegalItemDto
    - Agregar validador que requiere al menos un item
    - Crear FacturaFiltroDto para búsquedas
    - _Requirements: 7.1, 7.7, 8.1_
  
  - [ ]* 8.5 Crear tests para facturas legales
    - Escribir tests unitarios para cálculo de totales
    - Verificar incremento de numeroActual
    - Probar validación de timbrado vencido
    - Crear tests de concurrencia para numeración
    - _Requirements: 7.2, 7.5, 7.6_

- [x] 9. Implementar generación de documentos electrónicos
  - [x] 9.1 Crear servicio de certificados digitales
    - **IMPORTANTE**: Implementar carga DINÁMICA de certificados por empresa (no global)
    - Implementar CertificadoService con método firmarXml(xml, empresaId)
    - El método debe cargar el certificado .pfx específico de la empresa desde el path almacenado
    - Desencriptar la contraseña del certificado usando EncryptionService
    - Agregar método validarCertificadoVigente(empresa) que verifica expiración y existencia del archivo
    - Implementar método validarCertificado(path, password) para testing de certificados
    - Usar Bouncy Castle o Apache Santuario para firma digital XML según especificación SIFEN
    - Liberar recursos (KeyStore) después de cada firma (no mantener en memoria)
    - _Requirements: 22.1, 22.2, 22.3, 22.4_
    - _Referencia_: Ver implementación en https://github.com/GabFrank/franco-system-backend-filial/tree/facturacion-electronica/src/main/java/com/franco/dev/service/sifen
  
  - [x] 9.2 Crear servicio de generación de XML
    - Implementar XmlGeneratorService con método generarXmlOriginal()
    - Crear templates XML según especificación SIFEN
    - Implementar método generarCDC() con formato de 44 caracteres
    - Agregar método generarUrlQr() para código QR
    - _Requirements: 9.2, 9.4, 23.3_
  
  - [x] 9.3 Implementar servicio de documentos electrónicos
    - Crear DocumentoElectronicoService con método generarDE(facturaLegal)
    - Validar que la empresa tiene certificado vigente antes de generar DE
    - Integrar XmlGeneratorService y CertificadoService
    - Implementar flujo: validar certificado → generar XML → firmar con certificado de empresa → crear CDC → generar QR
    - **IMPORTANTE**: Pasar empresaId al CertificadoService.firmarXml() para carga dinámica del certificado
    - Agregar método para asociar DE a lote
    - Implementar método consultarYActualizarEstado(deId) para sincronizar con SIFEN
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 9.6_
  
  - [x] 9.4 Crear controlador REST de documentos electrónicos
    - Implementar DocumentoElectronicoController
    - Agregar endpoint POST /api/facturas/{id}/generar-de
    - Crear endpoint GET /api/documentos-electronicos con filtros por estado
    - Implementar endpoint GET /api/documentos-electronicos/{id}/xml para descargar XML
    - _Requirements: 9.1, 9.7, 20.5_
  
  - [x] 9.5 Crear DTOs de documentos electrónicos
    - Crear DocumentoElectronicoDto
    - Implementar enum EstadoDEDto
    - _Requirements: 9.1, 9.6_
  
  - [ ]* 9.6 Crear tests para generación de DEs
    - Escribir tests unitarios para generación de CDC
    - Verificar firma digital de XML
    - Probar validación de certificado
    - _Requirements: 9.2, 9.4, 22.1_


- [ ] 10. Configurar integración con SIFEN (Fase 2)
  - [ ] 10.1 Configurar dependencia jsifenlib en Maven
    - Agregar GitHub Packages como repositorio en pom.xml
    - Configurar autenticación con token de GitHub
    - Agregar dependencia jsifenlib versión 0.2.4-frc.13
    - **NOTA**: Revisar implementación de referencia en https://github.com/GabFrank/franco-system-backend-filial/tree/facturacion-electronica
    - _Requirements: 21.1, 21.2, 21.3_
  
  - [ ] 10.2 Crear servicio de integración SIFEN
    - **IMPORTANTE**: El servicio debe ser stateless - no mantener configuración global de certificados
    - Implementar SifenService con configuración de ambiente (test/production)
    - Crear método enviarLote(lote, empresaId) que usa el certificado de la empresa específica
    - Implementar método consultarDE(cdc, empresaId) por CDC
    - Agregar método enviarEventoCancelacion(evento, empresaId)
    - **CRÍTICO**: Cada llamada a SIFEN debe usar el certificado de la empresa correspondiente
    - Configurar manejo de errores y timeouts
    - Implementar retry logic con backoff exponencial
    - _Requirements: 21.4, 21.5, 21.6, 21.7_
    - _Referencia_: Ver SifenService en repositorio de ejemplo para entender integración con jsifenlib
  
  - [ ] 10.3 Implementar servicio de lotes
    - Crear LoteDEService con método crearLote()
    - Implementar método enviarLote() que llama a SifenService
    - Agregar método consultarYActualizarEstado()
    - Implementar método reenviarLote() para reintentos
    - Agregar contador de intentos y backoff exponencial
    - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5, 10.6, 10.7_
  
  - [ ] 10.4 Crear controlador REST de lotes
    - Implementar LoteDEController
    - Agregar endpoint POST /api/lotes con lista de DE IDs
    - Crear endpoint POST /api/lotes/{id}/enviar
    - Implementar endpoint GET /api/lotes/{id}/consultar-estado
    - _Requirements: 10.1, 10.2_
  
  - [ ] 10.5 Crear DTOs de lotes
    - Crear LoteDEDto con lista de documentos
    - Implementar LoteResponseDto con respuesta de SIFEN
    - _Requirements: 10.1, 10.3_
  
  - [ ]* 10.6 Crear tests para integración SIFEN
    - Escribir tests unitarios con mock de SifenClient
    - Verificar manejo de errores de conexión
    - Probar lógica de reintentos
    - _Requirements: 21.6, 21.7_

- [ ] 11. Implementar consulta de estado de DEs
  - [ ] 11.1 Agregar métodos de consulta en DocumentoElectronicoService
    - Implementar método consultarYActualizarEstado() que usa SifenService
    - Agregar lógica para actualizar estado según respuesta SIFEN
    - Implementar almacenamiento de código y mensaje de respuesta
    - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5, 11.6_
  
  - [ ] 11.2 Crear endpoint de consulta manual
    - Agregar endpoint POST /api/documentos-electronicos/{id}/consultar en controller
    - Implementar respuesta con estado actualizado
    - _Requirements: 11.1, 11.6_
  
  - [ ]* 11.3 Crear tests para consulta de estado
    - Escribir tests unitarios para actualización de estado
    - Verificar manejo de diferentes respuestas SIFEN
    - _Requirements: 11.2, 11.5_

- [ ] 12. Implementar cancelación de documentos electrónicos
  - [ ] 12.1 Crear servicio de eventos de cancelación
    - Implementar EventoCancelacionService con método crearEvento()
    - Agregar generación de XML de evento firmado
    - Implementar método enviarEvento() usando SifenService
    - Crear método consultarEstadoEvento()
    - Agregar lógica para actualizar estado del DE cuando evento es aprobado
    - _Requirements: 12.1, 12.2, 12.3, 12.4, 12.5, 12.6, 12.7_
  
  - [ ] 12.2 Crear controlador REST de cancelaciones
    - Implementar EventoCancelacionController
    - Agregar endpoint POST /api/documentos-electronicos/{id}/cancelar
    - Crear endpoint GET /api/eventos-cancelacion/{id}/consultar
    - _Requirements: 12.1, 12.2_
  
  - [ ] 12.3 Crear DTOs de eventos de cancelación
    - Crear EventoCancelacionDto con motivo requerido
    - Implementar validador de motivo (mínimo 10 caracteres)
    - _Requirements: 12.2_
  
  - [ ]* 12.4 Crear tests para cancelación
    - Escribir tests unitarios para flujo de cancelación
    - Verificar actualización de estado del DE
    - Probar validación de motivo
    - _Requirements: 12.1, 12.5_


- [ ] 13. Implementar procesamiento asíncrono con schedulers
  - [ ] 13.1 Configurar Spring Scheduling
    - Crear SchedulerConfig con ThreadPoolTaskScheduler
    - Configurar pool de threads para tareas programadas
    - Habilitar @EnableScheduling
    - _Requirements: 13.1_
  
  - [ ] 13.2 Crear scheduler para consulta de lotes
    - Implementar SifenScheduledTasks con método consultarEstadoLotes()
    - Configurar ejecución cada 5 minutos con @Scheduled(fixedDelay)
    - Agregar lógica para consultar solo lotes EN_PROCESO
    - Implementar manejo de errores por lote
    - _Requirements: 13.2, 13.5_
  
  - [ ] 13.3 Crear scheduler para consulta de DEs pendientes
    - Implementar método consultarDEsPendientes() en SifenScheduledTasks
    - Configurar ejecución cada 10 minutos
    - Agregar filtro para DEs con CDC generado
    - _Requirements: 13.2, 13.3_
  
  - [ ] 13.4 Crear scheduler para reintentos de lotes con error
    - Implementar método reintentarLotesConError()
    - Configurar ejecución cada hora con @Scheduled(cron)
    - Agregar límite de 3 intentos antes de marcar como ERROR
    - Implementar backoff exponencial entre reintentos
    - _Requirements: 13.5, 13.6_
  
  - [ ] 13.5 Crear scheduler para alertas de certificados
    - Implementar método alertarCertificadosPorVencer()
    - Configurar ejecución diaria a las 8 AM
    - Buscar certificados que expiran en 30 días
    - Integrar con servicio de notificaciones
    - _Requirements: 22.4, 24.6_
  
  - [ ]* 13.6 Crear tests para schedulers
    - Escribir tests unitarios para lógica de schedulers
    - Verificar filtros de estados
    - Probar límite de reintentos
    - _Requirements: 13.2, 13.6_

- [ ] 14. Implementar sistema de eventos de dominio
  - [ ] 14.1 Crear eventos de dominio
    - Implementar DEAprobadoEvent
    - Crear DERechazadoEvent
    - Implementar LoteAprobadoEvent
    - Crear FacturaCreadaEvent
    - _Requirements: 13.4_
  
  - [ ] 14.2 Crear listeners de eventos
    - Implementar DocumentoElectronicoEventListener
    - Agregar método onDEAprobado() con @EventListener y @Async
    - Crear método onDERechazado() para notificaciones
    - Implementar método onLoteAprobado() para actualizar DEs del lote
    - _Requirements: 13.4_
  
  - [ ] 14.3 Configurar procesamiento asíncrono de eventos
    - Crear AsyncConfig con @EnableAsync
    - Configurar ThreadPoolTaskExecutor para eventos
    - _Requirements: 13.4_
  
  - [ ]* 14.4 Crear tests para eventos
    - Escribir tests unitarios para listeners
    - Verificar propagación de eventos
    - Probar procesamiento asíncrono
    - _Requirements: 13.4_

- [x] 15. Implementar sistema de auditoría
  - [x] 15.1 Crear anotación @Auditable
    - Implementar anotación personalizada con parámetros entity y action
    - Definir enum AccionEnum (CREATE, UPDATE, DELETE)
    - _Requirements: 17.1, 17.2, 17.3_
  
  - [x] 15.2 Crear aspecto de auditoría
    - Implementar AuditAspect con @Around("@annotation(Auditable)")
    - Agregar captura de valores anteriores para UPDATE
    - Implementar serialización a JSON de valores
    - Capturar IP address y user agent del request
    - Guardar registro en AuditLog
    - _Requirements: 17.1, 17.2, 17.6_
  
  - [x] 15.3 Crear servicio de auditoría
    - Implementar AuditLogService con método save()
    - Agregar método getUltimasActividades() para dashboard
    - Crear método getHistorialEntidad() para consultar cambios de un registro
    - Implementar búsqueda con filtros (usuario, fecha, entidad, acción)
    - _Requirements: 17.4, 17.5_
  
  - [x] 15.4 Aplicar @Auditable a servicios críticos
    - Agregar @Auditable a métodos de EmpresaService
    - Aplicar a FacturaLegalService
    - Agregar a TimbradoService y ProductoService
    - _Requirements: 17.1, 17.2, 17.3_
  
  - [x] 15.5 Crear controlador REST de auditoría
    - Implementar AuditLogController
    - Agregar endpoint GET /api/auditoria con filtros
    - Crear endpoint GET /api/auditoria/entidad/{tipo}/{id} para historial específico
    - _Requirements: 17.4, 17.5_
  
  - [ ]* 15.6 Crear tests para auditoría
    - Escribir tests unitarios para AuditAspect
    - Verificar captura de valores anteriores/nuevos
    - Probar inmutabilidad de registros de auditoría
    - _Requirements: 17.1, 17.7_


- [x] 16. Implementar dashboards y reportes
  - [x] 16.1 Crear servicio de dashboard de usuario
    - Implementar DashboardService con método getDashboardUsuario()
    - Calcular cantidad de empresas con acceso
    - Obtener últimas 10 actividades del usuario desde AuditLog
    - Contar facturas creadas en el mes actual
    - _Requirements: 14.1, 14.2, 14.3, 14.4_
  
  - [x] 16.2 Crear servicio de dashboard de empresa
    - Implementar método getDashboardEmpresa() en DashboardService
    - Calcular total de facturas emitidas
    - Sumar total en guaraníes del mes actual
    - Calcular totales por tasa de IVA (10%, 5%, 0%)
    - Generar ranking de top 10 clientes por monto
    - _Requirements: 15.1, 15.2, 15.3, 15.4_
  
  - [x] 16.3 Crear controladores REST de dashboards
    - Implementar DashboardController
    - Agregar endpoint GET /api/dashboard/usuario
    - Crear endpoint GET /api/dashboard/empresa/{id}
    - Implementar filtros de fecha para dashboard de empresa
    - _Requirements: 14.1, 15.1, 15.5_
  
  - [x] 16.4 Crear servicio de reportes
    - Implementar ReporteService con método reporteFacturas()
    - Crear Specification para filtros dinámicos (fecha, cliente, estado)
    - Implementar reportePorCliente() con agrupación
    - Agregar reportePorProducto() con cantidad y monto
    - Crear reportePorUsuario() listando facturas por creador
    - _Requirements: 16.1, 16.2, 16.3, 16.4, 16.5_
  
  - [x] 16.5 Implementar exportación de reportes
    - Agregar método exportarReporteExcel() usando Apache POI
    - Implementar método exportarReportePDF() usando iText o Flying Saucer
    - Crear templates para diferentes tipos de reportes
    - _Requirements: 16.6_
  
  - [x] 16.6 Crear controlador REST de reportes
    - Implementar ReporteController
    - Agregar endpoint GET /api/reportes/facturas con filtros
    - Crear endpoint GET /api/reportes/clientes
    - Implementar endpoint GET /api/reportes/productos
    - Agregar endpoint GET /api/reportes/usuarios
    - Crear endpoints de exportación: /api/reportes/{tipo}/excel y /api/reportes/{tipo}/pdf
    - _Requirements: 16.1, 16.2, 16.3, 16.4, 16.5, 16.6_
  
  - [x] 16.7 Crear DTOs de reportes
    - Crear FacturaReporteDto
    - Implementar ClienteRankingDto
    - Crear ProductoReporteDto
    - Implementar FacturaFiltroDto con todos los filtros posibles
    - _Requirements: 16.1, 16.7_
  
  - [ ]* 16.8 Crear tests para dashboards y reportes
    - Escribir tests unitarios para cálculos de dashboard
    - Verificar filtros de reportes
    - Probar exportación a Excel y PDF
    - _Requirements: 15.2, 16.1_

- [ ] 17. Implementar sistema de notificaciones
  - [ ] 17.1 Crear servicio de notificaciones
    - Implementar NotificacionService con método notificarDEAprobado()
    - Agregar método notificarDERechazado() con motivo
    - Crear método alertarCertificadoPorVencer()
    - Implementar método alertarRangoAgotado()
    - Agregar método alertarTimbradoPorVencer()
    - _Requirements: 24.1, 24.2, 24.3, 24.4, 24.6_
  
  - [ ] 17.2 Implementar almacenamiento de notificaciones
    - Crear entidad Notificacion con usuario, tipo, mensaje, leída
    - Implementar NotificacionRepository
    - Agregar método para marcar como leída
    - _Requirements: 24.1, 24.2, 24.3, 24.4_
  
  - [ ] 17.3 Crear controlador REST de notificaciones
    - Implementar NotificacionController
    - Agregar endpoint GET /api/notificaciones para listar no leídas
    - Crear endpoint PUT /api/notificaciones/{id}/leer
    - Implementar endpoint GET /api/notificaciones/count para badge
    - _Requirements: 24.1_
  
  - [ ]* 17.4 Crear tests para notificaciones
    - Escribir tests unitarios para NotificacionService
    - Verificar creación de notificaciones en eventos
    - _Requirements: 24.1, 24.2_


- [x] 18. Implementar validadores paraguayos
  - [x] 18.1 Crear validadores de datos fiscales
    - Implementar RucValidator con verificación de dígito verificador
    - Crear TimbradoValidator para formato de 8 dígitos
    - Implementar CdcValidator para formato de 44 caracteres
    - Agregar IvaValidator para tasas válidas (0, 5, 10)
    - _Requirements: 23.1, 23.2, 23.3, 23.5_
  
  - [x] 18.2 Crear anotaciones de validación personalizadas
    - Implementar @ValidRuc con validador
    - Crear @ValidTimbrado
    - Implementar @ValidIva
    - _Requirements: 23.1, 23.2, 23.5_
  
  - [x] 18.3 Agregar validaciones a DTOs
    - Aplicar @ValidRuc a campos RUC en EmpresaDto, ClienteDto
    - Agregar @ValidTimbrado a TimbradoDto
    - Aplicar @ValidIva a ProductoDto
    - _Requirements: 23.1, 23.2, 23.5_
  
  - [ ]* 18.4 Crear tests para validadores
    - Escribir tests unitarios para RucValidator
    - Verificar cálculo de dígito verificador
    - Probar validación de formato de timbrado
    - _Requirements: 23.1, 23.2_

- [x] 19. Implementar frontend - Módulo Core
  - [x] 19.1 Configurar estructura de proyecto Angular
    - Crear estructura de carpetas (core, shared, features)
    - Configurar routing principal con lazy loading
    - Instalar dependencias adicionales (NgRx, Chart.js)
    - _Requirements: 19.1_
  
  - [x] 19.2 Implementar servicios API
    - Crear EmpresaApiService con métodos HTTP
    - Implementar TimbradoApiService
    - Crear ProductoApiService
    - Implementar ClienteApiService
    - Crear FacturaApiService
    - Implementar DocumentoElectronicoApiService
    - Agregar ReporteApiService
    - _Requirements: 19.1_
  
  - [x] 19.3 Configurar NgRx store
    - Crear estructura de store (auth, empresas, facturacion, documentos)
    - Implementar actions para cada módulo
    - Crear reducers con estado inicial
    - Implementar effects para llamadas API
    - Agregar selectors para consultas de estado
    - _Requirements: 19.1_
  
  - [x] 19.4 Crear guards de autorización
    - Implementar RoleGuard para verificar roles del sistema
    - Crear EmpresaAccessGuard para verificar acceso a empresa
    - _Requirements: 2.4, 18.7_
  
  - [x] 19.5 Crear componentes compartidos
    - Implementar ConfirmDialogComponent para confirmaciones
    - Crear LoadingSpinnerComponent
    - Implementar ErrorMessageComponent
    - Crear DataTableComponent genérico con paginación
    - _Requirements: 19.1_

- [x] 20. Implementar frontend - Gestión de Empresas
  - [x] 20.1 Crear componente de lista de empresas
    - Implementar EmpresaListComponent con tabla Material
    - Agregar búsqueda y filtros
    - Implementar botones de acciones (editar, ver, desactivar)
    - Conectar con NgRx store
    - _Requirements: 1.3, 19.1_
  
  - [x] 20.2 Crear componente de formulario de empresa
    - Implementar EmpresaFormComponent con Reactive Forms
    - Agregar validaciones en tiempo real
    - Crear secciones: datos básicos, domicilio fiscal, actividad económica
    - Implementar carga de certificado .pfx
    - _Requirements: 1.1, 1.2, 19.1_
  
  - [x] 20.3 Crear componente de asignación de usuarios
    - Implementar UsuarioEmpresaComponent
    - Agregar tabla de usuarios con roles
    - Crear diálogo para asignar nuevo usuario
    - Implementar cambio de rol (ADMINISTRADOR/LECTOR)
    - _Requirements: 2.1, 2.2, 2.3_
  
  - [x] 20.4 Configurar routing de empresas
    - Crear módulo EmpresasModule con routing
    - Configurar lazy loading
    - Agregar guards de autorización
    - _Requirements: 19.1_


- [x] 21. Implementar frontend - Gestión de Timbrados
  - [x] 21.1 Crear componente de lista de timbrados
    - Implementar TimbradoListComponent con filtros por empresa
    - Agregar indicador visual de vigencia (activo/vencido)
    - Mostrar alertas para timbrados próximos a vencer
    - _Requirements: 3.1, 19.2_
  
  - [x] 21.2 Crear componente de formulario de timbrado
    - Implementar TimbradoFormComponent con validaciones de fechas
    - Agregar campos condicionales para timbrado electrónico (CSC)
    - Crear sección de domicilio fiscal
    - Implementar validación fechaInicio < fechaFin
    - _Requirements: 3.1, 3.2, 3.3_
  
  - [x] 21.3 Crear componente de gestión de puntos de expedición
    - Implementar TimbradoDetalleListComponent
    - Agregar indicador de números disponibles
    - Mostrar alerta cuando rango se agota
    - Crear formulario para nuevo punto de expedición
    - _Requirements: 4.1, 4.2, 4.5_
  
  - [x] 21.4 Configurar routing de timbrados
    - Crear módulo TimbradosModule con routing
    - Configurar lazy loading
    - _Requirements: 19.2_

- [x] 22. Implementar frontend - Gestión de Productos y Clientes
  - [x] 22.1 Crear componente de lista de productos
    - Implementar ProductoListComponent con búsqueda
    - Agregar filtros por IVA y estado
    - Implementar importación masiva desde Excel
    - _Requirements: 5.1, 5.6, 19.3_
  
  - [x] 22.2 Crear componente de formulario de producto
    - Implementar ProductoFormComponent
    - Agregar validación de IVA (0, 5, 10)
    - Crear campo para código de producto
    - _Requirements: 5.1, 5.2_
  
  - [x] 22.3 Crear componente de lista de clientes
    - Implementar ClienteListComponent con búsqueda
    - Agregar autocompletado para búsqueda rápida
    - Mostrar tipo de contribuyente
    - _Requirements: 6.1, 6.4, 19.4_
  
  - [x] 22.4 Crear componente de formulario de cliente
    - Implementar ClienteFormComponent
    - Agregar validación condicional de RUC (si tributa=true)
    - Crear selector de tipo de contribuyente
    - _Requirements: 6.1, 6.2, 6.3_
  
  - [x] 22.5 Configurar routing de productos y clientes
    - Crear módulos ProductosModule y ClientesModule
    - Configurar lazy loading para ambos
    - _Requirements: 19.3, 19.4_

- [x] 23. Implementar frontend - Facturación
  - [x] 23.1 Crear componente de lista de facturas
    - Implementar FacturaListComponent con tabla virtual scrolling
    - Agregar filtros (fecha, cliente, estado DE)
    - Mostrar estado del documento electrónico
    - Implementar acciones (ver, editar, generar DE, imprimir)
    - _Requirements: 7.1, 16.2, 19.5_
  
  - [x] 23.2 Crear componente de formulario de factura
    - Implementar FacturaFormComponent con Reactive Forms
    - Agregar selector de empresa y timbrado detalle
    - Crear autocompletado de cliente con búsqueda
    - Implementar gestión de items con FormArray
    - Agregar cálculo automático de totales en tiempo real
    - Mostrar desglose por tasa de IVA
    - Implementar campo de descuento final
    - _Requirements: 7.1, 7.2, 7.5, 7.6, 20.1, 20.2, 20.3, 20.4, 20.5_
  
  - [x] 23.3 Crear componente de item de factura
    - Implementar FacturaItemComponent para cada fila
    - Agregar autocompletado de producto
    - Implementar cálculo automático de total (cantidad * precio)
    - Permitir override de precio del producto
    - _Requirements: 8.1, 8.2, 8.3, 8.4_
  
  - [x] 23.4 Crear componente de visualización de factura
    - Implementar FacturaViewComponent para ver detalles
    - Mostrar información completa de factura e items
    - Agregar botón para generar DE
    - Implementar botón de impresión PDF
    - _Requirements: 7.1, 20.6_
  
  - [x] 23.5 Configurar routing de facturación
    - Crear módulo FacturacionModule con routing
    - Configurar lazy loading
    - Agregar guards de permisos
    - _Requirements: 19.5_


- [-] 24. Implementar frontend - Documentos Electrónicos
  - [x] 24.1 Crear componente de lista de documentos electrónicos
    - Implementar DocumentoElectronicoListComponent
    - Agregar filtros por estado (PENDIENTE, APROBADO, RECHAZADO, etc.)
    - Mostrar CDC y estado con colores
    - Implementar acciones (consultar estado, ver XML, cancelar)
    - _Requirements: 9.1, 9.6, 11.1_
  
  - [x] 24.2 Crear componente de visualización de DE
    - Implementar DocumentoElectronicoViewComponent
    - Mostrar información completa del DE
    - Agregar visualización de código QR
    - Implementar descarga de XML firmado
    - Mostrar respuesta de SIFEN si existe
    - _Requirements: 9.1, 9.4, 9.5_
  
  - [x] 24.3 Crear componente de gestión de lotes
    - Implementar LoteListComponent
    - Agregar funcionalidad para crear lote con DEs seleccionados
    - Mostrar estado del lote
    - Implementar botón de envío a SIFEN
    - Agregar consulta de estado
    - _Requirements: 10.1, 10.2, 10.3_
  
  - [x] 24.4 Crear diálogo de cancelación
    - Implementar CancelarDEDialogComponent
    - Agregar campo de motivo con validación (mínimo 10 caracteres)
    - Mostrar confirmación antes de enviar
    - _Requirements: 12.1, 12.2_
  
  - [x] 24.5 Configurar routing de documentos
    - Crear módulo DocumentosModule con routing
    - Configurar lazy loading
    - _Requirements: 9.1_

- [x] 25. Implementar frontend - Dashboards
  - [x] 25.1 Crear componente de dashboard de usuario
    - Implementar DashboardUsuarioComponent
    - Mostrar cards con métricas (cantidad empresas, último acceso)
    - Agregar lista de últimas actividades
    - Mostrar facturas creadas en el mes
    - _Requirements: 14.1, 14.2, 14.3, 14.4_
  
  - [x] 25.2 Crear componente de dashboard de empresa
    - Implementar DashboardEmpresaComponent
    - Mostrar cards con métricas principales
    - Agregar gráfico de torta para ventas por IVA usando Chart.js
    - Implementar ranking de clientes con lista Material
    - Agregar selector de rango de fechas
    - _Requirements: 15.1, 15.2, 15.3, 15.4, 15.5_
  
  - [x] 25.3 Crear componentes de métricas reutilizables
    - Implementar MetricCardComponent para mostrar números
    - Crear ChartCardComponent para gráficos
    - Implementar RankingListComponent genérico
    - _Requirements: 15.1_
  
  - [x] 25.4 Configurar routing de dashboards
    - Crear módulo DashboardModule con routing
    - Configurar lazy loading
    - _Requirements: 14.1, 15.1_

- [x] 26. Implementar frontend - Reportes
  - [x] 26.1 Crear componente de reportes de facturas
    - Implementar ReporteFacturasComponent
    - Agregar formulario de filtros (fechas, cliente, estado)
    - Mostrar tabla con resultados paginados
    - Implementar botones de exportación (Excel, PDF)
    - _Requirements: 16.1, 16.2, 16.6_
  
  - [x] 26.2 Crear componente de reporte por clientes
    - Implementar ReporteClientesComponent
    - Mostrar totales agrupados por cliente
    - Agregar gráfico de barras con top clientes
    - _Requirements: 16.3_
  
  - [x] 26.3 Crear componente de reporte por productos
    - Implementar ReporteProductosComponent
    - Mostrar cantidad vendida y monto por producto
    - Agregar filtros de fecha
    - _Requirements: 16.4_
  
  - [x] 26.4 Crear componente de reporte por usuarios
    - Implementar ReporteUsuariosComponent
    - Listar facturas creadas por cada usuario
    - Mostrar totales por usuario
    - _Requirements: 16.5_
  
  - [x] 26.5 Configurar routing de reportes
    - Crear módulo ReportesModule con routing
    - Configurar lazy loading
    - _Requirements: 16.1_

- [x] 27. Implementar frontend - Auditoría
  - [x] 27.1 Crear componente de historial de cambios
    - Implementar AuditoriaListComponent
    - Agregar filtros (usuario, fecha, entidad, acción)
    - Mostrar tabla con cambios registrados
    - Implementar visualización de valores anteriores/nuevos
    - _Requirements: 17.4, 17.5_
  
  - [x] 27.2 Crear componente de detalle de cambio
    - Implementar AuditoriaDetailComponent
    - Mostrar diff de valores anteriores vs nuevos
    - Resaltar campos modificados
    - _Requirements: 17.6_
  
  - [x] 27.3 Configurar routing de auditoría
    - Crear módulo AuditoriaModule con routing
    - Configurar lazy loading
    - Restringir acceso solo a ADMIN
    - _Requirements: 17.4_


- [ ] 28. Implementar frontend - Notificaciones
  - [ ] 28.1 Crear servicio de notificaciones en tiempo real
    - Implementar NotificacionService con polling cada 30 segundos
    - Agregar método para obtener notificaciones no leídas
    - Crear método para marcar como leída
    - Implementar contador de notificaciones
    - _Requirements: 24.1, 24.2, 24.3_
  
  - [ ] 28.2 Crear componente de badge de notificaciones
    - Implementar NotificacionBadgeComponent en toolbar
    - Mostrar contador de no leídas
    - Agregar menú desplegable con últimas notificaciones
    - Implementar navegación al hacer clic
    - _Requirements: 24.1_
  
  - [ ] 28.3 Crear componente de lista de notificaciones
    - Implementar NotificacionListComponent
    - Mostrar todas las notificaciones con paginación
    - Agregar filtros por tipo y estado
    - Implementar marcar todas como leídas
    - _Requirements: 24.1, 24.2_
  
  - [ ] 28.4 Integrar notificaciones con eventos
    - Conectar notificaciones con eventos de DE aprobado/rechazado
    - Agregar notificaciones para alertas de timbrados
    - Implementar notificaciones de certificados por vencer
    - _Requirements: 24.1, 24.2, 24.3, 24.4, 24.6_

- [ ] 29. Implementar optimizaciones de performance
  - [ ] 29.1 Configurar caching en backend
    - Implementar CacheConfig con Caffeine
    - Agregar @Cacheable a métodos de consulta frecuente
    - Configurar @CacheEvict en operaciones de actualización
    - Definir TTL apropiado para cada cache
    - _Requirements: Performance_
  
  - [ ] 29.2 Optimizar queries de base de datos
    - Revisar y optimizar queries N+1 con @EntityGraph
    - Agregar índices adicionales según análisis de queries lentas
    - Implementar paginación en todos los listados
    - _Requirements: Performance_
  
  - [ ] 29.3 Implementar lazy loading en frontend
    - Configurar lazy loading para todos los módulos de features
    - Implementar preloading strategy para módulos críticos
    - _Requirements: Performance_
  
  - [ ] 29.4 Optimizar componentes Angular
    - Aplicar OnPush change detection a componentes presentacionales
    - Implementar virtual scrolling en listas grandes
    - Agregar trackBy functions en *ngFor
    - _Requirements: Performance_

- [ ] 30. Implementar manejo de errores global
  - [ ] 30.1 Mejorar GlobalExceptionHandler en backend
    - Agregar manejo específico para todas las excepciones custom
    - Implementar logging estructurado de errores
    - Crear respuestas de error consistentes
    - _Requirements: Error Handling_
  
  - [ ] 30.2 Mejorar ErrorInterceptor en frontend
    - Implementar manejo de errores de red
    - Agregar retry logic para requests fallidos
    - Mostrar mensajes de error user-friendly
    - Implementar logging de errores
    - _Requirements: Error Handling_
  
  - [ ] 30.3 Crear componente de página de error
    - Implementar ErrorPageComponent para errores 404, 403, 500
    - Agregar navegación de regreso
    - _Requirements: Error Handling_

- [ ] 31. Configurar deployment y DevOps
  - [ ] 31.1 Crear Dockerfiles
    - Crear Dockerfile para backend con multi-stage build
    - Implementar Dockerfile para frontend con nginx
    - Optimizar tamaño de imágenes
    - _Requirements: Deployment_
  
  - [ ] 31.2 Configurar docker-compose para desarrollo
    - Crear docker-compose.yml con servicios (backend, frontend, postgres)
    - Agregar volúmenes para persistencia
    - Configurar networking entre servicios
    - _Requirements: Deployment_
  
  - [ ] 31.3 Configurar CI/CD con GitHub Actions
    - Crear workflow para backend (test, build, deploy)
    - Implementar workflow para frontend (test, build, deploy)
    - Agregar quality gates (coverage, linting)
    - _Requirements: Deployment_
  
  - [ ] 31.4 Configurar monitoring y logging
    - Implementar Spring Actuator endpoints
    - Configurar Prometheus metrics export
    - Agregar structured logging con Logback
    - Configurar log aggregation
    - _Requirements: Deployment_


- [ ] 32. Crear documentación del sistema
  - [ ] 32.1 Documentar APIs con OpenAPI
    - Agregar anotaciones @Operation a todos los endpoints
    - Documentar request/response schemas
    - Agregar ejemplos de uso
    - Configurar Swagger UI con autenticación JWT
    - _Requirements: Documentation_
  
  - [ ] 32.2 Crear guía de usuario
    - Escribir manual de usuario en español
    - Documentar flujos principales (crear empresa, facturar, generar DE)
    - Agregar capturas de pantalla
    - Crear sección de preguntas frecuentes
    - _Requirements: Documentation_
  
  - [ ] 32.3 Crear documentación técnica
    - Documentar arquitectura del sistema
    - Crear diagramas de entidad-relación
    - Documentar flujos de integración con SIFEN
    - Agregar guía de deployment
    - _Requirements: Documentation_
  
  - [ ] 32.4 Crear README completo
    - Documentar requisitos del sistema
    - Agregar instrucciones de instalación
    - Documentar configuración de variables de entorno
    - Crear guía de desarrollo local
    - _Requirements: Documentation_

- [ ] 33. Realizar testing integral
  - [ ]* 33.1 Completar tests unitarios de backend
    - Alcanzar 80% de cobertura en servicios
    - Escribir tests para validadores
    - Crear tests para cálculos de facturación
    - _Requirements: Testing_
  
  - [ ]* 33.2 Completar tests de integración
    - Crear tests end-to-end para flujos principales
    - Usar Testcontainers para tests con base de datos
    - Verificar integración entre módulos
    - _Requirements: Testing_
  
  - [ ]* 33.3 Completar tests de frontend
    - Alcanzar 70% de cobertura en componentes
    - Escribir tests para servicios
    - Crear tests para formularios complejos
    - _Requirements: Testing_
  
  - [ ]* 33.4 Realizar tests E2E con Cypress
    - Crear tests para flujo de facturación completo
    - Implementar tests para gestión de empresas
    - Agregar tests para generación de DEs
    - _Requirements: Testing_

- [ ] 34. Realizar pruebas de seguridad
  - [ ] 34.1 Auditar seguridad de autenticación
    - Verificar fortaleza de JWT tokens
    - Probar expiración y refresh de tokens
    - Validar protección contra ataques de fuerza bruta
    - _Requirements: Security_
  
  - [ ] 34.2 Auditar control de acceso
    - Verificar RBAC en todos los endpoints
    - Probar acceso multi-empresa
    - Validar que usuarios solo accedan a sus empresas
    - _Requirements: Security_
  
  - [ ] 34.3 Auditar encriptación de datos
    - Verificar encriptación de CSC y certificados
    - Probar que datos sensibles no se loguean
    - Validar transmisión segura a SIFEN
    - _Requirements: Security_
  
  - [ ] 34.4 Realizar penetration testing básico
    - Probar inyección SQL
    - Verificar protección XSS
    - Validar protección CSRF
    - Probar exposición de información sensible
    - _Requirements: Security_

- [ ] 35. Preparar para producción
  - [ ] 35.1 Configurar ambientes
    - Crear configuración para ambiente de test
    - Configurar ambiente de staging
    - Preparar configuración de producción
    - Documentar variables de entorno por ambiente
    - _Requirements: Deployment_
  
  - [ ] 35.2 Realizar pruebas de carga
    - Ejecutar tests de carga con JMeter o k6
    - Verificar performance bajo carga
    - Identificar y resolver cuellos de botella
    - _Requirements: Performance_
  
  - [ ] 35.3 Crear scripts de deployment
    - Crear script de inicialización de base de datos
    - Implementar script de backup
    - Crear script de rollback
    - Documentar proceso de deployment
    - _Requirements: Deployment_
  
  - [ ] 35.4 Configurar monitoreo de producción
    - Configurar alertas para errores críticos
    - Implementar health checks
    - Configurar dashboards de métricas
    - Agregar logging de auditoría
    - _Requirements: Deployment_

- [ ] 36. Realizar deployment inicial
  - [ ] 36.1 Desplegar en ambiente de staging
    - Ejecutar migraciones de base de datos
    - Desplegar backend
    - Desplegar frontend
    - Verificar funcionamiento completo
    - _Requirements: Deployment_
  
  - [ ] 36.2 Realizar pruebas de aceptación
    - Ejecutar casos de prueba principales
    - Verificar integración con SIFEN en ambiente de test
    - Validar reportes y dashboards
    - Probar flujo completo de facturación
    - _Requirements: Testing_
  
  - [ ] 36.3 Desplegar en producción
    - Ejecutar migraciones de producción
    - Desplegar servicios en producción
    - Configurar certificados SSL
    - Verificar conectividad con SIFEN producción
    - _Requirements: Deployment_
  
  - [ ] 36.4 Realizar smoke tests en producción
    - Verificar login y autenticación
    - Probar creación de empresa
    - Validar creación de factura
    - Verificar generación de DE
    - _Requirements: Testing_
