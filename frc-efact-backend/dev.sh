#!/bin/bash

# Script de desarrollo rápido para FRC eFact Backend
# Uso: ./dev.sh [comando]

set -e

COMMAND=${1:-run}

case $COMMAND in
  run|start)
    echo "🚀 Iniciando aplicación en modo desarrollo..."
    ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
    ;;
    
  test)
    echo "🧪 Ejecutando tests..."
    ./mvnw test
    ;;
    
  test-coverage)
    echo "🧪 Ejecutando tests con cobertura..."
    ./mvnw test jacoco:report
    echo "📊 Reporte de cobertura: target/site/jacoco/index.html"
    ;;
    
  build)
    echo "🔨 Compilando aplicación..."
    ./mvnw clean package -DskipTests
    ;;
    
  build-prod)
    echo "🔨 Compilando para producción..."
    ./mvnw clean package -Dmaven.test.skip=true
    ;;
    
  clean)
    echo "🧹 Limpiando proyecto..."
    ./mvnw clean
    ;;
    
  flyway-info)
    echo "📋 Información de migraciones Flyway..."
    ./mvnw flyway:info
    ;;
    
  flyway-migrate)
    echo "🔄 Ejecutando migraciones Flyway..."
    ./mvnw flyway:migrate
    ;;
    
  flyway-clean)
    echo "⚠️  Limpiando base de datos (CUIDADO: elimina todos los datos)..."
    read -p "¿Estás seguro? (y/N): " -n 1 -r
    echo ""
    if [[ $REPLY =~ ^[Yy]$ ]]; then
      ./mvnw flyway:clean
      echo "✅ Base de datos limpiada"
    else
      echo "❌ Operación cancelada"
    fi
    ;;
    
  format)
    echo "✨ Formateando código..."
    ./mvnw spotless:apply 2>/dev/null || echo "⚠️  Spotless no configurado, saltando..."
    ;;
    
  deps)
    echo "📦 Actualizando dependencias..."
    ./mvnw dependency:resolve
    ;;
    
  help|*)
    echo "FRC eFact Backend - Scripts de Desarrollo"
    echo ""
    echo "Uso: ./dev.sh [comando]"
    echo ""
    echo "Comandos disponibles:"
    echo "  run, start       - Iniciar aplicación en modo desarrollo (default)"
    echo "  test             - Ejecutar tests unitarios"
    echo "  test-coverage    - Ejecutar tests con reporte de cobertura"
    echo "  build            - Compilar aplicación (sin tests)"
    echo "  build-prod       - Compilar para producción"
    echo "  clean            - Limpiar archivos compilados"
    echo "  flyway-info      - Ver estado de migraciones"
    echo "  flyway-migrate   - Ejecutar migraciones pendientes"
    echo "  flyway-clean     - Limpiar base de datos (⚠️  elimina datos)"
    echo "  format           - Formatear código"
    echo "  deps             - Actualizar dependencias"
    echo "  help             - Mostrar esta ayuda"
    echo ""
    echo "Ejemplos:"
    echo "  ./dev.sh              # Iniciar en modo desarrollo"
    echo "  ./dev.sh test         # Ejecutar tests"
    echo "  ./dev.sh build-prod   # Compilar para producción"
    ;;
esac
