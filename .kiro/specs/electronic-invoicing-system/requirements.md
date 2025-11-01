# Requirements Document

## Introduction

Este documento define los requerimientos para la implementación del sistema completo de facturación electrónica FRC eFact. El sistema permitirá a las empresas gestionar su facturación legal y electrónica, integrándose con el sistema SIFEN del gobierno paraguayo. La plataforma incluye gestión multi-empresa, control de acceso basado en roles, productos, clientes, facturación legal, generación de documentos electrónicos, procesamiento por lotes, dashboards analíticos y reportes detallados.

## Requirements

### Requirement 1: Gestión de Empresas

**User Story:** Como usuario del sistema, quiero poder crear y gestionar múltiples empresas, para que pueda administrar la facturación de diferentes entidades comerciales desde una sola plataforma.

#### Acceptance Criteria

1. WHEN un usuario crea una empresa THEN el sistema SHALL almacenar razón social, RUC, y path del certificado .pfx
2. WHEN se registra una empresa THEN el sistema SHALL permitir configurar datos fiscales completos (domicilio, actividad económica, tipo de sociedad)
3. WHEN un usuario accede al sistema THEN el sistema SHALL mostrar todas las empresas a las que tiene acceso
4. WHEN se edita una empresa THEN el sistema SHALL validar que el RUC tenga formato válido paraguayo
5. IF una empresa es desactivada THEN el sistema SHALL mantener el historial pero prevenir nuevas operaciones

### Requirement 2: Control de Acceso Multi-Empresa

**User Story:** Como administrador, quiero asignar usuarios a empresas con diferentes niveles de acceso, para que pueda controlar quién puede ver y modificar información de cada empresa.

#### Acceptance Criteria

1. WHEN se asigna un usuario a una empresa THEN el sistema SHALL permitir definir rol como lector o administrador
2. WHEN un usuario tiene rol de lector THEN el sistema SHALL permitir solo visualización de datos
3. WHEN un usuario tiene rol de administrador THEN el sistema SHALL permitir crear, editar y eliminar registros
4. WHEN un usuario accede a una empresa THEN el sistema SHALL verificar permisos antes de cada operación
5. WHEN se lista empresas THEN el sistema SHALL mostrar solo empresas donde el usuario tiene acceso
6. IF un usuario pierde acceso a una empresa THEN el sistema SHALL revocar inmediatamente todos los permisos

### Requirement 3: Gestión de Timbrados

**User Story:** Como administrador de empresa, quiero registrar y gestionar timbrados fiscales, para que pueda emitir facturas legales con la numeración autorizada por la SET.

#### Acceptance Criteria

1. WHEN se crea un timbrado THEN el sistema SHALL asociarlo a una empresa específica
2. WHEN se registra un timbrado THEN el sistema SHALL almacenar número, fechas de vigencia, y si es electrónico
3. WHEN un timbrado es electrónico THEN el sistema SHALL requerir CSC (Código de Seguridad del Contribuyente)
4. WHEN se configura un timbrado THEN el sistema SHALL permitir ingresar datos del domicilio fiscal completo
5. WHEN se valida un timbrado THEN el sistema SHALL verificar que las fechas de vigencia sean coherentes
6. IF un timbrado expira THEN el sistema SHALL prevenir su uso en nuevas facturas

### Requirement 4: Gestión de Timbrados Detalle (Puntos de Expedición)

**User Story:** Como administrador, quiero configurar puntos de expedición para cada timbrado, para que pueda gestionar múltiples sucursales o cajas de facturación.

#### Acceptance Criteria

