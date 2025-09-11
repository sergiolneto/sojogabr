# terraform/dynamodb.tf

# --- FONTES DE DADOS --
# Tenta encontrar as tabelas existentes.
data "aws_dynamodb_table" "existing_user_table" {
  name = "Usuario-${var.environment}"
}

data "aws_dynamodb_table" "existing_campeonato_table" {
  name = "SojogaBrTable-${var.environment}"
}

# --- LÓGICA LOCAL ---
locals {
  # O 'try' com 'data' é um padrão para verificar opcionalmente a existência de um recurso.
  # Se 'data.aws_dynamodb_table.existing_user_table' falhar (porque a tabela não existe),
  # 'try' retorna 'null', e a verificação '!= null' resulta em 'false'.
  user_table_exists      = try(data.aws_dynamodb_table.existing_user_table.arn, null) != null
  campeonato_table_exists = try(data.aws_dynamodb_table.existing_campeonato_table.arn, null) != null

  # Define os nomes e ARNs para serem usados em outros lugares,
  # escolhendo entre o recurso existente (data) ou o novo (resource).
  user_table_name = local.user_table_exists ? data.aws_dynamodb_table.existing_user_table.name : aws_dynamodb_table.user_table[0].name
  user_table_arn  = local.user_table_exists ? data.aws_dynamodb_table.existing_user_table.arn : aws_dynamodb_table.user_table[0].arn

  campeonato_table_name = local.campeonato_table_exists ? data.aws_dynamodb_table.existing_campeonato_table.name : aws_dynamodb_table.campeonato_table[0].name
  campeonato_table_arn  = local.campeonato_table_exists ? data.aws_dynamodb_table.existing_campeonato_table.arn : aws_dynamodb_table.campeonato_table[0].arn
}

# --- Tabela de Usuários ---
resource "aws_dynamodb_table" "user_table" {
  count = local.user_table_exists ? 0 : 1

  name = "Usuario-${var.environment}"
  billing_mode = "PAY_PER_REQUEST"
  hash_key = "id"

  attribute {
    name = "id"
    type = "S"
  }

  tags = {
    Project     = var.project_name
    ManagedBy   = "Terraform"
    Environment = var.environment
  }
}

# --- Tabela de Campeonatos ---
resource "aws_dynamodb_table" "campeonato_table" {
  count = local.campeonato_table_exists ? 0 : 1

  name         = "SojogaBrTable-${var.environment}"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "id"

  attribute {
    name = "id"
    type = "S"
  }

  tags = {
    Project     = var.project_name
    ManagedBy   = "Terraform"
    Environment = var.environment
  }
}
