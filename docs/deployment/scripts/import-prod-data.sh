#!/bin/bash

# Script para importar datos a producción
# Uso: ./import-prod-data.sh <DATABASE_URL>

set -e

if [ -z "$1" ]; then
    echo "❌ Error: Se requiere DATABASE_URL"
    echo ""
    echo "Uso: ./import-prod-data.sh <DATABASE_URL>"
    echo ""
    echo "Ejemplo:"
    echo "  ./import-prod-data.sh postgresql://user:pass@host:5432/dbname"
    echo ""
    echo "Obtener DATABASE_URL desde Render Dashboard:"
    echo "  - Ir a frc-efact-db → Connections"
    echo "  - Copiar 'Internal Database URL' o 'External Database URL'"
    exit 1
fi

DATABASE_URL="$1"
INPUT_DIR="${INPUT_DIR:-./deployment-data}"

echo "=========================================="
echo "FRC eFact - Importar Datos a Producción"
echo "=========================================="
echo ""

# Verificar que psql esté instalado
if ! command -v psql &> /dev/null; then
    echo "❌ Error: psql no está instalado"
    echo "Instalar PostgreSQL client tools"
    exit 1
fi

echo "✅ psql encontrado: $(psql --version)"
echo ""

# Verificar que existan los archivos
if [ ! -f "$INPUT_DIR/data_production.sql" ] && [ ! -f "$INPUT_DIR/essential_data.sql" ]; then
    echo "❌ Error: No se encontraron archivos de datos"
    echo "Ejecutar primero: ./export-dev-data.sh"
    exit 1
fi

echo "⚠️  ADVERTENCIA: Este script importará datos a la base de datos de producción"
echo "   Asegúrate de haber hecho un backup antes de continuar"
echo ""
read -p "¿Deseas continuar? (yes/no): " -r
echo ""

if [[ ! $REPLY =~ ^[Yy][Ee][Ss]$ ]]; then
    echo "Importación cancelada"
    exit 0
fi

# Verificar conexión
echo "Verificando conexión a la base de datos de producción..."
if ! psql "$DATABASE_URL" -c "SELECT 1;" &> /dev/null; then
    echo "❌ Error: No se puede conectar a la base de datos"
    echo "Verifica que DATABASE_URL sea correcto"
    exit 1
fi
echo "✅ Conexión exitosa"
echo ""

# Verificar que Flyway haya aplicado las migraciones
echo "Verificando que las migraciones de Flyway estén aplicadas..."
MIGRATION_COUNT=$(psql "$DATABASE_URL" -t -c "SELECT COUNT(*) FROM flyway_schema_history;" 2>/dev/null || echo "0")
if [ "$MIGRATION_COUNT" -eq "0" ]; then
    echo "⚠️  Advertencia: No se encontraron migraciones de Flyway"
    echo "   Asegúrate de que el backend haya iniciado al menos una vez"
    read -p "¿Deseas continuar de todas formas? (yes/no): " -r
    echo ""
    if [[ ! $REPLY =~ ^[Yy][Ee][Ss]$ ]]; then
        echo "Importación cancelada"
        exit 0
    fi
else
    echo "✅ Migraciones encontradas: $MIGRATION_COUNT"
fi
echo ""

# Preguntar qué datos importar
echo "¿Qué datos deseas importar?"
echo "  1) Todos los datos (data_production.sql)"
echo "  2) Solo datos esenciales (essential_data.sql)"
read -p "Selecciona una opción (1 o 2): " -r
echo ""

if [ "$REPLY" = "1" ]; then
    DATA_FILE="$INPUT_DIR/data_production.sql"
    if [ ! -f "$DATA_FILE" ]; then
        echo "❌ Error: No se encontró $DATA_FILE"
        exit 1
    fi
elif [ "$REPLY" = "2" ]; then
    DATA_FILE="$INPUT_DIR/essential_data.sql"
    if [ ! -f "$DATA_FILE" ]; then
        echo "❌ Error: No se encontró $DATA_FILE"
        exit 1
    fi
else
    echo "❌ Opción inválida"
    exit 1
fi

echo "Importando datos desde: $DATA_FILE"
echo "Esto puede tardar varios minutos..."
echo ""

# Importar datos
if psql "$DATABASE_URL" -f "$DATA_FILE"; then
    echo "✅ Datos importados correctamente"
else
    echo "❌ Error al importar datos"
    echo "Revisa los errores arriba"
    exit 1
fi
echo ""

# Importar secuencias si existe
if [ -f "$INPUT_DIR/sequences.sql" ]; then
    echo "Restaurando secuencias..."
    if psql "$DATABASE_URL" -f "$INPUT_DIR/sequences.sql"; then
        echo "✅ Secuencias restauradas"
    else
        echo "⚠️  Advertencia: Error al restaurar secuencias (puede ser normal)"
    fi
    echo ""
fi

# Verificar importación
echo "Verificando datos importados..."
psql "$DATABASE_URL" -c "
SELECT 
    'usuario' as tabla, COUNT(*) as registros FROM usuario
UNION ALL
SELECT 'empresa', COUNT(*) FROM empresa
UNION ALL
SELECT 'producto', COUNT(*) FROM producto
UNION ALL
SELECT 'cliente', COUNT(*) FROM cliente
UNION ALL
SELECT 'timbrado', COUNT(*) FROM timbrado
UNION ALL
SELECT 'timbrado_detalle', COUNT(*) FROM timbrado_detalle
UNION ALL
SELECT 'factura_legal', COUNT(*) FROM factura_legal
UNION ALL
SELECT 'nota_remision', COUNT(*) FROM nota_remision
ORDER BY tabla;
"

echo ""
echo "=========================================="
echo "✅ Importación completada!"
echo "=========================================="
echo ""
echo "Próximos pasos:"
echo "  1. Verificar que los datos se importaron correctamente"
echo "  2. Probar login con usuarios existentes"
echo "  3. Verificar que las empresas y datos relacionados funcionen"
echo ""
