# Multi-stage build para optimizar el tamaño de la imagen

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Build arguments para credenciales de GitHub Packages
ARG GITHUB_USERNAME
ARG GITHUB_TOKEN

# También intentamos leer desde variables de entorno
ENV GITHUB_USERNAME=${GITHUB_USERNAME}
ENV GITHUB_TOKEN=${GITHUB_TOKEN}

# Copiar archivos de configuración de Maven
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Configurar Maven settings para GitHub Packages
RUN mkdir -p ~/.m2 && \
    USERNAME="${GITHUB_USERNAME:-}" && \
    TOKEN="${GITHUB_TOKEN:-}" && \
    if [ -n "$USERNAME" ] && [ -n "$TOKEN" ]; then \
        echo '<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd"><servers><server><id>github-jsifenlib</id><username>'"$USERNAME"'</username><password>'"$TOKEN"'</password></server></servers></settings>' > ~/.m2/settings.xml && \
        echo "✅ Maven settings configured for GitHub Packages"; \
    else \
        echo "⚠️  WARNING: GITHUB_USERNAME or GITHUB_TOKEN not available"; \
        echo "   Configure Docker Build Arguments in Render Dashboard:"; \
        echo "   Settings → Build & Deploy → Docker Build Arguments"; \
        echo "   GITHUB_USERNAME=GabFrank"; \
        echo "   GITHUB_TOKEN=your-token"; \
    fi

# Descargar dependencias (esto se cachea si pom.xml no cambia)
RUN mvn dependency:go-offline -B

# Copiar código fuente
COPY src ./src

# Compilar la aplicación
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Crear directorio de certificados ANTES de cambiar al usuario spring
RUN mkdir -p /app/certificates

# Crear usuario no-root para seguridad
RUN addgroup -S spring && adduser -S spring -G spring

# Asignar propiedad del directorio /app al usuario spring
RUN chown -R spring:spring /app

# Cambiar al usuario spring
USER spring:spring

# Copiar el JAR compilado desde el stage de build
COPY --from=build /app/target/*.jar app.jar

# Exponer puerto
EXPOSE 8080

# Variables de entorno por defecto
ENV SPRING_PROFILES_ACTIVE=prod

# Comando de inicio
ENTRYPOINT ["java", "-Dserver.port=${PORT:-8080}", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "-jar", "app.jar"]
