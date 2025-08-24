# Usa uma imagem leve do Nginx
FROM nginx:alpine

# Remove a configuração padrão do Nginx
RUN rm /etc/nginx/conf.d/default.conf

# Copia a configuração personalizada do Nginx que faz o proxy para o backend
COPY nginx.conf /etc/nginx/conf.d/

# Copia os arquivos estáticos do frontend para o diretório do Nginx
COPY . /usr/share/nginx/html