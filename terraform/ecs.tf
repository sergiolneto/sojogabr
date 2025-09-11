# terraform/ecs.tf

# --- RECURSO DE LOGS ---
resource "aws_cloudwatch_log_group" "sojoga_backend_logs" {
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
          containerPort = 8080
          hostPort      = 8080
        }
      ]
      logConfiguration = {
        logDriver = "awslogs",
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.sojoga_backend_logs.name,
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
          value = "https://www.seu-frontend-de-producao.com.br"
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
  vpc_id      = local.vpc_id # Correção: usa a variável local com o ID da VPC

  ingress {
    from_port       = 8080
    to_port         = 8080
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

  network_configuration {
    subnets         = local.subnet_ids # Correção: usa a variável local com os IDs das sub-redes
    security_groups = [aws_security_group.ecs_service_sg.id]
    assign_public_ip = true
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.main.arn
    container_name   = "sojoga-backend-container"
    container_port   = 8080
  }

  depends_on = [aws_lb_listener.http]
}