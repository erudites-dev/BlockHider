package kr.pyke.blockhider.game;

import kr.pyke.blockhider.config.ModConfig;
import kr.pyke.blockhider.data.BlockHiderSavedData;
import kr.pyke.blockhider.effect.HintEffect;
import kr.pyke.blockhider.network.ModPackets;
import kr.pyke.blockhider.transform.PlayerTransform;
import kr.pyke.blockhider.transform.TransformableBlocks;
import kr.pyke.blockhider.type.GAME_ROLE;
import kr.pyke.blockhider.type.GAME_STATE;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition;

import java.util.*;

public class GameManager {
    private static final GameManager INSTANCE = new GameManager();
    private static final double BLOCK_CENTER_OFFSET = 0.5d;

    private final GameData data = new GameData();
    private final GameTimer timer = new GameTimer(this);
    private final Random random = new Random();

    private GameManager() { }

    public static GameManager getInstance() { return INSTANCE; }

    public GameTimer getTimer() { return this.timer; }

    public GameData getData() { return this.data; }

    public boolean isRunning() { return data.getState() != GAME_STATE.WAITING; }

    public boolean start(MinecraftServer server, List<UUID> manualSeekers) {
        if (isRunning()) { return false; }

        BlockHiderSavedData savedData = BlockHiderSavedData.get(server);
        List<ServerPlayer> participants = collectParticipants(server, savedData);
        if (participants.size() < 2) { return false; }

        int seekerCount = manualSeekers.isEmpty() ? ModConfig.getSeekerCount() : manualSeekers.size();
        if (participants.size() <= seekerCount) { return false; }

        data.clearPlayers();
        data.setHintUseCount(0);
        data.setDebugMode(false);
        assignRoles(participants, manualSeekers, seekerCount);
        data.setState(GAME_STATE.COUNTDOWN);

        this.timer.startCountdown(server);
        ModPackets.broadcastGameState(server);
        broadcastSeekers(server);

        return true;
    }

    public void stop(MinecraftServer server) {
        if (!isRunning()) { return; }

        this.timer.stop();

        BlockHiderSavedData savedData = BlockHiderSavedData.get(server);
        for (PlayerGameData playerGameData : data.getPlayers()) {
            ServerPlayer player = server.getPlayerList().getPlayer(playerGameData.getUUID());
            if (player == null) { continue; }
            cleanUpPlayer(player, playerGameData);
            teleportToSpawn(player, savedData);
        }

        data.clearPlayers();
        data.setState(GAME_STATE.WAITING);

        ModPackets.broadcastClearAll(server);
        ModPackets.broadcastGameState(server);
        server.getPlayerList().broadcastSystemMessage(Component.literal("§6[SYSTEM]§r 게임이 종료되었습니다."), false);
    }

    public void tickPlayer(ServerPlayer player) {
//        if (data.getState() != GAME_STATE.RUNNING) { return; }

//        PlayerGameData playerGameData = data.getPlayerData(player.getUUID());
//        if (playerGameData == null || !playerGameData.isAlive() || playerGameData.getRole() != GAME_ROLE.HIDER) { return; }

        PlayerTransform transform = (PlayerTransform) player;
        BlockState current = transform.blockhider$getTransformedBlock();
        BlockPos currentPos = transform.blockhider$getTransformedPos();

        if (!player.isCrouching()) {
            if (current != null) {
                transform.blockhider$setTransformedBlock(null, null);
                ModPackets.broadcastTransform(player.level().getServer(), player.getUUID(), null, null);
            }

            return;
        }

        ServerLevel level = player.level();
        BlockPos belowPos = player.blockPosition().below();
        BlockState belowState = level.getBlockState(belowPos);

        if (!TransformableBlocks.isTransformable(level, belowPos, belowState)) {
            if (current != null) {
                transform.blockhider$setTransformedBlock(null, null);
                ModPackets.broadcastTransform(player.level().getServer(), player.getUUID(), null, null);
            }

            return;
        }

        if (current == belowState && belowPos.equals(currentPos)) { return; }

        transform.blockhider$setTransformedBlock(belowState, belowPos);

        double centerX = belowPos.getX() + BLOCK_CENTER_OFFSET;
        double centerZ = belowPos.getZ() + BLOCK_CENTER_OFFSET;
        player.snapTo(centerX, player.getY(), centerZ, player.getYRot(), player.getXRot());

        ModPackets.broadcastTransform(player.level().getServer(), player.getUUID(), belowState, belowPos);
    }

    private void cleanUpPlayer(ServerPlayer player, PlayerGameData playerGameData) {
        player.getInventory().clearContent();
        if (playerGameData.getRole() == GAME_ROLE.SPECTATOR) {
            player.setGameMode(GameType.SURVIVAL);
        }

        PlayerTransform transform = (PlayerTransform)player;
        if (transform.blockhider$getTransformedBlock() != null) {
            transform.blockhider$setTransformedBlock(null, null);
        }
    }

    private void teleportToSpawn(ServerPlayer player, BlockHiderSavedData savedData) {
        TeleportTransition transition = savedData.createSpawnTransition(player.level().getServer(), TeleportTransition.DO_NOTHING);
        if (transition == null) { return; }

        player.teleport(transition);
    }

