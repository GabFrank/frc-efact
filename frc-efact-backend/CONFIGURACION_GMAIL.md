# Configuración de Gmail para Envío de Emails

Esta guía te ayudará a configurar Gmail para que el sistema pueda enviar emails automáticamente cuando un Documento Electrónico (DE) sea aprobado.

## Paso 1: Activar Verificación en Dos Pasos

1. Ve a tu cuenta de Google: https://myaccount.google.com/security
2. En la sección "Iniciar sesión en Google", busca "Verificación en dos pasos"
3. Haz clic en "Verificación en dos pasos" y sigue las instrucciones para activarla
4. Necesitarás un teléfono para recibir códigos de verificación

## Paso 2: Crear Contraseña de Aplicación

1. Una vez activada la verificación en dos pasos, ve a: https://myaccount.google.com/apppasswords
2. Si no ves la opción, asegúrate de que la verificación en dos pasos esté activada
3. En "Seleccionar app", elige: **"Correo"**
4. En "Seleccionar dispositivo", elige: **"Otro (nombre personalizado)"**
5. Escribe: **"FRC eFact Backend"**
6. Haz clic en **"Generar"**
7. **IMPORTANTE**: Copia la contraseña de 16 caracteres que aparece (sin espacios)
   - Ejemplo: `abcd efgh ijkl mnop` → usar `abcdefghijklmnop`

## Paso 3: Configurar Variable de Entorno

Tienes varias opciones dependiendo de tu entorno:

### Opción A: Desarrollo Local (macOS/Linux)

**Método 1: Exportar en la terminal (temporal)**
```bash
export MAIL_PASSWORD="tu-contraseña-de-16-caracteres-sin-espacios"
```

**Método 2: Agregar al archivo de perfil (permanente)**
```bash
# Agregar al final de ~/.zshrc o ~/.bashrc
echo 'export MAIL_PASSWORD="tu-contraseña-de-16-caracteres-sin-espacios"' >> ~/.zshrc
source ~/.zshrc
```

**Método 3: Crear archivo .env (recomendado para desarrollo)**
```bash
# En el directorio frc-efact-backend/
echo 'MAIL_PASSWORD=tu-contraseña-de-16-caracteres-sin-espacios' > .env
```

Luego modifica `dev.sh` para cargar el archivo:
```bash
# Agregar al inicio del script
export $(cat .env | xargs)
```

### Opción B: Desarrollo Local (Windows)

**Método 1: PowerShell (temporal)**
```powershell
$env:MAIL_PASSWORD="tu-contraseña-de-16-caracteres-sin-espacios"
```

**Método 2: Variables de Entorno del Sistema (permanente)**
1. Presiona `Win + R`, escribe `sysdm.cpl` y presiona Enter
2. Ve a la pestaña "Opciones avanzadas"
3. Haz clic en "Variables de entorno"
4. En "Variables del usuario", haz clic en "Nueva"
5. Nombre: `MAIL_PASSWORD`
6. Valor: `tu-contraseña-de-16-caracteres-sin-espacios`
7. Reinicia tu IDE/terminal

### Opción C: Producción (Render)

1. Ve a tu servicio en Render Dashboard
2. Ve a "Environment" → "Environment Variables"
3. Haz clic en "Add Environment Variable"
4. Key: `MAIL_PASSWORD`
5. Value: `tu-contraseña-de-16-caracteres-sin-espacios`
6. Haz clic en "Save Changes"
7. Reinicia el servicio

### Opción D: Docker

Si usas Docker, agrega la variable al comando:
```bash
docker run -p 8080:8080 \
  -e MAIL_PASSWORD="tu-contraseña-de-16-caracteres-sin-espacios" \
  -e DATABASE_URL="..." \
  frc-efact-backend
```

O en `docker-compose.yml`:
```yaml
services:
  backend:
    environment:
      - MAIL_PASSWORD=tu-contraseña-de-16-caracteres-sin-espacios
```

## Paso 4: Verificar Configuración

1. Inicia la aplicación
2. Busca en los logs mensajes como:
   ```
   ✅ Email enviado exitosamente a: cliente@example.com
   ```
3. Si ves errores, verifica:
   - Que la contraseña de aplicación sea correcta (16 caracteres, sin espacios)
   - Que la verificación en dos pasos esté activada
   - Que la variable `MAIL_PASSWORD` esté configurada correctamente

## Solución de Problemas

### Error: "535-5.7.8 Username and Password not accepted"
- **Causa**: La contraseña de aplicación es incorrecta o la verificación en dos pasos no está activada
- **Solución**: 
  1. Verifica que la verificación en dos pasos esté activada
  2. Genera una nueva contraseña de aplicación
  3. Asegúrate de copiar la contraseña sin espacios

### Error: "Could not connect to SMTP host"
- **Causa**: Problemas de red o firewall
- **Solución**: Verifica que el puerto 587 esté abierto

### Error: "Authentication failed"
- **Causa**: Usuario o contraseña incorrectos
- **Solución**: 
  1. Verifica que el email en `application.yml` sea correcto: `frcsistemasinformaticos@gmail.com`
  2. Verifica que `MAIL_PASSWORD` tenga la contraseña de aplicación correcta

## Notas Importantes

- ⚠️ **NUNCA** commitees la contraseña de aplicación al repositorio
- ⚠️ La contraseña de aplicación es diferente a tu contraseña de Gmail
- ⚠️ Si cambias la contraseña de tu cuenta de Gmail, necesitarás generar una nueva contraseña de aplicación
- ✅ Puedes tener múltiples contraseñas de aplicación (una por aplicación)
- ✅ Puedes revocar contraseñas de aplicación en cualquier momento desde https://myaccount.google.com/apppasswords

## Configuración Actual

La configuración actual en `application.yml` es:
```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: frcsistemasinformaticos@gmail.com
    password: ${MAIL_PASSWORD}  # Variable de entorno
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
    default-encoding: UTF-8
```

Solo necesitas configurar la variable `MAIL_PASSWORD` con la contraseña de aplicación que generaste.

