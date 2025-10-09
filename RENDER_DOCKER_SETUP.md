# Configuración de Docker en Render - Paso a Paso

Esta guía te ayudará a configurar el backend para usar Docker en Render y solucionar el error "JAVA_HOME is not defined correctly".

## ✅ Prerequisitos Completados

- [x] Dockerfile creado en `frc-efact-backend/Dockerfile`
- [x] .dockerignore creado
- [x] Archivos commiteados y pusheados a GitHub
- [x] render.yaml creado en el root del proyecto

## 🚀 Opciones de Configuración

Tienes 3 opciones para configurar Docker en Render:

---

## Opción 1: Usar render.yaml (Blueprint) - MÁS FÁCIL ⭐

Render puede detectar automáticamente el archivo `render.yaml` en el root de tu repositorio.

### Pasos:

1. **Ve a Render Dashboard**: https://dashboard.render.com/

2. **Elimina el servicio backend actual** (si existe):
   - Ve al servicio `frc-efact-backend`
   - Settings → Scroll hasta abajo
   - Click en "Delete Web Service"
   - Confirma

3. **Crea nuevo servicio desde Blueprint**:
   - En el Dashboard, click en **"New +"**
   - Selecciona **"Blueprint"**
   - Conecta tu repositorio de GitHub
   - Render detectará automáticamente el `render.yaml`
   - Click en **"Apply"**

4. **Configurar variables de entorno manualmente**:
   - Una vez creado el servicio, ve a **Settings**
   - En **Environment**, agrega:
     ```
     DATABASE_URL = <Internal Database URL de tu PostgreSQL>
     JWT_SECRET = <genera uno con: openssl rand -base64 64>
     JWT_EXPIRATION = 86400000
     SPRING_PROFILES_ACTIVE = prod
     CORS_ALLOWED_ORIGINS = https://frc-efact-frontend.onrender.com
     LOG_LEVEL = INFO
     ```

5. **Deploy**:
   - El servicio se desplegará automáticamente
   - Espera 5-10 minutos

---

## Opción 2: Configurar Manualmente en el Servicio Existente

Si prefieres mantener el servicio actual y solo cambiar la configuración:

### Pasos:

1. **Ve a tu servicio backend en Render Dashboard**

2. **Settings → Build & Deploy**

3. **Busca y configura estos campos**:

   **Si ves "Docker Command" o similar:**
   - Docker Command: (dejar vacío)
   - Dockerfile Path: `frc-efact-backend/Dockerfile`
   - Docker Context: `frc-efact-backend`

   **Si ves "Build Command" y "Start Command":**
   - **ELIMINA** o deja vacíos ambos campos
   - Render debería detectar automáticamente el Dockerfile

   **Root Directory:**
   - Si existe este campo, configúralo como: `frc-efact-backend`

4. **Guarda los cambios**

5. **Manual Deploy**:
   - Click en **"Manual Deploy"**
   - Selecciona **"Clear build cache & deploy"**
   - Espera 5-10 minutos

---

## Opción 3: Recrear el Servicio desde Cero

Si las opciones anteriores no funcionan:

### Pasos:

1. **Elimina el servicio actual**:
   - Settings → Delete Web Service

2. **Crea nuevo Web Service**:
   - Dashboard → **"New +"** → **"Web Service"**
   - Conecta tu repositorio de GitHub
   - Selecciona el repositorio `frc-efact`

3. **Configuración básica**:
   - **Name**: `frc-efact-backend`
   - **Region**: Oregon (USA) - misma que tu base de datos
   - **Branch**: `main`
   - **Root Directory**: `frc-efact-backend`

4. **Build Settings**:
   - Render debería detectar automáticamente el Dockerfile
   - Si no, busca campos para especificar:
     - Dockerfile Path: `Dockerfile` o `./Dockerfile`
     - Docker Context: `.` o `./`

5. **Environment Variables**:
   ```
   DATABASE_URL = <Internal Database URL>
   JWT_SECRET = <genera con: openssl rand -base64 64>
   JWT_EXPIRATION = 86400000
   SPRING_PROFILES_ACTIVE = prod
   CORS_ALLOWED_ORIGINS = https://frc-efact-frontend.onrender.com
   LOG_LEVEL = INFO
   ```

6. **Health Check**:
   - Path: `/actuator/health`
   - Interval: 30 seconds

7. **Create Web Service**

---

