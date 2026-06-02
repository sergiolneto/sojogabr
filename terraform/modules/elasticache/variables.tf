# terraform/modules/elasticache/variables.tf

variable "environment" {
  type = string
}

variable "project_name" {
  type = string
}

variable "vpc_id" {
  type = string
}

variable "subnet_ids" {
  type = list(string)
}

variable "ecs_tasks_security_group_id" {
  type = string
}
