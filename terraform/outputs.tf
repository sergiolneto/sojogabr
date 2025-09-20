# terraform/outputs.tf

output "name_servers" {
  description = "Name servers para a zona hospedada do Route 53. Configure-os no seu registrador de domínio."
  value       = aws_route53_zone.main.name_servers
}
