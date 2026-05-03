package kr.pyke.blockhider.client.state;

import net.minecraft.world.level.block.state.BlockState;

public interface TransformedRenderState {
    BlockState blockhider$getTransformedBlock();
    void blockhider$setTransformedBlock(BlockState blockState);
}
