# terraform/modules/network/variables.tf

variable "environment" {
  description = "O ambiente da implantação (ex: dev, prod)."
  type        = string
}

variable "project_name" {
  description = "O nome do projeto para usar nas tags."
  type        = string
}
