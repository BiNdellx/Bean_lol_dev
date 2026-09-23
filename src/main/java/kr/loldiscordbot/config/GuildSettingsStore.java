package kr.loldiscordbot.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.OptionalLong;
import java.util.Properties;

/**
 * 서버별 봇 설정을 properties 파일로 저장합니다.
 * 프로필 역할은 이름이 아니라 Discord Role ID로 보존합니다.
 */
public final class GuildSettingsStore {

    private static final Path SETTINGS_FILE = Path.of("data", "guild-settings.properties");
    private static final String PROFILE_ROLE_PREFIX = ".profileRole.";
    private static final String DEFAULT_ROLE_SUFFIX = ".defaultRoleId";
    private static final String PROFILE_ROLES_READY_SUFFIX = ".profileRolesReady";

    private final Properties properties = new Properties();

    public GuildSettingsStore() {
        load();
    }


    public synchronized void setDefaultRoleId(long guildId, long roleId) {
        properties.setProperty(
                Long.toUnsignedString(guildId) + DEFAULT_ROLE_SUFFIX,
                Long.toUnsignedString(roleId)
        );
        save();
    }

    public synchronized OptionalLong getDefaultRoleId(long guildId) {
        String raw = properties.getProperty(Long.toUnsignedString(guildId) + DEFAULT_ROLE_SUFFIX);
        if (raw == null || raw.isBlank()) {
            return OptionalLong.empty();
        }

        try {
            return OptionalLong.of(Long.parseUnsignedLong(raw));
        } catch (NumberFormatException e) {
            return OptionalLong.empty();
        }
    }

    public synchronized void setProfileRoleIds(long guildId, Map<String, Long> roleIds) {
        String guildPrefix = Long.toUnsignedString(guildId) + PROFILE_ROLE_PREFIX;
        properties.keySet().removeIf(key -> key.toString().startsWith(guildPrefix));

        for (Map.Entry<String, Long> entry : roleIds.entrySet()) {
            properties.setProperty(guildPrefix + entry.getKey(), Long.toUnsignedString(entry.getValue()));
        }

        properties.setProperty(
                Long.toUnsignedString(guildId) + PROFILE_ROLES_READY_SUFFIX,
                Boolean.TRUE.toString()
        );
        save();
    }

    public synchronized OptionalLong getProfileRoleId(long guildId, String roleKey) {
        String raw = properties.getProperty(
                Long.toUnsignedString(guildId) + PROFILE_ROLE_PREFIX + roleKey
        );

        if (raw == null || raw.isBlank()) {
            return OptionalLong.empty();
        }

        try {
            return OptionalLong.of(Long.parseUnsignedLong(raw));
        } catch (NumberFormatException e) {
            return OptionalLong.empty();
        }
    }

    public synchronized Map<String, Long> getProfileRoleIds(long guildId) {
        String guildPrefix = Long.toUnsignedString(guildId) + PROFILE_ROLE_PREFIX;
        Map<String, Long> result = new LinkedHashMap<>();

        for (String propertyName : properties.stringPropertyNames()) {
            if (!propertyName.startsWith(guildPrefix)) {
                continue;
            }

            String roleKey = propertyName.substring(guildPrefix.length());
            try {
                result.put(roleKey, Long.parseUnsignedLong(properties.getProperty(propertyName)));
            } catch (NumberFormatException ignored) {
                // 손상된 값은 무시하고 다음 /setup identify_role에서 다시 구성합니다.
            }
        }

        return result;
    }

    public synchronized boolean areProfileRolesConfigured(long guildId) {
        return Boolean.parseBoolean(properties.getProperty(
                Long.toUnsignedString(guildId) + PROFILE_ROLES_READY_SUFFIX,
                "false"
        ));
    }

    private void load() {
        if (!Files.exists(SETTINGS_FILE)) {
            return;
        }

        try (InputStream input = Files.newInputStream(SETTINGS_FILE)) {
            properties.load(input);
        } catch (IOException e) {
            throw new IllegalStateException("서버 설정 파일을 읽을 수 없습니다: " + SETTINGS_FILE, e);
        }
    }

    private void save() {
        try {
            Path parent = SETTINGS_FILE.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            try (OutputStream output = Files.newOutputStream(
                    SETTINGS_FILE,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            )) {
                properties.store(output, "Bean_lol_dev guild settings");
            }
        } catch (IOException e) {
            throw new IllegalStateException("서버 설정 파일을 저장할 수 없습니다: " + SETTINGS_FILE, e);
        }
    }
}
