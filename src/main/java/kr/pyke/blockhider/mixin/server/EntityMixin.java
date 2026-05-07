package kr.pyke.blockhider.mixin.server;

import kr.pyke.blockhider.transform.PlayerTransform;
import kr.pyke.blockhider.transform.TransformableBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Unique private static final double BLOCK_CENTER_OFFSET = 0.5d;

    @Redirect(method = "setPos(DDD)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;makeBoundingBox()Lnet/minecraft/world/phys/AABB;"))
    private AABB blockhider$useAttackBox(Entity self) {
        if (!(self instanceof Player player)) { return self.getDimensions(self.getPose()).makeBoundingBox(self.position()); }

        PlayerTransform transform = (PlayerTransform) player;
        BlockState blockState = transform.blockhider$getTransformedBlock();
        BlockPos blockPos = transform.blockhider$getTransformedPos();
        if (blockState == null || blockPos == null) { return self.getDimensions(self.getPose()).makeBoundingBox(self.position()); }

        EntityDimensions dimensions = TransformableBlocks.getDimensions(player.level(), blockPos, blockState);
        double centerX = blockPos.getX() + BLOCK_CENTER_OFFSET;
        double centerZ = blockPos.getZ() + BLOCK_CENTER_OFFSET;
        double minY = blockPos.getY() + 1;
        double half = dimensions.width() / 2d;
        return new AABB(centerX - half, minY, centerZ - half, centerX + half, minY + dimensions.height(), centerZ + half);
    }

    @Redirect(method = {"move", "collide"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getBoundingBox()Lnet/minecraft/world/phys/AABB;"))
    private AABB blockhider$useMovementBox(Entity self) {
        if (!(self instanceof Player player)) { return self.getBoundingBox(); }

        PlayerTransform transform = (PlayerTransform) player;
        if (transform.blockhider$getTransformedBlock() != null) { return player.getType().getDimensions().scale(player.getScale()).makeBoundingBox(player.position()); }

        return self.getBoundingBox();
    }

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