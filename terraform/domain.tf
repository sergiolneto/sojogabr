# terraform/domain.tf

# 1. Cria a Zona Hospedada no Route 53 para o seu domínio
resource "aws_route53_zone" "main" {
  for_each = toset(var.domain_names)
  name     = each.key
  comment = "Managed by Terraform"
}

# 2. Provisiona um certificado SSL/TLS usando o AWS Certificate Manager (ACM)
resource "aws_acm_certificate" "main" {
  domain_name       = var.domain_names[0]
  subject_alternative_names = var.domain_names
  validation_method = "DNS"

  lifecycle {
    create_before_destroy = true
  }

  tags = {
    Project     = var.project_name
    Environment = var.environment
  }
}

# 3. Cria o registro DNS necessário para validar o certificado ACM
resource "aws_route53_record" "cert_validation" {

  for_each = { for dvo in aws_acm_certificate.main.domain_validation_options : dvo.domain_name => dvo }

  allow_overwrite = true
  name            = each.value.resource_record_name
  records         = [each.value.resource_record_value]
  ttl             = 60
  type            = each.value.resource_record_type

  zone_id         = aws_route53_zone.main[each.key].zone_id
}

# Valida o certificado após a criação dos registros DNS
resource "aws_acm_certificate_validation" "main" {
  certificate_arn = aws_acm_certificate.main.arn
  # Espera que todos os registros de validação DNS sejam criados
  validation_record_fqdns = [for record in aws_route53_record.cert_validation : record.fqdn]
  region                  = "sa-east-1"
}

# Cria um registro 'A' em CADA zona para apontar para o mesmo Load Balancer
resource "aws_route53_record" "app" {
  for_each = aws_route53_zone.main

  zone_id = each.value.zone_id
  name    = each.key
  type    = "A"

  alias {
    name                   = aws_lb.main.dns_name
    zone_id                = aws_lb.main.zone_id
    evaluate_target_health = true
  }
}
