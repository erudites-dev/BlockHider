package kr.pyke.blockhider.handler;

import kr.pyke.blockhider.game.GameManager;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerPlayer;

public class ServerLivingEntityHandler {
    private ServerLivingEntityHandler() { }

    public static void register() {
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, damageAmount) -> {
            if (!(entity instanceof ServerPlayer player)) { return true; }

            return GameManager.getInstance().handlePlayerDeath(player);
        });
    }
}
