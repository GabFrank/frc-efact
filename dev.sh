#!/bin/bash

# Script de desarrollo principal para FRC eFact
# Gestiona backend y frontend desde un solo lugar
# Uso: ./dev.sh [comando]

set -e

COMMAND=${1:-help}

case $COMMAND in
  backend)
    echo "🔧 Iniciando backend..."
    cd frc-efact-backend
    ./dev.sh ${2:-run}
    ;;
    
  frontend)
    echo "🎨 Iniciando frontend..."
    cd frc-efact-frontend
    ./dev.sh ${2:-start}
    cd ..
    ;;
    
  start-all)
    echo "🚀 Iniciando backend y frontend..."
    echo ""
    echo "💡 RECOMENDACIÓN: Usa dos terminales separadas (más simple)"
    echo "   Terminal 1: ./dev.sh backend"
    echo "   Terminal 2: ./dev.sh frontend"
    echo ""
    
    # Verificar si tmux está disponible
    if command -v tmux &> /dev/null; then
      read -p "¿Deseas usar tmux para gestionar ambos servicios? (y/N): " -n 1 -r
      echo ""
      if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "✅ Usando tmux para gestionar ambos servicios..."
        
        # Crear sesión tmux
        tmux new-session -d -s frc-efact
        
        # Backend en el primer panel
        tmux send-keys -t frc-efact "cd frc-efact-backend && ./dev.sh run" C-m
        
        # Dividir ventana y frontend en el segundo panel
        tmux split-window -h -t frc-efact
        tmux send-keys -t frc-efact "cd frc-efact-frontend && ./dev.sh start" C-m
        
        # Adjuntar a la sesión
        echo ""
        echo "✅ Servicios iniciados en tmux"
        echo "   Para salir: Ctrl+B, luego D (detach)"
        echo "   Para cerrar: tmux kill-session -t frc-efact"
        echo ""
        tmux attach-session -t frc-efact
      else
        echo ""
        echo "Abre dos terminales y ejecuta:"
        echo "  Terminal 1: ./dev.sh backend"
        echo "  Terminal 2: ./dev.sh frontend"
      fi
    else
      echo "ℹ️  tmux no está instalado (no es necesario)"
      echo ""
      echo "Abre dos terminales y ejecuta:"
      echo "  Terminal 1: ./dev.sh backend"
      echo "  Terminal 2: ./dev.sh frontend"
      echo ""
      echo "Si deseas instalar tmux (opcional):"
      echo "  macOS: brew install tmux"
      echo "  Ubuntu: sudo apt-get install tmux"
    fi
    ;;
    
  test-all)
    echo "🧪 Ejecutando todos los tests..."
    echo ""
    echo "📦 Backend tests..."
    cd frc-efact-backend
    ./dev.sh test
    cd ..
    echo ""
    echo "🎨 Frontend tests..."
    cd frc-efact-frontend
    ./dev.sh test-ci
    cd ..
    echo ""
    echo "✅ Todos los tests completados"
    ;;
    
  build-all)
    echo "🔨 Compilando backend y frontend..."
    echo ""
    echo "📦 Compilando backend..."
    cd frc-efact-backend
    ./dev.sh build-prod
    cd ..
    echo ""
    echo "🎨 Compilando frontend..."
    cd frc-efact-frontend
    ./dev.sh build-prod
    cd ..
    echo ""
    echo "✅ Compilación completada"
    ;;
    
  clean-all)
    echo "🧹 Limpiando backend y frontend..."
    echo ""
    echo "📦 Limpiando backend..."
    cd frc-efact-backend
    ./dev.sh clean
    cd ..
    echo ""
    echo "🎨 Limpiando frontend..."
    cd frc-efact-frontend
    ./dev.sh clean
    cd ..
    echo ""
    echo "✅ Limpieza completada"
    ;;
    
  install-all)
    echo "📦 Instalando dependencias..."
    echo ""
    echo "🎨 Instalando dependencias del frontend..."
    cd frc-efact-frontend
    npm install
    cd ..
    echo ""
    echo "✅ Dependencias instaladas"
    echo ""
    echo "ℹ️  El backend usa Maven, las dependencias se descargan automáticamente"
    ;;
    
  status)
    echo "📊 Estado de los servicios..."
    echo ""
    
    # Verificar backend
    if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
      echo "✅ Backend: RUNNING (http://localhost:8080)"
    else
      echo "❌ Backend: NOT RUNNING"
    fi
    
    # Verificar frontend
    if curl -s http://localhost:4200 > /dev/null 2>&1; then
      echo "✅ Frontend: RUNNING (http://localhost:4200)"
    else
      echo "❌ Frontend: NOT RUNNING"
    fi
    echo ""
    ;;
    
  help|*)
    echo "FRC eFact - Scripts de Desarrollo"
    echo ""
    echo "Uso: ./dev.sh [comando] [opciones]"
    echo ""
    echo "Comandos principales:"
    echo "  backend [cmd]    - Ejecutar comando en backend"
    echo "  frontend [cmd]   - Ejecutar comando en frontend"
    echo "  start-all        - Iniciar backend y frontend (opcional: usa tmux)"
    echo "  test-all         - Ejecutar todos los tests"
    echo "  build-all        - Compilar backend y frontend"
    echo "  clean-all        - Limpiar archivos generados"
    echo "  install-all      - Instalar dependencias"
    echo "  status           - Ver estado de los servicios"
    echo "  help             - Mostrar esta ayuda"
    echo ""
    echo "💡 Recomendación para desarrollo:"
    echo "  Abre dos terminales:"
    echo "    Terminal 1: ./dev.sh backend"
    echo "    Terminal 2: ./dev.sh frontend"
    echo ""
    echo "Ejemplos:"
    echo "  ./dev.sh backend run        # Iniciar backend"
    echo "  ./dev.sh frontend start     # Iniciar frontend"
    echo "  ./dev.sh test-all           # Ejecutar todos los tests"
    echo "  ./dev.sh status             # Ver estado de servicios"
    echo ""
    echo "Comandos de backend disponibles:"
    echo "  run, test, test-coverage, build, build-prod, clean,"
    echo "  flyway-info, flyway-migrate, format, deps"
    echo ""
    echo "Comandos de frontend disponibles:"
    echo "  start, build, build-prod, test, test-ci, test-coverage,"
    echo "  lint, lint-fix, analyze, clean"
    echo ""
    echo "URLs útiles:"
    echo "  Frontend:    http://localhost:4200"
    echo "  Backend API: http://localhost:8080/api"
    echo "  Swagger UI:  http://localhost:8080/swagger-ui.html"
    echo "  Health:      http://localhost:8080/actuator/health"
    echo ""
    echo "Para más información sobre comandos específicos:"
    echo "  cd frc-efact-backend && ./dev.sh help"
    echo "  cd frc-efact-frontend && ./dev.sh help"
    ;;
esac
