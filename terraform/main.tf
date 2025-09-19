# terraform/main.tf

# Configuração do provedor AWS
provider "aws" {
  region = "sa-east-1" # Define a região padrão para os recursos
}

# --- Módulos da Infraestrutura ---

# 1. Módulo de Rede (VPC, Sub-redes, etc.)
module "network" {
  source = "./modules/network"

  environment  = var.environment
  project_name = var.project_name
}

# 2. Módulo do Banco de Dados (Tabelas DynamoDB)
module "dynamodb" {
  source = "./modules/dynamodb"

  environment  = var.environment
  project_name = var.project_name
}

# 3. Módulo de Permissões (Roles e Policies do IAM)
module "iam" {
  source = "./modules/iam"

  environment          = var.environment
  jwt_secret_arn       = var.jwt_secret_arn
  user_table_arn       = module.dynamodb.user_table_arn
  campeonato_table_arn = module.dynamodb.campeonato_table_arn
}

# 4. Módulo do Load Balancer (ALB, Target Groups, Listener)
module "load_balancer" {
  source = "./modules/load_balancer"

  project_name = var.project_name
  environment  = var.environment
  vpc_id       = module.network.vpc_id
  subnet_ids   = module.network.subnet_ids
}

# 5. Módulo de Computação (ECS, ECR, Task Definitions, Services)
module "ecs" {
  source = "./modules/ecs"

  project_name                = var.project_name
  environment                 = var.environment
  vpc_id                      = module.network.vpc_id
  subnet_ids                  = module.network.subnet_ids
  ecs_task_execution_role_arn = module.iam.ecs_task_execution_role_arn
  ecs_task_role_arn           = module.iam.ecs_task_role_arn
  user_table_name             = module.dynamodb.user_table_name
  campeonato_table_name       = module.dynamodb.campeonato_table_name
  jwt_secret_arn              = var.jwt_secret_arn
  lb_security_group_id        = module.load_balancer.lb_security_group_id
  backend_target_group_arn    = module.load_balancer.backend_target_group_arn
  frontend_target_group_arn   = module.load_balancer.frontend_target_group_arn
}
