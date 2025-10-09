# Guía de Deployment en Render

Esta es la **ÚNICA GUÍA** que necesitas seguir para hacer deployment en Render.

## 📋 Pre-requisitos

- Cuenta en Render.com
- Repositorio Git con el código
- PostgreSQL database en Render (se crea automáticamente)

## 🚀 Pasos para Deployment

### 1. Preparar el Proyecto

El proyecto ya está configurado con:
- ✅ `render.yaml` - Blueprint para crear servicios automáticamente
- ✅ `Dockerfile` en `frc-efact-backend/` - Para el backend Java
- ✅ Configuración de base de datos automática

### 2. Crear Servicios en Render

**Opción A: Usando Blueprint (Recomendado)**

1. Ve a [Render Dashboard](https://dashboard.render.com/)
2. Click en "New" → "Blueprint"
3. Conecta tu repositorio de GitHub/GitLab
4. Render detectará automáticamente el `render.yaml`
5. Click en "Apply" - Esto creará:
   - Base de datos PostgreSQL
   - Backend (Web Service con Docker)
   - Frontend (Static Site)

**Opción B: Manual**

Si prefieres crear los servicios manualmente, sigue la guía en `MANUAL_SETUP.md`

### 3. Configurar Variables de Entorno

Render configurará automáticamente la mayoría de las variables. Solo necesitas verificar:

**Backend:**
- `DATABASE_URL` - Se configura automáticamente desde la base de datos
- `JWT_SECRET` - Genera uno seguro (mínimo 32 caracteres)
- `FRONTEND_URL` - URL de tu frontend en Render

**Frontend:**
- `API_URL` - URL de tu backend en Render

### 4. Verificar el Deployment

Una vez que los servicios estén desplegados:

1. Verifica que el backend esté corriendo: `https://tu-backend.onrender.com/actuator/health`
2. Verifica que el frontend cargue: `https://tu-frontend.onrender.com`
3. Prueba el login con el usuario admin (ver credenciales en la documentación)

## 🔧 Solución de Problemas Comunes

### Error: "JAVA_HOME not found"
✅ **Solucionado** - El proyecto usa Docker, no necesita JAVA_HOME

### Error: "Invalid DATABASE_URL format"
✅ **Solucionado** - La clase `DatabaseConfig.java` convierte automáticamente el formato de Render

### El backend no inicia
1. Revisa los logs en Render Dashboard
2. Verifica que la base de datos esté corriendo
3. Verifica las variables de entorno

### El frontend no se conecta al backend
1. Verifica que `API_URL` en el frontend apunte al backend correcto
2. Verifica que CORS esté configurado correctamente en el backend
3. Revisa la consola del navegador para errores

## 📚 Documentación Adicional

- **Configuración Manual**: `MANUAL_SETUP.md`
- **Troubleshooting Detallado**: `../../troubleshooting/RENDER_ISSUES.md`
- **Scripts de Validación**: `../scripts/`

## 🎯 Checklist Rápido

- [ ] Cuenta en Render creada
- [ ] Repositorio conectado a Render
- [ ] Blueprint aplicado (servicios creados)
- [ ] Variables de entorno configuradas
- [ ] Backend desplegado y corriendo
- [ ] Frontend desplegado y corriendo
- [ ] Login funciona correctamente

## 💡 Consejos

- Los servicios gratuitos de Render se duermen después de 15 minutos de inactividad
- El primer request después de dormir puede tardar 30-60 segundos
- Usa el plan pagado para producción real
