#!/bin/bash
echo "----------- Criando tabela DynamoDB: Usuario -----------"

aws dynamodb create-table \
    --table-name Usuario \
    --attribute-definitions \
        AttributeName=id,AttributeType=S \
        AttributeName=username,AttributeType=S \
    --key-schema \
        AttributeName=id,KeyType=HASH \
    --provisioned-throughput \
        ReadCapacityUnits=5,WriteCapacityUnits=5 \
    --global-secondary-indexes \
        "[
            {
                \"IndexName\": \"username-index\",
                \"KeySchema\": [
                    {\"AttributeName\":\"username\",\"KeyType\":\"HASH\"}
                ],
                \"Projection\": {
                    \"ProjectionType\":\"ALL\"
                },
                \"ProvisionedThroughput\": {
                    \"ReadCapacityUnits\": 5,
                    \"WriteCapacityUnits\": 5
                }
            }
        ]" \
    --endpoint-url http://localhost:4566

echo "----------- Tabela criada com sucesso. -----------"