# --- OCI Authentication ---
# OCI 콘솔 > Identity > Users > API Keys 에서 생성

variable "tenancy_ocid" {
  description = "OCI 테넌시 OCID (ocid1.tenancy.oc1..xxx)"
  type        = string
}

variable "user_ocid" {
  description = "OCI 유저 OCID (ocid1.user.oc1..xxx)"
  type        = string
}

variable "api_fingerprint" {
  description = "OCI API Key fingerprint (xx:xx:xx...형태)"
  type        = string
}

variable "api_private_key_path" {
  description = "OCI API Key 개인키 파일 경로 (예: ~/.oci/oci_api_key.pem)"
  type        = string
  default     = "~/.oci/oci_api_key.pem"
}

variable "compartment_ocid" {
  description = "리소스를 생성할 compartment OCID (root compartment = tenancy OCID)"
  type        = string
}

variable "region" {
  description = "OCI region (예: ap-chuncheon-1, ap-seoul-1, ap-osaka-1)"
  type        = string
  default     = "ap-chuncheon-1"
}

# --- Instance ---

variable "instance_ocpus" {
  description = "ARM A1 OCPU 수 (Always Free 최대 4, 권장 2)"
  type        = number
  default     = 2
}

variable "instance_memory_gb" {
  description = "메모리 GB (OCPU당 최대 6GB, Always Free 최대 24GB)"
  type        = number
  default     = 12
}

variable "boot_volume_size_gb" {
  description = "부트 볼륨 크기 GB (Always Free 최대 200GB)"
  type        = number
  default     = 50
}

# --- Network ---

variable "my_ip" {
  description = "SSH 접근용 내 IP (CIDR 형식, 예: 1.2.3.4/32)"
  type        = string
}

variable "ssh_public_key" {
  description = "SSH 공개키 내용 (예: ssh-ed25519 AAAA... user@host)"
  type        = string
}

# --- Project ---

variable "project_name" {
  description = "리소스 태깅용 프로젝트 이름"
  type        = string
  default     = "onebite"
}
