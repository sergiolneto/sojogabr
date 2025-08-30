# Estágio 1: Build da aplicação frontend (exemplo para uma aplicação Node.js)
# Esta etapa instala as dependências e gera os arquivos estáticos de produção.
FROM node:20-alpine AS build
WORKDIR /app
# Copia ambos os arquivos de manifesto. O package-lock.json é essencial para o 'npm ci'.
COPY package*.json ./
# Usa 'npm ci' para uma instalação mais rápida e determinística, ideal para builds.
RUN npm ci
COPY . .
RUN npm run build

# Estágio 2: Servir os arquivos estáticos com Nginx
# Esta etapa cria a imagem final, que é leve e segura.
FROM nginx:1.27-alpine

# Copia apenas os arquivos de produção gerados no estágio anterior.
COPY --from=build /app/build /usr/share/nginx/html

# Copia a configuração customizada do Nginx.
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]