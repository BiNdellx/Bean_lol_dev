package kr.loldiscordbot.bot;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;

public final class BotListener extends ListenerAdapter {

    public static final String LINE_COMMAND = "line";
    public static final String LANE_SELECT_ID = "lane-demo-select";

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals(LINE_COMMAND)) {
            return;
        }

        StringSelectMenu.Builder menu = StringSelectMenu.create(LANE_SELECT_ID)
                .setPlaceholder("플레이할 라인을 선택하세요")
                .setRequiredRange(1, 1);

        for (Lane lane : Lane.values()) {
            menu.addOption(lane.koreanName(), lane.value());
        }

        event.reply("**주 라인을 선택해주세요.**")
                .addComponents(ActionRow.of(menu.build()))
                .setEphemeral(true)
                .queue();
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        if (!event.getComponentId().equals(LANE_SELECT_ID)) {
            return;
        }

        Lane selectedLane = Lane.fromValue(event.getValues().get(0));

        event.reply("선택한 라인: **" + selectedLane.koreanName() + " (" + selectedLane.value() + ")**")
                .setEphemeral(true)
                .queue();
    }
}
