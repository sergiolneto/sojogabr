# terraform/modules/ecs/outputs.tf

output "backend_ecr_repository_url" {
  description = "A URL do repositório ECR para fazer o push da imagem do backend."
  value       = aws_ecr_repository.backend.repository_url
}

output "frontend_ecr_repository_url" {
  description = "A URL do repositório ECR para fazer o push da imagem do frontend."
  value       = aws_ecr_repository.frontend.repository_url
}
