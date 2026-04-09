output "public_ip" {
  description = "Instance public IP"
  value       = oci_core_instance.onebite.public_ip
}

output "ssh_command" {
  description = "SSH command to connect"
  value       = "ssh opc@${oci_core_instance.onebite.public_ip}"
}

output "api_url" {
  description = "API base URL"
  value       = "http://${oci_core_instance.onebite.public_ip}:8080"
}

output "instance_ocid" {
  description = "Compute Instance OCID"
  value       = oci_core_instance.onebite.id
}

output "vcn_ocid" {
  description = "VCN OCID"
  value       = oci_core_vcn.onebite.id
}
