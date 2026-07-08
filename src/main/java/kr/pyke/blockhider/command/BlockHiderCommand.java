package kr.pyke.blockhider.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import kr.pyke.blockhider.config.ModConfig;
import kr.pyke.blockhider.data.BlockHiderSavedData;
import kr.pyke.blockhider.game.GameManager;
import kr.pyke.blockhider.transform.HitboxOwner;
import kr.pyke.blockhider.type.GAME_ROLE;
import kr.pyke.displayname.data.DisplayNameData;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Interaction;

import java.util.*;

public class BlockHiderCommand {
    private static final int OP_LEVEL = 2;
    private static final int MIN_COUNT = 0;
    private static final int MIN_SECONDS = 1;
    private static final int MIN_COOLDOWN = 0;

    private BlockHiderCommand() { }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ctx, Commands.CommandSelection selection) {
        dispatcher.register(Commands.literal("블숨")
            .requires(source -> source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(OP_LEVEL))))
            .then(Commands.literal("시작").executes(BlockHiderCommand::startGame))
            .then(Commands.literal("종료").executes(BlockHiderCommand::stopGame))
            .then(Commands.literal("술래")
                .then(Commands.argument("players", StringArgumentType.greedyString()).executes(BlockHiderCommand::startGameWithSeekers))
            )
            .then(Commands.literal("술래인원")
                .then(Commands.argument("count", IntegerArgumentType.integer(MIN_COUNT)).executes(BlockHiderCommand::setSeekerCount))
            )
            .then(Commands.literal("시간")
                .then(Commands.argument("seconds", IntegerArgumentType.integer(MIN_SECONDS)).executes(BlockHiderCommand::setGameTime))
            )
            .then(Commands.literal("대기시간")
                .then(Commands.argument("seconds", IntegerArgumentType.integer(MIN_SECONDS)).executes(BlockHiderCommand::setPreparationTime))
            )
            .then(Commands.literal("힌트개수")
                .then(Commands.argument("count", IntegerArgumentType.integer(MIN_COUNT)).executes(BlockHiderCommand::setHintItemCount))
            )
            .then(Commands.literal("눈덩이")
                .then(Commands.literal("개수")
                    .then(Commands.argument("count", IntegerArgumentType.integer(MIN_COUNT)).executes(BlockHiderCommand::setSnowballCount))
                )
                .then(Commands.literal("쿨타임")
                    .then(Commands.argument("ticks", IntegerArgumentType.integer(MIN_COOLDOWN)).executes(BlockHiderCommand::setSnowballCooldown))
                )
            )
            .then(Commands.literal("스폰").executes(BlockHiderCommand::setSpawnHere))
            .then(Commands.literal("관리자")
                .then(Commands.literal("추가")
                    .then(Commands.argument("player", EntityArgument.player()).executes(BlockHiderCommand::addAdmin))
                )
                .then(Commands.literal("제거")
                    .then(Commands.argument("player", EntityArgument.player()).executes(BlockHiderCommand::removeAdmin))
                )
                .then(Commands.literal("목록").executes(BlockHiderCommand::listAdmins))
            )
            .then(Commands.literal("디버그")
                .then(Commands.literal("도망자").executes(context -> startSolo(context, GAME_ROLE.HIDER)))
                .then(Commands.literal("술래").executes(context -> startSolo(context, GAME_ROLE.SEEKER)))
                .then(Commands.literal("히트박스").executes(BlockHiderCommand::spawnHitbox))
            )
            .then(Commands.literal("리로드").executes(BlockHiderCommand::reloadConfig))
        );
    }

    private static int startGame(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        boolean started = GameManager.getInstance().start(source.getServer(), List.of());
        if (!started) {
            source.sendSuccess(() -> Component.literal("§6[SYSTEM]§r 게임을 시작할 수 없습니다."), true);
            return 0;
        }

        source.sendSuccess(() -> Component.literal("§6[SYSTEM]§r 게임이 시작되었습니다."), true);
        return 1;
    }

    private static int stopGame(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        if (!GameManager.getInstance().isRunning()) {
            source.sendFailure(Component.literal("§6[SYSTEM]§r 진행 중인 게임이 없습니다."));
            return 0;
        }

        GameManager.getInstance().stop(source.getServer());
        return 1;
    }

    private static int setSeekerCount(CommandContext<CommandSourceStack> context) {
        int value = IntegerArgumentType.getInteger(context, "count");
        ModConfig.setSeekerCount(value);
        ModConfig.save();

        context.getSource().sendSuccess(() -> Component.literal("§6[SYSTEM]§r 술래 인원이 " + value + "명으로 설정되었습니다."), true);
        return 1;
    }

    private static int setGameTime(CommandContext<CommandSourceStack> context) {
        int value = IntegerArgumentType.getInteger(context, "seconds");
        ModConfig.setGameTimeSeconds(value);
        ModConfig.save();

        context.getSource().sendSuccess(() -> Component.literal("§6[SYSTEM]§r 게임 시간이 " + value + "초로 설정되었습니다."), true);
        return 1;
    }

    private static int setPreparationTime(CommandContext<CommandSourceStack> context) {
        int value = IntegerArgumentType.getInteger(context, "seconds");
        ModConfig.setPreparationTimeSeconds(value);
        ModConfig.save();

        context.getSource().sendSuccess(() -> Component.literal("§6[SYSTEM]§r 대기 시간이 " + value + "초로 설정되었습니다."), true);
        return 1;
    }

    private static int setHintItemCount(CommandContext<CommandSourceStack> context) {
        int value = IntegerArgumentType.getInteger(context, "count");
        ModConfig.setHintItemCount(value);
        ModConfig.save();

        context.getSource().sendSuccess(() -> Component.literal("§6[SYSTEM]§r 힌트 아이템 개수가 " + value + "개로 설정되었습니다."), true);
        return 1;
    }

    private static int setSnowballCount(CommandContext<CommandSourceStack> context) {
        int value = IntegerArgumentType.getInteger(context, "count");
        ModConfig.setSnowballCount(value);
        ModConfig.save();

        context.getSource().sendSuccess(() -> Component.literal("§6[SYSTEM]§r 눈덩이 개수가 " + value + "개로 설정되었습니다."), true);
        return 1;
    }

    private static int setSnowballCooldown(CommandContext<CommandSourceStack> context) {
        int value = IntegerArgumentType.getInteger(context, "ticks");
        ModConfig.setSnowballCooldownTicks(value);
        ModConfig.save();

        context.getSource().sendSuccess(() -> Component.literal("§6[SYSTEM]§r 눈덩이 쿨타임이 " + (value / 20.f) + "초로 설정되었습니다."), true);
        return 1;
    }

    private static int setSpawnHere(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendSuccess(() -> Component.literal("§6[SYSTEM]§r 플레이어만 사용할 수 있는 명령어입니다."), true);
            return 0;
        }

        BlockHiderSavedData savedData = BlockHiderSavedData.get(source.getServer());
        savedData.setSpawn(player.level().dimension(), player.position(), player.getYRot(), player.getXRot());

        source.sendSuccess(() -> Component.literal("§6[SYSTEM]§r 스폰 위치가 현재 위치로 설정되었습니다."), true);
        return 1;
    }

    private static int addAdmin(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        BlockHiderSavedData savedData = BlockHiderSavedData.get(source.getServer());

        boolean added = savedData.addAdmin(target.getUUID());
        if (!added) {
            source.sendSuccess(() -> Component.literal("§6[SYSTEM]§r " + target.getDisplayName().getString() + "은(는) 이미 관리자입니다."), true);
            return 0;
        }

        source.sendSuccess(() -> Component.literal("§6[SYSTEM]§r " + target.getDisplayName().getString() + "을(를) 관리자로 추가했습니다."), true);
        return 1;
    }

    private static int removeAdmin(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        BlockHiderSavedData savedData = BlockHiderSavedData.get(source.getServer());

        boolean removed = savedData.removeAdmin(target.getUUID());
        if (!removed) {
            source.sendSuccess(() -> Component.literal("§6[SYSTEM]§r " + target.getDisplayName().getString() + "은(는) 관리자가 아닙니다."), true);
            return 0;
        }

        source.sendSuccess(() -> Component.literal("§6[SYSTEM]§r " + target.getDisplayName().getString() + "을(를) 관리자에서 제거했습니다."), true);
        return 1;
    }

    private static int listAdmins(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        BlockHiderSavedData savedData = BlockHiderSavedData.get(source.getServer());
        List<UUID> admins = savedData.getAdmins();

        if (admins.isEmpty()) {
            source.sendSuccess(() -> Component.literal("§6[SYSTEM]§r 등록된 관리자가 없습니다."), false);
            return 0;
        }

        MinecraftServer server = source.getServer();
        StringBuilder builder = new StringBuilder("§6[SYSTEM]§r 관리자 목록 (").append(admins.size()).append("명): ");
        for (int i = 0; i < admins.size(); i++) {
            UUID uuid = admins.get(i);
            ServerPlayer admin = server.getPlayerList().getPlayer(uuid);
            String name = admin != null ? admin.getDisplayName().getString() : uuid.toString();
            if (i > 0) { builder.append(", "); }
            builder.append(name);
        }

        String message = builder.toString();
        source.sendSuccess(() -> Component.literal(message), false);
        return 1;
    }

    private static int startSolo(CommandContext<CommandSourceStack> context, GAME_ROLE role) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("§6[SYSTEM]§r 플레이어만 사용할 수 있는 명령어입니다."));
            return 0;
        }

        boolean started = GameManager.getInstance().startSolo(source.getServer(), player, role);
        if (!started) {
            source.sendFailure(Component.literal("§6[SYSTEM]§r 디버그 게임을 시작할 수 없습니다."));
            return 0;
        }

        String roleName = role == GAME_ROLE.HIDER ? "도망자" : "술래";
        source.sendSuccess(() -> Component.literal("§6[SYSTEM]§r 디버그 게임이 시작되었습니다. (" + roleName + ")"), true);
        return 1;
    }

    private static int startGameWithSeekers(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String input = StringArgumentType.getString(context, "players");
        MinecraftServer server = source.getServer();

        Set<UUID> seekerUUIDs = new LinkedHashSet<>();
        List<String> notFound = new ArrayList<>();

        for (String name : input.split(",")) {
            String trimmed = name.trim();
            if (trimmed.isEmpty()) { continue; }

            ServerPlayer target = server.getPlayerList().getPlayerByName(trimmed);
            if (target == null) {
                String targetName = DisplayNameData.getServerState(server).getRealName(trimmed);
                target = server.getPlayerList().getPlayerByName(targetName);

                if (target == null) {
                    notFound.add(trimmed);
                    continue;
                }

                seekerUUIDs.add(target.getUUID());
            }

            seekerUUIDs.add(target.getUUID());
        }

        if (!notFound.isEmpty()) {
            source.sendFailure(Component.literal("§6[SYSTEM]§r 찾을 수 없는 플레이어: " + String.join(", ", notFound)));
            return 0;
        }
        if (seekerUUIDs.isEmpty()) {
            source.sendFailure(Component.literal("§6[SYSTEM]§r 술래로 지정된 플레이어가 없습니다."));
            return 0;
        }

        boolean started = GameManager.getInstance().start(server, new ArrayList<>(seekerUUIDs));
        if (!started) {
            source.sendFailure(Component.literal("§6[SYSTEM]§r 게임을 시작할 수 없습니다."));
            return 0;
        }

        int count = seekerUUIDs.size();
        source.sendSuccess(() -> Component.literal("§6[SYSTEM]§r 게임이 시작되었습니다. (술래 " + count + "명 지정)"), true);
        return 1;
    }

    private static int spawnHitbox(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("§6[SYSTEM]§r 플레이어만 사용할 수 있는 명령어입니다."));
            return 0;
        }

        Interaction hitbox = EntityType.INTERACTION.create(player.level(), EntitySpawnReason.COMMAND);
        if (hitbox == null) {
            source.sendFailure(Component.literal("§6[SYSTEM]§r Interaction 생성 실패."));
            return 0;
        }

        hitbox.setWidth(1.f);
        hitbox.setHeight(1.f);
        hitbox.setPos(player.getX(), player.getY(), player.getZ());

        if (hitbox instanceof HitboxOwner owner) {
            owner.blockhider$setOwner(player);
        }

        player.level().addFreshEntity(hitbox);

        source.sendSuccess(() -> Component.literal("§6[SYSTEM]§r 테스트 Interaction 소환. (owner: " + player.getDisplayName().getString() + ")"), true);
        return 1;
    }

    private static int reloadConfig(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();

        ModConfig.load();
        source.sendSuccess(() -> Component.literal("§6[SYSTEM]§r Config 리로드가 완료되었습니다."), true);
        return 1;
    }
}
