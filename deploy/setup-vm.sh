#!/bin/bash
# Setup FRC eFact en la VM Hetzner COMPARTIDA (Fedora 42, deploy@178.105.107.171).
#
# ⚠️ Esta VM ya corre otros servicios productivos (farmacia Next.js, headscale,
# mediamtx, nginx, postgres nativo, firewalld, fail2ban). Este script NO toca
# firewall, ni SSH, ni nginx, ni postgres del host. Solo:
#   1. Instala Docker CE + compose plugin (repo oficial, Fedora)
#   2. Agrega el usuario actual al grupo docker
#   3. Crea el directorio de backups
#
# Idempotente. Correr como deploy (usa sudo puntualmente):
#   ./deploy/setup-vm.sh

set -euo pipefail

echo "=== FRC eFact — Setup VM (Fedora, VM compartida) ==="

# 0. Sanity: no correr en una VM equivocada
if ! grep -qi fedora /etc/os-release; then
    echo "❌ Este script es para la VM Fedora. Abortando."
    exit 1
fi

# 1. Docker CE + compose plugin
if ! command -v docker &>/dev/null; then
    echo "--- Instalando Docker CE ---"
    if command -v dnf5 &>/dev/null || dnf --version 2>/dev/null | grep -q dnf5; then
        sudo dnf config-manager addrepo --overwrite \
            --from-repofile=https://download.docker.com/linux/fedora/docker-ce.repo
    else
        sudo dnf -y install dnf-plugins-core
        sudo dnf config-manager --add-repo https://download.docker.com/linux/fedora/docker-ce.repo
    fi
    sudo dnf -y install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
    sudo systemctl enable --now docker
fi
echo "✅ $(docker --version 2>/dev/null || sudo docker --version)"

# 2. Grupo docker para el usuario actual
if ! id -nG "$USER" | grep -qw docker; then
    sudo usermod -aG docker "$USER"
    echo "✅ $USER agregado al grupo docker — cerrar sesión SSH y volver a entrar"
else
    echo "✅ $USER ya está en el grupo docker"
fi

# 3. Directorio de backups (escribible por deploy)
sudo mkdir -p /var/backups/frc-efact
sudo chown "$USER":"$USER" /var/backups/frc-efact
echo "✅ /var/backups/frc-efact"

# 4. Verificaciones (informativas, no mutan nada)
echo ""
echo "=== Estado del host (verificar, no tocar) ==="
echo "- firewalld: $(sudo firewall-cmd --state 2>/dev/null || echo '?') — puertos 80/443 ya los sirve el nginx del host"
echo "- SELinux httpd_can_network_connect: $(getsebool httpd_can_network_connect 2>/dev/null || echo '?') (debe ser on para que nginx proxee al stack)"
echo "- Puertos del stack: 8081 (backend) y 8082 (frontend) solo en 127.0.0.1"
echo ""
echo "=== Próximos pasos ==="
echo "1. cp deploy/.env.example deploy/.env && editar deploy/.env && chmod 600 deploy/.env"
echo "2. docker compose -f docker-compose.prod.yml --env-file deploy/.env up -d --build"
echo "3. sudo cp deploy/nginx-vhost-efact.conf /etc/nginx/conf.d/frc-efact.conf"
echo "   sudo nginx -t && sudo systemctl reload nginx"
echo "   sudo certbot --nginx -d efact.frc-ecommerce.com"
echo "Ver docs/deployment/hetzner/RUNBOOK_VM.md"
