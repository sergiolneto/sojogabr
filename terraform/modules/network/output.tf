# terraform/modules/network/output.tf

output "vpc_id" {
  description = "O ID da VPC (existente ou recém-criada)."
  value       = local.vpc_id
}

output "subnet_ids" {
  description = "A lista de IDs das sub-redes públicas."
  value       = local.subnet_ids
}
