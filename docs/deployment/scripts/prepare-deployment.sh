#!/bin/bash

# Script de Preparación para Deployment - FRC eFact
# Este script verifica que todo está listo para deployment a Render

set -e

# Colores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Contadores
CHECKS_PASSED=0
CHECKS_FAILED=0
WARNINGS=0

print_header() {
    echo ""
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}========================================${NC}"
}

print_check() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✓${NC} $2"
        ((CHECKS_PASSED++))
    else
        echo -e "${RED}✗${NC} $2"
        ((CHECKS_FAILED++))
    fi
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
    ((WARNINGS++))
}

print_info() {
    echo -e "${BLUE}ℹ${NC} $1"
}

# Inicio
clear
echo -e "${GREEN}╔════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║  Preparación para Deployment - Render ║${NC}"
echo -e "${GREEN}║         FRC eFact Webapp              ║${NC}"
echo -e "${GREEN}╔════════════════════════════════════════╗${NC}"
echo ""
echo "Este script verificará que tu proyecto está listo para deployment."
echo ""

# 1. Verificar estructura de proyecto
print_header "1. Verificación de Estructura de Proyecto"

if [ -d "frc-efact-backend" ]; then
    print_check 0 "Directorio backend existe"
else
    print_check 1 "Directorio backend no encontrado"
fi

if [ -d "frc-efact-frontend" ]; then
    print_check 0 "Directorio frontend existe"
else
    print_check 1 "Directorio frontend no encontrado"
fi

# 2. Verificar archivos de configuración
print_header "2. Verificación de Archivos de Configuración"

# Backend
if [ -f "frc-efact-backend/pom.xml" ]; then
    print_check 0 "Backend: pom.xml existe"
else
    print_check 1 "Backend: pom.xml no encontrado"
fi

if [ -f "frc-efact-backend/src/main/resources/application.yml" ]; then
    print_check 0 "Backend: application.yml existe"
else
    print_check 1 "Backend: application.yml no encontrado"
fi

if [ -f "frc-efact-backend/src/main/resources/application-prod.yml" ]; then
    print_check 0 "Backend: application-prod.yml existe"
else
    print_check 1 "Backend: application-prod.yml no encontrado"
fi

# Frontend
if [ -f "frc-efact-frontend/package.json" ]; then
    print_check 0 "Frontend: package.json existe"
else
    print_check 1 "Frontend: package.json no encontrado"
fi

if [ -f "frc-efact-frontend/angular.json" ]; then
    print_check 0 "Frontend: angular.json existe"
else
    print_check 1 "Frontend: angular.json no encontrado"
fi

if [ -f "frc-efact-frontend/src/environments/environment.prod.ts" ]; then
    print_check 0 "Frontend: environment.prod.ts existe"
else
    print_check 1 "Frontend: environment.prod.ts no encontrado"
fi

# 3. Verificar archivos de deployment
print_header "3. Verificación de Archivos de Deployment"

# Backend
if [ -f "frc-efact-backend/Dockerfile" ]; then
    print_check 0 "Backend: Dockerfile existe (recomendado para Render)"
else
    print_warning "Backend: Dockerfile no encontrado"
    print_info "Se recomienda usar Docker para deployment en Render"
fi

if [ -f "frc-efact-backend/system.properties" ]; then
    print_check 0 "Backend: system.properties existe"
else
    print_info "Backend: system.properties no encontrado (opcional si usas Docker)"
fi

# Frontend
if [ -f "frc-efact-frontend/src/_redirects" ]; then
    print_check 0 "Frontend: _redirects existe (necesario para SPA)"
else
    print_check 1 "Frontend: _redirects no encontrado"
    print_info "Crear archivo: frc-efact-frontend/src/_redirects con contenido: /*    /index.html   200"
fi

# 4. Verificar migraciones de base de datos
print_header "4. Verificación de Migraciones de Base de Datos"

