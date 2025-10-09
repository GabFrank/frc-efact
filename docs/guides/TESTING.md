# Guía de Testing

Cómo probar la aplicación manualmente y con herramientas.

## 🧪 Testing Manual

### 1. Verificar Backend

```bash
# Health check
curl http://localhost:8080/actuator/health

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

### 2. Verificar Frontend

1. Abre `http://localhost:4200`
2. Intenta hacer login con:
   - Usuario: `admin`
   - Password: `admin123`
3. Verifica que puedas navegar por la aplicación

## 🔧 Testing con Postman

Ver guía detallada en `POSTMAN_GUIDE.md`

## 🚀 Testing de Integración

Ejecuta el script de verificación:

```bash
./docs/deployment/scripts/verify-integration.sh
```

Este script verifica:
- Backend está corriendo
- Frontend está corriendo
- Conexión entre ambos funciona
- Base de datos está accesible

## 📊 Testing en Producción

Para validar el deployment en Render:

```bash
./docs/deployment/scripts/validate-production.sh \
  https://tu-backend.onrender.com \
  https://tu-frontend.onrender.com
```

## 🐛 Debugging

### Backend
- Revisa logs: `frc-efact-backend/logs/`
- Usa el debugger de tu IDE en puerto 5005

### Frontend
- Abre DevTools del navegador (F12)
- Revisa la pestaña Console para errores
- Revisa la pestaña Network para requests fallidos
