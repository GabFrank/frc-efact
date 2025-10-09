# Guía: Deployment con Blueprint en Render

Esta guía te llevará paso a paso para desplegar FRC eFact usando el archivo `render.yaml` (Blueprint).

## ✅ Prerequisitos

- [x] Cuenta en Render
- [x] Repositorio en GitHub con el código
- [x] Archivo `render.yaml` en el root del proyecto (ya está listo)
- [x] Dockerfile en `frc-efact-backend/Dockerfile` (ya está listo)

## 🚀 Pasos para Deployment

### Paso 1: Eliminar Servicio Existente (si existe)

Si ya creaste un servicio backend que está fallando:

1. Ve a [Render Dashboard](https://dashboard.render.com/)
2. Click en tu servicio `frc-efact-backend`
3. Ve a **Settings**
4. Scroll hasta el final
5. Click en **"Delete Web Service"**
6. Confirma escribiendo el nombre del servicio
7. Click en **"Delete"**

**Nota:** No elimines la base de datos PostgreSQL si ya la creaste.

---

### Paso 2: Crear Base de Datos (si no existe)

Si aún no has creado la base de datos PostgreSQL:

1. En Render Dashboard, click **"New +"**
2. Selecciona **"PostgreSQL"**
3. Configura:
   - **Name**: `frc-efact-db`
   - **Database**: `frc_efact_db`
   - **User**: `frc_efact_user` (o dejar default)
   - **Region**: **Oregon (USA)**
   - **PostgreSQL Version**: **15**
   - **Plan**: **Free**
4. Click **"Create Database"**
5. **Guarda** el "Internal Database URL" (lo necesitarás si el blueprint no lo conecta automáticamente)

---

### Paso 3: Desplegar con Blueprint

1. **Ve a Render Dashboard**: https://dashboard.render.com/

2. **Click en "New +"** (botón azul arriba a la derecha)

3. **Selecciona "Blueprint"**

4. **Conecta tu repositorio de GitHub**:
   - Si es la primera vez, autoriza a Render para acceder a GitHub
   - Selecciona tu repositorio: `frc-efact` o como lo hayas nombrado
   - Click **"Connect"**

5. **Render detectará el render.yaml**:
   - Deberías ver un mensaje: "Blueprint detected"
   - Render mostrará los servicios que va a crear:
     - ✅ Web Service: `frc-efact-backend`
     - ✅ PostgreSQL: `frc-efact-db`

6. **Revisa la configuración**:
   - Verifica que todo se ve correcto
   - Puedes editar nombres si quieres

7. **Click en "Apply"**

8. **Espera a que se creen los servicios**:
   - Render creará la base de datos primero (si no existe)
   - Luego creará el servicio backend
   - Esto puede tomar 5-10 minutos

---

### Paso 4: Verificar Variables de Entorno

Después de que se cree el servicio:

1. **Ve al servicio backend** en el Dashboard

2. **Click en "Environment"** en el menú lateral

3. **Verifica que existen estas variables**:
   ```
   SPRING_PROFILES_ACTIVE = prod
   DATABASE_URL = postgresql://... (debe estar conectado automáticamente)
   JWT_SECRET = (debe estar generado automáticamente)
   JWT_EXPIRATION = 86400000
   LOG_LEVEL = INFO
   CORS_ALLOWED_ORIGINS = https://frc-efact-frontend.onrender.com
   ```

4. **Si falta alguna variable**, agrégala manualmente:
   - Click en **"Add Environment Variable"**
   - Ingresa Key y Value
   - Click **"Save Changes"**

**Importante:** Si `DATABASE_URL` no se conectó automáticamente:
- Copia el "Internal Database URL" de tu PostgreSQL
- Agrégala manualmente como variable de entorno

---

### Paso 5: Monitorear el Deploy

1. **Ve a la pestaña "Logs"** del servicio backend

2. **Deberías ver**:
   ```
   ==> Cloning from https://github.com/TU_USUARIO/frc-efact
   ==> Checking out commit...
   ==> Building with Dockerfile...
   ==> Step 1/12 : FROM maven:3.9-eclipse-temurin-17 AS build
   ==> Step 2/12 : WORKDIR /app
   ...
   ==> Successfully built image
   ==> Starting service...
   ==> Service is live 🎉
   ```

3. **Señales de éxito**:
   - ✅ "Building with Dockerfile" (no "JAVA_HOME error")
   - ✅ Múltiples "Step X/Y" del Dockerfile
   - ✅ "Successfully built image"
   - ✅ "Service is live"
   - ✅ Estado cambia a verde "Live"

4. **Si hay errores**:
   - Lee el mensaje de error en los logs
   - Consulta [TROUBLESHOOTING_GUIDE.md](./TROUBLESHOOTING_GUIDE.md)

---

### Paso 6: Verificar que Funciona

Una vez que el servicio esté "Live":

#### Test 1: Health Check

```bash
curl https://frc-efact-backend.onrender.com/actuator/health
```

**Resultado esperado:**
```json
{"status":"UP"}
```

#### Test 2: Login Endpoint

```bash
curl -X POST https://frc-efact-backend.onrender.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin123!"}'
```

**Resultado esperado:**
```json
{
  "token": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "user": {
    "id": 1,
    "username": "admin",
    "email": "admin@example.com"
  }
}
```

Si obtienes el token, ¡el backend está funcionando correctamente! 🎉

---

## 🎯 Próximos Pasos

### 1. Desplegar el Frontend

Ahora que el backend funciona, despliega el frontend:

1. En Render Dashboard, click **"New +"** → **"Static Site"**
2. Conecta el mismo repositorio
3. Configura:
   - **Name**: `frc-efact-frontend`
   - **Root Directory**: `frc-efact-frontend`
   - **Build Command**: `npm ci && npm run build -- --configuration production`
   - **Publish Directory**: `dist/frc-efact-frontend/browser`
4. Click **"Create Static Site"**

### 2. Actualizar CORS

Una vez que el frontend esté desplegado:

1. Ve al servicio backend
2. **Environment** → Edita `CORS_ALLOWED_ORIGINS`
3. Asegúrate de que tiene la URL correcta del frontend
4. Guarda (esto reiniciará el backend)

### 3. Validar Integración

```bash
# Ejecutar script de validación
BACKEND_URL=https://frc-efact-backend.onrender.com \
FRONTEND_URL=https://frc-efact-frontend.onrender.com \
./validate-production.sh
```

---

## 🐛 Troubleshooting

### Blueprint no se detecta

**Problema:** Render no detecta el archivo `render.yaml`

**Solución:**
1. Verifica que `render.yaml` está en el **root** del repositorio (no en una subcarpeta)
2. Verifica que el archivo está en GitHub (haz push si no lo está)
3. Intenta refrescar la página de Render

### Error: "Database not found"

**Problema:** El servicio no puede conectarse a la base de datos

**Solución:**
1. Verifica que la base de datos existe y está "Available"
2. Verifica que `DATABASE_URL` está configurada
3. Verifica que backend y database están en la misma región (Oregon)
4. Usa el "Internal Database URL" (no External)

### Error: "Cannot find Dockerfile"

**Problema:** Render no encuentra el Dockerfile

**Solución:**
1. Verifica que `frc-efact-backend/Dockerfile` existe en GitHub
2. Verifica el path en `render.yaml`:
   ```yaml
   dockerfilePath: ./frc-efact-backend/Dockerfile
   dockerContext: ./frc-efact-backend
   ```
3. Haz push si falta el archivo

### Build muy lento

**Problema:** El build tarda más de 15 minutos

**Solución:**
- Es normal la primera vez (descarga todas las dependencias)
- Builds subsecuentes serán más rápidos (3-5 minutos)
- Si sigue lento, considera upgrade a plan Starter

### JWT_SECRET no se genera

**Problema:** La variable `JWT_SECRET` está vacía

**Solución:**
1. Genera un secret manualmente:
   ```bash
   openssl rand -base64 64
   ```
2. Ve a Environment en el servicio
3. Edita `JWT_SECRET` y pega el valor generado
4. Guarda cambios

---

## 📊 Ventajas del Blueprint

✅ **Configuración como código**: Todo en `render.yaml`  
✅ **Reproducible**: Fácil recrear el ambiente  
✅ **Versionado**: Cambios en Git  
✅ **Automático**: Menos configuración manual  
✅ **Consistente**: Mismo setup en todos los ambientes  

---

## 📝 Contenido del render.yaml

Tu archivo `render.yaml` actual configura:

```yaml
services:
  - Backend con Docker
  - Variables de entorno
  - Health check
  - Conexión a base de datos

databases:
  - PostgreSQL 15
  - Plan free
  - Región Oregon
```

---

## ✅ Checklist de Éxito

- [ ] Servicio backend anterior eliminado (si existía)
- [ ] Base de datos PostgreSQL creada y "Available"
- [ ] Blueprint aplicado desde Render Dashboard
- [ ] Servicio backend creado automáticamente
- [ ] Variables de entorno configuradas
- [ ] Logs muestran "Building with Dockerfile"
- [ ] Build completa sin errores
- [ ] Servicio en estado "Live" (verde)
- [ ] Health check retorna `{"status":"UP"}`
- [ ] Login endpoint retorna token JWT
- [ ] No hay error "JAVA_HOME is not defined"

---

## 🎉 ¡Éxito!

Si todos los checks están completos, tu backend está desplegado correctamente usando Docker.

**URLs importantes:**
- Backend: `https://frc-efact-backend.onrender.com`
- Health: `https://frc-efact-backend.onrender.com/actuator/health`
- Swagger: `https://frc-efact-backend.onrender.com/swagger-ui.html`

---

## 📚 Documentación Relacionada

- [RENDER_DOCKER_SETUP.md](./RENDER_DOCKER_SETUP.md) - Otras opciones de configuración
- [TROUBLESHOOTING_GUIDE.md](./TROUBLESHOOTING_GUIDE.md) - Solución de problemas
- [PRODUCTION_VALIDATION.md](./PRODUCTION_VALIDATION.md) - Validación completa

---

**¿Necesitas ayuda?** Revisa los logs en Render Dashboard y consulta la documentación de troubleshooting.
