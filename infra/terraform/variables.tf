variable "region" {
  description = "Região AWS. Deve ser a mesma de aws.region em secretsdemo.properties."
  type        = string
  default     = "us-east-1"
}

variable "hosted_zone_name" {
  description = "Hosted zone pública já existente no Route 53 (ex.: exemplo.com)."
  type        = string
}

variable "domain_name" {
  description = "FQDN do custom domain com mTLS (ex.: mtls-demo.exemplo.com). Deve estar dentro da hosted zone."
  type        = string
}

variable "truststore_pem_path" {
  description = "Caminho local do truststore.pem (= ca.pem), gerado na Parte A do guia."
  type        = string
}

variable "stage_name" {
  description = "Nome do stage do API Gateway."
  type        = string
  default     = "demo"
}

variable "secret_name" {
  description = "Nome do secret da API key. Deve bater com aws.secrets.api-key-id em secretsdemo.properties."
  type        = string
  default     = "hands-on/api-key"
}

variable "create_read_policy" {
  description = "Cria a política IAM de leitura do secret. Use false se o seu usuário não tiver iam:CreatePolicy."
  type        = bool
  default     = true
}

variable "attach_policy_to_user" {
  description = "Opcional: usuário IAM que receberá a política de leitura do secret. Vazio = só cria a política."
  type        = string
  default     = ""
}
