package kr.pyke.blockhider.handler;

import kr.pyke.blockhider.game.GameManager;
import kr.pyke.blockhider.game.PlayerGameData;
import kr.pyke.blockhider.network.ModPackets;
import kr.pyke.blockhider.network.payload.s2c.S2C_SeekerListPayload;
import kr.pyke.blockhider.transform.PlayerTransform;
import kr.pyke.blockhider.type.GAME_ROLE;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;

public class ServerPlayConnectionHandler {
    private ServerPlayConnectionHandler() { }

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            ModPackets.sendFullSync(player);
            ModPackets.sendGameState(player);
            sendFakeBlocksToJoiningPlayer(server, player);
            sendSeekerList(server);
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayer player = handler.getPlayer();
            PlayerTransform transform = (PlayerTransform) player;
            BlockPos currentPos = transform.blockhider$getTransformedPos();

            if (currentPos != null) {
                ServerLevel level = player.level();
                BlockPos visualPos = currentPos.above();
                BlockState restored = level.getBlockState(visualPos);
                ClientboundBlockUpdatePacket packet = new ClientboundBlockUpdatePacket(visualPos, restored);
                for (ServerPlayer other : server.getPlayerList().getPlayers()) {
                    if (other.getUUID().equals(player.getUUID())) { continue; }

                    other.connection.send(packet);
                }

                transform.blockhider$setTransformedBlock(null, null);
                ModPackets.broadcastTransform(server, player.getUUID(), null, null);
            }
        });
    }

    private static void sendFakeBlocksToJoiningPlayer(MinecraftServer server, ServerPlayer player) {
        for (ServerPlayer other : server.getPlayerList().getPlayers()) {
            if (other.getUUID().equals(player.getUUID())) { continue; }

            PlayerTransform transform = (PlayerTransform) other;
            BlockState block = transform.blockhider$getTransformedBlock();
            BlockPos pos = transform.blockhider$getTransformedPos();
            if (block == null || pos == null) { continue; }

            BlockPos visualPos = pos.above();
            ClientboundBlockUpdatePacket packet = new ClientboundBlockUpdatePacket(visualPos, block);
            player.connection.send(packet);
        }
    }

    public static void sendSeekerList(MinecraftServer server) {
        StringBuilder seekers = new StringBuilder();

        GameManager.getInstance().getData().getPlayers().forEach(playerGameData -> {
            if (playerGameData.getRole() == GAME_ROLE.SEEKER) {
                ServerPlayer seeker = server.getPlayerList().getPlayer(playerGameData.getUUID());
                if (seeker == null) { return; }

                if (seekers.isEmpty()) { seekers.append(seeker.getDisplayName().getString()); }
                else { seekers.append(", ").append(seeker.getDisplayName().getString()); }
            }
        });

        S2C_SeekerListPayload payload = new S2C_SeekerListPayload(seekers.toString());
        server.getPlayerList().getPlayers().forEach(player -> ServerPlayNetworking.send(player, payload));
    }
}
