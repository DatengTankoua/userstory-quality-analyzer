#!/usr/bin/env bash
# ── User Story Analyzer — macOS / Linux Launcher ────────────

JAR_NAME="userstory-analyzer-1.0-SNAPSHOT-jar-with-dependencies.jar"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Check Java
if ! command -v java &> /dev/null; then
    echo "[ERROR] Java not found. Please install JDK 21:"
    echo "  macOS:  brew install --cask temurin@21"
    echo "  Linux:  sudo apt install temurin-21-jdk"
    exit 1
fi

# Check Java version >= 21
JAVA_MAJOR=$(java -version 2>&1 | head -1 | sed 's/.*version "\([0-9]*\).*/\1/')
if [ "$JAVA_MAJOR" -lt 21 ] 2>/dev/null; then
    echo "[ERROR] Java $JAVA_MAJOR found, but Java 21+ is required."
    exit 1
fi

echo "Starting User Story Analyzer..."
java --add-opens java.base/java.lang=ALL-UNNAMED \
     --add-opens java.base/java.util=ALL-UNNAMED \
     -jar "$SCRIPT_DIR/$JAR_NAME"
