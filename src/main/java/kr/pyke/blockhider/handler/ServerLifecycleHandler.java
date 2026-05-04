package kr.pyke.blockhider.handler;

import kr.pyke.blockhider.game.GameManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class ServerLifecycleHandler {
    private ServerLifecycleHandler() { }

    public static void register() {
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            if (GameManager.getInstance().isRunning()) {
                GameManager.getInstance().stop(server);
            }
        });
    }
}
