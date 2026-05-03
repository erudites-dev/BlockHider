package kr.pyke.blockhider.mixin.server;

import kr.pyke.blockhider.transform.PlayerTransform;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Player.class)
public abstract class PlayerMixin implements PlayerTransform {
    @Unique private BlockState blockhider$transformedBlock;

    @Override public BlockState blockhider$getTransformedBlock() { return this.blockhider$transformedBlock; }

    @Override
    public void blockhider$setTransformedBlock(BlockState blockState) {
        if (blockState == this.blockhider$transformedBlock) { return; }

        this.blockhider$transformedBlock = blockState;
        ((Player) (Object) this).refreshDimensions();
    }
}
