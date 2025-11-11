@echo off
REM Script para ejecutar pruebas y generar dashboard HTML
REM Windows Batch Script

echo ========================================
echo  Generador de Dashboard de Pruebas
echo ========================================
echo.

REM Verificar si Maven Wrapper existe
if exist "mvnw.cmd" (
    echo [1/2] Ejecutando pruebas con Maven Wrapper...
    call mvnw.cmd test
    if errorlevel 1 (
        echo.
        echo ❌ Error al ejecutar las pruebas
        pause
        exit /b 1
    )
) else if exist "mvn.cmd" (
    echo [1/2] Ejecutando pruebas con Maven...
    call mvn.cmd test
    if errorlevel 1 (
        echo.
        echo ❌ Error al ejecutar las pruebas
        pause
        exit /b 1
    )
) else (
    echo ❌ No se encontró Maven (mvnw.cmd o mvn.cmd)
    echo    Por favor, ejecuta manualmente: mvn test
    echo    Luego ejecuta: python generate_test_report.py
    pause
    exit /b 1
)

echo.
echo [2/2] Generando dashboard HTML...
python generate_test_report.py

if errorlevel 1 (
    echo.
    echo ❌ Error al generar el dashboard
    pause
    exit /b 1
)

echo.
echo ========================================
echo  ✅ Proceso completado exitosamente
echo ========================================
echo.
echo 📊 Para ver el dashboard:
echo    1. Abre test-report.html en tu navegador
echo    2. O ejecuta: python -m http.server 8000
echo       Luego abre: http://localhost:8000/test-report.html
echo.

REM Intentar abrir el archivo automáticamente
if exist "test-report.html" (
    echo ¿Deseas abrir el dashboard ahora? (S/N)
    set /p open="> "
    if /i "%open%"=="S" (
        start test-report.html
    )
)

pause

