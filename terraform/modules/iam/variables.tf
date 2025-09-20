# terraform/modules/iam/variables.tf

variable "environment" {
  description = "O ambiente da implantação (ex: dev, prod)."
  type        = string
}

variable "jwt_secret_arn" {
  description = "O ARN do segredo do Secrets Manager que contém a chave JWT."
  type        = string
}

variable "user_table_arn" {
  description = "O ARN da tabela de usuários do DynamoDB."
  type        = string
}

variable "campeonato_table_arn" {
  description = "O ARN da tabela de campeonatos do DynamoDB."
  type        = string
}
