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
WORKDIR /app

# Copia o JAR compilado do estágio de build
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]