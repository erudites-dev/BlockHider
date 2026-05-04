package kr.pyke.blockhider.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import kr.pyke.blockhider.client.renderer.TransformedPlayerRenderer;
import kr.pyke.blockhider.client.state.TransformedRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
    @Inject(method = "submit", at = @At("HEAD"), cancellable = true)
    private void blockhider$submit(EntityRenderState renderState, CameraRenderState camera, double x, double y, double z, PoseStack poseStack, SubmitNodeCollector output, CallbackInfo ci) {
        if (!(renderState instanceof TransformedRenderState transformed)) { return; }

        BlockState block = transformed.blockhider$getTransformedBlock();
        BlockPos blockPos = transformed.blockhider$getTransformedPos();
        if (block == null || blockPos == null) { return; }

        Vec3 cameraPos = camera.pos;
        double blockRelX = blockPos.getX() - cameraPos.x();
        double blockRelY = blockPos.getY() - cameraPos.y();
        double blockRelZ = blockPos.getZ() - cameraPos.z();

        TransformedPlayerRenderer.submitBlock(block, blockPos, blockRelX, blockRelY, blockRelZ, poseStack, output);

        if (!transformed.blockhider$isLocalPlayer()) {
            ci.cancel();
        }
    }
}