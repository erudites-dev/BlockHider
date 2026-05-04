package kr.pyke.blockhider.mixin.server;

import kr.pyke.blockhider.transform.PlayerTransform;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Player.class)
public abstract class PlayerMixin implements PlayerTransform {
    @Unique private BlockState blockhider$transformedBlock;
    @Unique private BlockPos blockhider$transformedPos;

    @Override public BlockState blockhider$getTransformedBlock() { return this.blockhider$transformedBlock; }

    @Override public BlockPos blockhider$getTransformedPos() { return this.blockhider$transformedPos; }


    @Override
    public void blockhider$setTransformedBlock(BlockState blockState, BlockPos pos) {
        if (blockState == this.blockhider$transformedBlock && pos == this.blockhider$transformedPos) { return; }

        this.blockhider$transformedBlock = blockState;
        this.blockhider$transformedPos = pos;
        ((Player) (Object) this).refreshDimensions();
    }
}
