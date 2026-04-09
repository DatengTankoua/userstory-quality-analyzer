@echo off
setlocal

:: ── User Story Analyzer — Windows Launcher ──────────────────
set "JAR_NAME=userstory-analyzer-1.0-SNAPSHOT-jar-with-dependencies.jar"
set "LAUNCHER_DIR=%~dp0"

:: Check Java
java -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java not found. Please install JDK 21:
    echo   winget install EclipseAdoptium.Temurin.21.JDK
    pause
    exit /b 1
)

:: Check Java version >= 21
for /f "tokens=3" %%v in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVA_VER=%%v
)
set JAVA_VER=%JAVA_VER:"=%
for /f "delims=." %%m in ("%JAVA_VER%") do set JAVA_MAJOR=%%m
if %JAVA_MAJOR% LSS 21 (
    echo [ERROR] Java %JAVA_MAJOR% found, but Java 21+ is required.
    echo   winget install EclipseAdoptium.Temurin.21.JDK
    pause
    exit /b 1
)

:: Launch
echo Starting User Story Analyzer...
java --add-opens java.base/java.lang=ALL-UNNAMED ^
     --add-opens java.base/java.util=ALL-UNNAMED ^
     -jar "%LAUNCHER_DIR%%JAR_NAME%"

if errorlevel 1 (
    echo [ERROR] Application exited with an error.
    pause
)
endlocal