1. WHEN se crea un timbrado detalle THEN el sistema SHALL asociarlo a un timbrado padre
2. WHEN se configura un punto de expedición THEN el sistema SHALL definir punto de expedición y código de establecimiento
3. WHEN se establece rango de numeración THEN el sistema SHALL validar que rangoDesde < rangoHasta
4. WHEN se emite una factura THEN el sistema SHALL incrementar automáticamente numeroActual
5. WHEN numeroActual alcanza rangoHasta THEN el sistema SHALL alertar que el rango se agotó
6. WHEN se configura ubicación THEN el sistema SHALL almacenar departamento, ciudad, código de ciudad, localidad, barrio y dirección
7. IF se intenta usar un timbrado detalle sin números disponibles THEN el sistema SHALL rechazar la operación

### Requirement 5: Gestión de Productos

**User Story:** Como usuario de empresa, quiero crear y mantener un catálogo de productos, para que pueda facturar items con información consistente y correcta.

#### Acceptance Criteria

1. WHEN se crea un producto THEN el sistema SHALL requerir descripción, precio e IVA (0%, 5% o 10%)
2. WHEN se configura un producto THEN el sistema SHALL permitir marcar si requiere balanza
3. WHEN se lista productos THEN el sistema SHALL mostrar solo productos activos por defecto
4. WHEN se edita un producto THEN el sistema SHALL mantener historial de cambios de precio
5. WHEN se desactiva un producto THEN el sistema SHALL mantenerlo en facturas históricas pero no permitir uso en nuevas
6. IF se busca un producto THEN el sistema SHALL permitir filtrar por descripción, código o estado

### Requirement 6: Gestión de Clientes

**User Story:** Como usuario, quiero registrar y gestionar información de clientes, para que pueda emitir facturas con datos fiscales correctos.

#### Acceptance Criteria

1. WHEN se crea un cliente THEN el sistema SHALL almacenar nombre, razón social y si tributa
2. WHEN un cliente tributa THEN el sistema SHALL requerir RUC válido
3. WHEN se configura un cliente THEN el sistema SHALL permitir definir tipo de contribuyente (PF, PJ, EG)
4. WHEN se busca un cliente THEN el sistema SHALL permitir búsqueda por nombre, RUC o razón social
5. WHEN se lista clientes THEN el sistema SHALL mostrar solo clientes activos por defecto
6. IF un cliente es desactivado THEN el sistema SHALL mantener historial pero prevenir uso en nuevas facturas

### Requirement 7: Creación de Facturas Legales

**User Story:** Como usuario autorizado, quiero crear facturas legales con items de productos, para que pueda documentar ventas de forma legal y estructurada.

#### Acceptance Criteria

1. WHEN se crea una factura THEN el sistema SHALL asociarla a una empresa y timbrado detalle
2. WHEN se genera número de factura THEN el sistema SHALL usar el numeroActual del timbrado detalle e incrementarlo
3. WHEN se selecciona un cliente THEN el sistema SHALL autocompletar nombre, RUC y dirección
4. WHEN se agregan items THEN el sistema SHALL permitir seleccionar productos con cantidad y precio unitario
5. WHEN se calculan totales THEN el sistema SHALL separar IVA y totales por tasa (0%, 5%, 10%)
6. WHEN se aplica descuento final THEN el sistema SHALL recalcular totalFinal correctamente
7. WHEN se guarda una factura THEN el sistema SHALL validar que tenga al menos un item
8. IF la factura es a crédito THEN el sistema SHALL marcar el campo credito como true

### Requirement 8: Items de Factura Legal

**User Story:** Como usuario, quiero agregar múltiples items a una factura, para que pueda facturar varios productos en una sola transacción.

#### Acceptance Criteria

1. WHEN se agrega un item THEN el sistema SHALL asociarlo a una factura legal y producto
2. WHEN se ingresa cantidad THEN el sistema SHALL permitir decimales para productos de balanza
3. WHEN se define precio unitario THEN el sistema SHALL permitir modificarlo del precio base del producto
4. WHEN se calcula total del item THEN el sistema SHALL multiplicar cantidad por precio unitario
5. WHEN se modifica un item THEN el sistema SHALL recalcular totales de la factura automáticamente
6. IF se elimina un item THEN el sistema SHALL recalcular totales de la factura

