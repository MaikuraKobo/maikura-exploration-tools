@echo off
setlocal
set GRADLE_VERSION=9.2.1
set GRADLE_DIR=%USERPROFILE%\.gradle\wrapper\dists\gradle-%GRADLE_VERSION%-bin\maikura-local\gradle-%GRADLE_VERSION%
set GRADLE_ZIP=%TEMP%\gradle-%GRADLE_VERSION%-bin.zip
if not exist "%GRADLE_DIR%\bin\gradle.bat" (
  echo Downloading Gradle %GRADLE_VERSION% directly...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "[Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '%GRADLE_ZIP%'"
  mkdir "%USERPROFILE%\.gradle\wrapper\dists\gradle-%GRADLE_VERSION%-bin\maikura-local" >nul 2>nul
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Force '%GRADLE_ZIP%' '%USERPROFILE%\.gradle\wrapper\dists\gradle-%GRADLE_VERSION%-bin\maikura-local'"
)
call "%GRADLE_DIR%\bin\gradle.bat" %*
