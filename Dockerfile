# Estágio 1: Build da aplicação com Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copia o pom.xml para aproveitar o cache do Docker e baixar as dependências
# apenas quando o pom.xml for alterado.
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia o restante do código-fonte
COPY src ./src

# Compila o projeto e cria o pacote JAR
RUN mvn package -DskipTests

# Estágio 2: Cria a imagem final com o JRE para um tamanho menor
FROM eclipse-temurin:17-jre-alpine

# Cria um usuário e grupo específicos para a aplicação
RUN addgroup -S sojoga && adduser -S sojoga -G sojoga

WORKDIR /app

# Copia o JAR compilado do estágio de build
# Copia o JAR usando o nome final definido no pom.xml ('app.jar'),
# tornando o Dockerfile mais robusto e consistente com o build.
COPY --from=build /app/target/app.jar app.jar

# Define o novo usuário como proprietário do arquivo
RUN chown sojoga:sojoga app.jar

EXPOSE 8080
# Troca para o usuário não-root antes de iniciar a aplicação
USER sojoga
ENTRYPOINT ["java","-jar","app.jar"]