# FRC eFact - Sistema de Facturación Electrónica

Sistema web para gestión de facturación electrónica con backend en Spring Boot y frontend en Angular.

## 🚀 Inicio Rápido

### Desarrollo Local

```bash
# 1. Configurar base de datos
cd frc-efact-backend
./setup-local-db.sh

# 2. Iniciar backend (en una terminal)
./dev.sh

# 3. Iniciar frontend (en otra terminal)
cd ../frc-efact-frontend
./dev.sh
```

Accede a `http://localhost:4200` y usa:
- Usuario: `admin`
- Password: `admin123`

### Deployment en Render

Ver la guía completa en: **[docs/deployment/render/README.md](docs/deployment/render/README.md)**

## 📚 Documentación

### Para Empezar
- **[Guía de Desarrollo Local](docs/guides/DEVELOPMENT.md)** - Setup y desarrollo en tu máquina
- **[Guía de Testing](docs/guides/TESTING.md)** - Cómo probar la aplicación
- **[Guía de Postman](docs/guides/POSTMAN.md)** - Testing de API

### Deployment
- **[Deployment en Render](docs/deployment/render/README.md)** - ⭐ Guía principal para deployment
- **[Setup Manual en Render](docs/deployment/render/MANUAL_SETUP.md)** - Si prefieres crear servicios manualmente
- **[Scripts de Deployment](docs/deployment/scripts/README.md)** - Scripts útiles

### Solución de Problemas
- **[Problemas en Render](docs/troubleshooting/RENDER_ISSUES.md)** - Errores específicos de Render
- **[Errores Comunes](docs/troubleshooting/COMMON_ERRORS.md)** - Problemas generales

## 🏗️ Arquitectura

```
frc-efact/
├── frc-efact-backend/     # Spring Boot API
│   ├── src/
│   ├── Dockerfile
│   └── API_DOCUMENTATION.md
├── frc-efact-frontend/    # Angular SPA
│   ├── src/
│   └── README.md
├── docs/                  # 📚 Documentación organizada
│   ├── deployment/        # Guías de deployment
│   ├── guides/            # Guías de uso
│   └── troubleshooting/   # Solución de problemas
└── render.yaml            # Blueprint para Render
```

## 🔧 Tecnologías

**Backend:**
- Java 17
- Spring Boot 3.x
- PostgreSQL
- JWT Authentication
- Flyway Migrations

**Frontend:**
- Angular 17
- TypeScript
- Tailwind CSS
- RxJS

## 🔐 Seguridad

- Autenticación JWT
- CORS configurado
- Passwords hasheados con BCrypt
- HTTPS en producción

Ver más en:
- [Backend Security](frc-efact-backend/SECURITY.md)
- [Frontend Security](frc-efact-frontend/SECURITY.md)

## 📝 API Documentation

Ver documentación completa de endpoints en: [frc-efact-backend/API_DOCUMENTATION.md](frc-efact-backend/API_DOCUMENTATION.md)

## 🤝 Contribuir

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## 📄 Licencia

Este proyecto es privado y confidencial.

## 🆘 Soporte

¿Problemas? Revisa:
1. [Errores Comunes](docs/troubleshooting/COMMON_ERRORS.md)
2. [Problemas en Render](docs/troubleshooting/RENDER_ISSUES.md)
3. Logs de la aplicación
4. Issues en el repositorio
