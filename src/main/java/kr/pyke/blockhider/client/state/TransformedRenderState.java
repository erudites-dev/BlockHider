package kr.pyke.blockhider.client.state;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public interface TransformedRenderState {
    BlockState blockhider$getTransformedBlock();
    BlockPos blockhider$getTransformedPos();
    boolean blockhider$isLocalPlayer();
    void blockhider$setTransformedBlock(BlockState blockState, BlockPos blockPos);
    void blockhider$setLocalPlayer(boolean isLocalPlayer);
}