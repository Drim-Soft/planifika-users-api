# Planifika Users API

API para la gestión de usuarios y tickets de soporte en Planifika.

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

### Usuarios
- `GET /users` - Listar usuarios
- `POST /users` - Crear usuario
- `GET /users/{id}` - Obtener usuario por ID
- `PUT /users/{id}` - Actualizar usuario
- `PATCH /users/{id}/status/{status}` - Actualizar estado de usuario
- `DELETE /users/{id}` - Eliminar usuario

### Tickets de Soporte
- `POST /tickets` - Crear un nuevo ticket
- `GET /tickets` - Listar todos los tickets
- `GET /tickets/{id}` - Obtener ticket por ID
- `GET /tickets/user/{userId}` - Obtener tickets por usuario de Planifika
- `GET /tickets/status/{statusId}` - Obtener tickets por estado
- `PUT /tickets/{id}` - Actualizar ticket (estado, respuesta, usuario Drimsoft)
- `DELETE /tickets/{id}` - Eliminar ticket

## Configuración de Bases de Datos

Este proyecto utiliza dos bases de datos:

1. **Planifika Database**: Para la gestión de usuarios de Planifika
2. **Drimsoft Database**: Para la gestión de tickets de soporte

Asegurate de configurar ambas bases de datos en tu archivo `.env`. Puedes usar `.env.example` como referencia.

## Ejemplo de Creación de Ticket

```json
POST /api/v1/tickets
{
  "idPlanifikaUser": 1,
  "title": "Problema con la aplicación",
  "description": "No puedo acceder a mi cuenta"
}
```

## Ejemplo de Actualización de Ticket

```json
PUT /api/v1/tickets/{id}
{
  "idTicketStatus": 2,
  "answer": "Hemos resuelto el problema, intenta iniciar sesión nuevamente",
  "idDrimsoftUser": 1
}
```

