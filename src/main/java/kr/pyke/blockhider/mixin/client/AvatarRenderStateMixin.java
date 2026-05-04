package kr.pyke.blockhider.mixin.client;

import kr.pyke.blockhider.client.state.TransformedRenderState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AvatarRenderState.class)
public class AvatarRenderStateMixin implements TransformedRenderState {
    @Unique private BlockState blockhider$transformedBlock;
    @Unique private BlockPos blockhider$transformedPos;
    @Unique private boolean blockhider$isLocalPlayer;

    @Override public BlockState blockhider$getTransformedBlock() { return this.blockhider$transformedBlock; }
    @Override public BlockPos blockhider$getTransformedPos() { return this.blockhider$transformedPos; }
    @Override public boolean blockhider$isLocalPlayer() { return this.blockhider$isLocalPlayer; }

    @Override
    public void blockhider$setTransformedBlock(BlockState blockState, BlockPos blockPos) {
        this.blockhider$transformedBlock = blockState;
        this.blockhider$transformedPos = blockPos;
    }

    @Override
    public void blockhider$setLocalPlayer(boolean isLocalPlayer) {
        this.blockhider$isLocalPlayer = isLocalPlayer;
    }
}