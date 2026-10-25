@echo off
call gradlew.bat :game-core:test :app:assembleDebug
if errorlevel 1 exit /b %errorlevel%
echo.
echo Debug APK: app\build\outputs\apk\debug\app-debug.apk
