# terraform/dynamodb.tf

# --- Tabela de Usuários ---
resource "aws_dynamodb_table" "user_table" {
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
