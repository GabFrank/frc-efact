#!/bin/bash

# Script de Validación de Producción - FRC eFact
# Este script valida que el deployment en producción está funcionando correctamente

set -e

# Colores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Contadores
TESTS_PASSED=0
TESTS_FAILED=0
WARNINGS=0

# URLs de producción (actualizar con tus URLs reales)
BACKEND_URL="${BACKEND_URL:-https://frc-efact-backend.onrender.com}"
FRONTEND_URL="${FRONTEND_URL:-https://frc-efact-frontend.onrender.com}"

print_header() {
    echo ""
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}========================================${NC}"
}

print_test() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✓ PASS${NC}: $2"
        ((TESTS_PASSED++))
    else
        echo -e "${RED}✗ FAIL${NC}: $2"
        ((TESTS_FAILED++))
    fi
}

print_warning() {
    echo -e "${YELLOW}⚠ WARNING${NC}: $1"
    ((WARNINGS++))
}

print_info() {
    echo -e "${BLUE}ℹ INFO${NC}: $1"
}

# Inicio
clear
echo -e "${GREEN}╔════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║   Validación de Producción - Render   ║${NC}"
echo -e "${GREEN}║         FRC eFact Webapp              ║${NC}"
echo -e "${GREEN}╚════════════════════════════════════════╝${NC}"
echo ""
echo "Este script validará que tu deployment en producción está funcionando."
echo ""
echo "URLs a validar:"
echo "  Backend:  $BACKEND_URL"
echo "  Frontend: $FRONTEND_URL"
echo ""

# Verificar que curl está instalado
if ! command -v curl &> /dev/null; then
    echo -e "${RED}Error: curl no está instalado${NC}"
    exit 1
fi

# 1. Validación de Backend
print_header "1. Validación de Backend"

# Health check
print_info "Verificando health check..."
HEALTH_RESPONSE=$(curl -s -w "\n%{http_code}" "${BACKEND_URL}/actuator/health" 2>/dev/null || echo "ERROR\n000")
HTTP_CODE=$(echo "$HEALTH_RESPONSE" | tail -n1)
RESPONSE_BODY=$(echo "$HEALTH_RESPONSE" | sed '$d')

if [ "$HTTP_CODE" = "200" ]; then
    print_test 0 "Health check responde con 200 OK"
    
    if echo "$RESPONSE_BODY" | grep -q '"status":"UP"'; then
        print_test 0 "Health check status es UP"
    else
        print_test 1 "Health check status no es UP: $RESPONSE_BODY"
    fi
else
    print_test 1 "Health check no responde (HTTP $HTTP_CODE)"
fi

# Test de login
print_info "Probando endpoint de login..."
LOGIN_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${BACKEND_URL}/api/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"admin123"}' 2>/dev/null || echo "ERROR\n000")

LOGIN_HTTP_CODE=$(echo "$LOGIN_RESPONSE" | tail -n1)
LOGIN_BODY=$(echo "$LOGIN_RESPONSE" | sed '$d')

