package kr.pyke.blockhider.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import kr.pyke.blockhider.transform.PlayerTransform;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TransformedOutlineRenderer {
    private static final float LINE_WIDTH = 2.f;

    private TransformedOutlineRenderer() { }

    public static void render(LevelRenderContext context) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        LocalPlayer player = mc.player;
        if (level == null || player == null) { return; }

        BlockState block = ((PlayerTransform) player).blockhider$getTransformedBlock();
        BlockPos blockPos = ((PlayerTransform) player).blockhider$getTransformedPos();
        if (block == null || blockPos == null) { return; }

        PoseStack poseStack = context.poseStack();
        MultiBufferSource.BufferSource bufferSource = context.bufferSource();
        Vec3 camPos = context.levelState().cameraRenderState.pos;

        VoxelShape shape = block.getShape(level, blockPos, CollisionContext.of(player));
        if (shape.isEmpty()) { return; }

        VertexConsumer consumer = bufferSource.getBuffer(RenderTypes.lines());

        ShapeRenderer.renderShape(poseStack, consumer, shape, blockPos.getX() - camPos.x(), blockPos.getY() + 1 - camPos.y(), blockPos.getZ() - camPos.z(), ARGB.colorFromFloat(1.f, 1.f, 1.f, 1.f), LINE_WIDTH);

        bufferSource.endBatch(RenderTypes.lines());
    }
}