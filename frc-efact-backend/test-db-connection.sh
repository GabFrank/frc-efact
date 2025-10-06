#!/bin/bash

echo "=== Probando conexión a PostgreSQL en puerto 5551 ==="
echo ""

# Verificar si PostgreSQL está corriendo
echo "1. Verificando si PostgreSQL está activo..."
pg_isready -h localhost -p 5551
if [ $? -eq 0 ]; then
    echo "✓ PostgreSQL está corriendo"
else
    echo "✗ PostgreSQL no está corriendo en el puerto 5551"
    exit 1
fi

echo ""
echo "2. Verificando si la base de datos 'frc_efact_dev' existe..."
DB_EXISTS=$(psql -h localhost -p 5551 -U postgres -tAc "SELECT 1 FROM pg_database WHERE datname='frc_efact_dev'")

if [ "$DB_EXISTS" = "1" ]; then
    echo "✓ La base de datos 'frc_efact_dev' ya existe"
else
    echo "✗ La base de datos 'frc_efact_dev' no existe"
    echo ""
    echo "3. Creando la base de datos..."
    psql -h localhost -p 5551 -U postgres -c "CREATE DATABASE frc_efact_dev;"
    if [ $? -eq 0 ]; then
        echo "✓ Base de datos creada exitosamente"
    else
        echo "✗ Error al crear la base de datos"
        exit 1
    fi
fi

echo ""
echo "4. Probando conexión a la base de datos..."
psql -h localhost -p 5551 -U postgres -d frc_efact_dev -c "SELECT version();" > /dev/null 2>&1
if [ $? -eq 0 ]; then
    echo "✓ Conexión exitosa a frc_efact_dev"
else
    echo "✗ Error al conectar a frc_efact_dev"
    exit 1
fi

echo ""
echo "=== ✓ Todas las pruebas pasaron exitosamente ==="
echo ""
echo "Ahora puedes ejecutar Spring Boot con:"
echo "  frc-efact-backend/mvnw spring-boot:run -f frc-efact-backend/pom.xml"
