# SÃ³ Joga - Plataforma de Campeonatos e Eventos

Uma plataforma robusta para gerenciamento de campeonatos e eventos esportivos, construÃ­da com **Java 21**, **Spring Boot 3.3** e seguindo os princÃ­pios da **Arquitetura Hexagonal (Ports and Adapters)**.

## ðŸ’» Tecnologias e Conceitos

- **Java 21 & Spring Boot 3.3.1**
- **Arquitetura Hexagonal**: SeparaÃ§Ã£o clara entre a lÃ³gica de domÃ­nio (core) e as tecnologias externas (infraestrutura).
- **SeguranÃ§a**: AutenticaÃ§Ã£o e AutorizaÃ§Ã£o baseadas em **JWT (JSON Web Token)** com **Spring Security**.
- **Banco de Dados NoSQL**: **Amazon DynamoDB** para armazenamento escalÃ¡vel de usuÃ¡rios, eventos e partidas.
- **AWS SDK v2 (Enhanced Client)**: IntegraÃ§Ã£o otimizada com DynamoDB.
- **Mensageria/Eventos**: Suporte a processamento de eventos.
- **Infraestrutura como CÃ³digo**: Provisionamento automatizado via **Terraform**.
- **Desenvolvimento Local**: EmulaÃ§Ã£o de ambiente AWS utilizando **LocalStack** e **Docker**.

## ðŸ“ Architecture Layout (Hexagonal)

O projeto estÃ¡ organizado para garantir testabilidade e independÃªncia de framework:

- `domain`: ContÃ©m os modelos de negÃ³cio (`User`, `Event`, `Campeonato`), ports (interfaces de repositÃ³rio) e use cases.
- `application`: Implementa os serviÃ§os de aplicaÃ§Ã£o que orquestram os casos de uso.
- `infrastructure`: ContÃ©m as implementaÃ§Ãµes concretas dos adapters (DynamoDB, Security, JWT, Configuration).
- `api`: Camada de entrada (Controllers) que expÃµe os endpoints REST.

## ðŸ”🔑 Funcionalidades Principais

- **GestÃ£o de UsuÃ¡rios**: Cadastro, login e controle de permissÃµes (Roles: ADMIN, USER).
- **Sistema de Campeonatos**: CriaÃ§Ã£o e gerenciamento de torneios e partidas.
- **AutenticaÃ§Ã£o Segura**: Fluxo completo de emissÃ£o e validaÃ§Ã£o de tokens JWT.
- **InicializaÃ§Ã£o AutomÃ¡tica**: Seed de dados e criaÃ§Ã£o de tabelas DynamoDB via `DynamoDbInitializer`.

## ðŸ“ API Endpoints (Principais)

| MÃ©todo | Endpoint | DescriÃ§Ã£o |
| :--- | :--- | :--- |
| `POST` | `/api/auth/login` | Autentica usuÃ¡rio e retorna token JWT |
| `POST` | `/api/auth/register` | Registra um novo usuÃ¡rio no sistema |
| `POST` | `/api/campeonatos` | Cria um novo campeonato (Requer Auth) |
| `GET` | `/api/users/me` | Retorna os dados do usuÃ¡rio logado |

## ðŸ—ï¸ ExecuÃ§Ã£o do Ambiente

O projeto utiliza **Docker Compose** para orquestrar a aplicaÃ§Ã£o e o LocalStack.

```bash
# Para subir o ambiente completo (App + LocalStack)
docker-compose up --build
```

O **Terraform** serÃ¡ executado automaticamente pelo container `terraform-provisioner` para criar as tabelas necessÃ¡rias no LocalStack.

## âœ… Qualidade e Testes

- **JUnit 5 & Mockito**: Testes unitÃ¡rios e de comportamento.
- **Testcontainers**: Testes de integraÃ§Ã£o reais utilizando containers do LocalStack.
- **JaCoCo**: RelatÃ³rios de cobertura de cÃ³digo.

---
*Este README foi gerado automaticamente com base na anÃ¡lise tÃ©cnica do repositÃ³rio.*
