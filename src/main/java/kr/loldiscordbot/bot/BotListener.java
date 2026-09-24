package kr.loldiscordbot.bot;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import kr.loldiscordbot.config.GuildSettingsStore;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.modals.Modal;

public final class BotListener extends ListenerAdapter {

    public static final String SETUP_COMMAND = "setup";
    public static final String RESET_PROFILE_COMMAND = "reset_profile";
    public static final String RESET_PROFILE_USER_OPTION = "user";
    public static final String SETUP_DEFAULT_ROLE_SUBCOMMAND = "default_role";
    public static final String SETUP_IDENTIFY_ROLE_SUBCOMMAND = "identify_role";
    public static final String DEFAULT_ROLE_OPTION = "role";
    public static final String SETUP_MESSAGE_SUBCOMMAND = "message";

    private static final String PROFILE_BUTTON_ID = "profile-setup-button";
    private static final String PROFILE_MODAL_ID = "profile-setup-modal";

    private static final String MAIN_LANE_SELECT_ID = "profile-main-lane-select";
    private static final String SUB_LANE_SELECT_ID = "profile-sub-lane-select";
    private static final String TIER_SELECT_ID = "profile-tier-select";

    private static final Color SETUP_COLOR = new Color(46, 204, 113);
    private static final Color MAIN_LANE_ROLE_COLOR = new Color(87, 242, 135);
    private static final Color SUB_LANE_ROLE_COLOR = new Color(88, 101, 242);

    private final GuildSettingsStore settingsStore;

    /**
     * 프로필 선택값 자체는 아직 DB에 저장하지 않고 현재 프로세스 메모리에만 보관합니다.
     */
    private final ConcurrentMap<Long, ProfileSelection> profileSelections = new ConcurrentHashMap<>();

    public BotListener(GuildSettingsStore settingsStore) {
        this.settingsStore = settingsStore;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals(SETUP_COMMAND) && !event.getName().equals(RESET_PROFILE_COMMAND)) {
            return;
        }

        if (!event.isFromGuild() || event.getGuild() == null || event.getMember() == null
                || !event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            event.reply("이 명령어는 서버 관리자만 사용할 수 있습니다.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        if (event.getName().equals(RESET_PROFILE_COMMAND)) {
            handleResetProfile(event);
            return;
        }

        String subcommand = event.getSubcommandName();
        if (SETUP_DEFAULT_ROLE_SUBCOMMAND.equals(subcommand)) {
            handleSetupDefaultRole(event);
            return;
        }

        if (SETUP_IDENTIFY_ROLE_SUBCOMMAND.equals(subcommand)) {
            handleSetupIdentifyRole(event);
            return;
        }

        if (SETUP_MESSAGE_SUBCOMMAND.equals(subcommand)) {
            handleSetupMessage(event);
            return;
        }

        event.reply("지원하지 않는 setup 명령입니다.")
                .setEphemeral(true)
                .queue();
    }


    private void handleResetProfile(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) {
            return;
        }

