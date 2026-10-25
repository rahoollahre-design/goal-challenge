@echo off
echo Configure GOALCHALLENGE_RELEASE_* in your Gradle user properties before Play upload.
call gradlew.bat :game-core:test :app:bundleRelease
if errorlevel 1 exit /b %errorlevel%
echo.
echo Bundle: app\build\outputs\bundle\release\app-release.aab
