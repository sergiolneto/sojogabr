#!/bin/bash

# Este script executa a importação de recursos existentes do Terraform.

set -e # Encerra o script imediatamente se um comando falhar.
set -x # Imprime cada comando antes de executá-lo para debug.

echo "--- Starting resource import script ---"

# O caminho para o executável do Terraform é passado como o primeiro argumento ($1)
TERRAFORM_EXEC_PATH=$1

# Verifica se o argumento foi passado
if [ -z "$TERRAFORM_EXEC_PATH" ]; then
  echo "Error: Path to terraform executable was not provided as an argument." >&2
  exit 1
fi

echo "Terraform executable is located at: $TERRAFORM_EXEC_PATH"

# Função genérica de importação
import_resource() {
  local resource_type=$1
  local resource_name=$2
  local resource_id=$3
  
  echo "Attempting to import ${resource_type} ${resource_name}..."
  "$TERRAFORM_EXEC_PATH" import \
    -var="environment=prod" \
    -var="jwt_secret_arn=${JWT_SECRET_ARN}" \
    "${resource_type}.${resource_name}" "${resource_id}" || echo "Resource ${resource_name} not found or already in state. Continuing..."
}

# Função específica para importar Security Groups, que busca o ID pelo nome.
import_sg() {
  local resource_name=$1
  local sg_name=$2

  echo "Attempting to find Security Group ID for name: ${sg_name}"
  SG_ID=$(aws ec2 describe-security-groups --filters "Name=group-name,Values=${sg_name}" --query "SecurityGroups[0].GroupId" --output text | tr -d '\r')

  if [ -n "$SG_ID" ] && [ "$SG_ID" != "None" ]; then
    echo "Found Security Group ID: $SG_ID"
    import_resource "aws_security_group" "${resource_name}" "${SG_ID}"
  else
    echo "Security Group '${sg_name}' not found, skipping import."
  fi
}

# --- Recursos a serem importados ---

import_resource "aws_dynamodb_table" "user_table" "Usuario-prod"
import_resource "aws_dynamodb_table" "campeonato_table" "SojogaBrTable-prod" # Adicionado
import_resource "aws_ecr_repository" "sojoga_backend_repo" "sojoga-backend-prod"
import_resource "aws_iam_role" "ecs_task_execution_role" "ecs-task-execution-role-prod"
import_resource "aws_internet_gateway" "gw" "igw-00166dd7965f5b44e"


import_sg "lb_sg" "lb-sg-sojoga-br-prod"
import_sg "ecs_service_sg" "ecs-service-sg-sojoga-br-prod"

# Para o Target Group, precisamos buscar o ARN primeiro.
echo "Attempting to import Target Group..."
TG_ARN=$(aws elbv2 describe-target-groups --names tg-sojoga-br-prod --region sa-east-1 --query "TargetGroups[0].TargetGroupArn" --output text | tr -d '\r')

if [ -n "$TG_ARN" ] && [ "$TG_ARN" != "None" ]; then
  echo "Found Target Group ARN: $TG_ARN"
  import_resource "aws_lb_target_group" "main" "$TG_ARN"
else
  echo "Target Group 'tg-sojoga-br-prod' not found, skipping import."
fi

echo "--- Resource import script finished ---"
