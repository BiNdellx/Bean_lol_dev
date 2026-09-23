package kr.loldiscordbot;

import kr.loldiscordbot.bot.BotListener;
import kr.loldiscordbot.config.BotConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) throws InterruptedException {
        BotConfig config = BotConfig.fromEnvironment();

        JDA jda = JDABuilder.createDefault(config.token())
                .addEventListeners(new BotListener())
                .build()
                .awaitReady();

        registerCommands(jda, config);

        System.out.printf("Bot connected as %s%n", jda.getSelfUser().getName());
    }

    private static void registerCommands(JDA jda, BotConfig config) {
        var lineCommand = Commands.slash(BotListener.LINE_COMMAND, "라인 선택 메뉴를 표시합니다.");

        if (config.hasGuildId()) {
            Guild guild = jda.getGuildById(config.guildId());
            if (guild == null) {
                throw new IllegalStateException(
                        "DISCORD_GUILD_ID is set, but the bot cannot find that guild: " + config.guildId()
                );
            }

            guild.updateCommands()
                    .addCommands(lineCommand)
                    .queue(
                            ignored -> System.out.printf("Registered /%s in guild %s%n", BotListener.LINE_COMMAND, guild.getName()),
                            error -> System.err.println("Failed to register guild command: " + error.getMessage())
                    );
            return;
        }

        jda.updateCommands()
                .addCommands(lineCommand)
                .queue(
                        ignored -> System.out.printf("Registered global /%s command%n", BotListener.LINE_COMMAND),
                        error -> System.err.println("Failed to register global command: " + error.getMessage())
                );
    }
}
