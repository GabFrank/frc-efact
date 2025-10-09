#!/bin/bash

# Script de Verificación de Integración - FRC eFact
# Este script verifica la integración completa entre backend y frontend

set -e

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuración
BACKEND_URL="http://localhost:8080"
FRONTEND_URL="http://localhost:4200"
BACKEND_HEALTH="${BACKEND_URL}/actuator/health"
BACKEND_LOGIN="${BACKEND_URL}/api/auth/login"
BACKEND_PROFILE="${BACKEND_URL}/api/users/profile"

# Contadores
TESTS_PASSED=0
TESTS_FAILED=0

# Función para imprimir resultados
print_result() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✓ PASS${NC}: $2"
        ((TESTS_PASSED++))
    else
        echo -e "${RED}✗ FAIL${NC}: $2"
        ((TESTS_FAILED++))
    fi
}

# Función para imprimir sección
print_section() {
    echo ""
    echo -e "${YELLOW}========================================${NC}"
    echo -e "${YELLOW}$1${NC}"
    echo -e "${YELLOW}========================================${NC}"
}

# Inicio de verificación
echo -e "${GREEN}Iniciando Verificación de Integración FRC eFact${NC}"
echo "Fecha: $(date)"
echo ""

# 1. Verificar que el backend está corriendo
print_section "1. Verificación de Backend"

if curl -s -f "${BACKEND_HEALTH}" > /dev/null 2>&1; then
    print_result 0 "Backend está corriendo en ${BACKEND_URL}"
    
    # Verificar health check response
    HEALTH_STATUS=$(curl -s "${BACKEND_HEALTH}" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)
    if [ "$HEALTH_STATUS" = "UP" ]; then
        print_result 0 "Health check retorna status UP"
    else
        print_result 1 "Health check retorna status: ${HEALTH_STATUS}"
    fi
else
    print_result 1 "Backend no está accesible en ${BACKEND_URL}"
    echo -e "${RED}Error: Backend debe estar corriendo. Ejecuta: cd frc-efact-backend && ./dev.sh${NC}"
    exit 1
fi

# 2. Verificar endpoints de autenticación
print_section "2. Verificación de Endpoints de Autenticación"

# Test login con credenciales válidas
LOGIN_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${BACKEND_LOGIN}" \
    -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"Admin123!"}')

HTTP_CODE=$(echo "$LOGIN_RESPONSE" | tail -n1)
RESPONSE_BODY=$(echo "$LOGIN_RESPONSE" | sed '$d')

