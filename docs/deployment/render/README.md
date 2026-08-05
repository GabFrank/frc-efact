# Guía de Deployment en Render

> ⚠️ **DOCUMENTO LEGACY — Render ya no es producción.**
> Desde el **2026-07-07** producción corre en una **VM Hetzner**: `https://efact.frc-ecommerce.com`.
> El deploy es **manual por SSH**, no por `git push`. Guía vigente:
> [RUNBOOK_VM.md](../hetzner/RUNBOOK_VM.md).
> El servicio de Render quedó **suspendido** como ventana de rollback (conserva `autoDeploy`
> sobre `main`: reanudarlo lo vuelve a poner a auto-desplegar). Este documento se conserva
> como referencia histórica hasta darlo de baja.

Esta es la **ÚNICA GUÍA** que necesitas seguir para hacer deployment en Render.

## 📋 Pre-requisitos

- Cuenta en Render.com
- Repositorio Git con el código
- PostgreSQL database en Render (se crea automáticamente)

## 🚀 Pasos para Deployment

### 1. Preparar el Proyecto

El proyecto ya está configurado con:
- ✅ `render.yaml` - Blueprint para crear servicios automáticamente
- ✅ `Dockerfile` en `frc-efact-backend/` - Para el backend Java
- ✅ Configuración de base de datos automática

### 2. Crear Servicios en Render

**Opción A: Usando Blueprint (Recomendado) ⭐**

