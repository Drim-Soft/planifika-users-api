#!/bin/bash
# Script para ejecutar pruebas y generar dashboard HTML
# Linux/Mac Shell Script

echo "========================================"
echo " Generador de Dashboard de Pruebas"
echo "========================================"
echo ""

# Verificar si Maven Wrapper existe
if [ -f "./mvnw" ]; then
    echo "[1/2] Ejecutando pruebas con Maven Wrapper..."
    ./mvnw test
    if [ $? -ne 0 ]; then
        echo ""
        echo "❌ Error al ejecutar las pruebas"
        exit 1
    fi
elif command -v mvn &> /dev/null; then
    echo "[1/2] Ejecutando pruebas con Maven..."
    mvn test
    if [ $? -ne 0 ]; then
        echo ""
        echo "❌ Error al ejecutar las pruebas"
        exit 1
    fi
else
    echo "❌ No se encontró Maven (mvnw o mvn)"
    echo "   Por favor, ejecuta manualmente: mvn test"
    echo "   Luego ejecuta: python3 generate_test_report.py"
    exit 1
fi

echo ""
echo "[2/2] Generando dashboard HTML..."

# Intentar con python3 primero, luego python
if command -v python3 &> /dev/null; then
    python3 generate_test_report.py
elif command -v python &> /dev/null; then
    python generate_test_report.py
else
    echo "❌ No se encontró Python"
    exit 1
fi

if [ $? -ne 0 ]; then
    echo ""
    echo "❌ Error al generar el dashboard"
    exit 1
fi

echo ""
echo "========================================"
echo " ✅ Proceso completado exitosamente"
echo "========================================"
echo ""
echo "📊 Para ver el dashboard:"
echo "   1. Abre test-report.html en tu navegador"
echo "   2. O ejecuta: python3 -m http.server 8000"
echo "      Luego abre: http://localhost:8000/test-report.html"
echo ""

# Intentar abrir el archivo automáticamente en sistemas con xdg-open
if [ -f "test-report.html" ]; then
    if command -v xdg-open &> /dev/null; then
        read -p "¿Deseas abrir el dashboard ahora? (s/n): " open
        if [ "$open" = "s" ] || [ "$open" = "S" ]; then
            xdg-open test-report.html
        fi
    elif [[ "$OSTYPE" == "darwin"* ]]; then
        read -p "¿Deseas abrir el dashboard ahora? (s/n): " open
        if [ "$open" = "s" ] || [ "$open" = "S" ]; then
            open test-report.html
        fi
    fi
fi