if [ "$HTTP_CODE" = "200" ]; then
    print_result 0 "Login con credenciales válidas retorna 200 OK"
    
    # Verificar que la respuesta contiene token
    if echo "$RESPONSE_BODY" | grep -q '"token"'; then
        print_result 0 "Respuesta de login contiene token JWT"
        TOKEN=$(echo "$RESPONSE_BODY" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
    else
        print_result 1 "Respuesta de login no contiene token"
    fi
    
    # Verificar que la respuesta contiene información del usuario
    if echo "$RESPONSE_BODY" | grep -q '"user"'; then
        print_result 0 "Respuesta de login contiene información del usuario"
    else
        print_result 1 "Respuesta de login no contiene información del usuario"
    fi
else
    print_result 1 "Login con credenciales válidas retorna código: ${HTTP_CODE}"
fi

# Test login con credenciales inválidas
INVALID_LOGIN_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${BACKEND_LOGIN}" \
    -H "Content-Type: application/json" \
    -d '{"username":"invalid","password":"wrong"}')

INVALID_HTTP_CODE=$(echo "$INVALID_LOGIN_RESPONSE" | tail -n1)

if [ "$INVALID_HTTP_CODE" = "401" ]; then
    print_result 0 "Login con credenciales inválidas retorna 401 Unauthorized"
else
    print_result 1 "Login con credenciales inválidas retorna código: ${INVALID_HTTP_CODE}"
fi

# 3. Verificar endpoints protegidos
print_section "3. Verificación de Endpoints Protegidos"

# Test acceso sin token
NO_AUTH_RESPONSE=$(curl -s -w "\n%{http_code}" "${BACKEND_PROFILE}")
NO_AUTH_CODE=$(echo "$NO_AUTH_RESPONSE" | tail -n1)

if [ "$NO_AUTH_CODE" = "401" ] || [ "$NO_AUTH_CODE" = "403" ]; then
    print_result 0 "Endpoint protegido sin token retorna 401/403"
else
    print_result 1 "Endpoint protegido sin token retorna código: ${NO_AUTH_CODE}"
fi

# Test acceso con token válido
if [ ! -z "$TOKEN" ]; then
    AUTH_RESPONSE=$(curl -s -w "\n%{http_code}" "${BACKEND_PROFILE}" \
        -H "Authorization: Bearer ${TOKEN}")
    AUTH_CODE=$(echo "$AUTH_RESPONSE" | tail -n1)
    
    if [ "$AUTH_CODE" = "200" ]; then
        print_result 0 "Endpoint protegido con token válido retorna 200 OK"
    else
        print_result 1 "Endpoint protegido con token válido retorna código: ${AUTH_CODE}"
    fi
fi

# 4. Verificar CORS
print_section "4. Verificación de CORS"

CORS_RESPONSE=$(curl -s -I -X OPTIONS "${BACKEND_LOGIN}" \
    -H "Origin: http://localhost:4200" \
    -H "Access-Control-Request-Method: POST")

if echo "$CORS_RESPONSE" | grep -q "Access-Control-Allow-Origin"; then
    print_result 0 "CORS headers presentes en respuesta"
else
    print_result 1 "CORS headers no encontrados"
fi

# 5. Verificar Frontend
print_section "5. Verificación de Frontend"

if curl -s -f "${FRONTEND_URL}" > /dev/null 2>&1; then
    print_result 0 "Frontend está corriendo en ${FRONTEND_URL}"
else
    print_result 1 "Frontend no está accesible en ${FRONTEND_URL}"
    echo -e "${YELLOW}Advertencia: Frontend debe estar corriendo. Ejecuta: cd frc-efact-frontend && ./dev.sh${NC}"
fi

# 6. Verificar estructura de archivos
print_section "6. Verificación de Estructura de Proyecto"

# Backend files
if [ -f "frc-efact-backend/pom.xml" ]; then
    print_result 0 "Backend: pom.xml existe"
else
    print_result 1 "Backend: pom.xml no encontrado"
fi

if [ -f "frc-efact-backend/src/main/resources/application.yml" ]; then
    print_result 0 "Backend: application.yml existe"
else
    print_result 1 "Backend: application.yml no encontrado"
fi

# Frontend files
if [ -f "frc-efact-frontend/package.json" ]; then
    print_result 0 "Frontend: package.json existe"
else
    print_result 1 "Frontend: package.json no encontrado"
fi

if [ -f "frc-efact-frontend/angular.json" ]; then
    print_result 0 "Frontend: angular.json existe"
else
    print_result 1 "Frontend: angular.json no encontrado"
fi

# 7. Verificar documentación
print_section "7. Verificación de Documentación"

DOCS=("README.md" "docs/README.md" "docs/deployment/render/README.md" "frc-efact-backend/README.md" "frc-efact-frontend/README.md")

for doc in "${DOCS[@]}"; do
    if [ -f "$doc" ]; then
        print_result 0 "Documentación: $doc existe"
    else
        print_result 1 "Documentación: $doc no encontrado"
    fi
done

# Resumen final
print_section "Resumen de Verificación"

TOTAL_TESTS=$((TESTS_PASSED + TESTS_FAILED))
echo "Total de pruebas: ${TOTAL_TESTS}"
echo -e "${GREEN}Pruebas exitosas: ${TESTS_PASSED}${NC}"
echo -e "${RED}Pruebas fallidas: ${TESTS_FAILED}${NC}"

if [ $TESTS_FAILED -eq 0 ]; then
    echo ""
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}✓ VERIFICACIÓN COMPLETA EXITOSA${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo ""
    echo "La integración entre backend y frontend está funcionando correctamente."
    echo "El proyecto está listo para deployment a producción."
    exit 0
else
    echo ""
    echo -e "${RED}========================================${NC}"
    echo -e "${RED}✗ VERIFICACIÓN INCOMPLETA${NC}"
    echo -e "${RED}========================================${NC}"
    echo ""
    echo "Se encontraron ${TESTS_FAILED} problema(s). Por favor revisa los errores arriba."
    echo "Consulta docs/troubleshooting/COMMON_ERRORS.md para más detalles."
    exit 1
fi
