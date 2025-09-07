# terraform/variables.tf

variable "environment" {
  description = "O ambiente de implantação (ex: hom, prod)."
  type        = string
}

variable "project_name" {
  description = "O nome do projeto."
  type        = string
  default     = "sojoga-br"
}