    private List<ServerPlayer> collectParticipants(MinecraftServer server, BlockHiderSavedData savedData) {
        List<ServerPlayer> result = new ArrayList<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!savedData.isAdmin(player.getUUID())) {
                result.add(player);
            }
        }

        return result;
    }

    private void assignRoles(List<ServerPlayer> participants, List<UUID> manualSeekers, int seekerCount) {
        if (manualSeekers.isEmpty()) {
            List<ServerPlayer> shuffled = new ArrayList<>(participants);
            Collections.shuffle(shuffled, random);
            for (int i = 0; i < shuffled.size(); i++) {
                ServerPlayer player = shuffled.get(i);
                GAME_ROLE role = i < seekerCount ? GAME_ROLE.SEEKER : GAME_ROLE.HIDER;
                data.addPlayer(new PlayerGameData(player.getUUID(), role));
            }
        }
        else {
            for (ServerPlayer player : participants) {
                UUID uuid = player.getUUID();
                GAME_ROLE role = manualSeekers.contains(uuid) ? GAME_ROLE.SEEKER : GAME_ROLE.HIDER;
                data.addPlayer(new PlayerGameData(uuid, role));
            }
        }
    }

    public boolean handlePlayerDeath(ServerPlayer player) {
        if (data.getState() != GAME_STATE.RUNNING) { return true; }

        PlayerGameData playerGameData = data.getPlayerData(player.getUUID());
        if (playerGameData == null || !playerGameData.isAlive()) { return true; }
        if (playerGameData.getRole() != GAME_ROLE.SEEKER && playerGameData.getRole() != GAME_ROLE.HIDER) { return true; }

        eliminate(player, playerGameData);
        checkVictory(player.level().getServer());
        return false;
    }

    public boolean tryUseHint(ServerPlayer player) {
        if (data.getState() != GAME_STATE.RUNNING) { return false; }

        PlayerGameData playerGameData = data.getPlayerData(player.getUUID());
        if (playerGameData == null || playerGameData.getRole() != GAME_ROLE.SEEKER || !playerGameData.isAlive()) { return false; }

        int maxUseCount = ModConfig.getHintItemCount();
        if (maxUseCount == 0) { return false; }
        if (maxUseCount > 0 && data.getHintUseCount() >= maxUseCount) { return false; }

        data.incrementHintUseCount();

        ServerLevel level = player.level();
        for (PlayerGameData target : data.getPlayers()) {
            if (target.getRole() != GAME_ROLE.HIDER || !target.isAlive()) { continue; }

            ServerPlayer hider = level.getServer().getPlayerList().getPlayer(target.getUUID());
            if (hider == null || hider.level() != level) { continue; }

            HintEffect.spawn(level, hider.getX(), hider.getY(), hider.getZ());
        }

        ModPackets.broadcastGameState(level.getServer());
        return true;
    }

    private void eliminate(ServerPlayer player, PlayerGameData playerGameData) {
        playerGameData.setAlive(false);
        playerGameData.setRole(GAME_ROLE.SPECTATOR);

        PlayerTransform transform = (PlayerTransform) player;
        if (transform.blockhider$getTransformedBlock() != null) {
            transform.blockhider$setTransformedBlock(null, null);
            ModPackets.broadcastTransform(player.level().getServer(), player.getUUID(), null, null);
        }

        player.setHealth(player.getMaxHealth());
        player.setGameMode(GameType.SPECTATOR);
        player.getInventory().clearContent();

        ModPackets.broadcastGameState(player.level().getServer());
    }

    private void checkVictory(MinecraftServer server) {
        if (data.isDebugMode()) { return; }

        boolean anyHider = false;
        boolean anySeeker = false;

        for (PlayerGameData playerGameData : data.getPlayers()) {
            if (!playerGameData.isAlive()) { continue; }
            if (playerGameData.getRole() == GAME_ROLE.HIDER) { anyHider = true; }
            else if (playerGameData.getRole() == GAME_ROLE.SEEKER) { anySeeker = true; }
        }

        if (!anyHider) {
            announceVictory(server, GAME_ROLE.SEEKER);
            stop(server);
        }
        else if (!anySeeker) {
            announceVictory(server, GAME_ROLE.HIDER);
            stop(server);
        }
    }

    private void announceVictory(MinecraftServer server, GAME_ROLE winner) {
        String text = winner == GAME_ROLE.SEEKER ? "§6[SYSTEM]§r 술래의 승리입니다!" : "§6[SYSTEM]§r 숨은 사람들의 승리입니다!";
        server.getPlayerList().broadcastSystemMessage(Component.literal(text), false);
    }

    public void handleTimeUp(MinecraftServer server) {
        if (data.isDebugMode()) {
            stop(server);
            return;
        }

        boolean anyHider = false;
        for (PlayerGameData playerGameData : data.getPlayers()) {
            if (!playerGameData.isAlive()) { continue; }
            if (playerGameData.getRole() == GAME_ROLE.HIDER) {
                anyHider = true;
                break;
            }
        }

        if (anyHider) { announceVictory(server, GAME_ROLE.HIDER); }

        stop(server);
    }

    public boolean startSolo(MinecraftServer server, ServerPlayer player, GAME_ROLE role) {
        if (isRunning()) { return false; }

        data.clearPlayers();
        data.setHintUseCount(0);
        data.setDebugMode(true);
        data.addPlayer(new PlayerGameData(player.getUUID(), role));

        data.setState(GAME_STATE.COUNTDOWN);
        this.timer.startCountdown(server);
        ModPackets.broadcastGameState(server);

        return true;
    }

    private void broadcastSeekers(MinecraftServer server) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (PlayerGameData playerGameData : data.getPlayers()) {
            if (playerGameData.getRole() != GAME_ROLE.SEEKER) { continue; }

            ServerPlayer seeker = server.getPlayerList().getPlayer(playerGameData.getUUID());
            String name = seeker != null ? seeker.getDisplayName().getString() : playerGameData.getUUID().toString();
            if (!first) { builder.append(", "); }
            builder.append(name);
            first = false;
        }

        Component message = Component.literal("§6[SYSTEM]§r 술래: §c" + builder + "§r");
        server.getPlayerList().broadcastSystemMessage(message, false);
    }
}
