package kr.loldiscordbot.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record BotConfig(String token, String guildId) {

    private static final Path ENV_FILE = Path.of(".env");

    public static BotConfig load() {
        Map<String, String> dotenv = loadDotEnv();

        // 실제 OS 환경변수가 있으면 .env보다 우선합니다.
        // Ubuntu 운영 환경에서는 BOT_TOKEN을 시스템 환경변수로 주입하는 방식도 그대로 사용할 수 있습니다.
        String token = requireValue("BOT_TOKEN", dotenv);
        String guildId = getValue("DISCORD_GUILD_ID", dotenv);

        return new BotConfig(
                token.trim(),
                guildId == null ? "" : guildId.trim()
        );
    }

    public boolean hasGuildId() {
        return !guildId.isBlank();
    }

    private static String requireValue(String name, Map<String, String> dotenv) {
        String value = getValue(name, dotenv);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Required configuration is missing: " + name
                            + ". Set it as an environment variable or in the project root .env file."
            );
        }
        return value;
    }

    private static String getValue(String name, Map<String, String> dotenv) {
        String systemValue = System.getenv(name);
        if (systemValue != null && !systemValue.isBlank()) {
            return systemValue;
        }
        return dotenv.get(name);
    }

    private static Map<String, String> loadDotEnv() {
        Map<String, String> values = new HashMap<>();

        if (!Files.exists(ENV_FILE)) {
            return values;
        }

        try {
            List<String> lines = Files.readAllLines(ENV_FILE, StandardCharsets.UTF_8);

            for (String rawLine : lines) {
                String line = rawLine.trim();

                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                int separator = line.indexOf('=');
                if (separator <= 0) {
                    continue;
                }

                String key = line.substring(0, separator).trim();
                String value = line.substring(separator + 1).trim();

                if ((value.startsWith("\"") && value.endsWith("\""))
                        || (value.startsWith("'") && value.endsWith("'"))) {
                    if (value.length() >= 2) {
                        value = value.substring(1, value.length() - 1);
                    }
                }

                values.put(key, value);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read .env file: " + ENV_FILE.toAbsolutePath(), e);
        }

        return values;
    }
}
