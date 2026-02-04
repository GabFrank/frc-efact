# Cómo Agregar Variables de Entorno en Render (Manual)

Esta guía explica cómo agregar variables de entorno manualmente en Render Dashboard.

## Método 1: Desde Render Dashboard (Recomendado)

### Paso 1: Acceder al Servicio

1. Ir a [Render Dashboard](https://dashboard.render.com/)
2. Click en el servicio `frc-efact-backend`

### Paso 2: Ir a Environment Variables

1. En el menú lateral, click en **"Environment"**
2. O ir directamente a la pestaña **"Environment"** en la parte superior

### Paso 3: Agregar Variables

1. Click en el botón **"Add Environment Variable"**

2. **Agregar primera variable:**
   - **Key:** `GITHUB_USERNAME`
   - **Value:** `GabFrank` (o tu usuario de GitHub)
   - Click en **"Save"**

3. **Agregar segunda variable:**
   - Click en **"Add Environment Variable"** nuevamente
   - **Key:** `GITHUB_TOKEN`
   - **Value:** `<REDACTADO-PAT-GITHUB>` (tu Personal Access Token)
   - Click en **"Save"**

### Paso 4: Verificar

Deberías ver ambas variables en la lista:
- ✅ `GITHUB_USERNAME` = `GabFrank`
- ✅ `GITHUB_TOKEN` = `<REDACTADO-PAT-GITHUB>`

### Paso 5: Triggerear Nuevo Build

Render debería detectar automáticamente los cambios y iniciar un nuevo deployment. Si no:

1. Ir a la pestaña **"Events"** o **"Deploys"**
2. Click en **"Manual Deploy"** → **"Deploy latest commit"**

## Método 2: Usando MCP de Render (Automático)

Si tienes el MCP de Render configurado, puedes usar:

```typescript
// Las variables ya fueron agregadas automáticamente usando MCP
// GITHUB_USERNAME = GabFrank
// GITHUB_TOKEN = <REDACTADO-PAT-GITHUB>
```

## Verificar que Funcionó

### 1. Ver Logs del Build

1. Ir a `frc-efact-backend` → **"Logs"**
2. Buscar en los logs del build:
   - ✅ Debería crear `~/.m2/settings.xml`
   - ✅ Debería autenticarse con GitHub Packages
   - ✅ Debería descargar `jsifenlib` sin errores 401

### 2. Verificar Estado del Deployment

1. Ir a `frc-efact-backend` → **"Deploys"**
2. El último deployment debería mostrar:
   - **Status:** `build_in_progress` o `live`
   - **Trigger:** `api` (si se agregó vía MCP) o `env_var_change`

### 3. Verificar que el Servicio Esté Live

1. Ir a `frc-efact-backend` → **"Logs"**
2. Buscar: `Started FrcEfactBackendApplication`
3. Verificar que no haya errores de autenticación con GitHub Packages

## Troubleshooting

### Las variables no aparecen

1. Verificar que se guardaron correctamente
2. Refrescar la página del Dashboard
3. Verificar que estás en el servicio correcto (`frc-efact-backend`)

### El build sigue fallando

1. **Verificar que las variables estén configuradas:**
   - Ir a Environment → Verificar que ambas variables existan

2. **Verificar el formato del token:**
   - Debe empezar con `ghp_`
   - No debe tener espacios
   - Debe tener el scope `read:packages`

3. **Verificar los logs del build:**
   - Buscar errores específicos
   - Verificar que el Dockerfile esté creando el settings.xml

### El deployment no se inició automáticamente

1. Ir a **"Deploys"**
2. Click en **"Manual Deploy"** → **"Deploy latest commit"**

## Notas de Seguridad

- ⚠️ **Nunca subas el token a Git**
- ⚠️ **No compartas el token públicamente**
- ⚠️ **Rota el token periódicamente**
- ✅ **El token solo debe tener scope `read:packages`**

## Próximos Pasos

Una vez que el build sea exitoso:

1. Verificar que el backend esté "Live"
2. Verificar el health check: `https://frc-efact-backend.onrender.com/actuator/health`
3. Continuar con la migración de datos (Paso 6 de la guía)
