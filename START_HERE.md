# 👋 Empieza Aquí

## 🎯 ¿Qué quieres hacer?

### 💻 Desarrollo Local
**Quiero trabajar en mi máquina**

→ Lee: [docs/guides/DEVELOPMENT.md](docs/guides/DEVELOPMENT.md)

```bash
# Inicio rápido
cd frc-efact-backend && ./dev.sh    # Terminal 1
cd frc-efact-frontend && ./dev.sh   # Terminal 2
```

---

### 🚀 Deployment en Render
**Quiero subir la aplicación a Render**

→ Lee: [docs/deployment/render/README.md](docs/deployment/render/README.md)

Pasos:
1. Ve a Render Dashboard
2. New → Blueprint
3. Conecta tu repo
4. Apply

---

### 🧪 Testing
**Quiero probar la aplicación**

→ Lee: [docs/guides/TESTING.md](docs/guides/TESTING.md)

```bash
# Verificar integración
./docs/deployment/scripts/verify-integration.sh
```

---

### 🔧 Solución de Problemas
**Algo no funciona**

→ Lee: [docs/troubleshooting/RENDER_ISSUES.md](docs/troubleshooting/RENDER_ISSUES.md) (para Render)

→ Lee: [docs/troubleshooting/COMMON_ERRORS.md](docs/troubleshooting/COMMON_ERRORS.md) (general)

---

### 📚 Ver Toda la Documentación

→ Lee: [README.md](README.md) (raíz del proyecto)

→ O explora: [docs/ESTRUCTURA.md](docs/ESTRUCTURA.md) (estructura detallada)

---

## 🆘 Ayuda Rápida

**Backend no inicia:**
```bash
cd frc-efact-backend
./setup-local-db.sh
./dev.sh
```

**Frontend no inicia:**
```bash
cd frc-efact-frontend
npm install
./dev.sh
```

**Error en Render:**
- Revisa [docs/troubleshooting/RENDER_ISSUES.md](docs/troubleshooting/RENDER_ISSUES.md)
- Revisa los logs en Render Dashboard

**Credenciales por defecto:**
- Usuario: `admin`
- Password: `admin123`
