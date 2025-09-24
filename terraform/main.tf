# terraform/main.tf

module "network" {
  source       = "./modules/network"
  environment  = var.environment
  project_name = var.project_name
}