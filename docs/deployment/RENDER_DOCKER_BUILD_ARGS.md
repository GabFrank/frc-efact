# Configurar Build Arguments en Render para Docker

## Problema

Render no pasa automáticamente las variables de entorno como build arguments (`ARG`) durante el build de Docker. Las variables de entorno están disponibles en runtime, pero no durante el build.

## Solución

Render requiere que las variables se pasen explícitamente como build arguments. Hay dos formas de hacerlo:

### Opción 1: Configurar Build Arguments en Render Dashboard (Recomendado)

1. Ir a [Render Dashboard](https://dashboard.render.com/)
2. Seleccionar el servicio `frc-efact-backend`
3. Ir a **"Settings"** → **"Build & Deploy"**
4. En la sección **"Docker Build Arguments"**, agregar:
   - `GITHUB_USERNAME=GabFrank`
   - `GITHUB_TOKEN=<REDACTADO-PAT-GITHUB>`

5. Guardar cambios

**Nota:** Esta opción puede no estar disponible en todos los planes de Render. Si no ves esta opción, usa la Opción 2.

### Opción 2: Usar Script de Build Personalizado

Si Render no permite configurar build arguments directamente, podemos usar un script que lea las variables de entorno y las pase al build.

1. Crear un script `build.sh` en `frc-efact-backend/`:

```bash
#!/bin/bash
set -e

# Leer variables de entorno (Render las pasa automáticamente)
GITHUB_USERNAME=${GITHUB_USERNAME:-}
GITHUB_TOKEN=${GITHUB_TOKEN:-}

# Construir imagen Docker pasando las variables como build args
docker build \
  --build-arg GITHUB_USERNAME="${GITHUB_USERNAME}" \
  --build-arg GITHUB_TOKEN="${GITHUB_TOKEN}" \
  -t frc-efact-backend \
  .
```

2. Configurar en Render:
   - **Build Command:** `cd frc-efact-backend && chmod +x build.sh && ./build.sh`
   - **Start Command:** `docker run -p $PORT:8080 frc-efact-backend`

**Nota:** Esta opción requiere que Render tenga Docker disponible durante el build, lo cual puede no estar disponible en todos los planes.

### Opción 3: Usar Secret Files (Alternativa)

Si las opciones anteriores no funcionan, podemos usar secret files de Render:

1. Crear un archivo `maven-settings.xml` con las credenciales
2. Subirlo como secret file en Render
3. Modificar el Dockerfile para copiar este archivo

**⚠️ No recomendado por seguridad:** Las credenciales estarían en el repositorio o en archivos expuestos.

## Verificación

Después de configurar, verificar en los logs del build:

1. Ir a `frc-efact-backend` → **"Logs"**
2. Buscar: `✅ Maven settings configured for GitHub Packages`
3. Si ves `⚠️ WARNING`, las variables no se están pasando correctamente

## Solución Actual Implementada

El Dockerfile actual usa `ARG` para recibir las variables como build arguments. Para que funcione:

1. **Render Dashboard** → `frc-efact-backend` → **"Settings"**
2. Buscar sección **"Build & Deploy"** o **"Docker"**
3. Agregar build arguments:
   ```
   GITHUB_USERNAME=GabFrank
   GITHUB_TOKEN=<REDACTADO-PAT-GITHUB>
   ```

Si esta opción no está disponible en tu plan de Render, contacta con el soporte de Render o considera usar la Opción 2.

## Referencias

- [Render Docker Documentation](https://render.com/docs/docker)
- [Docker Build Arguments](https://docs.docker.com/engine/reference/commandline/build/#build-arg)