1. Ve a [Render Dashboard](https://dashboard.render.com/)
2. Click en "New" → "Blueprint"
3. Conecta tu repositorio de GitHub/GitLab
4. Render detectará automáticamente el `render.yaml`
5. Revisa el preview - verás que se crearán **3 servicios**:
   - 🗄️ **Base de datos PostgreSQL**: `frc-efact-db`
   - 🚀 **Backend (Docker)**: `frc-efact-backend`
   - 🎨 **Frontend (Static Site)**: `frc-efact-frontend`
6. Click en "Apply"

**¡Importante!** El Blueprint crea automáticamente:
- ✅ Base de datos (NO necesitas crearla antes)
- ✅ Backend con Docker
- ✅ Frontend con build de Angular
- ✅ `DATABASE_URL` y `JWT_SECRET` conectados/generados entre servicios

⚠️ **Pero NO todo es automático.** Antes de que el backend compile debes agregar manualmente en el Dashboard:
- `GITHUB_USERNAME` y `GITHUB_TOKEN` — **obligatorias**: la dependencia `jsifenlib` se descarga desde GitHub Packages y **sin estas credenciales el build de Docker falla**. Vienen comentadas en `render.yaml` (deben ir en el Dashboard, no en el YAML por ser secretos).
- `MAIL_PASSWORD` — necesaria para el envío de emails; sin ella el arranque puede fallar (no tiene valor por defecto en `application.yml`).
- `ENCRYPTION_KEY` — recomendada (AES-256, 32 caracteres) para cifrar datos sensibles como el CSC y la contraseña del certificado. Si falta, el sistema usa un valor por defecto **inseguro**.

**Opción B: Manual**

Si prefieres crear los servicios manualmente, sigue la guía en `MANUAL_SETUP.md`

### 3. Variables de Entorno

**Definidas automáticamente por el Blueprint (`render.yaml`):**

**Backend:**
- ✅ `DATABASE_URL` - Conectado automáticamente a la base de datos
- ✅ `JWT_SECRET` - Generado automáticamente (seguro)
- ✅ `SPRING_PROFILES_ACTIVE` - Configurado como `prod`
- `JWT_EXPIRATION=86400000` y `LOG_LEVEL=INFO` — declaradas en `render.yaml` pero **el código actual no las lee** (la expiración del JWT y los niveles de log están fijos en `application-prod.yml`). Son inertes.

**Base de Datos:**
- ✅ Creada automáticamente con nombre, usuario y contraseña

**Que debes configurar manualmente en el Dashboard (Environment):**
- ⚠️ `GITHUB_USERNAME` / `GITHUB_TOKEN` — **obligatorias o el build falla** (jsifenlib desde GitHub Packages).
- ⚠️ `MAIL_PASSWORD` — para envío de emails; sin ella el arranque puede fallar.
- ⚠️ `ENCRYPTION_KEY` — cifrado de datos sensibles (32 chars); sin ella se usa un default inseguro.

> ❌ **No existen** las variables `CORS_ALLOWED_ORIGINS` ni `API_URL`. **CORS no se configura por variable de entorno**: los orígenes permitidos están **fijos en el código** (`SecurityConfig.java`) y en `application-prod.yml` (`cors.allowed-origins`). La URL del backend que usa el frontend se fija en **tiempo de compilación** en `environment.prod.ts`.

### 4. Monitorear el Deployment

**Tiempos esperados:**
- 🗄️ Base de datos: 1-2 minutos
- 🚀 Backend (primera vez): 10-15 minutos
- 🎨 Frontend: 3-5 minutos

**Cómo monitorear:**
1. En Render Dashboard verás los 3 servicios
2. Click en cada uno para ver los logs en tiempo real
3. Espera a que todos muestren "Live" (verde)

### 5. Verificar que Todo Funciona

**Una vez que todos los servicios estén "Live":**

**1. Verifica el Backend:**
```
https://frc-efact-backend.onrender.com/api/actuator/health
```
Deberías ver: `{"status":"UP"}`

**Nota**: El backend usa `context-path: /api`, por lo que **todos los endpoints van bajo `/api`**:
- Health: `/api/actuator/health`
- Login: `/api/auth/login`
- Usuarios: `/api/usuarios`
- Perfil: `/api/perfil`

**2. Verifica el Frontend:**
```
https://frc-efact-frontend.onrender.com
```
Deberías ver la página de login

**3. Prueba el Login:**
- Usuario: `admin`
- Password: `admin123`

Si el login funciona, ¡todo está correcto! ✅

## 🔧 Solución de Problemas Comunes

### Error: "JAVA_HOME not found"
✅ **Solucionado** - El proyecto usa Docker, no necesita JAVA_HOME

### Error: "Invalid DATABASE_URL format"
✅ **Solucionado** - La clase `DatabaseConfig.java` convierte automáticamente el formato de Render

### El backend no inicia
1. Revisa los logs en Render Dashboard
2. Verifica que la base de datos esté corriendo
3. Verifica las variables de entorno

### El frontend no se conecta al backend
1. Verifica que `environment.prod.ts` tenga la `apiUrl` correcta (se fija **en tiempo de compilación**, no por variable de entorno). Si la cambias, hay que **rebuildear** el frontend.
2. Verifica que el origen del frontend esté permitido por CORS en `SecurityConfig.java` / `application-prod.yml`.
3. Revisa la consola del navegador para errores

## 📚 Documentación Adicional

- **Guía Paso a Paso**: `../STEP_BY_STEP_GUIDE.md` ⭐ **Empieza aquí para tu primer deployment**
- **Checklist de Deployment**: `../DEPLOYMENT_CHECKLIST.md`
- **Guía de Producción**: `../PRODUCTION_DEPLOYMENT.md`
- **Configuración Manual**: `MANUAL_SETUP.md`
- **Troubleshooting Detallado**: `../../troubleshooting/RENDER_ISSUES.md`
- **Scripts de Migración**: `../scripts/`

## 🎯 Checklist Rápido

- [ ] Cuenta en Render creada
- [ ] Repositorio conectado a Render
- [ ] Blueprint aplicado (servicios creados)
- [ ] `GITHUB_USERNAME` / `GITHUB_TOKEN` configuradas en el Dashboard (obligatorias)
- [ ] `MAIL_PASSWORD` y `ENCRYPTION_KEY` configuradas en el Dashboard
- [ ] Backend desplegado y corriendo
- [ ] Frontend desplegado y corriendo
- [ ] Login funciona correctamente

## 💡 Consejos

- Los servicios gratuitos de Render se duermen después de 15 minutos de inactividad
- El primer request después de dormir puede tardar 30-60 segundos
- Usa el plan pagado para producción real
