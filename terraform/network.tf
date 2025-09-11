# terraform/network.tf

# --- RECURSOS DE REDE GERENCIADOS PELO TERRAFORM ---

# 1. Cria a VPC
resource "aws_vpc" "main" {
  cidr_block           = "10.0.0.0/16"
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = {
    Name        = "vpc-sojoga-${var.environment}"
    Environment = var.environment
    Project     = var.project_name
  }
}

# 2. Cria as Sub-redes Públicas
# Vamos criar 3 sub-redes em zonas de disponibilidade diferentes para alta disponibilidade.
resource "aws_subnet" "public" {
  count                   = 3
  vpc_id                  = aws_vpc.main.id
  cidr_block              = cidrsubnet(aws_vpc.main.cidr_block, 8, count.index)
  availability_zone       = "${data.aws_region.current.name}${element(["a", "b", "c"], count.index)}"
  map_public_ip_on_launch = true

  tags = {
    Name        = "subnet-public-${count.index}-${var.environment}"
    Environment = var.environment
    Project     = var.project_name
  }
}

# 3. Cria o Internet Gateway
resource "aws_internet_gateway" "gw" {
  vpc_id = aws_vpc.main.id

  tags = {
    Name        = "igw-sojoga-${var.environment}"
    Environment = var.environment
    Project     = var.project_name
  }
}

# 4. Cria a Tabela de Rotas
resource "aws_route_table" "public_rt" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.gw.id
  }

  tags = {
    Name        = "rt-public-sojoga-${var.environment}"
    Environment = var.environment
    Project     = var.project_name
  }
}

# 5. Associa a Tabela de Rotas às Sub-redes
resource "aws_route_table_association" "public_assoc" {
  count          = length(aws_subnet.public)
  subnet_id      = aws_subnet.public[count.index].id
  route_table_id = aws_route_table.public_rt.id
}

# --- FONTES DE DADOS ---

# Descobre a região atual para construir os nomes das Zonas de Disponibilidade
data "aws_region" "current" {}
