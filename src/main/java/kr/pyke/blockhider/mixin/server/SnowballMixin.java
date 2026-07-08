package kr.pyke.blockhider.mixin.server;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Snowball.class)
public abstract class SnowballMixin {
    @Inject(method = "onHitEntity", at = @At("HEAD"), cancellable = true)
    private void blockhider$onHitPlayer(EntityHitResult hitResult, CallbackInfo ci) {
        Entity target = hitResult.getEntity();
        if (target instanceof Player player) {
            Snowball snowball = (Snowball) (Object) this;
            if (player.level() instanceof ServerLevel serverLevel) {
                player.hurtServer(serverLevel, snowball.damageSources().thrown(snowball, snowball.getOwner()), 0.5f);
            }

            ci.cancel();
        }
    }
}