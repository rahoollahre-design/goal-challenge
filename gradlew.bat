@echo off
setlocal EnableExtensions
set "GRADLE_VERSION=9.6.0"
set "BOOT=%USERPROFILE%\.gradle\goalchallenge-bootstrap"
set "GRADLE_HOME=%BOOT%\gradle-%GRADLE_VERSION%"
set "ZIP=%BOOT%\gradle-%GRADLE_VERSION%-bin.zip"
if not exist "%GRADLE_HOME%\bin\gradle.bat" (
  if not exist "%BOOT%" mkdir "%BOOT%"
  echo Downloading Gradle %GRADLE_VERSION%...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$ProgressPreference='SilentlyContinue'; Invoke-WebRequest -UseBasicParsing 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '%ZIP%'; Expand-Archive -Force '%ZIP%' '%BOOT%'"
  if errorlevel 1 exit /b 1
)
call "%GRADLE_HOME%\bin\gradle.bat" %*
exit /b %ERRORLEVEL%
