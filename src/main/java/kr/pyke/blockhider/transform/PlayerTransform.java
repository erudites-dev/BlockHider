package kr.pyke.blockhider.transform;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.level.block.state.BlockState;

public interface PlayerTransform {
    BlockState blockhider$getTransformedBlock();
    BlockPos blockhider$getTransformedPos();
    void blockhider$setTransformedBlock(BlockState blockState, BlockPos blockPos);

    Interaction blockhider$getHitboxEntity();
    void blockhider$setHitboxEntity(Interaction entity);
}