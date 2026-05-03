package kr.pyke.blockhider.transform;

import net.minecraft.world.level.block.state.BlockState;

public interface PlayerTransform {
    BlockState blockhider$getTransformedBlock();
    void blockhider$setTransformedBlock(BlockState blockState);
}
