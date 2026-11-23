output "mtls_url" {
  description = "URL pública protegida por mTLS."
  value       = "https://${var.domain_name}/hello"
}

output "secret_name" {
  value = aws_secretsmanager_secret.api_key.name
}

output "read_secret_policy_arn" {
  value = var.create_read_policy ? aws_iam_policy.read_secret[0].arn : null
}

output "put_secret_command" {
  description = "Grava a API key (valor fictício) no secret. Troque o valor e rode de novo para a demo de rotação."
  value       = "aws secretsmanager put-secret-value --region ${var.region} --secret-id ${var.secret_name} --secret-string sk-demo-123456"
}

output "test_command" {
  value = "curl --cert client.pem --key client.key https://${var.domain_name}/hello"
}
