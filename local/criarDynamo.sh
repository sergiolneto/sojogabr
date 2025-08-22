#!/bin/bash

ENDPOINT="http://localhost:4566"
TABLE_NAME="Usuario"
REGION="sa-east-1"

# Verifica se a tabela já existe
EXISTS=$(aws --endpoint-url=$ENDPOINT --region=$REGION dynamodb list-tables | grep -w "\"$TABLE_NAME\"")

if [ -z "$EXISTS" ]; then
  echo "Criando tabela $TABLE_NAME..."
  aws --endpoint-url=$ENDPOINT --region=$REGION dynamodb create-table \
    --table-name $TABLE_NAME \
    --attribute-definitions AttributeName=id,AttributeType=S \
    --key-schema AttributeName=id,KeyType=HASH \
    --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5
  echo "Tabela criada."
else
  echo "Tabela $TABLE_NAME já existe."
fi