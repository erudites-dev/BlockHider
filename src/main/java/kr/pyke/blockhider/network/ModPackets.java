package kr.pyke.blockhider.network;

import kr.pyke.blockhider.config.ModConfig;
import kr.pyke.blockhider.game.GameData;
import kr.pyke.blockhider.game.GameManager;
import kr.pyke.blockhider.game.PlayerGameData;
import kr.pyke.blockhider.network.payload.s2c.S2C_GameStatePayload;
import kr.pyke.blockhider.network.payload.s2c.S2C_TransformPayload;
import kr.pyke.blockhider.network.payload.s2c.S2C_TransformSyncPayload;
import kr.pyke.blockhider.transform.PlayerTransform;
import kr.pyke.blockhider.type.GAME_ROLE;
import kr.pyke.blockhider.type.GAME_STATE;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ModPackets {
    private ModPackets() { }

    public static void registerCodec() {
        // Server → Client
        PayloadTypeRegistry.clientboundPlay().register(S2C_TransformPayload.ID, S2C_TransformPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(S2C_TransformSyncPayload.ID, S2C_TransformSyncPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(S2C_GameStatePayload.ID, S2C_GameStatePayload.STREAM_CODEC);
    }

    public static void registerServer() {

    }

    public static void registerClient() {
        // S2C_TransformPayload
        ClientPlayNetworking.registerGlobalReceiver(S2C_TransformPayload.ID, S2C_TransformPayload::handle);
        // S2C_TransformSyncPayload
        ClientPlayNetworking.registerGlobalReceiver(S2C_TransformSyncPayload.ID, S2C_TransformSyncPayload::handle);
        // S2C_GameStatePayload
        ClientPlayNetworking.registerGlobalReceiver(S2C_GameStatePayload.ID, S2C_GameStatePayload::handle);
    }

    public static void broadcastTransform(MinecraftServer server, UUID playerUuid, BlockState block, BlockPos pos) {
        S2C_TransformPayload payload = new S2C_TransformPayload(playerUuid, Optional.ofNullable(block), Optional.ofNullable(pos));
        for (ServerPlayer player : server.getPlayerList().getPlayers()) { ServerPlayNetworking.send(player, payload); }
    }

    public static void broadcastGameState(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            sendGameState(player);
        }
    }

    public static void sendFullSync(ServerPlayer recipient) {
        Map<UUID, S2C_TransformSyncPayload.TransformEntry> entries = new HashMap<>();
        for (ServerPlayer player : recipient.level().getServer().getPlayerList().getPlayers()) {
            BlockState state = ((PlayerTransform) player).blockhider$getTransformedBlock();
            BlockPos pos = ((PlayerTransform) player).blockhider$getTransformedPos();
            if (state != null && pos != null) { entries.put(player.getUUID(), new S2C_TransformSyncPayload.TransformEntry(state, pos)); }
        }
        S2C_TransformSyncPayload payload = new S2C_TransformSyncPayload(entries);
        ServerPlayNetworking.send(recipient, payload);
    }

    public static void broadcastClearAll(MinecraftServer server) {
        S2C_TransformSyncPayload payload = new S2C_TransformSyncPayload(Map.of());
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    public static void sendGameState(ServerPlayer player) {
        GameData data = GameManager.getInstance().getData();
        PlayerGameData playerGameData = data.getPlayerData(player.getUUID());

        GAME_STATE state = data.getState();
        int remaining = GameManager.getInstance().getTimer().getRemainingSeconds();
        int total = GameManager.getInstance().getTimer().getTotalSeconds();
        Optional<GAME_ROLE> role = playerGameData != null ? Optional.of(playerGameData.getRole()) : Optional.empty();
        int aliveSeekers = data.getAliveCount(GAME_ROLE.SEEKER);
        int aliveHiders = data.getAliveCount(GAME_ROLE.HIDER);
        int hintUsed = data.getHintUseCount();
        int hintMax = ModConfig.getHintItemCount();

        S2C_GameStatePayload payload = new S2C_GameStatePayload(state, remaining, total, role, aliveSeekers, aliveHiders, hintUsed, hintMax);
        ServerPlayNetworking.send(player, payload);
    }
}
