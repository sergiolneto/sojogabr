# C:/Local/Workplace/sojoga/sojogabr/terraform/variables.tf

variable "domain_name" {
  description = "O nome de domínio para a aplicação (ex: sojogabr.eu.org)"
  type        = string
}

variable "environment" {
  description = "O ambiente de deploy (ex: dev, prod)."
  type        = string
}

variable "project_name" {
  description = "O nome do projeto, usado para tagueamento de recursos."
  type        = string
  default     = "sojoga"
}

variable "jwt_secret_arn" {
  description = "ARN do segredo no AWS Secrets Manager para a chave JWT."
  type        = string
  sensitive   = true
}