# terraform/output.tf

output "backend_ecr_repository_url" {
  description = "A URL do repositório ECR para a imagem do backend."
  value       = module.ecs.backend_ecr_repository_url
}

output "frontend_ecr_repository_url" {
  description = "A URL do repositório ECR para a imagem do frontend."
  value       = module.ecs.frontend_ecr_repository_url
}
