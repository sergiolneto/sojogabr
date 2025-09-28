# terraform/outputs.tf

output "name_servers" {
  description = "Mapa de domínios para seus respectivos Name Servers. Configure estes valores no seu registrador de domínio para cada domínio."
  value       = { for k, v in aws_route53_zone.main : k => v.name_servers }
}

output "backend_ecr_repository_url" {
  description = "A URL do repositório ECR para a imagem do backend."
  value       = module.ecs.backend_ecr_repository_url
}

output "frontend_ecr_repository_url" {
  description = "A URL do repositório ECR para a imagem do frontend."
  value       = module.ecs.frontend_ecr_repository_url
}

output "load_balancer_dns" {
  description = "O endereço DNS do Application Load Balancer. Use para testar diretamente, sem o domínio."
  value       = aws_lb.main.dns_name
}