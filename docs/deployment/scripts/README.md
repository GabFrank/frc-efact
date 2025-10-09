# Scripts de Deployment

Scripts útiles para preparar y validar deployments.

## Scripts Disponibles

### `prepare-deployment.sh`
Prepara el proyecto para deployment verificando configuraciones.

```bash
./docs/deployment/scripts/prepare-deployment.sh
```

### `validate-production.sh`
Valida que los servicios en producción estén funcionando correctamente.

```bash
./docs/deployment/scripts/validate-production.sh <backend-url> <frontend-url>
```

Ejemplo:
```bash
./docs/deployment/scripts/validate-production.sh \
  https://frc-efact-backend.onrender.com \
  https://frc-efact-frontend.onrender.com
```

### `verify-integration.sh`
Verifica la integración entre backend y frontend en desarrollo local.

```bash
./docs/deployment/scripts/verify-integration.sh
```

## Uso

Todos los scripts deben ejecutarse desde la raíz del proyecto.
