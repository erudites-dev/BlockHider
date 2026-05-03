package kr.pyke.blockhider.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TransformedPlayerRenderer {
    private static final double BLOCK_HORIZONTAL_OFFSET = 0.5d;
    private static final double BLOCK_VERTICAL_OFFSET = 1.d;

    private TransformedPlayerRenderer() { }

    public static void submitBlock(BlockState block, BlockPos blockPos, double x, double y, double z, PoseStack poseStack, SubmitNodeCollector output) {
        ClientLevel level = Minecraft.getInstance().level;

        MovingBlockRenderState renderState = new MovingBlockRenderState();
        renderState.blockState = block;
        renderState.blockPos = blockPos;
        renderState.randomSeedPos = blockPos;

        if (level != null) {
            renderState.lightEngine = level.getLightEngine();
            renderState.biome = level.getBiome(blockPos);
        }

        poseStack.pushPose();
        poseStack.translate(x - BLOCK_HORIZONTAL_OFFSET, y - BLOCK_VERTICAL_OFFSET, z - BLOCK_HORIZONTAL_OFFSET);

        output.submitMovingBlock(poseStack, renderState);

        poseStack.popPose();
    }
}
