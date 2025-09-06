# terraform/dynamodb.tf

resource "aws_dynamodb_table" "user_table" {
  # O nome da tabela que sua aplicação espera.
  name = "Usuario"

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
    Project     = "sojoga-br"
    ManagedBy   = "Terraform"
    Environment = "production" # Ou "development", dependendo do workspace
  }
}