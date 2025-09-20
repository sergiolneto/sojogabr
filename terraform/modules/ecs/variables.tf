# terraform/modules/ecs/variables.tf

variable "project_name" {
  description = "O nome do projeto."
  type        = string
}

variable "environment" {
  description = "O ambiente da implantação."
  type        = string
}

variable "vpc_id" {
  description = "O ID da VPC."
  type        = string
}

variable "subnet_ids" {
  description = "A lista de IDs das sub-redes."
  type        = list(string)
}

variable "ecs_task_execution_role_arn" {
  description = "O ARN da role de execução da tarefa ECS."
  type        = string
}

variable "ecs_task_role_arn" {
  description = "O ARN da role da tarefa ECS."
  type        = string
}

variable "user_table_name" {
  description = "O nome da tabela de usuários do DynamoDB."
  type        = string
}

variable "campeonato_table_name" {
  description = "O nome da tabela de campeonatos do DynamoDB."
  type        = string
}

variable "jwt_secret_arn" {
  description = "O ARN do segredo JWT."
  type        = string
}

variable "lb_security_group_id" {
  description = "O ID do Security Group do Load Balancer."
  type        = string
}

variable "backend_target_group_arn" {
  description = "O ARN do Target Group do backend."
  type        = string
}

variable "frontend_target_group_arn" {
  description = "O ARN do Target Group do frontend."
  type        = string
}