## 🔍 Verificación del Deploy

### Durante el Deploy

Deberías ver en los logs:

```
==> Cloning from https://github.com/TU_USUARIO/frc-efact
==> Checking out commit...
==> Building with Dockerfile...
==> Step 1/X : FROM maven:3.9-eclipse-temurin-17 AS build
==> Step 2/X : WORKDIR /app
...
==> Successfully built image
==> Starting service...
```

**Señales de éxito:**
- ✅ "Building with Dockerfile" (no "Running build command")
- ✅ Múltiples "Step X/Y" del Dockerfile
- ✅ "Successfully built image"
- ✅ Servicio cambia a estado "Live"

**Señales de problema:**
- ❌ "JAVA_HOME is not defined" (significa que no está usando Docker)
- ❌ "Running build command './mvnw...'" (está usando Java buildpack)
- ❌ "Cannot find Dockerfile" (path incorrecto)

### Después del Deploy

1. **Verificar que el servicio está "Live"**

2. **Probar health check**:
   ```bash
   curl https://frc-efact-backend.onrender.com/actuator/health
   ```
   
   Debería retornar:
   ```json
   {"status":"UP"}
   ```

3. **Probar login**:
   ```bash
   curl -X POST https://frc-efact-backend.onrender.com/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"admin","password":"Admin123!"}'
   ```
   
   Debería retornar un token JWT.

---

## 🐛 Troubleshooting

### Error: "Cannot find Dockerfile"

**Causa:** Path del Dockerfile incorrecto

**Solución:**
- Verifica que el Dockerfile existe en GitHub
- Ajusta el path:
  - Si Root Directory es `frc-efact-backend`: usa `Dockerfile` o `./Dockerfile`
  - Si Root Directory es `.` (root): usa `frc-efact-backend/Dockerfile`

### Error: Sigue usando Java buildpack

**Síntoma:** Logs muestran "Running build command './mvnw...'"

**Solución:**
1. Elimina completamente los campos "Build Command" y "Start Command"
2. Asegúrate de que Dockerfile Path está configurado
3. Haz "Clear build cache & deploy"

### Error: "COPY failed: file not found"

**Causa:** Docker Context incorrecto

**Solución:**
- Si Dockerfile está en `frc-efact-backend/Dockerfile`:
  - Docker Context debe ser `frc-efact-backend` o `.`
  - Root Directory debe ser `frc-efact-backend`

### Build muy lento (>15 minutos)

**Causa:** Primera vez descarga todas las dependencias

**Solución:**
- Es normal la primera vez
- Builds subsecuentes serán más rápidos (3-5 minutos)
- Render cachea las capas de Docker

---

## 📊 Comparación de Métodos

| Método | Dificultad | Tiempo | Recomendado |
|--------|------------|--------|-------------|
| Blueprint (render.yaml) | ⭐ Fácil | 5 min | ✅ Sí |
| Configurar existente | ⭐⭐ Media | 10 min | ⚠️ Depende |
| Recrear desde cero | ⭐⭐⭐ Difícil | 15 min | ❌ Solo si falla todo |

---

## ✅ Checklist de Éxito

- [ ] Dockerfile existe en `frc-efact-backend/Dockerfile`
- [ ] Archivos pusheados a GitHub
- [ ] Servicio configurado en Render
- [ ] Logs muestran "Building with Dockerfile"
- [ ] Build completa sin errores
- [ ] Servicio en estado "Live"
- [ ] Health check retorna `{"status":"UP"}`
- [ ] Login endpoint funciona

---

## 📚 Recursos Adicionales

- [RENDER_JAVA_FIX.md](./RENDER_JAVA_FIX.md) - Solución detallada del error JAVA_HOME
- [TROUBLESHOOTING_GUIDE.md](./TROUBLESHOOTING_GUIDE.md) - Guía general de troubleshooting
- [Render Docker Docs](https://render.com/docs/docker)

---

## 💬 ¿Necesitas Ayuda?

Si después de seguir estos pasos sigues teniendo problemas:

1. **Revisa los logs** en Render Dashboard → Service → Logs
2. **Busca el error específico** en [TROUBLESHOOTING_GUIDE.md](./TROUBLESHOOTING_GUIDE.md)
3. **Verifica** que todos los archivos están en GitHub: https://github.com/TU_USUARIO/frc-efact

---

**Última actualización:** 08/10/2025
