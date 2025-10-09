# Solución: Error de DATABASE_URL en Render

## 🔴 Problema

Al desplegar en Render, obtienes el error:

```
Driver org.postgresql.Driver claims to not accept jdbcUrl, postgresql://user:pass@host/db
```

## 🎯 Causa

Render proporciona la URL de la base de datos en formato:
```
postgresql://user:password@host:port/database
```

Pero Spring Boot/Hikari necesita:
```
jdbc:postgresql://host:port/database
```

Con usuario y contraseña como propiedades separadas.

## ✅ Solución Implementada

Hemos creado una clase `DatabaseConfig` que convierte automáticamente el formato de Render al formato JDBC.

### Archivo Creado

**`frc-efact-backend/src/main/java/com/frcefact/config/DatabaseConfig.java`**

Esta clase:
1. Lee la variable de entorno `DATABASE_URL` de Render
2. Parsea la URL para extraer: usuario, contraseña, host, puerto y nombre de base de datos
3. Construye la URL JDBC correcta: `jdbc:postgresql://host:port/database`
4. Crea el DataSource con las credenciales separadas

### Cambios en application-prod.yml

Se eliminó la configuración directa de `spring.datasource.url` porque ahora se configura programáticamente.

## 🚀 Deployment

Los cambios ya están en GitHub. Render detectará automáticamente el push y redesplegará.

### Qué Esperar en los Logs

**Antes (Error):**
```
Driver org.postgresql.Driver claims to not accept jdbcUrl, postgresql://...
```

**Después (Éxito):**
```
Connecting to database: jdbc:postgresql://dpg-xxx.oregon-postgres.render.com:5432/frc_efact_db
Database user: frc_efact_user
FrcEfactHikariCP-Prod - Starting...
FrcEfactHikariCP-Prod - Start completed.
```

## 📋 Verificación

Una vez que Render complete el nuevo deploy:

1. **Verifica los logs** en Render Dashboard → Backend Service → Logs
2. **Busca estas líneas:**
   ```
   Connecting to database: jdbc:postgresql://...
   Database user: frc_efact_user
   FrcEfactHikariCP-Prod - Starting...
   ```

3. **Verifica que no hay errores de conexión**

4. **Prueba el health check:**
   ```bash
   curl https://frc-efact-backend.onrender.com/actuator/health
   ```
   
   Debería retornar:
   ```json
   {"status":"UP"}
   ```

## 🔍 Cómo Funciona

### Formato de Render
```
postgresql://frc_efact_user:password123@dpg-xxx.oregon-postgres.render.com:5432/frc_efact_db
```

### Conversión Automática

1. **Parse URI:**
   - User: `frc_efact_user`
   - Password: `password123`
   - Host: `dpg-xxx.oregon-postgres.render.com`
   - Port: `5432`
   - Database: `frc_efact_db`

2. **Construye JDBC URL:**
   ```
   jdbc:postgresql://dpg-xxx.oregon-postgres.render.com:5432/frc_efact_db
   ```

3. **Configura DataSource:**
   ```java
   DataSourceBuilder.create()
       .url("jdbc:postgresql://...")
       .username("frc_efact_user")
       .password("password123")
       .driverClassName("org.postgresql.Driver")
       .build()
   ```

## 🐛 Troubleshooting

### Error: "DATABASE_URL environment variable is not set"

**Causa:** La variable de entorno no está configurada en Render.

**Solución:**
1. Ve a Render Dashboard → Backend Service → Environment
2. Verifica que existe `DATABASE_URL`
3. Si usaste Blueprint, debería estar conectada automáticamente
4. Si no, agrégala manualmente con el "Internal Database URL" de tu PostgreSQL

### Error: "Invalid DATABASE_URL format"

**Causa:** El formato de la URL no es el esperado.

**Solución:**
1. Verifica que la URL comienza con `postgresql://`
2. Verifica que tiene el formato: `postgresql://user:pass@host:port/db`
3. Copia exactamente el "Internal Database URL" de Render (no el External)

### Conexión funciona pero Flyway falla

**Causa:** Flyway también necesita la URL en formato JDBC.

**Solución:** Ya está solucionado. `DatabaseConfig` crea el DataSource que Flyway usa automáticamente.

## 📝 Alternativa Manual (No Recomendada)

Si prefieres no usar `DatabaseConfig`, puedes configurar manualmente en Render:

1. En lugar de usar `DATABASE_URL`, crea variables separadas:
   ```
   DB_HOST=dpg-xxx.oregon-postgres.render.com
   DB_PORT=5432
   DB_NAME=frc_efact_db
   DB_USER=frc_efact_user
   DB_PASSWORD=tu_password
   ```

2. Actualiza `application-prod.yml`:
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
       username: ${DB_USER}
       password: ${DB_PASSWORD}
   ```

**Nota:** Esta alternativa requiere más configuración manual y no es compatible con el Blueprint de Render.

## ✅ Ventajas de la Solución Implementada

- ✅ Compatible con Blueprint de Render
- ✅ Usa la variable `DATABASE_URL` estándar de Render
- ✅ No requiere configuración manual adicional
- ✅ Funciona automáticamente con Flyway
- ✅ Logs claros de conexión
- ✅ Manejo de errores robusto

## 🎉 Resultado Final

Con esta solución, el backend se conectará correctamente a la base de datos PostgreSQL de Render y:

- ✅ Flyway ejecutará las migraciones automáticamente
- ✅ El usuario admin se creará en la base de datos
- ✅ La aplicación iniciará correctamente
- ✅ El health check retornará status UP
- ✅ Los endpoints de API funcionarán

---

**Próximo paso:** Espera a que Render complete el deploy y verifica que funciona con el health check.
