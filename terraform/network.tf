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

# Descobre o Internet Gateway existente na VPC.
data "aws_internet_gateway" "gw" {
  filter {
    name   = "attachment.vpc-id"
    values = [data.aws_vpc.main.id]
  }
}

# Descobre a Tabela de Rotas Principal da VPC.
data "aws_route_table" "main_rt" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.main.id]
  }

  filter {
    name   = "association.main"
    values = ["true"]
  }
}

# --- RECURSO (Adicionando a rota para a internet) ---

# Garante que a rota para a internet exista na tabela de rotas principal.
resource "aws_route" "public_internet_access" {
  route_table_id         = data.aws_route_table.main_rt.id
  destination_cidr_block = "0.0.0.0/0"
  gateway_id             = data.aws_internet_gateway.gw.id
}
