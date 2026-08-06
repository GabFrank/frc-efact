# 📁 Estructura de la Documentación

Índice real de la carpeta `docs/` y de los documentos técnicos que viven junto al código.
Todos los enlaces de abajo apuntan a archivos que existen en el repositorio.

## 🗂️ Árbol real de `docs/`

```
docs/
├── ESTRUCTURA.md                     # Este archivo (índice de documentación)
├── FLUJO_SISTEMA_ENTIDADES.md        # Mapa entidad por entidad + flujo SIFEN
├── TAREAS_PENDIENTES.md              # Deuda técnica / issues conocidos
├── ANALISIS_DIFERENCIAS_SIFEN_SERVICE.md   # Comparativa SifenService vs. repo de referencia
├── AUTH0_SETUP.md                    # Configuración de Auth0
├── CURSOR_RULES.mdc                  # Reglas del editor
├── MEJORA_ACTIVIDAD_ECONOMICA.md     # Nota de mejora (actividad económica)
│
├── sifen/                            # 📑 SIFEN v150: manuales, ejemplos y recursos
│   ├── manual-implementacion-nre-y-cancelacion-sifen-v150.md
│   ├── manual-notas-credito-debito-remision-sifen-jsifenlib.md
│   ├── analisis-errores-nre-sifen-v150.md
│   ├── implementacion_monedas_sifen_v150.md
│   ├── tipos_clientes_sifen_v150.md
│   ├── tipos_productos_sifen_v150.md
│   ├── correcion-transportista-chofer.md
│   ├── ejemplo_de_aprobado.xml
│   ├── ejemplo_de_moneda_extranjera.xml
│   ├── ejemplo_nota_credito.xml
│   ├── KuDE_NotaCredito.jrxml        # Plantilla Jasper de KuDE
│   ├── carga_geografia_sifen.sql
│   ├── CODIGO DE REFERENCIA GEOGRAFICA.csv / .xlsx
│   └── email/                        # Recursos de plantillas de email
│
├── deployment/                       # 🚀 Deployment
│   ├── hetzner/                      # ⭐ PRODUCCIÓN ACTUAL (desde 2026-07-07)
│   │   ├── RUNBOOK_VM.md             # ⭐ Guía operativa vigente
│   │   └── PLAN_MIGRACION_HETZNER.md # Contexto y riesgos de la migración
│   ├── AGREGAR_VARIABLES_RENDER.md   # legacy
│   ├── DEPLOYMENT_CHECKLIST.md       # legacy
│   ├── PRODUCTION_DEPLOYMENT.md      # legacy
│   ├── RENDER_DOCKER_BUILD_ARGS.md   # legacy
│   ├── RENDER_GITHUB_PACKAGES.md     # legacy
│   ├── RENDER_MCP_SETUP.md           # legacy
│   ├── STEP_BY_STEP_GUIDE.md         # legacy
│   ├── render/                       # legacy — Render descartado
│   │   ├── README.md
│   │   └── MANUAL_SETUP.md
│   └── scripts/
│       ├── README.md
│       ├── prepare-deployment.sh
│       ├── validate-production.sh
│       ├── verify-integration.sh
│       ├── export-dev-data.sh
│       └── import-prod-data.sh
│
├── guides/                           # 📖 Guías de uso
│   ├── DEVELOPMENT.md
│   ├── TESTING.md
│   └── POSTMAN_GUIDE.md
│
├── troubleshooting/                  # 🔧 Solución de problemas
│   ├── COMMON_ERRORS.md
│   └── RENDER_ISSUES.md
│
├── archive/                          # 🗃️ Resúmenes históricos de implementación (referencia)
│   ├── README.md
│   └── *_IMPLEMENTATION_SUMMARY.md, etc.
│
├── rshk-jsifenlib/                   # 📦 Copia del fork de la librería SIFEN (referencia de código)
├── franco-system-backend-filial/     # (vacío — placeholder de repo de referencia)
└── franco-system-backend-servidor/   # (vacío — placeholder de repo de referencia)
```

## 📄 Documentación técnica junto al código

Estos documentos NO están en `docs/`, viven en cada subproyecto:

- [frc-efact-backend/API_DOCUMENTATION.md](../frc-efact-backend/API_DOCUMENTATION.md) — Endpoints REST
- [frc-efact-backend/DATABASE_STANDARDS.md](../frc-efact-backend/DATABASE_STANDARDS.md) — Estándares de DB
- [frc-efact-backend/CONTROLLER_ROUTING_RULE.md](../frc-efact-backend/CONTROLLER_ROUTING_RULE.md) — Regla de `@RequestMapping`
- [frc-efact-backend/SECURITY.md](../frc-efact-backend/SECURITY.md)
- [frc-efact-backend/CONFIGURACION_GMAIL.md](../frc-efact-backend/CONFIGURACION_GMAIL.md)
- [frc-efact-backend/GITHUB_PACKAGES_SETUP.md](../frc-efact-backend/GITHUB_PACKAGES_SETUP.md)
- [frc-efact-frontend/SECURITY.md](../frc-efact-frontend/SECURITY.md)

En la raíz del repo: [README.md](../README.md), [START_HERE.md](../START_HERE.md),
[MANUAL_DE_USUARIO.md](../MANUAL_DE_USUARIO.md), [CLAUDE.md](../CLAUDE.md), [CHANGELOG.md](../CHANGELOG.md).

## 🔍 Búsqueda rápida

| Quiero... | Ir a... |
|-----------|---------|
| Entender el modelo de datos y el flujo SIFEN | [FLUJO_SISTEMA_ENTIDADES.md](FLUJO_SISTEMA_ENTIDADES.md) |
| Ver deuda técnica / bugs conocidos | [TAREAS_PENDIENTES.md](TAREAS_PENDIENTES.md) |
| Deployar a producción (VM Hetzner) | [deployment/hetzner/RUNBOOK_VM.md](deployment/hetzner/RUNBOOK_VM.md) |
| Desarrollar localmente | [guides/DEVELOPMENT.md](guides/DEVELOPMENT.md) |
| Probar la API | [guides/POSTMAN_GUIDE.md](guides/POSTMAN_GUIDE.md) |
| Solucionar un error de la VM de producción | [deployment/hetzner/RUNBOOK_VM.md](deployment/hetzner/RUNBOOK_VM.md) (§ Troubleshooting) |
| Solucionar un error general | [troubleshooting/COMMON_ERRORS.md](troubleshooting/COMMON_ERRORS.md) |
| Manuales y ejemplos SIFEN | [sifen/](sifen/) |
| Configurar Auth0 | [AUTH0_SETUP.md](AUTH0_SETUP.md) |
| Ver endpoints REST | [../frc-efact-backend/API_DOCUMENTATION.md](../frc-efact-backend/API_DOCUMENTATION.md) |

## 📝 Notas

- La carpeta `archive/` contiene resúmenes de implementación de features ya construidas; sirven
  como referencia histórica, no como documentación viva.
- `rshk-jsifenlib/` es una copia del fork interno de la librería SIFEN (`GabFrank/rshk-jsifenlib`),
  útil para inspeccionar el código de la dependencia. Las carpetas `franco-system-backend-*`
  están reservadas para los repos de referencia del mismo autor y hoy están vacías.
