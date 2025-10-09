# Guía de Deployment en Render

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

**¡Importante!** El Blueprint crea TODO automáticamente:
- ✅ Base de datos (NO necesitas crearla antes)
- ✅ Backend con Docker
- ✅ Frontend con build de Angular
- ✅ Variables de entorno conectadas entre servicios
- ✅ CORS configurado automáticamente

**Opción B: Manual**

Si prefieres crear los servicios manualmente, sigue la guía en `MANUAL_SETUP.md`

### 3. Variables de Entorno (Automáticas)

**¡No necesitas configurar nada manualmente!** El Blueprint configura todo:

**Backend:**
- ✅ `DATABASE_URL` - Conectado automáticamente a la base de datos
- ✅ `JWT_SECRET` - Generado automáticamente (seguro)
- ✅ `CORS_ALLOWED_ORIGINS` - Apunta automáticamente al frontend
- ✅ `SPRING_PROFILES_ACTIVE` - Configurado como `prod`

**Frontend:**
- ✅ `API_URL` - Apunta automáticamente al backend

**Base de Datos:**
- ✅ Creada automáticamente con nombre, usuario y contraseña

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
https://frc-efact-backend.onrender.com/actuator/health
```
Deberías ver: `{"status":"UP"}`

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
1. Verifica que `API_URL` en el frontend apunte al backend correcto
2. Verifica que CORS esté configurado correctamente en el backend
3. Revisa la consola del navegador para errores

## 📚 Documentación Adicional

- **Configuración Manual**: `MANUAL_SETUP.md`
- **Troubleshooting Detallado**: `../../troubleshooting/RENDER_ISSUES.md`
- **Scripts de Validación**: `../scripts/`

## 🎯 Checklist Rápido

- [ ] Cuenta en Render creada
- [ ] Repositorio conectado a Render
- [ ] Blueprint aplicado (servicios creados)
- [ ] Variables de entorno configuradas
- [ ] Backend desplegado y corriendo
- [ ] Frontend desplegado y corriendo
- [ ] Login funciona correctamente

## 💡 Consejos

- Los servicios gratuitos de Render se duermen después de 15 minutos de inactividad
- El primer request después de dormir puede tardar 30-60 segundos
- Usa el plan pagado para producción real
