# terraform/modules/load_balancer/outputs.tf

output "backend_target_group_arn" {
  description = "O ARN do Target Group para o serviço de backend."
  value       = aws_lb_target_group.backend.arn
}

output "frontend_target_group_arn" {
  description = "O ARN do Target Group para o serviço de frontend."
  value       = aws_lb_target_group.frontend.arn
}

output "lb_security_group_id" {
  description = "O ID do Security Group do Load Balancer."
  value       = aws_security_group.lb_sg.id
}
