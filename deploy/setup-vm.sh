#!/bin/bash
# Bootstrap de la VM Hetzner (Ubuntu 22.04/24.04) para FRC eFact.
# Idempotente: se puede re-ejecutar. Correr como root o con sudo.
# Uso: ./deploy/setup-vm.sh

set -euo pipefail

echo "=== FRC eFact — Setup VM Hetzner ==="

# 1. Paquetes base
export DEBIAN_FRONTEND=noninteractive
apt-get update -q
apt-get install -yq ca-certificates curl gnupg ufw fail2ban unattended-upgrades postgresql-client

# 2. Docker (repo oficial) + compose plugin
if ! command -v docker &>/dev/null; then
    install -m 0755 -d /etc/apt/keyrings
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
    chmod a+r /etc/apt/keyrings/docker.asc
    echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] \
https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" \
        > /etc/apt/sources.list.d/docker.list
    apt-get update -q
    apt-get install -yq docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
fi
echo "✅ $(docker --version) / $(docker compose version --short)"

# 3. Firewall: solo SSH, HTTP y HTTPS
ufw default deny incoming
ufw default allow outgoing
ufw allow 22/tcp
ufw allow 80/tcp
ufw allow 443/tcp
ufw --force enable
echo "✅ ufw activo (22, 80, 443)"

# 4. fail2ban para SSH
systemctl enable --now fail2ban
echo "✅ fail2ban activo"

# 5. Actualizaciones de seguridad automáticas
dpkg-reconfigure -f noninteractive unattended-upgrades || true
echo "✅ unattended-upgrades configurado"

# 6. Hardening SSH básico (solo clave pública)
if grep -qE '^#?PasswordAuthentication' /etc/ssh/sshd_config; then
    sed -i 's/^#\?PasswordAuthentication.*/PasswordAuthentication no/' /etc/ssh/sshd_config
    systemctl reload ssh || systemctl reload sshd || true
    echo "✅ SSH: PasswordAuthentication no"
fi

echo ""
echo "=== Setup base completo. Próximos pasos ==="
echo "1. cp deploy/.env.example deploy/.env && editar deploy/.env"
echo "2. docker compose -f docker-compose.prod.yml --env-file deploy/.env up -d --build"
echo "3. ./deploy/migrate-db-from-render.sh   (migración inicial de datos)"
echo "Ver docs/deployment/hetzner/RUNBOOK_VM.md"
