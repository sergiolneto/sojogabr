
# Obtém a VPC que está sendo usada (assumindo que foi criada manualmente ou por outro processo)
data "aws_vpc" "main" {
  default = true # Altere para `false` e use `id = "vpc-..."` se não for a VPC padrão
}

# Obtém as sub-redes públicas existentes na VPC
data "aws_subnets" "public" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.main.id]
  }
  # Adicione tags se precisar filtrar sub-redes específicas
  # tags = {
  #   Tier = "Public"
  # }
}

# 1. Cria o Internet Gateway
resource "aws_internet_gateway" "gw" {
  vpc_id = data.aws_vpc.main.id

  tags = {
    Name = "sojoga-igw"
    Environment = "prod"
  }
}

# 2. Cria uma nova tabela de rotas para a VPC
resource "aws_route_table" "public_rt" {
  vpc_id = data.aws_vpc.main.id

  # 3. Adiciona uma rota que direciona o tráfego da internet (0.0.0.0/0) para o Internet Gateway
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.gw.id
  }

  tags = {
    Name = "sojoga-public-rt"
    Environment = "prod"
  }
}

# 4. Associa a nova tabela de rotas a todas as sub-redes públicas encontradas
resource "aws_route_table_association" "public_assoc" {
  count          = length(data.aws_subnets.public.ids)
  subnet_id      = element(data.aws_subnets.public.ids, count.index)
  route_table_id = aws_route_table.public_rt.id
}

