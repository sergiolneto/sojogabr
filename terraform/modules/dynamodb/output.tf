# terraform/modules/dynamodb/output.tf

output "user_table_name" {
  description = "O nome da tabela de usuários do DynamoDB."
  value       = aws_dynamodb_table.user_table.name
}

output "user_table_arn" {
  description = "O ARN da tabela de usuários do DynamoDB."
  value       = aws_dynamodb_table.user_table.arn
}

output "campeonato_table_name" {
  description = "O nome da tabela de campeonatos do DynamoDB."
  value       = aws_dynamodb_table.campeonato_table.name
}

output "campeonato_table_arn" {
  description = "O ARN da tabela de campeonatos do DynamoDB."
  value       = aws_dynamodb_table.campeonato_table.arn
}
