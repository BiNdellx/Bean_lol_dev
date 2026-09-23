#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="$PROJECT_ROOT/.env"

if [[ ! -f "$ENV_FILE" ]]; then
    echo "[ERROR] .env file not found."
    echo "        cp .env.example .env"
    echo "        Then set BOT_TOKEN in .env"
    exit 1
fi

set -a
# shellcheck disable=SC1090
source "$ENV_FILE"
set +a

if [[ -z "${BOT_TOKEN:-}" || "$BOT_TOKEN" == "put_your_discord_bot_token_here" ]]; then
    echo "[ERROR] BOT_TOKEN is not configured in .env"
    exit 1
fi

"$PROJECT_ROOT/build.sh"

echo "[RUN] Starting LoLDiscordBot..."
exec "$PROJECT_ROOT/build/install/LoLDiscordBot/bin/LoLDiscordBot"
