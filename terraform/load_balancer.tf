# terraform/load_balancer.tf

# Grupo de segurança para o Load Balancer, permitindo tráfego na porta 80 (HTTP)
resource "aws_security_group" "lb_sg" {
  name        = "lb-sg-${var.project_name}-${var.environment}"
  description = "Allow HTTP inbound traffic"
  vpc_id      = aws_vpc.main.id

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# Cria o Application Load Balancer (ALB)
resource "aws_lb" "main" {
  name               = "alb-${var.project_name}-${var.environment}"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.lb_sg.id]
  subnets            = aws_subnet.public[*].id
}

# Cria um Target Group, que é o grupo de alvos (nossas tarefas ECS) para o ALB
resource "aws_lb_target_group" "main" {
  name        = "tg-${var.project_name}-${var.environment}"
  port        = 8080
  protocol    = "HTTP"
  vpc_id      = aws_vpc.main.id
  target_type = "ip"
  health_check {
    path = "/actuator/health" # Usa o endpoint de saúde do Spring Boot
  }
}

# Cria um Listener na porta 80 do ALB, que encaminha o tráfego para o Target Group
resource "aws_lb_listener" "http" {
  load_balancer_arn = aws_lb.main.arn
  port              = "80"
  protocol          = "HTTP"
  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.main.arn
  }
}