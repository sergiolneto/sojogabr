# terraform/modules/ecs/variables.tf

variable "environment" {
  description = "O ambiente de deploy (ex: dev, prod)."
  type        = string
}

variable "project_name" {
  description = "O nome do projeto, usado para tagueamento de recursos."
  type        = string
}

variable "vpc_id" {
  description = "O ID da VPC onde os recursos do ECS serão criados."
  type        = string
}

variable "public_subnet_ids" {
  description = "Lista de IDs das sub-redes públicas para o serviço ECS."
  type        = list(string)
}

variable "target_group_arn" {
  description = "O ARN do Target Group do Load Balancer para associar ao serviço."
  type        = string
}

variable "jwt_secret_arn" {
  description = "ARN do segredo no AWS Secrets Manager para a chave JWT."
  type        = string
  sensitive   = true
}

variable "lb_security_group_id" {
  description = "O ID do Security Group do Load Balancer."
  type        = string
}