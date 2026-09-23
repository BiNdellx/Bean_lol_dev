#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="$PROJECT_ROOT/.env"

if [[ ! -f "$ENV_FILE" && -z "${BOT_TOKEN:-}" ]]; then
    echo "[ERROR] BOT_TOKEN is not configured."
    echo "        Create .env from .env.example or set BOT_TOKEN as an environment variable."
    exit 1
fi

"$PROJECT_ROOT/build.sh"

echo "[RUN] Starting Bean_lol_dev..."
exec "$PROJECT_ROOT/build/install/Bean_lol_dev/bin/Bean_lol_dev"
