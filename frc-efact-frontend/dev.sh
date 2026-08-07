#!/bin/bash

# Script de desarrollo rápido para FRC eFact Frontend
# Uso: ./dev.sh [comando]

set -e

# Verificar versión de Node.js
check_node_version() {
  if ! command -v node &> /dev/null; then
    echo "❌ Error: Node.js no está instalado"
    echo ""
    echo "Instalar Node.js 18+:"
    echo "  macOS: brew install node@18"
    echo "  Ubuntu: sudo apt-get install nodejs"
    echo "  O descargar desde: https://nodejs.org/"
    exit 1
  fi

  NODE_VERSION=$(node -v | cut -d'v' -f2 | cut -d'.' -f1)

  if [ "$NODE_VERSION" -lt 18 ]; then
    echo "❌ Error: Node.js versión 18+ requerida"
    echo "   Versión actual: $(node -v)"
    echo "   Versión mínima: v18.0.0"
    echo ""
    echo "Opciones para actualizar:"
    echo "  1. Usar nvm (recomendado):"
    echo "     nvm install 18"
    echo "     nvm use 18"
    echo ""
    echo "  2. Instalar manualmente:"
    echo "     macOS: brew install node@18"
    echo "     Ubuntu: sudo apt-get install nodejs"
    echo "     O descargar desde: https://nodejs.org/"
    # run nvm use 18
    nvm use 18
    echo "✅ Node.js $(node -v) detectado"
    echo "✅ Node.js $(node -v) detectado"
    exit 1
  fi

  echo "✅ Node.js $(node -v) detectado"
}

COMMAND=${1:-start}

case $COMMAND in
  start|run)
    check_node_version
    echo "🚀 Iniciando servidor de desarrollo..."
    npm start
    ;;

  start-open)
    check_node_version
    echo "🚀 Iniciando servidor y abriendo navegador..."
    npm run start:open
    ;;

  build)
    check_node_version
    echo "🔨 Compilando aplicación (desarrollo)..."
    npm run build:dev
    ;;

  build-prod)
    check_node_version
    echo "🔨 Compilando para producción..."
    npm run build:prod
    ;;

  test)
    check_node_version
    echo "🧪 Ejecutando tests..."
    npm test
    ;;

  test-ci)
    check_node_version
    echo "🧪 Ejecutando tests en modo CI..."
    npm run test:ci
    ;;

  test-coverage)
    check_node_version
    echo "🧪 Ejecutando tests con cobertura..."
    npm run test:coverage
    echo "📊 Reporte de cobertura: coverage/index.html"
    ;;

  lint)
    check_node_version
    echo "🔍 Ejecutando linter..."
    npm run lint
    ;;

  lint-fix)
    check_node_version
    echo "🔧 Corrigiendo problemas de linting..."
    npm run lint:fix
    ;;

  analyze)
    check_node_version
    echo "📊 Analizando tamaño del bundle..."
    npm run analyze
    ;;

  serve-dist)
    check_node_version
    echo "🌐 Sirviendo build de producción localmente..."
    npm run serve:dist
    ;;

  clean)
    check_node_version
    echo "🧹 Limpiando archivos generados..."
    npm run clean
    ;;

  clean-install)
    check_node_version
    echo "🧹 Limpiando e instalando dependencias..."
    npm run clean:install
    ;;

  install)
    check_node_version
    echo "📦 Instalando dependencias..."
    npm install
    ;;

  update)
    check_node_version
    echo "📦 Actualizando dependencias..."
    npm update
    ;;

  help|*)
    echo "FRC eFact Frontend - Scripts de Desarrollo"
    echo ""
    echo "Uso: ./dev.sh [comando]"
    echo ""
    echo "Comandos disponibles:"
    echo "  start, run       - Iniciar servidor de desarrollo (default)"
    echo "  start-open       - Iniciar servidor y abrir navegador"
    echo "  build            - Compilar aplicación (desarrollo)"
    echo "  build-prod       - Compilar para producción"
    echo "  test             - Ejecutar tests unitarios"
    echo "  test-ci          - Ejecutar tests en modo CI"
    echo "  test-coverage    - Ejecutar tests con cobertura"
    echo "  lint             - Ejecutar linter"
    echo "  lint-fix         - Corregir problemas de linting"
    echo "  analyze          - Analizar tamaño del bundle"
    echo "  serve-dist       - Servir build de producción localmente"
    echo "  clean            - Limpiar archivos generados"
    echo "  clean-install    - Limpiar e instalar dependencias"
    echo "  install          - Instalar dependencias"
    echo "  update           - Actualizar dependencias"
    echo "  help             - Mostrar esta ayuda"
    echo ""
    echo "Ejemplos:"
    echo "  ./dev.sh              # Iniciar servidor de desarrollo"
    echo "  ./dev.sh test         # Ejecutar tests"
    echo "  ./dev.sh build-prod   # Compilar para producción"
    echo ""
    echo "URLs útiles:"
    echo "  Desarrollo: http://localhost:4400"
    echo "  Backend API: http://localhost:8080/api"
    ;;
esac
