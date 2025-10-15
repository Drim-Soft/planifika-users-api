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
