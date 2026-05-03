package kr.pyke.blockhider.mixin.client;

import kr.pyke.blockhider.client.state.TransformedRenderState;
import kr.pyke.blockhider.transform.PlayerTransform;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin {
    @Inject(method = "extractRenderState*", at = @At("RETURN"))
    private void blockhider$extractRenderState(Avatar entity, AvatarRenderState state, float partialTicks, CallbackInfo ci) {
        ((TransformedRenderState) state).blockhider$setTransformedBlock(((PlayerTransform) entity).blockhider$getTransformedBlock());
    }
}