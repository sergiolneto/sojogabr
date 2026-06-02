# terraform/modules/dynamodb/main.tf

# --- Tabela de Usuários ---
resource "aws_dynamodb_table" "user_table" {
  name         = "Usuario-${var.environment}"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "pk"
  range_key    = "sk"

  attribute {
    name = "pk"
    type = "S"
  }

  attribute {
    name = "sk"
    type = "S"
  }

  attribute {
    name = "username"
    type = "S"
  }

  attribute {
    name = "status"
    type = "S"
  }

  global_secondary_index {
    name            = "username-index"
    hash_key        = "username"
    projection_type = "ALL"
  }

  global_secondary_index {
    name            = "status-index"
    hash_key        = "status"
    projection_type = "INCLUDE"
    non_key_attributes = ["id", "username", "nome", "email", "esportes"]
  }

  tags = {
    Project     = var.project_name
    ManagedBy   = "Terraform"
    Environment = var.environment
  }
}

# --- Tabela de Campeonatos ---
resource "aws_dynamodb_table" "campeonato_table" {
  name         = "SojogaBrTable-${var.environment}"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "pk" # Padronizando para pk/sk
  range_key    = "sk"

  attribute {
    name = "pk"
    type = "S"
  }

  attribute {
    name = "sk"
    type = "S"
  }

  tags = {
    Project     = var.project_name
    ManagedBy   = "Terraform"
    Environment = var.environment
  }
}
