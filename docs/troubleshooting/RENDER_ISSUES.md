# Solución de Problemas en Render

Problemas comunes y sus soluciones al hacer deployment en Render.

## ❌ Error: "JAVA_HOME not found"

**Síntoma**: El build falla con mensaje sobre JAVA_HOME no encontrado.

**Causa**: Render no tiene Java 17 en el buildpack por defecto.

**Solución**: ✅ Ya está solucionado - El proyecto usa Docker en lugar del buildpack de Java.

**Verificar**:
- El `render.yaml` debe tener `type: web` y `dockerfilePath: frc-efact-backend/Dockerfile`
- El Dockerfile existe en `frc-efact-backend/Dockerfile`

---

## ❌ Error: "Invalid DATABASE_URL format"

**Síntoma**: El backend falla al iniciar con error de formato de URL de base de datos.

**Causa**: Render proporciona `DATABASE_URL` en formato `postgresql://...` pero Spring Boot necesita `jdbc:postgresql://...`

**Solución**: ✅ Ya está solucionado - La clase `DatabaseConfig.java` convierte automáticamente el formato.

**Verificar**:
- Existe `frc-efact-backend/src/main/java/com/frcefact/config/DatabaseConfig.java`
- La variable `DATABASE_URL` está configurada en Render

---

## ❌ Backend se queda "Building" por mucho tiempo

**Síntoma**: El deployment del backend tarda más de 10 minutos.

**Posibles causas**:
1. Primera vez que se construye la imagen Docker (normal, puede tardar 10-15 min)
2. Problemas de red con Maven dependencies
3. Recursos insuficientes en plan Free

**Solución**:
1. Espera pacientemente el primer build (es normal)
2. Builds subsecuentes serán más rápidos gracias al cache
3. Revisa los logs en Render para ver el progreso
4. Si falla, intenta hacer redeploy

---

## ❌ Error: "Connection refused" al conectar a la base de datos

**Síntoma**: Backend no puede conectarse a PostgreSQL.

**Solución**:
1. Verifica que la base de datos esté corriendo en Render
2. Verifica que `DATABASE_URL` esté configurada correctamente
3. Usa la **Internal Database URL**, no la External
4. Verifica que backend y database estén en la misma región

---

## ❌ Frontend no se conecta al Backend

**Síntoma**: Frontend carga pero no puede hacer login o fetch data.

**Solución**:
1. Verifica que `API_URL` en el frontend apunte al backend correcto
2. Abre DevTools (F12) y revisa la consola para errores CORS
3. Verifica que `FRONTEND_URL` en el backend incluya la URL del frontend
4. Verifica que ambos servicios usen HTTPS (no mezclar HTTP/HTTPS)

---

## ❌ Error 503: Service Unavailable

**Síntoma**: Al acceder al backend obtienes error 503.

**Posibles causas**:
1. El servicio se durmió (plan Free)
2. El servicio está iniciando
3. El servicio falló al iniciar

**Solución**:
1. Espera 30-60 segundos (si estaba dormido)
2. Revisa los logs en Render Dashboard
3. Verifica el health check: `/actuator/health`
4. Si persiste, revisa las variables de entorno

---

## ❌ Migraciones de Base de Datos Fallan

**Síntoma**: Backend falla al iniciar con errores de Flyway.

**Solución**:
1. Revisa los logs para ver qué migración falló
2. Verifica que los archivos de migración estén en `src/main/resources/db/migration/`
3. Si necesitas resetear: elimina y recrea la base de datos en Render
4. Verifica que las migraciones estén numeradas correctamente (V1, V2, V3...)

---

## ❌ Variables de Entorno no se Aplican

**Síntoma**: Cambios en variables de entorno no tienen efecto.

**Solución**:
1. Después de cambiar variables, haz **Manual Deploy** o **Clear build cache & deploy**
2. Verifica que el nombre de la variable sea exacto (case-sensitive)
3. Reinicia el servicio desde Render Dashboard

---

## 🆘 Necesitas Más Ayuda?

1. Revisa los logs completos en Render Dashboard
2. Verifica la documentación oficial de Render: https://render.com/docs
3. Revisa la configuración en `render.yaml`
4. Compara con la configuración de ejemplo en esta documentación

---

## 📝 Logs Útiles

### Ver logs del Backend
1. Ve a Render Dashboard
2. Click en tu servicio de backend
3. Click en "Logs"
4. Busca errores en rojo

### Ver logs del Frontend
1. Abre el frontend en el navegador
2. Abre DevTools (F12)
3. Ve a la pestaña "Console"
4. Busca errores en rojo
