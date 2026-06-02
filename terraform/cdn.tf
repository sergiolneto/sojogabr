# terraform/cdn.tf

module "cdn" {
  source = "./modules/cloudfront"

  environment         = var.environment
  project_name        = var.project_name
  alb_dns_name        = aws_lb.main.dns_name
  acm_certificate_arn = aws_acm_certificate.main.arn
  domain_names        = var.domain_names
}
