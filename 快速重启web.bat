@echo off
echo Building mod...
cd /d "%~dp0"
call gradlew build

echo Killing existing node process...
taskkill /F /IM node.exe 2>nul

echo Starting web server...
cd /d "%~dp0web"
start "Web Server" cmd /c "node server.js"

echo Done!