MIGRATION_DIR="frc-efact-backend/src/main/resources/db/migration"
if [ -d "$MIGRATION_DIR" ]; then
    print_check 0 "Directorio de migraciones existe"
    
    MIGRATION_COUNT=$(ls -1 "$MIGRATION_DIR"/*.sql 2>/dev/null | wc -l)
    if [ $MIGRATION_COUNT -gt 0 ]; then
        print_check 0 "Encontradas $MIGRATION_COUNT migraciones"
        
        # Listar migraciones
        print_info "Migraciones encontradas:"
        ls -1 "$MIGRATION_DIR"/*.sql | while read file; do
            echo "    - $(basename $file)"
        done
    else
        print_check 1 "No se encontraron migraciones SQL"
    fi
else
    print_check 1 "Directorio de migraciones no encontrado"
fi

# 5. Verificar Git
print_header "5. Verificación de Git"

if [ -d ".git" ]; then
    print_check 0 "Repositorio Git inicializado"
    
    # Verificar si hay cambios sin commitear
    if git diff-index --quiet HEAD -- 2>/dev/null; then
        print_check 0 "No hay cambios sin commitear"
    else
        print_warning "Hay cambios sin commitear"
        print_info "Ejecuta: git status"
    fi
    
    # Verificar remote
    if git remote -v | grep -q "origin"; then
        print_check 0 "Remote 'origin' configurado"
        REMOTE_URL=$(git remote get-url origin)
        print_info "Remote URL: $REMOTE_URL"
    else
        print_warning "Remote 'origin' no configurado"
        print_info "Configura con: git remote add origin <URL>"
    fi
    
    # Verificar branch
    CURRENT_BRANCH=$(git branch --show-current)
    print_info "Branch actual: $CURRENT_BRANCH"
    if [ "$CURRENT_BRANCH" = "main" ] || [ "$CURRENT_BRANCH" = "master" ]; then
        print_check 0 "En branch principal"
    else
        print_warning "No estás en branch main/master"
    fi
else
    print_check 1 "Git no inicializado"
    print_info "Ejecuta: git init"
fi

# 6. Verificar archivos sensibles
print_header "6. Verificación de Seguridad"

# Verificar .gitignore
if [ -f ".gitignore" ]; then
    print_check 0 ".gitignore existe"
    
    # Verificar que .env está ignorado
    if grep -q "\.env" .gitignore; then
        print_check 0 ".env está en .gitignore"
    else
        print_warning ".env no está en .gitignore"
    fi
else
    print_check 1 ".gitignore no encontrado"
fi

# Buscar archivos .env
if find . -name ".env" -not -path "*/node_modules/*" | grep -q .; then
    print_warning "Archivos .env encontrados en el proyecto"
    print_info "Asegúrate de que no están en el repositorio"
    find . -name ".env" -not -path "*/node_modules/*" | while read file; do
        echo "    - $file"
    done
else
    print_check 0 "No se encontraron archivos .env"
fi

# 7. Verificar documentación
print_header "7. Verificación de Documentación"

DOCS=(
    "README.md"
    "DEPLOYMENT_GUIDE.md"
    "RENDER_DEPLOYMENT_GUIDE.md"
    "DEPLOYMENT_CHECKLIST.md"
    "frc-efact-backend/README.md"
    "frc-efact-frontend/README.md"
)

for doc in "${DOCS[@]}"; do
    if [ -f "$doc" ]; then
        print_check 0 "$doc existe"
    else
        print_warning "$doc no encontrado"
    fi
done

# 8. Verificar build local
print_header "8. Verificación de Build Local"

print_info "Verificando que el proyecto compila..."

# Backend
if [ -f "frc-efact-backend/mvnw" ]; then
    print_info "Compilando backend (esto puede tomar unos minutos)..."
    cd frc-efact-backend
    if ./mvnw clean package -DskipTests > /dev/null 2>&1; then
        print_check 0 "Backend compila correctamente"
    else
        print_check 1 "Backend no compila"
        print_info "Ejecuta manualmente: cd frc-efact-backend && ./mvnw clean package"
    fi
    cd ..
else
    print_warning "Maven wrapper no encontrado en backend"
fi

# Frontend
if [ -f "frc-efact-frontend/package.json" ]; then
    print_info "Verificando frontend..."
    cd frc-efact-frontend
    
    if [ -d "node_modules" ]; then
        print_check 0 "Frontend: node_modules existe"
    else
        print_warning "Frontend: node_modules no existe"
        print_info "Ejecuta: cd frc-efact-frontend && npm install"
    fi
    
    cd ..
else
    print_warning "package.json no encontrado en frontend"
fi

# 9. Generar JWT Secret
print_header "9. Generación de JWT Secret"

print_info "Generando JWT Secret seguro..."
JWT_SECRET=$(openssl rand -base64 64 | tr -d '\n')
echo ""
echo -e "${GREEN}JWT Secret generado:${NC}"
echo -e "${YELLOW}$JWT_SECRET${NC}"
echo ""
print_info "Guarda este secret de forma segura. Lo necesitarás para configurar Render."
echo ""

# 10. Resumen y próximos pasos
print_header "Resumen de Verificación"

TOTAL_CHECKS=$((CHECKS_PASSED + CHECKS_FAILED))
echo "Total de verificaciones: ${TOTAL_CHECKS}"
echo -e "${GREEN}Verificaciones exitosas: ${CHECKS_PASSED}${NC}"
echo -e "${RED}Verificaciones fallidas: ${CHECKS_FAILED}${NC}"
echo -e "${YELLOW}Advertencias: ${WARNINGS}${NC}"

echo ""

if [ $CHECKS_FAILED -eq 0 ]; then
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}✓ PROYECTO LISTO PARA DEPLOYMENT${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo ""
    echo "Tu proyecto está listo para ser desplegado en Render."
    echo ""
    echo -e "${BLUE}Próximos pasos:${NC}"
    echo "1. Sube tu código a GitHub (si no lo has hecho)"
    echo "2. Sigue la guía: RENDER_DEPLOYMENT_GUIDE.md"
    echo "3. Usa el checklist: DEPLOYMENT_CHECKLIST.md"
    echo ""
    echo -e "${YELLOW}Información importante para Render:${NC}"
    echo ""
    echo "Backend (Docker - Recomendado):"
    echo "  Runtime: Docker"
    echo "  Dockerfile Path: frc-efact-backend/Dockerfile"
    echo "  Docker Context: frc-efact-backend"
    echo ""
    echo "Backend (Java Buildpack - Alternativa):"
    echo "  Runtime: Java"
    echo "  Build Command: ./mvnw clean package -DskipTests"
    echo "  Start Command: java -Dserver.port=\$PORT -Dspring.profiles.active=prod -jar target/frc-efact-backend-*.jar"
    echo ""
    echo "Frontend Build Command:"
    echo "  npm ci && npm run build -- --configuration production"
    echo ""
    echo "Frontend Publish Directory:"
    echo "  dist/frc-efact-frontend/browser"
    echo ""
    echo -e "${YELLOW}JWT Secret (guárdalo de forma segura):${NC}"
    echo "$JWT_SECRET"
    echo ""
    exit 0
else
    echo -e "${RED}========================================${NC}"
    echo -e "${RED}✗ PROYECTO NO ESTÁ LISTO${NC}"
    echo -e "${RED}========================================${NC}"
    echo ""
    echo "Se encontraron ${CHECKS_FAILED} problema(s) que deben ser resueltos."
    echo "Por favor revisa los errores arriba y corrígelos antes de desplegar."
    echo ""
    if [ $WARNINGS -gt 0 ]; then
        echo "También hay ${WARNINGS} advertencia(s) que deberías revisar."
        echo ""
    fi
    exit 1
fi
