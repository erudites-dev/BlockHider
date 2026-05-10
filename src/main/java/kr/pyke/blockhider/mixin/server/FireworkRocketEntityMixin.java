package kr.pyke.blockhider.mixin.server;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FireworkRocketEntity.class)
public class FireworkRocketEntityMixin {
    @Inject(method = "dealExplosionDamage", at = @At("HEAD"), cancellable = true)
    private void blockhider$ignoreExplosionDamage(ServerLevel level, CallbackInfo ci) {
        ci.cancel();
    }
}
