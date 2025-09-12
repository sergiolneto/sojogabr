# terraform/ecs.tf

# --- FONTES DE DADOS ---
# Usa a fonte de dados no plural para não falhar se o grupo de logs não for encontrado.
data "aws_cloudwatch_log_groups" "existing_log_groups" {
  log_group_name_prefix = "/ecs/sojoga-backend-${var.environment}"
}

# --- LÓGICA LOCAL ---
locals {
  # Verifica se algum grupo de logs com o prefixo foi encontrado.
  log_group_exists = length(data.aws_cloudwatch_log_groups.existing_log_groups.log_group_names) > 0
  # Usa o nome do grupo de logs existente ou o nome do que será criado.
  log_group_name   = local.log_group_exists ? data.aws_cloudwatch_log_groups.existing_log_groups.log_group_names[0] : aws_cloudwatch_log_group.sojoga_backend_logs[0].name
}

# --- RECURSO DE LOGS ---
# Cria o grupo de logs somente se ele não for encontrado.
resource "aws_cloudwatch_log_group" "sojoga_backend_logs" {
  count             = local.log_group_exists ? 0 : 1
  name              = "/ecs/sojoga-backend-${var.environment}"
  retention_in_days = 7

  tags = {
    Project     = var.project_name
    Environment = var.environment
  }
}

# 1. Repositório de Imagens Docker (ECR)
resource "aws_ecr_repository" "sojoga_backend_repo" {
  name = "sojoga-backend-${var.environment}"
  tags = {
    Project     = var.project_name
    Environment = var.environment
  }
}

# 2. Cluster ECS
resource "aws_ecs_cluster" "sojoga_cluster" {
  name = "sojoga-cluster-${var.environment}"
  tags = {
    Project     = var.project_name
    Environment = var.environment
  }
}

# 3. Definição da Tarefa ECS
resource "aws_ecs_task_definition" "sojoga_backend_task" {
  family                   = "sojoga-backend-${var.environment}-task"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "256"
  memory                   = "512"

  execution_role_arn = aws_iam_role.ecs_task_execution_role.arn
  task_role_arn      = aws_iam_role.ecs_task_role.arn

  container_definitions = jsonencode([
    {
      name      = "sojoga-backend-container"
      image     = "${aws_ecr_repository.sojoga_backend_repo.repository_url}:latest"
      cpu       = 256
      memory    = 512
      essential = true
      portMappings = [
        {
          containerPort = 8787
          hostPort      = 8787
        }
      ]
      logConfiguration = {
        logDriver = "awslogs",
        options = {
          # CORREÇÃO: Usa a local para o nome do grupo de logs
          "awslogs-group"         = local.log_group_name,
          "awslogs-region"        = "sa-east-1",
          "awslogs-stream-prefix" = "ecs"
        }
      },
      environment = [
        {
          name  = "SPRING_PROFILES_ACTIVE",
          value = "prod"
        },
        {
          name  = "DYNAMODB_USER_TABLE_NAME",
          value = aws_dynamodb_table.user_table.name
        },
        {
          name  = "DYNAMODB_CAMPEONATO_TABLE_NAME",
          value = aws_dynamodb_table.campeonato_table.name
        },
        {
          name = "CORS_ALLOWED_ORIGINS",
          value = "*"
        }
      ],
      secrets = [
        {
          name      = "JWT_SECRET",
          valueFrom = var.jwt_secret_arn
        }
      ]
    }
  ])

  tags = {
    Project     = var.project_name
    Environment = var.environment
  }
}

# Grupo de segurança para o serviço ECS
resource "aws_security_group" "ecs_service_sg" {
  name        = "ecs-service-sg-${var.project_name}-${var.environment}"
  description = "Allow inbound traffic from the ALB"
  vpc_id      = local.vpc_id

  ingress {
    from_port       = 8787
    to_port         = 8787
    protocol        = "tcp"
    security_groups = [aws_security_group.lb_sg.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# 4. Serviço ECS
resource "aws_ecs_service" "main" {
  name            = "sojoga-backend-prod-service"
  cluster         = aws_ecs_cluster.sojoga_cluster.id
  task_definition = aws_ecs_task_definition.sojoga_backend_task.arn
  desired_count   = 1
  launch_type     = "FARGATE"

  lifecycle {
    create_before_destroy = true
  }

  network_configuration {
    subnets         = local.subnet_ids
    security_groups = [aws_security_group.ecs_service_sg.id]
    assign_public_ip = true
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.main.arn
    container_name   = "sojoga-backend-container"
    container_port   = 8787
  }

  depends_on = [aws_lb_listener.http]
}
