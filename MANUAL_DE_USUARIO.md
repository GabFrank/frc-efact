# Manual de Usuario - Sistema FRC eFact

## Índice
1. [Introducción](#introducción)
2. [Acceso al Sistema](#acceso-al-sistema)
3. [Dashboard Principal](#dashboard-principal)
4. [Gestión de Empresas](#gestión-de-empresas)
5. [Gestión de Timbrados](#gestión-de-timbrados)
6. [Gestión de Productos](#gestión-de-productos)
7. [Gestión de Clientes](#gestión-de-clientes)
8. [Facturación](#facturación)
9. [Documentos Electrónicos](#documentos-electrónicos)
10. [Reportes](#reportes)
11. [Auditoría](#auditoría)
12. [Configuración de Usuario](#configuración-de-usuario)

---

## Introducción

FRC eFact es un sistema integral de facturación electrónica diseñado para cumplir con las normativas paraguayas de la SET (Subsecretaría de Estado de Tributación). El sistema permite gestionar empresas, emitir facturas legales, generar documentos electrónicos y mantener un control completo de la facturación.

### Características principales:
- ✅ Facturación electrónica conforme a SIFEN
- ✅ Gestión multi-empresa
- ✅ Control de timbrados fiscales
- ✅ Generación de documentos electrónicos (DE)
- ✅ Reportes y dashboards
- ✅ Sistema de auditoría completo
- ✅ Validaciones paraguayas (RUC, CDC, Timbrados)

---

## Acceso al Sistema

### 1. Inicio de Sesión

1. **Abrir el navegador** y navegar a la URL del sistema
2. **Ingresar credenciales**:
   - **Usuario**: `admin`
   - **Contraseña**: `Admin123!`
3. **Hacer clic en "Iniciar Sesión"**

### 2. Usuarios por Defecto

El sistema viene con los siguientes usuarios preconfigurados:

| Usuario | Contraseña | Rol | Descripción |
|---------|------------|-----|-------------|
| `admin` | `Admin123!` | ADMIN | Administrador del sistema |
| `empresa_admin` | `Empresa123!` | EMPRESA_ADMIN | Administrador de empresa |
| `facturador` | `Facturador123!` | FACTURADOR | Usuario facturador |
| `lector` | `Lector123!` | LECTOR | Usuario solo lectura |

### 3. Recuperación de Contraseña

Si olvida su contraseña, contacte al administrador del sistema para restablecerla.

---

## Dashboard Principal

Al iniciar sesión, verá el dashboard principal que muestra:

### Dashboard de Usuario
- **Cantidad de empresas** con acceso
- **Último acceso** al sistema
- **Facturas creadas** en el mes actual
- **Actividades recientes** del usuario

### Dashboard de Empresa (si tiene acceso)
- **Total de facturas emitidas**
- **Total facturado** en el período
- **Desglose por IVA** (0%, 5%, 10%)
- **Ranking de clientes** por monto facturado

---

## Gestión de Empresas

### 1. Acceder a Empresas
- En el menú lateral, hacer clic en **"Empresas"**

### 2. Crear Nueva Empresa

1. **Hacer clic en "Nueva Empresa"**
2. **Completar los datos básicos**:
   - Razón Social (obligatorio)
   - RUC (formato paraguayo: 12345678-9)
   - Nombre de Fantasía
   - Email
   - Teléfono
   - Dirección

3. **Completar datos fiscales**:
   - Tipo de Sociedad
   - Domicilio Fiscal (Departamento, Ciudad, etc.)
   - Actividad Económica Principal
   - Actividades Económicas Secundarias

4. **Configurar certificado digital** (opcional):
   - Subir archivo .pfx
   - Ingresar contraseña del certificado
   - Fecha de expiración

5. **Hacer clic en "Guardar"**

### 3. Gestionar Usuarios de Empresa

1. **Seleccionar una empresa** de la lista
2. **Hacer clic en "Gestionar Usuarios"**
3. **Asignar usuarios**:
   - Seleccionar usuario del sistema
   - Asignar rol (ADMINISTRADOR o LECTOR)
   - Activar/desactivar acceso

### 4. Editar Empresa

1. **Hacer clic en el ícono de edición** junto a la empresa
2. **Modificar los datos** necesarios
3. **Guardar cambios**

---

## Gestión de Timbrados

### 1. Acceder a Timbrados
- En el menú lateral, hacer clic en **"Timbrados"**

### 2. Crear Nuevo Timbrado

1. **Seleccionar empresa** (si tiene acceso a múltiples)
2. **Hacer clic en "Nuevo Timbrado"**
3. **Completar información del timbrado**:
   - Número de Timbrado (8 dígitos)
   - Razón Social
   - RUC
   - Fecha de Inicio
   - Fecha de Fin
   - Tipo: Físico o Electrónico

4. **Para timbrados electrónicos**:
   - Ingresar CSC (Código de Seguridad del Contribuyente)
   - Completar datos adicionales requeridos por SIFEN

5. **Guardar timbrado**

### 3. Gestionar Puntos de Expedición

1. **Seleccionar un timbrado** de la lista
2. **Hacer clic en "Puntos de Expedición"**
3. **Agregar nuevo punto**:
   - Punto de Expedición (ej: 001)
   - Código de Establecimiento (ej: 001)
   - Rango de numeración (desde - hasta)
   - Ubicación del punto

4. **Guardar punto de expedición**

### 4. Monitorear Estado de Timbrados

- **Verde**: Timbrado activo y vigente
- **Amarillo**: Próximo a vencer (menos de 30 días)
- **Rojo**: Vencido o inactivo

---

## Gestión de Productos

### 1. Acceder a Productos
- En el menú lateral, hacer clic en **"Productos"**

### 2. Crear Nuevo Producto

1. **Hacer clic en "Nuevo Producto"**
2. **Completar información**:
   - Código interno (único por empresa)
   - Descripción del producto
   - Precio base
   - Tasa de IVA (0%, 5% o 10%)
   - Producto de balanza (sí/no)

3. **Guardar producto**

### 3. Gestionar Catálogo

- **Buscar productos**: Usar el campo de búsqueda
- **Filtrar por**: Código, descripción, tasa de IVA
- **Editar producto**: Hacer clic en el ícono de edición
- **Activar/Desactivar**: Cambiar estado del producto

### 4. Importar Productos (Funcionalidad Futura)

El sistema permitirá importar productos desde archivos Excel o CSV.

---

## Gestión de Clientes

### 1. Acceder a Clientes
- En el menú lateral, hacer clic en **"Clientes"**

### 2. Crear Nuevo Cliente

1. **Hacer clic en "Nuevo Cliente"**
2. **Completar datos básicos**:
   - Nombre del cliente
   - Razón Social (si es empresa)
   - RUC (si tributa)
   - Dirección
   - Teléfono
   - Email

3. **Configurar tipo de contribuyente**:
   - **PF**: Persona Física
   - **PJ**: Persona Jurídica
   - **EG**: Entidad Gubernamental

4. **Indicar si tributa** (requiere RUC válido)

5. **Guardar cliente**

### 3. Gestionar Base de Clientes

- **Buscar clientes**: Por nombre, RUC o razón social
- **Filtrar por tipo**: Contribuyente o no contribuyente
- **Editar información**: Actualizar datos de contacto
- **Ver historial**: Facturas emitidas al cliente

---

## Facturación

### 1. Acceder a Facturación
- En el menú lateral, hacer clic en **"Facturación"**

### 2. Crear Nueva Factura

1. **Hacer clic en "Nueva Factura"**
2. **Seleccionar timbrado detalle** (punto de expedición)
3. **Seleccionar cliente** (opcional para consumidor final)
4. **Configurar tipo de factura**:
   - Contado
   - Crédito

#### Agregar Items a la Factura

1. **Hacer clic en "Agregar Item"**
2. **Seleccionar producto** del catálogo o ingresar manualmente:
   - Descripción
   - Cantidad
   - Precio unitario
   - Tasa de IVA

3. **El sistema calculará automáticamente**:
   - Subtotal por item
   - IVA por tasa
   - Total de la factura

#### Aplicar Descuentos

1. **Descuento por item**: En cada línea de detalle
2. **Descuento final**: Sobre el total de la factura

### 3. Finalizar Factura

1. **Revisar totales**:
   - Subtotal por tasa de IVA
   - IVA calculado
   - Total final

2. **Guardar factura**
3. **La factura se numerará automáticamente** según el timbrado

### 4. Gestionar Facturas

- **Ver listado**: Todas las facturas de la empresa
- **Filtrar por**:
  - Fecha de emisión
  - Cliente
  - Estado (activa/anulada)
  - Tipo (contado/crédito)

- **Acciones disponibles**:
  - Ver detalle
  - Imprimir
  - Generar documento electrónico
  - Anular (si está permitido)

---

## Documentos Electrónicos

### 1. Acceder a Documentos Electrónicos
- En el menú lateral, hacer clic en **"Documentos Electrónicos"**

### 2. Generar Documento Electrónico (DE)

1. **Seleccionar factura legal** sin DE asociado
2. **Hacer clic en "Generar DE"**
3. **El sistema automáticamente**:
   - Genera el XML según especificaciones SIFEN
   - Calcula el CDC (Código de Control)
   - Firma digitalmente con el certificado
   - Genera código QR

### 3. Gestionar Lotes de Envío

#### Crear Lote
1. **Hacer clic en "Nuevo Lote"**
2. **Seleccionar documentos** pendientes de envío
3. **Crear lote** para envío a SIFEN

#### Enviar a SIFEN
1. **Seleccionar lote** creado
2. **Hacer clic en "Enviar a SIFEN"**
3. **Monitorear estado**:
   - Pendiente
   - En proceso
   - Aprobado
   - Rechazado

### 4. Cancelar Documentos Electrónicos

1. **Seleccionar DE aprobado**
2. **Hacer clic en "Cancelar"**
3. **Ingresar motivo** de cancelación
4. **Confirmar cancelación**
5. **El sistema genera evento de cancelación** para SIFEN

### 5. Estados de Documentos Electrónicos

- **🟡 Pendiente**: DE generado, no enviado
- **🔵 En Proceso**: Enviado a SIFEN, esperando respuesta
- **🟢 Aprobado**: Aprobado por SIFEN
- **🔴 Rechazado**: Rechazado por SIFEN
- **⚫ Cancelado**: Cancelado por evento

---

## Reportes

### 1. Acceder a Reportes
- En el menú lateral, hacer clic en **"Reportes"**

### 2. Tipos de Reportes Disponibles

#### Reporte de Facturas
- **Filtros**:
  - Rango de fechas
  - Cliente específico
  - Estado de factura
  - Tipo (contado/crédito)

- **Información incluida**:
  - Número de factura
  - Fecha de emisión
  - Cliente
  - Totales por IVA
  - Total final

#### Reporte de Productos
- **Productos más vendidos**
- **Análisis de precios**
- **Rotación de inventario**

#### Reporte de Clientes
- **Ranking por monto facturado**
- **Frecuencia de compras**
- **Análisis de cartera**

#### Reporte de Usuarios
- **Actividad por usuario**
- **Facturas emitidas**
- **Accesos al sistema**

### 3. Exportar Reportes

1. **Configurar filtros** del reporte
2. **Seleccionar formato**:
   - PDF
   - Excel
   - CSV

3. **Hacer clic en "Exportar"**
4. **Descargar archivo** generado

---

## Auditoría

### 1. Acceder a Auditoría
- En el menú lateral, hacer clic en **"Auditoría"**

### 2. Consultar Historial de Cambios

#### Filtros Disponibles
- **Usuario**: Quien realizó la acción
- **Empresa**: Contexto empresarial
- **Tipo de entidad**: Factura, Cliente, Producto, etc.
- **Acción**: CREATE, UPDATE, DELETE, READ
- **Rango de fechas**: Período de consulta

#### Información del Registro
- **Fecha y hora** exacta
- **Usuario** que realizó la acción
- **Entidad afectada** y su ID
- **Valores anteriores** (para modificaciones)
- **Valores nuevos** (para creaciones y modificaciones)
- **Dirección IP** de origen
- **Descripción** de la acción

### 3. Casos de Uso de Auditoría

- **Rastrear cambios** en facturas importantes
- **Verificar accesos** de usuarios
- **Investigar discrepancias** en datos
- **Cumplir requisitos** de trazabilidad fiscal

---

## Configuración de Usuario

### 1. Perfil de Usuario

1. **Hacer clic en el avatar** (esquina superior derecha)
2. **Seleccionar "Mi Perfil"**
3. **Actualizar información**:
   - Nombre completo
   - Email
   - Teléfono

### 2. Cambiar Contraseña

1. **En Mi Perfil, hacer clic en "Cambiar Contraseña"**
2. **Ingresar**:
   - Contraseña actual
   - Nueva contraseña
   - Confirmar nueva contraseña

3. **Guardar cambios**

### 3. Configuración de Empresa

Si es administrador de empresa:

1. **Acceder a configuración de empresa**
2. **Actualizar**:
   - Datos fiscales
   - Certificado digital
   - Configuraciones de facturación

---

## Flujo de Trabajo Típico

### Para Facturación Física

1. **Configurar empresa** con datos fiscales
2. **Registrar timbrado físico** con puntos de expedición
3. **Cargar catálogo de productos**
4. **Registrar clientes frecuentes**
5. **Emitir facturas** según demanda
6. **Generar reportes** periódicos

### Para Facturación Electrónica

1. **Configurar empresa** con certificado digital
2. **Registrar timbrado electrónico** con CSC
3. **Cargar catálogo de productos**
4. **Registrar clientes frecuentes**
5. **Emitir facturas legales**
6. **Generar documentos electrónicos**
7. **Enviar lotes a SIFEN**
8. **Monitorear aprobaciones**
9. **Generar reportes** de cumplimiento

---

## Validaciones del Sistema

### RUC Paraguayo
- Formato: 12345678-9
- Validación de dígito verificador
- Verificación de existencia en SET

### CDC (Código de Control)
- 44 caracteres alfanuméricos
- Generación automática según algoritmo SIFEN
- Validación de integridad

### Timbrados
- 8 dígitos numéricos
- Validación de vigencia
- Control de rangos de numeración

---

## Solución de Problemas Comunes

### Error de Autenticación
- Verificar usuario y contraseña
- Contactar administrador si está bloqueado

### Error al Generar DE
- Verificar certificado digital vigente
- Revisar configuración de timbrado electrónico
- Validar datos de la factura

### Error de Conexión SIFEN
- Verificar conectividad a internet
- Revisar configuración de ambiente (test/producción)
- Contactar soporte técnico

### Factura No Se Puede Editar
- Las facturas con DE aprobado no se pueden modificar
- Generar nota de crédito si es necesario

---

## Contacto y Soporte

Para soporte técnico o consultas:

- **Email**: soporte@frcefact.com
- **Teléfono**: +595 21 123-4567
- **Horario**: Lunes a Viernes, 8:00 - 17:00

---

## Notas Importantes

⚠️ **Importante**: 
- Mantenga siempre actualizado su certificado digital
- Realice respaldos periódicos de sus datos
- No comparta credenciales de acceso
- Revise regularmente los reportes de auditoría

✅ **Recomendaciones**:
- Configure alertas de vencimiento de timbrados
- Mantenga actualizado el catálogo de productos
- Revise periódicamente el estado de los DE en SIFEN
- Capacite a su equipo en el uso del sistema

---

*Manual de Usuario FRC eFact v1.0 - Actualizado: Octubre 2025*