### Requirement 9: Generación de Documentos Electrónicos

**User Story:** Como usuario, quiero generar documentos electrónicos a partir de facturas legales, para que pueda cumplir con la normativa de facturación electrónica de SIFEN.

#### Acceptance Criteria

1. WHEN se crea un documento electrónico THEN el sistema SHALL asociarlo a una factura legal única
2. WHEN se genera el DE THEN el sistema SHALL crear XML original con datos de la factura
3. WHEN se firma el DE THEN el sistema SHALL usar el certificado .pfx de la empresa para generar XML firmado
4. WHEN se genera CDC THEN el sistema SHALL crear código de control único según especificación SIFEN
5. WHEN se crea el DE THEN el sistema SHALL generar URL de código QR
6. WHEN se inicializa un DE THEN el sistema SHALL establecer estado como PENDIENTE
7. IF el XML no puede ser firmado THEN el sistema SHALL registrar error y mantener estado PENDIENTE

### Requirement 10: Gestión de Lotes de Documentos Electrónicos

**User Story:** Como usuario, quiero agrupar documentos electrónicos en lotes para envío a SIFEN, para que pueda procesar múltiples facturas eficientemente.

#### Acceptance Criteria

1. WHEN se crea un lote THEN el sistema SHALL permitir agregar múltiples DEs en estado PENDIENTE
2. WHEN se envía un lote THEN el sistema SHALL cambiar estado a EN_PROCESO
3. WHEN SIFEN responde THEN el sistema SHALL actualizar estado del lote según respuesta (APROBADO, RECHAZADO)
4. WHEN un lote es procesado THEN el sistema SHALL almacenar protocolo y respuesta completa de SIFEN
5. WHEN un lote falla THEN el sistema SHALL incrementar contador de intentos
6. WHEN se consulta estado de lote THEN el sistema SHALL permitir actualización desde SIFEN
7. IF un lote es aprobado THEN el sistema SHALL actualizar estado de todos los DEs asociados

### Requirement 11: Consulta de Estado de Documentos Electrónicos

**User Story:** Como usuario, quiero consultar el estado de documentos electrónicos en SIFEN, para que pueda verificar si fueron aprobados o rechazados.

#### Acceptance Criteria

1. WHEN se consulta un DE THEN el sistema SHALL usar el CDC para buscar en SIFEN
2. WHEN SIFEN responde THEN el sistema SHALL actualizar estado del DE (APROBADO, RECHAZADO, CANCELADO)
3. WHEN se recibe respuesta THEN el sistema SHALL almacenar código y mensaje de respuesta de SIFEN
4. WHEN un DE es aprobado THEN el sistema SHALL registrar fecha de recepción en SIFEN
5. WHEN un DE es rechazado THEN el sistema SHALL mostrar motivo del rechazo
6. IF la consulta falla THEN el sistema SHALL mantener estado actual y permitir reintento

### Requirement 12: Cancelación de Documentos Electrónicos

**User Story:** Como usuario autorizado, quiero cancelar documentos electrónicos ya emitidos, para que pueda anular facturas según normativa de SIFEN.

#### Acceptance Criteria

1. WHEN se solicita cancelación THEN el sistema SHALL crear un evento de cancelación asociado al DE
2. WHEN se genera evento THEN el sistema SHALL requerir motivo de cancelación
3. WHEN se envía evento THEN el sistema SHALL crear XML de evento firmado con certificado
4. WHEN SIFEN procesa evento THEN el sistema SHALL actualizar estado del evento (APROBADO, RECHAZADO)
5. WHEN evento es aprobado THEN el sistema SHALL cambiar estado del DE a CANCELADO
6. WHEN se almacena respuesta THEN el sistema SHALL guardar protocolo de autorización de SIFEN
7. IF el evento es rechazado THEN el sistema SHALL mantener DE en estado original y mostrar motivo

