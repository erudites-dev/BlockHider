package kr.pyke.blockhider.mixin.server;

import kr.pyke.blockhider.transform.PlayerTransform;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Redirect(method = "collide", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getBoundingBox()Lnet/minecraft/world/phys/AABB;"))
    private AABB blockhider$redirectGetBoundingBox(Entity self) {
        if (!(self instanceof Player player)) { return self.getBoundingBox(); }

        if (((PlayerTransform) player).blockhider$getTransformedBlock() == null) { return self.getBoundingBox(); }

        EntityDimensions vanillaDims = player.getType().getDimensions().scale(player.getScale());
        return vanillaDims.makeBoundingBox(player.position());
    }
}