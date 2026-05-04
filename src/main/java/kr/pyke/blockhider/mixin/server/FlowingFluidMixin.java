package kr.pyke.blockhider.mixin.server;

import kr.pyke.blockhider.transform.PlayerTransform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FlowingFluid.class)
public abstract class FlowingFluidMixin {
    @Inject(method = "canMaybePassThrough", at = @At("HEAD"), cancellable = true)
    private void blockhider$canMaybePassThrough(BlockGetter level, BlockPos sourcePos, BlockState sourceState, Direction direction, BlockPos testPos, BlockState testState, FluidState testFluidState, CallbackInfoReturnable<Boolean> cir) {
        if (!(level instanceof Level fullLevel)) { return; }

        for (Player player : fullLevel.players()) {
            PlayerTransform transform = (PlayerTransform) player;
            BlockPos transformedPos = transform.blockhider$getTransformedPos();
            if (transformedPos == null) { continue; }

            if (testPos.getX() == transformedPos.getX() && testPos.getY() == transformedPos.getY() + 1 && testPos.getZ() == transformedPos.getZ()) {
                cir.setReturnValue(false);
                return;
            }
        }
    }
}