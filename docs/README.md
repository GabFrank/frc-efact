# Documentación FRC eFact

Toda la documentación del proyecto organizada por categorías.

> 💡 **¿Primera vez aquí?** Lee [ESTRUCTURA.md](ESTRUCTURA.md) para entender cómo está organizada la documentación.

## 📖 Índice

### 🚀 Para Empezar

Si es tu primera vez con el proyecto:

1. **[Guía de Desarrollo Local](guides/DEVELOPMENT.md)** - Setup inicial y desarrollo
2. **[Guía de Testing](guides/TESTING.md)** - Cómo probar la aplicación
3. **[Guía de Postman](guides/POSTMAN_GUIDE.md)** - Testing de API

### 🌐 Deployment

Para hacer deployment en Render:

1. **[Deployment en Render](deployment/render/README.md)** - ⭐ **EMPIEZA AQUÍ**
2. [Setup Manual en Render](deployment/render/MANUAL_SETUP.md) - Alternativa manual
3. [Scripts de Deployment](deployment/scripts/README.md) - Scripts útiles

### 🔧 Solución de Problemas

¿Algo no funciona?

1. **[Problemas en Render](troubleshooting/RENDER_ISSUES.md)** - Errores específicos de Render
2. **[Errores Comunes](troubleshooting/COMMON_ERRORS.md)** - Problemas generales

### 📚 Documentación Técnica

Documentación detallada de cada componente:

- **Backend**: `../frc-efact-backend/`
  - [API Documentation](../frc-efact-backend/API_DOCUMENTATION.md)
  - [Security](../frc-efact-backend/SECURITY.md)
  - [Deployment](../frc-efact-backend/DEPLOYMENT.md)

- **Frontend**: `../frc-efact-frontend/`
  - [README](../frc-efact-frontend/README.md)
  - [Security](../frc-efact-frontend/SECURITY.md)
  - [Component Testing](../frc-efact-frontend/COMPONENT_TESTING.md)
  - [Deployment](../frc-efact-frontend/DEPLOYMENT.md)

## 🎯 Guías Rápidas

### Desarrollo Local

```bash
# Backend
cd frc-efact-backend && ./dev.sh

# Frontend
cd frc-efact-frontend && ./dev.sh
```

### Deployment en Render

1. Ve a [Render Dashboard](https://dashboard.render.com/)
2. New → Blueprint
3. Conecta tu repositorio
4. Apply

Ver guía completa: [deployment/render/README.md](deployment/render/README.md)

### Testing

```bash
# Verificar integración local
./docs/deployment/scripts/verify-integration.sh

# Validar producción
./docs/deployment/scripts/validate-production.sh <backend-url> <frontend-url>
```

## 📁 Estructura de Documentación

```
docs/
├── README.md                    # Este archivo
├── deployment/                  # Todo sobre deployment
│   ├── render/                  # Específico de Render
│   │   ├── README.md           # ⭐ Guía principal
│   │   └── MANUAL_SETUP.md     # Setup manual
│   └── scripts/                 # Scripts útiles
│       ├── README.md
│       ├── prepare-deployment.sh
│       ├── validate-production.sh
│       └── verify-integration.sh
├── guides/                      # Guías de uso
│   ├── DEVELOPMENT.md          # Desarrollo local
│   ├── TESTING.md              # Testing
│   └── POSTMAN_GUIDE.md        # API testing
└── troubleshooting/            # Solución de problemas
    ├── RENDER_ISSUES.md        # Problemas de Render
    └── COMMON_ERRORS.md        # Errores generales
```

## 🔍 Buscar Información

- **¿Cómo hacer deployment?** → [deployment/render/README.md](deployment/render/README.md)
- **¿Error en Render?** → [troubleshooting/RENDER_ISSUES.md](troubleshooting/RENDER_ISSUES.md)
- **¿Cómo desarrollar localmente?** → [guides/DEVELOPMENT.md](guides/DEVELOPMENT.md)
- **¿Cómo probar la API?** → [guides/POSTMAN_GUIDE.md](guides/POSTMAN_GUIDE.md)
- **¿Error general?** → [troubleshooting/COMMON_ERRORS.md](troubleshooting/COMMON_ERRORS.md)

## 💡 Consejos

- Siempre empieza por la guía principal de cada sección
- Los archivos marcados con ⭐ son los más importantes
- Usa los scripts en `deployment/scripts/` para automatizar tareas
- Revisa `troubleshooting/` si algo no funciona
