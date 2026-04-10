output "public_ip" {
  description = "Elastic IP of the EC2 instance"
  value       = aws_eip.onebite.public_ip
}

output "ssh_command" {
  description = "SSH command to connect"
  value       = "ssh -i onebite-key.pem ec2-user@${aws_eip.onebite.public_ip}"
}

output "api_url" {
  description = "API base URL"
  value       = "http://${aws_eip.onebite.public_ip}:8080"
}

output "instance_id" {
  description = "EC2 Instance ID"
  value       = aws_instance.onebite.id
}

output "private_key" {
  description = "SSH private key (save to file)"
  value       = tls_private_key.onebite.private_key_openssh
  sensitive   = true
}

output "github_actions_role_arn" {
  description = "IAM role ARN for GitHub Actions (OIDC-assumed)"
  value       = aws_iam_role.github_actions.arn
}