### Requirement 13: Procesamiento Reactivo y Schedulers

**User Story:** Como administrador del sistema, quiero que el estado de lotes y DEs se actualice automáticamente, para que no tenga que consultar manualmente cada documento.

#### Acceptance Criteria

1. WHEN se configura scheduler THEN el sistema SHALL ejecutar consultas periódicas a SIFEN
2. WHEN se consultan lotes THEN el sistema SHALL priorizar lotes en estado EN_PROCESO
3. WHEN se consultan DEs THEN el sistema SHALL actualizar solo DEs en estados transitorios
4. WHEN se detecta cambio de estado THEN el sistema SHALL notificar a usuarios interesados
5. WHEN hay error de conexión THEN el sistema SHALL reintentar con backoff exponencial
6. IF un lote tiene más de 3 intentos fallidos THEN el sistema SHALL marcar como ERROR y alertar

### Requirement 14: Dashboard de Usuario

**User Story:** Como usuario, quiero ver un dashboard con métricas personales, para que pueda monitorear mi actividad en el sistema.

#### Acceptance Criteria

1. WHEN un usuario accede al dashboard THEN el sistema SHALL mostrar cantidad de empresas con acceso
2. WHEN se carga el dashboard THEN el sistema SHALL mostrar fecha y hora del último acceso
3. WHEN se visualizan actividades THEN el sistema SHALL listar últimas 10 acciones realizadas
4. WHEN se muestran métricas THEN el sistema SHALL incluir cantidad de facturas creadas en el mes
5. IF el usuario no tiene actividad reciente THEN el sistema SHALL mostrar mensaje de bienvenida

### Requirement 15: Dashboard de Empresa

**User Story:** Como administrador de empresa, quiero ver métricas de facturación, para que pueda analizar el desempeño comercial.

#### Acceptance Criteria

1. WHEN se accede al dashboard de empresa THEN el sistema SHALL mostrar total de facturas emitidas
2. WHEN se calculan totales THEN el sistema SHALL mostrar total en guaraníes del mes actual
3. WHEN se desglosan montos THEN el sistema SHALL separar totales por tasa de IVA (10%, 5%, 0%)
4. WHEN se genera ranking THEN el sistema SHALL listar top 10 clientes por monto total facturado
5. WHEN se filtran datos THEN el sistema SHALL permitir seleccionar rango de fechas
6. IF no hay datos en el período THEN el sistema SHALL mostrar mensaje indicativo

### Requirement 16: Reportes de Facturación

**User Story:** Como usuario, quiero generar reportes detallados de facturación, para que pueda analizar ventas y cumplir con obligaciones fiscales.

#### Acceptance Criteria

1. WHEN se genera reporte por empresa THEN el sistema SHALL mostrar total de ventas y total de IVA por tasa
2. WHEN se lista facturas THEN el sistema SHALL permitir filtrar por fecha, cliente, estado y monto
3. WHEN se reporta por cliente THEN el sistema SHALL agrupar facturas y mostrar totales por cliente
4. WHEN se reporta por producto THEN el sistema SHALL mostrar cantidad vendida y monto total por producto
5. WHEN se reporta por usuario THEN el sistema SHALL listar facturas creadas por cada usuario
6. WHEN se exporta reporte THEN el sistema SHALL permitir descarga en PDF y Excel
7. IF se aplican filtros THEN el sistema SHALL mantener filtros al paginar resultados

### Requirement 17: Historial de Modificaciones (Audit Trail)

**User Story:** Como auditor, quiero consultar el historial completo de modificaciones, para que pueda rastrear cambios en registros críticos.

#### Acceptance Criteria