if [ "$LOGIN_HTTP_CODE" = "200" ]; then
    print_test 0 "Login endpoint responde con 200 OK"
    
    if echo "$LOGIN_BODY" | grep -q '"token"'; then
        print_test 0 "Login retorna token JWT"
        TOKEN=$(echo "$LOGIN_BODY" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
    else
        print_test 1 "Login no retorna token"
    fi
    
    if echo "$LOGIN_BODY" | grep -q '"user"'; then
        print_test 0 "Login retorna información de usuario"
    else
        print_test 1 "Login no retorna información de usuario"
    fi
else
    print_test 1 "Login endpoint falla (HTTP $LOGIN_HTTP_CODE)"
fi

# Test de endpoint protegido
if [ ! -z "$TOKEN" ]; then
    print_info "Probando endpoint protegido..."
    PROFILE_RESPONSE=$(curl -s -w "\n%{http_code}" "${BACKEND_URL}/api/users/profile" \
        -H "Authorization: Bearer ${TOKEN}" 2>/dev/null || echo "ERROR\n000")
    
    PROFILE_HTTP_CODE=$(echo "$PROFILE_RESPONSE" | tail -n1)
    
    if [ "$PROFILE_HTTP_CODE" = "200" ]; then
        print_test 0 "Endpoint protegido con token válido responde 200 OK"
    else
        print_test 1 "Endpoint protegido falla (HTTP $PROFILE_HTTP_CODE)"
    fi
fi

# Test de endpoint sin autenticación
print_info "Verificando protección de endpoints..."
NO_AUTH_RESPONSE=$(curl -s -w "\n%{http_code}" "${BACKEND_URL}/api/users/profile" 2>/dev/null || echo "ERROR\n000")
NO_AUTH_CODE=$(echo "$NO_AUTH_RESPONSE" | tail -n1)

if [ "$NO_AUTH_CODE" = "401" ] || [ "$NO_AUTH_CODE" = "403" ]; then
    print_test 0 "Endpoint protegido sin token retorna 401/403"
else
    print_test 1 "Endpoint protegido sin token no está protegido (HTTP $NO_AUTH_CODE)"
fi

# 2. Validación de Frontend
print_header "2. Validación de Frontend"

# Verificar que el sitio carga
print_info "Verificando que el sitio carga..."
FRONTEND_RESPONSE=$(curl -s -w "\n%{http_code}" "${FRONTEND_URL}" 2>/dev/null || echo "ERROR\n000")
FRONTEND_HTTP_CODE=$(echo "$FRONTEND_RESPONSE" | tail -n1)
FRONTEND_BODY=$(echo "$FRONTEND_RESPONSE" | sed '$d')

if [ "$FRONTEND_HTTP_CODE" = "200" ]; then
    print_test 0 "Frontend responde con 200 OK"
    
    if echo "$FRONTEND_BODY" | grep -q "<app-root>"; then
        print_test 0 "Frontend contiene componente Angular"
    else
        print_warning "No se detectó componente Angular en el HTML"
    fi
else
    print_test 1 "Frontend no responde (HTTP $FRONTEND_HTTP_CODE)"
fi

# 3. Validación de HTTPS
print_header "3. Validación de HTTPS"

# Verificar que backend usa HTTPS
if echo "$BACKEND_URL" | grep -q "https://"; then
    print_test 0 "Backend usa HTTPS"
else
    print_test 1 "Backend no usa HTTPS"
fi

# Verificar que frontend usa HTTPS
if echo "$FRONTEND_URL" | grep -q "https://"; then
    print_test 0 "Frontend usa HTTPS"
else
    print_test 1 "Frontend no usa HTTPS"
fi

# 4. Validación de CORS
print_header "4. Validación de CORS"

print_info "Verificando headers CORS..."
CORS_RESPONSE=$(curl -s -I -X OPTIONS "${BACKEND_URL}/api/auth/login" \
    -H "Origin: ${FRONTEND_URL}" \
    -H "Access-Control-Request-Method: POST" 2>/dev/null || echo "")

if echo "$CORS_RESPONSE" | grep -qi "Access-Control-Allow-Origin"; then
    print_test 0 "CORS headers presentes"
else
    print_warning "CORS headers no detectados (puede ser normal si el servidor no responde a OPTIONS)"
fi

# 5. Validación de Headers de Seguridad
print_header "5. Validación de Headers de Seguridad"

print_info "Verificando headers de seguridad..."
SECURITY_HEADERS=$(curl -s -I "${BACKEND_URL}/actuator/health" 2>/dev/null || echo "")

if echo "$SECURITY_HEADERS" | grep -qi "Strict-Transport-Security"; then
    print_test 0 "Header HSTS presente"
else
    print_warning "Header HSTS no detectado"
fi

if echo "$SECURITY_HEADERS" | grep -qi "X-Content-Type-Options"; then
    print_test 0 "Header X-Content-Type-Options presente"
else
    print_warning "Header X-Content-Type-Options no detectado"
fi

if echo "$SECURITY_HEADERS" | grep -qi "X-Frame-Options"; then
    print_test 0 "Header X-Frame-Options presente"
else
    print_warning "Header X-Frame-Options no detectado"
fi

# 6. Validación de Performance
print_header "6. Validación de Performance"

print_info "Midiendo tiempos de respuesta..."

# Medir tiempo de health check
START_TIME=$(date +%s%N)
curl -s "${BACKEND_URL}/actuator/health" > /dev/null 2>&1
END_TIME=$(date +%s%N)
HEALTH_TIME=$(( (END_TIME - START_TIME) / 1000000 ))

if [ $HEALTH_TIME -lt 2000 ]; then
    print_test 0 "Health check responde en ${HEALTH_TIME}ms (< 2s)"
else
    print_warning "Health check responde en ${HEALTH_TIME}ms (> 2s)"
fi

# Medir tiempo de login
START_TIME=$(date +%s%N)
curl -s -X POST "${BACKEND_URL}/api/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"admin123"}' > /dev/null 2>&1
END_TIME=$(date +%s%N)
LOGIN_TIME=$(( (END_TIME - START_TIME) / 1000000 ))

if [ $LOGIN_TIME -lt 1000 ]; then
    print_test 0 "Login responde en ${LOGIN_TIME}ms (< 1s)"
else
    print_warning "Login responde en ${LOGIN_TIME}ms (> 1s)"
fi

# 7. Resumen
print_header "Resumen de Validación"

TOTAL_TESTS=$((TESTS_PASSED + TESTS_FAILED))
echo "Total de pruebas: ${TOTAL_TESTS}"
echo -e "${GREEN}Pruebas exitosas: ${TESTS_PASSED}${NC}"
echo -e "${RED}Pruebas fallidas: ${TESTS_FAILED}${NC}"
echo -e "${YELLOW}Advertencias: ${WARNINGS}${NC}"

echo ""

if [ $TESTS_FAILED -eq 0 ]; then
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}✓ VALIDACIÓN EXITOSA${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo ""
    echo "Tu deployment en producción está funcionando correctamente."
    echo ""
    echo -e "${BLUE}Próximos pasos:${NC}"
    echo "1. Realizar pruebas manuales completas (ver MANUAL_TESTING_GUIDE.md)"
    echo "2. Verificar logs en Render Dashboard"
    echo "3. Configurar monitoreo y alertas"
    echo "4. Documentar URLs de producción"
    echo ""
    
    if [ $WARNINGS -gt 0 ]; then
        echo -e "${YELLOW}Nota: Hay ${WARNINGS} advertencia(s) que deberías revisar.${NC}"
        echo ""
    fi
    
    exit 0
else
    echo -e "${RED}========================================${NC}"
    echo -e "${RED}✗ VALIDACIÓN FALLIDA${NC}"
    echo -e "${RED}========================================${NC}"
    echo ""
    echo "Se encontraron ${TESTS_FAILED} problema(s) crítico(s)."
    echo "Por favor revisa los errores arriba y corrígelos."
    echo ""
    echo -e "${BLUE}Recursos de ayuda:${NC}"
    echo "- TROUBLESHOOTING_GUIDE.md"
    echo "- PRODUCTION_VALIDATION.md"
    echo "- Logs en Render Dashboard"
    echo ""
    exit 1
fi
