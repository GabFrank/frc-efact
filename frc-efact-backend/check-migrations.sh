#!/bin/bash

# Script para verificar que no haya migraciones duplicadas de Flyway
MIGRATION_DIR="src/main/resources/db/migration"

echo "Verificando migraciones de Flyway..."

# Extraer números de versión
versions=$(ls $MIGRATION_DIR/V*.sql 2>/dev/null | sed 's/.*\/V\([0-9]*\)__.*/\1/' | sort)

# Buscar duplicados
duplicates=$(echo "$versions" | uniq -d)

if [ -n "$duplicates" ]; then
    echo "❌ ERROR: Se encontraron versiones duplicadas:"
    for version in $duplicates; do
        echo "  Versión $version:"
        ls $MIGRATION_DIR/V${version}__*.sql | sed 's/^/    /'
    done
    exit 1
else
    echo "✅ No se encontraron duplicados"
    exit 0
fi
