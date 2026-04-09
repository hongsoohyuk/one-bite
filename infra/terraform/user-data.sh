#!/bin/bash
set -euxo pipefail

# --- Docker (Oracle Linux 9 / dnf) ---
dnf update -y
dnf install -y dnf-utils git
dnf config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
dnf install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin

systemctl enable docker
systemctl start docker

# Add opc user to docker group (OCI default user = opc)
usermod -aG docker opc

# --- App directory ---
mkdir -p /opt/onebite
chown opc:opc /opt/onebite

# --- Firewall (iptables) ---
# OCI Security List handles network rules, but OS firewall also needs to allow traffic
firewall-cmd --permanent --add-port=80/tcp
firewall-cmd --permanent --add-port=443/tcp
firewall-cmd --permanent --add-port=8080/tcp
firewall-cmd --reload

echo "=== Setup complete ==="
