# terraform/network.tf

# --- FONTES DE DADOS (Descobrindo a infraestrutura existente) ---

# Descobre a VPC padrão da conta.
data "aws_vpc" "main" {
  default = true
}

# Descobre as sub-redes públicas existentes na VPC.
data "aws_subnets" "public" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.main.id]
  }
}

# Correção Final: Descobre o Internet Gateway existente em vez de tentar criá-lo.
data "aws_internet_gateway" "gw" {
  filter {
    name   = "attachment.vpc-id"
    values = [data.aws_vpc.main.id]
  }
}

# --- RECURSOS (Criando a infraestrutura que falta) ---

# Cria uma nova tabela de rotas para garantir que o tráfego vá para a internet.
resource "aws_route_table" "public_rt" {
  vpc_id = data.aws_vpc.main.id

  # Adiciona uma rota que direciona o tráfego da internet (0.0.0.0/0) para o Internet Gateway descoberto.
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = data.aws_internet_gateway.gw.id
  }

  tags = {
    Name        = "sojoga-public-rt"
    Environment = "prod"
  }
}

# Associa a nova tabela de rotas a todas as sub-redes públicas encontradas.
resource "aws_route_table_association" "public_assoc" {
  count          = length(data.aws_subnets.public.ids)
  subnet_id      = element(data.aws_subnets.public.ids, count.index)
  route_table_id = aws_route_table.public_rt.id
}
