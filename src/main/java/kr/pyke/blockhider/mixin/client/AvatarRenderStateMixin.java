package kr.pyke.blockhider.mixin.client;

import kr.pyke.blockhider.client.state.TransformedRenderState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AvatarRenderState.class)
public class AvatarRenderStateMixin implements TransformedRenderState {
    @Unique private BlockState blockhider$transformedBlock;

    @Override public BlockState blockhider$getTransformedBlock() { return this.blockhider$transformedBlock; }

    @Override public void blockhider$setTransformedBlock(BlockState blockState) { this.blockhider$transformedBlock = blockState; }
}