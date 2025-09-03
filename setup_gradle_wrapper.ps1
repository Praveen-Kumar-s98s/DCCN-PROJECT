# Try to download gradle wrapper from a different source
Write-Host "Attempting to download gradle wrapper from alternative source..."

$url = "https://services.gradle.org/distributions/gradle-8.5-bin.zip"
$gradleDir = "gradle-8.5"
$gradleZip = "gradle-8.5-bin.zip"

Write-Host "Downloading Gradle 8.5..."
try {
    Invoke-WebRequest -Uri $url -OutFile $gradleZip -UseBasicParsing
    Write-Host "Download completed!"
    
    Write-Host "Extracting Gradle..."
    Expand-Archive -Path $gradleZip -DestinationPath "." -Force
    
    Write-Host "Setting up gradle wrapper..."
    & ".\$gradleDir\bin\gradle.bat" wrapper --gradle-version 8.5
    
    Write-Host "Cleaning up..."
    Remove-Item $gradleZip -Force
    Remove-Item $gradleDir -Recurse -Force
    
    Write-Host "Gradle wrapper setup complete!"
} catch {
    Write-Host "Failed to download Gradle. Trying manual approach..."
}
