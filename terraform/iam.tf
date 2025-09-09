# terraform/iam.tf

# --- Role para a Execução da Tarefa ECS ---

# Define a relação de confiança: permite que o serviço ECS assuma esta role.
data "aws_iam_policy_document" "ecs_task_execution_policy" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

# Cria a role que a tarefa ECS usará para interagir com outros serviços da AWS.
resource "aws_iam_role" "ecs_task_execution_role" {
  name               = "ecs-task-execution-role-${var.environment}"
  assume_role_policy = data.aws_iam_policy_document.ecs_task_execution_policy.json
}

# Anexa a política gerenciada pela AWS que dá as permissões básicas (puxar imagem do ECR, etc.).
resource "aws_iam_role_policy_attachment" "ecs_task_execution_attachment" {
  role       = aws_iam_role.ecs_task_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}


# --- Permissão para Ler o Segredo JWT ---

# Define a política de permissão para acessar o segredo JWT.
data "aws_iam_policy_document" "jwt_secret_access_policy" {
  statement {
    actions   = ["secretsmanager:GetSecretValue"]
    resources = [var.jwt_secret_arn] # Permite acesso APENAS ao segredo específico.
  }
}

# Cria a política do IAM com base no documento acima.
resource "aws_iam_policy" "jwt_secret_access" {
  name        = "jwt-secret-access-policy-${var.environment}"
  description = "Allows ECS tasks to retrieve the JWT secret from Secrets Manager"
  policy      = data.aws_iam_policy_document.jwt_secret_access_policy.json
}

# Anexa a nova política de acesso ao segredo à nossa role de execução da tarefa.
resource "aws_iam_role_policy_attachment" "jwt_secret_attachment" {
  role       = aws_iam_role.ecs_task_execution_role.name
  policy_arn = aws_iam_policy.jwt_secret_access.arn
}
