# C:/Local/Workplace/sojoga/sojogabr/terraform/variables.tf

variable "domain_names" {
  description = "Uma lista dos nomes de domínio para a aplicação (ex: [\"sojoga.com\", \"www.sojoga.com\"])"
  type        = list(string)
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