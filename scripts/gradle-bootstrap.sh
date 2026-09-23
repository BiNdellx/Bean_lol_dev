#!/usr/bin/env bash
set -euo pipefail

GRADLE_VERSION="9.7.1"
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TOOLS_DIR="$PROJECT_ROOT/.gradle-dist"
GRADLE_HOME="$TOOLS_DIR/gradle-$GRADLE_VERSION"
ZIP_PATH="$TOOLS_DIR/gradle-$GRADLE_VERSION-bin.zip"
DOWNLOAD_URL="https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"

if [[ ! -x "$GRADLE_HOME/bin/gradle" ]]; then
    command -v curl >/dev/null 2>&1 || {
        echo "[ERROR] curl is required. Install it with: sudo apt install curl" >&2
        exit 1
    }
    command -v unzip >/dev/null 2>&1 || {
        echo "[ERROR] unzip is required. Install it with: sudo apt install unzip" >&2
        exit 1
    }

    mkdir -p "$TOOLS_DIR"
    echo "[SETUP] Downloading Gradle $GRADLE_VERSION..."
    curl --fail --location --retry 3 --output "$ZIP_PATH" "$DOWNLOAD_URL"
    unzip -q -o "$ZIP_PATH" -d "$TOOLS_DIR"
    rm -f "$ZIP_PATH"
fi

printf '%s\n' "$GRADLE_HOME/bin/gradle"
