package kr.pyke.blockhider.mixin.server;

import kr.pyke.blockhider.game.GameManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodData.class)
public class FoodDataMixin {
    @Shadow private int foodLevel;

    @Inject(method = "tick", at = @At("HEAD"))
    private void blockhider$fixFoodLevel(ServerPlayer player, CallbackInfo ci) {
        if (GameManager.getInstance().isRunning()) {
            this.foodLevel = 19;
        }
    }
}
