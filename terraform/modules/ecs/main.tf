# terraform/modules/ecs/main.tf

# 1. Repositórios de Imagem (ECR)
resource "aws_ecr_repository" "backend" {
  name                 = "${var.project_name}-backend-${var.environment}"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }

  tags = {
    Environment = var.environment
    Project     = var.project_name
  }
}

resource "aws_ecr_repository" "frontend" {
  name                 = "${var.project_name}-frontend-${var.environment}"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }

  tags = {
    Environment = var.environment
    Project     = var.project_name
  }
}

# 2. Cluster ECS
resource "aws_ecs_cluster" "main" {
  name = "${var.project_name}-cluster-${var.environment}"

  tags = {
    Environment = var.environment
    Project     = var.project_name
  }
}

# 3. IAM Role para Execução da Tarefa ECS
resource "aws_iam_role" "ecs_task_execution_role" {
  name = "${var.project_name}-ecs-task-execution-role-${var.environment}"

  assume_role_policy = jsonencode({
    Version   = "2012-10-17"
    Statement = [
      {
        Action    = "sts:AssumeRole"
        Effect    = "Allow"
        Principal = {
          Service = "ecs-tasks.amazonaws.com"
        }
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "ecs_task_execution_role_policy" {
  role       = aws_iam_role.ecs_task_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

# Permissão para a task ler o segredo JWT
resource "aws_iam_role_policy" "read_jwt_secret" {
  name = "${var.project_name}-read-jwt-secret-policy-${var.environment}"
  role = aws_iam_role.ecs_task_execution_role.id

  policy = jsonencode({
    Version   = "2012-10-17"
    Statement = [
      {
        Effect   = "Allow"
        Action   = ["secretsmanager:GetSecretValue"]
        Resource = [var.jwt_secret_arn]
      }
    ]
  })
}

# 4. Security Group para os contêineres
resource "aws_security_group" "ecs_tasks" {
  name        = "sojoga-ecs-tasks-${var.environment}"
  description = "Permite tráfego para os contêineres ECS"
  vpc_id      = var.vpc_id

  # Permite tráfego de entrada do Load Balancer na porta da aplicação
  ingress {
    protocol        = "tcp"
    from_port       = 8080
    to_port         = 8080
    security_groups = [var.lb_security_group_id]
  }

  # Permite todos o tráfego de saída
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name        = "sojoga-ecs-tasks-${var.environment}"
    Environment = var.environment
    Project     = var.project_name
  }
}

# 5. Definição da Tarefa (Task Definition) para o Backend
resource "aws_ecs_task_definition" "backend" {
  family                   = "${var.project_name}-backend-task-${var.environment}"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "256"  # 0.25 vCPU
  memory                   = "512"  # 512 MB
  execution_role_arn       = aws_iam_role.ecs_task_execution_role.arn

  container_definitions = jsonencode([
    {
      name      = "sojoga-backend-container"
      image     = "${aws_ecr_repository.backend.repository_url}:latest" # Usará a imagem mais recente
      essential = true
      portMappings = [
        {
          containerPort = 8080
          hostPort      = 8080
        }
      ]
      secrets = [
        {
          name      = "JWT_SECRET"
          valueFrom = var.jwt_secret_arn
        }
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = "/ecs/${var.project_name}-backend"
          "awslogs-region"        = "sa-east-1" # Pode ser uma variável
          "awslogs-stream-prefix" = "ecs"
        }
      }
    }
  ])

  tags = {
    Environment = var.environment
    Project     = var.project_name
  }
}

# 6. Serviço ECS para o Backend
resource "aws_ecs_service" "backend" {
  name            = "${var.project_name}-backend-service-${var.environment}"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.backend.arn
  desired_count   = 1 # Inicia com 1 instância da aplicação
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = var.public_subnet_ids
    security_groups  = [aws_security_group.ecs_tasks.id]
    assign_public_ip = true # Necessário para puxar a imagem do ECR em sub-redes públicas
  }

  load_balancer {
    target_group_arn = var.target_group_arn
    container_name   = "sojoga-backend-container"
    container_port   = 8080
  }

  tags = {
    Environment = var.environment
    Project     = var.project_name
  }
}

# 7. Grupo de Logs para o serviço
resource "aws_cloudwatch_log_group" "backend" {
  name              = "/ecs/${var.project_name}-backend"
  retention_in_days = 7

  tags = {
    Environment = var.environment
    Project     = var.project_name
  }
}