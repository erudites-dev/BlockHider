package kr.pyke.blockhider.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

import java.util.Collection;

public class ScoreCommand {
    private static final int OP_LEVEL = 2;
    private static final String OBJECTIVE_NAME = "server_score";

    private ScoreCommand() { }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ctx, Commands.CommandSelection selection) {
        dispatcher.register(Commands.literal("점수")
            .requires(source -> source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(OP_LEVEL))))
            .then(Commands.literal("추가")
                .then(Commands.argument("targets", EntityArgument.players())
                    .executes(context -> manipulateScore(context, 1, MODE.ADD))
                    .then(Commands.argument("score", IntegerArgumentType.integer())
                        .executes(context -> manipulateScore(context, IntegerArgumentType.getInteger(context, "score"), MODE.ADD))
                    )
                )
            )
            .then(Commands.literal("제거")
                .then(Commands.argument("targets", EntityArgument.players())
                    .executes(context -> manipulateScore(context, 1, MODE.REMOVE))
                    .then(Commands.argument("score", IntegerArgumentType.integer())
                        .executes(context -> manipulateScore(context, IntegerArgumentType.getInteger(context, "score"), MODE.REMOVE))
                    )
                )
            )
            .then(Commands.literal("설정")
                .then(Commands.argument("targets", EntityArgument.players())
                    .then(Commands.argument("score", IntegerArgumentType.integer())
                        .executes(context -> manipulateScore(context, IntegerArgumentType.getInteger(context, "score"), MODE.SET))
                    )
                )
            )
        );
    }

    private static int manipulateScore(CommandContext<CommandSourceStack> context, int amount, MODE mode) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "targets");
        MinecraftServer server = source.getServer();
        Scoreboard scoreboard = server.getScoreboard();

        Objective objective = scoreboard.getObjective(OBJECTIVE_NAME);
        if (objective == null) {
            objective = scoreboard.addObjective(OBJECTIVE_NAME, ObjectiveCriteria.DUMMY, Component.literal("점수표"), ObjectiveCriteria.RenderType.INTEGER, false, null);
            scoreboard.setDisplayObjective(DisplaySlot.SIDEBAR, objective);
        }

        for (ServerPlayer player : players) {
            ScoreAccess scoreAccess = scoreboard.getOrCreatePlayerScore(player, objective);

            int newScore = scoreAccess.get();

            if (mode == MODE.ADD) { newScore += amount; }
            else if (mode == MODE.REMOVE) { newScore -= amount; }
            else if (mode == MODE.SET) { newScore = amount; }

            scoreAccess.set(newScore);
            scoreAccess.display(player.getDisplayName());
        }

        source.sendSuccess(() -> Component.literal("§6[SYSTEM]§r " + players.size() + "명의 점수를 업데이트했습니다."), true);
        return players.size();
    }

    private enum MODE {
        ADD,
        REMOVE,
        SET
    }
}