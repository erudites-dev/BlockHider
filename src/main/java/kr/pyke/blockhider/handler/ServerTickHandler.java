package kr.pyke.blockhider.handler;

import kr.pyke.blockhider.game.GameManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class ServerTickHandler {
    private ServerTickHandler() { }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> GameManager.getInstance().getTimer().tick(server));
    }
}
