# terraform/ecs.tf

module "ecs" {
  source = "./modules/ecs"

  environment      = var.environment
  project_name     = var.project_name
  vpc_id           = module.network.vpc_id
  public_subnet_ids = module.network.public_subnet_ids
  target_group_arn = aws_lb_target_group.main.arn
  jwt_secret_arn   = var.jwt_secret_arn
  lb_security_group_id = aws_security_group.lb.id
}