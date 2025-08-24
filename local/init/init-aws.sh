#!/bin/bash
echo "--- Criando tabelas do DynamoDB ---"

# O comando 'awslocal' é um wrapper do AWS CLI já configurado para o endpoint do LocalStack.
awslocal dynamodb create-table \
    --table-name Usuario \
    --attribute-definitions AttributeName=id,AttributeType=S \
    --key-schema AttributeName=id,KeyType=HASH \
    --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5

echo "--- Tabelas criadas com sucesso! ---"