#!/bin/bash
echo "=================================================="
echo "Aguardando LocalStack estar pronto..."
echo "=================================================="

# Este loop aguarda até que o serviço DynamoDB esteja disponível dentro do LocalStack
until awslocal dynamodb list-tables; do
  >&2 echo "DynamoDB não está pronto ainda - aguardando..."
  sleep 1
done

echo "=================================================="
echo "Criando a tabela 'Usuario' no DynamoDB..."
echo "=================================================="

awslocal dynamodb create-table \
    --table-name Usuario \
    --attribute-definitions AttributeName=id,AttributeType=S \
    --key-schema AttributeName=id,KeyType=HASH \
    --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5