#!/bin/bash

# Este script executa a importação de recursos existentes do Terraform.

set -e # Encerra o script imediatamente se um comando falhar.
set -x # Imprime cada comando antes de executá-lo para debug.

echo "--- Starting resource import script ---"

echo "--- DIAGNOSTICS ---"
echo "Current user: $(whoami)"
echo "Current PATH: $PATH"
echo "Terraform executable location: $(which terraform)"
echo "Checking for aliases..."
alias
echo "--- END DIAGNOSTICS ---"

# A função de importação tenta importar um recurso.
# Usamos 'command terraform' para garantir que estamos chamando o executável real, ignorando quaisquer aliases.
import_resource() {
  local resource_type=$1
  local resource_name=$2
  local resource_id=$3
  
  echo "Attempting to import ${resource_type} ${resource_name} using 'command terraform'..."
  command terraform import "${resource_type}.${resource_name}" "${resource_id}" || echo "Resource ${resource_name} not found or already in state. Continuing..."
}

# --- Recursos a serem importados ---

import_resource "aws_dynamodb_table" "user_table" "Usuario-prod"
import_resource "aws_ecr_repository" "sojoga_backend_repo" "sojoga-backend-prod"
import_resource "aws_iam_role" "ecs_task_execution_role" "ecs-task-execution-role-prod"
import_resource "aws_internet_gateway" "gw" "igw-0b9a57d3570e0eb60"

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
