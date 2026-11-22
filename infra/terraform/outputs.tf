output "mtls_url" {
  description = "URL pública protegida por mTLS."
  value       = "https://${var.domain_name}/hello"
}

output "secret_name" {
  value = aws_secretsmanager_secret.rsa_private_key.name
}

output "read_secret_policy_arn" {
  value = aws_iam_policy.read_secret.arn
}

output "put_secret_command" {
  description = "Grava a chave privada no secret (rode fora do repositório)."
  value       = "aws secretsmanager put-secret-value --region ${var.region} --secret-id ${var.secret_name} --secret-string file://app-privada.pem"
}

output "test_command" {
  value = "curl --cert client.pem --key client.key https://${var.domain_name}/hello"
}
