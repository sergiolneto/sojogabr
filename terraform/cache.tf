# terraform/cache.tf

module "cache" {
  source = "./modules/elasticache"

  environment                 = var.environment
  project_name                = var.project_name
  vpc_id                      = module.network.vpc_id
  subnet_ids                  = module.network.public_subnet_ids
  ecs_tasks_security_group_id = module.ecs.ecs_tasks_security_group_id
}
