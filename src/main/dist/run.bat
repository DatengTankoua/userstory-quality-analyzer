@echo off
setlocal EnableDelayedExpansion

:: ── User Story Analyzer — Windows Launcher ──────────────────
set "JAR_NAME=userstory-analyzer-1.0-SNAPSHOT-jar-with-dependencies.jar"
set "LAUNCHER_DIR=%~dp0"
set "JAVA_CMD=java"

:: ── Auto-detect JDK 21 in common install locations ──────────
:: Priority: JAVA_HOME (if set) → Eclipse Adoptium → Oracle → Liberica → Microsoft
set "JAVA_CMD="
for %%D in (
    "%JAVA_HOME%"
    "%ProgramFiles%\Eclipse Adoptium\jdk-21.0.5.11-hotspot"
    "%ProgramFiles%\Eclipse Adoptium\jdk-21.0.6.7-hotspot"
    "%ProgramFiles%\Eclipse Adoptium\jdk-21.0.3.9-hotspot"
    "%ProgramFiles%\Eclipse Adoptium\jdk-21.0.4.7-hotspot"
    "%ProgramFiles%\Microsoft\jdk-21.0.6.7-hotspot"
    "%ProgramFiles%\Microsoft\jdk-21.0.5.11-hotspot"
    "%ProgramFiles%\Java\jdk-21"
    "%ProgramFiles%\BellSoft\LibericaJDK-21-Full"
    "%ProgramFiles%\BellSoft\LibericaJDK-21"
) do (
    if "!JAVA_CMD!"=="" (
        if exist "%%~D\bin\java.exe" (
            set "JAVA_CMD=%%~D\bin\java.exe"
        )
    )
)

:: Fallback: search Eclipse Adoptium folder for any jdk-21* subfolder
if "!JAVA_CMD!"=="" (
    for /d %%D in ("%ProgramFiles%\Eclipse Adoptium\jdk-21*") do (
        if "!JAVA_CMD!"=="" (
            if exist "%%D\bin\java.exe" set "JAVA_CMD=%%D\bin\java.exe"
        )
    )
)

:: Fallback: use PATH java (may be wrong version)
if "!JAVA_CMD!"=="" set "JAVA_CMD=java"

:: Verify is at least Java 21
:: Use --version (no quotes in output): "openjdk 21.0.5 2024-10-15 LTS"
"!JAVA_CMD!" --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java 21 not found. Please install JDK 21:
    echo   winget install EclipseAdoptium.Temurin.21.JDK
    pause
    exit /b 1
)

set "JAVA_MAJOR=0"
for /f "tokens=2" %%v in ('"!JAVA_CMD!" --version 2^>^&1') do (
    if "!JAVA_MAJOR!"=="0" (
        for /f "delims=." %%m in ("%%v") do set JAVA_MAJOR=%%m
    )
)
if !JAVA_MAJOR! LSS 21 (
    echo [ERROR] Java !JAVA_MAJOR! found at !JAVA_CMD!, but Java 21+ is required.
    echo   Please install JDK 21:  winget install EclipseAdoptium.Temurin.21.JDK
    echo   Then set JAVA_HOME to the JDK 21 directory and re-run.
    pause
    exit /b 1
)

:: Check JAR exists (catches "run from inside ZIP" mistake)
if not exist "%LAUNCHER_DIR%%JAR_NAME%" (
    echo [ERROR] JAR file not found:
    echo   %LAUNCHER_DIR%%JAR_NAME%
    echo.
    echo This usually means you ran run.bat directly from inside the ZIP archive.
    echo Please EXTRACT the ZIP first, then run run.bat from the extracted folder.
    echo.
    echo How to extract: Right-click the ZIP ^> "Extract All..." ^> choose a folder ^> click Extract
    pause
    exit /b 1
)

:: Launch
echo Starting User Story Analyzer (Java !JAVA_MAJOR!)...
"!JAVA_CMD!" --add-opens java.base/java.lang=ALL-UNNAMED ^
             --add-opens java.base/java.util=ALL-UNNAMED ^
             --add-opens java.base/java.io=ALL-UNNAMED ^
             --add-exports javafx.graphics/com.sun.javafx.application=ALL-UNNAMED ^
             --add-exports javafx.base/com.sun.javafx=ALL-UNNAMED ^
             -jar "%LAUNCHER_DIR%%JAR_NAME%"

if errorlevel 1 (
    echo [ERROR] Application exited with an error.
    pause
)
endlocal
