# terraform/iam.tf

# --- Role para a EXECUÇÃO da Tarefa (Crachá do Entregador) ---
# Permite ao ECS puxar imagens e segredos.
data "aws_iam_policy_document" "ecs_task_assume_role_policy" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "ecs_task_execution_role" {
  name               = "ecs-task-execution-role-${var.environment}"
  assume_role_policy = data.aws_iam_policy_document.ecs_task_assume_role_policy.json
}

resource "aws_iam_role_policy_attachment" "ecs_task_execution_attachment" {
  role       = aws_iam_role.ecs_task_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

# --- Role para a TAREFA em si (Crachá do Funcionário) ---
# Permite que a APLICAÇÃO acesse outros serviços da AWS.
resource "aws_iam_role" "ecs_task_role" {
  name               = "ecs-task-role-${var.environment}"
  assume_role_policy = data.aws_iam_policy_document.ecs_task_assume_role_policy.json
}

# --- Políticas de Permissão ---

# Política para ler o segredo JWT
data "aws_iam_policy_document" "jwt_secret_access_policy" {
  statement {
    actions   = ["secretsmanager:GetSecretValue"]
    resources = [var.jwt_secret_arn]
  }
}

resource "aws_iam_policy" "jwt_secret_access" {
  name   = "jwt-secret-access-policy-${var.environment}"
  policy = data.aws_iam_policy_document.jwt_secret_access_policy.json
}

# Anexa a permissão de ler segredo à ROLE DE EXECUÇÃO (para o entregador)
resource "aws_iam_role_policy_attachment" "jwt_secret_attachment" {
  role       = aws_iam_role.ecs_task_execution_role.name
  policy_arn = aws_iam_policy.jwt_secret_access.arn
}

# Política para acessar o DynamoDB
data "aws_iam_policy_document" "dynamodb_access_policy" {
  statement {
    actions = ["dynamodb:*"] # Simplificado para garantir o acesso
    resources = [
      aws_dynamodb_table.user_table.arn,
      aws_dynamodb_table.campeonato_table.arn
    ]
  }
}

resource "aws_iam_policy" "dynamodb_access" {
  name   = "dynamodb-access-policy-${var.environment}"
  policy = data.aws_iam_policy_document.dynamodb_access_policy.json
}

# Anexa a permissão do DynamoDB à ROLE DA TAREFA (para o funcionário)
resource "aws_iam_role_policy_attachment" "dynamodb_access_attachment" {
  role       = aws_iam_role.ecs_task_role.name
  policy_arn = aws_iam_policy.dynamodb_access.arn
}
