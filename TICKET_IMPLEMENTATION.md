# Ticket Support System Implementation

## Overview
This implementation adds a ticket support system for Planifika that stores ticket data in the Drimsoft database while keeping user data in the Planifika database.

## Database Architecture

### Planifika Database
- Contains user data (userplanifika table)
- Original database for the application

### Drimsoft Database
- Contains ticket support data (ticketsupport, ticketstatus tables)
- Contains Drimsoft user data (userdrimsoft table)
- Used for support ticket management

## Multi-Database Configuration

The application now uses two separate datasources:

1. **Planifika DataSource** (`PlanifikaDataSourceConfig.java`)
   - Repository package: `com.planifikausersapi.usersapi.repository.planifika`
   - Entity Manager: `planifikaEntityManagerFactory`
   - Transaction Manager: `planifikaTransactionManager`

2. **Drimsoft DataSource** (`DrimsoftDataSourceConfig.java`) - PRIMARY
   - Repository package: `com.planifikausersapi.usersapi.repository.drimsoft`
   - Entity Manager: `drimsoftEntityManagerFactory`
   - Transaction Manager: `drimsoftTransactionManager`

## New Components

### Models
- `TicketSupport.java` - Ticket entity
- `TicketStatus.java` - Ticket status entity
- `UserDrimsoft.java` - Drimsoft user entity

### DTOs
- `CreateTicketRequest.java` - Request to create a ticket
- `TicketResponse.java` - Ticket response with status name
- `UpdateTicketRequest.java` - Request to update a ticket

### Repositories
- `TicketSupportRepository.java` - Ticket data access
- `TicketStatusRepository.java` - Ticket status data access

### Service
- `TicketService.java` - Business logic for ticket management

### Controller
- `TicketController.java` - REST API endpoints for tickets

## API Endpoints

### Create Ticket
```
POST /api/v1/tickets
Body: {
  "idPlanifikaUser": 1,
  "title": "Issue title",
  "description": "Issue description"
}
```

### Get All Tickets
```
GET /api/v1/tickets
```

### Get Ticket by ID
```
GET /api/v1/tickets/{id}
```

### Get Tickets by User
```
GET /api/v1/tickets/user/{userId}
```

### Get Tickets by Status
```
GET /api/v1/tickets/status/{statusId}
```

### Update Ticket
```
PUT /api/v1/tickets/{id}
Body: {
  "idTicketStatus": 2,
  "answer": "Response to the ticket",
  "idDrimsoftUser": 1
}
```

### Delete Ticket
```
DELETE /api/v1/tickets/{id}
```

## Environment Variables

Add these to your `.env` file:

```
DRIMSOFT_DB_URL=jdbc:postgresql://aws-0-us-east-2.pooler.supabase.com:6543/postgres?sslmode=require
DRIMSOFT_DB_USER=postgres.zqbwjlrlnxjlrusmmciw
DRIMSOFT_DB_PASSWORD=Drimsoft2025.
```

## Features

1. **Automatic Status Assignment**: When a ticket is created, it automatically gets assigned a "PENDING" status
2. **Status Name in Response**: Ticket responses include both the status ID and status name
3. **Flexible Updates**: Tickets can be updated with new status, answers, and assigned Drimsoft users
4. **User Filtering**: Get tickets by Planifika user or by status
5. **Transaction Management**: All database operations use appropriate transaction managers

## Testing

Use the provided PowerShell scripts to test the API:

- `test-ticket-create.ps1` - Creates a sample ticket
- `test-ticket-get.ps1` - Retrieves tickets

## Security

The ticket endpoints are protected by the existing JWT authentication. Make sure to include a valid JWT token in the Authorization header when making requests.

## Notes

- The Drimsoft database connection uses the same port configuration as provided in the .env
- All ticket operations are transactional using the Drimsoft transaction manager
- The implementation follows the existing patterns in the codebase
- No emojis or special characters are used in the code
