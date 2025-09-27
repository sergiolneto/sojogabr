# terraform/modules/iam/outputs.tf

output "ecs_task_execution_role_arn" {
  description = "O ARN da role de execução da tarefa ECS."
  value       = aws_iam_role.ecs_task_execution_role.arn
}

output "ecs_task_role_arn" {
  description = "O ARN da role da tarefa ECS (para a aplicação)."
  value       = aws_iam_role.ecs_task_role.arn
}
