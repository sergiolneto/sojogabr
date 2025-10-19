# Como o frontend é composto por arquivos estáticos (HTML, CSS, JS) que não precisam de um passo de "build",
# podemos usar uma imagem Nginx diretamente e pular todos o estágio de build com Node.js.
# Isso torna a construção da imagem muito mais rápida e simples.

# Estágio 1: Servir os arquivos estáticos com Nginx
FROM nginx:1.27-alpine

# O diretório 'public' contém todos os nossos arquivos estáticos.
# Copiamos o conteúdo de 'public' para o diretório padrão do Nginx onde ele serve os arquivos.
COPY ./public/ /usr/share/nginx/html/

# Copia a configuração customizada do Nginx.
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]