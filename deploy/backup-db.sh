#!/bin/bash
# Backup diario de PostgreSQL + certificados. Instalar en cron (como deploy):
#   crontab -e →  30 3 * * * /home/deploy/frc-efact/deploy/backup-db.sh >> /var/backups/frc-efact/backup.log 2>&1
# Retención local: 30 días. Copia off-site: completar la sección rsync/rclone.

set -euo pipefail
cd "$(dirname "$0")/.."

COMPOSE="docker compose -f docker-compose.prod.yml --env-file deploy/.env"
BACKUP_DIR="/var/backups/frc-efact"
STAMP="$(date +%Y%m%d_%H%M%S)"
RETENTION_DAYS=30

mkdir -p "$BACKUP_DIR"

echo "[$(date -Is)] Iniciando backup..."

# 1. Base de datos (formato custom, comprimido)
$COMPOSE exec -T postgres pg_dump -U frc_efact_user -d frc_efact_db -Fc \
    > "$BACKUP_DIR/db_$STAMP.dump"
echo "  DB: db_$STAMP.dump ($(du -h "$BACKUP_DIR/db_$STAMP.dump" | cut -f1))"

# 2. Certificados .pfx (documentos fiscales dependen de ellos)
docker run --rm -v frc-efact_certificates:/certs:ro -v "$BACKUP_DIR":/backup alpine \
    tar czf "/backup/certificates_$STAMP.tar.gz" -C /certs .
echo "  Certificados: certificates_$STAMP.tar.gz"

# 3. Rotación local
find "$BACKUP_DIR" -name '*.dump' -mtime +$RETENTION_DAYS -delete
find "$BACKUP_DIR" -name 'certificates_*.tar.gz' -mtime +$RETENTION_DAYS -delete

# 4. Copia off-site (OBLIGATORIO para documentos fiscales — completar una opción):
# rsync -az --delete "$BACKUP_DIR/" u000000@u000000.your-storagebox.de:frc-efact-backups/
# rclone sync "$BACKUP_DIR" b2:frc-efact-backups

echo "[$(date -Is)] Backup completo."
