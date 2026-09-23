#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
GRADLE_BIN="$($PROJECT_ROOT/scripts/gradle-bootstrap.sh)"

cd "$PROJECT_ROOT"
"$GRADLE_BIN" clean installDist

echo
printf '[OK] Build complete: %s\n' "$PROJECT_ROOT/build/install/LoLDiscordBot"