1. WHEN se crea un registro THEN el sistema SHALL registrar usuario, fecha y acción CREATE
2. WHEN se modifica un registro THEN el sistema SHALL registrar usuario, fecha, acción UPDATE y valores anteriores
3. WHEN se elimina un registro THEN el sistema SHALL registrar usuario, fecha y acción DELETE
4. WHEN se consulta historial THEN el sistema SHALL mostrar todos los cambios de un registro específico
5. WHEN se filtra historial THEN el sistema SHALL permitir búsqueda por usuario, fecha, entidad y acción
6. WHEN se visualiza cambio THEN el sistema SHALL mostrar valores antes y después de la modificación
7. IF se intenta modificar historial THEN el sistema SHALL rechazar la operación (tabla inmutable)

### Requirement 18: Sistema de Roles y Permisos

**User Story:** Como administrador del sistema, quiero gestionar roles de usuarios, para que pueda controlar acceso a funcionalidades según responsabilidades.

#### Acceptance Criteria

1. WHEN se crea un usuario THEN el sistema SHALL permitir asignar uno o más roles
2. WHEN se definen roles THEN el sistema SHALL incluir ADMIN, EMPRESA_ADMIN, FACTURADOR, LECTOR
3. WHEN un usuario tiene rol ADMIN THEN el sistema SHALL permitir acceso completo a todas las funcionalidades
4. WHEN un usuario tiene rol EMPRESA_ADMIN THEN el sistema SHALL permitir gestión completa de su empresa
5. WHEN un usuario tiene rol FACTURADOR THEN el sistema SHALL permitir crear y editar facturas
6. WHEN un usuario tiene rol LECTOR THEN el sistema SHALL permitir solo visualización de datos
7. WHEN se verifica permiso THEN el sistema SHALL validar rol antes de ejecutar operación
8. IF un usuario no tiene permiso THEN el sistema SHALL retornar error 403 Forbidden

### Requirement 19: Pantallas de Gestión

**User Story:** Como usuario, quiero interfaces intuitivas para gestionar entidades, para que pueda realizar operaciones de forma eficiente.

#### Acceptance Criteria

1. WHEN se accede a gestión de empresas THEN el sistema SHALL mostrar lista con búsqueda y filtros
2. WHEN se accede a gestión de timbrados THEN el sistema SHALL permitir CRUD completo con validaciones
3. WHEN se accede a gestión de productos THEN el sistema SHALL incluir importación masiva desde Excel
4. WHEN se accede a gestión de clientes THEN el sistema SHALL permitir búsqueda rápida por RUC o nombre
5. WHEN se crea/edita registro THEN el sistema SHALL mostrar formulario con validaciones en tiempo real
6. WHEN se lista registros THEN el sistema SHALL incluir paginación, ordenamiento y exportación
7. IF hay errores de validación THEN el sistema SHALL mostrar mensajes claros junto a cada campo

### Requirement 20: Pantalla de Facturación

**User Story:** Como facturador, quiero una interfaz ágil para crear facturas, para que pueda procesar ventas rápidamente.

#### Acceptance Criteria

1. WHEN se crea factura THEN el sistema SHALL permitir selección rápida de cliente con autocompletado
2. WHEN se agregan items THEN el sistema SHALL permitir búsqueda de productos por código o descripción
3. WHEN se modifica cantidad o precio THEN el sistema SHALL recalcular totales en tiempo real
4. WHEN se visualizan totales THEN el sistema SHALL mostrar desglose por tasa de IVA
5. WHEN se guarda factura THEN el sistema SHALL ofrecer opción de generar DE inmediatamente
6. WHEN se imprime factura THEN el sistema SHALL generar PDF con formato legal
7. IF hay productos sin stock THEN el sistema SHALL alertar antes de guardar

### Requirement 21: Integración con SIFEN (Fase 2)

**User Story:** Como administrador, quiero conectar el sistema con SIFEN, para que pueda enviar documentos electrónicos al gobierno automáticamente.

#### Acceptance Criteria

