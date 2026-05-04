package kr.pyke.blockhider.client.state;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public interface TransformedRenderState {
    BlockState blockhider$getTransformedBlock();
    BlockPos blockhider$getTransformedPos();
    void blockhider$setTransformedBlock(BlockState blockState, BlockPos blockPos);
}