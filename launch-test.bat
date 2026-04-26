@echo off
title Game Sync Test Launcher
cd /d "%~dp0"

echo ============================================
echo   Game Sync Test Launcher
echo ============================================
echo.

echo [1/3] Building mod with Gradle...
call gradlew build --no-daemon 2>&1 | findstr /C:"BUILD" /C:"FAILED" /C:"error" /C:"Error"
if errorlevel 1 (
    echo.
    echo [BUILD FAILED] Check errors above.
    pause
    exit /b 1
)
echo [OK] Build succeeded
echo.

echo [2/3] Starting WebSocket relay server (Node.js)...
where node >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Node.js is not installed or not in PATH
    echo Please install Node.js from https://nodejs.org/
    pause
    exit /b 1
)

if not exist "sync-stream-server\package.json" (
    echo [ERROR] package.json not found in sync-stream-server\
    pause
    exit /b 1
)

start "SyncStreamServer" cmd /k "cd /d "%~dp0sync-stream-server" && node server.js"

echo Waiting 3 seconds for server to start...
timeout /t 3 /nobreak >nul
echo.

echo [3/3] Starting Minecraft Client...
echo Open http://localhost:8080/ in your browser to view the stream
echo.
call gradlew runClient --no-daemon 2>&1

echo.
echo Minecraft closed. Press any key to exit...
pause >nul
