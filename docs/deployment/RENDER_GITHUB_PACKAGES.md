# Configurar GitHub Packages en Render

> ⚠️ **DOCUMENTO LEGACY — Render ya no es producción.**
> Desde el **2026-07-07** producción corre en una **VM Hetzner**: `https://efact.frc-ecommerce.com`.
> El deploy es **manual por SSH**, no por `git push`. Guía vigente:
> [RUNBOOK_VM.md](hetzner/RUNBOOK_VM.md).
> El servicio de Render quedó **suspendido** como ventana de rollback (conserva `autoDeploy`
> sobre `main`: reanudarlo lo vuelve a poner a auto-desplegar). Este documento se conserva
> como referencia histórica hasta darlo de baja.

Este documento explica cómo configurar las credenciales de GitHub Packages en Render para que el build del backend pueda descargar la dependencia `jsifenlib`.

## Problema

El build del backend falla con el error:

```
Could not transfer artifact io.github.gabfrank:jsifenlib:pom:0.2.4-frc.13
status code: 401, reason phrase: Unauthorized (401)
```

Esto ocurre porque Maven necesita autenticarse con GitHub Packages para descargar la librería `jsifenlib`.

## Solución

### Paso 1: Crear Personal Access Token (PAT) de GitHub

1. Ir a [GitHub Settings → Developer settings → Personal access tokens](https://github.com/settings/tokens)
2. Click en **"Generate new token (classic)"**
3. Darle un nombre descriptivo (ej: "Render Maven Build")
4. Seleccionar el scope **`read:packages`**
5. Click en **"Generate token"**
6. **IMPORTANTE**: Copiar el token generado inmediatamente (solo se muestra una vez)

### Paso 2: Agregar Variables de Entorno en Render

1. Ir a [Render Dashboard](https://dashboard.render.com/)
2. Seleccionar el servicio `frc-efact-backend`
3. Ir a la pestaña **"Environment"**
4. Click en **"Add Environment Variable"**
5. Agregar las siguientes variables:

   **Variable 1:**
   - **Key:** `GITHUB_USERNAME`
   - **Value:** Tu nombre de usuario de GitHub (ej: `GabFrank`)

   **Variable 2:**
   - **Key:** `GITHUB_TOKEN`
   - **Value:** El Personal Access Token que generaste en el Paso 1

6. Click en **"Save Changes"**

### Paso 3: Verificar Configuración

1. Verificar que las variables de entorno estén configuradas:
   - `GITHUB_USERNAME` debe tener tu usuario de GitHub
   - `GITHUB_TOKEN` debe tener el token generado

2. El Dockerfile ya está configurado para usar estas variables automáticamente

### Paso 4: Triggerear Nuevo Build

1. Opción A: Hacer un commit y push (si auto-deploy está habilitado)
   ```bash
   git commit --allow-empty -m "trigger: Reiniciar build con credenciales GitHub"
   git push
   ```

2. Opción B: Manual desde Render Dashboard
   - Ir al servicio `frc-efact-backend`
   - Click en **"Manual Deploy"** → **"Deploy latest commit"**

## Verificación

Una vez configurado, el build debería:

1. ✅ Crear automáticamente el archivo `~/.m2/settings.xml` con las credenciales
2. ✅ Autenticarse correctamente con GitHub Packages
3. ✅ Descargar la dependencia `jsifenlib` sin errores
4. ✅ Completar el build exitosamente

## Troubleshooting

### El build sigue fallando con 401

1. **Verificar que las variables estén configuradas:**
   - Ir a Render Dashboard → `frc-efact-backend` → Environment
   - Verificar que `GITHUB_USERNAME` y `GITHUB_TOKEN` existan

2. **Verificar que el token tenga el scope correcto:**
   - El token debe tener el scope `read:packages`
   - Si no lo tiene, generar un nuevo token

3. **Verificar que el token no haya expirado:**
   - Si el token expiró, generar uno nuevo y actualizar `GITHUB_TOKEN` en Render

4. **Verificar el username:**
   - El `GITHUB_USERNAME` debe ser exactamente tu nombre de usuario de GitHub
   - No debe tener espacios ni caracteres especiales

### El build falla pero no muestra el error 401

1. Revisar los logs completos del build en Render Dashboard
2. Verificar que el Dockerfile esté usando las variables correctamente
3. Verificar que el ID del repositorio en `pom.xml` sea `github-jsifenlib`

## Seguridad

### Buenas Prácticas

1. **No compartir el token:**
   - Cada desarrollador debe tener su propio token
   - No compartir tokens entre equipos

2. **Rotar tokens periódicamente:**
   - Generar nuevos tokens cada cierto tiempo
   - Revocar tokens antiguos que ya no se usen

3. **Usar tokens con permisos mínimos:**
   - Solo usar el scope `read:packages`
   - No usar tokens con permisos de administrador

4. **No exponer tokens en logs:**
   - Los tokens no se muestran en los logs de Render
   - Pero verificar que no aparezcan en commits o código

## Alternativa: Usar Librería Local

Si no quieres usar GitHub Packages, puedes:

1. Compilar `jsifenlib` localmente e incluirla en el proyecto
2. O usar la versión que está en `docs/rshk-jsifenlib` (aunque esto requeriría cambios más significativos)

Ver `frc-efact-backend/GITHUB_PACKAGES_SETUP.md` para más detalles.
