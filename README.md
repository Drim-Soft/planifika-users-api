# Planifika Users API

API para la gestión de usuarios en Planifika.

## Requisitos

- Docker instalado
- Archivo `.env` con las variables de entorno necesarias

## Construcción de la imagen

```bash
docker build -t pl-users-api .
```

## Ejecución del contenedor

```bash
docker run --env-file .env -p 8080:8080 pl-users-api
```

## Endpoints principales

- `GET /users` - Listar usuarios
- `POST /users` - Crear usuario
- `GET /users/{id}` - Obtener usuario por ID

ejecutar sonar:
curl http://localhost:9000/api/authentication/validate -u "sqp_28616d39877f01c5a0881e04373636ba8bd43cd5:"

FUNCIONO ASÍ EN GIT BASH:
$mvn clean verify sonar:sonar \
  -Dsonar.projectKey=planfuapi \
  -Dsonar.projectName='planfuapi' \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.token=sqp_28616d39877f01c5a0881e04373636ba8bd43cd5