package kr.pyke.blockhider.mixin.server;

import kr.pyke.blockhider.game.GameManager;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
    @Inject(method = "tick", at = @At("RETURN"))
    public void blockhider$tick(CallbackInfo ci) {
        GameManager.getInstance().tickPlayer((ServerPlayer)(Object)this);
    }
}
