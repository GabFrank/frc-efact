# 📁 Estructura de Documentación

Visualización de cómo está organizada toda la documentación del proyecto.

## 🗂️ Estructura Completa

```
frc-efact/
│
├── START_HERE.md                    # 👈 EMPIEZA AQUÍ - Guía rápida
├── README.md                        # 📖 Índice principal del proyecto
│
├── docs/                            # 📚 Toda la documentación
│   ├── ESTRUCTURA.md                # Guía de navegación
│   ├── RESUMEN_CAMBIOS.md           # Resumen de reorganización
│   │
│   ├── deployment/                  # 🚀 Todo sobre deployment
│   │   ├── render/                  # Específico de Render
│   │   │   ├── README.md           # ⭐ GUÍA PRINCIPAL DE RENDER
│   │   │   └── MANUAL_SETUP.md     # Setup manual (alternativa)
│   │   │
│   │   └── scripts/                 # Scripts automatizados
│   │       ├── README.md
│   │       ├── prepare-deployment.sh
│   │       ├── validate-production.sh
│   │       └── verify-integration.sh
│   │
│   ├── guides/                      # 📖 Guías de uso
│   │   ├── DEVELOPMENT.md          # Desarrollo local
│   │   ├── TESTING.md              # Testing manual y automatizado
│   │   └── POSTMAN_GUIDE.md        # Testing de API con Postman
│   │
│   └── troubleshooting/            # 🔧 Solución de problemas
│       ├── RENDER_ISSUES.md        # Problemas específicos de Render
│       └── COMMON_ERRORS.md        # Errores generales
│
├── frc-efact-backend/              # Backend Spring Boot
│   ├── README.md                    # Documentación del backend
│   ├── API_DOCUMENTATION.md        # Documentación de endpoints
│   ├── SECURITY.md                 # Configuración de seguridad
│   ├── DEPLOYMENT.md               # Deployment del backend
│   ├── Dockerfile                  # Para deployment en Render
│   ├── dev.sh                      # Script para desarrollo local
│   └── setup-local-db.sh           # Setup de base de datos local
│
└── frc-efact-frontend/             # Frontend Angular
    ├── README.md                    # Documentación del frontend
    ├── SECURITY.md                 # Configuración de seguridad
    ├── DEPLOYMENT.md               # Deployment del frontend
    └── dev.sh                      # Script para desarrollo local
```

## 🎯 Flujo de Navegación

### Para Desarrollo Local
```
START_HERE.md
    ↓
docs/guides/DEVELOPMENT.md
    ↓
frc-efact-backend/README.md
frc-efact-frontend/README.md
```

### Para Deployment en Render
```
START_HERE.md
    ↓
docs/deployment/render/README.md  ⭐ ÚNICA GUÍA NECESARIA
    ↓
(Si hay problemas)
    ↓
docs/troubleshooting/RENDER_ISSUES.md
```

### Para Testing
```
START_HERE.md
    ↓
docs/guides/TESTING.md
    ↓
docs/guides/POSTMAN_GUIDE.md (para API)
```

### Para Solución de Problemas
```
(Encuentras un error)
    ↓
docs/troubleshooting/RENDER_ISSUES.md (si es en Render)
    o
docs/troubleshooting/COMMON_ERRORS.md (si es general)
```

## 📊 Documentos por Categoría

### 🚀 Deployment (2 guías)
- `docs/deployment/render/README.md` - **Guía principal** (la única que necesitas)
- `docs/deployment/render/MANUAL_SETUP.md` - Alternativa manual

### 📖 Guías de Uso (3 guías)
- `docs/guides/DEVELOPMENT.md` - Desarrollo local
- `docs/guides/TESTING.md` - Testing
- `docs/guides/POSTMAN_GUIDE.md` - API testing

### 🔧 Troubleshooting (2 guías)
- `docs/troubleshooting/RENDER_ISSUES.md` - Problemas de Render
- `docs/troubleshooting/COMMON_ERRORS.md` - Errores generales

### 🛠️ Scripts (3 scripts)
- `docs/deployment/scripts/prepare-deployment.sh` - Preparar deployment
- `docs/deployment/scripts/validate-production.sh` - Validar producción
- `docs/deployment/scripts/verify-integration.sh` - Verificar integración

### 📚 Documentación Técnica (6 documentos)
- `frc-efact-backend/README.md` - Backend general
- `frc-efact-backend/API_DOCUMENTATION.md` - Endpoints
- `frc-efact-backend/SECURITY.md` - Seguridad backend
- `frc-efact-frontend/README.md` - Frontend general
- `frc-efact-frontend/SECURITY.md` - Seguridad frontend
- `frc-efact-frontend/COMPONENT_TESTING.md` - Testing de componentes

## 🎨 Código de Colores

En los documentos verás estos emojis:

- ⭐ = Documento principal/más importante
- 👈 = Punto de entrada recomendado
- 🚀 = Relacionado con deployment
- 📖 = Guía de uso
- 🔧 = Solución de problemas
- 💻 = Desarrollo local
- 🧪 = Testing
- 📚 = Documentación técnica

## 💡 Consejos de Navegación

1. **Siempre empieza en `START_HERE.md`** - Te dirije al documento correcto
2. **Para Render, solo necesitas un documento** - `docs/deployment/render/README.md`
3. **Los documentos están enlazados** - Sigue los links internos
4. **Usa el README principal** - `README.md` en la raíz tiene todo organizado
5. **Busca por emoji** - Los emojis te ayudan a identificar el tipo de documento

## 🔍 Búsqueda Rápida

¿Buscas algo específico?

| Quiero... | Ve a... |
|-----------|---------|
| Hacer deployment en Render | `docs/deployment/render/README.md` |
| Desarrollar localmente | `docs/guides/DEVELOPMENT.md` |
| Probar la API | `docs/guides/POSTMAN_GUIDE.md` |
| Solucionar error en Render | `docs/troubleshooting/RENDER_ISSUES.md` |
| Solucionar error general | `docs/troubleshooting/COMMON_ERRORS.md` |
| Ver endpoints disponibles | `frc-efact-backend/API_DOCUMENTATION.md` |
| Configurar seguridad | `frc-efact-backend/SECURITY.md` |

## 📝 Notas

- **Eliminados**: 17 documentos duplicados que estaban en la raíz
- **Consolidados**: Múltiples guías de Render en una sola
- **Organizados**: Todo en carpetas lógicas
- **Simplificados**: Una guía clara para cada tarea
