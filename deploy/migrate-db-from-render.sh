#!/bin/bash
# Migración inicial de datos: PostgreSQL de Render → contenedor postgres local.
# Correr EN LA VM, desde la raíz del repo, con el stack levantado (al menos postgres)
# y RENDER_DATABASE_URL definida en deploy/.env.
#
# Uso:
#   ./deploy/migrate-db-from-render.sh            # dump + restore + verificación
#   ./deploy/migrate-db-from-render.sh --dump-only  # solo dump (ensayo)

set -euo pipefail
cd "$(dirname "$0")/.."

ENV_FILE="deploy/.env"
COMPOSE="docker compose -f docker-compose.prod.yml --env-file $ENV_FILE"
BACKUP_DIR="/var/backups/frc-efact"
STAMP="$(date +%Y%m%d_%H%M%S)"
DUMP_FILE="$BACKUP_DIR/render_migracion_$STAMP.dump"

# shellcheck disable=SC1090
source <(grep -E '^(RENDER_DATABASE_URL)=' "$ENV_FILE")

if [ -z "${RENDER_DATABASE_URL:-}" ]; then
    echo "❌ Definir RENDER_DATABASE_URL en $ENV_FILE (External Database URL de Render)"
    exit 1
fi

mkdir -p "$BACKUP_DIR"

echo "=== 1/4 Dump desde Render ==="
# ⚠️ Usar pg_dump/pg_restore DEL CONTENEDOR (v16, igual al server): el del host
# (Fedora, v17) genera formato de archivo 1.16 que el pg_restore 16 del
# contenedor rechaza con "unsupported version (1.16) in file header".
# (</dev/null: sin eso, compose exec -T consume el stdin del script y el
# 'read' de confirmación de abajo recibe EOF)
$COMPOSE exec -T postgres pg_dump "$RENDER_DATABASE_URL" -Fc --no-owner --no-privileges > "$DUMP_FILE" < /dev/null
echo "✅ Dump: $DUMP_FILE ($(du -h "$DUMP_FILE" | cut -f1))"
TABLAS=$($COMPOSE exec -T postgres pg_restore --list < "$DUMP_FILE" | grep -c 'TABLE DATA' || true)
echo "   Tablas con datos: $TABLAS"
if [ "${TABLAS:-0}" -eq 0 ]; then
    echo "❌ El dump no contiene datos de tablas — no seguir."
    exit 1
fi

if [ "${1:-}" = "--dump-only" ]; then
    echo "Modo --dump-only: no se restaura nada."
    exit 0
fi

echo ""
echo "=== 2/4 Restore en el postgres local ==="
echo "⚠️  Esto reemplaza el contenido actual de frc_efact_db en la VM."
read -rp "Escribí 'MIGRAR' para continuar: " CONFIRM
[ "$CONFIRM" = "MIGRAR" ] || { echo "Cancelado."; exit 1; }

$COMPOSE exec -T postgres pg_restore -U frc_efact_user -d frc_efact_db \
    --no-owner --no-privileges --clean --if-exists < "$DUMP_FILE" \
    || echo "⚠️  pg_restore terminó con avisos (normal con --clean en DB nueva); verificar counts abajo."

# Verificación dura: si empresa quedó vacía, el restore NO aplicó (los avisos taparon un error real)
EMPRESAS=$($COMPOSE exec -T postgres psql -U frc_efact_user -d frc_efact_db -tAc "SELECT count(*) FROM empresa.empresa" | tr -d '[:space:]')
if [ "${EMPRESAS:-0}" -eq 0 ]; then
    echo "❌ Restore NO aplicó datos (empresa.empresa vacía). Abortando."
    exit 1
fi

echo ""
echo "=== 3/4 Verificación de contenido ==="
$COMPOSE exec -T postgres psql -U frc_efact_user -d frc_efact_db -c "
SELECT n.nspname AS esquema, c.relname AS tabla, c.reltuples::bigint AS filas_aprox
FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace
WHERE c.relkind = 'r'
  AND n.nspname IN ('persona','empresa','financiero','productos','clientes','auditoria','catalogo','public')
ORDER BY 1, 2;"

echo ""
echo "=== 4/4 Recordatorios ==="
echo " - Comparar los counts contra Render antes del cutover."
echo " - Verificar secuencias de autonumeración si hubo avisos en el restore."
echo " - Copiar los .pfx al volumen: docker compose -f docker-compose.prod.yml cp <archivo>.pfx backend:/app/certificates/"
echo " - Levantar el backend y revisar que Flyway VALIDE (no re-migre): docker compose -f docker-compose.prod.yml logs backend | grep -i flyway"
