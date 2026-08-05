# Guía de Puesta en Producción - FRC eFact

> ⚠️ **DOCUMENTO LEGACY — Render ya no es producción.**
> Desde el **2026-07-07** producción corre en una **VM Hetzner**: `https://efact.frc-ecommerce.com`.
> El deploy es **manual por SSH**, no por `git push`. Guía vigente:
> [RUNBOOK_VM.md](hetzner/RUNBOOK_VM.md).
> El servicio de Render quedó **suspendido** como ventana de rollback (conserva `autoDeploy`
> sobre `main`: reanudarlo lo vuelve a poner a auto-desplegar). Este documento se conserva
> como referencia histórica hasta darlo de baja.

Esta guía documenta el proceso completo de despliegue a producción en Render.

## URLs de Producción

**Backend:** `https://frc-efact-backend.onrender.com`  
**Frontend:** `https://frc-efact-frontend.onrender.com`  
**Base de Datos:** PostgreSQL en Render (creada automáticamente)

## Credenciales de Acceso

### Usuario Administrador por Defecto
- **Username:** `admin`
- **Password:** `admin123` (sembrado por la migración Flyway `V4`)

### Base de Datos
- **Host:** (obtener desde Render Dashboard → frc-efact-db → Connections)
- **Database:** `frc_efact_db`
- **User:** `frc_efact_user`
- **Password:** (generado automáticamente por Render)

## Variables de Entorno en Producción

### Backend

**Definidas por `render.yaml` (automáticas):**
- `DATABASE_URL` - Conectado automáticamente desde la base de datos
- `JWT_SECRET` - Generado automáticamente por Render
- `SPRING_PROFILES_ACTIVE=prod`
- `JWT_EXPIRATION=86400000` — ⚠️ **declarada pero inerte**: el código lee `jwt.expiration-ms`, que está fijo en `application-prod.yml`. Esta variable no tiene efecto.
- `LOG_LEVEL=INFO` — ⚠️ **declarada pero inerte**: los niveles de log están fijos en `application-prod.yml`; el código no lee `LOG_LEVEL`.

**A configurar manualmente en el Dashboard (Environment):**
- `GITHUB_USERNAME` / `GITHUB_TOKEN` — **obligatorias**: sin ellas el build de Docker falla al descargar `jsifenlib` de GitHub Packages. Vienen comentadas en `render.yaml`.
- `MAIL_PASSWORD` — para el envío de emails. **Sin valor por defecto**: si falta, el arranque puede fallar.
- `ENCRYPTION_KEY` — AES-256 (32 caracteres) para cifrar datos sensibles (CSC, contraseña del certificado). Si falta, se usa un default **inseguro**.

### Frontend
- **Ninguna variable de entorno de runtime.** La URL del backend (`apiUrl`) se fija en `environment.prod.ts` en **tiempo de compilación**; no se configura automáticamente ni por variable en Render. Si cambia, hay que editar `environment.prod.ts` y rebuildear el frontend.

## Proceso de Deployment

### 1. Preparación
- ✅ Verificar configuraciones en `application-prod.yml`
- ✅ Verificar `Dockerfile` del backend
- ✅ Verificar `render.yaml` en la raíz
- ✅ Verificar build del frontend

### 2. Crear Servicios en Render
1. Ir a [Render Dashboard](https://dashboard.render.com/)
2. Click en "New" → "Blueprint"
3. Conectar repositorio de GitHub
4. Render detectará `render.yaml` automáticamente
5. Revisar preview y hacer click en "Apply"

### 3. Monitorear Deployment
- Backend: 10-15 minutos (primera vez)
- Frontend: 3-5 minutos
- Base de datos: 1-2 minutos

### 4. Migración de Datos
Ver sección "Migración de Datos" más abajo.

## Migración de Datos

### Exportar Datos de Desarrollo

```bash
cd docs/deployment/scripts
./export-dev-data.sh
```

Esto generará:
- `deployment-data/data_production.sql` - Todos los datos
- `deployment-data/essential_data.sql` - Datos esenciales
- `deployment-data/sequences.sql` - Secuencias de IDs
- `deployment-data/data_report.txt` - Reporte de datos

### Importar Datos a Producción

1. Obtener DATABASE_URL desde Render Dashboard:
   - Ir a `frc-efact-db` → "Connections"
   - Copiar "Internal Database URL" o "External Database URL"

2. Importar datos:
```bash
cd docs/deployment/scripts
./import-prod-data.sh <DATABASE_URL>
```

3. Seguir las instrucciones del script para seleccionar qué datos importar.

## Verificación Post-Deployment

### 1. Verificar Backend
```bash
curl https://frc-efact-backend.onrender.com/api/actuator/health
```
Debe retornar: `{"status":"UP"}`

> El backend usa `context-path: /api`, por lo que el health real es **`/api/actuator/health`**.

### 2. Verificar Frontend
- Abrir `https://frc-efact-frontend.onrender.com`
- Debe mostrar la página de login

### 3. Pruebas Funcionales
- Login con usuario existente
- Navegación por la aplicación
- CRUD de productos y clientes
- Crear factura de prueba

## Backup y Restauración

### Backup Manual
```bash
pg_dump <DATABASE_URL> -F c -f backup_$(date +%Y%m%d_%H%M%S).dump
```

### Restauración
```bash
pg_restore -d <DATABASE_URL> backup_YYYYMMDD_HHMMSS.dump
```

## Troubleshooting

### Backend no inicia
1. Revisar logs en Render Dashboard
2. Verificar variables de entorno
3. Verificar conexión a base de datos
4. Verificar que Flyway completó las migraciones

### Frontend no se conecta al backend
1. Verificar URL del backend en `environment.prod.ts`
2. Verificar CORS en `application-prod.yml`
3. Revisar consola del navegador para errores

### Datos no se importan correctamente
1. Verificar que Flyway haya aplicado las migraciones primero
2. Verificar formato del DATABASE_URL
3. Revisar logs de importación para errores específicos

## Monitoreo

### Logs
- Backend: Render Dashboard → frc-efact-backend → Logs
- Frontend: Render Dashboard → frc-efact-frontend → Logs

### Métricas
- Health check: `/api/actuator/health`
- Métricas: `/api/actuator/metrics`
- Info: `/api/actuator/info`

## Actualizaciones Futuras

1. Hacer cambios en el código
2. Commit y push a GitHub
3. Render detectará cambios y redeployará automáticamente. **Nota:** el auto-deploy funciona por el **valor por defecto de Render** (push a la rama del servicio = deploy); **no** está fijado explícitamente en `render.yaml` (no hay claves `branch` ni `autoDeploy`).
4. Para forzar un redeploy del mismo commit, hacer `git commit --allow-empty` y push (no usar "Manual Deploy" del Dashboard, por trazabilidad commit↔deploy).

## Notas Importantes

- Los servicios gratuitos de Render se duermen después de 15 minutos de inactividad
- El primer request después de dormir puede tardar 30-60 segundos
- Considerar upgrade a plan pagado para producción real
- JWT_SECRET es generado automáticamente y debe mantenerse consistente
- Hacer backups regulares de la base de datos
