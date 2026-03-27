#!/bin/bash
set -euxo pipefail

# --- Docker ---
dnf update -y
dnf install -y docker git
systemctl enable docker
systemctl start docker

# Docker Compose plugin
mkdir -p /usr/local/lib/docker/cli-plugins
curl -SL "https://github.com/docker/compose/releases/latest/download/docker-compose-linux-$(uname -m)" \
  -o /usr/local/lib/docker/cli-plugins/docker-compose
chmod +x /usr/local/lib/docker/cli-plugins/docker-compose

# Add ec2-user to docker group
usermod -aG docker ec2-user

# --- App directory ---
mkdir -p /opt/onebite
chown ec2-user:ec2-user /opt/onebite

echo "=== Setup complete ==="
