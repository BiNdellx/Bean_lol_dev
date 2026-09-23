package kr.loldiscordbot;

import kr.loldiscordbot.bot.BotListener;
import kr.loldiscordbot.config.BotConfig;
import kr.loldiscordbot.config.GuildSettingsStore;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) throws InterruptedException {
        BotConfig config = BotConfig.load();
        GuildSettingsStore settingsStore = new GuildSettingsStore();

        JDA jda = JDABuilder.createDefault(config.token())
                .addEventListeners(new BotListener(settingsStore))
                .build()
                .awaitReady();

        registerCommands(jda, config);

        System.out.printf("Bot connected as %s%n", jda.getSelfUser().getName());
    }

    private static void registerCommands(JDA jda, BotConfig config) {
        var setupCommand = Commands.slash(BotListener.SETUP_COMMAND, "내전방 프로필 설정을 관리합니다.")
                .addSubcommands(
                        new SubcommandData(
                                BotListener.SETUP_DEFAULT_ROLE_SUBCOMMAND,
                                "프로필 설정 완료 후 최종 지급할 접근 권한 역할을 지정합니다."
                        ).addOption(
                                OptionType.ROLE,
                                BotListener.DEFAULT_ROLE_OPTION,
                                "프로필 설정 완료 후 지급할 역할",
                                true
                        ),
                        new SubcommandData(
                                BotListener.SETUP_IDENTIFY_ROLE_SUBCOMMAND,
                                "라인/티어 식별 역할을 자동으로 생성하거나 기존 역할을 연결합니다."
                        ),
                        new SubcommandData(
                                BotListener.SETUP_MESSAGE_SUBCOMMAND,
                                "내전방 프로필 설정 메시지를 출력합니다."
                        )
                )
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));

        if (config.hasGuildId()) {
            Guild guild = jda.getGuildById(config.guildId());
            if (guild == null) {
                throw new IllegalStateException(
                        "DISCORD_GUILD_ID is set, but the bot cannot find that guild: " + config.guildId()
                );
            }

            guild.updateCommands()
                    .addCommands(setupCommand)
                    .queue(
                            ignored -> System.out.printf("Registered /%s in guild %s%n", BotListener.SETUP_COMMAND, guild.getName()),
                            error -> System.err.println("Failed to register guild command: " + error.getMessage())
                    );
            return;
        }

        jda.updateCommands()
                .addCommands(setupCommand)
                .queue(
                        ignored -> System.out.printf("Registered global /%s command%n", BotListener.SETUP_COMMAND),
                        error -> System.err.println("Failed to register global command: " + error.getMessage())
                );
    }
}
