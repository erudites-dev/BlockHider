package kr.pyke.blockhider.handler;

import kr.pyke.blockhider.network.ModPackets;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class ServerPlayConnectionHandler {
    private ServerPlayConnectionHandler() { }

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ModPackets.sendFullSync(handler.getPlayer());
            ModPackets.sendGameState(handler.getPlayer());
        });
    }
}
