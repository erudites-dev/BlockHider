package kr.pyke.blockhider.mixin.server;

import kr.pyke.blockhider.transform.PlayerTransform;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "canBeCollidedWith", at = @At("HEAD"), cancellable = true)
    private void blockhider$canBeCollidedWith(@Nullable Entity other, CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof Player player)) { return; }

        if (((PlayerTransform) player).blockhider$getTransformedBlock() != null) {
            cir.setReturnValue(true);
        }
    }
}