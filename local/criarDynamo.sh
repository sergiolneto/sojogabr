#!/bin/bash
echo "=================================================="
echo "Aguardando LocalStack estar pronto..."
echo "=================================================="

until awslocal dynamodb list-tables; do
  >&2 echo "DynamoDB não está pronto ainda - aguardando..."
  sleep 1
done

echo "=================================================="
echo "Criando tabelas no DynamoDB..."
echo "=================================================="

# 1. Tabela de Usuários com GSIs
awslocal dynamodb create-table \
    --table-name Usuario \
    --attribute-definitions \
        AttributeName=pk,AttributeType=S \
        AttributeName=sk,AttributeType=S \
        AttributeName=username,AttributeType=S \
        AttributeName=status,AttributeType=S \
    --key-schema \
        AttributeName=pk,KeyType=HASH \
        AttributeName=sk,KeyType=RANGE \
    --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5 \
    --global-secondary-indexes \
        "[
            {
                \"IndexName\": \"username-index\",
                \"KeySchema\": [{\"AttributeName\":\"username\",\"KeyType\":\"HASH\"}],
                \"Projection\": {\"ProjectionType\":\"ALL\"},
                \"ProvisionedThroughput\": {\"ReadCapacityUnits\": 5, \"WriteCapacityUnits\": 5}
            },
            {
                \"IndexName\": \"status-index\",
                \"KeySchema\": [{\"AttributeName\":\"status\",\"KeyType\":\"HASH\"}],
                \"Projection\": {\"ProjectionType\":\"INCLUDE\", \"NonKeyAttributes\":[\"id\", \"username\", \"nome\", \"email\", \"esportes\"]},
                \"ProvisionedThroughput\": {\"ReadCapacityUnits\": 5, \"WriteCapacityUnits\": 5}
            }
        ]"

# 2. Tabela de Campeonatos (Single Table Design)
awslocal dynamodb create-table \
    --table-name SojogaBrTable \
    --attribute-definitions \
        AttributeName=pk,AttributeType=S \
        AttributeName=sk,AttributeType=S \
    --key-schema \
        AttributeName=pk,KeyType=HASH \
        AttributeName=sk,KeyType=RANGE \
    --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5

echo "=================================================="
echo "Tabelas criadas com sucesso!"
echo "=================================================="
