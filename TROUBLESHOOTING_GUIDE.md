# Guía de Troubleshooting - FRC eFact

Esta guía proporciona soluciones a problemas comunes que pueden ocurrir durante el desarrollo y deployment de FRC eFact.

## 📋 Tabla de Contenidos

1. [Problemas de Backend](#problemas-de-backend)
2. [Problemas de Frontend](#problemas-de-frontend)
3. [Problemas de Base de Datos](#problemas-de-base-de-datos)
4. [Problemas de Integración](#problemas-de-integración)
5. [Problemas de Deployment](#problemas-de-deployment)
6. [Problemas de Performance](#problemas-de-performance)

---

## Problemas de Backend

### Backend no inicia localmente

**Síntomas:**
- Error al ejecutar `./dev.sh` o `./mvnw spring-boot:run`
- Aplicación se detiene inmediatamente después de iniciar

**Causas comunes y soluciones:**

#### 1. PostgreSQL no está corriendo

```bash
# Verificar si PostgreSQL está corriendo
# macOS
brew services list | grep postgresql

# Linux
sudo systemctl status postgresql

# Iniciar PostgreSQL
# macOS
brew services start postgresql

# Linux
sudo systemctl start postgresql
```

#### 2. Credenciales de base de datos incorrectas

**Verificar configuración en `application-dev.yml`:**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/frc_efact_db
    username: postgres
    password: postgres
```

**Probar conexión:**
```bash
psql -h localhost -U postgres -d frc_efact_db
```

#### 3. Puerto 8080 ya está en uso

```bash
# Encontrar proceso usando puerto 8080
# macOS/Linux
lsof -i :8080

# Matar proceso
kill -9 <PID>

# O cambiar puerto en application.yml
server:
  port: 8081
```

#### 4. Java version incorrecta

```bash
# Verificar versión de Java
java -version

# Debe ser Java 17 o superior
# Si no, instalar Java 17:
# macOS
brew install openjdk@17

# Linux
sudo apt install openjdk-17-jdk
```

### Error: "Failed to configure a DataSource"

**Síntomas:**
```
Failed to configure a DataSource: 'url' attribute is not specified
```

**Soluciones:**

1. **Verificar que PostgreSQL está corriendo**
2. **Verificar application.yml:**
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/frc_efact_db
       username: postgres
       password: postgres
   ```
3. **Crear base de datos si no existe:**
   ```bash
   psql -U postgres -c "CREATE DATABASE frc_efact_db;"
   ```

### Error: "Flyway migration failed"

**Síntomas:**
```
FlywayException: Migration V1__xxx.sql failed
```

**Soluciones:**

1. **Verificar sintaxis SQL en migraciones**
2. **Limpiar historial de Flyway:**
   ```sql
   -- Conectar a la base de datos
   psql -U postgres -d frc_efact_db
   
   -- Limpiar tabla de Flyway
   DELETE FROM flyway_schema_history;
   
   -- O eliminar y recrear la base de datos
   DROP DATABASE frc_efact_db;
   CREATE DATABASE frc_efact_db;
   ```
3. **Verificar orden de migraciones (V1, V2, V3...)**

### Error: "JWT token validation failed"

**Síntomas:**
- Requests autenticados retornan 401
- Error en logs: "Invalid JWT signature"

**Soluciones:**

1. **Verificar JWT_SECRET:**
   - En desarrollo: verificar `application-dev.yml`
   - En producción: verificar variable de entorno en Render

2. **Regenerar token:**
   - Hacer logout y login nuevamente
   - Token puede haber expirado

3. **Verificar formato del token:**
   ```bash
   # Token debe tener formato: eyJhbGc...
   # Verificar que se envía en header:
   Authorization: Bearer <token>
   ```

### Error: "CORS policy blocked"

**Síntomas:**
```
Access to XMLHttpRequest has been blocked by CORS policy
```

**Soluciones:**

1. **Verificar configuración de CORS en SecurityConfig.java:**
   ```java
   configuration.setAllowedOriginPatterns(Arrays.asList(
       "http://localhost:4200",
       "https://frc-efact-frontend.onrender.com"
   ));
   ```

2. **Verificar que frontend está en la lista de orígenes permitidos**

3. **En producción, verificar variable de entorno:**
   ```bash
   CORS_ALLOWED_ORIGINS=https://frc-efact-frontend.onrender.com
   ```

---

## Problemas de Frontend

### Frontend no inicia localmente

**Síntomas:**
- Error al ejecutar `npm start`
- Aplicación no se carga en `http://localhost:4200`

**Causas comunes y soluciones:**

#### 1. Node modules no instalados

```bash
cd frc-efact-frontend
npm install
```

#### 2. Puerto 4200 ya está en uso

```bash
# Encontrar proceso usando puerto 4200
lsof -i :4200

# Matar proceso
kill -9 <PID>

# O cambiar puerto
ng serve --port 4201
```

#### 3. Versión de Node incorrecta

```bash
# Verificar versión de Node
node -version

# Debe ser Node 18 o superior
# Instalar nvm para gestionar versiones
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
nvm install 18
nvm use 18
```

### Error: "Cannot find module '@angular/core'"

**Síntomas:**
```
Error: Cannot find module '@angular/core'
```

**Soluciones:**

1. **Limpiar node_modules y reinstalar:**
   ```bash
   rm -rf node_modules package-lock.json
   npm install
   ```

2. **Verificar que package.json tiene todas las dependencias**

3. **Limpiar caché de npm:**
   ```bash
   npm cache clean --force
   npm install
   ```

### Error de compilación TypeScript

**Síntomas:**
```
Error: TS2304: Cannot find name 'xxx'
```

**Soluciones:**

1. **Verificar imports:**
   ```typescript
   import { Component } from '@angular/core';
   ```

2. **Verificar tipos:**
   ```bash
   npm install --save-dev @types/node
   ```

3. **Limpiar y recompilar:**
   ```bash
   rm -rf .angular
   ng serve
   ```

### Login no funciona (frontend)

**Síntomas:**
- Botón de login no responde
- Error en consola del navegador

**Soluciones:**

1. **Verificar que backend está corriendo:**
   ```bash
   curl http://localhost:8080/actuator/health
   ```

2. **Verificar configuración de API URL en environment.ts:**
   ```typescript
   export const environment = {
     production: false,
     apiUrl: 'http://localhost:8080/api'
   };
   ```

3. **Verificar en DevTools → Network:**
   - Request se envía a URL correcta
   - Status code de la respuesta
   - Mensaje de error en response body

4. **Verificar que AuthInterceptor está configurado en app.config.ts**

---

## Problemas de Base de Datos

### No se puede conectar a PostgreSQL

**Síntomas:**
```
Connection refused: localhost:5432
```

**Soluciones:**

1. **Verificar que PostgreSQL está corriendo:**
   ```bash
   # macOS
   brew services list | grep postgresql
   
   # Linux
   sudo systemctl status postgresql
   ```

2. **Verificar puerto:**
   ```bash
   # PostgreSQL por defecto usa puerto 5432
   netstat -an | grep 5432
   ```

3. **Verificar pg_hba.conf:**
   ```bash
   # Ubicación típica:
   # macOS: /usr/local/var/postgres/pg_hba.conf
   # Linux: /etc/postgresql/15/main/pg_hba.conf
   
   # Debe tener línea:
   host    all             all             127.0.0.1/32            md5
   ```

### Error: "password authentication failed"

**Síntomas:**
```
FATAL: password authentication failed for user "postgres"
```

**Soluciones:**

1. **Resetear password de PostgreSQL:**
   ```bash
   # macOS
   psql postgres
   ALTER USER postgres PASSWORD 'postgres';
   
   # Linux
   sudo -u postgres psql
   ALTER USER postgres PASSWORD 'postgres';
   ```

2. **Verificar credenciales en application.yml**

### Base de datos no existe

**Síntomas:**
```
FATAL: database "frc_efact_db" does not exist
```

**Soluciones:**

1. **Crear base de datos:**
   ```bash
   psql -U postgres -c "CREATE DATABASE frc_efact_db;"
   ```

2. **O usar script de setup:**
   ```bash
   cd frc-efact-backend
   ./setup-local-db.sh
   ```

### Migraciones no se aplican

**Síntomas:**
- Tablas no existen en la base de datos
- Error al iniciar backend

**Soluciones:**

1. **Verificar que Flyway está habilitado en application.yml:**
   ```yaml
   spring:
     flyway:
       enabled: true
       baseline-on-migrate: true
   ```

2. **Verificar ubicación de migraciones:**
   ```
   src/main/resources/db/migration/
   ```

3. **Verificar nombres de archivos:**
   ```
   V1__Create_users_table.sql
   V2__Add_indexes.sql
   ```

4. **Ejecutar migraciones manualmente:**
   ```bash
   ./mvnw flyway:migrate
   ```

---

## Problemas de Integración

### Frontend no puede conectar con Backend

**Síntomas:**
- Error en consola: "Failed to load resource"
- Network error en DevTools

**Soluciones:**

1. **Verificar que ambos servicios están corriendo:**
   ```bash
   # Backend
   curl http://localhost:8080/actuator/health
   
   # Frontend
   curl http://localhost:4200
   ```

2. **Verificar URL de API en environment.ts:**
   ```typescript
   apiUrl: 'http://localhost:8080/api'
   ```

3. **Verificar CORS en backend**

4. **Verificar proxy configuration en angular.json (si se usa)**

### Token no se envía en requests

**Síntomas:**
- Requests a endpoints protegidos retornan 401
- Header Authorization no aparece en Network tab

**Soluciones:**

1. **Verificar que AuthInterceptor está configurado:**
   ```typescript
   // app.config.ts
   provideHttpClient(
     withInterceptors([authInterceptor, errorInterceptor])
   )
   ```

2. **Verificar que token se almacena después del login:**
   ```typescript
   // En AuthService
   login(credentials: LoginRequest): Observable<AuthResponse> {
     return this.http.post<AuthResponse>(`${this.apiUrl}/auth/login`, credentials)
       .pipe(
         tap(response => {
           this.token = response.token; // Almacenar token
         })
       );
   }
   ```

3. **Verificar que interceptor agrega el header:**
   ```typescript
   // auth.interceptor.ts
   const token = authService.getToken();
   if (token) {
     req = req.clone({
       setHeaders: {
         Authorization: `Bearer ${token}`
       }
     });
   }
   ```

### Rutas protegidas no redirigen

**Síntomas:**
- Se puede acceder a rutas protegidas sin autenticación
- AuthGuard no funciona

**Soluciones:**

1. **Verificar que AuthGuard está aplicado en las rutas:**
   ```typescript
   // app.routes.ts
   {
     path: 'welcome',
     component: WelcomeComponent,
     canActivate: [authGuard]
   }
   ```

2. **Verificar implementación de AuthGuard:**
   ```typescript
   export const authGuard: CanActivateFn = (route, state) => {
     const authService = inject(AuthService);
     const router = inject(Router);
     
     if (authService.isAuthenticated()) {
       return true;
     }
     
     router.navigate(['/login']);
     return false;
   };
   ```

3. **Verificar que AuthService.isAuthenticated() funciona correctamente**

---

## Problemas de Deployment

### Backend deployment falla en Render

**Síntomas:**
- Build falla
- Servicio no inicia
- Estado: "Deploy failed"
- Error: "JAVA_HOME is not defined correctly"

**Soluciones:**

#### 1. Error "JAVA_HOME is not defined correctly"

Este error ocurre cuando Render no detecta correctamente Java. **Solución recomendada: Usar Docker**

**Opción A: Usar Docker (Recomendado)**

1. Asegúrate de que existe `frc-efact-backend/Dockerfile`
2. En Render Dashboard → Backend Service → Settings:
   - Runtime: **Docker**
   - Dockerfile Path: `frc-efact-backend/Dockerfile`
   - Docker Context: `frc-efact-backend`
3. Guardar y hacer Manual Deploy

**Opción B: Usar Buildpack de Java**

1. Crear archivo `system.properties` en `frc-efact-backend/`:
   ```properties
   java.runtime.version=17
   maven.version=3.9.6
   ```
2. En Render, cambiar Runtime a "Java"
3. Build Command: `./mvnw clean package -DskipTests`
4. Start Command: `java -Dserver.port=$PORT -Dspring.profiles.active=prod -jar target/frc-efact-backend-*.jar`

#### 2. Build Command incorrecto

**Verificar en Render:**
```bash
./mvnw clean package -DskipTests
```

#### 3. Start Command incorrecto

**Verificar en Render:**
```bash
java -Dserver.port=$PORT -Dspring.profiles.active=prod -jar target/frc-efact-backend-*.jar
```

#### 3. Variables de entorno faltantes

**Verificar que están configuradas:**
- `DATABASE_URL`
- `JWT_SECRET`
- `SPRING_PROFILES_ACTIVE=prod`

#### 4. Root Directory incorrecto (si es monorepo)

**Verificar en Render:**
```
Root Directory: frc-efact-backend
```

#### 5. Revisar logs de build

**En Render Dashboard → Service → Logs:**
- Buscar errores de compilación
- Verificar que dependencies se descargan
- Verificar que JAR se crea correctamente

### Frontend deployment falla en Render

**Síntomas:**
- Build falla
- Página en blanco
- Error 404

**Soluciones:**

#### 1. Build Command incorrecto

**Verificar en Render:**
```bash
npm ci && npm run build -- --configuration production
```

#### 2. Publish Directory incorrecto

**Verificar en Render:**
```
dist/frc-efact-frontend/browser
```

#### 3. Archivo _redirects faltante

**Crear archivo `src/_redirects`:**
```
/*    /index.html   200
```

#### 4. Environment de producción no configurado

**Verificar `environment.prod.ts`:**
```typescript
export const environment = {
  production: true,
  apiUrl: 'https://frc-efact-backend.onrender.com/api'
};
```

### Database connection falla en producción

**Síntomas:**
```
Connection refused
Authentication failed
```

**Soluciones:**

1. **Usar Internal Database URL (no External):**
   ```
   postgresql://user:pass@dpg-xxx-a/database
   ```

2. **Verificar que backend y database están en la misma región**

3. **Verificar que database está en estado "Available"**

4. **Verificar credenciales en Render Dashboard**

### Cold start lento (Plan Free)

**Síntomas:**
- Primera request después de inactividad tarda 30-60 segundos
- Servicio "duerme" después de 15 minutos

**Soluciones:**

1. **Upgrade a plan Starter ($7/mes):**
   - Elimina cold starts
   - Servicio siempre activo

2. **Implementar ping periódico (workaround):**
   ```bash
   # Usar servicio externo como UptimeRobot
   # O crear cron job que haga ping cada 10 minutos
   */10 * * * * curl https://frc-efact-backend.onrender.com/actuator/health
   ```

---

## Problemas de Performance

### Backend responde lentamente

**Síntomas:**
- Requests tardan > 2 segundos
- Timeout errors

**Soluciones:**

1. **Verificar logs para errores**

2. **Optimizar queries de base de datos:**
   ```sql
   -- Agregar índices
   CREATE INDEX idx_usuario_username ON persona.usuario(username);
   ```

3. **Aumentar connection pool:**
   ```yaml
   spring:
     datasource:
       hikari:
         maximum-pool-size: 10
   ```

4. **Upgrade plan de Render:**
   - Más CPU y RAM
   - Mejor performance

5. **Implementar caching:**
   ```java
   @Cacheable("users")
   public User findById(Long id) {
       return userRepository.findById(id);
   }
   ```

### Frontend carga lentamente

**Síntomas:**
- Página tarda > 5 segundos en cargar
- Bundles muy grandes

**Soluciones:**

1. **Optimizar build de producción:**
   ```bash
   ng build --configuration production --optimization
   ```

2. **Implementar lazy loading:**
   ```typescript
   {
     path: 'admin',
     loadChildren: () => import('./admin/admin.module').then(m => m.AdminModule)
   }
   ```

3. **Optimizar imágenes:**
   - Usar formatos modernos (WebP)
   - Comprimir imágenes
   - Usar lazy loading para imágenes

4. **Analizar bundle size:**
   ```bash
   npm run build -- --stats-json
   npx webpack-bundle-analyzer dist/frc-efact-frontend/browser/stats.json
   ```

### Database queries lentas

**Síntomas:**
- Queries tardan > 1 segundo
- Backend logs muestran slow queries

**Soluciones:**

1. **Agregar índices:**
   ```sql
   CREATE INDEX idx_usuario_email ON persona.usuario(email);
   CREATE INDEX idx_usuario_is_active ON persona.usuario(is_active);
   ```

2. **Optimizar queries:**
   ```java
   // Usar fetch joins para evitar N+1
   @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.roles WHERE u.id = :id")
   Optional<Usuario> findByIdWithRoles(@Param("id") Long id);
   ```

3. **Habilitar query logging:**
   ```yaml
   spring:
     jpa:
       show-sql: true
       properties:
         hibernate:
           format_sql: true
   ```

4. **Upgrade plan de database en Render**

---

## Comandos Útiles de Diagnóstico

### Backend

```bash
# Ver logs en tiempo real
tail -f logs/spring-boot-logger.log

# Verificar health
curl http://localhost:8080/actuator/health

# Verificar métricas
curl http://localhost:8080/actuator/metrics

# Test de conexión a database
psql -h localhost -U postgres -d frc_efact_db -c "SELECT 1;"

# Compilar sin tests
./mvnw clean package -DskipTests

# Ejecutar solo tests
./mvnw test
```

### Frontend

```bash
# Limpiar caché
rm -rf .angular node_modules package-lock.json
npm install

# Build de producción
npm run build -- --configuration production

# Analizar bundle
npm run build -- --stats-json
npx webpack-bundle-analyzer dist/frc-efact-frontend/browser/stats.json

# Ejecutar tests
npm test

# Lint
npm run lint
```

### Database

```bash
# Conectar a database
psql -U postgres -d frc_efact_db

# Ver tablas
\dt

# Ver esquemas
\dn

# Ver índices
\di

# Ver tamaño de tablas
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
FROM pg_tables
WHERE schemaname NOT IN ('pg_catalog', 'information_schema')
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

# Ver queries lentas
SELECT pid, now() - pg_stat_activity.query_start AS duration, query 
FROM pg_stat_activity 
WHERE state = 'active' 
ORDER BY duration DESC;
```

---

## Contacto y Soporte

Si ninguna de estas soluciones resuelve tu problema:

1. **Revisar logs detalladamente**
2. **Buscar error específico en Google/Stack Overflow**
3. **Consultar documentación oficial:**
   - [Spring Boot Docs](https://docs.spring.io/spring-boot/docs/current/reference/html/)
   - [Angular Docs](https://angular.io/docs)
   - [Render Docs](https://render.com/docs)
4. **Crear issue en el repositorio del proyecto**

---

**Última actualización:** 08/10/2025


---

## Problema Específico de Render: DATABASE_URL Format

### Error: "Driver claims to not accept jdbcUrl"

**Síntomas:**
```
Driver org.postgresql.Driver claims to not accept jdbcUrl, postgresql://user:pass@host/db
```

**Causa:**
Render proporciona `DATABASE_URL` en formato `postgresql://` pero Spring Boot necesita `jdbc:postgresql://`.

**Solución:**
✅ **Ya está solucionado** en el código con `DatabaseConfig.java` que convierte automáticamente el formato.

**Verificar que tienes la solución:**
1. Asegúrate de que tu código tiene el archivo:
   ```
   frc-efact-backend/src/main/java/com/frcefact/config/DatabaseConfig.java
   ```

2. Haz pull del código más reciente:
   ```bash
   git pull origin main
   ```

3. Render redesplegará automáticamente

**Ver documentación completa:** [RENDER_DATABASE_URL_FIX.md](./RENDER_DATABASE_URL_FIX.md)

---

**Última actualización:** 08/10/2025
