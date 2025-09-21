# terraform/modules/network/main.tf

# --- FONTES DE DADOS ---

# Procura por uma VPC existente com as tags correspondentes
data "aws_vpcs" "existing" {
  tags = {
    Name        = "vpc-sojoga-${var.environment}"
    Environment = var.environment
    Project     = var.project_name
  }
}

# Obtém as sub-redes da VPC existente, se ela for encontrada
data "aws_subnets" "existing_subnets" {
  count = length(data.aws_vpcs.existing.ids) > 0 ? 1 : 0

  filter {
    name   = "vpc-id"
    values = [data.aws_vpcs.existing.ids[0]]
  }
}

# Descobre a região atual para construir os nomes das Zonas de Disponibilidade
data "aws_region" "current" {}

# --- LÓGICA LOCAL ---

locals {
  # Verifica se a VPC já existe (se a lista de IDs retornada não está vazia)
  vpc_exists = length(data.aws_vpcs.existing.ids) > 0
  # Define o ID da VPC: usa o da existente ou o da que será criada
  vpc_id     = local.vpc_exists ? data.aws_vpcs.existing.ids[0] : aws_vpc.main[0].id
  # Define os IDs das sub-redes: usa as da VPC existente ou as que serão criadas
  subnet_ids = local.vpc_exists ? data.aws_subnets.existing_subnets[0].ids : aws_subnet.public[*].id
}

# --- RECURSOS DE REDE GERENCIADOS PELO TERRAFORM ---

# 1. Cria a VPC (somente se não existir uma com as tags correspondentes)
resource "aws_vpc" "main" {
  count                = local.vpc_exists ? 0 : 1
  cidr_block           = "10.0.0.0/16"
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = {
    Name        = "vpc-sojoga-${var.environment}"
    Environment = var.environment
    Project     = var.project_name
  }
}

# 2. Cria as Sub-redes Públicas (somente se a VPC for criada)
resource "aws_subnet" "public" {
  count                   = local.vpc_exists ? 0 : 3
  vpc_id                  = aws_vpc.main[0].id # Dependência explícita
  cidr_block              = cidrsubnet(aws_vpc.main[0].cidr_block, 8, count.index)
  availability_zone       = "${data.aws_region.current.id}${element(["a", "b", "c"], count.index)}"
  map_public_ip_on_launch = true

  tags = {
    Name        = "subnet-public-${count.index}-${var.environment}"
    Environment = var.environment
    Project     = var.project_name
  }
}

# 3. Cria o Internet Gateway (somente se a VPC for criada)
resource "aws_internet_gateway" "gw" {
  count  = local.vpc_exists ? 0 : 1
  vpc_id = aws_vpc.main[0].id # Dependência explícita

  tags = {
    Name        = "igw-sojoga-${var.environment}"
    Environment = var.environment
    Project     = var.project_name
  }
}

# 4. Cria a Tabela de Rotas (somente se a VPC for criada)
resource "aws_route_table" "public_rt" {
  count  = local.vpc_exists ? 0 : 1
  vpc_id = aws_vpc.main[0].id # Dependência explícita

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.gw[0].id
  }

  tags = {
    Name        = "rt-public-sojoga-${var.environment}"
    Environment = var.environment
    Project     = var.project_name
  }
}

# 5. Associa a Tabela de Rotas às Sub-redes (somente se a VPC for criada)
resource "aws_route_table_association" "public_assoc" {
  count          = local.vpc_exists ? 0 : 3
  subnet_id      = aws_subnet.public[count.index].id
  route_table_id = aws_route_table.public_rt[0].id
}
