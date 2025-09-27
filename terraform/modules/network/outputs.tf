# terraform/modules/network/outputs.tf

output "vpc_id" {
  description = "O ID da VPC (existente ou recém-criada)."
  value       = local.vpc_id
}

output "public_subnet_ids" {
  description = "A lista de IDs das sub-redes públicas (existentes ou recém-criadas)."
  value       = local.subnet_ids
}