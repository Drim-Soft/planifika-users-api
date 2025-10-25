# Test script for creating a ticket in Planifika Users API

$baseUrl = "http://localhost:8080/api/v1"

# Create a new ticket
Write-Host "Creating a new ticket..." -ForegroundColor Cyan

$ticketData = @{
    idPlanifikaUser = 1
    title = "Cannot access my account"
    description = "I am getting an error when trying to log in to my Planifika account. The error message says 'Invalid credentials' but I am sure my password is correct."
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/tickets" `
        -Method Post `
        -Body $ticketData `
        -ContentType "application/json"
    
    Write-Host "Ticket created successfully!" -ForegroundColor Green
    Write-Host "Ticket ID: $($response.idTickets)" -ForegroundColor Yellow
    Write-Host "Title: $($response.title)" -ForegroundColor Yellow
    Write-Host "Status: $($response.ticketStatusName)" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Full Response:" -ForegroundColor Cyan
    $response | ConvertTo-Json -Depth 10 | Write-Host
    
} catch {
    Write-Host "Error creating ticket:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        Write-Host $_.ErrorDetails.Message -ForegroundColor Red
    }
}
