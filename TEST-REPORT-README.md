# 📊 Dashboard de Pruebas - Guía de Uso

Este proyecto incluye un generador automático de dashboards HTML para visualizar los resultados de las pruebas de manera clara y profesional.

## 🚀 Inicio Rápido

### Opción 1: Script Automático (Recomendado para Windows)

Ejecuta el script batch que ejecuta las pruebas y genera el dashboard automáticamente:

```bash
.\generate-report.bat
```

Este script:
1. ✅ Ejecuta las pruebas con Maven (`mvnw.cmd test`)
2. ✅ Genera el dashboard HTML automáticamente
3. ✅ Te ofrece abrir el dashboard en el navegador

### Opción 2: Manual (Paso a Paso)

#### Paso 1: Ejecutar las Pruebas

```bash
# Con Maven Wrapper (recomendado)
.\mvnw.cmd test

# O con Maven instalado
mvn test
```

#### Paso 2: Generar el Dashboard

```bash
python generate_test_report.py
```

#### Paso 3: Ver el Dashboard

**Método 1: Abrir directamente**
- Navega a la carpeta del proyecto
- Haz doble clic en `test-report.html`
- Se abrirá en tu navegador predeterminado

**Método 2: Servidor Web Local (Recomendado)**

```bash
# Inicia un servidor HTTP simple
python -m http.server 8000
```

Luego abre en tu navegador:
```
http://localhost:8000/test-report.html
```

Para detener el servidor, presiona `Ctrl+C` en la terminal.

## 📋 Requisitos

- **Python 3.6+** (solo biblioteca estándar, no requiere instalaciones adicionales)
- **Maven** o **Maven Wrapper** (incluido en el proyecto como `mvnw.cmd`)
- **Java 17+** (para ejecutar las pruebas)

## 🎨 Características del Dashboard

El dashboard HTML incluye:

- ✅ **Estadísticas Globales**: Total de pruebas, exitosas, fallidas, errores, omitidas
- 📊 **Gráfico de Progreso**: Barra visual con porcentaje de éxito
- 📋 **Suites de Pruebas**: Desglose detallado por cada suite
- 🔍 **Detalles de Errores**: Mensajes completos de errores y fallos
- ⏱️ **Tiempos de Ejecución**: Tiempo de cada prueba y total
- 🎯 **Interfaz Interactiva**: Click para expandir/colapsar suites
- 📱 **Diseño Responsive**: Se adapta a diferentes tamaños de pantalla

## 📁 Estructura de Archivos

```
planifika-users-api/
├── generate_test_report.py    # Script generador de dashboard
├── generate-report.bat        # Script automático para Windows
├── test-report.html           # Dashboard generado (se crea automáticamente)
└── target/
    └── surefire-reports/      # Reportes XML de Maven (se generan con mvn test)
        ├── TEST-*.xml
        └── ...
```

## 🔧 Solución de Problemas

### Error: "No se encontró el directorio target/surefire-reports"

**Solución**: Ejecuta primero las pruebas:
```bash
.\mvnw.cmd test
```

### Error: "No se encontraron reportes XML"

**Solución**: Verifica que las pruebas se ejecutaron correctamente. Revisa que existan archivos `TEST-*.xml` en `target/surefire-reports/`.

### Error: "python no se reconoce como comando"

**Solución**: 
- Asegúrate de tener Python instalado
- Verifica que Python esté en el PATH
- Prueba con `py` en lugar de `python`:
```bash
py generate_test_report.py
```

### El dashboard no se actualiza

**Solución**: 
- Asegúrate de ejecutar las pruebas nuevamente antes de generar el dashboard
- Elimina `test-report.html` y vuelve a generarlo
- Verifica que los archivos XML en `target/surefire-reports/` estén actualizados

## 📝 Personalización

### Cambiar el directorio de reportes

Edita `generate_test_report.py` y modifica:
```python
REPORTS_DIR = Path('target/surefire-reports')  # Cambia esta ruta
```

### Cambiar el archivo de salida

Edita `generate_test_report.py` y modifica:
```python
OUTPUT_FILE = Path('test-report.html')  # Cambia este nombre
```

## 🌐 Integración con CI/CD

Puedes integrar este script en tu pipeline de CI/CD:

```yaml
# Ejemplo para GitHub Actions
- name: Run Tests
  run: mvn test

- name: Generate Test Report
  run: python generate_test_report.py

- name: Upload Test Report
  uses: actions/upload-artifact@v2
  with:
    name: test-report
    path: test-report.html
```

## 📞 Soporte

Si encuentras algún problema o tienes sugerencias, por favor:
1. Verifica que todos los requisitos estén instalados
2. Revisa la sección de solución de problemas
3. Asegúrate de que las pruebas se ejecuten correctamente antes de generar el dashboard

---

**¡Disfruta de tu dashboard de pruebas! 🎉**

