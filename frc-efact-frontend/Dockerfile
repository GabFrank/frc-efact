# Build multi-stage del frontend Angular para producción (VM Hetzner)
# Stage 1: Build
FROM node:20-alpine AS build
WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build:prod

# Stage 2: Runtime (nginx sirviendo la SPA)
FROM nginx:1.27-alpine
COPY --from=build /app/dist/frc-efact-frontend/browser /usr/share/nginx/html
COPY deploy/nginx-spa.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
