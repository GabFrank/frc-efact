# Guía de Testing

Cómo probar la aplicación manualmente y con herramientas.

## 🧪 Testing Manual

### 1. Verificar Backend

```bash
# Health check (context-path /api)
curl http://localhost:8080/api/actuator/health

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

Producción corre en la **VM Hetzner** (`https://efact.frc-ecommerce.com`) desde el 2026-07-07:

```bash
./docs/deployment/scripts/validate-production.sh \
  https://efact.frc-ecommerce.com \
  https://efact.frc-ecommerce.com
```

> Backend y frontend comparten dominio: `/` → SPA, `/api` → backend.
> El health de producción es `https://efact.frc-ecommerce.com/api/actuator/health`.

**Antes de probar en producción**, verifica que el backend arrancó con las variables obligatorias de `deploy/.env` en la VM (antes iban en el Dashboard de Render):
- `GITHUB_USERNAME` / `GITHUB_TOKEN` — sin ellas el build de Docker ni siquiera compila (`jsifenlib`).
- `MAIL_PASSWORD` — sin valor por defecto; su ausencia puede impedir el arranque.
- `ENCRYPTION_KEY` — 32 caracteres; si falta se usa un default inseguro.

## 🐛 Debugging

### Backend
- Revisa logs: `frc-efact-backend/logs/`
- Usa el debugger de tu IDE en puerto 5005

### Frontend
- Abre DevTools del navegador (F12)
- Revisa la pestaña Console para errores
- Revisa la pestaña Network para requests fallidos
