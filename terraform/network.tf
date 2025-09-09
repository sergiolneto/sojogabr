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
# Não precisamos mais criar rotas ou associações, apenas precisamos saber que ela existe
# para que outros recursos (como as sub-redes) possam usá-la implicitamente.
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
