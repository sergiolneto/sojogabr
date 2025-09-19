# terraform/modules/load_balancer/variables.tf

variable "project_name" {
  description = "O nome do projeto para usar nas tags e nomes de recursos."
  type        = string
}

variable "environment" {
  description = "O ambiente da implantação (ex: dev, prod)."
  type        = string
}

variable "vpc_id" {
  description = "O ID da VPC onde o Load Balancer será criado."
  type        = string
}

variable "subnet_ids" {
  description = "A lista de IDs das sub-redes para o Load Balancer."
  type        = list(string)
}
