# Test script for getting tickets from Planifika Users API

$baseUrl = "http://localhost:8080/api/v1"

# Get all tickets
Write-Host "Getting all tickets..." -ForegroundColor Cyan

try {
    $tickets = Invoke-RestMethod -Uri "$baseUrl/tickets" -Method Get
    
    Write-Host "Tickets retrieved successfully!" -ForegroundColor Green
    Write-Host "Total tickets: $($tickets.Count)" -ForegroundColor Yellow
    Write-Host ""
    
    foreach ($ticket in $tickets) {
        Write-Host "Ticket ID: $($ticket.idTickets)" -ForegroundColor Cyan
        Write-Host "  Title: $($ticket.title)" -ForegroundColor White
        Write-Host "  Status: $($ticket.ticketStatusName)" -ForegroundColor White
        Write-Host "  User ID: $($ticket.idPlanifikaUser)" -ForegroundColor White
        Write-Host ""
    }
    
} catch {
    Write-Host "Error getting tickets:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        Write-Host $_.ErrorDetails.Message -ForegroundColor Red
    }
}

# Get tickets by user
Write-Host "`nGetting tickets for user ID 1..." -ForegroundColor Cyan

try {
    $userTickets = Invoke-RestMethod -Uri "$baseUrl/tickets/user/1" -Method Get
    
    Write-Host "User tickets retrieved successfully!" -ForegroundColor Green
    Write-Host "Total tickets for user: $($userTickets.Count)" -ForegroundColor Yellow
    $userTickets | ConvertTo-Json -Depth 10 | Write-Host
    
} catch {
    Write-Host "Error getting user tickets:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
}
