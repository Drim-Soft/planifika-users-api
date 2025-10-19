param(
    [string]$BaseUrl = "http://localhost:8080/api/v1",
    [Parameter(Mandatory = $true)][string]$Token,
    [string]$Name,
    [string]$Password
)

if (-not $Name -and -not $Password) {
    Write-Error "Provide -Name and/or -Password to update"
    exit 1
}

$body = @{}
if ($Name) { $body.name = $Name }
if ($Password) { $body.password = $Password }

$headers = @{ Authorization = "Bearer $Token" }

try {
    $response = Invoke-RestMethod -Method Patch -Uri "$BaseUrl/auth/me" -Headers $headers -ContentType "application/json" -Body ($body | ConvertTo-Json)
    $response | ConvertTo-Json -Depth 6
} catch {
    Write-Host "Request failed:" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $reader.BaseStream.Position = 0
        $reader.DiscardBufferedData()
        $errorBody = $reader.ReadToEnd()
        Write-Host $errorBody
    } else {
        Write-Host $_
    }
    exit 1
}
