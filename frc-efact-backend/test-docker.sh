#!/bin/bash

# Script para probar el Dockerfile localmente antes de desplegar a Render

set -e

# Colores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Test de Dockerfile - FRC eFact Backend${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Verificar que Docker está instalado
if ! command -v docker &> /dev/null; then
    echo -e "${RED}Error: Docker no está instalado${NC}"
    echo "Instala Docker desde: https://docs.docker.com/get-docker/"
    exit 1
fi

echo -e "${GREEN}✓${NC} Docker está instalado"
echo ""

# Nombre de la imagen
IMAGE_NAME="frc-efact-backend"
CONTAINER_NAME="frc-efact-backend-test"

# Limpiar contenedores anteriores
echo -e "${YELLOW}Limpiando contenedores anteriores...${NC}"
docker rm -f $CONTAINER_NAME 2>/dev/null || true
echo ""

# Construir la imagen
echo -e "${YELLOW}Construyendo imagen Docker...${NC}"
echo "Esto puede tomar varios minutos la primera vez..."
echo ""

if docker build -t $IMAGE_NAME .; then
    echo ""
    echo -e "${GREEN}✓${NC} Imagen construida exitosamente"
else
    echo ""
    echo -e "${RED}✗${NC} Error al construir la imagen"
    exit 1
fi

echo ""
echo -e "${YELLOW}Información de la imagen:${NC}"
docker images $IMAGE_NAME

echo ""
echo -e "${YELLOW}Iniciando contenedor...${NC}"
echo ""

# Variables de entorno para testing local
# NOTA: Ajusta estas variables según tu configuración local
DATABASE_URL="${DATABASE_URL:-jdbc:postgresql://host.docker.internal:5432/frc_efact_db?user=postgres&password=postgres}"
JWT_SECRET="${JWT_SECRET:-test-secret-key-for-local-testing-only-do-not-use-in-production}"

# Iniciar contenedor
if docker run -d \
    --name $CONTAINER_NAME \
    -p 8080:8080 \
    -e DATABASE_URL="$DATABASE_URL" \
    -e JWT_SECRET="$JWT_SECRET" \
    -e JWT_EXPIRATION=86400000 \
    -e SPRING_PROFILES_ACTIVE=dev \
    -e LOG_LEVEL=INFO \
    $IMAGE_NAME; then
    
    echo -e "${GREEN}✓${NC} Contenedor iniciado"
else
    echo -e "${RED}✗${NC} Error al iniciar contenedor"
    exit 1
fi

echo ""
echo -e "${YELLOW}Esperando que la aplicación inicie...${NC}"
echo "Esto puede tomar 30-60 segundos..."
echo ""

# Esperar a que la aplicación inicie
MAX_ATTEMPTS=30
ATTEMPT=0

while [ $ATTEMPT -lt $MAX_ATTEMPTS ]; do
    if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo -e "${GREEN}✓${NC} Aplicación iniciada correctamente"
        break
    fi
    
    ATTEMPT=$((ATTEMPT + 1))
    echo -n "."
    sleep 2
done

echo ""

if [ $ATTEMPT -eq $MAX_ATTEMPTS ]; then
    echo -e "${RED}✗${NC} Timeout esperando que la aplicación inicie"
    echo ""
    echo "Logs del contenedor:"
    docker logs $CONTAINER_NAME
    echo ""
    echo "Limpiando..."
    docker rm -f $CONTAINER_NAME
    exit 1
fi

echo ""
echo -e "${YELLOW}Probando endpoints...${NC}"
echo ""

# Test health check
echo -n "Health check: "
HEALTH_RESPONSE=$(curl -s http://localhost:8080/actuator/health)
if echo "$HEALTH_RESPONSE" | grep -q '"status":"UP"'; then
    echo -e "${GREEN}✓ PASS${NC}"
else
    echo -e "${RED}✗ FAIL${NC}"
    echo "Response: $HEALTH_RESPONSE"
fi

# Test login endpoint
echo -n "Login endpoint: "
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"Admin123!"}')

if echo "$LOGIN_RESPONSE" | grep -q '"token"'; then
    echo -e "${GREEN}✓ PASS${NC}"
else
    echo -e "${YELLOW}⚠ WARNING${NC} (puede fallar si la BD no tiene datos)"
    echo "Response: $LOGIN_RESPONSE"
fi

echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${GREEN}Test completado${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo "La aplicación está corriendo en: http://localhost:8080"
echo "Health check: http://localhost:8080/actuator/health"
echo "Swagger UI: http://localhost:8080/swagger-ui.html"
echo ""
echo "Para ver logs:"
echo "  docker logs -f $CONTAINER_NAME"
echo ""
echo "Para detener el contenedor:"
echo "  docker stop $CONTAINER_NAME"
echo ""
echo "Para eliminar el contenedor:"
echo "  docker rm -f $CONTAINER_NAME"
echo ""
echo -e "${GREEN}Si todo funciona aquí, debería funcionar en Render.${NC}"
echo ""
