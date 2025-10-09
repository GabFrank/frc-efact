#!/bin/bash

# Script OPCIONAL para configurar PostgreSQL con usuario dedicado
# 
# NOTA: Este script NO es necesario para desarrollo básico.
# El backend crea automáticamente la base de datos si no existe (perfil dev).
# 
# Este script es útil si deseas:
# - Crear un usuario PostgreSQL dedicado (en lugar de usar 'postgres')
# - Configurar permisos específicos
# - Generar un archivo de configuración personalizado
#
# Uso: ./setup-local-db.sh

set -e

echo "=========================================="
echo "FRC eFact - Setup Opcional de PostgreSQL"
echo "=========================================="
echo ""
echo "⚠️  NOTA: Este script es OPCIONAL"
echo "   El backend crea la BD automáticamente en modo dev"
echo ""

# Configuración por defecto
DB_NAME="${DB_NAME:-frc_efact_dev}"
DB_USER="${DB_USER:-frc_efact}"
DB_PASSWORD="${DB_PASSWORD:-frc_efact_password}"
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5551}"

echo "Configuración:"
echo "  Base de datos: $DB_NAME"
echo "  Usuario: $DB_USER"
echo "  Host: $DB_HOST"
echo "  Puerto: $DB_PORT"
echo ""

read -p "¿Deseas continuar con el setup? (y/N): " -n 1 -r
echo ""
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Setup cancelado."
    echo ""
    echo "Para desarrollo rápido, simplemente ejecuta:"
    echo "  ./mvnw spring-boot:run"
    echo ""
    echo "El backend creará la base de datos automáticamente."
    exit 0
fi

# Verificar si PostgreSQL está instalado
if ! command -v psql &> /dev/null; then
    echo "❌ Error: PostgreSQL no está instalado"
    echo ""
    echo "Instalar PostgreSQL:"
    echo "  macOS: brew install postgresql@15"
    echo "  Ubuntu: sudo apt-get install postgresql-15"
    echo "  Windows: Descargar desde https://www.postgresql.org/download/"
    exit 1
fi

echo "✅ PostgreSQL encontrado: $(psql --version)"
echo ""

# Verificar si el servicio PostgreSQL está corriendo
if ! pg_isready -h $DB_HOST -p $DB_PORT &> /dev/null; then
    echo "❌ Error: PostgreSQL no está corriendo en $DB_HOST:$DB_PORT"
    echo ""
    echo "Iniciar PostgreSQL:"
    echo "  macOS: brew services start postgresql@15"
    echo "  Ubuntu: sudo systemctl start postgresql"
    echo "  Windows: Iniciar desde Services"
    exit 1
fi

echo "✅ PostgreSQL está corriendo"
echo ""

# Crear usuario si no existe
echo "Creando usuario de base de datos..."
psql -h $DB_HOST -p $DB_PORT -U postgres -tc "SELECT 1 FROM pg_user WHERE usename = '$DB_USER'" | grep -q 1 || \
    psql -h $DB_HOST -p $DB_PORT -U postgres -c "CREATE USER $DB_USER WITH PASSWORD '$DB_PASSWORD' CREATEDB;"

echo "✅ Usuario '$DB_USER' creado/verificado"
echo ""

# Crear archivo de configuración local
CONFIG_FILE="src/main/resources/application-local.yml"
echo "Creando archivo de configuración local..."

cat > $CONFIG_FILE << EOF
# Configuración local para desarrollo
# Este archivo NO debe ser versionado en Git

spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USER}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
  
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true
  
  flyway:
    enabled: true
    baseline-on-migrate: true

# JWT Configuration
jwt:
  secret: local-development-secret-key-change-in-production-min-256-bits
  expiration-ms: 86400000  # 24 horas

# Logging
logging:
  level:
    com.frcefact: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
EOF

echo "✅ Archivo de configuración creado: $CONFIG_FILE"
echo ""

# Agregar al .gitignore si no está
if ! grep -q "application-local.yml" .gitignore 2>/dev/null; then
    echo "application-local.yml" >> .gitignore
    echo "✅ Agregado application-local.yml a .gitignore"
    echo ""
fi

echo ""
echo "=========================================="
echo "✅ Configuración completada!"
echo "=========================================="
echo ""
echo "Próximos pasos:"
echo "  1. Ejecutar: ./mvnw spring-boot:run -Dspring-boot.run.profiles=local"
echo "  2. La base de datos se creará automáticamente si no existe"
echo "  3. Flyway aplicará las migraciones automáticamente"
echo "  4. Acceder a Swagger UI: http://localhost:8080/swagger-ui.html"
echo ""
echo "Credenciales de base de datos:"
echo "  Host: $DB_HOST:$DB_PORT"
echo "  Database: $DB_NAME (se crea automáticamente)"
echo "  Username: $DB_USER"
echo "  Password: $DB_PASSWORD"
echo ""
echo "Usuario de prueba (creado por migraciones):"
echo "  Username: admin"
echo "  Password: Admin123!"
echo ""
