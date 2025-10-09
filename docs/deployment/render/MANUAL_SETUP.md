# Setup Manual en Render

Si prefieres crear los servicios manualmente en lugar de usar el Blueprint.

## 1. Crear Base de Datos PostgreSQL

1. En Render Dashboard, click "New" → "PostgreSQL"
2. Nombre: `frc-efact-db`
3. Database: `frc_efact`
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
   JWT_SECRET=<genera-un-secreto-seguro-minimo-32-caracteres>
   FRONTEND_URL=<URL-del-frontend-cuando-lo-crees>
   SPRING_PROFILES_ACTIVE=prod
   ```

5. Click "Create Web Service"

## 3. Crear Frontend (Static Site)

1. Click "New" → "Static Site"
2. Conecta tu repositorio
3. Configuración:
   - **Name**: `frc-efact-frontend`
   - **Branch**: `main`
   - **Root Directory**: `frc-efact-frontend`
   - **Build Command**: `npm install && npm run build`
   - **Publish Directory**: `dist/frc-efact-frontend/browser`

4. Variables de Entorno:
   ```
   API_URL=<URL-del-backend-del-paso-2>
   ```

5. Click "Create Static Site"

## 4. Actualizar Variables de Entorno

Una vez que ambos servicios estén creados:

1. Ve al backend y actualiza `FRONTEND_URL` con la URL del frontend
2. Ve al frontend y verifica que `API_URL` apunte al backend
3. Redeploy ambos servicios si es necesario

## 5. Verificar

- Backend health: `https://tu-backend.onrender.com/actuator/health`
- Frontend: `https://tu-frontend.onrender.com`
- Login con usuario admin

## Notas

- El setup manual es más tedioso pero te da más control
- Usa el Blueprint para deployments más rápidos
- Guarda las URLs de tus servicios para futuras referencias
