#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="$PROJECT_ROOT/.env"
JAVA_21_HOME="/usr/lib/jvm/java-21-openjdk-arm64"

# Ubuntu ARM64 서버에서는 시스템 기본 Java 버전과 관계없이 JDK 21을 사용한다.
if [[ ! -x "$JAVA_21_HOME/bin/java" || ! -x "$JAVA_21_HOME/bin/javac" ]]; then
    echo "[ERROR] Java 21 JDK was not found at: $JAVA_21_HOME"
    echo "        Install it with: sudo apt install openjdk-21-jdk"
    exit 1
fi

export JAVA_HOME="$JAVA_21_HOME"
export PATH="$JAVA_HOME/bin:$PATH"

# Gradle toolchain도 동일한 JDK 21 경로만 사용하도록 강제한다.
export GRADLE_OPTS="${GRADLE_OPTS:-} -Dorg.gradle.java.installations.paths=$JAVA_HOME -Dorg.gradle.java.installations.auto-detect=false"

if [[ ! -f "$ENV_FILE" && -z "${BOT_TOKEN:-}" ]]; then
    echo "[ERROR] BOT_TOKEN is not configured."
    echo "        Create .env from .env.example or set BOT_TOKEN as an environment variable."
    exit 1
fi

echo "[JAVA] Using JAVA_HOME=$JAVA_HOME"
"$JAVA_HOME/bin/java" -version

"$PROJECT_ROOT/build.sh"

echo "[RUN] Starting Bean_lol_dev..."
exec "$PROJECT_ROOT/build/install/Bean_lol_dev/bin/Bean_lol_dev"
