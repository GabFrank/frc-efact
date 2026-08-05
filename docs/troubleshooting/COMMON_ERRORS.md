# Errores Comunes (General)

Problemas comunes que pueden ocurrir en desarrollo o producción.

## 🔐 Problemas de Autenticación

### Error: "Invalid credentials"

**Solución**:
1. Verifica que el usuario exista en la base de datos
2. Usuario admin por defecto: `admin` / `admin123`
3. Verifica que la migración V3 se haya ejecutado correctamente

### Error: "Token expired"

**Solución**:
1. Haz login nuevamente
2. El token JWT expira después de cierto tiempo (configurable)
3. Verifica que `JWT_SECRET` sea el mismo en todos los deployments

---

## 🗄️ Problemas de Base de Datos

### Error: "Connection refused"

**Solución**:
1. Verifica que PostgreSQL esté corriendo
2. Verifica las credenciales/URL en `application-dev.yml` (dev) o `DATABASE_URL` (prod)
3. En **dev**, la URL por defecto es `jdbc:postgresql://172.25.0.36:5551/frc_efact_dev` (host `172.25.0.36`, puerto `5551`), no `localhost:5432`. Ajusta a tu entorno si corresponde.

### Error: "Database does not exist"

**Solución** (dev — PostgreSQL no crea la base automáticamente):
```bash
psql -h 172.25.0.36 -p 5551 -U postgres -c "CREATE DATABASE frc_efact_dev;"
```

### Error: "Flyway migration failed"

**Solución**:
1. Revisa qué migración falló en los logs
2. Si es desarrollo, puedes resetear: `DROP DATABASE frc_efact_dev; CREATE DATABASE frc_efact_dev;`
3. Si es producción, crea una migración de corrección

---

## 🌐 Problemas de CORS

### Error: "CORS policy blocked"

**Síntoma**: Frontend no puede hacer requests al backend.

**Solución**:
1. **No existe** una variable `FRONTEND_URL`. El CORS está **fijo en el código**: los orígenes permitidos se definen en `SecurityConfig.java` (`corsConfigurationSource()`, con patrones como `https://*.onrender.com`, `http://localhost:4200`, IPs locales) y en `cors.allowed-origins` de `application-prod.yml` / `application-dev.yml`.
2. Si el origen del frontend no está permitido, agrégalo en `SecurityConfig.java` / el `application-*.yml` correspondiente y **rebuildea/redeploya** el backend.
3. Verifica que el frontend apunte a la URL correcta del backend (`environment.ts` en dev, `environment.prod.ts` en prod).

---

## 🚀 Problemas de Build

### Error: "npm install failed"

**Solución**:
1. Elimina `node_modules` y `package-lock.json`
2. Ejecuta `npm install` nuevamente
3. Verifica tu versión de Node.js (debe ser 18+)

### Error: "Maven build failed"

**Solución**:
1. Ejecuta `mvn clean install`
2. Verifica tu versión de Java (debe ser 17+)
3. Verifica que `JAVA_HOME` esté configurado correctamente

---

## 🔧 Problemas de Configuración

### Variables de entorno no se cargan

**Solución**:
1. Verifica que el archivo `.env` exista (si lo usas)
2. Verifica que las variables estén en el formato correcto
3. Reinicia la aplicación después de cambiar variables

### Puerto ya en uso

**Solución**:
```bash
# Encontrar proceso usando el puerto
lsof -i :8080  # Backend
lsof -i :4200  # Frontend

# Matar el proceso
kill -9 <PID>
```

---

## 📱 Problemas de Frontend

### Página en blanco

**Solución**:
1. Abre DevTools (F12) y revisa la consola
2. Verifica que el build se haya completado correctamente (`npm run build:prod`)
3. Verifica la `apiUrl` en `environment.ts` (dev) / `environment.prod.ts` (prod). No hay variable `API_URL` de runtime: la URL se fija en tiempo de compilación.

### Estilos no se cargan

**Solución**:
1. Ejecuta `npm run build` nuevamente
2. Limpia el cache del navegador
3. Verifica que los archivos CSS estén en `dist/`

---

## 🐛 Debugging Tips

### Backend
```bash
# Ejecutar con debug habilitado
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -jar app.jar
```

### Frontend
```bash
# Ejecutar con source maps
ng serve --source-map
```

### Ver logs detallados
```bash
# Backend
tail -f logs/application.log

# Frontend (en el navegador)
# DevTools > Console > Verbose
```
