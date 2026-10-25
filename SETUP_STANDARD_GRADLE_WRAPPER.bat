@echo off
setlocal

echo Generating the official Gradle 9.6.0 Wrapper files...
call gradlew.bat wrapper --gradle-version 9.6.0 --distribution-type bin
if errorlevel 1 exit /b %errorlevel%

rem Gradle recommends running wrapper a second time so the wrapper itself is fully updated.
call gradlew.bat wrapper --gradle-version 9.6.0 --distribution-type bin
if errorlevel 1 exit /b %errorlevel%

echo.
echo Standard Gradle Wrapper generated successfully.
echo Expected file: gradle\wrapper\gradle-wrapper.jar
endlocal
