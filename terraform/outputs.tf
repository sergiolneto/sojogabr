# terraform/outputs.tf

output "name_servers" {
  description = "Name servers para a zona hospedada do Route 53. Configure-os no seu registrador de domínio."
  value       = aws_route53_zone.main.name_servers
}

output "backend_ecr_repository_url" {
  description = "A URL do repositório ECR para a imagem do backend."
  value       = module.ecs.backend_ecr_repository_url
}

output "frontend_ecr_repository_url" {
  description = "A URL do repositório ECR para a imagem do frontend."
  value       = module.ecs.frontend_ecr_repository_url
}
