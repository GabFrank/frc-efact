# Guía Paso a Paso - Deployment a Producción

Esta guía te llevará paso a paso a través del proceso completo de deployment.

> 💡 **Tip:** Configura el MCP de Render para acceder a información de deployments directamente desde Cursor. Ver [RENDER_MCP_SETUP.md](RENDER_MCP_SETUP.md)

## Paso 1: Preparación (Completado ✅)

Las siguientes verificaciones ya están completas:
- ✅ Configuraciones del backend verificadas
- ✅ Configuraciones del frontend verificadas
- ✅ Migraciones de Flyway verificadas (35 archivos)
- ✅ Scripts de migración creados

## Paso 2: Crear Servicios en Render

### 2.1 Acceder a Render

1. Ir a [Render Dashboard](https://dashboard.render.com/)
2. Iniciar sesión con tu cuenta

### 2.2 Crear Blueprint

1. Click en el botón **"New +"** en la parte superior
2. Seleccionar **"Blueprint"**
3. Conectar tu repositorio de GitHub:
   - Si es la primera vez, autorizar acceso a GitHub
   - Seleccionar el repositorio `frc-efact`
   - Seleccionar la rama (generalmente `main` o `master`)

### 2.3 Revisar Configuración

Render detectará automáticamente el archivo `render.yaml` y mostrará un preview de los servicios que se crearán:

- **Base de datos PostgreSQL:** `frc-efact-db`
- **Backend Web Service:** `frc-efact-backend` (Docker)
- **Frontend Static Site:** `frc-efact-frontend`

**Verificar:**
- ✅ Los 3 servicios aparecen en el preview
- ✅ La región es "Oregon" (o la más cercana a tus usuarios)
- ✅ El plan es "Free" (o el que prefieras)

### 2.4 Aplicar Blueprint

1. Click en **"Apply"** para crear los servicios
2. Render comenzará a crear los servicios automáticamente

### 2.5 Monitorear Deployment

**Tiempos esperados:**
- Base de datos: 1-2 minutos
- Backend: 10-15 minutos (primera vez)
- Frontend: 3-5 minutos

**Cómo monitorear:**
1. En el Dashboard verás los 3 servicios
2. Click en cada servicio para ver los logs en tiempo real
3. Esperar a que todos muestren estado **"Live"** (verde)

**Logs importantes del Backend:**
- Buscar: `Started FrcEfactBackendApplication`
- Verificar que Flyway aplicó las migraciones: `Flyway migration completed`
- Verificar conexión a base de datos: `Connecting to database`

**Logs importantes del Frontend:**
- Build completado sin errores
- Archivos generados en `dist/frc-efact-frontend/browser`

## Paso 3: Obtener URLs de Producción

Una vez que todos los servicios estén "Live":

1. **Backend URL:**
   - Click en `frc-efact-backend`
   - Copiar la URL (ej: `https://frc-efact-backend-xxxxx.onrender.com`)

2. **Frontend URL:**
   - Click en `frc-efact-frontend`
   - Copiar la URL (ej: `https://frc-efact-frontend-xxxxx.onrender.com`)

3. **Database URL:**
   - Click en `frc-efact-db`
   - Ir a la pestaña "Connections"
   - Copiar "Internal Database URL" (para uso interno)
   - O "External Database URL" (para conexión desde fuera de Render)

**⚠️ IMPORTANTE:** Guarda estas URLs, las necesitarás en los siguientes pasos.

## Paso 4: Actualizar Configuración del Frontend

### 4.1 Actualizar environment.prod.ts

1. Abrir `frc-efact-frontend/src/environments/environment.prod.ts`
2. Actualizar la URL del backend:

```typescript
export const environment = {
  production: true,
  apiUrl: 'https://frc-efact-backend-xxxxx.onrender.com/api', // ← Actualizar aquí
  // ... resto de la configuración
};
```

3. Guardar el archivo

### 4.2 Actualizar CORS en Backend (si es necesario)

Si la URL del frontend es diferente a la configurada por defecto:

1. Abrir `frc-efact-backend/src/main/resources/application-prod.yml`
2. Actualizar la URL en la sección CORS:

```yaml
cors:
  allowed-origins:
    - https://frc-efact-frontend-xxxxx.onrender.com  # ← Actualizar aquí
```

3. Guardar el archivo

### 4.3 Configurar Credenciales de GitHub Packages

**⚠️ IMPORTANTE:** El backend necesita credenciales de GitHub Packages para descargar la dependencia `jsifenlib`.

1. Ir a Render Dashboard → `frc-efact-backend` → "Environment"
2. Agregar las siguientes variables de entorno:
   - **`GITHUB_USERNAME`**: Tu nombre de usuario de GitHub
   - **`GITHUB_TOKEN`**: Un Personal Access Token de GitHub con scope `read:packages`
   
   Ver guía completa en [RENDER_GITHUB_PACKAGES.md](RENDER_GITHUB_PACKAGES.md)

### 4.4 Commit y Push

```bash
git add frc-efact-frontend/src/environments/environment.prod.ts
git add frc-efact-backend/src/main/resources/application-prod.yml  # Solo si lo modificaste
git commit -m "feat: Actualizar URLs de producción"
git push
```

Render detectará los cambios y redeployará automáticamente (si auto-deploy está habilitado).

## Paso 5: Exportar Datos de Desarrollo

### 5.1 Preparar el Script

1. Abrir terminal
2. Navegar al directorio del proyecto:
```bash
cd /Users/gabfranck/workspace/frc-sistemas-informaticos/frc-efact
```

### 5.2 Ejecutar Exportación

```bash
cd docs/deployment/scripts
./export-dev-data.sh
```

El script te pedirá:
- Contraseña de PostgreSQL (para la base de datos de desarrollo)

### 5.3 Verificar Exportación

El script generará archivos en `deployment-data/`:
- `data_production.sql` - Todos los datos
- `essential_data.sql` - Datos esenciales
- `sequences.sql` - Secuencias de IDs
- `data_report.txt` - Reporte de datos

Revisar el reporte para verificar que los datos se exportaron correctamente.

## Paso 6: Importar Datos a Producción

### 6.1 Verificar que el Backend Inició

**IMPORTANTE:** El backend debe haber iniciado completamente para que Flyway aplique las migraciones.

Verificar en los logs del backend:
- ✅ `Flyway migration completed`
- ✅ `Started FrcEfactBackendApplication`

### 6.2 Ejecutar Importación

```bash
cd docs/deployment/scripts
./import-prod-data.sh <DATABASE_URL>
```

Reemplazar `<DATABASE_URL>` con la URL copiada en el Paso 3.

**Ejemplo:**
```bash
./import-prod-data.sh postgresql://frc_efact_user:password@dpg-xxxxx-a.oregon-postgres.render.com:5432/frc_efact_db
```

### 6.3 Seguir las Instrucciones

El script te preguntará:
1. ¿Deseas continuar? → `yes`
2. ¿Qué datos importar?
   - Opción 1: Todos los datos
   - Opción 2: Solo datos esenciales
3. El script importará los datos y mostrará un reporte

### 6.4 Verificar Importación

El script mostrará un reporte de datos importados. Verificar que los números coincidan con los de desarrollo.

## Paso 7: Verificar Servicios

### 7.1 Verificar Backend

```bash
curl https://frc-efact-backend-xxxxx.onrender.com/actuator/health
```

**Respuesta esperada:**
```json
{"status":"UP"}
```

### 7.2 Verificar Frontend

1. Abrir en el navegador: `https://frc-efact-frontend-xxxxx.onrender.com`
2. Debe mostrar la página de login
3. No debe haber errores en la consola del navegador (F12)

## Paso 8: Pruebas Funcionales

### 8.1 Login

1. Abrir el frontend en el navegador
2. Intentar login con un usuario existente de desarrollo
3. Verificar que el login funcione correctamente

### 8.2 Navegación

1. Verificar que el dashboard cargue
2. Navegar por las diferentes secciones del menú
3. Verificar que no haya errores 404

### 8.3 CRUD Básico

1. **Productos:**
   - Listar productos
   - Crear un producto nuevo
   - Editar un producto
   - Verificar que los cambios se guarden

2. **Clientes:**
   - Listar clientes
   - Crear un cliente nuevo
   - Editar un cliente
   - Verificar que los cambios se guarden

3. **Empresas:**
   - Verificar que las empresas se muestren
   - Verificar que se pueda cambiar de empresa

### 8.4 Facturación

1. Crear una factura de prueba
2. Agregar items
3. Guardar la factura
4. Verificar que se guarde correctamente

## Paso 9: Documentación Final

### 9.1 Actualizar PRODUCTION_DEPLOYMENT.md

1. Abrir `docs/deployment/PRODUCTION_DEPLOYMENT.md`
2. Actualizar las URLs con las reales:
   - Backend URL
   - Frontend URL
3. Documentar credenciales de acceso (guardar de forma segura)

### 9.2 Backup Inicial

Hacer un backup completo de la base de datos de producción:

```bash
pg_dump <DATABASE_URL> -F c -f backup_production_$(date +%Y%m%d_%H%M%S).dump
```

Guardar el backup en un lugar seguro.

## Paso 10: Checklist Final

Usar el checklist en `docs/deployment/DEPLOYMENT_CHECKLIST.md` para verificar que todo esté completo.

## Troubleshooting

### El backend no inicia

1. Revisar logs en Render Dashboard
2. Verificar variables de entorno
3. Verificar conexión a base de datos
4. Verificar que Flyway completó las migraciones

### El frontend no se conecta al backend

1. Verificar URL del backend en `environment.prod.ts`
2. Verificar CORS en `application-prod.yml`
3. Revisar consola del navegador (F12) para errores
4. Verificar que el backend esté "Live"

### Los datos no se importan

1. Verificar que Flyway haya aplicado las migraciones
2. Verificar formato del DATABASE_URL
3. Revisar logs de importación para errores específicos
4. Verificar que las tablas existan en producción

### Error de CORS

1. Verificar que la URL del frontend en `application-prod.yml` sea correcta
2. Verificar que no haya espacios o caracteres especiales
3. Reiniciar el backend después de cambiar CORS

## Siguientes Pasos

Una vez completado el deployment:

1. Configurar backups automáticos (si es posible)
2. Configurar monitoreo y alertas
3. Documentar proceso para el equipo
4. Planificar actualizaciones futuras

## Soporte

Si encuentras problemas:
1. Revisar logs en Render Dashboard
2. Consultar `docs/troubleshooting/RENDER_ISSUES.md`
3. Consultar `docs/troubleshooting/COMMON_ERRORS.md`
