# FRC eFact — Sistema de Facturación Electrónica (Paraguay / SIFEN)

Sistema web full-stack para emitir y gestionar **Documentos Electrónicos (DE)** conforme a la
normativa **SIFEN** (Sistema Integrado de Facturación Electrónica Nacional) de la **SET** paraguaya.

- **Multi-empresa / multi-usuario** con roles (ADMIN, EMPRESA_ADMIN, FACTURADOR, LECTOR).
- **Firma digital** con certificado `.pfx` por empresa, generación de **XML SIFEN**, **CDC** de 44
  caracteres, **QR** y envío a SIFEN en lotes.
- **KuDE PDF** (Jasper) descargable y envío por email.
- **Documentos soportados**: Factura electrónica, **Nota de Crédito**, **Nota de Débito** y
  **Nota de Remisión** (con transporte: vehículos y choferes).
- **Eventos SIFEN**: cancelación, inutilización de numeración y nominación de DE.

---

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
npm start
```

Backend en `http://localhost:8080` (context-path `/api`, Swagger en `/swagger-ui.html`).
Frontend en `http://localhost:4200`.

**Credenciales dev** (sembradas por Flyway `V3`/`V4`):

| Usuario    | Password   |
|------------|------------|
| `admin`    | `admin123` |
| `testuser` | `test123`  |

> Nota: `EMPRESA_ADMIN`, `FACTURADOR` y `LECTOR` son **roles**, no usuarios sembrados. El único
> usuario con rol de administrador global cargado por defecto es `admin`.

### Deployment

Producción corre en una **VM Hetzner** desde el 2026-07-07: **https://efact.frc-ecommerce.com**
(`/` → SPA, `/api` → backend). El deploy es **manual por SSH** (`docker compose up -d --build`);
`git push` **no** despliega, solo dispara `semantic-release`.

Guía operativa: **[docs/deployment/hetzner/RUNBOOK_VM.md](docs/deployment/hetzner/RUNBOOK_VM.md)**.
La documentación de Render queda como referencia histórica — el servicio está **suspendido**
como ventana de rollback, no dado de baja.

---

## 📚 Documentación

> 💡 **¿Primera vez?** Leé [START_HERE.md](START_HERE.md) para una guía rápida.
> Toda la documentación vive en [docs/](docs/) — el índice está en [docs/ESTRUCTURA.md](docs/ESTRUCTURA.md).

### 🚀 Para empezar
- **[Guía de Desarrollo Local](docs/guides/DEVELOPMENT.md)** — Setup y desarrollo
- **[Guía de Testing](docs/guides/TESTING.md)** — Cómo probar la aplicación
- **[Guía de Postman](docs/guides/POSTMAN_GUIDE.md)** — Testing de API

### 🌐 Deployment
- **[Runbook VM Hetzner](docs/deployment/hetzner/RUNBOOK_VM.md)** — Guía principal (producción actual)
- **[Plan de migración a Hetzner](docs/deployment/hetzner/PLAN_MIGRACION_HETZNER.md)** — Contexto y riesgos
- **[Scripts de Deployment](docs/deployment/scripts/README.md)** — Scripts útiles
- _Legacy:_ [Deployment en Render](docs/deployment/render/README.md) · [Setup Manual](docs/deployment/render/MANUAL_SETUP.md) — Render descartado

### 🔧 Solución de problemas
- **[Errores Comunes](docs/troubleshooting/COMMON_ERRORS.md)**
- _Legacy:_ [Problemas en Render](docs/troubleshooting/RENDER_ISSUES.md)

### 📖 Arquitectura y dominio
- **[Estructura de Documentación](docs/ESTRUCTURA.md)** — Cómo está organizado `docs/`
- **[Flujo del Sistema — Entidades](docs/FLUJO_SISTEMA_ENTIDADES.md)** — Mapa entidad por entidad y flujo SIFEN
- **[Tareas Pendientes / Deuda técnica](docs/TAREAS_PENDIENTES.md)**

---

## 🏗️ Arquitectura

```
frc-efact/
├── frc-efact-backend/     # Spring Boot API (puerto 8080, context /api)
│   ├── src/
│   ├── Dockerfile
│   └── API_DOCUMENTATION.md
├── frc-efact-frontend/    # Angular SPA (puerto 4200)
│   ├── src/
│   └── README.md
├── docs/                  # 📚 Documentación técnica y funcional
├── certificates/          # Certificados .pfx para firma SIFEN (no se commitean)
├── deploy/                # Stack VM Hetzner (.env.example, nginx vhost, backup)
├── docker-compose.prod.yml # Stack de producción (VM Hetzner)
└── render.yaml            # Blueprint Render (legacy — servicio suspendido)
```

Detalle de capas y flujo de datos: [docs/FLUJO_SISTEMA_ENTIDADES.md](docs/FLUJO_SISTEMA_ENTIDADES.md).

## 🔧 Tecnologías

**Backend:**
- Java 17
- Spring Boot 3.2.1
- PostgreSQL 15+ con Flyway (migraciones versionadas)
- Spring Security 6 + JWT (jjwt) + Auth0 OAuth2 Resource Server
- JPA / Hibernate 6 (`ddl-auto: validate`)
- jsifenlib (fork interno) para SIFEN, JasperReports (KuDE), ZXing (QR), Apache POI (Excel)

**Frontend:**
- Angular 17 (standalone components)
- Angular Material 17 + SCSS
- NgRx 17 (store / effects / entity)
- @auth0/auth0-angular, Chart.js
- TypeScript 5.4, RxJS 7.8

## 🔐 Seguridad

- Autenticación JWT local **y** Auth0 (ambas vías).
- Passwords hasheados con BCrypt.
- CORS configurado; HTTPS en producción.
- Datos sensibles (CSC, password de certificado) cifrados con AES-256.

Ver más en:
- [Backend Security](frc-efact-backend/SECURITY.md)
- [Frontend Security](frc-efact-frontend/SECURITY.md)

## 📝 API Documentation

Documentación de endpoints en [frc-efact-backend/API_DOCUMENTATION.md](frc-efact-backend/API_DOCUMENTATION.md)
y Swagger en `http://localhost:8080/swagger-ui.html`.

## 📄 Licencia

Proyecto privado y confidencial.

## 🆘 Soporte

Ante problemas, revisá:
1. **[START_HERE.md](START_HERE.md)** — Guía rápida de inicio
2. **[Errores Comunes](docs/troubleshooting/COMMON_ERRORS.md)**
3. **[Troubleshooting de la VM](docs/deployment/hetzner/RUNBOOK_VM.md)** — sección final
4. Logs de la aplicación
