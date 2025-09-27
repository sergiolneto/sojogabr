# terraform/modules/network/output.tf

output "vpc_id" {
  description = "O ID da VPC (existente ou recém-criada)."
  value       = local.vpc_id
}

output "public_subnet_ids" {
  description = "Lista com os IDs das sub-redes públicas (existentes ou recém-criadas)."
  value       = local.subnet_ids
}