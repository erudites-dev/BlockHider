package kr.pyke.blockhider.mixin.client;

import kr.pyke.blockhider.client.state.ClientGameState;
import kr.pyke.blockhider.client.state.TransformedRenderState;
import kr.pyke.blockhider.transform.PlayerTransform;
import kr.pyke.blockhider.type.GAME_STATE;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin {
    @Unique private static final int GLOW_OUTLINE_COLOR = 0xFFFFFFFF;

    @Inject(method = "extractRenderState*", at = @At("RETURN"))
    private void blockhider$extractRenderState(Avatar entity, AvatarRenderState state, float partialTicks, CallbackInfo ci) {
        PlayerTransform transform = (PlayerTransform) entity;
        TransformedRenderState transformed = (TransformedRenderState) state;
        transformed.blockhider$setTransformedBlock(transform.blockhider$getTransformedBlock(), transform.blockhider$getTransformedPos());

        LocalPlayer localPlayer = Minecraft.getInstance().player;
        boolean isLocal = localPlayer != null && entity == localPlayer;
        transformed.blockhider$setLocalPlayer(isLocal);

        if (isLocal && transform.blockhider$getTransformedBlock() != null) {
            state.outlineColor = GLOW_OUTLINE_COLOR;
            state.isInvisible = true;
        }
        else if (!isLocal && localPlayer != null && localPlayer.isSpectator() && transform.blockhider$getTransformedBlock() != null) {
            state.isInvisible = true;
        }
    }

    @Inject(method = "shouldShowName*", at = @At("HEAD"), cancellable = true)
    private void blockhider$shouldShowName(Avatar entity, double distanceToCameraSq, CallbackInfoReturnable<Boolean> cir) {
        if (ClientGameState.getState() != GAME_STATE.WAITING) {
            cir.setReturnValue(false);
        }
    }
}