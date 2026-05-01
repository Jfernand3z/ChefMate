# Etapa 1: Construcción
FROM node:20-alpine AS build
WORKDIR /app

# Instalar dependencias
COPY package.json package-lock.json* ./
RUN npm install

# Copiar el código y compilar
COPY . .
# Podemos pasar las variables de entorno para Vite durante el build
ARG VITE_API_URL
ARG VITE_RECAPTCHA_SITE_KEY
ENV VITE_API_URL=$VITE_API_URL
ENV VITE_RECAPTCHA_SITE_KEY=$VITE_RECAPTCHA_SITE_KEY

RUN npm run build

# Etapa 2: Servidor Web
FROM nginx:alpine

# Puerto por defecto, Nginx lo usará para hacer el bind
ENV PORT=80

# Copiar configuración dinámica de Nginx (usa envsubst en la imagen oficial)
COPY nginx.conf.template /etc/nginx/templates/default.conf.template

# Copiar los estáticos de React
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE $PORT

# El comando de la imagen base de nginx:alpine ya incluye la ejecución de envsubst 
# antes de lanzar nginx, por lo que convertirá la variable ${PORT} en default.conf
CMD ["nginx", "-g", "daemon off;"]
