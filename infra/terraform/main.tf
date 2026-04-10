terraform {
  required_version = ">= 1.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    tls = {
      source  = "hashicorp/tls"
      version = "~> 4.0"
    }
  }
}

provider "aws" {
  profile = var.aws_profile
  region  = var.aws_region
}

# --- Default VPC ---
data "aws_vpc" "default" {
  default = true
}

data "aws_subnets" "default" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }
}

# --- Amazon Linux 2023 AMI ---
data "aws_ami" "al2023" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-*-kernel-*-arm64"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

# --- Security Group ---
resource "aws_security_group" "onebite" {
  name        = "${var.project_name}-sg"
  description = "Security group for One Bite server"
  vpc_id      = data.aws_vpc.default.id

  # SSH (restricted to my IP)
  ingress {
    description = "SSH"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = [var.my_ip]
  }

  # API (8080) - open for mobile app
  ingress {
    description = "Spring Boot API"
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # HTTP (for future reverse proxy)
  ingress {
    description = "HTTP"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # HTTPS (for future reverse proxy)
  ingress {
    description = "HTTPS"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    description = "All outbound"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name    = "${var.project_name}-sg"
    Project = var.project_name
  }
}

# --- SSH Key Pair ---
resource "tls_private_key" "onebite" {
  algorithm = "ED25519"
}

resource "aws_key_pair" "onebite" {
  key_name   = "${var.project_name}-key"
  public_key = tls_private_key.onebite.public_key_openssh

  tags = {
    Name    = "${var.project_name}-key"
    Project = var.project_name
  }
}

# --- EC2 Instance ---
resource "aws_instance" "onebite" {
  ami                    = data.aws_ami.al2023.id
  instance_type          = var.instance_type
  key_name               = aws_key_pair.onebite.key_name
  vpc_security_group_ids = [aws_security_group.onebite.id]
  subnet_id              = data.aws_subnets.default.ids[0]

  user_data = file("${path.module}/user-data.sh")

  root_block_device {
    volume_size = 30
    volume_type = "gp3"
  }

  tags = {
    Name    = "${var.project_name}-server"
    Project = var.project_name
  }
}

# --- Elastic IP ---
resource "aws_eip" "onebite" {
  instance = aws_instance.onebite.id

  tags = {
    Name    = "${var.project_name}-eip"
    Project = var.project_name
  }
}
