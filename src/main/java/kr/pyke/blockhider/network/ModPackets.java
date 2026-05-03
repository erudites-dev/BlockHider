package kr.pyke.blockhider.network;

import kr.pyke.blockhider.network.payload.s2c.S2C_TransformPayload;
import kr.pyke.blockhider.network.payload.s2c.S2C_TransformSyncPayload;
import kr.pyke.blockhider.transform.PlayerTransform;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
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
    }

    public static void registerServer() {

    }

    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(S2C_TransformPayload.ID, S2C_TransformPayload::handle);
        ClientPlayNetworking.registerGlobalReceiver(S2C_TransformSyncPayload.ID, S2C_TransformSyncPayload::handle);
    }

    public static void broadcastTransform(MinecraftServer server, UUID playerUUID, BlockState block) {
        S2C_TransformPayload payload = new S2C_TransformPayload(playerUUID, Optional.ofNullable(block));
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    public static void sendFullSync(ServerPlayer recipient) {
        Map<UUID, BlockState> entries = new HashMap<>();
        for (ServerPlayer player : recipient.level().getServer().getPlayerList().getPlayers()) {
            BlockState state = ((PlayerTransform) player).blockhider$getTransformedBlock();
            if (state != null) {
                entries.put(player.getUUID(), state);
            }
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
}
