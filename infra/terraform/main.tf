terraform {
  required_version = ">= 1.0"

  required_providers {
    oci = {
      source  = "oracle/oci"
      version = "~> 6.0"
    }
  }
}

provider "oci" {
  tenancy_ocid     = var.tenancy_ocid
  user_ocid        = var.user_ocid
  fingerprint      = var.api_fingerprint
  private_key_path = var.api_private_key_path
  region           = var.region
}

# --- Availability Domains ---
data "oci_identity_availability_domains" "ads" {
  compartment_id = var.compartment_ocid
}

# --- VCN ---
resource "oci_core_vcn" "onebite" {
  compartment_id = var.compartment_ocid
  display_name   = "${var.project_name}-vcn"
  cidr_blocks    = ["10.0.0.0/16"]
  dns_label      = "onebite"

  freeform_tags = {
    Project = var.project_name
  }
}

# --- Internet Gateway ---
resource "oci_core_internet_gateway" "onebite" {
  compartment_id = var.compartment_ocid
  vcn_id         = oci_core_vcn.onebite.id
  display_name   = "${var.project_name}-igw"
  enabled        = true

  freeform_tags = {
    Project = var.project_name
  }
}

# --- Route Table ---
resource "oci_core_route_table" "onebite" {
  compartment_id = var.compartment_ocid
  vcn_id         = oci_core_vcn.onebite.id
  display_name   = "${var.project_name}-rt"

  route_rules {
    destination       = "0.0.0.0/0"
    network_entity_id = oci_core_internet_gateway.onebite.id
  }

  freeform_tags = {
    Project = var.project_name
  }
}

# --- Security List ---
resource "oci_core_security_list" "onebite" {
  compartment_id = var.compartment_ocid
  vcn_id         = oci_core_vcn.onebite.id
  display_name   = "${var.project_name}-sl"

  # Egress: allow all outbound
  egress_security_rules {
    destination = "0.0.0.0/0"
    protocol    = "all"
    description = "All outbound"
  }

  # SSH (restricted to my IP)
  ingress_security_rules {
    source      = var.my_ip
    protocol    = "6" # TCP
    description = "SSH"

    tcp_options {
      min = 22
      max = 22
    }
  }

  # HTTP
  ingress_security_rules {
    source      = "0.0.0.0/0"
    protocol    = "6"
    description = "HTTP"

    tcp_options {
      min = 80
      max = 80
    }
  }

  # HTTPS
  ingress_security_rules {
    source      = "0.0.0.0/0"
    protocol    = "6"
    description = "HTTPS"

    tcp_options {
      min = 443
      max = 443
    }
  }

  # Spring Boot API
  ingress_security_rules {
    source      = "0.0.0.0/0"
    protocol    = "6"
    description = "Spring Boot API"

    tcp_options {
      min = 8080
      max = 8080
    }
  }

  freeform_tags = {
    Project = var.project_name
  }
}

# --- Public Subnet ---
resource "oci_core_subnet" "onebite" {
  compartment_id             = var.compartment_ocid
  vcn_id                     = oci_core_vcn.onebite.id
  display_name               = "${var.project_name}-subnet"
  cidr_block                 = "10.0.1.0/24"
  dns_label                  = "onebite"
  route_table_id             = oci_core_route_table.onebite.id
  security_list_ids          = [oci_core_security_list.onebite.id]
  prohibit_public_ip_on_vnic = false

  freeform_tags = {
    Project = var.project_name
  }
}

# --- Oracle Linux 9 ARM Image ---
data "oci_core_images" "ol9_arm" {
  compartment_id           = var.compartment_ocid
  operating_system         = "Oracle Linux"
  operating_system_version = "9"
  shape                    = "VM.Standard.A1.Flex"
  sort_by                  = "TIMECREATED"
  sort_order               = "DESC"

  filter {
    name   = "display_name"
    values = ["^Oracle-Linux-9\\.\\d+-aarch64-\\d{4}\\.\\d{2}\\.\\d{2}-\\d+$"]
    regex  = true
  }
}

# --- Compute Instance (Always Free ARM A1) ---
resource "oci_core_instance" "onebite" {
  compartment_id      = var.compartment_ocid
  availability_domain = data.oci_identity_availability_domains.ads.availability_domains[0].name
  display_name        = "${var.project_name}-server"
  shape               = "VM.Standard.A1.Flex"

  shape_config {
    ocpus         = var.instance_ocpus
    memory_in_gbs = var.instance_memory_gb
  }

  source_details {
    source_type             = "image"
    source_id               = data.oci_core_images.ol9_arm.images[0].id
    boot_volume_size_in_gbs = var.boot_volume_size_gb
  }

  create_vnic_details {
    subnet_id        = oci_core_subnet.onebite.id
    assign_public_ip = true
    display_name     = "${var.project_name}-vnic"
  }

  metadata = {
    ssh_authorized_keys = var.ssh_public_key
    user_data           = base64encode(file("${path.module}/user-data.sh"))
  }

  freeform_tags = {
    Project = var.project_name
  }
}
