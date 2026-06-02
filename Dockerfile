# Estágio 1: Build da aplicação com Maven e JDK padrão
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copia todo o projeto para o container
COPY . .

# Garante permissão de execução para o Maven Wrapper
RUN chmod +x mvnw

# Compila a aplicação gerando o JAR padrão
RUN ./mvnw clean package -DskipTests

# Estágio 2: Imagem de execução com JRE
FROM eclipse-temurin:21-jre-alpine

# Instala o curl para o healthcheck
RUN apk add --no-cache curl

# Cria um usuário e grupo específicos para a aplicação
RUN addgroup -S sojoga && adduser -S sojoga -G sojoga

WORKDIR /app

# Copia o JAR gerado no estágio anterior
COPY --from=build /app/target/*.jar app.jar

# Define o novo usuário como proprietário do JAR
RUN chown sojoga:sojoga app.jar

EXPOSE 8787

# Troca para o usuário não-root antes de iniciar a aplicação
USER sojoga

# Inicia a aplicação usando a JVM
ENTRYPOINT ["java", "-jar", "app.jar"]
