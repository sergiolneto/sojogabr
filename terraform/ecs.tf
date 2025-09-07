# terraform/ecs.tf

# 1. Repositório de Imagens Docker (ECR)
resource "aws_ecr_repository" "sojoga_backend_repo" {
  name = "sojoga-backend-${var.environment}" # ex: sojoga-backend-prod
  tags = {
    Project     = var.project_name
    Environment = var.environment
  }
}

# 2. Cluster ECS
resource "aws_ecs_cluster" "sojoga_cluster" {
  name = "sojoga-cluster-${var.environment}" # ex: sojoga-cluster-prod
  tags = {
    Project     = var.project_name
    Environment = var.environment
  }
}

# 3. Definição da Tarefa ECS (O Blueprint da sua Aplicação)
# Esta é a parte mais importante. Ela define como seu contêiner deve rodar.
resource "aws_ecs_task_definition" "sojoga_backend_task" {
  family                   = "sojoga-backend-${var.environment}-task"
  network_mode             = "awsvpc" # Modo de rede recomendado para Fargate
  requires_compatibilities = ["FARGATE"]
  cpu                      = "256"  # 1/4 de uma vCPU
  memory                   = "512"  # 512MB de RAM

  # Role que permite ao ECS puxar imagens do ECR e enviar logs
  execution_role_arn = aws_iam_role.ecs_task_execution_role.arn

  # Definição do contêiner da sua aplicação
  container_definitions = jsonencode([
    {
      name      = "sojoga-backend-container"
      # A imagem será atualizada pelo pipeline de CI/CD, aqui usamos um placeholder
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
      # Injetando as variáveis de ambiente na aplicação
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
          value = "SojogaBrTable-${var.environment}" # Supondo que você criará esta tabela também
        }
      ]
      # Injetando o segredo do JWT de forma segura
      secrets = [
        {
          name      = "JWT_SECRET"
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

# Grupo de segurança para o serviço ECS, permitindo tráfego vindo do Load Balancer
resource "aws_security_group" "ecs_service_sg" {
  name        = "ecs-service-sg-${var.project_name}-${var.environment}"
  description = "Allow inbound traffic from the ALB"
  vpc_id      = aws_vpc.main.id

  ingress {
    from_port       = 8080
    to_port         = 8080
    protocol        = "tcp"
    security_groups = [aws_security_group.lb_sg.id] # Só permite tráfego do nosso LB
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# 4. Serviço ECS (Garante que a aplicação esteja rodando)
resource "aws_ecs_service" "main" {
  name            = "sojoga-backend-prod-service" # Nome fixo para o pipeline de deploy encontrar
  cluster         = aws_ecs_cluster.sojoga_cluster.id
  task_definition = aws_ecs_task_definition.sojoga_backend_task.arn
  desired_count   = 1 # Queremos 1 instância da nossa aplicação rodando
  launch_type     = "FARGATE"

  network_configuration {
    subnets         = aws_subnet.public[*].id
    security_groups = [aws_security_group.ecs_service_sg.id]
    assign_public_ip = true
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.main.arn
    container_name   = "sojoga-backend-container"
    container_port   = 8080
  }

  # Garante que o listener do LB seja criado antes do serviço tentar se registrar
  depends_on = [aws_lb_listener.http]
}