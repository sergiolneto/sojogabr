# terraform/iam.tf

# Política de permissão para a execução de tarefas ECS
data "aws_iam_policy_document" "ecs_task_execution_policy" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

# Role que será assumida pelas tarefas ECS
resource "aws_iam_role" "ecs_task_execution_role" {
  name               = "ecs-task-execution-role-${var.environment}"
  assume_role_policy = data.aws_iam_policy_document.ecs_task_execution_policy.json
  # Anexa a política gerenciada pela AWS que dá as permissões básicas
  managed_policy_arns = ["arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"]
}