# terraform/network.tf

# Cria uma nova VPC para isolar os recursos do projeto
resource "aws_vpc" "main" {
  cidr_block = "10.0.0.0/16"
  enable_dns_support   = true
  enable_dns_hostnames = true
  tags = {
    Name = "vpc-${var.project_name}-${var.environment}"
  }
}

# Cria duas subnets públicas em zonas de disponibilidade diferentes para alta disponibilidade
resource "aws_subnet" "public" {
  count = 2
  vpc_id            = aws_vpc.main.id
  cidr_block        = "10.0.${count.index + 1}.0/24"
  availability_zone = "sa-east-1${element(["a", "b"], count.index)}" # Garante AZs diferentes em São Paulo
  map_public_ip_on_launch = true
  tags = {
    Name = "subnet-public-${var.project_name}-${var.environment}-${count.index + 1}"
  }
}