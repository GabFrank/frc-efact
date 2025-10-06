# FRC eFact - Sistema de Facturación Electrónica

Monorepo completo del sistema FRC eFact, incluyendo backend (Spring Boot) y frontend (Angular).

## 📁 Estructura del Proyecto

```
frc-efact/
├── frc-efact-backend/      # Backend API (Spring Boot + PostgreSQL)
├── frc-efact-frontend/     # Frontend SPA (Angular + Material Design)
├── .kiro/                  # Especificaciones y configuración de desarrollo
└── README.md               # Este archivo
```

## 🚀 Inicio Rápido

### Backend
```bash
cd frc-efact-backend
./mvnw spring-boot:run
```
El backend estará disponible en `http://localhost:8080`

### Frontend
```bash
cd frc-efact-frontend
npm install
npm start
```
El frontend estará disponible en `http://localhost:4200`

## 📚 Documentación

- **Backend**: Ver [frc-efact-backend/README.md](./frc-efact-backend/README.md)
- **Frontend**: Ver [frc-efact-frontend/README.md](./frc-efact-frontend/README.md)
- **Estándares de BD**: Ver [frc-efact-backend/DATABASE_STANDARDS.md](./frc-efact-backend/DATABASE_STANDARDS.md)
- **Testing**: Ver [POSTMAN_TEST_GUIDE.md](./POSTMAN_TEST_GUIDE.md)

## 🛠️ Tecnologías

### Backend
- Java 17+
- Spring Boot 3.2+
- Spring Security 6+ (JWT)
- PostgreSQL 15+
- Flyway (migraciones)
- Maven

### Frontend
- Angular 17+
- Angular Material
- TypeScript
- RxJS
- Standalone Components

## 🔧 Requisitos Previos

- **Java**: 17 o superior
- **Node.js**: 18 o superior
- **PostgreSQL**: 15 o superior
- **Maven**: 3.9+ (o usar el wrapper incluido)
- **npm**: 9+ (incluido con Node.js)

## 🗄️ Base de Datos

### Configuración Local

1. Crear la base de datos:
```bash
psql -h localhost -p 5432 -U postgres -c "CREATE DATABASE frc_efact_dev;"
```

2. Las migraciones se ejecutan automáticamente al iniciar el backend

### Credenciales de Prueba

Después de ejecutar las migraciones, puedes usar:
- **Usuario**: `admin`
- **Contraseña**: `admin123`

## 🚢 Deployment

Este proyecto está configurado para deployment en Render usando un monorepo:

- **Backend**: Web Service (Java)
- **Frontend**: Static Site (Node)
- **Base de datos**: PostgreSQL

Ver documentación específica en cada subcarpeta para detalles de deployment.

## 📝 Desarrollo

### Convenciones de Commits

Usamos prefijos para identificar qué parte del proyecto se modifica:

```
feat(backend): Agregar endpoint de usuarios
fix(frontend): Corregir validación de login
docs: Actualizar README principal
chore(backend): Actualizar dependencias
```

### Branches

- `main`: Rama principal (producción)
- `develop`: Rama de desarrollo
- `feature/*`: Nuevas funcionalidades
- `fix/*`: Correcciones de bugs

## 🧪 Testing

### Backend
```bash
cd frc-efact-backend
./mvnw test
```

### Frontend
```bash
cd frc-efact-frontend
npm test
```

## 📄 Licencia

Proyecto privado - FRC eFact

## 👥 Equipo

Desarrollado por el equipo de FRC eFact

---

Para más información sobre cada componente, consulta los README específicos en cada directorio.
