@echo off
echo Downloading gradle-wrapper.jar...

powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://github.com/gradle/gradle/raw/v9.0-milestone-1/gradle/wrapper/gradle-wrapper.jar' -OutFile 'gradle\wrapper\gradle-wrapper.jar' -UseBasicParsing}"

if exist "gradle\wrapper\gradle-wrapper.jar" (
    echo Download completed successfully!
    echo File size: 
    dir "gradle\wrapper\gradle-wrapper.jar"
) else (
    echo Download failed. Please try manual download.
    echo URL: https://github.com/gradle/gradle/raw/v9.0-milestone-1/gradle/wrapper/gradle-wrapper.jar
    echo Save to: gradle\wrapper\gradle-wrapper.jar
)

pause
