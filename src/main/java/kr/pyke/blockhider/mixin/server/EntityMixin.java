package kr.pyke.blockhider.mixin.server;

import kr.pyke.blockhider.transform.PlayerTransform;
import kr.pyke.blockhider.transform.TransformableBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    private static final double BLOCK_CENTER_OFFSET = 0.5d;

    @Inject(method = "canBeCollidedWith", at = @At("HEAD"), cancellable = true)
    private void blockhider$canBeCollidedWith(@Nullable Entity other, CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof Player player)) { return; }

        if (((PlayerTransform) player).blockhider$getTransformedBlock() != null) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "makeBoundingBox*", at = @At("HEAD"), cancellable = true)
    private void blockhider$makeBoundingBox(Vec3 pos, CallbackInfoReturnable<AABB> cir) {
        if (!((Object) this instanceof Player player)) { return; }

        PlayerTransform transform = (PlayerTransform) player;
        BlockState blockState = transform.blockhider$getTransformedBlock();
        BlockPos blockPos = transform.blockhider$getTransformedPos();
        if (blockState == null || blockPos == null) { return; }

        EntityDimensions dimensions = TransformableBlocks.getDimensions(player.level(), blockPos, blockState);
        double centerX = blockPos.getX() + BLOCK_CENTER_OFFSET;
        double centerZ = blockPos.getZ() + BLOCK_CENTER_OFFSET;
        double minY = blockPos.getY() + 1;
        double half = dimensions.width() / 2d;

        cir.setReturnValue(new AABB(centerX - half, minY, centerZ - half, centerX + half, minY + dimensions.height(), centerZ + half));
    }
}