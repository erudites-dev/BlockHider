package kr.pyke.blockhider.handler;

import kr.pyke.blockhider.network.ModPackets;
import kr.pyke.blockhider.transform.PlayerTransform;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
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
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayer player = handler.getPlayer();
            PlayerTransform transform = (PlayerTransform) player;
            BlockPos currentPos = transform.blockhider$getTransformedPos();
            if (currentPos == null) { return; }

            ServerLevel level = player.level();
            BlockPos visualPos = currentPos.above();
            BlockState restored = level.getBlockState(visualPos);
            ClientboundBlockUpdatePacket packet = new ClientboundBlockUpdatePacket(visualPos, restored);
            for (ServerPlayer other : server.getPlayerList().getPlayers()) {
                if (other.getUUID().equals(player.getUUID())) { continue; }

                other.connection.send(packet);
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
}
