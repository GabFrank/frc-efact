# 👋 Empieza Aquí

## 🚀 Inicio Rápido

### Desarrollo Local

```bash
# Terminal 1 - Backend
cd frc-efact-backend && ./dev.sh

# Terminal 2 - Frontend
cd frc-efact-frontend && ./dev.sh
```

**Credenciales por defecto:**
- Usuario: `admin`
- Password: `admin123`

---

## 📚 Documentación

### Guías Principales
- [Desarrollo Local](docs/guides/DEVELOPMENT.md)
- [Deployment en Render](docs/deployment/render/README.md)
- [Testing](docs/guides/TESTING.md)

### Solución de Problemas
- [Problemas de Render](docs/troubleshooting/RENDER_ISSUES.md)
- [Errores Comunes](docs/troubleshooting/COMMON_ERRORS.md)

### Documentación Técnica
- [Backend](frc-efact-backend/README.md)
- [Frontend](frc-efact-frontend/README.md)
- [API](frc-efact-backend/API_DOCUMENTATION.md)
- [Seguridad](frc-efact-backend/SECURITY.md)

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
