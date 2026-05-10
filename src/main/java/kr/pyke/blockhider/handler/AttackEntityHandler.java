package kr.pyke.blockhider.handler;

import kr.pyke.blockhider.transform.HitboxOwner;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

public class AttackEntityHandler {
    private AttackEntityHandler() { }

    public static void register() {
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            if (level.isClientSide()) { return InteractionResult.PASS; }
            if (!(entity instanceof HitboxOwner owner)) { return InteractionResult.PASS; }

            Player target = owner.blockhider$getOwner();
            if (target == null || target == player || !target.isAlive()) { return InteractionResult.PASS; }

            player.attack(target);
            return InteractionResult.SUCCESS;
        });
    }
}
