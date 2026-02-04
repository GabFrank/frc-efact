#!/bin/bash

# Script para exportar datos de desarrollo a producción
# Uso: ./export-dev-data.sh

set -e

echo "=========================================="
echo "FRC eFact - Exportar Datos de Desarrollo"
echo "=========================================="
echo ""

# Configuración de base de datos de desarrollo
DB_HOST="${DB_HOST:-172.25.0.36}"
DB_PORT="${DB_PORT:-5551}"
DB_NAME="${DB_NAME:-frc_efact_dev}"
DB_USER="${DB_USER:-postgres}"

# Directorio de salida
OUTPUT_DIR="${OUTPUT_DIR:-./deployment-data}"
mkdir -p "$OUTPUT_DIR"

echo "Configuración:"
echo "  Host: $DB_HOST"
echo "  Puerto: $DB_PORT"
echo "  Base de datos: $DB_NAME"
echo "  Usuario: $DB_USER"
echo "  Directorio de salida: $OUTPUT_DIR"
echo ""

# Verificar que pg_dump esté instalado
if ! command -v pg_dump &> /dev/null; then
    echo "❌ Error: pg_dump no está instalado"
    echo "Instalar PostgreSQL client tools"
    exit 1
fi

echo "✅ pg_dump encontrado: $(pg_dump --version)"
echo ""

# Solicitar contraseña
read -sp "Contraseña de PostgreSQL: " DB_PASSWORD
echo ""
export PGPASSWORD="$DB_PASSWORD"

# Verificar conexión
echo "Verificando conexión a la base de datos..."
if ! psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "SELECT 1;" &> /dev/null; then
    echo "❌ Error: No se puede conectar a la base de datos"
    exit 1
fi
echo "✅ Conexión exitosa"
echo ""

# Exportar solo datos (sin estructura, ya que Flyway creará la estructura)
echo "Exportando datos de todas las tablas..."
pg_dump -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" \
  --data-only \
  --no-owner \
  --no-privileges \
  --file "$OUTPUT_DIR/data_production.sql"

echo "✅ Datos exportados a: $OUTPUT_DIR/data_production.sql"
echo ""

# Exportar datos esenciales (tablas principales)
echo "Exportando datos esenciales..."
pg_dump -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" \
  --data-only \
  --no-owner \
  --no-privileges \
  --table=usuario \
  --table=rol \
  --table=usuario_rol \
  --table=empresa \
  --table=usuario_empresa \
  --table=producto \
  --table=cliente \
  --table=timbrado \
  --table=timbrado_detalle \
  --table=factura_legal \
  --table=factura_legal_item \
  --table=nota_credito \
  --table=nota_debito \
  --table=nota_remision \
  --table=nota_remision_item \
  --table=vehiculo \
  --table=chofer \
  --table=geografia_pais \
  --table=geografia_departamento \
  --table=geografia_distrito \
  --table=geografia_ciudad \
  --table=geografia_barrio \
  --file "$OUTPUT_DIR/essential_data.sql"

echo "✅ Datos esenciales exportados a: $OUTPUT_DIR/essential_data.sql"
echo ""

# Exportar secuencias (importante para mantener los IDs correctos)
echo "Exportando secuencias..."
psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -c "
SELECT 'SELECT setval(''' || sequence_name || ''', ' || last_value || ', true);'
FROM information_schema.sequences
WHERE sequence_schema = 'public';
" > "$OUTPUT_DIR/sequences.sql"

echo "✅ Secuencias exportadas a: $OUTPUT_DIR/sequences.sql"
echo ""

# Generar reporte de datos
echo "Generando reporte de datos..."
psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "
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
UNION ALL
SELECT 'vehiculo', COUNT(*) FROM vehiculo
UNION ALL
SELECT 'chofer', COUNT(*) FROM chofer
ORDER BY tabla;
" > "$OUTPUT_DIR/data_report.txt"

echo "✅ Reporte generado en: $OUTPUT_DIR/data_report.txt"
echo ""

# Limpiar contraseña
unset PGPASSWORD

echo "=========================================="
echo "✅ Exportación completada!"
echo "=========================================="
echo ""
echo "Archivos generados:"
echo "  - $OUTPUT_DIR/data_production.sql (todos los datos)"
echo "  - $OUTPUT_DIR/essential_data.sql (datos esenciales)"
echo "  - $OUTPUT_DIR/sequences.sql (secuencias)"
echo "  - $OUTPUT_DIR/data_report.txt (reporte de datos)"
echo ""
echo "Próximos pasos:"
echo "  1. Revisar los archivos exportados"
echo "  2. Importar a producción usando: ./import-prod-data.sh"
echo ""