1. WHEN se configura integración THEN el sistema SHALL usar librería jsifenlib versión 0.2.4-frc.13
2. WHEN se configura Maven THEN el sistema SHALL agregar GitHub Packages como repositorio
3. WHEN se autentica con GitHub THEN el sistema SHALL usar token proporcionado
4. WHEN se envía lote THEN el sistema SHALL usar jsifenlib para comunicación con SIFEN
5. WHEN se consulta estado THEN el sistema SHALL usar métodos de la librería para obtener respuesta
6. WHEN hay error de conexión THEN el sistema SHALL registrar error y permitir reintento manual
7. IF SIFEN está en mantenimiento THEN el sistema SHALL encolar operaciones para reintento automático

### Requirement 22: Configuración de Certificados Digitales

**User Story:** Como administrador de empresa, quiero cargar y gestionar certificados digitales, para que el sistema pueda firmar documentos electrónicos.

#### Acceptance Criteria

1. WHEN se carga certificado THEN el sistema SHALL validar formato .pfx
2. WHEN se almacena certificado THEN el sistema SHALL encriptar el archivo en el servidor
3. WHEN se usa certificado THEN el sistema SHALL requerir contraseña para desencriptar
4. WHEN expira certificado THEN el sistema SHALL alertar con 30 días de anticipación
5. WHEN se renueva certificado THEN el sistema SHALL mantener historial de certificados anteriores
6. IF el certificado es inválido THEN el sistema SHALL rechazar carga y mostrar motivo

### Requirement 23: Validaciones de Datos Fiscales

**User Story:** Como desarrollador, quiero que el sistema valide datos fiscales paraguayos, para que se cumplan normativas locales.

#### Acceptance Criteria

1. WHEN se ingresa RUC THEN el sistema SHALL validar formato y dígito verificador
2. WHEN se ingresa número de timbrado THEN el sistema SHALL validar formato numérico de 8 dígitos
3. WHEN se genera CDC THEN el sistema SHALL seguir especificación de SIFEN (44 caracteres)
4. WHEN se valida fecha de factura THEN el sistema SHALL verificar que esté dentro de vigencia del timbrado
5. WHEN se calcula IVA THEN el sistema SHALL usar tasas válidas en Paraguay (0%, 5%, 10%)
6. IF datos no cumplen formato THEN el sistema SHALL mostrar mensaje de error específico

### Requirement 24: Notificaciones y Alertas

**User Story:** Como usuario, quiero recibir notificaciones sobre eventos importantes, para que pueda tomar acciones oportunas.

#### Acceptance Criteria

1. WHEN un timbrado está por vencer THEN el sistema SHALL notificar con 15 días de anticipación
2. WHEN un rango de numeración se agota THEN el sistema SHALL alertar al administrador
3. WHEN un lote es rechazado THEN el sistema SHALL notificar al usuario que lo creó
4. WHEN un DE es aprobado THEN el sistema SHALL notificar confirmación
5. WHEN hay error en scheduler THEN el sistema SHALL alertar a administradores del sistema
6. IF un certificado expira THEN el sistema SHALL enviar alertas escaladas (30, 15, 7, 1 día antes)

### Requirement 25: Seguridad y Encriptación

**User Story:** Como responsable de seguridad, quiero que datos sensibles estén protegidos, para que se cumplan estándares de seguridad.

#### Acceptance Criteria

1. WHEN se almacena certificado .pfx THEN el sistema SHALL encriptarlo con AES-256
2. WHEN se almacena CSC THEN el sistema SHALL encriptarlo en base de datos
3. WHEN se transmite a SIFEN THEN el sistema SHALL usar HTTPS/TLS 1.2+
4. WHEN se registra en audit trail THEN el sistema SHALL NO almacenar datos sensibles (passwords, CSC)
5. WHEN se exportan reportes THEN el sistema SHALL permitir solo a usuarios autorizados
6. IF se detecta intento de acceso no autorizado THEN el sistema SHALL registrar y bloquear IP temporalmente
