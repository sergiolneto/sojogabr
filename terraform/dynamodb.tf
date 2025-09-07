# terraform/dynamodb.tf

resource "aws_dynamodb_table" "user_table" {
  # O nome da tabela será sufixado com o ambiente (ex: Usuario-hom, Usuario-prod)
  name = "Usuario-${var.environment}"

  # Define o modo de cobrança como "sob demanda", que é ideal para
  # aplicações com tráfego imprevisível. Você só paga pelo que usa.
  billing_mode = "PAY_PER_REQUEST"

  # Define a chave de partição (Chave Primária)
  hash_key = "id"

  # Define os atributos da tabela.
  # Neste caso, 'id' é uma string (S).
  attribute {
    name = "id"
    type = "S"
  }

  # Tags são essenciais para organização e controle de custos.
  tags = {
    Project     = var.project_name
    ManagedBy   = "Terraform"
    Environment = var.environment
  }
}