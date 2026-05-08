package kr.pyke.blockhider.mixin.server;

import kr.pyke.blockhider.transform.PlayerTransform;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "isInWall", at = @At("HEAD"), cancellable = true)
    private void blockhider$isInsideWall(CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof Player player)) { return; }

        PlayerTransform transform = (PlayerTransform) player;
        if (transform.blockhider$getTransformedBlock() != null) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "canBeCollidedWith", at = @At("HEAD"), cancellable = true)
    private void blockhider$canBeCollidedWith(@Nullable Entity other, CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof Player player)) { return; }

        PlayerTransform transform = (PlayerTransform) player;
        if (transform.blockhider$getTransformedBlock() != null) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "push*", at = @At("HEAD"), cancellable = true)
    private void blockhider$cancelPush(Entity entity, CallbackInfo ci) {
        if ((Object) this instanceof Player player) {
            PlayerTransform transform = (PlayerTransform) player;
            if (transform.blockhider$getTransformedBlock() != null) {
                ci.cancel();
                return;
            }
        }

        if (entity instanceof Player target) {
            PlayerTransform transform = (PlayerTransform) target;
            if (transform.blockhider$getTransformedBlock() != null) {
                ci.cancel();
            }
        }
    }
}