        var option = event.getOption(RESET_PROFILE_USER_OPTION);
        if (option == null) {
            event.reply("초기화할 유저를 선택해주세요.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        Member target = option.getAsMember();
        if (target == null) {
            event.reply("해당 유저를 현재 서버에서 찾을 수 없습니다.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        if (!guild.getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
            event.reply("봇에게 `역할 관리` 권한이 없습니다. 먼저 봇 권한을 확인해주세요.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        List<Role> rolesToRemove = new ArrayList<>();

        for (Role role : getConfiguredProfileRoles(guild)) {
            if (target.getRoles().contains(role) && guild.getSelfMember().canInteract(role)) {
                rolesToRemove.add(role);
            }
        }

        Role defaultRole = getConfiguredDefaultRole(guild);
        if (defaultRole != null
                && target.getRoles().contains(defaultRole)
                && guild.getSelfMember().canInteract(defaultRole)
                && !rolesToRemove.contains(defaultRole)) {
            rolesToRemove.add(defaultRole);
        }

        profileSelections.remove(target.getIdLong());

        if (rolesToRemove.isEmpty()) {
            event.reply(target.getAsMention() + " 유저에게 초기화할 프로필 역할이 없습니다.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        event.deferReply(true).queue(hook -> guild.modifyMemberRoles(target, List.of(), rolesToRemove).queue(
                success -> hook.editOriginal(
                                "✅ " + target.getAsMention() + " 유저의 내전방 프로필을 초기화했습니다.
"
                                        + "라인/티어 역할과 내전방 접근 역할이 제거되었습니다."
                        )
                        .queue(),
                error -> hook.editOriginal(
                                "프로필 초기화 중 오류가 발생했습니다.
오류: " + rootMessage(error)
                        )
                        .queue()
        ));
    }

    private void handleSetupDefaultRole(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) {
            return;
        }

        Member selfMember = guild.getSelfMember();
        if (!selfMember.hasPermission(Permission.MANAGE_ROLES)) {
            event.reply("봇에게 `역할 관리` 권한이 없습니다. 먼저 봇 권한을 확인해주세요.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        var option = event.getOption(DEFAULT_ROLE_OPTION);
        if (option == null) {
            event.reply("지급할 역할을 선택해주세요.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        Role role = option.getAsRole();
        if (role.isPublicRole()) {
            event.reply("`@everyone` 역할은 기본 접근 역할로 사용할 수 없습니다.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        if (role.isManaged()) {
            event.reply("Discord 또는 연동 서비스가 관리하는 역할은 사용할 수 없습니다.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        if (!selfMember.canInteract(role)) {
            event.reply("선택한 역할이 봇의 최고 역할보다 높거나 같습니다. 봇 역할을 더 위로 이동해주세요.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        settingsStore.setDefaultRoleId(guild.getIdLong(), role.getIdLong());
        event.reply("기본 접근 역할을 " + role.getAsMention() + "(으)로 설정했습니다.\n"
                        + "프로필 3개 항목을 모두 완료한 유저에게 이 역할이 추가로 지급됩니다.")
                .setEphemeral(true)
                .queue();
    }

    private void handleSetupIdentifyRole(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) {
            return;
        }

        Member selfMember = guild.getSelfMember();
        if (!selfMember.hasPermission(Permission.MANAGE_ROLES)) {
            event.reply("봇에게 `역할 관리` 권한이 없습니다. 먼저 봇 권한을 확인해주세요.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        event.deferReply(true).queue(hook -> {
            List<RoleSpec> specs = buildProfileRoleSpecs();
            Map<String, CompletableFuture<Role>> futures = new LinkedHashMap<>();

            for (RoleSpec spec : specs) {
                futures.put(spec.key(), ensureRole(guild, spec));
            }

            CompletableFuture<?>[] all = futures.values().toArray(CompletableFuture[]::new);
            CompletableFuture.allOf(all).whenComplete((ignored, error) -> {
                if (error != null) {
                    hook.editOriginal("프로필 역할 구성 중 오류가 발생했습니다.\n"
                                    + "봇 역할이 기존 프로필 역할보다 위에 있는지 확인한 뒤 다시 실행해주세요.\n"
                                    + "오류: " + rootMessage(error))
                            .queue();
                    return;
                }

                Map<String, Long> roleIds = new LinkedHashMap<>();
                for (Map.Entry<String, CompletableFuture<Role>> entry : futures.entrySet()) {
                    roleIds.put(entry.getKey(), entry.getValue().join().getIdLong());
                }

                settingsStore.setProfileRoleIds(guild.getIdLong(), roleIds);

                hook.editOriginal("프로필 역할 설정이 완료되었습니다.\n"
                                + "• 주라인 역할: `탑 1 / 정글 1 / 미드 1 / 원딜 1 / 서폿 1`\n"
                                + "• 부라인 역할: `탑 2 / 정글 2 / 미드 2 / 원딜 2 / 서폿 2`\n"
                                + "• 티어 역할: `아이언 ~ 챌린저`\n\n"
                                + "같은 이름의 역할이 이미 있으면 재사용하고 색상을 다시 적용했습니다.\n"
                                + "`/setup default_role` 설정까지 완료하면 `/setup message`를 사용할 수 있습니다.")
                        .queue();
            });
        });
    }

    private CompletableFuture<Role> ensureRole(Guild guild, RoleSpec spec) {
        List<Role> matches = guild.getRolesByName(spec.name(), false);
        if (!matches.isEmpty()) {
            Role role = matches.getFirst();

            if (role.isManaged()) {
                return CompletableFuture.failedFuture(
                        new IllegalStateException("관리형 역할은 사용할 수 없습니다: " + role.getName())
                );
            }

            if (!guild.getSelfMember().canInteract(role)) {
                return CompletableFuture.failedFuture(
                        new IllegalStateException("봇보다 높거나 같은 위치의 역할입니다: " + role.getName())
                );
            }

            return role.getManager()
                    .setColor(spec.color())
                    .submit()
                    .thenApply(ignored -> role);
        }

        return guild.createRole()
                .setName(spec.name())
                .setColor(spec.color())
                .setMentionable(false)
                .setHoisted(false)
                .submit();
    }

    private List<RoleSpec> buildProfileRoleSpecs() {
        List<RoleSpec> specs = new ArrayList<>();

        for (Lane lane : Lane.values()) {
            specs.add(new RoleSpec(
                    laneRoleKey(lane, 1),
                    lane.koreanName() + " 1",
                    MAIN_LANE_ROLE_COLOR
            ));
        }

        for (Lane lane : Lane.values()) {
            specs.add(new RoleSpec(
                    laneRoleKey(lane, 2),
                    lane.koreanName() + " 2",
                    SUB_LANE_ROLE_COLOR
            ));
        }

        for (Tier tier : Tier.values()) {
            specs.add(new RoleSpec(
                    tierRoleKey(tier),
                    tier.koreanName(),
                    tier.roleColor()
            ));
        }

        return specs;
    }

    private void handleSetupMessage(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) {
            return;
        }

        if (!hasCompleteRoleSetup(guild)) {
            event.reply("먼저 `/setup identify_role`과 `/setup default_role` 설정을 모두 완료해주세요.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        var embed = new EmbedBuilder()
                .setColor(SETUP_COLOR)
                .setTitle("내전방 프로필 설정")
                .setDescription("내전방 이용을 위해 아래 **3개 항목을 모두 설정**해주세요.\n"
                        + "주라인, 부라인, 티어 설정을 모두 완료해야 내전방을 이용할 수 있습니다.")
                .addField("⭐ 주라인", "가장 자주 플레이하는 포지션을 선택합니다.", false)
                .addField("🔁 부라인", "주라인과 다른 두 번째 포지션을 선택합니다.", false)
                .addField("🏆 티어", "현재 본인의 리그 오브 레전드 티어를 선택합니다.", false)
                .setFooter("Bean LoL • 내전방 프로필")
                .build();

        Button profileButton = Button.success(PROFILE_BUTTON_ID, "프로필 설정");

        event.replyEmbeds(embed)
                .addComponents(ActionRow.of(profileButton))
                .queue();
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (!event.getComponentId().equals(PROFILE_BUTTON_ID)) {
            return;
        }

        Guild guild = event.getGuild();
        if (guild == null) {
            event.reply("서버 안에서만 사용할 수 있습니다.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        if (!hasCompleteRoleSetup(guild)) {
            event.reply("현재 프로필 역할 구성이 완료되어 있지 않습니다. 관리자에게 문의해주세요.")
                    .setEphemeral(true)
                    .queue(hook -> hook.deleteOriginal().queueAfter(5, TimeUnit.SECONDS));
            return;
        }

        event.replyModal(createProfileModal()).queue();
    }

    /**
     * 주라인 -> 부라인 -> 티어 순서로 한 번에 설정하는 단일 Modal입니다.
     * Discord Modal은 열려 있는 상태에서 다른 Select의 선택값에 맞춰
     * 옵션을 실시간으로 변경할 수 없으므로, 주/부라인 중복은 제출 시 검증합니다.
     */
    private Modal createProfileModal() {
        StringSelectMenu.Builder mainLaneMenu = StringSelectMenu.create(MAIN_LANE_SELECT_ID)
                .setPlaceholder("주라인을 선택하세요")
                .setRequired(true)
                .setRequiredRange(1, 1);

        StringSelectMenu.Builder subLaneMenu = StringSelectMenu.create(SUB_LANE_SELECT_ID)
                .setPlaceholder("부라인을 선택하세요")
                .setRequired(true)
                .setRequiredRange(1, 1);

        for (Lane lane : Lane.values()) {
            mainLaneMenu.addOption(lane.koreanName(), lane.value());
            subLaneMenu.addOption(lane.koreanName(), lane.value());
        }

        StringSelectMenu.Builder tierMenu = StringSelectMenu.create(TIER_SELECT_ID)
                .setPlaceholder("티어를 선택하세요")
                .setRequired(true)
                .setRequiredRange(1, 1);

        for (Tier tier : Tier.values()) {
            tierMenu.addOption(tier.koreanName(), tier.value());
        }

        return Modal.create(PROFILE_MODAL_ID, "내전방 프로필 설정")
                .addComponents(
                        Label.of(
                                "주라인",
                                "가장 자주 플레이하는 라인을 선택해주세요.",
                                mainLaneMenu.build()
                        ),
                        Label.of(
                                "부라인",
                                "주라인과 다른 두 번째 라인을 선택해주세요.",
                                subLaneMenu.build()
                        ),
                        Label.of(
                                "티어",
                                "현재 본인의 리그 오브 레전드 티어를 선택해주세요.",
                                tierMenu.build()
                        )
                )
                .build();
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (!event.getModalId().equals(PROFILE_MODAL_ID)) {
            return;
        }

        Guild guild = event.getGuild();
        Member member = event.getMember();
        if (guild == null || member == null) {
            event.reply("서버 안에서만 프로필을 설정할 수 있습니다.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        Lane mainLane = Lane.fromValue(getSingleSelectedValue(event, MAIN_LANE_SELECT_ID));
        Lane subLane = Lane.fromValue(getSingleSelectedValue(event, SUB_LANE_SELECT_ID));
        Tier tier = Tier.fromValue(getSingleSelectedValue(event, TIER_SELECT_ID));

        if (mainLane == subLane) {
            event.reply("주라인과 부라인은 같은 라인으로 설정할 수 없습니다. 다시 선택해주세요.")
                    .setEphemeral(true)
                    .queue(hook -> hook.deleteOriginal().queueAfter(5, TimeUnit.SECONDS));
            return;
        }

        Role mainRole = getConfiguredRole(guild, laneRoleKey(mainLane, 1));
        Role subRole = getConfiguredRole(guild, laneRoleKey(subLane, 2));
        Role tierRole = getConfiguredRole(guild, tierRoleKey(tier));
        Role defaultRole = getConfiguredDefaultRole(guild);

        if (mainRole == null || subRole == null || tierRole == null || defaultRole == null) {
            event.reply("역할 구성이 손상되어 있습니다. 관리자에게 `/setup identify_role` 및 `/setup default_role` 설정 확인을 요청해주세요.")
                    .setEphemeral(true)
                    .queue(hook -> hook.deleteOriginal().queueAfter(5, TimeUnit.SECONDS));
            return;
        }

        ProfileSelection selection = new ProfileSelection(mainLane, subLane, tier);
        profileSelections.put(event.getUser().getIdLong(), selection);
        logSelection(event, selection);

        List<Role> targetRoles = List.of(mainRole, subRole, tierRole, defaultRole);
        List<Role> currentProfileRoles = getConfiguredProfileRoles(guild);

        List<Role> rolesToAdd = targetRoles.stream()
                .filter(role -> !member.getRoles().contains(role))
                .toList();

        List<Role> rolesToRemove = currentProfileRoles.stream()
                .filter(member.getRoles()::contains)
                .filter(role -> !targetRoles.contains(role))
                .toList();

        event.deferReply(true).queue(hook -> guild.modifyMemberRoles(member, rolesToAdd, rolesToRemove).queue(
                success -> {
                    System.out.printf(
                            "Profile roles updated: guild=%s, user=%s(%s), roles=[%s, %s, %s, %s]%n",
                            guild.getName(),
                            event.getUser().getName(),
                            event.getUser().getId(),
                            mainRole.getName(),
                            subRole.getName(),
                            tierRole.getName(),
                            defaultRole.getName()
                    );

                    hook.editOriginal(
                                    "✅ **내전방 프로필 설정이 완료되었습니다.**\n\n"
                                            + "• 주라인: **" + mainLane.koreanName() + "** (`" + mainRole.getName() + "`)\n"
                                            + "• 부라인: **" + subLane.koreanName() + "** (`" + subRole.getName() + "`)\n"
                                            + "• 티어: **" + tier.koreanName() + "**\n"
                                            + "이제 내전방을 이용할 수 있습니다."
                            )
                            .queue();
                },
                error -> {
                    System.err.printf(
                            "Failed to update profile roles for %s(%s): %s%n",
                            event.getUser().getName(),
                            event.getUser().getId(),
                            error.getMessage()
                    );

                    hook.editOriginal("프로필 역할 지급 중 오류가 발생했습니다. 관리자에게 문의해주세요.\n"
                                    + "오류: " + rootMessage(error))
                            .queue();
                }
        ));
    }

    private boolean hasCompleteRoleSetup(Guild guild) {
        if (!settingsStore.areProfileRolesConfigured(guild.getIdLong())) {
            return false;
        }

        Role defaultRole = getConfiguredDefaultRole(guild);
        if (defaultRole == null || defaultRole.isManaged() || !guild.getSelfMember().canInteract(defaultRole)) {
            return false;
        }

        for (RoleSpec spec : buildProfileRoleSpecs()) {
            Role role = getConfiguredRole(guild, spec.key());
            if (role == null || role.isManaged() || !guild.getSelfMember().canInteract(role)) {
                return false;
            }
        }

        return true;
    }

    private Role getConfiguredDefaultRole(Guild guild) {
        var roleId = settingsStore.getDefaultRoleId(guild.getIdLong());
        if (roleId.isEmpty()) {
            return null;
        }
        return guild.getRoleById(roleId.getAsLong());
    }

    private Role getConfiguredRole(Guild guild, String roleKey) {
        var roleId = settingsStore.getProfileRoleId(guild.getIdLong(), roleKey);
        if (roleId.isEmpty()) {
            return null;
        }
        return guild.getRoleById(roleId.getAsLong());
    }

    private List<Role> getConfiguredProfileRoles(Guild guild) {
        List<Role> roles = new ArrayList<>();
        for (Long roleId : settingsStore.getProfileRoleIds(guild.getIdLong()).values()) {
            Role role = guild.getRoleById(roleId);
            if (role != null && !roles.contains(role)) {
                roles.add(role);
            }
        }
        return roles;
    }

    private String getSingleSelectedValue(ModalInteractionEvent event, String componentId) {
        ModalMapping mapping = event.getValue(componentId);
        if (mapping == null || mapping.getAsStringList().isEmpty()) {
            throw new IllegalStateException("선택값을 찾을 수 없습니다: " + componentId);
        }

        return mapping.getAsStringList().getFirst();
    }

    private void logSelection(ModalInteractionEvent event, ProfileSelection selection) {
        System.out.printf(
                "Profile updated: user=%s(%s), main=%s, sub=%s, tier=%s, complete=%s%n",
                event.getUser().getName(),
                event.getUser().getId(),
                selection.mainLane().value(),
                selection.subLane().value(),
                selection.tier().value(),
                selection.isComplete()
        );
    }

    private static String laneRoleKey(Lane lane, int priority) {
        return "lane." + lane.value() + "." + priority;
    }

    private static String tierRoleKey(Tier tier) {
        return "tier." + tier.value();
    }

    private static String rootMessage(Throwable error) {
        Throwable current = error;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() == null ? current.getClass().getSimpleName() : current.getMessage();
    }

    private record RoleSpec(String key, String name, Color color) {
    }

    private record ProfileSelection(Lane mainLane, Lane subLane, Tier tier) {

        private boolean isComplete() {
            return mainLane != null && subLane != null && tier != null;
        }
    }
}
