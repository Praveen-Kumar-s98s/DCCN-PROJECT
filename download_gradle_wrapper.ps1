# Download Gradle Wrapper JAR
$url = "https://github.com/gradle/gradle/raw/v9.0-milestone-1/gradle/wrapper/gradle-wrapper.jar"
$output = "gradle\wrapper\gradle-wrapper.jar"

Write-Host "Downloading gradle-wrapper.jar..."
try {
    Invoke-WebRequest -Uri $url -OutFile $output -UseBasicParsing
    Write-Host "Download completed successfully!"
} catch {
    Write-Host "Download failed. Trying alternative method..."
    try {
        $webClient = New-Object System.Net.WebClient
        $webClient.DownloadFile($url, $output)
        Write-Host "Download completed successfully using WebClient!"
    } catch {
        Write-Host "Both download methods failed. Please download manually."
        Write-Host "URL: $url"
        Write-Host "Save to: $output"
    }
}
