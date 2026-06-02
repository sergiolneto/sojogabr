# terraform/modules/cloudfront/variables.tf

variable "environment" {
  type = string
}

variable "project_name" {
  type = string
}

variable "alb_dns_name" {
  type = string
}

variable "acm_certificate_arn" {
  type = string
}

variable "domain_names" {
  type = list(string)
}
