# Scripts de Deployment

Scripts útiles para preparar, validar deployments y migrar datos.

## Scripts Disponibles

### `export-dev-data.sh`
Exporta datos de la base de datos de desarrollo para migración a producción.

```bash
cd docs/deployment/scripts
./export-dev-data.sh
```

**Configuración:**
- Variables de entorno opcionales:
  - `DB_HOST` (default: 172.25.0.36)
  - `DB_PORT` (default: 5551)
  - `DB_NAME` (default: frc_efact_dev)
  - `DB_USER` (default: postgres)
  - `OUTPUT_DIR` (default: ./deployment-data)

**Genera:**
- `deployment-data/data_production.sql` - Todos los datos
- `deployment-data/essential_data.sql` - Datos esenciales
- `deployment-data/sequences.sql` - Secuencias de IDs
- `deployment-data/data_report.txt` - Reporte de datos

### `import-prod-data.sh`
Importa datos a la base de datos de producción.

```bash
cd docs/deployment/scripts
./import-prod-data.sh <DATABASE_URL>
```

**Obtener DATABASE_URL:**
1. Ir a Render Dashboard → `frc-efact-db` → "Connections"
2. Copiar "Internal Database URL" o "External Database URL"

**Ejemplo:**
```bash
./import-prod-data.sh postgresql://user:pass@host:5432/dbname
```

**Nota:** Asegúrate de que Flyway haya aplicado las migraciones primero (el backend debe haber iniciado al menos una vez).

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

## Proceso de Migración de Datos

### Paso 1: Exportar Datos de Desarrollo
```bash
cd docs/deployment/scripts
./export-dev-data.sh
```

### Paso 2: Desplegar Servicios en Render
Seguir la guía en `../render/README.md` para crear los servicios.

### Paso 3: Esperar a que el Backend Inicie
El backend debe iniciar completamente para que Flyway aplique las migraciones.

### Paso 4: Importar Datos a Producción
```bash
cd docs/deployment/scripts
./import-prod-data.sh <DATABASE_URL_FROM_RENDER>
```

### Paso 5: Verificar Importación
El script mostrará un reporte de datos importados. Verificar que los números coincidan con los de desarrollo.

## Uso

Todos los scripts deben ejecutarse desde la raíz del proyecto o desde el directorio `docs/deployment/scripts/`.
