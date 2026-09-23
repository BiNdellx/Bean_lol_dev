package kr.loldiscordbot.config;

public record BotConfig(String token, String guildId) {

    public static BotConfig fromEnvironment() {
        String token = requireEnvironmentVariable("BOT_TOKEN");
        String guildId = System.getenv("DISCORD_GUILD_ID");

        if (guildId != null) {
            guildId = guildId.trim();
        }

        return new BotConfig(token.trim(), guildId == null ? "" : guildId);
    }

    public boolean hasGuildId() {
        return !guildId.isBlank();
    }

    private static String requireEnvironmentVariable(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Required environment variable is missing: " + name);
        }
        return value;
    }
}
