# Configuración de Variables de Entorno en el IDE

Si ejecutas la aplicación desde el IDE (IntelliJ IDEA, VS Code, etc.), necesitas configurar las variables de entorno en la configuración de ejecución.

## IntelliJ IDEA

### Opción 1: Configuración de Ejecución

1. Ve a **Run** → **Edit Configurations...**
2. Selecciona tu configuración de ejecución (o crea una nueva)
3. En la sección **Environment variables**, haz clic en el ícono de carpeta
4. Agrega:
   - **Name**: `MAIL_PASSWORD`
   - **Value**: `<APP_PASSWORD — no versionar; ver docs/TAREAS_PENDIENTES.md §9>` (tu contraseña de aplicación sin espacios)
5. Haz clic en **OK** y **Apply**

### Opción 2: Archivo `.env` (Recomendado)

1. Crea un archivo `.env` en la raíz del proyecto `frc-efact-backend/`:
   ```bash
   MAIL_PASSWORD=<APP_PASSWORD — no versionar; ver docs/TAREAS_PENDIENTES.md §9>
   ```

2. Instala el plugin **EnvFile** en IntelliJ:
   - **File** → **Settings** → **Plugins**
   - Busca "EnvFile" e instálalo
   - Reinicia IntelliJ

3. En tu configuración de ejecución:
   - Ve a **Run** → **Edit Configurations...**
   - Selecciona tu configuración
   - En la sección **EnvFile**, marca **Enable EnvFile**
   - Agrega el archivo `.env` que creaste

### Opción 3: VM Options

En la configuración de ejecución, agrega en **VM options**:
```
-DMAIL_PASSWORD=<APP_PASSWORD — no versionar; ver docs/TAREAS_PENDIENTES.md §9>
```

## VS Code

### Opción 1: Archivo `.env` con Extension

1. Instala la extensión **Java Extension Pack** si no la tienes
2. Crea un archivo `.env` en la raíz del proyecto `frc-efact-backend/`:
   ```bash
   MAIL_PASSWORD=<APP_PASSWORD — no versionar; ver docs/TAREAS_PENDIENTES.md §9>
   ```

3. En `.vscode/launch.json`, agrega:
   ```json
   {
     "type": "java",
     "name": "Launch Backend",
     "request": "launch",
     "mainClass": "com.frcefact.FrcEfactBackendApplication",
     "env": {
       "MAIL_PASSWORD": "<APP_PASSWORD — no versionar; ver docs/TAREAS_PENDIENTES.md §9>"
     }
   }
   ```

### Opción 2: Terminal Integrado

1. Abre la terminal integrada en VS Code
2. Ejecuta:
   ```bash
   export MAIL_PASSWORD="<APP_PASSWORD — no versionar; ver docs/TAREAS_PENDIENTES.md §9>"
   ```
3. Luego ejecuta la aplicación desde la terminal

## Eclipse

1. **Run** → **Run Configurations...**
2. Selecciona tu configuración Java Application
3. Ve a la pestaña **Environment**
4. Haz clic en **New**
5. **Name**: `MAIL_PASSWORD`
6. **Value**: `<APP_PASSWORD — no versionar; ver docs/TAREAS_PENDIENTES.md §9>`
7. **OK** y **Run**

## Verificación

Después de configurar, reinicia la aplicación y verifica en los logs que aparezca:

```
✅ MAIL_PASSWORD configurada (longitud: 16 caracteres)
```

Si ves:
```
⚠️ MAIL_PASSWORD no está configurada. El envío de emails no funcionará.
```

Significa que la variable no se está leyendo correctamente. Revisa la configuración del IDE.

## Nota de Seguridad

⚠️ **IMPORTANTE**: No commitees el archivo `.env` con contraseñas reales. Asegúrate de que esté en `.gitignore`.

