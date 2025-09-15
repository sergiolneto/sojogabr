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
  
  echo "Attempting to import ${resource_type}.${resource_name}..."
  "$TERRAFORM_EXEC_PATH" import \
    -var="environment=prod" \
    -var="jwt_secret_arn=${JWT_SECRET_ARN}" \
    "${resource_type}.${resource_name}" "${resource_id}" || echo "Resource ${resource_name} not found or already in state. Continuing..."
}

# Função para importar políticas do IAM, que constrói o ARN a partir do ID da conta.
import_iam_policy() {
  local resource_name=$1
  local policy_name=$2

  echo "Attempting to find AWS Account ID to import IAM Policy ${policy_name}..."
  ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text | tr -d '\r')
  POLICY_ARN="arn:aws:iam::${ACCOUNT_ID}:policy/${policy_name}"

  echo "Constructed IAM Policy ARN: ${POLICY_ARN}"
  import_resource "aws_iam_policy" "${resource_name}" "${POLICY_ARN}"
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

# Função para importar o Listener do ALB
import_listener() {
  local resource_name=$1
  local lb_name=$2
  local listener_port=$3

  echo "Attempting to find Load Balancer ARN for name: ${lb_name}"
  LB_ARN=$(aws elbv2 describe-load-balancers --names "${lb_name}" --query "LoadBalancers[0].LoadBalancerArn" --output text | tr -d '\r')

  if [ -n "$LB_ARN" ] && [ "$LB_ARN" != "None" ]; then
    echo "Found Load Balancer ARN: $LB_ARN"
    echo "Attempting to find Listener ARN for port: ${listener_port}"
    LISTENER_ARN=$(aws elbv2 describe-listeners --load-balancer-arn "${LB_ARN}" --query "Listeners[?Port==\
${listener_port}\
].ListenerArn" --output text | tr -d '\r')

    if [ -n "$LISTENER_ARN" ] && [ "$LISTENER_ARN" != "None" ]; then
      echo "Found Listener ARN: $LISTENER_ARN"
      import_resource "aws_lb_listener" "${resource_name}" "${LISTENER_ARN}"
    else
      echo "Listener on port '${listener_port}' not found for LB '${lb_name}', skipping import."
    fi
  else
    echo "Load Balancer '${lb_name}' not found, skipping listener import."
  fi
}

# --- Recursos a serem importados ---
import_resource "aws_dynamodb_table" "user_table" "Usuario-prod"
import_resource "aws_dynamodb_table" "campeonato_table" "SojogaBrTable-prod"
import_resource "aws_ecr_repository" "sojoga_backend_repo" "sojoga-backend-prod"

# Importa as Roles do IAM
import_resource "aws_iam_role" "ecs_task_execution_role" "ecs-task-execution-role-prod"
import_resource "aws_iam_role" "ecs_task_role" "ecs-task-role-prod"

import_sg "lb_sg" "lb-sg-sojoga-br-prod"
import_sg "ecs_service_sg" "ecs-service-sg-sojoga-br-prod"

# Importa as políticas do IAM pelo nome
import_iam_policy "jwt_secret_access" "jwt-secret-access-policy-prod"
import_iam_policy "dynamodb_access" "dynamodb-access-policy-prod"

# Para o Target Group
echo "Attempting to import Target Group..."
TG_ARN=$(aws elbv2 describe-target-groups --names tg-sojoga-br-prod --region sa-east-1 --query "TargetGroups[0].TargetGroupArn" --output text | tr -d '\r')
if [ -n "$TG_ARN" ] && [ "$TG_ARN" != "None" ]; then
  import_resource "aws_lb_target_group" "main" "$TG_ARN"
fi

# Para o Load Balancer
echo "Attempting to import Load Balancer..."
LB_ARN=$(aws elbv2 describe-load-balancers --names alb-sojoga-br-prod --region sa-east-1 --query "LoadBalancers[0].LoadBalancerArn" --output text | tr -d '\r')
if [ -n "$LB_ARN" ] && [ "$LB_ARN" != "None" ]; then
  import_resource "aws_lb" "main" "$LB_ARN"
fi

# Para o Cluster ECS - CORRIGIDO
echo "Attempting to import ECS Cluster..."
CLUSTER_NAME="sojoga-cluster-prod"
# A importação do cluster usa o NOME, não o ARN.
import_resource "aws_ecs_cluster" "sojoga_cluster" "${CLUSTER_NAME}"


# --- Lógica de Importação Idempotente para o Serviço ECS ---
echo "--- Handling ECS Service Import ---"
SERVICE_NAME="sojoga-backend-prod-service"
RESOURCE_ADDRESS="aws_ecs_service.main"
# O ID de importação para um serviço ECS é "nome-do-cluster/nome-do-serviço"
IMPORT_ID="${CLUSTER_NAME}/${SERVICE_NAME}"

# 1. Verifica se o serviço já está no estado do Terraform
echo "Checking if ECS Service is already in Terraform state..."
if "$TERRAFORM_EXEC_PATH" state list | grep -q "^${RESOURCE_ADDRESS}"; then
  echo "ECS Service '${SERVICE_NAME}' is already managed by Terraform. Skipping import."
else
  # 2. Se não estiver no estado, verifica se ele existe na AWS para poder importá-lo
  echo "ECS Service not in state. Checking if it exists in AWS..."
  SERVICE_STATUS=$(aws ecs describe-services --cluster "${CLUSTER_NAME}" --services "${SERVICE_NAME}" --query "services[0].status" --output text | tr -d '\r' || echo "NOT_FOUND")

  # 3. Importa apenas se o serviço estiver 'ACTIVE' ou 'DRAINING' (ou seja, existe de fato)
  if [ "$SERVICE_STATUS" != "NOT_FOUND" ] && [ "$SERVICE_STATUS" != "None" ]; then
    echo "Service found in AWS with status '${SERVICE_STATUS}'. Attempting to import..."
    import_resource "aws_ecs_service" "main" "${IMPORT_ID}"
  else
    echo "Service '${SERVICE_NAME}' not found in AWS or is inactive. Terraform will create it if necessary. Skipping import."
  fi
fi

# Importa o Listener do ALB
import_listener "http" "alb-sojoga-br-prod" 80

echo "--- Resource import script finished ---"