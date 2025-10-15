$body = @{
    name = "test"
    email = "test@test.com"
    password = "123456"
    photoUrl = $null
    userRole = 1
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/signup" -Method POST -ContentType "application/json" -Body $body
    Write-Host "Success! Response:"
    $response | ConvertTo-Json -Depth 10
} catch {
    Write-Host "Error: $($_.Exception.Message)"
    Write-Host "Response: $($_.Exception.Response)"
}
