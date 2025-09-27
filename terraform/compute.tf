# terraform/compute.tf

# 1. Security Group para o Application Load Balancer
resource "aws_security_group" "lb" {
  name        = "sojoga-lb-${var.environment}"
  description = "Controla o acesso ao Application Load Balancer"
  vpc_id      = module.network.vpc_id

  # Permite tráfego de entrada na porta 80 (HTTP) de qualquer lugar
  ingress {
    protocol    = "tcp"
    from_port   = 80
    to_port     = 80
    cidr_blocks = ["0.0.0.0/0"]
  }

  # Permite tráfego de entrada na porta 443 (HTTPS) de qualquer lugar
  ingress {
    protocol    = "tcp"
    from_port   = 443
    to_port     = 443
    cidr_blocks = ["0.0.0.0/0"]
  }

  # Permite todos os tráfegos de saída
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name        = "sojoga-lb-${var.environment}"
    Environment = var.environment
    Project     = var.project_name
  }
}

# 2. Application Load Balancer (ALB)
resource "aws_lb" "main" {
  name               = "alb-sojoga-${var.environment}"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.lb.id]
  subnets            = module.network.public_subnet_ids

  tags = {
    Environment = var.environment
    Project     = var.project_name
  }
}

# 3. Target Group para o serviço ECS
resource "aws_lb_target_group" "main" {
  name        = "tg-sojoga-${var.environment}"
  port        = 8080 # A porta que o container da sua aplicação expõe
  protocol    = "HTTP"
  vpc_id      = module.network.vpc_id
  target_type = "ip"

  health_check {
    path                = "/actuator/health" # Endpoint de health check da sua aplicação Spring Boot
    protocol            = "HTTP"
    matcher             = "200"
    interval            = 30
    timeout             = 5
    healthy_threshold   = 2
    unhealthy_threshold = 2
  }
}

# 4. Listener HTTP na porta 80 para redirecionar para HTTPS
resource "aws_lb_listener" "http" {
  load_balancer_arn = aws_lb.main.arn
  port              = "80"
  protocol          = "HTTP"

  default_action {
    type = "redirect"
    redirect {
      port        = "443"
      protocol    = "HTTPS"
      status_code = "HTTP_301"
    }
  }
}

# 5. Listener HTTPS na porta 443 para encaminhar para o Target Group
resource "aws_lb_listener" "https" {
  load_balancer_arn = aws_lb.main.arn
  port              = "443"
  protocol          = "HTTPS"
  ssl_policy        = "ELBSecurityPolicy-2016-08"
  certificate_arn   = aws_acm_certificate.main.arn

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.main.arn
  }
}