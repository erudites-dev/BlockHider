package kr.pyke.blockhider.mixin.server;

import kr.pyke.blockhider.transform.PlayerTransform;
import kr.pyke.blockhider.transform.TransformableBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "getDimensions", at = @At("HEAD"), cancellable = true)
    private void blockhider$getDimensions(Pose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        if (!((Object) this instanceof Player player)) { return; }

        BlockState block = ((PlayerTransform)player).blockhider$getTransformedBlock();
        BlockPos pos = ((PlayerTransform)player).blockhider$getTransformedPos();
        if (block == null || pos == null) { return; }

        cir.setReturnValue(TransformableBlocks.getDimensions(player.level(), pos, block));
    }

    @Inject(method = "isPushable", at = @At("HEAD"), cancellable = true)
    private void blockhider$isPushable(CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof Player player)) { return; }

        if (((PlayerTransform) player).blockhider$getTransformedBlock() != null) {
            cir.setReturnValue(false);
        }
    }
}