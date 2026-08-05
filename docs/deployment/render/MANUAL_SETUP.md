# Setup Manual en Render

Si prefieres crear los servicios manualmente en lugar de usar el Blueprint.

## 1. Crear Base de Datos PostgreSQL

1. En Render Dashboard, click "New" → "PostgreSQL"
2. Nombre: `frc-efact-db`
3. Database: `frc_efact_db`
4. User: `frc_efact_user`
5. Region: Elige la más cercana
6. Plan: Free (para desarrollo)
7. Click "Create Database"

**Guarda estos datos:**
- Internal Database URL (para el backend)
- External Database URL (para conexiones locales)

## 2. Crear Backend (Web Service)

1. Click "New" → "Web Service"
2. Conecta tu repositorio
3. Configuración:
   - **Name**: `frc-efact-backend`
   - **Region**: Misma que la base de datos
   - **Branch**: `main`
   - **Root Directory**: `frc-efact-backend`
   - **Environment**: `Docker`
   - **Dockerfile Path**: `frc-efact-backend/Dockerfile`
   - **Docker Build Context**: `.` (raíz del proyecto)
   - **Plan**: Free

4. Variables de Entorno:
   ```
   DATABASE_URL=<Internal Database URL de paso 1>
   JWT_SECRET=<genera-un-secreto-seguro-minimo-512-bits>
   SPRING_PROFILES_ACTIVE=prod
   # Obligatorias para que el build de Docker (jsifenlib) funcione:
   GITHUB_USERNAME=<tu-usuario-github>
   GITHUB_TOKEN=<tu-personal-access-token>
   # Necesarias en runtime:
   MAIL_PASSWORD=<password-app-gmail>        # sin ella el arranque puede fallar
   ENCRYPTION_KEY=<32-caracteres>            # AES-256; sin ella se usa un default inseguro
   ```

   > No existe una variable `FRONTEND_URL`. El backend no la consume; el CORS de producción está fijo en `SecurityConfig.java` / `application-prod.yml`.

5. Click "Create Web Service"

## 3. Crear Frontend (Static Site)

1. Click "New" → "Static Site"
2. Conecta tu repositorio
3. Configuración:
   - **Name**: `frc-efact-frontend`
   - **Branch**: `main`
   - **Root Directory**: `frc-efact-frontend`
   - **Build Command**: `npm ci && npm run build:prod`
   - **Publish Directory**: `dist/frc-efact-frontend/browser`

   > Usa `npm run build:prod` (no `npm run build`, que genera un build de desarrollo).

4. Variables de Entorno: **ninguna requerida.** La URL del backend se fija en `environment.prod.ts` en **tiempo de compilación**; no existe una variable `API_URL` que el frontend consuma en runtime. Si cambia la URL del backend, editar `environment.prod.ts` y rebuildear.

5. Click "Create Static Site"

## 4. Verificar

- Backend health: `https://tu-backend.onrender.com/api/actuator/health`
- Frontend: `https://tu-frontend.onrender.com`
- Login con usuario `admin` / `admin123`

## Notas

- El setup manual es más tedioso pero te da más control
- Usa el Blueprint para deployments más rápidos
- Guarda las URLs de tus servicios para futuras referencias
